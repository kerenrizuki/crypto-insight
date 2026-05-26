import fs from 'fs';

const html = fs.readFileSync('body.html', 'utf-8');
const srcMatches = html.match(/src="([^"]+)"/g) || [];
const urls = srcMatches.map(m => m.slice(5, -1));

async function run() {
    for (const url of urls) {
        if (!url.startsWith('https://files.manuscdn.com/webapp')) continue;
        try {
            const res = await fetch(url, {
                headers: {
                    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
                }
            });
            const content = await res.text();
            if (content.includes('fileShareId')) {
                console.log('Found "fileShareId" in:', url);
                // Print all context occurrences
                let idx = content.indexOf('fileShareId');
                while (idx !== -1) {
                    console.log(`  Context: ... ${content.substring(Math.max(0, idx - 100), Math.min(content.length, idx + 150)).replace(/\s+/g, ' ')} ...`);
                    idx = content.indexOf('fileShareId', idx + 1);
                }
            }
        } catch (e) {
            console.log('Error:', url, e.message);
        }
    }
}

run();
