import React, { useState } from 'react';
import { useMutation, useQueryClient } from 'react-query';
import { fileApi } from '../api/fileApi';
import bytes from 'bytes';
import { formatDistanceToNow } from 'date-fns';
import { ru } from 'date-fns/locale';
import { CreateSharedLinkDialog } from './CreateSharedLinkDialog';

const FileItem = ({ file, viewMode, onFolderClick, onFileDeleted, onDeleteToTrash, onDragStart, onDragOver, onDrop, onDragEnd, isDragTarget }) => {
  const [contextMenu, setContextMenu] = useState(null);
  const [isRenaming, setIsRenaming] = useState(false);
  const [newName, setNewName] = useState('');
  const [isDragOver, setIsDragOver] = useState(false);
  const [showShareDialog, setShowShareDialog] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(null);
  const queryClient = useQueryClient();

  if (!file) return null;

  const name = file.fileName || file.folderName || '';

  const deleteMutation = useMutation(
    () => {
      if (file.isFolder) return fileApi.deleteFolder(file.id);
      return fileApi.moveToTrash(file.id);
    },
    { onSuccess: () => { onFileDeleted(); queryClient.invalidateQueries('storage'); } }
  );

  const renameMutation = useMutation(
    (newNameValue) => {
      if (file.isFolder) return fileApi.renameFolder(file.id, newNameValue);
      return Promise.resolve();
    },
    { onSuccess: () => { onFileDeleted(); setIsRenaming(false); } }
  );

  const getFileIcon = () => {
    if (file.isFolder) return '📁';
    const ext = file.fileExtension?.toLowerCase();
    if (!ext) return '📎';
    if (['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(ext)) return '🖼️';
    if (['mp4', 'avi', 'mkv', 'mov'].includes(ext)) return '🎬';
    if (['mp3', 'wav', 'flac'].includes(ext)) return '🎵';
    if (['pdf'].includes(ext)) return '📄';
    return '📎';
  };

  const formatDate = (date) => {
    if (!date) return '';
    return formatDistanceToNow(new Date(date), { addSuffix: true, locale: ru });
  };

  const handleClick = () => {
    if (file.isFolder && !isRenaming) onFolderClick(file.id);
  };

  const handleContextMenu = (e) => {
    e.preventDefault();
    setContextMenu({ x: e.clientX, y: e.clientY });
  };

  const handleCloseMenu = () => setContextMenu(null);

  const handleDelete = () => {
    handleCloseMenu();
    if (onDeleteToTrash) {
      onDeleteToTrash();
    } else {
      setConfirmDelete(name);
    }
  };
  // const handleDownload = () => {
  //     handleCloseMenu();
  //     fileApi.downloadFile(file.id);
  // };
  const handleDownload = () => {
    handleCloseMenu();
    const ext = file.fileExtension ? '.' + file.fileExtension : '';
    const fullName = (file.fileName || 'download') + (file.isFolder ? '' : '');
    fileApi.downloadFile(file.id, file.fileName);
};
  const confirmDeleteAction = () => {
    setConfirmDelete(null);
    deleteMutation.mutate();
  };
  const handleDownloadFolder = async () => {
      handleCloseMenu();
      try {
          const response = await fileApi.downloadFolder(file.id);
          const url = window.URL.createObjectURL(response.data);
          const link = document.createElement('a');
          link.href = url;
          link.setAttribute('download', `${name}.zip`);
          document.body.appendChild(link);
          link.click();
          link.remove();
          window.URL.revokeObjectURL(url);
      } catch {
          alert('Ошибка при скачивании папки');
      }
  };
  const handleRenameStart = () => {
    handleCloseMenu();
    const dotIndex = file.isFolder ? -1 : name.lastIndexOf('.');
    setNewName(dotIndex > 0 ? name.substring(0, dotIndex) : name);
    setIsRenaming(true);
  };

  const handleRenameSubmit = (e) => {
    e.stopPropagation();
    const ext = !file.isFolder && name.includes('.') ? name.substring(name.lastIndexOf('.')) : '';
    renameMutation.mutate(newName.trim() + ext);
  };

  const handleShare = () => {
    handleCloseMenu();
    setShowShareDialog(true);
  };

  if (isRenaming) {
    return (
      <div className={`file-item-${viewMode}`}>
        <span className="file-icon">{getFileIcon()}</span>
        <input value={newName} onChange={(e) => setNewName(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleRenameSubmit(e)}
          onClick={(e) => e.stopPropagation()} autoFocus maxLength={255} />
        <button onClick={handleRenameSubmit}>✅</button>
        <button onClick={() => setIsRenaming(false)}>❌</button>
      </div>
    );
  }

  const handleDragStart = (e) => {
    if (onDragStart) onDragStart(e, file);
  };

  const handleDragOver = (e) => {
    if (isDragTarget) {
      e.preventDefault();
      setIsDragOver(true);
      if (onDragOver) onDragOver(e, file);
    }
  };

  const handleDragLeave = () => {
    setIsDragOver(false);
  };

  const handleDrop = (e) => {
    setIsDragOver(false);
    if (onDrop) onDrop(e, file);
  };

  return (
    <>
      <div
        className={`file-item-${viewMode} ${isDragOver ? 'drag-over' : ''}`}
        onClick={handleClick}
        onContextMenu={handleContextMenu}
        draggable
        onDragStart={handleDragStart}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        onDragEnd={onDragEnd}
      >
        <div className="file-icon">{getFileIcon()}</div>
        <div className="file-name" title={name}>{name}</div>
        {!file.isFolder && (
          <>
            <span className="file-size">{bytes(file.fileSizeBytes || 0)}</span>
            <span className="file-date">{formatDate(file.createdAt)}</span>
          </>
        )}
      </div>

      {contextMenu && (
      <>
          <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, zIndex: 998 }} onClick={handleCloseMenu} />
          <div style={{
              position: 'fixed', left: contextMenu.x, top: contextMenu.y, zIndex: 999,
              background: 'white', border: '1px solid #ccc', borderRadius: 4,
              padding: 8, boxShadow: '0 2px 8px rgba(0,0,0,0.2)'
          }}>
              {!file.isFolder && <div className="context-menu-item" onClick={handleDownload}>Скачать</div>}
              {file.isFolder && <div className="context-menu-item" onClick={handleDownloadFolder}>Скачать ZIP</div>}
              <div className="context-menu-item" onClick={handleShare}>Поделиться</div>
              <div className="context-menu-item" onClick={handleRenameStart}>Переименовать</div>
              <div className="context-menu-item" style={{ color: 'red' }} onClick={handleDelete}>Удалить</div>
          </div>
      </>
      )}

      {confirmDelete && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          background: 'rgba(0,0,0,0.5)', display: 'flex',
          alignItems: 'center', justifyContent: 'center', zIndex: 2000
        }}>
          <div style={{
            background: 'white', borderRadius: 12, padding: 24,
            maxWidth: 400, textAlign: 'center', boxShadow: '0 4px 20px rgba(0,0,0,0.3)'
          }}>
            <div style={{ fontSize: 40, marginBottom: 12 }}>🗑️</div>
            <h3 style={{ margin: '0 0 8px' }}>Удалить файл?</h3>
            <p style={{ color: '#666', margin: '0 0 20px' }}>
              «{confirmDelete}» будет перемещён в корзину.
            </p>
            <div style={{ display: 'flex', gap: 10, justifyContent: 'center' }}>
              <button onClick={() => setConfirmDelete(null)} style={{
                padding: '10px 24px', borderRadius: 8, border: '1px solid #ccc',
                background: 'white', cursor: 'pointer'
              }}>
                Отмена
              </button>
              <button onClick={confirmDeleteAction} style={{
                padding: '10px 24px', borderRadius: 8, border: 'none',
                background: '#e53935', color: 'white', cursor: 'pointer'
              }}>
                Удалить
              </button>
            </div>
          </div>
        </div>
      )}

      {showShareDialog && (
        <CreateSharedLinkDialog
          item={file}
          open={showShareDialog}
          onClose={() => setShowShareDialog(false)}
        />
      )}
    </>
  );
};

export default FileItem;