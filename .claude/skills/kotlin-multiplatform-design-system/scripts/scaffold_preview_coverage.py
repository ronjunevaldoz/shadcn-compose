#!/usr/bin/env python3
from __future__ import annotations

import argparse
import re
from pathlib import Path


CONTENT_FUN_RE = re.compile(
    r"@Composable\s+fun\s+(?P<name>\w+Content)\s*\((?P<params>.*?)\)\s*(?::\s*[^{]+)?\{",
    re.DOTALL,
)
PARAM_RE = re.compile(
    r"(?P<name>\w+)\s*:\s*(?P<type>[^=,]+?)(?:\s*=\s*(?P<default>.*))?$"
)


def _read_package(path: Path) -> str | None:
    for line in path.read_text(encoding="utf-8", errors="ignore").splitlines():
        stripped = line.strip()
        if stripped.startswith("package "):
            return stripped.removeprefix("package ").strip()
    return None


def _infer_group_id(package_name: str) -> str | None:
    for marker in (".feature.", ".core."):
        if marker in package_name:
            return package_name.split(marker, 1)[0]
    if package_name.startswith("com.") or package_name.startswith("io."):
        parts = package_name.split(".")
        return ".".join(parts[:3]) if len(parts) >= 3 else package_name
    return None


def _split_params(params: str) -> list[str]:
    values: list[str] = []
    depth = 0
    current = []
    for ch in params:
        if ch == "," and depth == 0:
            value = "".join(current).strip()
            if value:
                values.append(value)
            current = []
            continue
        current.append(ch)
        if ch in "([{<":
            depth += 1
        elif ch in ")]}>":
            depth = max(0, depth - 1)
    tail = "".join(current).strip()
    if tail:
        values.append(tail)
    return values


def _parse_params(params: str) -> list[dict[str, str | None]]:
    parsed: list[dict[str, str | None]] = []
    for raw in _split_params(params):
        match = PARAM_RE.match(raw.strip())
        if not match:
            continue
        parsed.append(match.groupdict())
    return parsed


def _infer_call_args(params: list[dict[str, str | None]]) -> list[str]:
    args: list[str] = []
    for param in params:
        name = (param.get("name") or "").strip()
        type_ = (param.get("type") or "").strip()
        default = param.get("default")

        if not name or name == "modifier" or "Modifier" in type_:
            continue
        if default is not None:
            continue
        if "->" in type_ or name.startswith("on"):
            args.append(f"{name} = {{}}")
            continue
        if name == "state" or type_.endswith("State") or type_.endswith("UiState") or ".State" in type_:
            args.append(f"{name} = {type_}()")
            continue
        args.append(f"{name} = TODO(\"Provide preview value\")")
    return args


def _extract_content_signature(content_file: Path) -> tuple[str, list[dict[str, str | None]]] | None:
    text = content_file.read_text(encoding="utf-8", errors="ignore")
    match = CONTENT_FUN_RE.search(text)
    if not match:
        return None
    return match.group("name"), _parse_params(match.group("params"))


def _ensure_dir(path: Path) -> None:
    path.mkdir(parents=True, exist_ok=True)


def _preview_package(package_name: str) -> str:
    return f"{package_name}.previews"


def _relative_package_path(package_name: str) -> Path:
    return Path(*package_name.split("."))


def _multi_device_preview_source(package_name: str) -> str:
    preview_pkg = _preview_package(package_name)
    return f"""package {preview_pkg}

import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview(name = "Phone", widthDp = 360, heightDp = 640)
@Preview(name = "Tablet", widthDp = 673, heightDp = 841)
@Preview(name = "Desktop", widthDp = 1280, heightDp = 800)
annotation class MultiDevicePreview
"""


def _preview_file_source(package_name: str, content_name: str, call_args: list[str], group_id: str) -> str:
    preview_pkg = _preview_package(package_name)
    args = ",\n            ".join(call_args)
    if args:
        args = f"\n            {args},\n        "
    return f"""package {preview_pkg}

import androidx.compose.runtime.Composable
import {group_id}.core.designsystem.theme.AppTheme

@MultiDevicePreview
@Composable
fun {content_name}() {{
    AppTheme {{
        {content_name}({args})
    }}
}}
"""


def _screenshot_file_source(package_name: str, content_name: str, call_args: list[str], group_id: str) -> str:
    preview_pkg = _preview_package(package_name)
    args = ",\n            ".join(call_args)
    if args:
        args = f"\n            {args},\n        "
    lower = re.sub(r"(?<!^)(?=[A-Z])", "_", content_name.removesuffix("Content")).lower()
    return f"""package {preview_pkg}

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import {group_id}.core.designsystem.theme.AppTheme
import kotlin.test.Test

class {content_name}ScreenshotTest {{

    @Test
    fun {lower}_phone_light() {{
        captureRoboImage("{lower}_phone_light.png") {{
            AppTheme {{
                Box(modifier = Modifier.size(360.dp, 640.dp)) {{
                    {content_name}({args})
                }}
            }}
        }}
    }}

    @Test
    fun {lower}_phone_dark() {{
        captureRoboImage("{lower}_phone_dark.png") {{
            AppTheme(darkTheme = true) {{
                Box(modifier = Modifier.size(360.dp, 640.dp)) {{
                    {content_name}({args})
                }}
            }}
        }}
    }}

    @Test
    fun {lower}_tablet_light() {{
        captureRoboImage("{lower}_tablet_light.png") {{
            AppTheme {{
                Box(modifier = Modifier.size(673.dp, 841.dp)) {{
                    {content_name}({args})
                }}
            }}
        }}
    }}

    @Test
    fun {lower}_tablet_dark() {{
        captureRoboImage("{lower}_tablet_dark.png") {{
            AppTheme(darkTheme = true) {{
                Box(modifier = Modifier.size(673.dp, 841.dp)) {{
                    {content_name}({args})
                }}
            }}
        }}
    }}

    @Test
    fun {lower}_desktop_light() {{
        captureRoboImage("{lower}_desktop_light.png") {{
            AppTheme {{
                Box(modifier = Modifier.size(1280.dp, 800.dp)) {{
                    {content_name}({args})
                }}
            }}
        }}
    }}

    @Test
    fun {lower}_desktop_dark() {{
        captureRoboImage("{lower}_desktop_dark.png") {{
            AppTheme(darkTheme = true) {{
                Box(modifier = Modifier.size(1280.dp, 800.dp)) {{
                    {content_name}({args})
                }}
            }}
        }}
    }}
}}
"""


def scaffold_preview_coverage(project_root: Path, dry_run: bool = False) -> list[str]:
    created: list[str] = []

    for content_file in sorted(project_root.rglob("*Content.kt")):
        if "/feature/" not in content_file.as_posix() or "/ui/" not in content_file.as_posix():
            continue
        sig = _extract_content_signature(content_file)
        if sig is None:
            continue

        content_name, params = sig
        package_name = _read_package(content_file)
        if not package_name:
            continue
        group_id = _infer_group_id(package_name)
        if not group_id:
            continue

        call_args = _infer_call_args(params)
        preview_pkg = _preview_package(package_name)
        preview_dir = content_file.parent / "previews"
        _ensure_dir(preview_dir)

        preview_support = preview_dir / "MultiDevicePreview.kt"
        if not preview_support.exists():
            source = _multi_device_preview_source(package_name)
            if not dry_run:
                preview_support.write_text(source, encoding="utf-8")
            created.append(str(preview_support))

        preview_file = preview_dir / f"{content_file.stem}Preview.kt"
        if not preview_file.exists():
            source = _preview_file_source(package_name, content_name, call_args, group_id)
            if not dry_run:
                preview_file.write_text(source, encoding="utf-8")
            created.append(str(preview_file))

        test_dir = content_file.as_posix().replace("/src/commonMain/kotlin/", "/src/jvmTest/kotlin/")
        test_path = Path(test_dir).parent / "previews" / f"{content_file.stem}ScreenshotTest.kt"
        _ensure_dir(test_path.parent)
        if not test_path.exists():
            source = _screenshot_file_source(package_name, content_name, call_args, group_id)
            if not dry_run:
                test_path.write_text(source, encoding="utf-8")
            created.append(str(test_path))

    return created


def main() -> int:
    parser = argparse.ArgumentParser(description="Scaffold preview coverage for feature UI screens.")
    parser.add_argument("project_root", type=Path, help="Path to the KMP project root")
    parser.add_argument("--dry-run", action="store_true", help="Report files without writing them")
    args = parser.parse_args()

    root = args.project_root.resolve()
    if not root.exists():
        raise SystemExit(f"error: {root} does not exist")

    created = scaffold_preview_coverage(root, dry_run=args.dry_run)
    if created:
        for path in created:
            print(path)
    else:
        print("OK: all feature UI preview coverage files already exist")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
