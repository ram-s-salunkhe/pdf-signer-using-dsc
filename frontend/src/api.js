import axios from 'axios';
import dotenv from 'dotenv';
dotenv.config();

const API_BASE_URL = dotenv.API_BASE_URL

export const getData = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/example-endpoint`);
    return response.data;
  } catch (error) {
    console.error("Error fetching data:", error);
    return null;
  }
};
