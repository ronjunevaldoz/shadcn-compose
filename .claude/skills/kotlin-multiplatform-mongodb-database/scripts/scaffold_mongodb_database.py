#!/usr/bin/env python3
from __future__ import annotations

import argparse
from pathlib import Path


FILES = {
    "MongoClientFactory.kt": """package {pkg}.database\n\nobject MongoClientFactory {{\n    // TODO: create and configure the Mongo client\n}}\n""",
    "di/DatabaseModule.kt": """package {pkg}.database.di\n\nval databaseModule = org.koin.dsl.module {{\n    // TODO: wire Mongo client, database, collections\n}}\n""",
    "user/data/UserDocument.kt": """package {pkg}.user.data\n\ndata class UserDocument(val id: String, val email: String, val name: String)\n""",
    "user/repository/UserRepository.kt": """package {pkg}.user.repository\n\ninterface UserRepository {{\n    suspend fun findById(id: String): Any?\n}}\n""",
    "user/repository/UserRepositoryImpl.kt": """package {pkg}.user.repository\n\nclass UserRepositoryImpl {{\n    // TODO: map documents to domain models\n}}\n""",
}


def scaffold_mongodb_database(output: Path, package_name: str) -> None:
    root = output.resolve()
    pkg = package_name.rstrip(".")

    for rel, template in FILES.items():
        path = root / rel
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(template.format(pkg=pkg), encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser(description="Scaffold a MongoDB data layer.")
    parser.add_argument("output", type=Path, help="Output directory")
    parser.add_argument("--package", dest="package_name", required=True, help="Base package, e.g. com.example.app.server")
    args = parser.parse_args()

    scaffold_mongodb_database(args.output, args.package_name)
    print(f"OK: scaffolded MongoDB data layer at {args.output.resolve()}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
