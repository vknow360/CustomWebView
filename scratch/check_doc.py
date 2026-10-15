with open('d:/CustomWebView/src/easy.txt', 'r', encoding='utf-8') as f:
    matches = [l.strip() for l in f if '$document' in l or ',document' in l]
print('Real matches:', len(matches))
for m in matches[:10]:
    print('  ', m)
