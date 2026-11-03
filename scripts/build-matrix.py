#!/usr/bin/env python3
"""Expand versions/*/gradle.properties into a GitHub Actions build matrix.

Each enabled loader of each Minecraft version becomes one node, described by the
Gradle task path :<loader>:<version>:build.

Usage:
  python3 scripts/build-matrix.py [versions] [loaders]

Filters come from (in order of precedence):
  1. positional arguments:  build-matrix.py 1.20.1,1.21.1 fabric
  2. INPUT_VERSIONS / INPUT_LOADERS environment variables (used by the workflow)
Both are optional - no filter means "every node".

GITHUB_OUTPUT  when set, writes `matrix=<json>` and `count=<n>` there;
               otherwise prints `matrix=<json>` to stdout.
"""
from __future__ import annotations

import json
import os
import pathlib
import re
import sys

LOADER_ORDER = ["fabric", "forge", "neoforge"]


def csv_set(value: str | None) -> set[str]:
    return {part.strip() for part in (value or "").split(",") if part.strip()}


def version_key(version: str) -> tuple:
    """Sort 1.20.1 before 1.21.1, and anything non-numeric last."""
    parts = re.split(r"[.\-]", version)
    key = []
    for part in parts:
        key.append((0, int(part)) if part.isdigit() else (1, part))
    return tuple(key)


def collect_nodes(want_versions: set[str], want_loaders: set[str]) -> list[dict]:
    nodes: list[dict] = []
    version_dirs = sorted(
        pathlib.Path("versions").glob("*/gradle.properties"),
        key=lambda p: version_key(p.parent.name),
    )
    for props in version_dirs:
        version = props.parent.name
        if want_versions and version not in want_versions:
            continue

        text = props.read_text(encoding="utf-8")
        match = re.search(r"^project\.enabled-loaders=(.*)$", text, re.M)
        enabled = [x.strip() for x in (match.group(1) if match else "").split(",") if x.strip()]

        ordered = [l for l in LOADER_ORDER if l in enabled]
        ordered += [l for l in enabled if l not in LOADER_ORDER]

        for loader in ordered:
            if want_loaders and loader not in want_loaders:
                continue
            nodes.append(
                {
                    "node": f"{version}-{loader}",
                    "version": version,
                    "loader": loader,
                    "task": f":{loader}:{version}:build",
                }
            )
    return nodes


def main() -> int:
    # Positional args win; the workflow passes filters through env instead so that
    # empty workflow_dispatch inputs cannot turn into stray empty arguments.
    arg_versions = sys.argv[1] if len(sys.argv) > 1 else None
    arg_loaders = sys.argv[2] if len(sys.argv) > 2 else None

    nodes = collect_nodes(
        csv_set(arg_versions if arg_versions is not None else os.environ.get("INPUT_VERSIONS")),
        csv_set(arg_loaders if arg_loaders is not None else os.environ.get("INPUT_LOADERS")),
    )
    if not nodes:
        print("no nodes matched the requested filters", file=sys.stderr)
        return 1

    payload = json.dumps({"include": nodes}, separators=(",", ":"))
    out_path = os.environ.get("GITHUB_OUTPUT")
    if out_path:
        with open(out_path, "a", encoding="utf-8") as handle:
            handle.write(f"matrix={payload}\n")
            handle.write(f"count={len(nodes)}\n")
    else:
        print(f"matrix={payload}")
        print(f"count={len(nodes)}")

    print(f"resolved {len(nodes)} node(s): {', '.join(n['node'] for n in nodes)}", file=sys.stderr)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
