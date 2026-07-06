#!/usr/bin/env python3
"""
detect_data_collection.py — scan a KMP project for data-collection evidence and
compare against an existing Privacy Policy markdown file to find gaps and conflicts.

Usage:
  python3 detect_data_collection.py <project_root>
  python3 detect_data_collection.py <project_root> --policy path/to/privacy_policy.md
  python3 detect_data_collection.py <project_root> --json

Output (default): human-readable report with DETECTED / GAPS / CONFLICTS sections.
Output (--json):  machine-readable JSON consumed by the legal-docs agent.
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path
from dataclasses import dataclass, field, asdict


# ── detection rules ──────────────────────────────────────────────────────────
# Each rule: (data_type, evidence_pattern, file_glob, policy_keyword)
# evidence_pattern is matched case-insensitively against file content.

RULES: list[tuple[str, str, str, str]] = [
    # Analytics
    ("analytics",          r"firebase.analytics|FirebaseAnalytics|amplitude|Amplitude|mixpanel|Mixpanel|AnalyticsEvent|logEvent",
                           "**/*.{kt,toml,gradle.kts}",  "analytics"),
    # Crash reporting
    ("crash_reporting",    r"crashlytics|Crashlytics|sentry|Sentry|recordException|logException",
                           "**/*.{kt,toml,gradle.kts}",  "crash"),
    # Push notifications
    ("push_notifications", r"FirebaseMessaging|FCM|fcmToken|pushToken|APNs|UNUserNotificationCenter|RemoteMessage",
                           "**/*.{kt,swift,toml}",       "push notification|notification"),
    # Location
    ("location_precise",   r"ACCESS_FINE_LOCATION|FusedLocationProviderClient|CLLocationManagerDelegate|requestWhenInUseAuthorization.*accuracy.*full",
                           "**/*.{kt,swift,xml}",        "precise location|gps"),
    ("location_approximate", r"ACCESS_COARSE_LOCATION|CLLocationAccuracy.*reduced|locationAccuracy.*reduced",
                           "**/*.{kt,swift,xml}",        "approximate location|location"),
    # Camera / microphone
    ("camera",             r"CAMERA|CameraX|CameraCapture|AVCaptureSession|camera_permission",
                           "**/*.{kt,swift,xml}",        "camera"),
    ("microphone",         r"RECORD_AUDIO|AudioRecord|AVAudioSession|microphone_permission",
                           "**/*.{kt,swift,xml}",        "microphone"),
    # Contacts
    ("contacts",           r"READ_CONTACTS|ContactsContract|CNContactStore",
                           "**/*.{kt,swift,xml}",        "contacts"),
    # Biometric
    ("biometric",          r"BiometricPrompt|BiometricManager|LAContext|LocalAuthentication|BiometricAuthenticator",
                           "**/*.{kt,swift}",            "biometric|fingerprint|face id"),
    # Advertising ID / device identifiers
    ("advertising_id",     r"AdvertisingIdClient|IDFA|ASIdentifierManager|AdServices|gaid|advertising.id",
                           "**/*.{kt,swift,toml}",       "advertising id|device id|idfa"),
    # Social login
    ("google_sign_in",     r"GoogleSignIn|google.identity|BeginSignInRequest|oneTap|googleSignIn",
                           "**/*.{kt,toml,gradle.kts}", "google sign.in|google login"),
    ("apple_sign_in",      r"SignInWithApple|ASAuthorizationAppleIDProvider|credentialState",
                           "**/*.{kt,swift}",            "sign in with apple|apple login"),
    # Payments
    ("in_app_purchases",   r"BillingClient|PurchasesUpdatedListener|SKProductsRequest|StoreKit|paymentQueue",
                           "**/*.{kt,swift,toml}",       "purchase|payment|billing|in.app"),
    ("stripe",             r"stripe|Stripe|PaymentSheet",
                           "**/*.{kt,toml,gradle.kts}", "stripe|payment"),
    # Photo library
    ("photo_library",      r"READ_MEDIA_IMAGES|PHPhotoLibrary|UIImagePickerController|rememberLauncherForActivityResult.*PickVisualMedia",
                           "**/*.{kt,swift,xml}",        "photo|image library|media"),
    # Health
    ("health_data",        r"HealthConnect|HealthDataClient|HKHealthStore|HealthKit",
                           "**/*.{kt,swift,toml}",       "health|fitness"),
    # Account / email
    ("account_email",      r"signInWithEmailAndPassword|createUserWithEmailAndPassword|FirebaseAuth|emailAddress|userEmail",
                           "**/*.{kt,toml}",             "email|account"),
    # DataStore / local prefs (signals user settings storage, not a disclosure risk by itself)
    ("local_storage",      r"DataStore|dataStore|SharedPreferences|EncryptedSharedPreferences",
                           "**/*.{kt,toml}",             ""),   # no disclosure needed
]

# Data types that don't require explicit policy disclosure on their own
DISCLOSURE_EXEMPT = {"local_storage"}


@dataclass
class Detection:
    data_type: str
    evidence: list[str] = field(default_factory=list)   # file:line snippets
    confidence: str = "high"   # high | medium | low


def glob_files(root: Path, pattern: str) -> list[Path]:
    """Expand a multi-extension glob like **/*.{kt,toml} into real paths."""
    m = re.match(r"^(.*)\.\{(.+)\}$", pattern)
    if not m:
        return list(root.glob(pattern))
    base, exts = m.group(1), m.group(2).split(",")
    results: list[Path] = []
    for ext in exts:
        results.extend(root.glob(f"{base}.{ext.strip()}"))
    return results


def scan_project(root: Path) -> list[Detection]:
    detections: list[Detection] = []

    for data_type, pattern, file_glob, _ in RULES:
        regex = re.compile(pattern, re.IGNORECASE)
        evidence: list[str] = []

        for fpath in glob_files(root, file_glob):
            try:
                lines = fpath.read_text(encoding="utf-8", errors="ignore").splitlines()
            except OSError:
                continue
            rel = fpath.relative_to(root)
            for i, line in enumerate(lines, 1):
                if regex.search(line):
                    evidence.append(f"{rel}:{i}  {line.strip()[:80]}")
                    if len(evidence) >= 3:  # cap evidence per type
                        break
            if evidence:
                break  # found evidence in at least one file

        if evidence:
            detections.append(Detection(data_type=data_type, evidence=evidence))

    return detections


def load_policy_text(policy_path: Path) -> str:
    if policy_path.exists():
        return policy_path.read_text(encoding="utf-8", errors="ignore").lower()
    return ""


def find_gaps(detections: list[Detection], policy_text: str) -> list[str]:
    """Data types detected in code but not mentioned in the policy."""
    gaps = []
    for d in detections:
        if d.data_type in DISCLOSURE_EXEMPT:
            continue
        _, _, _, keyword = next(r for r in RULES if r[0] == d.data_type)
        if not keyword:
            continue
        # Check any of the keyword alternatives appear in the policy
        alternatives = [k.strip() for k in keyword.split("|")]
        if not any(alt in policy_text for alt in alternatives):
            gaps.append(d.data_type)
    return gaps


def find_conflicts(detections: list[Detection], policy_text: str) -> list[str]:
    """Data types mentioned in the policy but not detected in code (potential over-disclosure)."""
    detected_types = {d.data_type for d in detections}
    conflicts = []
    for data_type, _, _, keyword in RULES:
        if not keyword or data_type in DISCLOSURE_EXEMPT:
            continue
        alternatives = [k.strip() for k in keyword.split("|")]
        policy_mentions = any(alt in policy_text for alt in alternatives)
        if policy_mentions and data_type not in detected_types:
            conflicts.append(data_type)
    return conflicts


def format_report(
    detections: list[Detection],
    gaps: list[str],
    conflicts: list[str],
    policy_path: Path | None,
) -> str:
    lines = ["=" * 60, "  Data Collection Detector — kmm-agent-skills", "=" * 60, ""]

    if detections:
        lines.append(f"DETECTED ({len(detections)} data types found in project):")
        for d in detections:
            marker = "⚠️  UNDISCLOSED" if d.data_type in gaps else "✅ "
            lines.append(f"  {marker} {d.data_type.replace('_', ' ').title()}")
            for ev in d.evidence[:2]:
                lines.append(f"       {ev}")
        lines.append("")
    else:
        lines.append("DETECTED: no known data-collection patterns found.\n")

    if gaps:
        lines.append(f"⚠️  GAPS — detected in code but NOT disclosed in policy ({len(gaps)}):")
        for g in gaps:
            lines.append(f"  • {g.replace('_', ' ').title()}")
        lines.append("  → Add disclosures for these types to your Privacy Policy.\n")
    elif policy_path and policy_path.exists():
        lines.append("✅  No disclosure gaps found.\n")

    if conflicts:
        lines.append(f"ℹ️  POSSIBLE OVER-DISCLOSURE — in policy but not detected in code ({len(conflicts)}):")
        for c in conflicts:
            lines.append(f"  • {c.replace('_', ' ').title()}")
        lines.append("  → Verify these disclosures are still accurate or remove them.\n")

    if not policy_path or not policy_path.exists():
        lines.append("ℹ️  No --policy file provided. Gap/conflict analysis skipped.")
        lines.append("    Pass --policy path/to/privacy_policy.md to enable comparison.\n")

    lines.append("=" * 60)
    return "\n".join(lines)


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("project_root", help="Root directory of the KMP project to scan")
    parser.add_argument("--policy", default=None, help="Path to existing Privacy Policy markdown file")
    parser.add_argument("--json", action="store_true", help="Output JSON instead of a human-readable report")
    args = parser.parse_args(argv)

    root = Path(args.project_root).resolve()
    if not root.is_dir():
        print(f"Error: {root} is not a directory", file=sys.stderr)
        return 1

    policy_path = Path(args.policy).resolve() if args.policy else None
    policy_text = load_policy_text(policy_path) if policy_path else ""

    detections = scan_project(root)
    gaps = find_gaps(detections, policy_text)
    conflicts = find_conflicts(detections, policy_text)

    if args.json:
        print(json.dumps({
            "project_root": str(root),
            "policy_path": str(policy_path) if policy_path else None,
            "detections": [asdict(d) for d in detections],
            "gaps": gaps,
            "conflicts": conflicts,
            "has_gaps": bool(gaps),
            "has_conflicts": bool(conflicts),
        }, indent=2))
    else:
        print(format_report(detections, gaps, conflicts, policy_path))

    return 1 if gaps else 0


if __name__ == "__main__":
    sys.exit(main())
