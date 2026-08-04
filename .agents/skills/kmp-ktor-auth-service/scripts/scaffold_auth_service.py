#!/usr/bin/env python3
from __future__ import annotations

import argparse
from pathlib import Path


FILES = {
    "routes/AuthRoutes.kt": """package {pkg}.auth.routes\n\nfun authRoutes() {{\n    // TODO: login, refresh, logout routes\n}}\n""",
    "service/AuthService.kt": """package {pkg}.auth.service\n\nclass AuthService {{\n    // TODO: verify credentials, issue tokens, refresh tokens\n}}\n""",
    "service/TokenService.kt": """package {pkg}.auth.service\n\nclass TokenService {{\n    // TODO: sign and verify JWTs\n}}\n""",
    "model/AuthRequest.kt": """package {pkg}.auth.model\n\ndata class AuthRequest(val email: String, val password: String)\n""",
    "model/AuthResponse.kt": """package {pkg}.auth.model\n\ndata class AuthResponse(val accessToken: String, val refreshToken: String)\n""",
    "model/AuthError.kt": """package {pkg}.auth.model\n\nsealed interface AuthError {{\n    data object InvalidCredentials : AuthError\n    data object Unauthorized : AuthError\n}}\n""",
    "di/AuthModule.kt": """package {pkg}.auth.di\n\nval authModule = org.koin.dsl.module {{\n    // TODO: wire auth service and repositories\n}}\n""",
}


def scaffold_auth_service(output: Path, package_name: str) -> None:
    root = output.resolve()
    pkg = package_name.rstrip(".")

    for rel, template in FILES.items():
        path = root / rel
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(template.format(pkg=pkg), encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser(description="Scaffold a Ktor auth service module.")
    parser.add_argument("output", type=Path, help="Output directory")
    parser.add_argument("--package", dest="package_name", required=True, help="Base package, e.g. com.example.app.server")
    args = parser.parse_args()

    scaffold_auth_service(args.output, args.package_name)
    print(f"OK: scaffolded auth service at {args.output.resolve()}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
