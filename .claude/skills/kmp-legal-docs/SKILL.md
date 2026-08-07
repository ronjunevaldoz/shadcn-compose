---
name: kmp-legal-docs
description: >
  Lawyer agent for KMP apps: generates Privacy Policy and Terms & Conditions tailored
  to the data your app actually collects. Covers Google Play data safety section, App
  Store privacy nutrition labels, GDPR (EU), CCPA (California), and in-app display via
  a CMP composable. Produces web-ready markdown and a KMP WebView/ScrollView screen for
  showing the docs inside the app. Does NOT provide legal advice — output is a
  best-practice template that must be reviewed by a qualified lawyer before publishing.
license: Apache-2.0
metadata:
  author: kmp-agent-skills
  last-updated: '2026-06-21'
  keywords:
    - privacy policy
    - terms and conditions
    - terms of service
    - GDPR
    - CCPA
    - Google Play data safety
    - App Store privacy labels
    - legal docs
    - KMP legal
    - data collection disclosure
    - app store compliance
    - privacy screen
    - in-app terms
    - cookie policy
---

## When to Use This Skill

Use this skill when you need to:
- Generate a Privacy Policy for a KMP app (Android, iOS, Web)
- Generate Terms & Conditions / Terms of Service
- Fill in the Google Play **Data Safety** section
- Fill in the App Store **App Privacy** (privacy nutrition labels)
- Add GDPR or CCPA compliance disclosures
- Build an in-app screen that displays the Privacy Policy or Terms
- Add a "Terms & Privacy" consent flow at first launch

Do NOT use this skill when:
- You need advice on data law specific to your jurisdiction — consult a qualified lawyer
- You need a cookie consent banner for a web-only product — this skill targets KMP apps
- Your app is in a highly regulated domain (healthcare, finance, children under 13) — those require specialist legal review beyond what templates can provide

**Trigger keywords:** privacy policy, terms and conditions, terms of service, GDPR, CCPA,
data safety, App Store privacy, legal docs, user data disclosure, consent screen,
app compliance, play store legal, privacy screen.

**Freshness rule:** Google Play data safety categories and App Store privacy label types
change with platform updates — recheck the [Google Play policy](https://support.google.com/googleplay/android-developer/answer/10787469)
and [App Store privacy details](https://developer.apple.com/app-store/app-privacy-details/)
pages before filling in store listings.

> **Legal disclaimer:** Output from this skill is a template based on common app
> patterns and publicly available platform requirements. It is NOT legal advice. Have
> the generated documents reviewed by a qualified lawyer licensed in your jurisdiction
> before publishing.

---

## Recommendation First

Default to this sequence:
1. **Auto-scan** — run `detect_data_collection.py` against the project first (Step 1a). This replaces most of the manual questionnaire.
2. **Fill gaps** — ask only the questions the scanner cannot answer (Step 1b)
3. **Generate** — produce Privacy Policy + Terms in markdown (Steps 2–3)
4. **Map to stores** — fill in the Google Play Data Safety answers and App Store privacy label rows (Step 4)
5. **Add to app** — wire a `LegalDocsScreen` composable that loads the docs from a URL or embedded string (Step 5)
6. **Wire into CI** — run `detect_data_collection.py --policy` in `./gradlew check` so new SDK additions are caught automatically (Step 6)

Generate both documents in one pass. Do not generate Privacy Policy without Terms (or vice versa) — stores require both.

---

## Step 1a: Auto-Detect from the Project

Run the bundled scanner first. It inspects `*.kt`, `*.swift`, `*.xml`, `libs.versions.toml`,
and `AndroidManifest.xml` for known SDK imports, permission declarations, and class usages.

```bash
# Scan only — show what data types the code collects:
python3 skills/kmp-legal-docs/scripts/detect_data_collection.py <project_root>

# Compare against an existing policy and show gaps + conflicts:
python3 skills/kmp-legal-docs/scripts/detect_data_collection.py <project_root> \
  --policy docs/privacy_policy.md

# JSON output for agent consumption:
python3 skills/kmp-legal-docs/scripts/detect_data_collection.py <project_root> --json
```

**What the scanner detects automatically:**

| Data type | Evidence it looks for |
|---|---|
| Analytics | `FirebaseAnalytics`, `Amplitude`, `Mixpanel`, `logEvent`, `AnalyticsEvent` |
| Crash reporting | `Crashlytics`, `Sentry`, `recordException` |
| Push notifications | `FirebaseMessaging`, `fcmToken`, `UNUserNotificationCenter` |
| Precise location | `ACCESS_FINE_LOCATION`, `FusedLocationProviderClient` |
| Approximate location | `ACCESS_COARSE_LOCATION` |
| Camera | `CAMERA` permission, `CameraX`, `AVCaptureSession` |
| Microphone | `RECORD_AUDIO`, `AudioRecord`, `AVAudioSession` |
| Biometric | `BiometricPrompt`, `LAContext`, `BiometricAuthenticator` |
| Advertising ID | `AdvertisingIdClient`, `IDFA`, `ASIdentifierManager` |
| Google Sign-In | `GoogleSignIn`, `BeginSignInRequest` |
| Apple Sign-In | `ASAuthorizationAppleIDProvider` |
| In-app purchases | `BillingClient`, `SKProductsRequest`, `StoreKit` |
| Stripe payments | `PaymentSheet`, `stripe` dependency |
| Photo library | `READ_MEDIA_IMAGES`, `PHPhotoLibrary` |
| Health data | `HealthConnect`, `HKHealthStore` |
| Contacts | `READ_CONTACTS`, `CNContactStore` |
| Account / email | `FirebaseAuth`, `signInWithEmailAndPassword` |

**Output sections:**

- `DETECTED` — data types found in the codebase, with file:line evidence
- `⚠️ GAPS` — detected in code but **not disclosed** in the existing policy → add disclosures
- `ℹ️ CONFLICTS` — disclosed in the policy but **not detected** in code → remove or verify these sections
- Exit code `0` = no gaps; `1` = gaps found (CI can fail on this)

**What the scanner cannot detect** (still needs the manual questionnaire from Step 1b):
- Third-party SDKs that use obfuscated or indirect data collection
- Data collected server-side (your backend, not the app)
- Whether data is "linked to the user" or anonymous (Google Play / App Store distinction)
- Jurisdiction: which markets the app targets (EU → GDPR, California → CCPA)
- Data retention periods and deletion policy

---

## Step 1b: Manual Questionnaire (gaps only)

After running the scanner, ask only the questions it cannot answer:

- **Jurisdiction**: EU/EEA? California USA? Both? Other?
- **Server-side data**: does your backend store anything the app sends that the scanner wouldn't see?
- **Third-party sharing**: is any data shared with parties beyond the detected SDKs? (ad networks, data brokers)
- **Retention & deletion**: how long is data kept? Can users request deletion?
- **Linked to user**: for analytics/crash data — is it linked to a user identity or anonymous?
- **Children**: is the app directed at children under 13?

---

## Step 1: Data Collection Questionnaire

**Ask all of these before writing a single sentence of the policy.** Answers determine every disclosure requirement.

### 1a. Account & Identity
- Does the app require an account? (`yes` / `no`)
- What login methods? (`email+password`, `Google Sign-In`, `Sign in with Apple`, `Facebook`, `phone number`, `anonymous/guest`)
- What profile data is stored? (`name`, `email`, `avatar`, `display name`, `bio`)

### 1b. Usage & Analytics
- Is any analytics SDK integrated? (`Firebase Analytics`, `Amplitude`, `Mixpanel`, `custom`, `none`)
- Is crash reporting enabled? (`Firebase Crashlytics`, `Sentry`, `none`)
- Is session recording used? (`FullStory`, `Hotjar`, `none`)

### 1c. Device & Identifiers
- Does the app read device identifiers? (`Android Advertising ID (GAID)`, `Apple IDFA`, `neither`, `both`)
- Does the app read device model / OS version for diagnostics? (`yes` / `no`)

### 1d. Location
- Does the app access location? (`precise (GPS)`, `approximate (network)`, `no`)
- Is location used in the background? (`yes` / `no`)

### 1e. Communications
- Does the app send push notifications? (`yes` / `no`)
- Does it access contacts, SMS, or call log? (`yes` / `no`)

### 1f. Media & Sensors
- Does the app access camera or microphone? (`camera`, `microphone`, `both`, `neither`)
- Does it access the photo library? (`read`, `write`, `both`, `no`)

### 1g. Payments & Financial
- Does the app handle payments? (`Google Play Billing (IAP)`, `Apple IAP`, `Stripe`, `PayPal`, `none`)
- Note: if using Play Billing or Apple IAP, the store processes the transaction — the app itself does not receive payment card data.

### 1h. Health & Sensitive Data
- Does the app collect health or fitness data? (`yes` / `no`)
- Biometric authentication (fingerprint / Face ID)? (`yes` / `no`)

### 1i. Third-Party Sharing
- Is any data shared with third parties beyond what is needed for the features above? (`yes — list them` / `no`)
- Which advertising networks are used? (`AdMob`, `Meta Audience Network`, `none`)

### 1j. Retention & Deletion
- How long is user data retained? (e.g. `until account deletion`, `90 days after last use`)
- Can users request deletion? (`yes — in-app`, `yes — via email`, `no`)

### 1k. Jurisdiction
- Primary markets? (`EU/EEA` → GDPR required, `California USA` → CCPA required, `both`, `other`)
- Is the app directed at children under 13? (`yes` → COPPA required — stop and consult a lawyer)

---

## Step 2: Privacy Policy Template

Full content: `references/step2-privacy-policy-template.md`.

## Step 3: Terms & Conditions Template

Full content: `references/step3-terms-conditions-template.md`.

## Step 4: Store Listing Answers

### Google Play — Data Safety Section

Use the questionnaire answers from Step 1 to fill in each row:

| Data type | Collected? | Shared? | Required? | User can delete? |
|---|---|---|---|---|
| Name | ✅ if account | Per Step 1i | No — optional | ✅ if Step 1j = yes |
| Email address | ✅ if account | Per Step 1i | No | ✅ |
| User IDs | ✅ if account | No | No | ✅ |
| Crash logs | ✅ if Crashlytics | To Crashlytics | No | N/A |
| Diagnostics | ✅ if analytics | To Analytics | No | N/A |
| App interactions | ✅ if analytics | To Analytics | No | N/A |
| Device or other IDs | Per Step 1c | Per Step 1i | No | N/A |
| Precise location | Per Step 1d | Per Step 1i | No | N/A |
| Photos / videos | Per Step 1f | No | No | N/A |
| Financial info | Per Step 1g | No | No | N/A |

**Data collection practices to answer:**
- "Is data encrypted in transit?" → Yes (HTTPS / TLS)
- "Can users request deletion?" → Yes / No (per Step 1j)
- "Is data collected required or optional?" → Required only for core features; analytics is optional

### App Store — App Privacy Details

Map questionnaire answers to Apple's categories:

| Category | Sub-type | Purpose | Linked to user? |
|---|---|---|---|
| Contact info → Email | Account info | App functionality | Yes |
| Contact info → Name | Account info | App functionality | Yes |
| Identifiers → User ID | Account info | App functionality | Yes |
| Identifiers → Device ID | Analytics | Analytics | No |
| Usage data → Product interaction | Analytics | Analytics | No |
| Diagnostics → Crash data | Diagnostics | App functionality | No |
| Location → Precise location | Per Step 1d | App functionality | Yes/No |
| Purchases → Purchase history | Per Step 1g | App functionality | Yes |

---

## Step 5: In-App Legal Docs Screen

Full content: `references/step5-in-app-legal-docs-screen.md`.

## Step 6: Web Deployment

Host the markdown files as static HTML at `WEBSITE_URL/privacy` and `WEBSITE_URL/terms`.

Quick options:
- **GitHub Pages** — commit `docs/privacy.md` and `docs/terms.md`; enable Pages from `/docs`
- **Netlify / Vercel** — add a static site with MDX or plain HTML wrappers
- **Firebase Hosting** — `firebase deploy --only hosting`

The URL must be publicly accessible (no login) for both stores to accept it.

---

## Step 6: CI Gate — Keep Policy in Sync with Code

Add `detect_data_collection.py` to your CI pipeline so that adding a new SDK (analytics,
location, biometric) that is not yet disclosed in the policy causes the build to fail.

### GitHub Actions step (add to `.github/workflows/ci.yml`):

```yaml
- name: Check privacy policy coverage
  run: |
    python3 skills/kmp-legal-docs/scripts/detect_data_collection.py . \
      --policy docs/privacy_policy.md
  # Exit code 1 = gaps found → build fails
```

### What triggers a CI failure:

| Scenario | What happens |
|---|---|
| New analytics SDK added, policy not updated | `GAPS: analytics` → exit 1 → CI fails |
| Firebase removed, policy still mentions it | `CONFLICTS: analytics` → warning only (exit 0) |
| Policy and code in sync | No gaps, no conflicts → exit 0 |

**Workflow when CI fails on gaps:**

1. CI reports `⚠️ GAPS — detected in code but NOT disclosed in policy`
2. Developer runs `/kmp-release-notes` to draft the policy update
3. Developer updates `docs/privacy_policy.md` and bumps `POLICY_VERSION` in `gradle.properties`
4. CI passes on next push

This closes the loop between feature development and legal compliance — no more shipping analytics without disclosing it.

---

## Testing

```kotlin
class LegalDocsScreenTest {
    @Test
    fun `privacy policy screen shows correct title`() {
        // Compose test
        composeTestRule.setContent {
            LegalDocsScreen(docType = LegalDocType.PRIVACY_POLICY, onBack = {})
        }
        composeTestRule.onNodeWithText("Privacy Policy").assertIsDisplayed()
    }

    @Test
    fun `terms screen shows correct title`() {
        composeTestRule.setContent {
            LegalDocsScreen(docType = LegalDocType.TERMS_AND_CONDITIONS, onBack = {})
        }
        composeTestRule.onNodeWithText("Terms & Conditions").assertIsDisplayed()
    }

    @Test
    fun `consent screen shows both links`() {
        composeTestRule.setContent { ConsentScreen(onAccept = {}) }
        composeTestRule.onNodeWithText("Terms & Conditions").assertIsDisplayed()
        composeTestRule.onNodeWithText("Privacy Policy").assertIsDisplayed()
        composeTestRule.onNodeWithText("I agree — Continue").assertIsDisplayed()
    }
}
```

---

## Common Anti-Patterns

- **Publishing without lawyer review** — this skill generates best-practice templates;
  laws vary by jurisdiction and platform. A qualified lawyer must review before you publish.
- **Hardcoding the privacy policy text in the app** — use a remote URL so you can update
  the policy without shipping a new build. Only embed as a last-resort fallback.
- **One policy for all platforms** — Privacy Policy and Terms live at a URL; the same URL
  works for Android, iOS, and web. Do not maintain separate copies.
- **Forgetting to update the store listing after changing the policy** — when the policy
  URL or content changes materially, re-submit the Google Play Data Safety section and
  App Store App Privacy details.
- **Missing GDPR section for EU users** — if your app is available in the EU/EEA,
  the GDPR section is legally required. The `primary markets` field in Step 1 determines this.
- **No version pinning on the consent gate** — store the policy version (e.g. `"1.1"`)
  not just `true`/`false`. When the policy changes, increment the version so existing
  users see the updated consent screen.
- **Skipping the Data Safety / App Privacy form** — incomplete store forms trigger review
  delays and can cause app rejection. Complete Step 4 before every release.

---

## References

Full template content lives in `references/*.md`: `step2-privacy-policy-template`,
`step3-terms-conditions-template`, `step5-in-app-legal-docs-screen`. Load the specific
file named in the pointer under its matching heading above, not all of them.

---

## Related Skills

- `kmp-flavor-environment` — store `PRIVACY_POLICY_URL` and `TERMS_URL`
  per environment via `BuildKonfig`; dev can point to a staging version
- `kmp-datastore` — persist the accepted policy version for the consent gate
- `kmp-navigation` — add `LegalDocsScreen` and `ConsentScreen` routes to the nav graph
- `kmp-feature-scaffold` — `LegalDocsScreen` lives in `:feature:settings:ui`
  or `:core:ui`; add the route after scaffolding

---

## Output Style

When asked to generate legal documents, respond in this order:
1. Confirm the questionnaire answers (Step 1) — ask any missing ones before generating
2. Output Privacy Policy in a markdown code block
3. Output Terms & Conditions in a markdown code block
4. Output the Google Play Data Safety table (Step 4a)
5. Output the App Store App Privacy table (Step 4b)
6. Offer to add the `LegalDocsScreen` composable (Step 5)

Always include the legal disclaimer at the top of both documents.
Never generate a policy without first completing the questionnaire — a generic policy
that does not match what the app actually collects is worse than no policy.

---

## Changelog

| Date | Change |
|---|---|
| 2026-08-04 | Split Steps 2, 3, and 5 (Privacy Policy template, Terms & Conditions template, In-App Legal Docs Screen — 292 lines combined) out of SKILL.md into `references/*.md`, leaving pointer stubs plus a new References section. SKILL.md drops from 796 to 392 lines, clearing the agentskills.io 500-line recommendation. No content removed, only relocated. Part of the same backlog cleanup as `kmp-compose-design-system`/`-extended`/`kmp-mvi`/`kmp-feature-scaffold`/`kmp-code-quality`/`kmp-library-publishing`/`kmp-expert`/`kmp-navigation` (KI-008). |
| 2026-06-21 | **Improved** — `detect_data_collection.py` scanner added: auto-detects 17 data types from project source; gap/conflict analysis against existing policy; CI gate via exit code; Step 6 CI integration guide added. |
| 2026-06-21 | **Improved** — Consent gate explained with version-pinning pattern; `ConsentViewModel` with `StateFlow` added; `POLICY_VERSION` in `gradle.properties` → `BuildKonfig`. |
| 2026-06-21 | Initial release — Privacy Policy + T&C templates, Google Play Data Safety, App Store privacy labels, GDPR/CCPA sections, in-app `LegalDocsScreen`, consent gate, web deployment guide. |
