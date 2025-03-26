import { useState, useEffect } from "react";
import axios from "axios";
import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

export default function PdfSigner() {
  const [files, setFiles] = useState(null);
  const [position, setPosition] = useState("left-top");
  const [dscList, setDscList] = useState([]);
  const [selectedDsc, setSelectedDsc] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    fetchDscList();
  }, []);

  const fetchDscList = async () => {
    try {
      const response = await axios.get("http://localhost:8081/api/pdf/dsc-list");
      if (response.data.length > 0) {
        setDscList(response.data);
        setSelectedDsc(response.data[0].thumbprint);
      }
    } catch (error) {
      console.error("Error fetching DSCs:", error);
      toast.error("❌ Failed to load Digital Signature Certificates.");
    }
  };

  const handleFileChange = (event) => {
    const selectedFiles = event.target.files;
    setFiles(selectedFiles.length > 1 ? Array.from(selectedFiles) : selectedFiles);
  };

  const handleUpload = async () => {
    if (!files || (files instanceof FileList && files.length === 0)) {
      toast.warn(" Please select at least one PDF file to sign.");
      return;
    }
    if (!selectedDsc) {
      toast.warn(" Please select a DSC certificate.");
      return;
    }
  
    setIsLoading(true);
    const formData = new FormData();
    const fileArray = files instanceof FileList ? Array.from(files) : files;
  
    for (let file of fileArray) {
      formData.append("files", file);
    }
    formData.append("position", position);
    formData.append("thumbprint", selectedDsc); // ✅ Ensure alias is a string
  
    try {
      const response =  await axios.post("http://localhost:8081/api/pdf/sign-multiple", formData, {
        headers: { "Content-Type": "multipart/form-data" },
        responseType: "blob",
      });
      
      // ✅ Get content type from response headers
      const contentType = response.headers["content-type"];

      // ✅ Determine the correct file extension
      let fileExtension = contentType === "application/pdf" ? "pdf" : "zip";
      let defaultFileName = `signed_pdfs.${fileExtension}`;

      // ✅ Extract filename from headers if available
      let filename = response.headers["content-disposition"]?.split("filename=")[1] || defaultFileName;
      filename = filename.replace(/['"]/g, ""); // Remove any quotes

      // ✅ Create a download link for ZIP or PDF response
      const blob = new Blob([response.data], { type: contentType });
      const link = document.createElement("a");
      link.href = window.URL.createObjectURL(blob);
      link.download = filename;
      
      // ✅ Trigger download
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);

      toast.success("PDFs signed and downloaded successfully!");
    } catch (error) {
      console.log("Error signing PDFs:", error.response.data.text());
      const respErr = await error.response.data.text()
      toast.error( respErr ||`❌ Failed to sign PDFs`);
    }
  
    setIsLoading(false);
  };
  
  
  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100 p-4">
      <ToastContainer position="top-right" autoClose={3000} />
      <div className="bg-white shadow-lg p-6 rounded-lg w-96">
        <h2 className="text-xl font-semibold mb-4 text-center text-blue-600">PDF Signer</h2>

        <input 
          type="file" 
          multiple 
          accept="application/pdf" 
          onChange={handleFileChange} 
          className="mb-3 border p-2 w-full"
        />

        <select
          value={position}
          onChange={(e) => setPosition(e.target.value)}
          className="mb-3 border p-2 w-full"
        >
          <option value="left-top">Top Left</option>
          <option value="right-top">Top Right</option>
          <option value="left-bottom">Bottom Left</option>
          <option value="right-bottom">Bottom Right</option>
        </select>

        {/* ✅ DSC Selection Dropdown */}
        <select
          onChange={(e) => setSelectedDsc(e.target.value)}
          value={selectedDsc}
          className="mb-3 border p-2 w-full max-w-full overflow-hidden text-ellipsis"
        >
          <option value="" disabled>Select certificate</option>
          {dscList.map((dsc) => {
            // Extract CN (Common Name)
            const match = dsc.name.match(/CN=([^,]*)/);
            const commonName = match ? match[1] : dsc.name;

            // Extract last 4 digits of the thumbprint
            const thumbprintLast4 = dsc.thumbprint.slice(-4);
            
            return (
              <option key={dsc.thumbprint} value={dsc.thumbprint} 
              style={{ color: dsc.isExpired === "true" ? "red" : "green" }}
              >
                {commonName} - {thumbprintLast4} (Expires: {dsc.expiryDate})
              </option>
            );
          })}
        </select>

        <button
          onClick={handleUpload}
          className="bg-blue-500 text-white px-4 py-2 rounded w-full"
          disabled={isLoading}
        >
          {isLoading ? "Signing..." : "Sign PDF"}
        </button>
      </div>
    </div>
  );
}
