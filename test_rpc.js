const url = 'https://api.manus.im/session.v1.SessionPublicService/GetSharedSessionFile';
const body = {
    shareId: '7ead0cc0-868f-4015-9b72-c603bb0ef83f'
};

async function testRPC() {
    console.log('Sending Connect/gRPC JSON request to:', url);
    try {
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(body)
        });
        
        console.log('Status:', response.status);
        console.log('Response Headers:', Object.fromEntries(response.headers.entries()));
        
        const data = await response.text();
        console.log('Response Body Length:', data.length);
        console.log('Response Body:');
        console.log(data);
        
        // Write the data to a file for safe keeping
        fs.writeFileSync('api_response.json', data);
    } catch (e) {
        console.error('Error occurred:', e);
    }
}

import fs from 'fs';
testRPC();
