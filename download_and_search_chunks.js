import fs from 'fs';

const html = fs.readFileSync('body.html', 'utf-8');

// Find all src in <script src="...">
const srcMatches = html.match(/src="([^"]+)"/g) || [];
const urls = srcMatches.map(m => m.slice(5, -1));

console.log('Found JS chunk URLs:', urls.length);

async function search() {
    for (const url of urls) {
        if (!url.startsWith('https://files.manuscdn.com/webapp')) continue;
        console.log('Scanning URL:', url);
        try {
            const res = await fetch(url, {
                headers: {
                    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
                }
            });
            const content = await res.text();
            
            // Search inside chunk
            const keywords = ['/shares/', '/share/', '/file/', 'api.manus.im', 'v1/share', 'v1/shares'];
            for (const kw of keywords) {
                if (content.includes(kw)) {
                    console.log(`  -> Found keyword "${kw}" inside chunk!`);
                    // Print some context around it
                    let idx = content.indexOf(kw);
                    while (idx !== -1) {
                        console.log(`    Context: ... ${content.substring(Math.max(0, idx - 50), Math.min(content.length, idx + kw.length + 120)).replace(/\s+/g, ' ')} ...`);
                        idx = content.indexOf(kw, idx + 1);
                    }
                }
            }
        } catch (e) {
            console.log('Error scanning:', url, e.message);
        }
    }
}

search();
