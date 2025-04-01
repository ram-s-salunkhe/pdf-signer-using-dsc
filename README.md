# PDF Signer Build Process

## To Create JAR File (Branch: `pdf-signer-jar-file`)

1. **Install Dependencies:**
   - Run the following command in the backend folder:
     ```bash
     npm install
     ```

2. **Bundle Frontend:**
   - Navigate to the frontend directory and run:
     ```bash
     npm run build
     ```

3. **Copy Frontend Files:**
   - After the build completes, copy all files from the `dist` folder and paste them into the `static` folder in the backend directory.
   - Path: `backend/src/main/resources/static`

4. **Package Backend:**
   - Navigate to the backend directory and run:
     ```bash
     mvn clean package
     mvn package
     ```

5. **Locate JAR File:**
   - After running the above commands, the JAR file will be available in:
     - `backend/src/target`

6. **Run the JAR File:**
   - Copy the JAR file to any location (e.g., `D:/`).
   - Open Command Prompt in that folder and run:
     ```bash
     java -jar jarfile.jar
     ```

---

## To Create Electron App (Branch: `pdf-signer-electron-app`)

1. **Install Dependencies:**
   - Run the following command in the frontend folder:
     ```bash
     npm install
     ```

2. **Install JDK 8:**
   - Ensure that JDK 8 is installed on your system (mandatory).

3. **Create EXE from JAR File:**
   - Use **Launch4j** software to create an EXE file from the JAR file.
   - Paste the EXE file into the `frontend/electron` folder.

4. **Copy JAR File:**
   - Copy the JAR file and paste it into the `frontend/electron` folder.

5. **Package Electron App:**
   - Run the following command in the frontend folder:
     ```bash
     npm run package
     ```

6. **Locate the Electron EXE:**
   - After running the above command, the Electron EXE will be available in the `release` folder under:
     - `PDFSigner setup 0.0.0.exe`

7. **Install Electron App:**
   - The EXE file will install the application like a desktop application. Users need to have JDK 8 installed on their system.

8. **Run Without Installing:**
   - If you only want to run the app without installing, navigate to the `release` folder:
     - `win-unpacked`
     - Locate and run: `PDFSigner.exe`
