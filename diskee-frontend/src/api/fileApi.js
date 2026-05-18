import axios from './axios';

export const fileApi = {
  getContents: (parentId = null) => {
    const params = parentId ? { parentId } : {};
    return axios.get('/api/folders', { params });
  },

  uploadFile: (file, parentFolderId = null) => {
    const formData = new FormData();
    formData.append('file', file);
    if (parentFolderId) {
      formData.append('parentFolderId', parentFolderId);
    }
    return axios.post('/api/files', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },


  searchFiles: (query, folderId, category) => {
    const params = {};
    if (query) params.q = query;
    if (folderId) params.folderId = folderId;
    if (category) params.category = category;
    return axios.get('/api/files/search', { params });
  },


  downloadFile: (fileId, fileName) => {
    return axios.get(`/api/files/${fileId}`, { responseType: 'blob' })
      .then(response => {
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', fileName);
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.URL.revokeObjectURL(url);
      });
  },

  moveToTrash: (fileId) => axios.post(`/api/files/${fileId}/trash`),
  deleteFile: (fileId) => axios.delete(`/api/files/${fileId}`),
  moveFile: (fileId, targetFolderId) =>
    axios.patch(`/api/files/${fileId}/move`, { parentFolderId: targetFolderId }),

  createFolder: (folderName, parentFolderId = null) =>
    axios.post('/api/folders', { folderName, parentFolderId }),
  renameFolder: (folderId, folderName) =>
    axios.patch(`/api/folders/${folderId}`, { folderName }),
  moveFolder: (folderId, targetFolderId) =>
    axios.patch(`/api/folders/${folderId}/move`, { parentFolderId: targetFolderId }),
  copyFolder: (folderId, targetFolderId) =>
    axios.post(`/api/folders/${folderId}/copy`, { parentFolderId: targetFolderId }),
  deleteFolder: (folderId) => axios.delete(`/api/folders/${folderId}`),
  downloadFolder: (folderId) => {
      return axios.get(`/api/folders/${folderId}/download`, { responseType: 'blob' });
  },
  getStorageInfo: () => axios.get('/api/user/storage'),

  // ============ КОРЗИНА ============
  getTrash: () => axios.get('/api/trash'),
  restoreFromTrash: (id) => axios.post(`/api/trash/restore/${id}`),
  permanentDelete: (id) => axios.delete(`/api/trash/permanent/${id}`),
  clearTrash: () => axios.delete('/api/trash/clear'),

};
