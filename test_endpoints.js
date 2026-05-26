const id = '7ead0cc0-868f-4015-9b72-c603bb0ef83f';

const endpoints = [
    `https://api.manus.im/v1/share/file/${id}`,
    `https://api.manus.im/v1/shares/files/${id}`,
    `https://api.manus.im/api/share/file/${id}`,
    `https://api.manus.im/v1/share/${id}`,
    `https://api.manus.im/share/file/${id}`,
    `https://manus.im/api/share/file/${id}`,
    `https://api.manus.im/v1/share/file?id=${id}`,
    `https://api.manus.im/v1/file/${id}`,
    `https://api.manus.im/v1/files/${id}`,
    `https://api.manus.im/v1/share/file/detail/${id}`,
    `https://api.manus.im/v1/share-file/${id}`,
    `https://api.manus.im/v1/shares/${id}`
];

async function probe() {
    for (const url of endpoints) {
        console.log('Probing:', url);
        try {
            const res = await fetch(url, {
                headers: {
                    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
                    'Accept': 'application/json'
                }
            });
            console.log('  Status:', res.status);
            if (res.status === 200) {
                const text = await res.text();
                console.log('  Success! Substring:', text.substring(0, 1000));
                console.log('  Full URL worked:', url);
                break;
            }
        } catch (e) {
            console.log('  Error:', e.message);
        }
    }
}

probe();
