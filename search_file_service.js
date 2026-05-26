import fs from 'fs';

const js = fs.readFileSync('page_chunk.js', 'utf-8');

const term = 'SessionPublicService';
let idx = js.indexOf(term);
if (idx !== -1) {
    console.log('Found SessionPublicService!');
    const start = Math.max(0, idx - 100);
    const end = Math.min(js.length, idx + term.length + 800);
    console.log(js.substring(start, end).replace(/\s+/g, ' '));
} else {
    console.log('Not found');
}
