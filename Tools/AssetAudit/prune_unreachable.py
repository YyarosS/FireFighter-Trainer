#!/usr/bin/env python3
"""Аудит ассетов Unity: поиск и удаление файлов, недостижимых из «корней».

Зачем: после удаления демонстрационных объектов из сцены часть ассетов
шаблона (туториал, видео, демо-префабы, неиспользуемые материалы) больше
ни на что не ссылается. Список таких файлов вручную поддерживать нельзя,
поэтому он вычисляется по графу GUID-ссылок.

Алгоритм:
  1. строим карту GUID → путь по всем *.meta в Assets и Library/PackageCache;
  2. берём корни: весь Assets/_Project, конфигурацию фреймворков
     (Settings, XR, XRI, CompositionLayers, TextMesh Pro), все *.asmdef
     и ProjectSettings/*.asset;
  3. обходом в ширину собираем все GUID, на которые ссылаются корни
     (и далее по цепочке) в текстовых ассетах (.unity, .prefab, .asset,
     .mat, .shader, .controller, .anim и т.д.);
  4. файлы Assets, не попавшие в достижимое множество, считаются
     неиспользуемыми.

Все пути внутри скрипта — относительные от корня проекта.

По умолчанию работает в режиме отчёта; удаление — только с --apply.
Запуск: python3 -I Tools/AssetAudit/prune_unreachable.py [--apply]
"""

from __future__ import annotations

import argparse
import os
import re
import sys
from collections import deque

ASSETS = "Assets"
PACKAGE_CACHE = "Library/PackageCache"
PROJECT_SETTINGS = "ProjectSettings"

GUID_IN_TEXT = re.compile(r"guid:\s*([0-9a-fA-F]{32})")
GUID_IN_META = re.compile(r"^guid:\s*([0-9a-fA-F]{32})", re.MULTILINE)

# Каталоги, содержимое которых считается используемым по определению.
ROOT_DIRS = (
    "Assets/_Project",
    "Assets/Settings",
    "Assets/XR",
    "Assets/XRI",
    "Assets/CompositionLayers",
    "Assets/TextMesh Pro",
)

ROOT_FILES = ("Assets/DefaultVolumeProfile.asset",)

# Расширения, в которых имеет смысл искать GUID-ссылки.
TEXT_ASSET_EXTENSIONS = {
    ".unity", ".prefab", ".asset", ".mat", ".shader", ".shadergraph",
    ".shadersubgraph", ".controller", ".overridecontroller", ".anim",
    ".playable", ".mixer", ".physicmaterial", ".physicsmaterial2d",
    ".guiskin", ".fontsettings", ".inputactions", ".scenetemplate",
    ".asmdef", ".asmref", ".json", ".txt", ".csv", ".yaml", ".yml",
}

# Служебные файлы, которые не являются ассетами Unity.
NON_ASSET_NAMES = {".gitkeep", ".DS_Store", "Thumbs.db", "desktop.ini"}

# Скрипты не удаляются никогда: зависимости C#-типов не выражаются
# GUID-ссылками, поэтому аудит по графу ассетов не может доказать,
# что скрипт не нужен другому скрипту.
PROTECTED_EXTENSIONS = {".cs", ".asmdef", ".asmref"}

# Защита от ошибки в аудите: эти каталоги не удаляются никогда.
PROTECTED_PREFIXES = ("Assets/_Project", "Assets/Settings", "Assets/XR",
                      "Assets/XRI", "Assets/CompositionLayers", "Assets/TextMesh Pro")


def normalize(path: str) -> str:
    return os.path.normpath(path).replace(os.sep, "/")


def collect_guid_map(directory: str) -> dict[str, str]:
    """GUID → путь к ассету (без суффикса .meta)."""
    mapping: dict[str, str] = {}
    if not os.path.isdir(directory):
        return mapping
    for root, _dirs, files in os.walk(directory):
        for name in files:
            if not name.endswith(".meta"):
                continue
            meta_path = os.path.join(root, name)
            try:
                with open(meta_path, "r", encoding="utf-8", errors="ignore") as handle:
                    match = GUID_IN_META.search(handle.read())
            except OSError:
                continue
            if match:
                mapping[match.group(1).lower()] = normalize(meta_path[: -len(".meta")])
    return mapping


def iter_references(path: str):
    """GUID-ссылки, встречающиеся в тексте ассета."""
    extension = os.path.splitext(path)[1].lower()
    if extension not in TEXT_ASSET_EXTENSIONS:
        return
    try:
        with open(path, "r", encoding="utf-8", errors="ignore") as handle:
            content = handle.read()
    except OSError:
        return
    for match in GUID_IN_TEXT.finditer(content):
        yield match.group(1).lower()


def collect_roots() -> list[str]:
    roots: list[str] = []

    for relative in ROOT_DIRS:
        if not os.path.isdir(relative):
            continue
        for current, _dirs, files in os.walk(relative):
            for name in files:
                if name.endswith(".meta"):
                    continue
                roots.append(normalize(os.path.join(current, name)))

    for relative in ROOT_FILES:
        if os.path.isfile(relative):
            roots.append(normalize(relative))

    if os.path.isdir(ASSETS):
        for current, _dirs, files in os.walk(ASSETS):
            for name in files:
                if name.endswith(".asmdef") or name.endswith(".asmref"):
                    roots.append(normalize(os.path.join(current, name)))

    if os.path.isdir(PROJECT_SETTINGS):
        for name in os.listdir(PROJECT_SETTINGS):
            if name.endswith(".asset"):
                roots.append(normalize(os.path.join(PROJECT_SETTINGS, name)))

    return roots


def compute_reachable(assets_guids: dict[str, str]) -> set[str]:
    """
    Достижимые файлы внутри Assets.

    Важно: карта GUID строится только по Assets. Исходники сэмплов в
    Library/PackageCache содержат те же GUID, что и импортированные копии,
    и если учитывать пакеты, ссылка разрешится в путь пакета, а файл в
    Assets ошибочно попадёт в «неиспользуемые».
    """
    reachable: set[str] = set()
    visited_guids: set[str] = set()
    queue: deque[str] = deque()

    def add_file(path: str) -> None:
        reachable.add(normalize(path))
        for guid in iter_references(path):
            if guid not in visited_guids:
                visited_guids.add(guid)
                queue.append(guid)

    roots = collect_roots()
    for path in roots:
        add_file(path)

    guid_by_path = {normalize(path): guid for guid, path in assets_guids.items()}
    for path in roots:
        guid = guid_by_path.get(normalize(path))
        if guid and guid not in visited_guids:
            visited_guids.add(guid)
            queue.append(guid)

    while queue:
        guid = queue.popleft()
        path = assets_guids.get(guid)
        if path is None:
            continue  # встроенный ресурс Unity или ассет пакета
        if path in reachable:
            continue
        add_file(path)

    return reachable


def collect_unused(reachable: set[str]) -> list[str]:
    unused: list[str] = []
    for current, _dirs, files in os.walk(ASSETS):
        for name in files:
            if name.endswith(".meta") or name in NON_ASSET_NAMES:
                continue
            path = normalize(os.path.join(current, name))
            if os.path.splitext(path)[1].lower() in PROTECTED_EXTENSIONS:
                continue
            if path not in reachable:
                unused.append(path)
    return sorted(unused)


def grouped_sizes(paths: list[str]) -> dict[str, int]:
    sizes: dict[str, int] = {}
    for path in paths:
        parts = path.split("/")
        key = "/".join(parts[:3]) if len(parts) > 3 else "/".join(parts[:2])
        try:
            sizes[key] = sizes.get(key, 0) + os.path.getsize(path)
        except OSError:
            pass
    return sizes


def human(size: int) -> str:
    value = float(size)
    for unit in ("Б", "КБ", "МБ", "ГБ"):
        if value < 1024 or unit == "ГБ":
            return f"{value:.1f} {unit}"
        value /= 1024
    return f"{value:.1f} ГБ"


def is_protected(path: str) -> bool:
    return any(path == prefix or path.startswith(prefix + "/") for prefix in PROTECTED_PREFIXES)


def main() -> int:
    parser = argparse.ArgumentParser(description="Удаление недостижимых ассетов Unity")
    parser.add_argument("--apply", action="store_true", help="удалить файлы (по умолчанию только отчёт)")
    parser.add_argument("--list", action="store_true", help="напечатать все неиспользуемые пути")
    parser.add_argument("--project", default=".", help="корень Unity-проекта")
    args = parser.parse_args()

    os.chdir(os.path.abspath(args.project))

    assets_guids = collect_guid_map(ASSETS)
    reachable = compute_reachable(assets_guids)
    unused = collect_unused(reachable)
    total = sum(os.path.getsize(path) for path in unused if os.path.isfile(path))

    print(f"Достижимых файлов: {len(reachable)}")
    print(f"Неиспользуемых файлов: {len(unused)} ({human(total)})")
    print()
    for directory, size in sorted(grouped_sizes(unused).items(), key=lambda item: -item[1]):
        print(f"  {human(size):>10}  {directory}")

    if args.list:
        print("\nНеиспользуемые файлы:")
        for path in unused:
            print("  " + path)

    protected = [path for path in unused if is_protected(path)]
    if protected:
        print("\nОШИБКА: в список на удаление попали защищённые файлы. Удаление отменено.")
        for path in protected[:20]:
            print("  " + path)
        return 2

    if not args.apply:
        print("\nРежим отчёта. Для удаления запустите с --apply.")
        return 0

    removed = 0
    for path in unused:
        for candidate in (path, path + ".meta"):
            if os.path.isfile(candidate):
                os.remove(candidate)
                removed += 1

    # Чистка опустевших каталогов и их .meta.
    # Проверяем фактическое содержимое: списки dirs/files из os.walk
    # кэшируются до удаления и после очистки уже не отражают состояние.
    for current, _dirs, _files in os.walk(ASSETS, topdown=False):
        if os.listdir(current):
            continue
        if normalize(current) == ASSETS or is_protected(normalize(current)):
            continue
        os.rmdir(current)
        meta = current + ".meta"
        if os.path.isfile(meta):
            os.remove(meta)
            removed += 1

    print(f"\nУдалено файлов: {removed}")
    return 0


if __name__ == "__main__":
    sys.exit(main())