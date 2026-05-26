import fs from 'fs';

const url = 'https://manus.im/share/file/7ead0cc0-868f-4015-9b72-c603bb0ef83f';

async function run() {
    console.log('Fetching...');
    const res = await fetch(url, {
        headers: {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
        }
    });
    const html = await res.text();
    fs.writeFileSync('body.html', html);
    console.log('Saved html, size:', html.length);

    // Let's search for json string in HTML
    // Next.js page data contains self.__next_f.push lines
    // Let's extract any json parts
    const nextFLines = html.match(/self\.__next_f\.push\(\[(.*?)\]\)/g);
    if (nextFLines) {
        console.log('Found next_f lines:', nextFLines.length);
        fs.writeFileSync('next_f.txt', nextFLines.join('\n'));
    }
}

run().catch(console.error);
