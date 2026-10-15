with open('d:/CustomWebView/adblock_tester.js', 'r', encoding='utf-8') as f:
    text = f.read()

import re
checks = re.findall(r'id:"([^"]+)",type:"([^"]+)"', text)
print(f"Total checks: {len(checks)}")
for c in checks:
    print(" ", c)
