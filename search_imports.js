import fs from 'fs';

const js = fs.readFileSync('page_chunk.js', 'utf-8');

const index = 55423;
const start = Math.max(0, index - 3000);
const end = index + 500;
console.log('--- Segment before index 55423 ---');
console.log(js.substring(start, end).replace(/\s+/g, ' '));
