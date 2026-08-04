# Audio DSP Pitfalls in JNI Pipelines

Common mistakes when implementing or porting audio reconstruction (ISTFT, OLA, resampling)
in a JNI C++ wrapper. These apply to any pipeline that converts spectrogram embeddings to
PCM audio — WavTokenizer, EnCodec, DAC, and similar vocoders.

---

## Pitfall 1 — Symmetric vs periodic Hann window

A Hann window has two forms:

```
Symmetric (length = n):   w[i] = 0.5 * (1 - cos(2π*i / (n-1)))   ← for filters
Periodic  (length = n):   w[i] = 0.5 * (1 - cos(2π*i / n))       ← for STFT/OLA
```

For Overlap-Add reconstruction, the **periodic** form is required. It satisfies the
Constant Overlap-Add (COLA) condition: the sum of overlapping windows equals a constant.
The symmetric form does NOT satisfy COLA — the normalisation denominator `norm[i]` varies
across the buffer, introducing amplitude ripple in the reconstructed waveform.

**Check**: if you copy a Hann window from a signal-processing textbook, it is likely
symmetric. Verify against your STFT library's reference implementation.

---

## Pitfall 2 — IRFFT normalisation factor

For a one-sided spectrum with `N_FFT = 1280` bins, the IRFFT has `N = N_FFT/2 + 1 = 641`
complex input bins. The correct normalisation divides by `N = 641`, not by `N_FFT = 1280`.

```cpp
// CORRECT — divide by number of complex input bins
out_real[i] = real_output[i] / N;   // N = n/2 + 1

// WRONG — divides by output length
out_real[i] = real_output[i] / n;   // n = N_FFT
```

The difference is a factor of 2×. Output amplitude will be half what the reference
produces. When combined with a hard clip at ±1.0, this means the clip fires for signals
that are 2× larger than expected — causing distortion at lower actual amplitudes than
the reference would clip at.

---

## Pitfall 3 — Hard float clip before peak normalisation

Do NOT do this:
```cpp
result.push_back(std::max(-1.0f, std::min(1.0f, v)));  // WRONG
```

Vocoder embeddings produce ISTFT output that can legitimately exceed ±1.0 in absolute
amplitude. Clipping at the float level shears loud frames into square waves before
conversion to int16. The correct approach is:

```cpp
// Collect raw values
std::vector<float> audio = istft(embd);

// Peak normalise if needed (reference implementations typically don't — ISTFT output
// is naturally bounded for correct embeddings; clip only at int16 conversion)
float peak = *std::max_element(audio.begin(), audio.end(),
    [](float a, float b){ return std::abs(a) < std::abs(b); });
// if (peak > 1.0f) for (float& s : audio) s /= peak;  // optional peak norm

// int16 conversion is the only clip needed
int16_t sample = static_cast<int16_t>(std::clamp(s * 32767.0f, -32768.0f, 32767.0f));
```

**Diagnostic**: if `ffprobe -af volumedetect` reports `max_volume = -0.0 dB` on EVERY
output file, regardless of content, the float-domain clip is firing.

---

## Pitfall 4 — Missing lead silence / vocoder transient

Vocoders produce a startup transient (click or pop) in the first ~0.25 seconds of
reconstructed audio. This is a known artefact of the overlap-add warm-up — the first
few frames have partial overlap accumulation that does not represent real signal content.

**Fix**: zero the first `sample_rate / 4` samples of the float output before conversion:
```cpp
constexpr int LEAD_SILENCE = sample_rate / 4;  // 6000 @ 24kHz
std::fill(audio.begin(), audio.begin() + std::min(LEAD_SILENCE, (int)audio.size()), 0.0f);
```

Always check the reference implementation for this step — it is easy to miss because
it is a post-processing step after the main OLA loop.

---

## Pitfall 5 — Wrong embedding dimension (n_embd vs n_embd_out)

Encoder models (vocoders, codecs) expose two embedding dimensions:
- `n_embd` — the internal/hidden dimension of the transformer
- `n_embd_out` — the output dimension of the encoder (what `get_embeddings()` returns)

These are often different. Using `n_embd` as the per-frame stride when indexing
`get_embeddings()` output shifts every frame by a fixed offset, corrupting all audio.

```cpp
// WRONG
const int n_embd = model_n_embd(model);         // hidden dim, not output stride

// CORRECT
const int n_embd = model_n_embd_out(model);     // output dim = actual embedding width
```

**Check**: read the reference implementation's embedding loop. Find which dimension
function it passes to `embd[frame * n_embd + k]`. Use that exact function.

---

## Pitfall 6 — Embedding layout assumptions

Vocoder embeddings are often packed as interleaved real/imaginary parts:
```
frame[0..n_embd/2)    = log-magnitude per bin
frame[n_embd/2..n_embd) = phase angle per bin
```

If you assume a different layout (e.g., all magnitudes then all phases across frames,
or complex pairs), every frame will be decoded with the wrong spectrum. The output will
be non-silent but completely wrong spectrally.

**Rule**: do not assume the embedding layout. Read the reference `embd_to_audio` function
and copy the indexing arithmetic verbatim.

---

## Correctness checklist for audio JNI wrappers

- [ ] Hann window uses periodic form (`denominator = n`, not `n-1`)
- [ ] IRFFT divides by `N = n/2+1`, not by `n`
- [ ] No hard float clip; clip only at int16 conversion boundary
- [ ] Lead silence zeroed for the first `sample_rate/4` samples
- [ ] Embedding stride uses `n_embd_out`, not `n_embd`
- [ ] Embedding layout copied verbatim from reference (mag/phase split index)
- [ ] GPU synchronise called before reading embeddings
- [ ] Output sample rate matches the vocoder's baked-in rate (not assumed 16kHz or 22kHz)
