import axios from './axios';

export const fileApi = {
  // Загрузка файла
  uploadFile: (file, parentFolderId = null) => {
    const formData = new FormData();
    formData.append('file', file);
    if (parentFolderId) {
      formData.append('parentFolderId', parentFolderId);
    }
    
    return axios.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        const percentCompleted = Math.round(
          (progressEvent.loaded * 100) / progressEvent.total
        );
        // Можно использовать для отображения прогресса загрузки
        console.log(`Upload progress: ${percentCompleted}%`);
      },
    });
  },

  // Получение списка файлов в папке
  getFiles: (folderId = null) => {
    const url = folderId ? `/folders/${folderId}/files` : '/files/root';
    return axios.get(url);
  },

  // Скачивание файла
  downloadFile: async (fileName) => {
    const response = await axios.get(`/files/download/${fileName}`, {
      responseType: 'blob',
    });
    
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', fileName);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },

  // Удаление файла
  deleteFile: (fileName) => {
    return axios.delete(`/files/${fileName}`);
  },

  // Создание папки
  createFolder: (folderName, parentFolderId = null) => {
    return axios.post('/folders', { folderName, parentFolderId });
  },

  // Получение информации о хранилище
  getStorageInfo: () => {
    return axios.get('/user/storage');
  },

  // Поиск файлов
  searchFiles: (query) => {
    return axios.get('/files/search', { params: { q: query } });
  },
};