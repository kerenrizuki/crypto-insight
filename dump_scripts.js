import * as fsLib from 'fs';

const html = fsLib.readFileSync('body.html', 'utf-8');

// Find all matches of <script ...> block
const matches = html.match(/<script\b[^>]*>([\s\S]*?)<\/script>/gi);
console.log('Total script blocks:', matches ? matches.length : 0);

if (matches) {
    matches.forEach((m, i) => {
        const text = m.replace(/<script\b[^>]*>/i, '').replace(/<\/script>/i, '').trim();
        console.log(`Script ${i} length:`, text.length);
        if (text.includes('7ead0cc0-868f-4015-9b72-c603bb0ef83f') || text.includes('Firebase') || text.includes('PRD')) {
            console.log(`Script ${i} contains keywords!`);
        }
        if (text.length > 0 && text.length < 500) {
            console.log(`Script ${i} preview:`, text);
        } else if (text.length >= 500) {
            console.log(`Script ${i} preview (first 200):`, text.substring(0, 200));
        }
    });

    // Also let's print all script tags metadata (attributes)
    console.log('Script attributes:');
    const attrRegex = /<script\b([^>]*)>/gi;
    let match;
    while ((match = attrRegex.exec(html)) !== null) {
        console.log(match[1]);
    }
}
