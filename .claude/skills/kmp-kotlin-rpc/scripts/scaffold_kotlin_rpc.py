#!/usr/bin/env python3
from __future__ import annotations

import argparse
from pathlib import Path


FILES = {
    "shared/rpc/GreetingService.kt": """package {pkg}.rpc\n\ninterface GreetingService {{\n    suspend fun greet(request: GreetingRequest): GreetingResponse\n}}\n""",
    "shared/rpc/model/GreetingRequest.kt": """package {pkg}.rpc.model\n\ndata class GreetingRequest(val name: String)\n""",
    "shared/rpc/model/GreetingResponse.kt": """package {pkg}.rpc.model\n\ndata class GreetingResponse(val message: String)\n""",
    "server/rpc/GreetingRpcModule.kt": """package {pkg}.server.rpc\n\nfun greetingRpcModule() {{\n    // TODO: wire the RPC service behind authenticated Ktor routes\n}}\n""",
    "client/rpc/GreetingRpcClient.kt": """package {pkg}.client.rpc\n\nclass GreetingRpcClient {{\n    // TODO: configure transport and generated RPC stubs\n}}\n""",
}


def scaffold_kotlin_rpc(output: Path, package_name: str) -> None:
    root = output.resolve()
    pkg = package_name.rstrip(".")

    for rel, template in FILES.items():
        path = root / rel
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(template.format(pkg=pkg), encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser(description="Scaffold a Kotlin RPC layout.")
    parser.add_argument("output", type=Path, help="Output directory")
    parser.add_argument("--package", dest="package_name", required=True, help="Base package, e.g. com.example.app")
    args = parser.parse_args()

    scaffold_kotlin_rpc(args.output, args.package_name)
    print(f"OK: scaffolded Kotlin RPC layout at {args.output.resolve()}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
