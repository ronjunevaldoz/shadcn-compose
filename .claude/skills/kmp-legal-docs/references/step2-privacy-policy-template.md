# Step 2: Privacy Policy Template

Part of `kmp-legal-docs`. Load this file when working on: step 2: privacy policy template.

---

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

