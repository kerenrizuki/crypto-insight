import fs from 'fs';

const js = fs.readFileSync('page_chunk.js', 'utf-8');

// Find all matches of "SessionPublicService" and find nearby imports
const term = 'SessionPublicService';
let idx = js.indexOf(term);

while (idx !== -1) {
    // Find the enclosing function block or search backwards 500 chars
    const start = Math.max(0, idx - 1000);
    const sub = js.substring(start, idx + term.length);
    console.log(`--- Context near ${idx} ---`);
    console.log(sub.replace(/\s+/g, ' '));
    
    // Search backward in sub for things like "var ... = t(" or "let ... = t("
    const matches = sub.match(/(?:var|let|const|,)\s*([a-zA-Z0-9_$]+)\s*=\s*[a-zA-Z0-9_$]+\((\d+)\)/g);
    if (matches) {
        console.log('Found variable bindings:', matches);
    }
    
    idx = js.indexOf(term, idx + 1);
}
