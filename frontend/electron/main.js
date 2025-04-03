/* eslint-disable no-undef */
import { app, BrowserWindow, dialog, net } from "electron";
import { spawn } from "child_process";
import { join } from "path";
import { signMessage } from "./AuthSigner.js";
import fetch from "node-fetch"; 
import isDev from "electron-is-dev";
import { fileURLToPath } from "url";
import { dirname } from "path";

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

// Prevent multiple instances
const gotTheLock = app.requestSingleInstanceLock();

if (!gotTheLock) {
    dialog.showErrorBox("App is already running", "Only one instance can be run at a time.");
    app.quit();
} else {
    let mainWindow;
    let backendProcess;

    function checkBackendReady(url, callback) {
        const request = net.request(url);
        request.on("response", (response) => {
            if (response.statusCode === 200) {
                callback(true);
            } else {
                callback(false);
            }
        });
        request.on("error", () => callback(false));
        request.end();
    }

    async function authenticate() {
        try {
            const signedMessage = signMessage("authenticate");
            console.log("Signed Message:", signedMessage); // DEBUG LOG

            if (!signedMessage) {
                console.error("Failed to generate signed message");
                return false;
            }

            const response = await fetch("http://localhost:8081/auth/verify", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ signedMessage }),
            });

            const result = await response.text();
            console.log("Auth Result:", result);

            return result === "Authenticated";
        } catch (error) {
            console.error("Authentication request failed:", error);
            return false;
        }
    }

    app.whenReady().then(() => {
        let backendPath;

        if (isDev) {
            console.log("Running in development mode");
            console.log("Current directory:", __dirname);
            console.log("Current file:", __filename);

            backendPath = join(__dirname, "AuthPdfSigner.exe");
        } else {
            backendPath = join(process.resourcesPath, "app", "electron", "AuthPdfSigner.exe");
        }

        console.log(`Starting backend from: ${backendPath}`);

        try {
            if (app.isPackaged) {
                backendProcess = spawn(backendPath, [], {
                    detached: true,
                    stdio: "ignore",
                });
            } else {
                backendProcess = spawn(backendPath, [], { windowsHide: true });
            }

            backendProcess.unref();
        } catch (error) {
            console.error("Failed to start backend:", error);
            app.quit();
            return;
        }

        // Wait for backend before authenticating
        const checkInterval = setInterval(() => {
            checkBackendReady("http://localhost:8081", async (isReady) => {
                if (isReady) {
                    clearInterval(checkInterval);

                    const isAuthenticated = await authenticate();
                    if (!isAuthenticated) {
                        console.log("Authentication failed. Closing app...");
                        app.quit();
                        return;
                    }

                    // Authentication successful, now create the window
                    mainWindow = new BrowserWindow({
                        width: 1200,
                        height: 800,
                        webPreferences: {
                            nodeIntegration: true,
                            contextIsolation: false,
                        },
                    });

                    mainWindow.loadURL("http://localhost:8081");
                }
            });
        }, 1000);
    });

    app.on("window-all-closed", () => {
        if (process.platform !== "darwin") {
            backendProcess?.kill();
            app.quit();
        }
    });
}

