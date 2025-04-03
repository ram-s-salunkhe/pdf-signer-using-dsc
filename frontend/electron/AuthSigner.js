// import process from "process"; // ✅ Explicitly import process
import crypto from "crypto";
import { readFileSync } from "fs";
import { fileURLToPath } from "url";
import { dirname, join } from "path";

// Convert `import.meta.url` to a file path
const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const privateKeyPath = join(__dirname, "keys", "private_key.pem");

export function signMessage(message) {
    try {
        const privateKey = readFileSync(privateKeyPath, "utf8");
        const signer = crypto.createSign("SHA256");
        signer.update(message);
        signer.end();

        const signature = signer.sign(privateKey, "base64");
        return signature;
    } catch (error) {
        console.error("Error signing message:", error);
        return null;
    }
}

// For testing manually
// if (process.argv[1] === fileURLToPath(import.meta.url)) {
//     const signedMessage = signMessage("authenticate");
//     console.log("Signed Message:", signedMessage);
// }
