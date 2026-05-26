import fs from 'fs';

const js = fs.readFileSync('service_chunk.js', 'utf-8');

// We want to find references of AN like "AN=" or "let AN" or "const AN" or similar inside service_chunk.js
const targets = [
    'AN =',
    ' AN=',
    ',AN=',
    '{AN}'
];

targets.forEach(t => {
    let idx = js.indexOf(t);
    while (idx !== -1) {
        console.log(`Match for "${t}" at ${idx}:`);
        console.log(js.substring(Math.max(0, idx - 100), idx + t.length + 500).replace(/\s+/g, ' '));
        idx = js.indexOf(t, idx + 1);
    }
});

// Let's search for "SessionPublicService" definitions or paths
// Usually Connect definition looks like:
// const AN = { typeName: "...", methods: { getSharedSessionFile: { ... } } }
// So let's search for '"getSharedSessionFile"' or '"GetSharedSessionFile"' inside service_chunk.js and print its context!
console.log('--- Search for getSharedSessionFile ---');
let idx = js.indexOf('getSharedSessionFile');
if (idx === -1) idx = js.indexOf('GetSharedSessionFile');
if (idx !== -1) {
    console.log('Found getSharedSessionFile inside service_chunk.js at', idx);
    console.log(js.substring(Math.max(0, idx - 200), idx + 800).replace(/\s+/g, ' '));
} else {
    console.log('getSharedSessionFile not found in service_chunk.js');
}
