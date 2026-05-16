import React, { useState } from 'react';
import { useQuery } from 'react-query';
import FileItem from './FileItem';
import { fileApi } from '../api/fileApi';

const FileList = ({ currentFolderId, onFolderClick }) => {
  const [viewMode, setViewMode] = useState('grid'); // grid или list
  
  const { data, isLoading, error, refetch } = useQuery(
    ['files', currentFolderId],
    () => fileApi.getFiles(currentFolderId),
    { refetchOnWindowFocus: false }
  );

  if (isLoading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Загрузка файлов...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="error-container">
        <p>Ошибка загрузки файлов: {error.message}</p>
        <button onClick={refetch}>Повторить</button>
      </div>
    );
  }

  const files = data?.data || [];

  return (
    <div className="file-manager">
      <div className="view-controls">
        <button
          className={`view-btn ${viewMode === 'grid' ? 'active' : ''}`}
          onClick={() => setViewMode('grid')}
        >
          📱 Сетка
        </button>
        <button
          className={`view-btn ${viewMode === 'list' ? 'active' : ''}`}
          onClick={() => setViewMode('list')}
        >
          📋 Список
        </button>
      </div>

      <div className={`files-container ${viewMode}`}>
        {files.length === 0 ? (
          <div className="empty-state">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z" strokeWidth="2"/>
            </svg>
            <p>Папка пуста</p>
            <p className="empty-hint">Загрузите файлы или создайте новую папку</p>
          </div>
        ) : (
          files.map((file) => (
            <FileItem
              key={file.id}
              file={file}
              viewMode={viewMode}
              onFolderClick={onFolderClick}
              onFileDeleted={refetch}
            />
          ))
        )}
      </div>
    </div>
  );
};

export default FileList;