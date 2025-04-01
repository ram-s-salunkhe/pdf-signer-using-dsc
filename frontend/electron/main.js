/* eslint-disable no-undef */
import { app, BrowserWindow } from "electron";
import { spawn } from "child_process";
import { join } from "path";
import { net } from "electron"; // Import net module to check if backend is running

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
    const backendPath = app.isPackaged
        ? join(process.resourcesPath, "app", "electron", "pdfSigner.exe")
        : join(__dirname, "electron", "pdfSigner.exe");

    console.log("Starting backend from:", backendPath);

    backendProcess = spawn(backendPath, [], {
        detached: true,
        stdio: "ignore"
    });

    backendProcess.unref();

    mainWindow = new BrowserWindow({
        width: 1200,
        height: 800,
        webPreferences: {
            nodeIntegration: true,
            contextIsolation: false,
        },
    });

    // Wait for backend before loading frontend
    const checkInterval = setInterval(() => {
        checkBackendReady("http://localhost:8081", (isReady) => {
            if (isReady) {
                clearInterval(checkInterval);
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
