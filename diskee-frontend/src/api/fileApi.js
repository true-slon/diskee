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

  downloadFile: (fileId) => {
    return axios.get(`/api/files/${fileId}`, { responseType: 'blob' })
      .then(response => {
        const disposition = response.headers['content-disposition'];
        let fileName = 'download';
        if (disposition) {
          const match = disposition.match(/filename="(.+)"/);
          if (match) fileName = match[1];
        }
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

  getStorageInfo: () => axios.get('/api/user/storage'),
  searchFiles: (query) => axios.get('/api/files/search', { params: { q: query } }),

  // ============ КОРЗИНА ============
  getTrash: () => axios.get('/api/trash'),
  restoreFromTrash: (id) => axios.post(`/api/trash/restore/${id}`),
  permanentDelete: (id) => axios.delete(`/api/trash/permanent/${id}`),
  clearTrash: () => axios.delete('/api/trash/clear'),

  // ============ ОБЩИЕ ССЫЛКИ ============
  createForFile: (fileId, request) =>
    axios.post(`/app/v4/share/file/${fileId}`, request),
 
  createForFolder: (folderId, request) =>
    axios.post(`/app/v4/share/folder/${folderId}`, request),
 
  resolve: (token) =>
    axios.get(`/app/v4/share/${token}`),
 
  deleteLink: (linkId) =>
    axios.delete(`/app/v4/share/${linkId}`),
 
  getMyLinks: () =>
    axios.get('/app/v4/share/my'),

};
