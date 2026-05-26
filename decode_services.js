import fs from 'fs';

const url = 'https://files.manuscdn.com/webapp/_next/static/chunks/24508-3c1f5c4a639dc953.js';

async function run() {
    console.log('Fetching service chunk...');
    const res = await fetch(url, {
        headers: {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
        }
    });
    const js = await res.text();
    fs.writeFileSync('service_chunk.js', js);
    console.log('Saved service_chunk.js, size:', js.length);

    // Let's find occurrences of AN or AW or other variables to see what gRPC paths they represent!
    // Connect/gRPC definitions look like: "butterfly_effect.session.v1.SessionPublicService" or similar.
    // Let's search for "SessionPublicService" inside chunk
    let idx = js.indexOf('SessionPublicService');
    while (idx !== -1) {
        console.log(`Context direct near SessionPublicService at ${idx}:`);
        console.log(js.substring(Math.max(0, idx - 100), idx + 300).replace(/\s+/g, ' '));
        idx = js.indexOf('SessionPublicService', idx + 1);
    }

    // Let's also look for all strings containing "v1" or "Service" or dots like "session.v1." or similar
    const regex = /["'](?:[a-zA-Z0-9_$]+\.)+[a-zA-Z0-9_$]+["']/g;
    let serviceNames = [];
    let match;
    while ((match = regex.exec(js)) !== null) {
        serviceNames.push(match[0]);
    }
    console.log('Found service-like string keys:', serviceNames.slice(0, 30));
}

run();
