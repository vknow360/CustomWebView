with open('d:/CustomWebView/adblock_tester.js', 'r', encoding='utf-8') as f:
    text = f.read()

import re
matches = [m.start() for m in re.finditer(r'statusWeights', text)]
for idx in matches:
    print(text[idx-100:idx+200].encode('ascii', errors='replace').decode('ascii'))
    print('---')
