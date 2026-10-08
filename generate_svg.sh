#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
UML_DIR="$SCRIPT_DIR/uml"
PNG_DIR="$UML_DIR/svg"
TMP_DIR="$(mktemp -d)"

cleanup() { rm -rf "$TMP_DIR"; }
trap cleanup EXIT

mkdir -p "$PNG_DIR"

generated=0
skipped=0

for md in "$UML_DIR"/*.md; do
  base="$(basename "$md" .md)"
  out_png="$PNG_DIR/${base}.svg"
  tmp_mmd="$TMP_DIR/${base}.mmd"

  # extract first mermaid block from markdown
  python3 - "$md" "$tmp_mmd" <<'PYEOF'
import sys, re

src, dst = sys.argv[1], sys.argv[2]
with open(src, encoding="utf-8") as f:
    content = f.read()

blocks = re.findall(r"```mermaid\n(.*?)```", content, re.DOTALL)
if not blocks:
    sys.exit(1)

# join multiple blocks separated by a newline (rare)
with open(dst, "w", encoding="utf-8") as f:
    f.write("\n".join(b.strip() for b in blocks))
PYEOF
  ec=$?

  if [[ $ec -ne 0 ]]; then
    echo "  skip: $base (no mermaid block)"
    ((skipped++)) || true
    continue
  fi

  echo "  render: $base -> $(basename "$out_png")"
  npx --yes @mermaid-js/mermaid-cli \
    -i "$tmp_mmd" \
    -o "$out_png" \
    --quiet 2>/dev/null
  ((generated++)) || true
done

echo ""
echo "Done. Generated: $generated  Skipped: $skipped"
