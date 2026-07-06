---
name: kotlin-multiplatform-legal-docs
description: >
  Lawyer agent for KMP apps: generates Privacy Policy and Terms & Conditions tailored
  to the data your app actually collects. Covers Google Play data safety section, App
  Store privacy nutrition labels, GDPR (EU), CCPA (California), and in-app display via
  a CMP composable. Produces web-ready markdown and a KMP WebView/ScrollView screen for
  showing the docs inside the app. Does NOT provide legal advice — output is a
  best-practice template that must be reviewed by a qualified lawyer before publishing.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
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
python3 skills/kotlin-multiplatform-legal-docs/scripts/detect_data_collection.py <project_root>

# Compare against an existing policy and show gaps + conflicts:
python3 skills/kotlin-multiplatform-legal-docs/scripts/detect_data_collection.py <project_root> \
  --policy docs/privacy_policy.md

# JSON output for agent consumption:
python3 skills/kotlin-multiplatform-legal-docs/scripts/detect_data_collection.py <project_root> --json
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

Fill the placeholders (`APP_NAME`, `COMPANY_NAME`, `CONTACT_EMAIL`, `EFFECTIVE_DATE`, `WEBSITE_URL`) from the questionnaire answers. Remove entire sections that do not apply to the app.

```markdown
# Privacy Policy

**Effective date:** EFFECTIVE_DATE  
**Last updated:** LAST_UPDATED_DATE

This Privacy Policy explains how COMPANY_NAME ("we", "us", "our") collects, uses, and
shares information about you when you use APP_NAME ("the App").

---

## 1. Information We Collect

### 1a. Information you provide
<!-- Include if account exists -->
- **Account information:** name, email address, and profile picture when you register
  or sign in with Google / Apple / email.
<!-- Include if payments exist (non-IAP) -->
- **Payment information:** processed by [Stripe / PayPal]; we do not store your card number.

### 1b. Information collected automatically
<!-- Include if analytics exist -->
- **Usage data:** screens visited, features used, session duration, and in-app actions,
  collected via [Firebase Analytics / Amplitude].
<!-- Include if crash reporting exists -->
- **Crash reports:** device model, OS version, app version, and stack traces collected
  via [Firebase Crashlytics / Sentry] when the App crashes.
<!-- Include if location exists -->
- **Location data:** [precise GPS / approximate network] location when you [use X feature].
  <!-- If background location: -->
  The App accesses your location in the background to [reason].
<!-- Include if device identifiers exist -->
- **Device identifiers:** Android Advertising ID or Apple IDFA for [analytics / advertising].

### 1c. Information from third parties
<!-- Include if social login exists -->
- **Social login:** when you sign in with Google or Apple, we receive your name and
  email address from that provider.

---

## 2. How We Use Your Information

We use the information we collect to:
- Provide, operate, and improve the App
<!-- Include if analytics exist -->
- Understand how users interact with the App and fix issues
<!-- Include if push exists -->
- Send you push notifications (you can opt out in device settings)
<!-- Include if location exists -->
- Provide [location-based feature name]
<!-- Include if payments exist -->
- Process payments and prevent fraud
- Respond to your support requests

---

## 3. How We Share Your Information

We do not sell your personal information.

We share information with:
<!-- Include if analytics SDK exists -->
- **[Firebase / Amplitude / Mixpanel]** — to provide analytics services. Their privacy
  policy: [link].
<!-- Include if crash reporting exists -->
- **[Crashlytics / Sentry]** — to provide crash reporting. Their privacy policy: [link].
<!-- Include if AdMob / ads exist -->
- **[AdMob / Meta]** — to display advertisements. Their privacy policy: [link].
- **Legal authorities** — when required by law or to protect our rights.

---

## 4. Data Retention

We retain your personal information for as long as your account is active or as needed
to provide the App. You may request deletion of your account and associated data by
[emailing CONTACT_EMAIL / using the in-app "Delete account" feature]. We will process
deletion requests within 30 days.

---

## 5. Your Rights

<!-- Include if EU/EEA -->
### 5a. GDPR (EU / EEA residents)
If you are in the EU or EEA, you have the right to:
- **Access** — request a copy of the data we hold about you
- **Rectification** — correct inaccurate data
- **Erasure** — request deletion of your data ("right to be forgotten")
- **Portability** — receive your data in a machine-readable format
- **Objection** — object to processing based on legitimate interest
- **Withdraw consent** — where processing is based on consent

To exercise these rights, contact us at CONTACT_EMAIL. You may also lodge a complaint
with your local supervisory authority.

<!-- Include if California USA -->
### 5b. CCPA (California residents)
California residents have the right to:
- **Know** — request disclosure of the categories and specific pieces of personal
  information we have collected, the sources, our business purposes, and the categories
  of third parties we share it with
- **Delete** — request deletion of your personal information (subject to exceptions)
- **Opt out of sale** — we do not sell personal information
- **Non-discrimination** — we will not discriminate against you for exercising these rights

To submit a request, contact us at CONTACT_EMAIL.

---

## 6. Children's Privacy

The App is not directed to children under 13. We do not knowingly collect personal
information from children under 13. If you believe we have collected such information,
contact us at CONTACT_EMAIL.

---

## 7. Security

We use industry-standard measures to protect your information, including TLS in transit
and encryption at rest. No method of transmission over the internet is 100% secure.

---

## 8. Changes to This Policy

We will notify you of material changes by updating the "Last updated" date above and,
where appropriate, via an in-app notification. Continued use of the App after changes
constitutes acceptance.

---

## 9. Contact Us

COMPANY_NAME  
CONTACT_EMAIL  
WEBSITE_URL
```

---

## Step 3: Terms & Conditions Template

```markdown
# Terms & Conditions

**Effective date:** EFFECTIVE_DATE  
**Last updated:** LAST_UPDATED_DATE

By downloading or using APP_NAME, you agree to these Terms. If you do not agree,
do not use the App.

---

## 1. License

COMPANY_NAME grants you a limited, non-exclusive, non-transferable, revocable licence
to use the App for personal, non-commercial purposes on devices you own or control,
subject to these Terms.

---

## 2. User Accounts
<!-- Include if accounts exist -->
You are responsible for maintaining the confidentiality of your credentials. You agree
to notify us immediately at CONTACT_EMAIL if you suspect unauthorised access.

We may suspend or terminate your account if you violate these Terms.

---

## 3. Acceptable Use

You agree not to:
- Reverse-engineer, decompile, or disassemble the App
- Use the App to transmit harmful, illegal, or infringing content
- Attempt to gain unauthorised access to our systems
- Use automated tools to scrape or overload the App

---

## 4. In-App Purchases
<!-- Include if IAP exists -->
The App offers optional purchases processed by [Google Play / Apple App Store].
All purchases are final except where required by law. Contact the relevant store
for refund requests.

---

## 5. Intellectual Property

All content, trademarks, and data in the App are the property of COMPANY_NAME or
its licensors. You may not reproduce or distribute them without written permission.

---

## 6. Disclaimer of Warranties

The App is provided "as is" without warranties of any kind. We do not warrant that
the App will be error-free, uninterrupted, or meet your requirements.

---

## 7. Limitation of Liability

To the fullest extent permitted by law, COMPANY_NAME shall not be liable for any
indirect, incidental, special, or consequential damages arising from your use of
the App.

---

## 8. Governing Law

These Terms are governed by the laws of [YOUR JURISDICTION], without regard to
conflict of law principles.

---

## 9. Changes to Terms

We may update these Terms. Material changes will be communicated via in-app
notification or email. Continued use after changes constitutes acceptance.

---

## 10. Contact

COMPANY_NAME  
CONTACT_EMAIL  
WEBSITE_URL
```

---

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

Add a `LegalDocsScreen` to `:core:ui` or `:feature:settings:ui`. The screen loads the
documents from a remote URL (so they can be updated without an app release) with an
embedded fallback.

### `LegalDocsScreen.kt` — in `:feature:settings:ui` or `:core:ui`

```kotlin
enum class LegalDocType { PRIVACY_POLICY, TERMS_AND_CONDITIONS }

@Composable
fun LegalDocsScreen(
    docType: LegalDocType,
    onBack: () -> Unit,
) {
    val title = when (docType) {
        LegalDocType.PRIVACY_POLICY        -> "Privacy Policy"
        LegalDocType.TERMS_AND_CONDITIONS  -> "Terms & Conditions"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LegalDocsContent(
            docType = docType,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
fun LegalDocsContent(
    docType: LegalDocType,
    modifier: Modifier = Modifier,
) {
    // Load from remote URL; fall back to embedded markdown
    val url = when (docType) {
        LegalDocType.PRIVACY_POLICY       -> AppConfig.privacyPolicyUrl
        LegalDocType.TERMS_AND_CONDITIONS -> AppConfig.termsUrl
    }

    // Use expect/actual WebView on each platform, or a simple scrollable Text for simple docs
    PlatformWebView(url = url, modifier = modifier.fillMaxSize())
}
```

### `AppConfig` additions

```kotlin
// Add to AppConfig in commonMain:
object AppConfig {
    // ... existing fields ...
    val privacyPolicyUrl: String  get() = BuildKonfig.PRIVACY_POLICY_URL
    val termsUrl: String          get() = BuildKonfig.TERMS_URL
}
```

### `gradle.properties` additions

```properties
PRIVACY_POLICY_URL=https://example.com/privacy
TERMS_URL=https://example.com/terms
```

### `buildkonfig {}` additions

```kotlin
defaultConfigs {
    // ... existing fields ...
    buildConfigField(STRING, "PRIVACY_POLICY_URL", project.property("PRIVACY_POLICY_URL") as String)
    buildConfigField(STRING, "TERMS_URL", project.property("TERMS_URL") as String)
}
```

### First-launch consent gate

**What it is:** a "Before you continue" screen shown to the user before they can use the app. It displays links to both legal documents and has a single "I agree — Continue" button.

**Why version-pin it:** The naïve approach stores a `true/false` flag — shown once, never again. The problem: when you update your Privacy Policy (e.g. you add analytics), existing users who already accepted the old policy need to see and accept the new one. If you store only `true`, you have no way to re-show the screen.

The solution: store the *version string of the policy they accepted* (e.g. `"1.2"`). At launch, compare it against the current policy version from `BuildKonfig`. If they match → skip. If they differ → show the gate again. Users only see it when the policy actually changes, not on every launch.

```
First launch:          stored = ""       current = "1.0"  → show gate
After accept:          stored = "1.0"    current = "1.0"  → skip
After policy update:   stored = "1.0"    current = "1.1"  → show gate again
After re-accept:       stored = "1.1"    current = "1.1"  → skip
```

**`gradle.properties`** — add the policy version alongside the URLs:
```properties
PRIVACY_POLICY_URL=https://example.com/privacy
TERMS_URL=https://example.com/terms
POLICY_VERSION=1.0
```

**`buildkonfig {}`** additions:
```kotlin
buildConfigField(STRING, "POLICY_VERSION", project.property("POLICY_VERSION") as String)
```

**`AppConfig`** additions:
```kotlin
val privacyPolicyVersion: String get() = BuildKonfig.POLICY_VERSION
```

**`ConsentScreen.kt`:**

```kotlin
@Composable
fun ConsentScreen(
    onAccept: () -> Unit,
    onViewTerms: () -> Unit,
    onViewPrivacy: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text("Before you continue", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Text(
                "By using this app you agree to our Terms & Conditions and Privacy Policy.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(12.dp))
            Row {
                TextButton(onClick = onViewTerms)   { Text("Terms & Conditions") }
                Text("  and  ", modifier = Modifier.align(Alignment.CenterVertically))
                TextButton(onClick = onViewPrivacy) { Text("Privacy Policy") }
            }
        }

        Button(
            onClick = onAccept,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("I agree — Continue")
        }
    }
}
```

**`ConsentViewModel.kt`** — checks whether the gate should show, and records acceptance:

```kotlin
class ConsentViewModel(
    private val dataStore: DataStore<Preferences>,
) : ViewModel() {

    private val POLICY_VERSION_KEY = stringPreferencesKey("accepted_policy_version")

    val shouldShowConsent: StateFlow<Boolean> = dataStore.data
        .map { prefs ->
            val accepted = prefs[POLICY_VERSION_KEY] ?: ""
            accepted != AppConfig.privacyPolicyVersion
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun acceptPolicy() {
        viewModelScope.launch {
            dataStore.edit { it[POLICY_VERSION_KEY] = AppConfig.privacyPolicyVersion }
        }
    }
}
```

**When to bump `POLICY_VERSION`:** any time the policy changes in a way that is material to the user (new data collected, new third-party shared with, new jurisdiction section). Typo fixes do not need a bump.

---

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
    python3 skills/kotlin-multiplatform-legal-docs/scripts/detect_data_collection.py . \
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
2. Developer runs `/kmm-release-notes` to draft the policy update
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

## Related Skills

- `kotlin-multiplatform-flavor-environment` — store `PRIVACY_POLICY_URL` and `TERMS_URL`
  per environment via `BuildKonfig`; dev can point to a staging version
- `kotlin-multiplatform-datastore` — persist the accepted policy version for the consent gate
- `kotlin-multiplatform-navigation` — add `LegalDocsScreen` and `ConsentScreen` routes to the nav graph
- `kotlin-multiplatform-feature-scaffold` — `LegalDocsScreen` lives in `:feature:settings:ui`
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
| 2026-06-21 | **Improved** — `detect_data_collection.py` scanner added: auto-detects 17 data types from project source; gap/conflict analysis against existing policy; CI gate via exit code; Step 6 CI integration guide added. |
| 2026-06-21 | **Improved** — Consent gate explained with version-pinning pattern; `ConsentViewModel` with `StateFlow` added; `POLICY_VERSION` in `gradle.properties` → `BuildKonfig`. |
| 2026-06-21 | Initial release — Privacy Policy + T&C templates, Google Play Data Safety, App Store privacy labels, GDPR/CCPA sections, in-app `LegalDocsScreen`, consent gate, web deployment guide. |
