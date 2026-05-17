import axios from './axios';
 
export const sharedLinkApi = {
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
