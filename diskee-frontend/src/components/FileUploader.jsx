import React, { useCallback } from 'react';
import { useDropzone } from 'react-dropzone';
import { useMutation, useQueryClient } from 'react-query';
import { fileApi } from '../api/fileApi';

const FileUploader = ({ currentFolderId }) => {
  const queryClient = useQueryClient();

  const uploadMutation = useMutation(
    ({ file, folderId }) => fileApi.uploadFile(file, folderId),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['files', currentFolderId]);
        queryClient.invalidateQueries('storage');  // ← добавить эту строку

      },
    }
  );

  const onDrop = useCallback(
    (acceptedFiles) => {
      acceptedFiles.forEach((file) => {
        uploadMutation.mutate({ file, folderId: currentFolderId });
      });
    },
    [currentFolderId]
  );

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    multiple: true,
  });

  return (
    <div
      {...getRootProps()}
      className={`upload-area ${isDragActive ? 'drag-active' : ''}`}
    >
      <input {...getInputProps()} />
      <div className="upload-content">
        {isDragActive ? (
          <p>Отпустите файлы для загрузки...</p>
        ) : (
          <>
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path d="M12 3v12m0 0l-3-3m3 3l3-3M5 17h14" strokeWidth="2" strokeLinecap="round"/>
            </svg>
            <p>Перетащите файлы сюда или нажмите для выбора</p>
            {uploadMutation.isLoading && <p className="uploading">Загрузка...</p>}
          </>
        )}
      </div>
    </div>
  );
};

export default FileUploader;