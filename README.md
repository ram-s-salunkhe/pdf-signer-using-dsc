# PDF Signer Build Process
**Install Dependencies:**
   - Run the following command in the frontend folder:
     ```bash
     npm install
     ```

**Install JDK 8:**
   - Ensure that JDK 8 is installed on your system (mandatory).

**Create keys:**
   - create public_key and private_key by using openssl
   - openssl genpkey -algorithm RSA -out private_key.pem 
   - openssl rsa -pubout -in private_key.pem -out public_key.pem
   - copy this keys and paste it into the 
      - private_key.pem into the frontend/electron/keys
      - public_key.pem into the backend/src/main/resources/keys

## To Create JAR File

1. **Bundle Frontend:**
   - Navigate to the frontend directory and run:
     ```bash
     npm run build
     ```

2. **Copy Frontend Files:**
   - After the build completes, copy all files from the `dist` folder and paste them into the `static` folder in the backend directory.
   - Path: `backend/src/main/resources/static`
   
3. **Package Backend:**
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

## To Create EXE file
1. **create exe file without bundle with JRE:**
first install **launch4j**
Run launch4j in output file navigate to location where you want to create .exe file and give file name with .exe extension ex. pdfSigner.exe
then, select the jar file location
if you want a icon select icon file location
and then click build wrapper (it is looking like setting button)
now, exe is get created


2. **create exe file bundle with JRE:**
Run launch4j in output file navigate to location where you want to create .exe file and give file name with .exe extension ex. pdfSigner.exe
then, select the jar file location
if you want a icon select icon file location
go to classpath select custom classpath select the location where jar file is located
go to jre tab give name as jre and create jre name folder in the location where you give the output file location and paste jre from C:\Program Files\Java\jre1.8.0_441

ex: java_exe (main folder)
      - jre (this is folder)
      - pdfSigner.exe
and then click build wrapper (it is looking like setting button)
now, exe is get created

## To Create Electron App

1. **Do chages in frontend/electron:**
   - copy and paste jar file into the `frontend/electron` folder
   - copy and paste exe file into the `frontend/electron` folder
   - copy and paste jre folder into the `frontend/electron` folder which was created during **create exe file bundle with JRE:**

5. **Package Electron App:**
   - Run the following command in the frontend folder:
     ```bash
     npm run package
     ```

6. **Locate the Electron EXE:**
   - After running the above command, the Electron EXE will be available in the `release` folder under:
     - `PDFSigner setup 0.0.0.exe`

7. **Install Electron App:**
   - The EXE file will install the application like a desktop application. Users does not need to have JDK 8 installed on their system.

8. **Run Without Installing:**
   - If you only want to run the app without installing, navigate to the `release` folder:
     - `win-unpacked`
     - Locate and run: `PDFSigner.exe`
