import React, { useState } from 'react';
import ReactDOM from 'react-dom';
import { useMutation } from 'react-query';
import { sharedLinkApi } from '../api/sharedLinkApi';

export function CreateSharedLinkDialog({ item, open, onClose }) {
  const [permission, setPermission] = useState('view');
  const [expiryType, setExpiryType] = useState('none');
  const [expiryDate, setExpiryDate] = useState('');
  const [createdLink, setCreatedLink] = useState(null);
  const [copied, setCopied] = useState(false);

  const createMutation = useMutation(
    () => {
      const expiresAt =
        expiryType === 'date' && expiryDate
          ? new Date(expiryDate).toISOString()
          : expiryType === '7days'
          ? new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString()
          : expiryType === '30days'
          ? new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString()
          : null;

      const request = { permission, expiresAt };

      if (item.isFolder) {
        return sharedLinkApi.createForFolder(item.id, request);
      }
      return sharedLinkApi.createForFile(item.id, request);
    },
    {
      onSuccess: (res) => {
        setCreatedLink(res.data);
      },
    }
  );

  if (!open || !item) return null;

  const name = item.fileName || item.folderName || '';
  const linkUrl = createdLink
    ? `${window.location.origin}/share/${createdLink.token}`
    : null;

  const handleCopy = () => {
    if (!linkUrl) return;
    navigator.clipboard.writeText(linkUrl).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    });
  };

  const handleClose = () => {
    setCreatedLink(null);
    setCopied(false);
    setPermission('view');
    setExpiryType('none');
    setExpiryDate('');
    onClose();
  };

  const getTodayString = () => {
    const d = new Date();
    d.setDate(d.getDate() + 1);
    return d.toISOString().split('T')[0];
  };

  if (!open || !item) return null;

  const modal = (
    <div className="modal-overlay" onClick={handleClose} style={{ zIndex: 9999 }}>
      <div
        className="modal shared-link-modal"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="modal-title-row">
          <h3>🔗 Поделиться</h3>
          <button className="modal-close-btn" onClick={handleClose}>✕</button>
        </div>

        <div className="shared-link-item-name">
          <span>{item.isFolder ? '📁' : '📄'}</span>
          <span title={name}>{name}</span>
        </div>

        {!createdLink ? (
          <>
            <div className="shared-link-form-section">
              <label className="shared-link-form-label">Доступ</label>
              <div className="shared-link-radio-group">
                <label className={`radio-option ${permission === 'view' ? 'active' : ''}`}>
                  <input
                    type="radio"
                    name="permission"
                    value="view"
                    checked={permission === 'view'}
                    onChange={() => setPermission('view')}
                  />
                  <span className="radio-label">👁️ Просмотр</span>
                  <span className="radio-desc">Только чтение и скачивание</span>
                </label>
                <label className={`radio-option ${permission === 'edit' ? 'active' : ''}`}>
                  <input
                    type="radio"
                    name="permission"
                    value="edit"
                    checked={permission === 'edit'}
                    onChange={() => setPermission('edit')}
                  />
                  <span className="radio-label">✏️ Редактирование</span>
                  <span className="radio-desc">Изменение файлов и папок</span>
                </label>
              </div>
            </div>

            <div className="shared-link-form-section">
              <label className="shared-link-form-label">Срок действия</label>
              <div className="shared-link-expiry-options">
                {[
                  { value: 'none', label: 'Бессрочно' },
                  { value: '7days', label: '7 дней' },
                  { value: '30days', label: '30 дней' },
                  { value: 'date', label: 'Своя дата' },
                ].map((opt) => (
                  <button
                    key={opt.value}
                    className={`expiry-btn ${expiryType === opt.value ? 'active' : ''}`}
                    onClick={() => setExpiryType(opt.value)}
                  >
                    {opt.label}
                  </button>
                ))}
              </div>
              {expiryType === 'date' && (
                <input
                  type="date"
                  value={expiryDate}
                  min={getTodayString()}
                  onChange={(e) => setExpiryDate(e.target.value)}
                  className="shared-link-date-input"
                />
              )}
            </div>

            {createMutation.isError && (
              <div className="error">Ошибка создания ссылки. Попробуйте снова.</div>
            )}

            <div className="modal-buttons">
              <button
                onClick={() => createMutation.mutate()}
                disabled={createMutation.isLoading || (expiryType === 'date' && !expiryDate)}
                className="btn-primary"
              >
                {createMutation.isLoading ? 'Создание...' : 'Создать ссылку'}
              </button>
              <button type="button" onClick={handleClose}>Отмена</button>
            </div>
          </>
        ) : (
          <div className="shared-link-created-block">
            <div className="shared-link-success-icon">✅</div>
            <p className="shared-link-success-msg">Ссылка создана!</p>

            <div className="shared-link-url-row">
              <input
                readOnly
                value={linkUrl}
                className="shared-link-url-input"
                onFocus={(e) => e.target.select()}
              />
              <button
                className={`shared-link-copy-btn ${copied ? 'copied' : ''}`}
                onClick={handleCopy}
              >
                {copied ? '✅' : '📋'}
              </button>
            </div>

            <div className="shared-link-created-meta">
              <span>Доступ: {permission === 'edit' ? 'Редактирование' : 'Просмотр'}</span>
              {createdLink.expiresAt && (
                <span>
                  Истекает:{' '}
                  {new Date(createdLink.expiresAt).toLocaleDateString('ru-RU')}
                </span>
              )}
            </div>

            <div className="modal-buttons">
              <button className="btn-primary" onClick={handleClose}>
                Готово
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );

  return ReactDOM.createPortal(modal, document.body);
}
