# PDF Signer Build Process

## 1. Install Dependencies  
Run the following command in the frontend folder:  
```bash
npm install
```

## 2. Install JDK 8  
Ensure that **JDK 8** is installed on your system (mandatory).

## 3. Create Keys (Public & Private)  
Generate the RSA key pair using OpenSSL:
```bash
openssl genpkey -algorithm RSA -out private_key.pem  
openssl rsa -pubout -in private_key.pem -out public_key.pem
```
Then, move the generated keys to the correct locations:
- `private_key.pem` → `frontend/electron/keys`
- `public_key.pem` → `backend/src/main/resources/keys`

---

# To Create JAR File

### 1. Bundle the Frontend  
Navigate to the frontend directory and run:
```bash
npm run build
```

### 2. Copy Frontend Files to Backend  
- Copy all files from the `dist` folder.
- Paste them into `backend/src/main/resources/static`.

### 3. Package the Backend  
Navigate to the backend directory and run:
```bash
mvn clean package
mvn package
```

### 4. Locate & Run the JAR File  
- The JAR file will be available at:  
  `backend/src/target`
- To run the JAR file, copy it to any location (e.g., `D:/`) and execute:
  ```bash
  java -jar jarfile.jar
  then open browser goto localhost:8081 
  ```

---

# To Create EXE File

## 1. Create EXE Without Bundling JRE  
Using **Launch4j**:
1. Open **Launch4j**.
2. Under **Output File**, select the location where the `.exe` should be created give name with .exe extension.  
   - Example: `pdfSigner.exe`
3. Select the **JAR file** location.
4. (Optional) Select an **icon** for the EXE.
5. Click the **Build Wrapper** button (⚙️ Settings icon).
6. The EXE file is now created.

## 2. Create EXE with Bundled JRE  
1. Open **Launch4j**.
2. Under **Output File**, choose where to create the `.exe`.
3. Select the **JAR file** location.
4. (Optional) Select an **icon**.
5. Under **Classpath**, select **Custom Classpath** and choose the JAR file location.
6. Under the **JRE tab**:
   - Name the JRE folder as `jre`.
   - Copy the JRE folder from `C:\Program Files\Java\jre1.8.0_441`.
   - Paste it into the same folder where the EXE file is created.
7. Folder structure should be:
   ```
   java_exe (Main Folder)
   ├── jre  (JRE folder)
   ├── pdfSigner.exe
   ```
8. Click the **Build Wrapper** button (⚙️ Settings icon).
9. The EXE file is now created.

---

# To Create Electron App

## 1. Update `frontend/electron` Folder  
Copy and paste the following files/folders into the `frontend/electron` folder:
- **JAR file**
- **EXE file**
- **JRE folder** (created in the step above copy from the above location)

## 2. Package the Electron App  
Run the following command in the frontend folder:
```bash
npm run package
```

## 3. Locate the Electron EXE  
After packaging, the Electron EXE will be available in the `release` folder:
```
release/
└── PDFSigner setup 0.0.0.exe
```

## 4. Install the Electron App  
Running this EXE will install the application as a desktop app.  
**No need to install JDK 8** on the user's system.

## 5. Run Without Installing  
If you don't want to install it, navigate to:
```
release/win-unpacked/PDFSigner.exe
```
and run **PDFSigner.exe** directly from this folder.
