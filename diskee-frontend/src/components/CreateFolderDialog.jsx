import React, { useState } from 'react';
import { validateName } from '../utils/helpers';

export function CreateFolderDialog({ open, onClose, onCreate }) {
  const [name, setName] = useState('');
  const [error, setError] = useState('');

  if (!open) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    const err = validateName(name);
    if (err) {
      setError(err);
      return;
    }
    onCreate(name.trim());
    setName('');
    setError('');
    onClose();
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h3>Новая папка</h3>
        <form onSubmit={handleSubmit}>
          <input
            type="text"
            value={name}
            onChange={(e) => { setName(e.target.value); setError(''); }}
            placeholder="Название папки"
            maxLength={255}
            autoFocus
          />
          {error && <div className="error">{error}</div>}
          <div className="modal-buttons">
            <button type="submit">Создать</button>
            <button type="button" onClick={onClose}>Отмена</button>
          </div>
        </form>
      </div>
    </div>
  );
}