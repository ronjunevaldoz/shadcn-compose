#!/usr/bin/env python3
from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path


PATTERNS = [
    ("state copy race", re.compile(r"_state\.value\s*=\s*_state\.value\.copy\(")),
    ("sharedflow replay effect", re.compile(r"MutableSharedFlow<.*replay\s*=\s*1")),
    ("network result in ui", re.compile(r"NetworkResult<")),
    ("data import in ui", re.compile(r"import .*\.data\.")),
    ("manual screen capture", re.compile(
        r"playwright|adb\s+screencap|xcrun\s+simctl\s+io|Robot\(\)\.createScreenCapture|ProcessBuilder.*screenshot",
        re.IGNORECASE,
    )),
    ("magic color literal", re.compile(r"\bColor\(0x[0-9A-Fa-f]")),
    ("named color in ui", re.compile(
        r"\bColor\.(Black|White|Gray|LightGray|DarkGray|Red|Green|Blue|Yellow|Cyan|Magenta)\b"
    )),
    ("hardcoded divider color", re.compile(
        r"\b(HorizontalDivider|VerticalDivider|Divider)\b[^)]*color\s*=\s*Color\b"
    )),
    ("system dark theme scatter", re.compile(r"\bisSystemInDarkTheme\(\)")),
    ("hardcoded spacing", re.compile(r"\bpadding\([^)]*[1-9]\d*\.dp")),
    ("livedata in viewmodel", re.compile(r"MutableLiveData|LiveData<")),
    ("direct state assignment", re.compile(r"_state\.value\s*=")),
    ("globalscope usage", re.compile(r"\bGlobalScope\b")),
    ("navcontroller in viewmodel", re.compile(r"NavController.*ViewModel|ViewModel.*NavController")),
    ("dto leak to domain", re.compile(r"import .*\.dto\.|import .*\.entity\.|@SerialName.*class.*UseCase")),
]


def _at(text: str, pos: int) -> tuple[int, str]:
    """Return (1-based line number, stripped source line) for a match offset.

    Used to attach verifiable evidence (file:line + the matched line) to findings so a
    reviewer can confirm a finding before committing to a refactor.
    """
    line_no = text.count("\n", 0, pos) + 1
    lines = text.splitlines()
    snippet = lines[line_no - 1].strip() if 1 <= line_no <= len(lines) else ""
    return line_no, snippet

# ── Roadmap detection ─────────────────────────────────────────────────────────

def _has(root: Path, *globs: str) -> bool:
    return any(root.rglob(g) for g in globs)


def _count_files(root: Path, *globs: str) -> int:
    return sum(1 for g in globs for _ in root.rglob(g))


def _read_all(root: Path, *globs: str) -> str:
    parts = []
    for g in globs:
        for p in root.rglob(g):
            try:
                parts.append(p.read_text(encoding="utf-8", errors="ignore"))
            except OSError:
                pass
    return "\n".join(parts)


def _detect_state_mgmt(root: Path) -> str:
    kt = _read_all(root, "*.kt")
    if "MutableStateFlow" in kt and ("sealed interface Intent" in kt or "sealed class Intent" in kt):
        return "MVI (StateFlow + Contract)"
    if "MutableStateFlow" in kt:
        return "StateFlow (no MVI Contract)"
    if "MutableLiveData" in kt:
        return "LiveData (MVVM)"
    if "MutableState" in kt and "remember" in kt:
        return "Compose remember (no ViewModel)"
    return "unknown"


def _detect_modules(root: Path) -> str:
    settings = root / "settings.gradle.kts"
    if not settings.exists():
        settings = root / "settings.gradle"
    if not settings.exists():
        return "single-module (no settings.gradle found)"
    text = settings.read_text(encoding="utf-8", errors="ignore")
    feature_modules = re.findall(r'include\("[^"]*feature[^"]*"\)', text)
    if len(feature_modules) >= 4:
        return f"multi-module ({len(feature_modules)} feature modules)"
    if feature_modules:
        return f"partial split ({len(feature_modules)} feature modules)"
    return "single-module (no :feature: includes)"


def _detect_di(root: Path) -> str:
    kt = _read_all(root, "*.kt")
    if "@KoinViewModel" in kt or "koinViewModel()" in kt:
        return "Koin 4 (annotated)"
    if "koinInject" in kt or "val module = module {" in kt:
        return "Koin (manual)"
    if "@HiltViewModel" in kt or "@AndroidEntryPoint" in kt:
        return "Hilt"
    if "@Inject" in kt:
        return "Dagger / manual inject"
    return "none detected"


def _detect_tests(root: Path) -> str:
    test_files = _count_files(root, "*Test.kt", "*Spec.kt")
    if test_files == 0:
        return "none"
    if test_files < 5:
        return f"minimal ({test_files} test files)"
    return f"present ({test_files} test files)"


def _detect_detekt(root: Path) -> str:
    if _has(root, "detekt.yml", "detekt.yaml", "detekt-config.yml"):
        return "configured"
    return "missing"


def _detect_version_catalog(root: Path) -> str:
    if _has(root, "libs.versions.toml"):
        return "present"
    return "missing"


def assess_project(root: Path) -> dict:
    vm_info = _detect_viewmodel_size(root)
    return {
        "state_mgmt":       _detect_state_mgmt(root),
        "modules":          _detect_modules(root),
        "feature_split":    _detect_feature_split(root),
        "di":               _detect_di(root),
        "tests":            _detect_tests(root),
        "detekt":           _detect_detekt(root),
        "version_catalog":  _detect_version_catalog(root),
        "viewmodel_max_lines": vm_info["max_lines"],
        "large_vms":        vm_info["large_vms"],
    }


ADOPTION_PLAN = [
    # (condition_fn, priority, skill, reason, action)
    (
        lambda s: s["detekt"] == "missing",
        "HIGH",
        "kotlin-multiplatform-code-quality",
        "No Detekt gates — new violations accumulate faster than you migrate them",
        "Add detekt.yml with layer rules before touching any architecture code",
    ),
    (
        lambda s: s["version_catalog"] == "missing",
        "HIGH",
        "kotlin-multiplatform-feature-scaffold",
        "No version catalog — dependency versions drift across modules",
        "Add gradle/libs.versions.toml and migrate build files to use it",
    ),
    (
        lambda s: "LiveData" in s["state_mgmt"],
        "HIGH",
        "kotlin-multiplatform-mvi",
        "LiveData detected — migrate to StateFlow+MVI screen by screen",
        "Pick the highest-traffic screen, write tests for it, then migrate to StateFlow (Path A Step 1)",
    ),
    (
        lambda s: s["state_mgmt"] == "StateFlow (no MVI Contract)",
        "MEDIUM",
        "kotlin-multiplatform-mvi",
        "StateFlow present but no MVI Contract — effects may be using SharedFlow or callbacks",
        "Add Contract (State/Intent/Effect) to screens that have navigation side-effects",
    ),
    (
        lambda s: "single-module" in s["modules"],
        "MEDIUM",
        "kotlin-multiplatform-clean-architecture",
        "Single module — no layer isolation; UI can import data layer directly",
        "Extract :model first (zero-logic move), then :api, then :domain (see migration Path B)",
    ),
    (
        lambda s: "partial split" in s["modules"],
        "MEDIUM",
        "kotlin-multiplatform-clean-architecture",
        "Partial module split — some features separated, others still monolithic",
        "Complete the split for the highest-churn feature first",
    ),
    (
        lambda s: s["tests"] == "none",
        "MEDIUM",
        "kotlin-multiplatform-unit-testing",
        "No tests — migrating without tests risks invisible regressions",
        "Add ViewModel tests (with FakeRepository) before migrating each screen",
    ),
    (
        lambda s: "Hilt" in s["di"] or "Dagger" in s["di"],
        "LOW",
        "kotlin-multiplatform-dependency-injection",
        "Hilt/Dagger detected — not compatible with KMP non-Android targets",
        "Migrate one @Module at a time to Koin 4 (Path C); Hilt and Koin can coexist during migration",
    ),
    (
        lambda s: s["tests"] == "minimal",
        "LOW",
        "kotlin-multiplatform-unit-testing",
        "Few tests — coverage is too thin to migrate safely at speed",
        "Add tests for every ViewModel being migrated before the migration PR",
    ),
    (
        lambda s: s["viewmodel_max_lines"] >= 300,
        "HIGH",
        "kotlin-multiplatform-mvi",
        "God ViewModel detected (300+ lines) — business logic has leaked into the ViewModel",
        "Extract business operations into use cases (see 'ViewModel Size and Decomposition' in mvi skill); "
        "each handleIntent branch that touches 2+ repos belongs in a use case",
    ),
    (
        lambda s: 150 <= s["viewmodel_max_lines"] < 300,
        "MEDIUM",
        "kotlin-multiplatform-mvi",
        "Large ViewModel detected (150–299 lines) — growing toward monolithic",
        "Review handleIntent branches for inline logic that can be extracted to use cases before size crosses 300 lines",
    ),
    (
        lambda s: "no feature layer split" in s["feature_split"] and "multi-module" in s["modules"],
        "HIGH",
        "kotlin-multiplatform-clean-architecture",
        "Multi-module project but features have no :presenter / :domain / :ui layer split",
        "Apply the start-thin tier decision: each feature needs at least :ui; add :presenter when "
        "the screen has its own ViewModel; add :domain when use cases are shared or complex",
    ),
    (
        lambda s: "thin split" in s["feature_split"],
        "MEDIUM",
        "kotlin-multiplatform-clean-architecture",
        "Features have :ui modules only — no :presenter separation",
        "Promote features with complex ViewModels to medium tier (:presenter + :ui); "
        "reserve full tier for CRUD / offline-first features",
    ),
]


def build_roadmap(state: dict) -> list[dict]:
    plan = []
    for condition, priority, skill, reason, action in ADOPTION_PLAN:
        if condition(state):
            plan.append({
                "priority": priority,
                "skill":    skill,
                "reason":   reason,
                "action":   action,
            })
    plan.sort(key=lambda x: {"HIGH": 0, "MEDIUM": 1, "LOW": 2}[x["priority"]])
    return plan


def print_roadmap(root: Path, state: dict, plan: list[dict]) -> None:
    print(f"\n{'='*60}")
    print("  KMP ADOPTION ROADMAP")
    print(f"  Project: {root}")
    print(f"{'='*60}\n")

    print("Current state:")
    print(f"  State management : {state['state_mgmt']}")
    print(f"  Module structure : {state['modules']}")
    print(f"  Feature split    : {state['feature_split']}")
    print(f"  DI               : {state['di']}")
    print(f"  Tests            : {state['tests']}")
    print(f"  Detekt           : {state['detekt']}")
    print(f"  Version catalog  : {state['version_catalog']}")
    vm_max = state["viewmodel_max_lines"]
    if vm_max > 0:
        vm_label = "god ViewModel (300+)" if vm_max >= 300 else "large (150–299)"
        print(f"  Largest ViewModel: {vm_max} lines ({vm_label})")
        if state["large_vms"]:
            top = state["large_vms"][:3]
            for rel, n in top:
                print(f"    - {rel} ({n} lines)")
    else:
        print(f"  Largest ViewModel: not detected")
    print()

    if not plan:
        print("No adoption gaps detected. Project appears well-structured.")
        print("Run without --roadmap to check for implementation violations.\n")
        return

    print(f"Adoption plan ({len(plan)} items):\n")
    for i, item in enumerate(plan, 1):
        print(f"  {i}. [{item['priority']}] {item['skill']}")
        print(f"     Why:    {item['reason']}")
        print(f"     Action: {item['action']}")
        print()

    print("Run audit_project.py without --roadmap to check for implementation violations.")
    print()


# ── Agent & consumer setup checks ────────────────────────────────────────────

def _detect_agent_setup(root: Path) -> list[str]:
    # Only meaningful for real Gradle projects; skip bare temp dirs used in unit tests.
    is_gradle_project = (root / "settings.gradle.kts").exists() or (root / "settings.gradle").exists()
    if not is_gradle_project:
        return []

    findings: list[str] = []
    claude = root / ".claude"

    if not (root / "CLAUDE.md").exists():
        findings.append("agent-setup [HIGH]: CLAUDE.md missing — skills context never loads (run /kmm-setup-agents)")

    if not (claude / "AGENTS.md").exists():
        findings.append("agent-setup [HIGH]: .claude/AGENTS.md missing — no skill routing table (run /kmm-setup-agents)")

    commands_dir = claude / "commands"
    if not commands_dir.exists() or not any(commands_dir.iterdir()):
        findings.append("agent-setup [MEDIUM]: .claude/commands/ missing — consumer commands not installed")

    skills_dir = claude / "skills"
    if not skills_dir.exists() or not any(skills_dir.iterdir()):
        findings.append("agent-setup [MEDIUM]: .claude/skills/ missing or empty — skills not deployed")

    # Multi-surface project: AGENTS.md exists but only mentions one surface
    agents_md = claude / "AGENTS.md"
    if agents_md.exists():
        text = agents_md.read_text(encoding="utf-8", errors="ignore")
        settings = root / "settings.gradle.kts"
        if settings.exists():
            s = settings.read_text(encoding="utf-8", errors="ignore")
            has_studio = "studio" in s or "shared" in s
            has_core   = ":core:" in s or ":native" in s
            if has_studio and has_core:
                mentions_studio = "studio" in text.lower() or "shared" in text.lower()
                mentions_core   = "core" in text.lower() or "native" in text.lower()
                if not (mentions_studio and mentions_core):
                    findings.append(
                        "agent-setup [MEDIUM]: AGENTS.md covers only one surface of a multi-surface project "
                        "— add routing for the missing surface"
                    )

    return findings


def _detect_mvi_placement(root: Path) -> list[str]:
    findings: list[str] = []
    for path in root.rglob("MviViewModel.kt"):
        rel = path.relative_to(root).as_posix()
        # Flag if it's inside a feature module, not in shared/core
        if any(seg in rel for seg in ("feature", "studio", "app")) and not any(
            seg in rel for seg in ("shared/core", "core/mvi", "core/common", ":core:mvi")
        ):
            findings.append(
                f"arch [MEDIUM]: MviViewModel base class in feature module ({rel}) "
                f"— move to :shared:core or :core:mvi so all features can extend it"
            )
    return findings


def _detect_design_system_wiring(root: Path) -> list[str]:
    findings: list[str] = []
    theme_pattern    = re.compile(r"MaterialTheme\s*\(")
    dark_hardcoded   = re.compile(r"darkTheme\s*=\s*false")
    token_file_pattern = re.compile(r"(ULong|Long)\s*=\s*0x[0-9A-Fa-f]{6,}")

    token_files: list[Path] = []
    for path in root.rglob("*Tokens.kt"):
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if token_file_pattern.search(text):
            token_files.append(path)

    if len(token_files) >= 2:
        rel_paths = ", ".join(str(p.relative_to(root)) for p in token_files[:3])
        findings.append(
            f"design-system [LOW]: multiple parallel token files with raw ULong constants "
            f"({rel_paths}) — consolidate under a single AppColors data class"
        )

    for path in root.rglob("*.kt"):
        if not any(part in path.stem for part in ("Theme", "theme")):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        rel = path.relative_to(root)
        if theme_pattern.search(text):
            findings.append(
                f"design-system [MEDIUM]: {rel} wraps MaterialTheme — "
                f"blocks custom token ownership; use CompositionLocalProvider + AppTheme"
            )
        if dark_hardcoded.search(text):
            findings.append(
                f"design-system [MEDIUM]: {rel} hardcodes darkTheme=false — "
                f"replace with isSystemInDarkTheme() default"
            )

    return findings


# ── Standard audit ────────────────────────────────────────────────────────────

# A class that extends a ViewModel base (ViewModel, MviViewModel, BaseViewModel, …).
# Filename-independent: catches *Presenter.kt, coordinators, *VM.kt, etc.
# Note: no leading \b before ViewModel — the base may be MviViewModel/BaseViewModel
# (no word boundary inside the identifier), so we match `…ViewModel` as a suffix.
_VM_CLASS_DECL_RE = re.compile(
    r"\bclass\s+\w+[^:{]*:\s*[^{]*ViewModel\b",
    re.DOTALL,
)


def _is_viewmodel_file(text: str) -> bool:
    """True if the file defines a ViewModel — by supertype or viewModelScope usage —
    regardless of filename. Hardens detectors against alternate naming conventions."""
    return "viewModelScope" in text or bool(_VM_CLASS_DECL_RE.search(text))


def _detect_viewmodel_size(root: Path) -> dict:
    """Return max line count and list of oversized ViewModel files.

    Detects ViewModels by content (supertype / viewModelScope) so files that do not
    follow the *ViewModel.kt naming convention are still measured.
    """
    large: list[tuple[Path, int]] = []
    seen: set[Path] = set()
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or path in seen:
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        # Filename convention OR content signal
        if not (path.stem.endswith("ViewModel") or _is_viewmodel_file(text)):
            continue
        seen.add(path)
        count = len(text.splitlines())
        if count >= 150:
            large.append((path, count))
    large.sort(key=lambda x: x[1], reverse=True)
    return {
        "max_lines": large[0][1] if large else 0,
        "large_vms": [(str(p.relative_to(root)), n) for p, n in large],
    }


def _detect_feature_split(root: Path) -> str:
    """Detect whether features follow the layer split convention."""
    settings = root / "settings.gradle.kts"
    if not settings.exists():
        settings = root / "settings.gradle"
    if not settings.exists():
        return "unknown (no settings.gradle)"
    text = settings.read_text(encoding="utf-8", errors="ignore")
    presenter = re.findall(r'include\("[^"]*:presenter[^"]*"\)', text)
    domain    = re.findall(r'include\("[^"]*:domain[^"]*"\)', text)
    ui        = re.findall(r'include\("[^"]*:ui[^"]*"\)', text)
    if presenter and domain and ui:
        return f"full split (presenter={len(presenter)}, domain={len(domain)}, ui={len(ui)})"
    if ui and not presenter:
        return f"thin split (:ui only, {len(ui)} modules — no :presenter or :domain)"
    if presenter and not domain:
        return f"medium split (:presenter+:ui, no :domain)"
    return "no feature layer split detected"


_MULTI_VM_RE = re.compile(r'\bkoinViewModel\s*[<(]')
_LAUNCHED_EFFECT_RE = re.compile(r'\bLaunchedEffect\s*\(')
_EFFECT_COLLECT_RE = re.compile(r'\.effect\s*\.\s*collect\b')


def _detect_multi_viewmodel_screen(root: Path) -> list[str]:
    """Flag composables that instantiate 3+ ViewModels directly.

    Each koinViewModel() in one composable creates tight coupling and makes it
    untestable in isolation. Detected on any Compose file (by content), not just
    *Screen.kt, so non-convention names (Dashboard, Hub, Home) are still caught.
    The fix: split each feature into its own screen, sharing data via a repository.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if not _is_compose_ui_file(text, path):
            continue
        count = len(_MULTI_VM_RE.findall(text))
        if count >= 3:
            line_no, snippet = _at(text, _MULTI_VM_RE.search(text).start())
            findings.append(
                f"multi viewmodel screen [MEDIUM]: {path.relative_to(root)}:{line_no} "
                f"— {count} koinViewModel() calls; split each feature into its own screen "
                f"behind a NavHost (one ViewModel per screen), sharing data via a repository "
                f"(see MVI skill → feature orchestration decision order)\n"
                f"    {line_no} | {snippet}"
            )
    return findings


# Matches a @Composable function whose signature has a param typed `*ViewModel`
# that does NOT have a `= koinViewModel()` default (i.e. the VM is forced in by the caller).
_COMPOSABLE_FUN_RE = re.compile(
    r"@Composable\s+(?:private\s+|internal\s+|public\s+)?fun\s+(\w+)\s*\((?P<params>.*?)\)",
    re.DOTALL,
)
_VM_PARAM_NO_DEFAULT_RE = re.compile(
    r"\b(\w+)\s*:\s*\w*ViewModel\b(?P<after>[^,)]*)"
)


def _detect_viewmodel_as_composable_param(root: Path) -> list[str]:
    """Flag @Composable functions that receive a ViewModel as a forced parameter.

    A screen should obtain its ViewModel via `vm: FooViewModel = koinViewModel()` (a
    defaulted param) — never as a required parameter passed down by a parent. A required
    `*ViewModel` param means the parent is constructing/owning child ViewModels and threading
    them down: the god-composable shape. The fix is the same decision order: separate screens
    with their own koinViewModel(), or hoist state (not the VM) into the parent.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        for m in _COMPOSABLE_FUN_RE.finditer(text):
            params = m.group("params")
            for pm in _VM_PARAM_NO_DEFAULT_RE.finditer(params):
                after = pm.group("after")
                # Skip if this param has a koinViewModel()/viewModel() default
                if "koinViewModel" in after or "= viewModel" in after:
                    continue
                line_no, snippet = _at(text, m.start())
                findings.append(
                    f"viewmodel as composable param [MEDIUM]: {path.relative_to(root)}:{line_no} "
                    f"— @Composable {m.group(1)}(...) takes '{pm.group(1)}: *ViewModel' as a "
                    f"required param; use 'vm: FooViewModel = koinViewModel()' instead, or split "
                    f"into separate screens (see MVI skill → feature orchestration decision order)\n"
                    f"    {line_no} | {snippet}"
                )
                break  # one finding per composable
    return findings


# Matches ANY class declaration with a primary constructor and a supertype list.
# The supertype is checked for `ViewModel` separately, so this catches coordinators
# that extend a VM base without a *ViewModel name (e.g. `class DashboardCoordinator(...)`).
_VM_CLASS_RE = re.compile(
    r"class\s+(\w+)\s*(?:@\w+(?:\([^)]*\))?\s*)?\((?P<ctor>.*?)\)\s*:\s*(?P<super>[^{]+)",
    re.DOTALL,
)
_VM_PARAM_RE = re.compile(r":\s*\w*ViewModel\b")
# A ViewModel held as a property (by inject, by viewModels, type annotation) inside a VM.
_VM_PROPERTY_RE = re.compile(r"\b(?:val|var)\s+\w+\s*:\s*\w*ViewModel\b")
# A ViewModel instantiated directly: `= FooViewModel(`
_VM_INSTANTIATE_RE = re.compile(r"=\s*\w*ViewModel\s*\(")
# A ViewModel obtained via a DI generic: inject<FooViewModel>() / koinInject<FooViewModel>()
_VM_INJECT_GENERIC_RE = re.compile(r"\b(?:inject|koinInject|koinViewModel)\s*<\s*\w*ViewModel\b")


def _detect_viewmodel_in_viewmodel(root: Path) -> list[str]:
    """Flag a ViewModel that depends on another ViewModel — by constructor param,
    injected property, internal instantiation, or DI generic.

    ViewModels are created by ViewModelProvider/factory with their own viewModelScope,
    SavedStateHandle, and CreationExtras — nesting them breaks lifecycle ownership and
    DI. The fix: demote the injected sub-unit to a State Holder (plain class taking a
    CoroutineScope) or a use case. Scans all .kt files so coordinators that don't follow
    the *ViewModel.kt naming convention are still caught.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue

        # The file must define a ViewModel for any of these to be the anti-pattern.
        if not _is_viewmodel_file(text):
            continue

        vm_name = path.stem
        how: str | None = None
        pos = 0

        # 1) Constructor param typed *ViewModel (precise — scoped to the ctor block)
        for m in _VM_CLASS_RE.finditer(text):
            if "ViewModel" in m.group("super"):
                pm = _VM_PARAM_RE.search(m.group("ctor"))
                if pm:
                    vm_name, how = m.group(1), "a constructor param"
                    pos = m.start("ctor") + pm.start()
                    break

        # 2) Property / instantiation / DI-generic anywhere in the VM file
        if how is None:
            for rx, desc in (
                (_VM_PROPERTY_RE, "an injected/declared property"),
                (_VM_INSTANTIATE_RE, "a directly instantiated property"),
                (_VM_INJECT_GENERIC_RE, "a DI lookup (inject<…ViewModel>())"),
            ):
                mm = rx.search(text)
                if mm:
                    how, pos = desc, mm.start()
                    break
            if how is not None:
                cm = re.search(r"\bclass\s+(\w+)", text)
                if cm:
                    vm_name = cm.group(1)

        if how is not None:
            line_no, snippet = _at(text, pos)
            findings.append(
                f"viewmodel in viewmodel [HIGH]: {path.relative_to(root)}:{line_no} "
                f"— {vm_name} depends on another ViewModel via {how}; "
                f"demote it to a State Holder (plain class + injected CoroutineScope) "
                f"or a use case (see MVI skill → Coordinator ViewModel)\n"
                f"    {line_no} | {snippet}"
            )
    return findings


def _detect_god_composable(root: Path) -> list[str]:
    """Flag Screen/Content composables that orchestrate too much side-effect logic.

    Signals of a 'god composable' — orchestration that belongs in a coordinator
    ViewModel but leaked into the UI layer:
      - 5+ LaunchedEffect blocks (effect collection / persistence / restore in UI), OR
      - 3+ .effect.collect calls (the composable is acting as a VM-to-VM message bus)

    The fix is a coordinator ViewModel: move state assembly, effect collection,
    and persistence into viewModelScope so the screen shrinks to state + onIntent.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        # A screen by name, or any Compose file (catches non-convention composables)
        if not (any(path.stem.endswith(part) for part in _SCREEN_STEMS) or _is_compose_ui_file(text, path)):
            continue
        le_count = len(_LAUNCHED_EFFECT_RE.findall(text))
        collect_count = len(_EFFECT_COLLECT_RE.findall(text))
        if le_count >= 5 or collect_count >= 3:
            severity = "HIGH" if (le_count >= 8 or collect_count >= 5) else "MEDIUM"
            anchor = _LAUNCHED_EFFECT_RE.search(text) or _EFFECT_COLLECT_RE.search(text)
            line_no, snippet = _at(text, anchor.start())
            findings.append(
                f"god composable [{severity}]: {path.relative_to(root)}:{line_no} "
                f"— {le_count} LaunchedEffect blocks, {collect_count} effect.collect calls; "
                f"split features into separate screens behind a NavHost (sharing data via a "
                f"repository), or if they must share one screen, move state assembly + effect "
                f"collection + persistence into viewModelScope (see MVI skill decision order)\n"
                f"    {line_no} | {snippet}"
            )
    return findings


_EXCLUDED_DIRS = {
    "build", ".gradle", ".git", "vendor", "third_party",
    "node_modules", ".idea", ".kotlin", "kotlin-js-store",
    "worktrees",  # .claude/worktrees/ — agent scratch copies of the repo
}

# ── Hardcoded Android versionCode ─────────────────────────────────────────────

# A bare integer literal assignment — nothing else on the value side. A derived
# expression (major * 1_000_000 + minor * 1_000 + patch, or a variable reference)
# never matches this because the lookahead requires end-of-line/comment/brace right
# after the digits, with no trailing operator or identifier.
_VERSION_CODE_LITERAL_RE = re.compile(
    r"versionCode\s*=\s*([0-9][0-9_]*)[ \t]*(?=\r?\n|//|\}|$)", re.MULTILINE
)
_ANDROID_APP_MARKER_RE = re.compile(r"applicationId\s*=|com\.android\.application")


def _detect_hardcoded_android_version_code(root: Path) -> list[str]:
    """Flag a hardcoded (literal) Android versionCode in an app module.

    versionCode must be strictly increasing across ACCEPTED Play Console uploads. A bare
    integer literal will not auto-increment on the next release — it must be derived from
    the single semver source of truth (e.g. major*1_000_000 + minor*1_000 + patch). This
    is a silent trap: the app builds and runs identically whether versionCode is derived
    or hardcoded, and the bug only surfaces as a hard Play Console rejection on the
    SECOND release, by which point several versions may have shipped unnoticed.
    """
    findings: list[str] = []
    for path in root.rglob("*.gradle.kts"):
        if _is_excluded(path, root):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if not _ANDROID_APP_MARKER_RE.search(text):
            continue
        m = _VERSION_CODE_LITERAL_RE.search(text)
        if m:
            line_no, snippet = _at(text, m.start())
            findings.append(
                f"hardcoded android versioncode [MEDIUM]: {path.relative_to(root)}:{line_no} "
                f"— versionCode is a literal integer ({m.group(1)}); Play Console rejects an "
                f"upload whose versionCode isn't strictly higher than the last accepted one — "
                f"derive it from the semver source instead, e.g. major*1_000_000 + minor*1_000 "
                f"+ patch (see release skill → platform-native version fields)\n"
                f"    {line_no} | {snippet}"
            )
    return findings


# ── Compose Styles API compliance ─────────────────────────────────────────────
# Detectors below check generated/hand-written code against the official Do's/Don'ts
# and Limitations in references/compose-styles-api-reference.md (design-system skill).

# Don't #2 — a `style: Style = Style { ... }` default WITH A BODY. The sanctioned form
# is an empty default (`style: Style = Style`); defaults are merged inside the function.
_STYLE_DEFAULT_WITH_BODY_RE = re.compile(r"style\s*:\s*Style\s*=\s*Style\s*\{")

# Regression guard for the isEnabled bug: `.enabled = ` is not a real StyleState
# property (the official API uses `isEnabled`). Scoped to a `styleState` receiver name
# to avoid matching unrelated `.enabled` properties on other types.
_STYLE_STATE_WRONG_ENABLED_RE = re.compile(r"\b(?:styleState|it)\.enabled\s*=")

# Don't #3 — a `style: Style` parameter on a composable whose name signals a screen/page,
# not a component. Reuses the same stems the structural detectors already recognize.
_STYLE_PARAM_ON_SCREEN_RE = re.compile(
    r"@Composable\s+(?:private\s+|internal\s+|public\s+)?fun\s+(\w+)\s*\([^)]*\bstyle\s*:\s*Style\b"
)

# Don't #4 — a @Composable function named ...Style(): Style that reads a CompositionLocal
# (MaterialTheme.* or a Local*.current accessor) and returns a Style built from it. The
# value is captured once at definition time and goes stale when the theme changes.
_STYLE_RETURNING_FUN_RE = re.compile(
    r"@Composable\s+fun\s+\w*[Ss]tyle\s*\([^)]*\)\s*:\s*Style\s*\{(?P<body>.*?)\n\}",
    re.DOTALL,
)
_COMPOSITIONLOCAL_READ_RE = re.compile(r"MaterialTheme\.\w+|\bLocal\w+\.current\b")

# Limitation §5 — pressed{}/hovered{} Style blocks combined with a clickable() that
# doesn't set indication = null render both the Style animation AND the default ripple.
_STYLE_PRESSED_OR_HOVERED_RE = re.compile(r"\b(?:pressed|hovered)\s*\{")
_CLICKABLE_CALL_RE = re.compile(r"\bclickable\s*\(")
_INDICATION_NULL_RE = re.compile(r"\bindication\s*=\s*null\b")


def _detect_style_default_with_body(root: Path) -> list[str]:
    """Flag `style: Style = Style { ... }` — a default WITH a body.

    The sanctioned pattern is an empty default (`style: Style = Style`) with project
    defaults merged inside the function via `defaultStyle then style` in
    `Modifier.styleable(...)`. A default with a body can silently clobber the merge
    order and makes the "empty by convention" contract ambiguous for callers.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        m = _STYLE_DEFAULT_WITH_BODY_RE.search(text)
        if m:
            line_no, snippet = _at(text, m.start())
            findings.append(
                f"style default with body [MEDIUM]: {path.relative_to(root)}:{line_no} "
                f"— 'style: Style = Style {{ ... }}' as a parameter default; use an empty "
                f"'style: Style = Style' and merge project defaults inside via "
                f"'defaultStyle then style' in Modifier.styleable(...) "
                f"(see compose-styles-api-reference.md → Do's #6, Don't #2)\n"
                f"    {line_no} | {snippet}"
            )
    return findings


def _detect_style_state_wrong_enabled(root: Path) -> list[str]:
    """Flag `styleState.enabled = ...` — not a real StyleState property.

    The official API property is `isEnabled`, set via
    `rememberUpdatedStyleState(interactionSource) { it.isEnabled = enabled }`. This is a
    regression guard for the exact bug found and fixed across AppButton/AppChip/
    AppTextField/AppIconButton during the Compose Styles API doc audit.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        m = _STYLE_STATE_WRONG_ENABLED_RE.search(text)
        if m:
            line_no, snippet = _at(text, m.start())
            findings.append(
                f"style state wrong enabled property [HIGH]: {path.relative_to(root)}:{line_no} "
                f"— '.enabled = ' is not a real StyleState property; the API uses 'isEnabled'. "
                f"Use rememberUpdatedStyleState(interactionSource) {{ it.isEnabled = enabled }} "
                f"(see compose-styles-api-reference.md § State construction)\n"
                f"    {line_no} | {snippet}"
            )
    return findings


def _detect_style_param_on_screen(root: Path) -> list[str]:
    """Flag a `style: Style` parameter on a screen/page-named composable.

    Styles are designed for components, not layouts or screen-level composables — the
    official docs call this out explicitly (unclear to callers what a style would do at
    the layout level).
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        for m in _STYLE_PARAM_ON_SCREEN_RE.finditer(text):
            fn_name = m.group(1)
            if not any(fn_name.endswith(stem) for stem in _SCREEN_STEMS):
                continue
            line_no, snippet = _at(text, m.start())
            findings.append(
                f"style param on screen composable [MEDIUM]: {path.relative_to(root)}:{line_no} "
                f"— @Composable {fn_name}(...) takes a 'style: Style' param; Styles are for "
                f"components, not screens/layouts — hoist the styling into a child component "
                f"instead (see compose-styles-api-reference.md → Don't #3)\n"
                f"    {line_no} | {snippet}"
            )
    return findings


def _detect_stale_compositionlocal_in_style_function(root: Path) -> list[str]:
    """Flag a @Composable ...Style(): Style function that reads a CompositionLocal and
    returns a built Style — the value is captured once at definition time, not at the
    point the Style is actually consumed, and goes stale when the theme changes.

    The correct pattern is a StyleScope extension property (e.g. `val StyleScope.colors
    get() = ...`) read INSIDE the `Style { }` lambda, never outside it.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        for m in _STYLE_RETURNING_FUN_RE.finditer(text):
            body = m.group("body")
            return_style_idx = body.find("return Style")
            if return_style_idx == -1:
                continue
            before_return = body[:return_style_idx]
            local_match = _COMPOSITIONLOCAL_READ_RE.search(before_return)
            if not local_match:
                continue
            line_no, snippet = _at(text, m.start() + len("@Composable fun "))
            findings.append(
                f"stale compositionlocal in style function [HIGH]: {path.relative_to(root)}:{line_no} "
                f"— a @Composable fun ...Style(): Style reads '{local_match.group(0)}' before "
                f"returning the Style; the value is captured once at definition time and goes "
                f"stale when the theme changes. Use a StyleScope extension property read inside "
                f"the Style {{ }} lambda instead (see compose-styles-api-reference.md → Don't #4)\n"
                f"    {line_no} | {snippet}"
            )
    return findings


def _detect_missing_indication_null_with_style_state(root: Path) -> list[str]:
    """Flag a file with a Style `pressed {}`/`hovered {}` block and a `clickable(...)`
    call that has no `indication = null` anywhere in the file.

    Without indication = null, the Style-driven visual change AND the platform's default
    ripple render simultaneously — a visibly doubled effect (official Limitations §5).
    This is a file-level heuristic; verify the specific clickable() at the flagged line.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if not _STYLE_PRESSED_OR_HOVERED_RE.search(text):
            continue
        click_match = _CLICKABLE_CALL_RE.search(text)
        if not click_match:
            continue
        if _INDICATION_NULL_RE.search(text):
            continue
        line_no, snippet = _at(text, click_match.start())
        findings.append(
            f"missing indication null with style state [LOW]: {path.relative_to(root)}:{line_no} "
            f"— file has a Style pressed{{}}/hovered{{}} block and a clickable(...) with no "
            f"indication = null anywhere in the file; the Style animation and the default "
            f"ripple will render simultaneously (see compose-styles-api-reference.md → "
            f"Limitations §5)\n"
            f"    {line_no} | {snippet}"
        )
    return findings


# ── Design system prefix mismatch ─────────────────────────────────────────────
# "App" in the design-system skill is a template placeholder (see Step 0) — real
# projects must substitute their resolved COMPONENT_PREFIX when generating files, not
# leave literal App* names on disk. This detector catches the case where a project has
# already resolved and recorded a different prefix but App*-named declarations still
# exist under core/designsystem — i.e. the substitution was skipped during generation.

_COMPONENT_PREFIX_ROW_RE = re.compile(r"\|\s*Component prefix\s*\|\s*([A-Za-z][A-Za-z0-9]*)\s*\|")
_APP_PREFIXED_DECL_RE = re.compile(
    r"\b(?:class|fun|object|val|data class|sealed interface|enum class)\s+(App[A-Z]\w*)"
)


def _detect_design_system_prefix_mismatch(root: Path) -> list[str]:
    """Flag literal App*-prefixed declarations under core/designsystem when the project
    has already resolved and recorded a different COMPONENT_PREFIX in docs/design-system.md.

    Only fires when a resolved prefix is on record and it is not "App" itself — a project
    that genuinely chose "App" as its prefix is not a mismatch. Scoped to designsystem
    paths to avoid flagging unrelated App-prefixed identifiers elsewhere in the project.
    """
    doc = root / "docs" / "design-system.md"
    if not doc.exists():
        return []
    try:
        doc_text = doc.read_text(encoding="utf-8", errors="ignore")
    except OSError:
        return []
    m = _COMPONENT_PREFIX_ROW_RE.search(doc_text)
    if not m:
        return []
    resolved_prefix = m.group(1)
    if resolved_prefix in ("COMPONENT_PREFIX", "App"):
        return []  # unfilled placeholder, or the project genuinely chose "App"

    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        if "designsystem" not in path.as_posix().lower():
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        decl_match = _APP_PREFIXED_DECL_RE.search(text)
        if not decl_match:
            continue
        line_no, snippet = _at(text, decl_match.start())
        findings.append(
            f"design system prefix mismatch [HIGH]: {path.relative_to(root)}:{line_no} "
            f"— '{decl_match.group(1)}' uses the literal 'App' placeholder, but "
            f"docs/design-system.md records COMPONENT_PREFIX '{resolved_prefix}'; generate "
            f"with the resolved prefix directly instead of leaving App* names to rename "
            f"later (see design-system skill → Step 0)\n"
            f"    {line_no} | {snippet}"
        )
    return findings


# ── Empty platform-specific source sets ───────────────────────────────────────
# KMP convention plugins register android/iOS/JVM/JS targets even for pure-common
# layers (:model, :api, :domain) because Gradle needs a compiled artifact per target —
# that's normal and required. The smell is a physical androidMain/iosMain/jvmMain/...
# *source directory* that was scaffolded on disk with no real content: either zero .kt
# files, or files containing nothing but a package declaration/imports/comments. An
# empty source set compiles fine without existing at all — the directory is pure
# clutter and signals unclear architecture intent ("why does :domain have an iosMain?").

_PLATFORM_MAIN_DIR_RE = re.compile(
    r"^(?:android|ios(?:Arm64|SimulatorArm64|X64)?|jvm|js|wasmJs|native|desktop|"
    r"macos|linux|mingw)Main$"
)
_KOTLIN_COMMENT_OR_TRIVIAL_RE = re.compile(
    r"^\s*(?://.*|/\*.*?\*/|package\s+[\w.]+|import\s+[\w.*]+)?\s*$",
    re.MULTILINE,
)


def _is_trivial_kotlin_file(text: str) -> bool:
    """True if a .kt file has no real declarations — only package/import/comments/blank
    lines. A block comment spanning multiple lines is not handled line-by-line here;
    good enough as a heuristic since real declarations reliably fail this check."""
    for line in text.splitlines():
        if not _KOTLIN_COMMENT_OR_TRIVIAL_RE.match(line):
            return False
    return True


def _detect_empty_platform_sourceset(root: Path) -> list[str]:
    """Flag a platform source set directory (androidMain/iosMain/jvmMain/...) that was
    scaffolded on disk but never given real content.

    Gradle does not require the directory to exist or contain files for a target to
    compile — an empty platform source set is unnecessary in every case. This is a
    lower-stakes cleanup finding (delete the directory, or add the real expect/actual
    code if there's a genuine platform need), not an architecture violation.
    """
    findings: list[str] = []
    seen_dirs: set[Path] = set()
    for kotlin_dir in root.rglob("src/*/kotlin"):
        sourceset_dir = kotlin_dir.parent
        if sourceset_dir in seen_dirs:
            continue
        seen_dirs.add(sourceset_dir)
        if not _PLATFORM_MAIN_DIR_RE.match(sourceset_dir.name):
            continue
        if _is_excluded(kotlin_dir, root):
            continue
        kt_files = [p for p in kotlin_dir.rglob("*.kt") if not _is_excluded(p, root)]
        if not kt_files:
            findings.append(
                f"empty platform source set [LOW]: {kotlin_dir.relative_to(root)} "
                f"— no .kt files; Gradle compiles this target fine without the directory "
                f"existing at all. Remove it, or add the real expect/actual code if this "
                f"module genuinely needs platform-specific logic"
            )
            continue
        non_trivial = [p for p in kt_files if not _is_trivial_kotlin_file(
            p.read_text(encoding="utf-8", errors="ignore")
        )]
        if not non_trivial:
            findings.append(
                f"empty platform source set [LOW]: {kotlin_dir.relative_to(root)} "
                f"— {len(kt_files)} file(s) contain only package/import/comments, no real "
                f"declarations; remove the directory or implement the platform code it "
                f"was scaffolded for"
            )
    return findings


# Stems that signal a top-level screen/page composable, across naming conventions.
_SCREEN_STEMS = ("Screen", "Content", "Page", "View", "Route")

# ── String-based (non-type-safe) navigation ───────────────────────────────────

# composable("home") or composable(route = "home") — string route in a NavHost.
# Type-safe form is composable<Route>{…}, which has no string first arg.
_STRING_COMPOSABLE_RE = re.compile(r'\bcomposable\s*\(\s*(?:route\s*=\s*)?"')
# startDestination = "home" — type-safe uses a route object (no quotes).
_STRING_START_DEST_RE = re.compile(r'\bstartDestination\s*=\s*"')
# navigate("home") — type-safe uses navigate(Route). A URI (contains "://") is a
# legitimate deep-link navigation, so those are excluded.
_STRING_NAVIGATE_RE = re.compile(r'\bnavigate\s*\(\s*"(?![^"]*://)')


def _detect_string_navigation(root: Path) -> list[str]:
    """Flag string-based (non-type-safe) Navigation Compose usage.

    The navigation skill mandates @Serializable type-safe routes
    (composable<Route>, navigate(Route), startDestination = Route). String routes lose
    compile-time destination/argument checking. Detected by the string-arg forms; the
    type-safe forms (which use type params or route objects) never match.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        forms = []
        anchor = None
        for rx, lbl in (
            (_STRING_COMPOSABLE_RE, 'composable("…")'),
            (_STRING_START_DEST_RE, 'startDestination = "…"'),
            (_STRING_NAVIGATE_RE, 'navigate("…")'),
        ):
            mm = rx.search(text)
            if mm:
                forms.append(lbl)
                if anchor is None:
                    anchor = mm
        if forms:
            line_no, snippet = _at(text, anchor.start())
            findings.append(
                f"string navigation [MEDIUM]: {path.relative_to(root)}:{line_no} "
                f"— string-based routes ({', '.join(forms)}); switch to @Serializable "
                f"type-safe routes: composable<Route>, navigate(Route), "
                f"startDestination = Route (see navigation skill → type-safe routes)\n"
                f"    {line_no} | {snippet}"
            )
    return findings


# ── Repository interface leaking data-layer types ─────────────────────────────

_REPO_INTERFACE_RE = re.compile(r"\binterface\s+\w*Repository\b")
_REPO_CLASS_RE = re.compile(r"\bclass\s+\w*Repository\b")
# A DTO or DB-entity type referenced by name (UserDto, ProductEntity, …).
_DATA_TYPE_TOKEN_RE = re.compile(r"\b\w+(?:Dto|Entity)\b")


def _detect_repository_leaks_data_type(root: Path) -> list[str]:
    """Flag a Repository *interface* that mentions DTO or DB-entity types.

    The repository interface belongs in :api and must speak domain types only — DTOs
    and DB entities never cross the interface boundary (they are mapped in :data). A
    `*Dto`/`*Entity` anywhere in an interface file is a boundary leak.

    Only interface files are checked — a `*RepositoryImpl` in :data legitimately uses
    DTOs/entities internally, so files declaring a Repository class are skipped.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if not _REPO_INTERFACE_RE.search(text):
            continue
        # Skip impl/combined files — the implementation may use DTOs/entities internally.
        if _REPO_CLASS_RE.search(text):
            continue
        leak = _DATA_TYPE_TOKEN_RE.search(text)
        if leak:
            line_no, snippet = _at(text, leak.start())
            findings.append(
                f"repository leaks data type [MEDIUM]: {path.relative_to(root)}:{line_no} "
                f"— Repository interface references '{leak.group(0)}'; the interface must "
                f"speak domain types only. Return domain models (or Result<Domain>) and map "
                f"DTOs/entities in :data (see repository-pattern skill → Type Mapping)\n"
                f"    {line_no} | {snippet}"
            )
    return findings


# ── Raw component bypassing the design system ─────────────────────────────────

# Raw Material/Foundation components that have an App* design-system equivalent.
_RAW_COMPONENT_MAP = {
    "Scaffold": "AppScaffold",
    "TopAppBar": "AppTopAppBar",
    "CenterAlignedTopAppBar": "AppTopAppBar",
    "Button": "AppButton",
    "OutlinedButton": "AppButton",
    "TextButton": "AppButton",
    "ElevatedButton": "AppButton",
    "FilledTonalButton": "AppButton",
    "Card": "AppCard",
    "ElevatedCard": "AppCard",
    "OutlinedCard": "AppCard",
    "TextField": "AppTextField",
    "OutlinedTextField": "AppTextField",
    "AlertDialog": "AppDialog",
    "ModalBottomSheet": "AppBottomSheet",
    "IconButton": "AppIconButton",
    "AssistChip": "AppChip",
    "FilterChip": "AppChip",
    "SuggestionChip": "AppChip",
    "InputChip": "AppChip",
    "Badge": "AppBadge",
}
# Longest names first so e.g. OutlinedButton matches before Button at the same position.
# Matches both call forms: Component( … ) and the trailing-lambda-only Component { … }.
_RAW_COMPONENT_RE = re.compile(
    r"\b(" + "|".join(sorted(map(re.escape, _RAW_COMPONENT_MAP), key=len, reverse=True)) + r")\s*[({]"
)
# Markers that a project HAS a design system (so raw components are a bypass, not a choice).
_DESIGN_SYSTEM_MARKER_RE = re.compile(r"\bAppTheme\b|\bfun\s+App[A-Z]\w*\s*\(")
# A file that DEFINES App* wrappers — legitimately uses raw primitives internally.
_APP_WRAPPER_DEF_RE = re.compile(r"\bfun\s+App[A-Z]")


def _project_has_design_system(root: Path) -> bool:
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if _DESIGN_SYSTEM_MARKER_RE.search(text):
            return True
    return False


def _detect_raw_component_bypass(root: Path) -> list[str]:
    """Flag raw Material/Foundation components used where an App* wrapper exists.

    Only runs when the project HAS a design system (App* components / AppTheme). Files
    that define App* wrappers, theme/token files, and previews are skipped — they
    legitimately build on raw primitives. The design-system skill mandates the App*
    components ('never raw Scaffold'); raw usage elsewhere is a design-system bypass.
    """
    if not _project_has_design_system(root):
        return []

    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if not _is_compose_ui_file(text, path):
            continue
        # Skip the design-system definition layer, theme/token files, and previews.
        if _APP_WRAPPER_DEF_RE.search(text):
            continue
        if any(p in path.stem for p in ("Theme", "theme", "Token", "token", "Preview")):
            continue
        if "designsystem" in path.as_posix() or "design-system" in path.as_posix():
            continue

        found: dict[str, str] = {}
        anchor = None
        for m in _RAW_COMPONENT_RE.finditer(text):
            raw = m.group(1)
            found.setdefault(raw, _RAW_COMPONENT_MAP[raw])
            if anchor is None:
                anchor = m
        if found:
            line_no, snippet = _at(text, anchor.start())
            mapping = ", ".join(f"{r}→{a}" for r, a in list(found.items())[:5])
            findings.append(
                f"raw component bypass [MEDIUM]: {path.relative_to(root)}:{line_no} "
                f"— raw components instead of design-system wrappers ({mapping}); use the "
                f"App* components so styling/tokens stay consistent (see design-system skill)\n"
                f"    {line_no} | {snippet}"
            )
    return findings


# ── Hand-written ImageVector path data ────────────────────────────────────────

_IMAGEVECTOR_BUILDER_RE = re.compile(r"\bImageVector\.Builder\s*\(|\bmaterialIcon\s*\(")
_PATH_CMD_RE = re.compile(r"\b(?:moveTo|lineTo|curveTo|quadTo|horizontalLineTo|verticalLineTo)\s*\(")
_GENERATED_HEADER_RE = re.compile(r"GENERATED by convert_image_to_imagevector")
_HANDWRITTEN_VECTOR_MIN_CMDS = 10


def _detect_handwritten_imagevector(root: Path) -> list[str]:
    """Flag hand-written ImageVector.Builder blocks with substantial path data.

    Hallucinated float coordinates produce broken art. Vector path data must come from
    convert_image_to_imagevector.py, which stamps a GENERATED header. Tiny builders
    (< 10 path commands) are trivially reviewable and skipped; generated files are
    identified by the header and skipped.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if not _IMAGEVECTOR_BUILDER_RE.search(text):
            continue
        if _GENERATED_HEADER_RE.search(text):
            continue
        cmd_count = len(_PATH_CMD_RE.findall(text))
        if cmd_count >= _HANDWRITTEN_VECTOR_MIN_CMDS:
            m = _IMAGEVECTOR_BUILDER_RE.search(text)
            line_no, snippet = _at(text, m.start())
            findings.append(
                f"handwritten imagevector [HIGH]: {path.relative_to(root)}:{line_no} "
                f"— ImageVector.Builder with {cmd_count} hand-written path commands and no "
                f"GENERATED header; hallucinated coordinates produce broken art — re-trace "
                f"the source with convert_image_to_imagevector.py "
                f"(see imagevector-generator skill)\n"
                f"    {line_no} | {snippet}"
            )
    return findings


# ── Raster image assets in commonMain ─────────────────────────────────────────

_RASTER_EXTS = {".png", ".jpg", ".jpeg", ".webp"}
_RASTER_EXEMPT_TOKENS = ("/photos/", "/screenshots/", "/goldens/", "/snapshots/")


def _detect_raster_in_commonmain(root: Path) -> list[str]:
    """Flag raster images shipped in commonMain resources.

    Icons/logos/flat art should be compiled ImageVectors (theme-tintable, resolution-
    independent, no per-density buckets). Photos are exempt via assets/photos/;
    screenshot goldens are exempt via their directory names.
    """
    findings: list[str] = []
    for path in root.rglob("*"):
        if not path.is_file() or path.suffix.lower() not in _RASTER_EXTS:
            continue
        if _is_excluded(path, root):
            continue
        p = path.as_posix().lower()
        if "/commonmain/" not in p:
            continue
        if any(tok in p for tok in _RASTER_EXEMPT_TOKENS):
            continue
        findings.append(
            f"raster asset in commonMain [MEDIUM]: {path.relative_to(root)} "
            f"— PNG/JPG icons and flat art should be compiled ImageVectors "
            f"(convert_image_to_imagevector.py); photos belong under assets/photos/ "
            f"(see imagevector-generator skill)"
        )
    return findings


# ── Arbitrary weight literals ─────────────────────────────────────────────────

_WEIGHT_LITERAL_RE = re.compile(r"\.weight\s*\(\s*(\d+(?:\.\d+)?)f?\s*[),]")


def _detect_raw_weight_literal(root: Path) -> list[str]:
    """Flag arbitrary layout weights (e.g. weight(0.37f)) — guessed proportions.

    Weights must come from a closed set of simple fractions (whole numbers or .5 steps,
    e.g. 1f, 1.5f, 2f, 3f) so layouts are deterministic and reviewable. An arbitrary
    float like 0.37f is a guessed proportion the layout contract can't express.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if not _is_compose_ui_file(text, path):
            continue
        for m in _WEIGHT_LITERAL_RE.finditer(text):
            value = float(m.group(1))
            if (value * 2) != int(value * 2):  # not a whole or .5 fraction
                line_no, snippet = _at(text, m.start())
                findings.append(
                    f"raw weight literal [LOW]: {path.relative_to(root)}:{line_no} "
                    f"— weight({m.group(1)}f) is an arbitrary proportion; use simple "
                    f"fractions (1f, 1.5f, 2f, 3f) from the layout contract so the split "
                    f"is deterministic (see layout-system skill → slot grid)\n"
                    f"    {line_no} | {snippet}"
                )
                break  # one finding per file
    return findings


# ── Incomplete WindowSizeClass branch coverage ────────────────────────────────

_WSC_CLASS_REF_RE = re.compile(r"\bWindowWidthSizeClass\.(Compact|Medium|Expanded)\b")


def _detect_breakpoint_branch_missing(root: Path) -> list[str]:
    """Flag files that branch on WindowWidthSizeClass but don't cover all breakpoints.

    A file referencing some (but not all) of Compact/Medium/Expanded with no `else`
    branch silently falls through on the unhandled size. LOW severity — file-level
    heuristic; verify at the flagged when-block.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        refs = set(_WSC_CLASS_REF_RE.findall(text))
        if not refs or len(refs) == 3:
            continue
        if re.search(r"\belse\s*->", text):
            continue
        missing = sorted({"Compact", "Medium", "Expanded"} - refs)
        m = _WSC_CLASS_REF_RE.search(text)
        line_no, snippet = _at(text, m.start())
        findings.append(
            f"breakpoint branch missing [LOW]: {path.relative_to(root)}:{line_no} "
            f"— branches on WindowWidthSizeClass but never handles {', '.join(missing)} "
            f"and has no else branch; the unhandled size falls through silently\n"
            f"    {line_no} | {snippet}"
        )
    return findings


# ── Fixed width that overflows a compact phone ────────────────────────────────

# Fixed width/size in dp — overflows a compact phone (~360.dp) when >= threshold.
_FIXED_WIDTH_RE = re.compile(r"\.(width|size)\s*\(\s*(\d+)\s*\.dp")
# requiredWidth/requiredSize IGNORE incoming constraints — overflow risk at lower values.
_REQUIRED_SIZE_RE = re.compile(r"\.(requiredWidth|requiredSize)\s*\(\s*(\d+)\s*\.dp")
_COMPACT_WIDTH_DP = 360   # standard compact-phone width
_REQUIRED_OVERFLOW_DP = 200


def _detect_fixed_width_overflow(root: Path) -> list[str]:
    """Flag fixed widths likely to overflow a compact phone.

    'Compact enough' is ultimately a rendered property (see Roborazzi at 360x800), but a
    fixed `.width(360.dp)` / `.size(360.dp)` or a constraint-ignoring `.requiredWidth(…)`
    is a reliable static correlate of a non-responsive layout. The fix is `fillMaxWidth()`,
    `weight()`, or `widthIn(max = …)` so the layout adapts to the available width.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if not _is_compose_ui_file(text, path):
            continue
        if any(p in path.stem for p in ("Theme", "theme", "Token", "token")):
            continue

        hit = None
        for m in _FIXED_WIDTH_RE.finditer(text):
            if int(m.group(2)) >= _COMPACT_WIDTH_DP:
                hit = (m, f".{m.group(1)}({m.group(2)}.dp) ≥ {_COMPACT_WIDTH_DP}.dp")
                break
        if hit is None:
            for m in _REQUIRED_SIZE_RE.finditer(text):
                if int(m.group(2)) >= _REQUIRED_OVERFLOW_DP:
                    hit = (m, f".{m.group(1)}({m.group(2)}.dp) ignores parent constraints")
                    break
        if hit:
            m, why = hit
            line_no, snippet = _at(text, m.start())
            findings.append(
                f"fixed width overflow [LOW]: {path.relative_to(root)}:{line_no} "
                f"— {why}; this overflows a compact phone — use fillMaxWidth(), weight(), "
                f"or widthIn(max = …) so the layout adapts (see adaptive-layout skill)\n"
                f"    {line_no} | {snippet}"
            )
    return findings


# ── Redundant title detection ─────────────────────────────────────────────────

# Matches a heading-style text call: AppText/Text with a style that looks like a
# page-level title (H1/H2/Heading/HeadlineLarge/TitleLarge/DisplaySmall).
_HEADING_STYLE_RE = re.compile(
    r"\b(AppText|Text)\s*\([^)]*style\s*=\s*[A-Za-z.]*"
    r"(?:H1|H2|Heading|HeadlineLarge|TitleLarge|DisplaySmall)\b",
)
_TOPBAR_RE = re.compile(r"\b(AppTopAppBar|TopAppBar|CenterAlignedTopAppBar)\b")
_TOPBAR_SLOT_RE = re.compile(r"\btopBar\s*=\s*\{")


def _detect_redundant_title(root: Path) -> list[str]:
    """Flag Screen/Content files that have a scaffold topBar AND a heading-style
    Text in the content body — the title is shown twice visually."""
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root):
            continue
        if not any(path.stem.endswith(part) for part in _SCREEN_STEMS):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        has_topbar = _TOPBAR_RE.search(text) and _TOPBAR_SLOT_RE.search(text)
        has_heading = _HEADING_STYLE_RE.search(text)
        if has_topbar and has_heading:
            line_no, snippet = _at(text, has_heading.start())
            findings.append(
                f"redundant screen title [MEDIUM]: {path.relative_to(root)}:{line_no} "
                f"— AppTopAppBar in topBar slot AND a heading-style Text in content; "
                f"the title appears twice — remove the in-body heading\n"
                f"    {line_no} | {snippet}"
            )
    return findings


# ── Missing adaptive breakpoint coverage ──────────────────────────────────────

_WINDOW_SIZE_CLASS_RE = re.compile(r"\bWindowSizeClass\b")
# Any param TYPED WindowSizeClass counts — the param name is the project's choice.
_WINDOW_SIZE_CLASS_PARAM_RE = re.compile(r"\w+\s*:\s*WindowSizeClass\b")


def _detect_missing_adaptive_coverage(root: Path) -> list[str]:
    """If any file in the project uses WindowSizeClass, every Screen composable in
    a :ui module should accept a windowSizeClass param.  Flag screens that don't."""
    # First pass — is adaptive layout in use at all?
    project_uses_adaptive = False
    for path in root.rglob("*.kt"):
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if _WINDOW_SIZE_CLASS_RE.search(text):
            project_uses_adaptive = True
            break

    if not project_uses_adaptive:
        return []

    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root):
            continue
        # Top-level screens by name (Screen/Page/View), regardless of module path
        if not any(path.stem.endswith(s) for s in ("Screen", "Page", "View")):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if not _is_compose_ui_file(text, path):
            continue
        if not _WINDOW_SIZE_CLASS_PARAM_RE.search(text):
            findings.append(
                f"adaptive coverage [LOW]: {path.relative_to(root)} "
                f"— project uses WindowSizeClass but this screen has no windowSizeClass param; "
                f"add windowSizeClass: WindowSizeClass and branch layout per breakpoint"
            )
    return findings


def _is_excluded(path: Path, root: Path) -> bool:
    parts = path.relative_to(root).parts
    return any(
        part in _EXCLUDED_DIRS or part.endswith(".cpp")  # excludes llama.cpp/, stable-diffusion.cpp/ submodules
        for part in parts
    )


_TEST_DIR_TOKENS = (
    "/test/", "/commontest/", "/jvmtest/", "/androidtest/", "/androidunittest/",
    "/androidinstrumentedtest/", "/iostest/", "/desktoptest/", "/unittest/", "/nativetest/",
)


def _is_test_source(path: Path) -> bool:
    """True for test source sets / test files. Production-UI smells (raw components,
    fixed widths) should not fire on test fixtures — screenshot tests set fixed canvas
    sizes and use raw components in capture blocks intentionally."""
    p = path.as_posix().lower()
    if any(t in p for t in _TEST_DIR_TOKENS):
        return True
    return path.stem.endswith("Test") or path.stem.endswith("Spec")


def iter_files(root: Path):
    for path in root.rglob("*"):
        if not path.is_file():
            continue
        if _is_excluded(path, root):
            continue
        if path.suffix in {".kt", ".kts"}:
            yield path


def _is_compose_ui_file(text: str, path: Path) -> bool:
    """A file is treated as Compose UI if it declares/imports Compose, OR lives in a
    conventional UI path. Content detection makes UI smells (hardcoded colors, spacing,
    dark-theme scatter) fire even when the project does not use a /ui/ module layout."""
    if "@Composable" in text or "androidx.compose" in text or "koinViewModel" in text:
        return True
    return any(token in path.as_posix() for token in ("/ui/", "/presentation/"))


# ── Lesson / positive pattern detection ──────────────────────────────────────

def _detect_positive_patterns(root: Path) -> list[dict]:
    """
    Scan the consumer for patterns that exceed or are absent from current skill guidance.
    Returns structured lesson candidates for upstreaming.
    Each entry: { skill, pattern, description, evidence }
    """
    lessons: list[dict] = []
    kt_all = _read_all(root, "*.kt")

    # ── Design system ──────────────────────────────────────────────────────────

    theme_kt = _read_all(root, "*Theme*.kt", "*theme*.kt")

    if "compositionLocalOf<Boolean?>" in theme_kt and "isSystemInDarkTheme()" in theme_kt:
        lessons.append({
            "skill": "kotlin-multiplatform-design-system",
            "pattern": "LocalAppDarkTheme compositionLocalOf<Boolean?> override",
            "description": (
                "Consumer defines LocalAppDarkTheme = compositionLocalOf<Boolean?> { null } "
                "so in-app theme toggles override isSystemInDarkTheme() without changing AppTheme's signature. "
                "null = follow system, true/false = force. Skill should document this as the canonical override pattern."
            ),
            "evidence": "grep -r 'compositionLocalOf<Boolean?>' in *Theme*.kt",
        })

    if "userPreference" in theme_kt and ("LocalStorage" in theme_kt or "DataStore" in theme_kt or "SharedPreferences" in theme_kt):
        lessons.append({
            "skill": "kotlin-multiplatform-design-system",
            "pattern": "ThemeSettings persistent override with cross-platform storage",
            "description": (
                "Consumer persists theme preference via LocalStorage/DataStore into a ThemeSettings object "
                "backed by mutableStateOf<Boolean?>. Survives app restart. "
                "Skill should add a 'persisting theme choice' step after LocalAppDarkTheme is wired."
            ),
            "evidence": "grep -r 'userPreference' in *Theme*.kt",
        })

    # currentIsDark() helper
    if "fun currentIsDark()" in theme_kt or "fun isDark()" in theme_kt:
        lessons.append({
            "skill": "kotlin-multiplatform-design-system",
            "pattern": "currentIsDark() single-call-site helper",
            "description": (
                "Consumer wraps the preference-or-system fallback into a @Composable fun currentIsDark(): Boolean. "
                "Reduces duplication across multiple theme entry points (Android, iOS, Desktop, Web). "
                "Skill should recommend this helper in multi-platform entry wiring (Step 7)."
            ),
            "evidence": "grep -r 'fun currentIsDark' in *Theme*.kt",
        })

    # ── MVI ───────────────────────────────────────────────────────────────────

    if "BaseViewModel" in kt_all and ("Channel.BUFFERED" in kt_all or "receiveAsFlow()" in kt_all):
        lessons.append({
            "skill": "kotlin-multiplatform-mvi",
            "pattern": "Project-level BaseViewModel wrapping MviViewModel",
            "description": (
                "Consumer defines a thin BaseViewModel<S,E,I> that extends MviViewModel from :core, "
                "adding project-specific defaults (e.g. error handling, logging hooks). "
                "Skill should mention this as an optional layer between :core:mvi and feature ViewModels."
            ),
            "evidence": "grep -r 'class BaseViewModel' in *.kt",
        })

    if "UNDO_WINDOW_MS" in kt_all or (re.search(r"undoJob.*cancel|cancel.*undoJob", kt_all) and "delay(" in kt_all):
        lessons.append({
            "skill": "kotlin-multiplatform-mvi",
            "pattern": "Timed undo window (soft-delete + cancel)",
            "description": (
                "Consumer implements undo via a coroutine Job: on delete intent, schedule actual deletion "
                "after UNDO_WINDOW_MS delay; an undo intent cancels the Job. "
                "Skill should add this as a named recipe under 'one-shot delete with undo'."
            ),
            "evidence": "grep -r 'UNDO_WINDOW_MS\\|undoJob' in *.kt",
        })

    # Contract pattern: all three in one sealed object
    has_intent = re.search(r"sealed (interface|class) Intent", kt_all)
    has_effect = re.search(r"sealed (interface|class) Effect", kt_all)
    has_contract_obj = re.search(r"object \w+Contract", kt_all)
    if has_intent and has_effect and has_contract_obj:
        lessons.append({
            "skill": "kotlin-multiplatform-mvi",
            "pattern": "Contract object groups State + Intent + Effect",
            "description": (
                "Consumer colocates State (data class), Intent (sealed interface), and Effect (sealed interface) "
                "inside a single object FooContract. Improves discoverability vs three separate top-level files. "
                "Skill already recommends this but should show the grouping as the default."
            ),
            "evidence": "grep -r 'object.*Contract' in *.kt",
        })

    # ── Architecture / structure ───────────────────────────────────────────────

    if (root / "build-logic").exists() and any((root / "build-logic").rglob("*.gradle.kts")):
        lessons.append({
            "skill": "kotlin-multiplatform-clean-architecture",
            "pattern": "build-logic/ convention plugins",
            "description": (
                "Consumer uses a build-logic/ includeBuild with convention plugins to centralize AGP/KMP "
                "configuration across modules. Eliminates copy-paste Gradle config. "
                "Skill should recommend this pattern for multi-module projects."
            ),
            "evidence": "ls build-logic/convention/src/",
        })

    # FuzzyMatcher for search UX
    if "FuzzyMatcher" in kt_all or re.search(r"fun fuzzyMatch|levenshtein|editDistance", kt_all, re.IGNORECASE):
        lessons.append({
            "skill": "kotlin-multiplatform-clean-architecture",
            "pattern": "FuzzyMatcher utility for search",
            "description": (
                "Consumer ships a FuzzyMatcher (Levenshtein/edit-distance) utility in :core for member/item search. "
                "Purely platform-agnostic, testable, reusable. "
                "Skill could mention domain utilities like matchers as candidates for :core:util."
            ),
            "evidence": "grep -r 'FuzzyMatcher' in *.kt",
        })

    # ── CI ────────────────────────────────────────────────────────────────────

    if _has(root, ".github/workflows/governance.yml", ".github/workflows/governance.yaml"):
        lessons.append({
            "skill": "kotlin-multiplatform-ci-github-actions",
            "pattern": "governance.yml quality gate",
            "description": (
                "Consumer has a separate governance.yml workflow (distinct from build/test) "
                "that enforces merge rules, code ownership, or audit checks. "
                "Skill should add governance workflow as an optional CI step."
            ),
            "evidence": "ls .github/workflows/governance.yml",
        })

    # Multi-surface deployment (separate deploy workflows per target)
    deploy_workflows = list((root / ".github" / "workflows").glob("deploy-*.yml")) if (root / ".github" / "workflows").exists() else []
    if len(deploy_workflows) >= 2:
        lessons.append({
            "skill": "kotlin-multiplatform-ci-github-actions",
            "pattern": "Per-surface deploy workflows (deploy-web.yml, deploy-image.yml, …)",
            "description": (
                f"Consumer has {len(deploy_workflows)} separate deploy-*.yml workflows, one per deployment surface. "
                "Keeps CI graphs readable and allows surface-specific secrets/environments. "
                "Skill should recommend this split for multi-surface KMP projects."
            ),
            "evidence": f"ls .github/workflows/deploy-*.yml  ({len(deploy_workflows)} found)",
        })

    # ── Agent setup ───────────────────────────────────────────────────────────

    claude = root / ".claude"
    if (claude / "AGENTS.md").exists() and (claude / "commands").exists() and (root / "CLAUDE.md").exists():
        lessons.append({
            "skill": "kotlin-multiplatform-audit",
            "pattern": "Full agent setup (CLAUDE.md + AGENTS.md + commands)",
            "description": (
                "Consumer has all three agent setup artifacts in place. "
                "This project is a good reference for what the /kmm-setup-agents command should produce."
            ),
            "evidence": "ls CLAUDE.md .claude/AGENTS.md .claude/commands/",
        })

    return lessons


def harvest_project(root: Path) -> dict:
    """Return findings + positive lessons as a structured dict (for --harvest JSON output)."""
    return {
        "project": str(root),
        "findings": audit_project(root),
        "lessons": _detect_positive_patterns(root),
    }


def audit_project(root: Path) -> list[str]:
    findings: list[str] = []

    # ── Agent & consumer setup ─────────────────────────────────────────────────
    findings.extend(_detect_agent_setup(root))

    # ── MVI base class placement ───────────────────────────────────────────────
    findings.extend(_detect_mvi_placement(root))

    # ── Design system wiring ───────────────────────────────────────────────────
    findings.extend(_detect_design_system_wiring(root))

    # ── Multi-ViewModel screen ─────────────────────────────────────────────────
    findings.extend(_detect_multi_viewmodel_screen(root))

    # ── God composable (side-effect orchestration in UI) ───────────────────────
    findings.extend(_detect_god_composable(root))

    # ── ViewModel taking another ViewModel as a constructor param ──────────────
    findings.extend(_detect_viewmodel_in_viewmodel(root))

    # ── ViewModel passed as a composable parameter ─────────────────────────────
    findings.extend(_detect_viewmodel_as_composable_param(root))

    # ── String-based (non-type-safe) navigation ────────────────────────────────
    findings.extend(_detect_string_navigation(root))

    # ── Repository interface leaking data-layer types ──────────────────────────
    findings.extend(_detect_repository_leaks_data_type(root))

    # ── Raw component bypassing the design system ──────────────────────────────
    findings.extend(_detect_raw_component_bypass(root))

    # ── Fixed width that overflows a compact phone ─────────────────────────────
    findings.extend(_detect_fixed_width_overflow(root))

    # ── Hand-written ImageVector path data ─────────────────────────────────────
    findings.extend(_detect_handwritten_imagevector(root))

    # ── Raster image assets in commonMain ──────────────────────────────────────
    findings.extend(_detect_raster_in_commonmain(root))

    # ── Arbitrary weight literals ───────────────────────────────────────────────
    findings.extend(_detect_raw_weight_literal(root))

    # ── Incomplete WindowSizeClass branch coverage ──────────────────────────────
    findings.extend(_detect_breakpoint_branch_missing(root))

    # ── Hardcoded Android versionCode ──────────────────────────────────────────
    findings.extend(_detect_hardcoded_android_version_code(root))

    # ── Compose Styles API compliance ──────────────────────────────────────────
    findings.extend(_detect_style_default_with_body(root))
    findings.extend(_detect_style_state_wrong_enabled(root))
    findings.extend(_detect_style_param_on_screen(root))
    findings.extend(_detect_stale_compositionlocal_in_style_function(root))
    findings.extend(_detect_missing_indication_null_with_style_state(root))

    # ── Design system prefix mismatch ──────────────────────────────────────────
    findings.extend(_detect_design_system_prefix_mismatch(root))

    # ── Empty platform-specific source sets ────────────────────────────────────
    findings.extend(_detect_empty_platform_sourceset(root))

    # ── Redundant screen title ─────────────────────────────────────────────────
    findings.extend(_detect_redundant_title(root))

    # ── Missing adaptive breakpoint coverage ───────────────────────────────────
    findings.extend(_detect_missing_adaptive_coverage(root))

    # ── ViewModel size check (not regex-detectable, needs line count) ──────────
    vm_info = _detect_viewmodel_size(root)
    for rel_path, line_count in vm_info["large_vms"]:
        severity = "god viewmodel" if line_count >= 300 else "large viewmodel"
        findings.append(f"{severity} ({line_count} lines): {rel_path}")

    for path in iter_files(root):
        text = path.read_text(encoding="utf-8", errors="ignore")
        is_compose = _is_compose_ui_file(text, path)
        for label, pattern in PATTERNS:
            if label == "network result in ui" and not is_compose:
                continue
            if label == "data import in ui":
                if not is_compose or path.stem.endswith("ViewModel"):
                    continue
            if label == "dto leak to domain":
                # domain by path OR by a domain-layer filename (use case / interactor)
                is_domain = (
                    "/domain/" in path.as_posix()
                    or path.stem.endswith("UseCase")
                    or path.stem.endswith("Interactor")
                )
                if not is_domain:
                    continue
            if label == "navcontroller in viewmodel" and not path.stem.endswith("ViewModel"):
                continue
            if label == "magic color literal":
                if not is_compose:
                    continue
                if any(part in path.stem for part in ("Color", "Token", "Theme", "color", "token", "theme")):
                    continue
            if label == "named color in ui":
                if not is_compose:
                    continue
                if any(part in path.stem for part in ("Color", "Token", "Theme", "color", "token", "theme")):
                    continue
            if label == "hardcoded divider color":
                if not is_compose:
                    continue
            if label == "system dark theme scatter" and any(
                part in path.stem for part in ("Theme", "theme", "App")
            ):
                continue
            if label == "hardcoded spacing":
                if not is_compose:
                    continue
                if any(part in path.stem for part in ("Spacing", "spacing", "Token", "token", "Theme", "theme")):
                    continue
            mt = pattern.search(text)
            if mt:
                line_no, snippet = _at(text, mt.start())
                findings.append(
                    f"{label}: {path.relative_to(root)}:{line_no}\n    {line_no} | {snippet}"
                )

    return findings


def main() -> int:
    import json as _json

    parser = argparse.ArgumentParser(description="KMP architecture audit and adoption roadmap.")
    parser.add_argument("project_root", type=Path, help="Path to the KMP project root")
    parser.add_argument(
        "--roadmap",
        action="store_true",
        help="Output a prioritized adoption plan instead of violation findings",
    )
    parser.add_argument(
        "--harvest",
        action="store_true",
        help="Output findings + positive lessons as JSON for upstreaming to skills",
    )
    args = parser.parse_args()

    root = args.project_root.resolve()

    if args.roadmap:
        state = assess_project(root)
        plan  = build_roadmap(state)
        print_roadmap(root, state, plan)
        return 1 if plan else 0

    if args.harvest:
        result = harvest_project(root)
        print(_json.dumps(result, indent=2))
        # Exit 1 if there are HIGH findings (so CI can gate on it)
        has_high = any("[HIGH]" in f or "[HIGH]:" in f for f in result["findings"])
        return 1 if has_high else 0

    findings = audit_project(root)

    if findings:
        print("FINDINGS:")
        for finding in findings:
            print(f"- {finding}")
        return 1

    print("OK: no architecture violations detected")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
