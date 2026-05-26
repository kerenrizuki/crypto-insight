import fs from 'fs';

const html = fs.readFileSync('body.html', 'utf-8');
const searchStr = '7ead0cc0-868f-4015-9b72-c603bb0ef83f';

let index = 0;
while (true) {
    index = html.indexOf(searchStr, index);
    if (index === -1) break;
    const start = Math.max(0, index - 300);
    const end = Math.min(html.length, index + searchStr.length + 300);
    console.log(`Match at ${index}:`);
    console.log(html.substring(start, end));
    console.log('\n=====================================\n');
    index += searchStr.length + 1;
}
