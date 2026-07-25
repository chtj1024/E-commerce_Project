import axios from "axios";

export const apiClient = axios.create({
  baseURL: "http://localhost:8080/api",
  timeout: 5000,
  withCredentials: true,
});

export const setApiAccessToken = (accessToken: string | null) => {
  if (accessToken) {
    apiClient.defaults.headers.common.Authorization = `Bearer ${accessToken}`;
    return;
  }

  delete apiClient.defaults.headers.common.Authorization;
};
