import fs from 'fs';

const jsUrl = 'https://files.manuscdn.com/webapp/_next/static/chunks/app/share/file/%5BfileShareId%5D/page-0b2c1d333204b976.js';

async function run() {
    console.log('Fetching JS chunk...');
    const res = await fetch(jsUrl, {
        headers: {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
        }
    });
    const js = await res.text();
    fs.writeFileSync('page_chunk.js', js);
    console.log('Saved JS, size:', js.length);

    // Let's search for fetch URLs or path parameters inside page_chunk.js
    // Look for string patterns like "/share/file/" or "api" or "http"
    const regex = /["']\/[^"']*["']/g;
    let paths = [];
    let match;
    while ((match = regex.exec(js)) !== null) {
        paths.push(match[0]);
    }
    console.log('Found paths count:', paths.length);
    fs.writeFileSync('paths_in_js.txt', paths.join('\n'));

    // Also look for fetching methods or JSON API calls
    const keyWords = ['fetch', 'axios', 'get', 'api', 'share', 'file'];
    keyWords.forEach(kw => {
        let count = 0;
        let pos = js.indexOf(kw);
        while (pos !== -1) {
            count++;
            pos = js.indexOf(kw, pos + 1);
        }
        console.log(`Keyword "${kw}" count:`, count);
    });
}

run().catch(console.error);
