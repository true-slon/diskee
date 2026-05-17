import React, { useState } from 'react';
import { useMutation, useQueryClient } from 'react-query';
import { fileApi } from '../api/fileApi';
import bytes from 'bytes';
import { formatDistanceToNow } from 'date-fns';
import { ru } from 'date-fns/locale';

const FileItem = ({ file, viewMode, onFolderClick, onFileDeleted }) => {
  const [showMenu, setShowMenu] = useState(false);
  const queryClient = useQueryClient();

  const deleteMutation = useMutation(
    () => fileApi.deleteFile(file.fileName),
    {
      onSuccess: () => {
        onFileDeleted();
        queryClient.invalidateQueries('storage');
      },
    }
  );

  const downloadMutation = useMutation(() => fileApi.downloadFile(file.fileName));

  const getFileIcon = () => {
    const ext = file.fileExtension?.toLowerCase();
    if (file.isFolder) return '📁';
    if (['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(ext)) return '🖼️';
    if (['mp4', 'avi', 'mkv', 'mov'].includes(ext)) return '🎬';
    if (['mp3', 'wav', 'flac'].includes(ext)) return '🎵';
    if (['pdf'].includes(ext)) return '📄';
    if (['doc', 'docx'].includes(ext)) return '📝';
    if (['xls', 'xlsx'].includes(ext)) return '📊';
    if (['zip', 'rar', '7z'].includes(ext)) return '🗜️';
    return '📎';
  };

  const formatDate = (date) => {
    return formatDistanceToNow(new Date(date), { addSuffix: true, locale: ru });
  };

  const handleClick = () => {
    if (file.isFolder) {
      onFolderClick(file.id);
    }
  };

  const handleDownload = (e) => {
    e.stopPropagation();
    downloadMutation.mutate();
  };

  const handleDelete = (e) => {
    e.stopPropagation();
    if (window.confirm(`Удалить файл "${file.fileName}"?`)) {
      deleteMutation.mutate();
    }
  };

  if (viewMode === 'grid') {
    return (
      <div className="file-item-grid" onClick={handleClick}>
        <div className="file-icon">{getFileIcon()}</div>
        <div className="file-name" title={file.fileName}>
          {file.fileName}
        </div>
        {!file.isFolder && (
          <div className="file-meta">
            <span className="file-size">{bytes(file.fileSizeBytes)}</span>
            <span className="file-date">{formatDate(file.createdAt)}</span>
          </div>
        )}
        {!file.isFolder && (
          <div className="file-actions">
            <button onClick={handleDownload} className="action-btn" title="Скачать">
              ⬇️
            </button>
            <button onClick={handleDelete} className="action-btn" title="Удалить">
              🗑️
            </button>
          </div>
        )}
      </div>
    );
  }

  return (
    <div className="file-item-list" onClick={handleClick}>
      <div className="file-info">
        <span className="file-icon">{getFileIcon()}</span>
        <span className="file-name">{file.fileName}</span>
      </div>
      {!file.isFolder ? (
        <>
          <span className="file-size">{bytes(file.fileSizeBytes)}</span>
          <span className="file-date">{formatDate(file.createdAt)}</span>
          <div className="file-actions">
            <button onClick={handleDownload} className="action-btn">Скачать</button>
            <button onClick={handleDelete} className="action-btn delete">Удалить</button>
          </div>
        </>
      ) : (
        <div className="file-actions">
          <span className="folder-hint">Открыть →</span>
        </div>
      )}
    </div>
  );
};

export default FileItem;