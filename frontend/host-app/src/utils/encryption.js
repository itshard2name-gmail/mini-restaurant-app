import JSEncrypt from 'jsencrypt';
import request from './request';

let publicKey = null;
let fetchPromise = null;

async function getPublicKey() {
    if (publicKey) return publicKey;
    if (fetchPromise) return fetchPromise;

    fetchPromise = request.get('/auth/publicKey')
        .then(res => {
            // Check if response is string or object (axios might wrap it)
            // request.js interceptor returns response.data
            // The endpoint returns String, so res should be the PEM string
            publicKey = res;
            fetchPromise = null;
            return publicKey;
        })
        .catch(err => {
            fetchPromise = null;
            console.error('Failed to fetch public key:', err);
            throw err;
        });

    return fetchPromise;
}

export async function encrypt(text) {
    try {
        const key = await getPublicKey();
        const encryptor = new JSEncrypt();
        encryptor.setPublicKey(key);
        return encryptor.encrypt(text);
    } catch (error) {
        console.error('Encryption error:', error);
        return null;
    }
}
