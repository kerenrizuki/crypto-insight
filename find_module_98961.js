import fs from 'fs';

const html = fs.readFileSync('body.html', 'utf-8');
const srcMatches = html.match(/src="([^"]+)"/g) || [];
const urls = srcMatches.map(m => m.slice(5, -1));

async function run() {
    console.log(`Starting scan of ${urls.length} chunks...`);
    for (const url of urls) {
        if (!url.startsWith('https://files.manuscdn.com/webapp')) continue;
        try {
            const res = await fetch(url, {
                headers: {
                    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
                }
            });
            const js = await res.text();
            if (js.includes('98961') || js.includes('SessionPublicService')) {
                console.log(`Found match in chunk: ${url}`);
                let idx = js.indexOf('98961');
                if (idx !== -1) {
                    console.log('  Context "98961":', js.substring(Math.max(0, idx - 100), idx + 300).replace(/\s+/g, ' '));
                }
                let idx2 = js.indexOf('SessionPublicService');
                if (idx2 !== -1) {
                    console.log('  Context "SessionPublicService":', js.substring(Math.max(0, idx2 - 50), idx2 + 350).replace(/\s+/g, ' '));
                }
                // Save this chunk file locally
                fs.writeFileSync('service_chunk.js', js);
            }
        } catch (e) {
            console.log('Error scanning', url, e.message);
        }
    }
}

run();
