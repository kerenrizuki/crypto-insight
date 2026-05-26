import fs from 'fs';

const responseData = JSON.parse(fs.readFileSync('api_response.json', 'utf-8'));
const fileUrl = responseData.fileUrl;

async function fetchPRD() {
    console.log('Fetching Markdown PRD from:', fileUrl);
    try {
        const response = await fetch(fileUrl, {
            headers: {
                'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
            }
        });
        console.log('Status:', response.status);
        const mdText = await response.text();
        console.log('Markdown Length:', mdText.length);
        fs.writeFileSync('PRD_Crypto_Token_Screener_App.md', mdText);
        console.log('Successfully saved PRD in PRD_Crypto_Token_Screener_App.md');
        
        // Print first 1000 characters
        console.log('--- PRD PREVIEW ---');
        console.log(mdText.substring(0, 2000));
        console.log('-------------------');
    } catch (e) {
        console.error('Error fetching markdown:', e);
    }
}

fetchPRD();
