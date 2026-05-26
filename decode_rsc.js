import * as fsLib from 'fs';

const html = fsLib.readFileSync('body.html', 'utf-8');

// Compile all script blocks containing self.__next_f.push
const matches = html.match(/self\.__next_f\.push\(\[(.*?)\]\)/g) || [];
console.log('Found pushes:', matches.length);

let combinedData = '';
for (const match of matches) {
    const raw = match.match(/\[(.*)\]/);
    if (!raw) continue;
    try {
        // Evaluate as a JS array to get the string
        const arr = eval('[' + raw[1] + ']');
        if (arr.length >= 2 && typeof arr[1] === 'string') {
            combinedData += arr[1];
        }
    } catch (e) {
        // Fallback simple parsing
        const firstComma = raw[1].indexOf(',');
        if (firstComma !== -1) {
            let strPart = raw[1].substring(firstComma + 1).trim();
            if (strPart.startsWith('"') && strPart.endsWith('"')) {
                strPart = strPart.slice(1, -1);
                combinedData += strPart.replace(/\\"/g, '"').replace(/\\n/g, '\n');
            }
        }
    }
}

fsLib.writeFileSync('combined_rsc.txt', combinedData);
console.log('Combined RSC length:', combinedData.length);

// Let's search inside combinedData for strings.
// NextJS RSC format uses "foo" or :["foo", ...]
// Let's extract any long strings or markdown-like content.
// We can find all double-quoted strings or texts.
// Or simply find any text sequences longer than 15 characters of standard letters, spaces, punctuation.
const regex = /"([^"\\]|\\.)*"/g;
let stringHits = [];
let m;
while ((m = regex.exec(combinedData)) !== null) {
    try {
        const val = JSON.parse(m[0]);
        if (typeof val === 'string' && val.trim().length > 15) {
            stringHits.push(val.trim());
        }
    } catch (e) {}
}

const cleanHits = stringHits.filter(h => !h.includes('static/chunks') && !h.includes('https://') && !h.includes('application/'));
fsLib.writeFileSync('string_hits.txt', cleanHits.join('\n\n---\n\n'));
console.log('Extracted', cleanHits.length, 'meaningful strings');

// Let's print the top 10 longest strings to see if it's the PRD content!
const sortedHits = [...cleanHits].sort((a, b) => b.length - a.length);
console.log('Top longest strings:');
for (let i = 0; i < Math.min(10, sortedHits.length); i++) {
    console.log(`\n--- String #${i} (length: ${sortedHits[i].length}) ---`);
    console.log(sortedHits[i].substring(0, 1000));
}
