import * as fsLib from 'fs';

const html = fsLib.readFileSync('body.html', 'utf-8');

// Strip tags
let clean = html
    .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '')
    .replace(/<style\b[^<]*(?:(?!<\/style>)<[^<]*)*<\/style>/gi, '')
    .replace(/<[^>]+>/g, ' ')
    .replace(/&nbsp;/g, ' ')
    .replace(/&amp;/g, '&')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/\s+/g, ' ')
    .trim();

fsLib.writeFileSync('body_plain.txt', clean);
console.log('Plain text length:', clean.length);
console.log('Preview:');
console.log(clean.substring(0, 3000));
