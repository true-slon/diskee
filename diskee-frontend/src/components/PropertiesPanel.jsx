import React from 'react';
import { formatBytes, formatDate } from '../utils/helpers';

export function PropertiesPanel({ item, onClose }) {
  if (!item) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h3>Свойства</h3>
        <p><strong>Имя:</strong> {item.fileName}</p>
        <p><strong>Тип:</strong> {item.isFolder ? 'Папка' : item.fileExtension || 'Файл'}</p>
        {!item.isFolder && (
          <p><strong>Размер:</strong> {formatBytes(item.fileSizeBytes)}</p>
        )}
        <p><strong>Создан:</strong> {formatDate(item.createdAt)}</p>
        <button onClick={onClose}>Закрыть</button>
      </div>
    </div>
  );
}