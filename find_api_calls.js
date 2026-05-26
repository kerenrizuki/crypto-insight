import fs from 'fs';

const js = fs.readFileSync('page_chunk.js', 'utf-8');

// Find all matches for "api." or "manus" or "/share" or "/file" or template literals with $
// Let's print out slices of code around functions that look like fetch or api requests
// A common pattern is fetching `/api/...` or calling `serverUrl`
const patterns = [
    'serverUrl',
    '/share',
    '/file',
    'b.get',
    'fetch(',
    'get('
];

patterns.forEach(pat => {
    let index = 0;
    console.log(`=== Matches for: ${pat} ===`);
    while (true) {
        index = js.indexOf(pat, index);
        if (index === -1) break;
        // Print 150 characters before and after
        const start = Math.max(0, index - 100);
        const end = Math.min(js.length, index + pat.length + 150);
        console.log(`Index ${index}: ... ${js.substring(start, end).replace(/\s+/g, ' ')} ...`);
        index += pat.length + 1; // move past
    }
});
