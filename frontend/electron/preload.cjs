const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('electronAPI', {
    sendMessage: (message) => ipcRenderer.send('message', message),
    onReceiveMessage: (callback) => ipcRenderer.on('message', (_event, data) => callback(data)),
});
