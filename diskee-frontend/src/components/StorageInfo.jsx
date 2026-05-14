import React from 'react';
import { useQuery } from 'react-query';
import { fileApi } from '../api/fileApi';
import bytes from 'bytes';

const StorageInfo = () => {
  const { data, isLoading, error } = useQuery('storage', () => fileApi.getStorageInfo(), {
    refetchInterval: 30000, // Обновлять каждые 30 секунд
  });

  if (isLoading) {
    return <div className="storage-info loading">Загрузка...</div>;
  }

  if (error) {
    return <div className="storage-info error">Ошибка загрузки информации</div>;
  }

  const storage = data?.data;
  const usedPercent = (storage.storageUsedBytes / storage.storageLimitBytes) * 100;

  return (
    <div className="storage-info">
      <div className="storage-stats">
        <span className="storage-used">{bytes(storage.storageUsedBytes)}</span>
        <span className="storage-separator">/</span>
        <span className="storage-limit">{bytes(storage.storageLimitBytes)}</span>
      </div>
      <div className="storage-bar">
        <div 
          className="storage-bar-fill" 
          style={{ width: `${Math.min(usedPercent, 100)}%` }}
        />
      </div>
      <div className="storage-percent">{usedPercent.toFixed(1)}% использовано</div>
    </div>
  );
};

export default StorageInfo;