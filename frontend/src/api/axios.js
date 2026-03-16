import axios from 'axios';

const baseURL = import.meta.env.VITE_SERVER_URL ?? '/';

const api = axios.create({
  baseURL,
  timeout: Number(import.meta.env.VITE_REQUEST_TIMEOUT_MS ?? 15000),
  headers: {
    'Content-Type': 'application/json'
  }
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const refreshToken = localStorage.getItem('refreshToken');

    if (
      originalRequest &&
      error.response?.status === 401 &&
      !originalRequest?._retry &&
      refreshToken &&
      !originalRequest?.url?.includes('/api/v1/auth/refresh')
    ) {
      originalRequest._retry = true;

      try {
        const refreshResponse = await axios.post(
          `${baseURL}/api/v1/auth/refresh`,
          { refreshToken }
        );

        const newAccessToken = refreshResponse.data?.accessToken;
        const newRefreshToken = refreshResponse.data?.refreshToken;

        if (!newAccessToken || !newRefreshToken) {
          throw new Error('Missing tokens after refresh');
        }

        localStorage.setItem('accessToken', newAccessToken);
        localStorage.setItem('refreshToken', newRefreshToken);

        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default api;
