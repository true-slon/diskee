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
  };

  const overlayStyle = {
    position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
    background: 'rgba(0,0,0,0.5)', display: 'flex',
    alignItems: 'center', justifyContent: 'center', zIndex: 2000
  };

  const dialogStyle = {
    background: '#fff', borderRadius: 16, padding: 32, width: 400,
    boxShadow: '0 20px 60px rgba(0,0,0,0.3)', textAlign: 'center'
  };

  const iconStyle = { fontSize: 48, marginBottom: 12 };

  const titleStyle = { margin: '0 0 20px', fontSize: 20, fontWeight: 600, color: '#333' };

  const inputStyle = {
    width: '100%', padding: '14px 18px', borderRadius: 12,
    border: error ? '2px solid #e53935' : '2px solid #e0e0e0',
    fontSize: 16, outline: 'none', boxSizing: 'border-box',
    transition: 'border 0.2s'
  };

  const errorStyle = { color: '#e53935', fontSize: 13, marginTop: 8, textAlign: 'left' };

  const buttonsStyle = { display: 'flex', gap: 12, justifyContent: 'flex-end', marginTop: 24 };

  const cancelBtnStyle = {
    padding: '12px 24px', borderRadius: 10, border: '2px solid #e0e0e0',
    background: '#fff', cursor: 'pointer', fontSize: 15, fontWeight: 500,
    color: '#555'
  };

  const submitBtnStyle = {
    padding: '12px 28px', borderRadius: 10, border: 'none',
    background: '#4f46e5', color: '#fff', cursor: 'pointer',
    fontSize: 15, fontWeight: 600
  };

  return (
    <div style={overlayStyle} onClick={onClose}>
      <div style={dialogStyle} onClick={(e) => e.stopPropagation()}>
        <div style={iconStyle}>📁</div>
        <h3 style={titleStyle}>Новая папка</h3>
        <form onSubmit={handleSubmit}>
          <input
            type="text"
            value={name}
            onChange={(e) => { setName(e.target.value); setError(''); }}
            placeholder="Введите название"
            maxLength={255}
            autoFocus
            style={inputStyle}
          />
          {error && <div style={errorStyle}>{error}</div>}
          <div style={buttonsStyle}>
            <button type="button" onClick={onClose} style={cancelBtnStyle}>Отмена</button>
            <button type="submit" style={submitBtnStyle}>Создать</button>
          </div>
        </form>
      </div>
    </div>
  );
}