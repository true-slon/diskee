import React, { useState, useEffect } from 'react';
import { validateName } from '../utils/helpers';

export function RenameDialog({ item, open, onClose, onRename }) {
  const [name, setName] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    if (item) {
      const dotIndex = item.isFolder ? -1 : item.fileName?.lastIndexOf('.');
      const base = dotIndex > 0 ? item.fileName.substring(0, dotIndex) : (item.fileName || '');
      setName(base);
    }
  }, [item]);

  if (!open || !item) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    const ext = !item.isFolder && item.fileName?.includes('.') 
      ? item.fileName.substring(item.fileName.lastIndexOf('.')) 
      : '';
    const fullName = name + ext;
    const err = validateName(fullName);
    if (err) {
      setError(err);
      return;
    }
    onRename(item, fullName);
    setError('');
    onClose();
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h3>Переименовать</h3>
        <form onSubmit={handleSubmit}>
          <input
            type="text"
            value={name}
            onChange={(e) => { setName(e.target.value); setError(''); }}
            maxLength={255}
            autoFocus
          />
          {error && <div className="error">{error}</div>}
          <div className="modal-buttons">
            <button type="submit">OK</button>
            <button type="button" onClick={onClose}>Отмена</button>
          </div>
        </form>
      </div>
    </div>
  );
}