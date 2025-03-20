import { useState } from "react";
import axios from "axios";
import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

export default function PdfSigner() {
  const [file, setFile] = useState(null);
  const [position, setPosition] = useState("left-top");
  const [isLoading, setIsLoading] = useState(false);

  const handleFileChange = (event) => {
    setFile(event.target.files[0]);
  };

  const handlePositionChange = (event) => {
    setPosition(event.target.value);
  };

  const handleUpload = async () => {
    if (!file) {
      toast.warn("⚠️ Please select a PDF file to sign.");
      return;
    }
    setIsLoading(true);
  
    const formData = new FormData();
    formData.append("file", file);
    formData.append("position", position);
  
    try {
      const response = await axios.post("http://localhost:8080/api/pdf/sign", formData, {
        headers: { "Content-Type": "multipart/form-data" },
        responseType: "blob", // Ensure we receive the file as a binary blob
      });
  
      // ✅ Create a download link
      const blob = new Blob([response.data], { type: "application/pdf" });
      const link = document.createElement("a");
      link.href = window.URL.createObjectURL(blob);
      
      // ✅ Set filename dynamically
      const originalName = file.name.replace(".pdf", "") + "_signed.pdf";
      link.download = originalName;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
  
      toast.success("✅ PDF signed and downloaded successfully!");
    } catch (error) {
      console.error("Error signing PDF:", error);
      toast.error("❌ Failed to sign PDF");
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
          accept="application/pdf"
          onChange={handleFileChange}
          className="mb-3 border p-2 w-full"
        />
        <select
          value={position}
          onChange={handlePositionChange}
          className="mb-3 border p-2 w-full"
        >
          <option value="left-top">Top Left</option>
          <option value="right-top">Top Right</option>
          <option value="left-bottom">Bottom Left</option>
          <option value="right-bottom">Bottom Right</option>
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
