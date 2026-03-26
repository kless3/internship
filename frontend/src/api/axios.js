import axios from 'axios';
import keycloak from '../auth/keycloak.js';

const baseURL = import.meta.env.VITE_SERVER_URL ?? '/';
let unauthorizedHandler = null;

export function setUnauthorizedHandler(handler) {
  unauthorizedHandler = typeof handler === 'function' ? handler : null;
}

const api = axios.create({
  baseURL,
  timeout: Number(import.meta.env.VITE_REQUEST_TIMEOUT_MS ?? 15000),
  headers: {
    'Content-Type': 'application/json'
  }
});

api.interceptors.request.use(async (config) => {
  if (keycloak.authenticated) {
    try {
      await keycloak.updateToken(30);
    } catch {
    }

    if (keycloak.token) {
      config.headers.Authorization = `Bearer ${keycloak.token}`;
    }
  }

  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && originalRequest && !originalRequest._retry && keycloak.authenticated) {
      originalRequest._retry = true;

      try {
        await keycloak.updateToken(0);
        if (keycloak.token) {
          originalRequest.headers.Authorization = `Bearer ${keycloak.token}`;
        }
        return api(originalRequest);
      } catch {
        await keycloak.logout({ redirectUri: `${window.location.origin}/login` });
      }
    }

    return Promise.reject(error);
  }
);

export default api;
