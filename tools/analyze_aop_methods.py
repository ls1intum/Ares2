#!/usr/bin/env python3
import argparse
import re
import subprocess
from collections import defaultdict
from pathlib import Path

DOC_PATH = Path("/Users/markuspaulsen/Documents/Ares2/docs/aop/BlockFileSystemAccessAOP.md")
SECTION_RE = re.compile(r"^##\s+2\.(2|3|4|5|6)\s+What Are The Monitored (READ|WRITE|CREATE|DELETE|EXECUTE) Operations\?")


class JavapCache:
    def __init__(self):
        self.cache = {}

    def get(self, class_name):
        if class_name in self.cache:
            return self.cache[class_name]
        info = self._load(class_name)
        self.cache[class_name] = info
        return info

    def _load(self, class_name):
        try:
            out = subprocess.check_output(["javap", "-public", class_name], stderr=subprocess.STDOUT, text=True)
        except subprocess.CalledProcessError as e:
            return {
                "class": class_name,
                "decl_line": None,
                "supertypes": [],
                "instance_methods": set(),
                "static_methods": set(),
                "error": e.output.strip(),
            }

        lines = out.splitlines()
        decl_line = None
        for line in lines:
            if " class " in line or " interface " in line or line.strip().startswith("class ") or line.strip().startswith("interface "):
                decl_line = line.strip()
                break
        supertypes = parse_supertypes(decl_line) if decl_line else []

        simple_name = class_name.split(".")[-1]
        instance_methods = set()
        static_methods = set()
        for line in lines:
            line = line.strip()
            if not line or line.startswith("Compiled from"):
                continue
            if "(" not in line or not line.endswith(";"):
                continue
            if decl_line and line == decl_line:
                continue
            pre = line.split("(")[0].strip()
            tokens = pre.split()
            if not tokens:
                continue
            name = tokens[-1]
            if name == simple_name:
                name = "<init>"
            is_static = " static " in f" {line} "
            if is_static:
                static_methods.add(name)
            else:
                instance_methods.add(name)
        return {
            "class": class_name,
            "decl_line": decl_line,
            "supertypes": supertypes,
            "instance_methods": instance_methods,
            "static_methods": static_methods,
            "error": None,
        }


def strip_generics(s):
    while True:
        new = re.sub(r"<[^<>]*>", "", s)
        if new == s:
            return new
        s = new


def parse_supertypes(decl_line):
    if not decl_line:
        return []
    line = strip_generics(decl_line)
    line = line.split("{")[0].strip()
    supertypes = []

    if " extends " in line:
        after_ext = line.split(" extends ", 1)[1]
        if " implements " in after_ext:
            ext_part, impl_part = after_ext.split(" implements ", 1)
        else:
            ext_part, impl_part = after_ext, None
        ext_part = ext_part.strip()
        if ext_part:
            supertypes.extend([p.strip().strip(",") for p in ext_part.split(",") if p.strip()])
    else:
        impl_part = None
        if " implements " in line:
            impl_part = line.split(" implements ", 1)[1]

    if impl_part:
        impl_part = impl_part.strip()
        impl_part = impl_part.replace("{", "")
        for p in impl_part.split(","):
            p = p.strip().strip(",")
            if p:
                supertypes.append(p)
    return supertypes


def all_supertypes(cache, class_name):
    seen = set()
    stack = [class_name]
    while stack:
        cls = stack.pop()
        info = cache.get(cls)
        for sup in info["supertypes"]:
            if sup not in seen:
                seen.add(sup)
                stack.append(sup)
    return seen


def load_sections(lines):
    sections = []
    for i, line in enumerate(lines):
        m = SECTION_RE.match(line.strip())
        if m:
            sections.append((m.group(2), i))
    results = []
    for idx, (action, start) in enumerate(sections):
        end = sections[idx + 1][1] if idx + 1 < len(sections) else len(lines)
        results.append((action, start, end))
    return results


def extract_entries(lines, start, end):
    entries = []
    current_table = None
    in_method_table = False
    for line in lines[start:end]:
        line = line.rstrip()
        if line.startswith("**") and line.endswith("**"):
            current_table = line.strip("*")
            in_method_table = False
            continue
        if not line.startswith("|"):
            in_method_table = False
            continue
        if "Class (fully qualified)" in line and "Method" in line:
            in_method_table = True
            continue
        if line.startswith("| ---"):
            continue
        parts = [p.strip() for p in line.strip().strip("|").split("|")]
        if len(parts) < 2:
            continue
        if not in_method_table:
            continue
        cls = parts[0]
        method = parts[1].replace("`", "").strip()
        entries.append({"class": cls, "method": method, "table": current_table})
    return entries


def analyze_section(action, entries, cache):
    by_key = defaultdict(list)
    for e in entries:
        by_key[(e["class"], e["method"])].append(e)

    duplicates = {k: v for k, v in by_key.items() if len(v) > 1}

    coverage = defaultdict(list)
    for e in entries:
        cls = e["class"]
        method = e["method"]
        if method == "<new>":
            continue

        info = cache.get(cls)
        if method in info["static_methods"]:
            continue

        sups = all_supertypes(cache, cls)
        for other_cls in sups:
            key = (other_cls, method)
            if key in by_key:
                other_info = cache.get(other_cls)
                if method in other_info["instance_methods"]:
                    coverage[(cls, method)].append(other_cls)

    missing = []
    for e in entries:
        cls = e["class"]
        method = e["method"]
        if method == "<new>":
            continue
        info = cache.get(cls)
        if info["error"]:
            missing.append((cls, method, "javap error"))
            continue
        if method not in info["instance_methods"] and method not in info["static_methods"]:
            inherited = False
            for sup in all_supertypes(cache, cls):
                sup_info = cache.get(sup)
                if method in sup_info["instance_methods"] or method in sup_info["static_methods"]:
                    inherited = True
                    break
            if not inherited:
                missing.append((cls, method, "not declared"))

    return duplicates, coverage, missing


def main():
    parser = argparse.ArgumentParser(description="Analyze monitored methods in BlockFileSystemAccessAOP.md")
    parser.add_argument("--section", choices=["READ", "WRITE", "CREATE", "DELETE", "EXECUTE", "ALL"], default="ALL")
    args = parser.parse_args()

    lines = DOC_PATH.read_text().splitlines()
    sections = load_sections(lines)
    if not sections:
        raise SystemExit("No matching sections found")

    cache = JavapCache()

    for action, start, end in sections:
        if args.section != "ALL" and action != args.section:
            continue
        entries = extract_entries(lines, start, end)
        for cls in sorted({e["class"] for e in entries}):
            cache.get(cls)

        duplicates, coverage, missing = analyze_section(action, entries, cache)

        print(f"=== {action} OPERATIONS ANALYSIS ===")
        print(f"Entries: {len(entries)}")
        print(f"Unique class+method: {len(set((e['class'], e['method']) for e in entries))}")

        print("\n--- Duplicates (class+method appearing in multiple tables) ---")
        if not duplicates:
            print("(none)")
        else:
            for (cls, method), rows in sorted(duplicates.items()):
                tables = sorted({r['table'] for r in rows})
                print(f"{cls}.{method} -> {', '.join(tables)}")

        print("\n--- Covered by other methods (supertype entry exists) ---")
        if not coverage:
            print("(none)")
        else:
            for (cls, method), sups in sorted(coverage.items()):
                sups_sorted = ", ".join(sorted(set(sups)))
                print(f"{cls}.{method} covered by {sups_sorted}.{method}")

        print("\n--- Methods not declared in class (javap -public) ---")
        if not missing:
            print("(none)")
        else:
            for cls, method, reason in missing:
                print(f"{cls}.{method} -> {reason}")
        print("")


if __name__ == "__main__":
    main()
