import fs from 'fs';

const content = fs.readFileSync('next_f.txt', 'utf-8');
const lines = content.split('\n');

let allStrings = [];

for (const line of lines) {
    // Parse the push array. Each line is like self.__next_f.push([1,"..."])
    const match = line.match(/self\.__next_f\.push\(\[(.*)\]\)/);
    if (!match) continue;
    const arrayContent = match[1];
    
    // The array content usually has structure: index, string_data
    // Let's use a simple JSON.parse to parse the whole array if we wrap it in []
    try {
        const parsed = JSON.parse('[' + arrayContent + ']');
        if (parsed && parsed.length >= 2) {
            allStrings.push(parsed[1]);
        }
    } catch (e) {
        // Fallback: extract string via regex
        const strMatch = arrayContent.match(/"(.*)"/);
        if (strMatch) {
            allStrings.push(strMatch[1]);
        }
    }
}

// Write the compiled raw strings to a file
fs.writeFileSync('raw_strings.txt', allStrings.join('\n'));

// Let's filter and clean up strings to get readable parts
// In next_f, strings can be separated by : or have control sequences like "3:HL", etc.
// Let's replace escaped newlines with actual newlines
let cleanText = '';
for (const s of allStrings) {
    if (typeof s === 'string') {
        // Unescape string
        let unescaped = s
            .replace(/\\n/g, '\n')
            .replace(/\\"/g, '"')
            .replace(/\\t/g, '\t');
        cleanText += unescaped + '\n';
    }
}

// Write clean text
fs.writeFileSync('clean_text.txt', cleanText);

// Print first 2000 chars of clean text to console
console.log('--- PREVIEW ---');
console.log(cleanText.substring(0, 3000));
console.log('--- END PREVIEW ---');
