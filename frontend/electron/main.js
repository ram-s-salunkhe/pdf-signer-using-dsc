/* eslint-disable no-undef */
import { app, BrowserWindow, dialog, net } from "electron";
import { spawn } from "child_process";
import { join } from "path";
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

    app.whenReady().then(() => {
        let backendPath;

        if (isDev) {
            backendPath = join(__dirname, "pdfSigner.exe");
        } else {
            backendPath = join(process.resourcesPath, "app", "electron", "pdfSigner.exe");
        }

        try {
            if (app.isPackaged) {
                backendProcess = spawn(backendPath, [], {
                    detached: true,
                    stdio: "ignore",
                    env: {
                        ...process.env,
                        JAVA_HOME: join(__dirname, "jre")  // Set JAVA_HOME for the bundled JRE
                    }
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

