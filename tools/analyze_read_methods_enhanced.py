#!/usr/bin/env python3
"""
Enhanced analysis tool for section 2.2 READ Operations.

This tool analyzes:
1. Duplicate methods (same class+method appearing multiple times)
2. Methods covered by supertype monitoring (inheritance/interface hierarchy)
3. Methods covered by constructor monitoring (if <new> is monitored, instance methods might be redundant)
4. Wrapping relationships (BufferedInputStream wraps FileInputStream, etc.)
5. Semantic analysis of monitoring completeness
"""

import re
import subprocess
from collections import defaultdict
from pathlib import Path
from dataclasses import dataclass
from typing import Dict, List, Set, Tuple, Optional

DOC_PATH = Path("/Users/markuspaulsen/Documents/Ares2/docs/aop/BlockFileSystemAccessAOP.md")
SECTION_START = "## 2.2 What Are The Monitored READ Operations?"
SECTION_END = "## 2.3 What Are The Monitored WRITE Operations?"

# Known wrapping relationships: wrapper class -> typically wraps these classes
WRAPPING_RELATIONSHIPS = {
    "java.io.BufferedInputStream": ["java.io.InputStream", "java.io.FileInputStream"],
    "java.io.DataInputStream": ["java.io.InputStream", "java.io.FileInputStream", "java.io.BufferedInputStream"],
    "java.util.Scanner": ["java.io.InputStream", "java.io.FileInputStream", "java.io.Reader"],
    "java.util.jar.JarInputStream": ["java.util.zip.ZipInputStream"],
    "java.util.jar.JarFile": ["java.util.zip.ZipFile"],
}

# Constructor monitoring may cover these instance methods
CONSTRUCTOR_COVERS_INSTANCE_METHODS = {
    "java.io.FileInputStream": ["read"],
    "java.io.BufferedInputStream": ["read"],
    "java.io.RandomAccessFile": ["read"],
    "java.io.DataInputStream": ["read", "readBoolean", "readByte", "readChar", "readDouble", 
                                  "readFloat", "readFully", "readInt", "readLine", "readLong",
                                  "readShort", "readUTF", "readUnsignedByte", "readUnsignedShort"],
    "java.io.Reader": ["read"],
    "java.util.zip.ZipInputStream": ["getNextEntry"],
    "java.util.jar.JarInputStream": ["getNextJarEntry"],
    "java.util.zip.ZipFile": ["entries", "getInputStream"],
    "java.util.jar.JarFile": ["entries", "getInputStream"],
}


@dataclass
class MethodEntry:
    class_name: str
    method_name: str
    table_category: str
    
    @property
    def key(self) -> Tuple[str, str]:
        return (self.class_name, self.method_name)
    
    def __str__(self) -> str:
        return f"{self.class_name}.{self.method_name}"


class JavapCache:
    def __init__(self):
        self.cache = {}

    def get(self, class_name: str) -> dict:
        if class_name in self.cache:
            return self.cache[class_name]
        info = self._load(class_name)
        self.cache[class_name] = info
        return info

    def _load(self, class_name: str) -> dict:
        try:
            out = subprocess.check_output(
                ["javap", "-public", class_name], 
                stderr=subprocess.STDOUT, 
                text=True
            )
        except subprocess.CalledProcessError as e:
            return {
                "class": class_name,
                "decl_line": None,
                "supertypes": [],
                "instance_methods": set(),
                "static_methods": set(),
                "constructors": set(),
                "error": e.output.strip(),
            }

        lines = out.splitlines()
        decl_line = None
        for line in lines:
            if " class " in line or " interface " in line:
                decl_line = line.strip()
                break
            if line.strip().startswith("class ") or line.strip().startswith("interface "):
                decl_line = line.strip()
                break
        
        supertypes = self._parse_supertypes(decl_line) if decl_line else []
        simple_name = class_name.split(".")[-1]
        instance_methods = set()
        static_methods = set()
        constructors = set()
        
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
            
            # Check if constructor
            if name == simple_name:
                constructors.add("<new>")
                continue
            
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
            "constructors": constructors,
            "error": None,
        }

    def _strip_generics(self, s: str) -> str:
        while True:
            new = re.sub(r"<[^<>]*>", "", s)
            if new == s:
                return new
            s = new

    def _parse_supertypes(self, decl_line: str) -> List[str]:
        if not decl_line:
            return []
        line = self._strip_generics(decl_line)
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
            impl_part = impl_part.strip().replace("{", "")
            for p in impl_part.split(","):
                p = p.strip().strip(",")
                if p:
                    supertypes.append(p)
        return supertypes

    def all_supertypes(self, class_name: str) -> Set[str]:
        seen = set()
        stack = [class_name]
        while stack:
            cls = stack.pop()
            info = self.get(cls)
            for sup in info["supertypes"]:
                if sup not in seen:
                    seen.add(sup)
                    stack.append(sup)
        return seen


def read_section_lines() -> List[str]:
    lines = DOC_PATH.read_text().splitlines()
    start = None
    end = None
    for i, line in enumerate(lines):
        if line.strip() == SECTION_START:
            start = i
            continue
        if start is not None and line.strip() == SECTION_END:
            end = i
            break
    if start is None:
        raise SystemExit("Start section not found")
    if end is None:
        raise SystemExit("End section not found")
    return lines[start:end]


def extract_entries(lines: List[str]) -> List[MethodEntry]:
    entries = []
    current_table = "Unknown"
    
    for line in lines:
        line = line.rstrip()
        if line.startswith("**") and line.endswith("**"):
            current_table = line.strip("*")
            continue
        if not line.startswith("|"):
            continue
        if line.startswith("| ---"):
            continue
        
        parts = [p.strip() for p in line.strip().strip("|").split("|")]
        if len(parts) < 2:
            continue
        if parts[0] == "Class (fully qualified)":
            continue
        
        cls = parts[0]
        method = parts[1].replace("`", "").strip()
        entries.append(MethodEntry(
            class_name=cls,
            method_name=method,
            table_category=current_table
        ))
    
    return entries


def find_duplicates(entries: List[MethodEntry]) -> Dict[Tuple[str, str], List[MethodEntry]]:
    """Find methods appearing multiple times."""
    by_key = defaultdict(list)
    for e in entries:
        by_key[e.key].append(e)
    return {k: v for k, v in by_key.items() if len(v) > 1}


def find_supertype_coverage(entries: List[MethodEntry], cache: JavapCache) -> Dict[Tuple[str, str], List[str]]:
    """Find methods where a supertype's method is also monitored."""
    coverage = defaultdict(list)
    entry_keys = {e.key for e in entries}
    
    for e in entries:
        if e.method_name == "<new>":
            continue
        
        info = cache.get(e.class_name)
        if e.method_name in info["static_methods"]:
            continue
        
        sups = cache.all_supertypes(e.class_name)
        for sup_cls in sups:
            if (sup_cls, e.method_name) in entry_keys:
                sup_info = cache.get(sup_cls)
                if e.method_name in sup_info["instance_methods"]:
                    coverage[e.key].append(sup_cls)
    
    return coverage


def find_constructor_coverage(entries: List[MethodEntry]) -> Dict[Tuple[str, str], MethodEntry]:
    """
    Find instance methods that might be covered by constructor monitoring.
    If a class's <new> is monitored, subsequent instance method calls 
    on that instance might be redundant.
    """
    coverage = {}
    
    # Build maps
    entries_by_key = {e.key: e for e in entries}
    constructors = {e.class_name: e for e in entries if e.method_name == "<new>"}
    
    for class_name, covered_methods in CONSTRUCTOR_COVERS_INSTANCE_METHODS.items():
        if class_name in constructors:
            for method in covered_methods:
                key = (class_name, method)
                if key in entries_by_key:
                    coverage[key] = constructors[class_name]
    
    return coverage


def find_interface_implementation_redundancy(entries: List[MethodEntry], cache: JavapCache) -> List[Tuple[MethodEntry, List[MethodEntry]]]:
    """
    Find cases where an interface method is monitored AND its implementing class methods
    are also monitored (potential redundancy).
    """
    redundancies = []
    entries_by_key = {e.key: e for e in entries}
    entries_by_class = defaultdict(list)
    for e in entries:
        entries_by_class[e.class_name].append(e)
    
    # Known interfaces in the monitored list
    interfaces = ["java.io.DataInput", "java.io.DataOutput", "java.nio.channels.SeekableByteChannel"]
    
    for iface in interfaces:
        if iface not in entries_by_class:
            continue
        
        iface_entries = entries_by_class[iface]
        # Find all monitored classes that implement this interface
        impl_classes = []
        for cls in entries_by_class.keys():
            if cls == iface:
                continue
            sups = cache.all_supertypes(cls)
            if iface in sups:
                impl_classes.append(cls)
        
        for iface_entry in iface_entries:
            # Check if any implementing class also has this method monitored
            impl_entries = []
            for impl_cls in impl_classes:
                impl_key = (impl_cls, iface_entry.method_name)
                if impl_key in entries_by_key:
                    impl_entries.append(entries_by_key[impl_key])
            
            if impl_entries:
                redundancies.append((iface_entry, impl_entries))
    
    return redundancies


def find_wrapping_redundancy(entries: List[MethodEntry]) -> List[Tuple[str, str, str]]:
    """
    Find cases where a wrapper class constructor is monitored AND the wrapped 
    class constructor is also monitored.
    """
    redundancies = []
    constructor_classes = {e.class_name for e in entries if e.method_name == "<new>"}
    
    for wrapper, wrapped_list in WRAPPING_RELATIONSHIPS.items():
        if wrapper in constructor_classes:
            for wrapped in wrapped_list:
                if wrapped in constructor_classes:
                    redundancies.append((wrapper, wrapped, "<new>"))
    
    return redundancies


def analyze_missing_methods(entries: List[MethodEntry], cache: JavapCache) -> List[Tuple[str, str, str]]:
    """Find methods not declared in the class according to javap."""
    missing = []
    for e in entries:
        if e.method_name == "<new>":
            continue
        info = cache.get(e.class_name)
        if info["error"]:
            missing.append((e.class_name, e.method_name, f"javap error: {info['error'][:50]}..."))
            continue
        if e.method_name not in info["instance_methods"] and e.method_name not in info["static_methods"]:
            missing.append((e.class_name, e.method_name, "not declared in public API"))
    return missing


def print_report(
    entries: List[MethodEntry],
    duplicates: Dict[Tuple[str, str], List[MethodEntry]],
    supertype_coverage: Dict[Tuple[str, str], List[str]],
    constructor_coverage: Dict[Tuple[str, str], MethodEntry],
    interface_redundancy: List[Tuple[MethodEntry, List[MethodEntry]]],
    wrapping_redundancy: List[Tuple[str, str, str]],
    missing_methods: List[Tuple[str, str, str]]
):
    """Print comprehensive analysis report."""
    
    print("=" * 80)
    print("ANALYSIS REPORT: Section 2.2 - Monitored READ Operations")
    print("=" * 80)
    
    # Summary statistics
    print(f"\n📊 SUMMARY STATISTICS")
    print(f"   Total entries: {len(entries)}")
    unique_keys = set(e.key for e in entries)
    print(f"   Unique class+method combinations: {len(unique_keys)}")
    categories = set(e.table_category for e in entries)
    print(f"   Categories: {len(categories)}")
    
    # Category breakdown
    print(f"\n📁 ENTRIES BY CATEGORY")
    cat_counts = defaultdict(int)
    for e in entries:
        cat_counts[e.table_category] += 1
    for cat, count in sorted(cat_counts.items(), key=lambda x: -x[1]):
        print(f"   {count:3d} - {cat}")
    
    # 1. Duplicates
    print(f"\n{'='*80}")
    print("🔄 1. DUPLICATE METHODS (same class+method appears multiple times)")
    print("=" * 80)
    
    if duplicates:
        for (cls, method), dup_entries in sorted(duplicates.items()):
            print(f"\n   ⚠️  {cls}.{method}")
            for de in dup_entries:
                print(f"      - Category: '{de.table_category}'")
    else:
        print("   ✅ No duplicates found")
    
    # 2. Supertype coverage
    print(f"\n{'='*80}")
    print("🔗 2. SUPERTYPE COVERAGE (method also monitored in parent class/interface)")
    print("   These might be redundant if monitoring the parent already covers subclasses.")
    print("=" * 80)
    
    if supertype_coverage:
        for (cls, method), sups in sorted(supertype_coverage.items()):
            print(f"\n   ⚠️  {cls}.{method}")
            print(f"      Also monitored in: {', '.join(sorted(set(sups)))}")
    else:
        print("   ✅ No supertype coverage issues found")
    
    # 3. Constructor coverage
    print(f"\n{'='*80}")
    print("🏗️  3. CONSTRUCTOR COVERAGE (instance methods covered by constructor monitoring)")
    print("   If <new> is monitored, subsequent method calls might be blocked at constructor.")
    print("=" * 80)
    
    if constructor_coverage:
        grouped = defaultdict(list)
        for (cls, method), constructor in constructor_coverage.items():
            grouped[constructor.class_name].append(method)
        
        for cls, methods in sorted(grouped.items()):
            print(f"\n   📍 {cls}.<new> potentially covers:")
            for m in sorted(methods):
                print(f"      - {m}")
    else:
        print("   ✅ No constructor coverage issues found")
    
    # 4. Interface implementation redundancy
    print(f"\n{'='*80}")
    print("🔀 4. INTERFACE/IMPLEMENTATION REDUNDANCY")
    print("   Interface methods monitored along with implementing class methods.")
    print("=" * 80)
    
    if interface_redundancy:
        for iface_entry, impl_entries in interface_redundancy:
            print(f"\n   ⚠️  Interface: {iface_entry.class_name}.{iface_entry.method_name}")
            print(f"      Also monitored in implementations:")
            for impl in impl_entries:
                print(f"         - {impl.class_name}.{impl.method_name}")
    else:
        print("   ✅ No interface redundancy found")
    
    # 5. Wrapping redundancy
    print(f"\n{'='*80}")
    print("📦 5. WRAPPER/WRAPPED REDUNDANCY")
    print("   Wrapper class constructor monitored along with wrapped class constructor.")
    print("=" * 80)
    
    if wrapping_redundancy:
        for wrapper, wrapped, method in wrapping_redundancy:
            print(f"\n   ⚠️  {wrapper}.{method} wraps {wrapped}.{method}")
            print(f"      Both constructors are monitored (might be intentional for defense-in-depth)")
    else:
        print("   ✅ No wrapping redundancy found")
    
    # 6. Missing methods
    print(f"\n{'='*80}")
    print("❓ 6. METHODS NOT FOUND IN PUBLIC API (javap -public)")
    print("=" * 80)
    
    if missing_methods:
        for cls, method, reason in sorted(missing_methods):
            print(f"   ⚠️  {cls}.{method} -> {reason}")
    else:
        print("   ✅ All methods found in public API")
    
    # Final summary
    total_issues = (
        len(duplicates) + 
        len(supertype_coverage) + 
        len(constructor_coverage) + 
        len(interface_redundancy) + 
        len(wrapping_redundancy) +
        len(missing_methods)
    )
    
    print(f"\n{'='*80}")
    print(f"📋 FINAL SUMMARY: {total_issues} potential issues found")
    print("=" * 80)
    print(f"   Duplicates: {len(duplicates)}")
    print(f"   Supertype coverage: {len(supertype_coverage)}")
    print(f"   Constructor coverage: {len(constructor_coverage)}")
    print(f"   Interface redundancy: {len(interface_redundancy)}")
    print(f"   Wrapper redundancy: {len(wrapping_redundancy)}")
    print(f"   Missing methods: {len(missing_methods)}")


def export_csv(entries: List[MethodEntry], output_path: Path):
    """Export entries to CSV."""
    import csv
    with output_path.open('w', newline='') as f:
        writer = csv.writer(f)
        writer.writerow(['Class', 'Method', 'Category'])
        for e in sorted(entries, key=lambda x: (x.class_name, x.method_name)):
            writer.writerow([e.class_name, e.method_name, e.table_category])
    print(f"\n📄 CSV exported to: {output_path}")


def main():
    print(f"Analyzing: {DOC_PATH}\n")
    
    # Parse
    lines = read_section_lines()
    entries = extract_entries(lines)
    
    # Initialize cache
    cache = JavapCache()
    
    # Preload class info
    classes = sorted({e.class_name for e in entries})
    print(f"Loading class info for {len(classes)} classes...")
    for cls in classes:
        cache.get(cls)
    print("Done.\n")
    
    # Analysis
    duplicates = find_duplicates(entries)
    supertype_coverage = find_supertype_coverage(entries, cache)
    constructor_coverage = find_constructor_coverage(entries)
    interface_redundancy = find_interface_implementation_redundancy(entries, cache)
    wrapping_redundancy = find_wrapping_redundancy(entries)
    missing_methods = analyze_missing_methods(entries, cache)
    
    # Report
    print_report(
        entries,
        duplicates,
        supertype_coverage,
        constructor_coverage,
        interface_redundancy,
        wrapping_redundancy,
        missing_methods
    )
    
    # Export
    csv_path = DOC_PATH.parent.parent.parent / "tools" / "read_methods_analysis_enhanced.csv"
    export_csv(entries, csv_path)


if __name__ == "__main__":
    main()
