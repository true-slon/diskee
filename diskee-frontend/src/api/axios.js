import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Для тестирования: перехватываем запросы и возвращаем моковые данные
const isDevelopment = true; // Временно включаем мок-режим

axiosInstance.interceptors.request.use(
  async (config) => {
    // Мок-режим: возвращаем тестовые данные вместо реальных запросов
    if (isDevelopment && config.url === '/auth/me') {
      return Promise.resolve({
        data: { email: 'test@example.com', displayName: 'Test User' },
        status: 200,
        statusText: 'OK',
        headers: {},
        config,
      });
    }
    
    if (isDevelopment && config.url === '/files/root') {
      return Promise.resolve({
        data: [
          {
            id: 1,
            fileName: 'test-document.pdf',
            fileExtension: 'pdf',
            fileSizeBytes: 1024000,
            createdAt: new Date().toISOString(),
            isFolder: false,
          },
          {
            id: 2,
            fileName: 'images',
            isFolder: true,
          },
          {
            id: 3,
            fileName: 'vacation-photo.jpg',
            fileExtension: 'jpg',
            fileSizeBytes: 2048000,
            createdAt: new Date().toISOString(),
            isFolder: false,
          },
        ],
        status: 200,
        statusText: 'OK',
        headers: {},
        config,
      });
    }
    
    if (isDevelopment && config.url === '/user/storage') {
      return Promise.resolve({
        data: {
          storageUsedBytes: 5120000000,
          storageLimitBytes: 10737418240,
        },
        status: 200,
        statusText: 'OK',
        headers: {},
        config,
      });
    }
    
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export default axiosInstance;