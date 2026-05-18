import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import FileItem from './FileItem';
import { fileApi } from '../api/fileApi';
import { CreateFolderDialog } from './CreateFolderDialog';

const FileList = ({ currentFolderId, onFolderClick }) => {
  const [viewMode, setViewMode] = useState('grid');
  const [dragItem, setDragItem] = useState(null);
  const [showCreateFolder, setShowCreateFolder] = useState(false);
  const queryClient = useQueryClient();

  const { data, isLoading, error, refetch } = useQuery(
    ['files', currentFolderId],
    () => fileApi.getContents(currentFolderId),
    { refetchOnWindowFocus: false }
  );

  const moveMutation = useMutation(
    ({ itemId, targetFolderId }) => {
      if (dragItem?.isFolder) {
        return fileApi.moveFolder(itemId, targetFolderId);
      }
      return fileApi.moveFile(itemId, targetFolderId);
    },
    { 
      onSuccess: () => { 
        refetch(); 
        queryClient.invalidateQueries('files'); 
      } 
    }
  );

  const createFolderMutation = useMutation(
    (folderName) => fileApi.createFolder(folderName, currentFolderId),
    { onSuccess: () => { refetch(); setShowCreateFolder(false); } }
  );

  if (isLoading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Загрузка...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="error-container">
        <p>Ошибка: {error.message}</p>
        <button onClick={refetch}>Повторить</button>
      </div>
    );
  }

  const response = data?.data || {};
  const folderIds = new Set((response.folders || []).map(f => f.id));
  const items = [
    ...(response.folders || []).map(f => ({ ...f, isFolder: true, fileName: f.folderName })),
    ...(response.files || [])
        .filter(f => !folderIds.has(f.id))
        .map(f => ({ ...f, isFolder: false }))
  ];

  const handleDragStart = (e, item) => { setDragItem(item); e.dataTransfer.effectAllowed = 'move'; };
  const handleDragOver = (e, targetItem) => { if (targetItem?.isFolder) { e.preventDefault(); e.dataTransfer.dropEffect = 'move'; } };
  const handleDrop = (e, targetItem) => { 
    e.preventDefault(); 
    if (dragItem && targetItem?.isFolder && dragItem.id !== targetItem.id) { 
        moveMutation.mutate({ itemId: dragItem.id, targetFolderId: targetItem.id });
        setTimeout(() => refetch(), 300);
    } 
    setDragItem(null); 
  };  
  const handleDragEnd = () => setDragItem(null);

  return (
    <div className="file-manager">
      <div className="view-controls">
        <button className="create-folder-btn" onClick={() => setShowCreateFolder(true)}> Создать папку </button>
        <div className="view-btns">
          <button className={`view-btn ${viewMode === 'grid' ? 'active' : ''}`} onClick={() => setViewMode('grid')}>Сетка</button>
          <button className={`view-btn ${viewMode === 'list' ? 'active' : ''}`} onClick={() => setViewMode('list')}>Список</button>
        </div>
      </div>

      <div className={`files-container ${viewMode}`}>
        {items.length === 0 ? (
          <div className="empty-state"><p>Папка пуста</p></div>
        ) : (
          items.map((file) => (
            <FileItem
              key={file.id}
              file={file}
              viewMode={viewMode}
              onFolderClick={onFolderClick}
              onFileDeleted={refetch}
              onDragStart={handleDragStart}
              onDragOver={handleDragOver}
              onDrop={handleDrop}
              onDragEnd={handleDragEnd}
              isDragTarget={file.isFolder}
            />
          ))
        )}
      </div>

      {showCreateFolder && (
        <CreateFolderDialog
          open={showCreateFolder}
          onClose={() => setShowCreateFolder(false)}
          onCreate={(name) => createFolderMutation.mutate(name)}
        />
      )}
    </div>
  );
};

export default FileList;