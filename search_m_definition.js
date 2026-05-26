import fs from 'fs';

const js = fs.readFileSync('page_chunk.js', 'utf-8');

// Let's search for "SessionPublicService" in page_chunk.js and see if it's imported.
// In Webpack/NextJs, modules are often imported at the beginning of the file or via require (like t(12345))
// Let's find occurrences of m = t( or similar.
const regex = /SessionPublicService/gi;
let idx = js.indexOf('SessionPublicService');
while (idx !== -1) {
    console.log(`Index ${idx}:`);
    console.log(js.substring(Math.max(0, idx - 150), Math.min(js.length, idx + 250)).replace(/\s+/g, ' '));
    idx = js.indexOf('SessionPublicService', idx + 1);
}
