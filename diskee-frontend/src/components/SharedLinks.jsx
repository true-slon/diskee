import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { sharedLinkApi } from '../api/sharedLinkApi';
import { formatDistanceToNow } from 'date-fns';
import { ru } from 'date-fns/locale';

const SharedLinks = () => {
  const queryClient = useQueryClient();
  const [copiedId, setCopiedId] = useState(null);

  const { data, isLoading, error, refetch } = useQuery(
    'sharedLinks',
    () => sharedLinkApi.getMyLinks(),
    { refetchOnWindowFocus: false }
  );

  const deleteMutation = useMutation(
    (linkId) => sharedLinkApi.deleteLink(linkId),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('sharedLinks');
        refetch();
      },
    }
  );

  const handleCopy = (token) => {
    const url = `${window.location.origin}/share/${token}`;
    navigator.clipboard.writeText(url).then(() => {
      setCopiedId(token);
      setTimeout(() => setCopiedId(null), 2000);
    });
  };

  const handleDelete = (link) => {
    const name = link.name || 'ссылку';
    if (window.confirm(`Удалить ссылку на «${name}»?`)) {
      deleteMutation.mutate(link.id);
    }
  };

  const formatDate = (date) => {
    if (!date) return null;
    return formatDistanceToNow(new Date(date), { addSuffix: true, locale: ru });
  };

  const isExpired = (expiresAt) => {
    if (!expiresAt) return false;
    return new Date(expiresAt) < new Date();
  };

  const getPermissionLabel = (permission) => {
    if (permission === 'edit') return 'Редактирование';
    return 'Просмотр';
  };

  const getTypeIcon = (type) => {
    return type === 'folder' ? '📁' : '📄';
  };

  if (isLoading) {
    return (
      <div className="loading-container">
        <div className="spinner" />
        <p>Загрузка...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="error-container">
        <p>Ошибка: {error.message}</p>
        <button onClick={refetch}>Повторить</button>
      </div>
    );
  }

  const links = data?.data || [];

  return (
    <div className="shared-links-page">
      <div className="shared-links-header">
        <h2>Общие ссылки</h2>
        <p className="shared-links-subtitle">
          Ссылки, которые вы создали для файлов и папок
        </p>
      </div>

      {links.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">🔗</div>
          <p>У вас пока нет общих ссылок</p>
          <span>Нажмите правой кнопкой на файл или папку, чтобы поделиться</span>
        </div>
      ) : (
        <div className="shared-links-list">
          {links.map((link) => {
            const expired = isExpired(link.expiresAt);
            return (
              <div
                key={link.id}
                className={`shared-link-item ${expired ? 'expired' : ''}`}
              >
                <div className="shared-link-info">
                  <div className="shared-link-name">
                    {/* <span className="shared-link-type-icon">
                      {getTypeIcon(link.type)}
                    </span> */}
                    <span className="shared-link-filename">
                      {link.name || 'Без названия'}
                    </span>
                    {expired && (
                      <span className="shared-link-badge expired-badge">
                        Истекла
                      </span>
                    )}
                    <span className="shared-link-badge permission-badge">
                      {getPermissionLabel(link.permission)}
                    </span>
                  </div>

                  <div className="shared-link-meta">
                    <span className="shared-link-url">
                      {`${window.location.origin}/share/${link.token}`}
                    </span>
                  </div>

                  <div className="shared-link-stats">
                    {link.expiresAt && (
                      <span className={`shared-link-expiry ${expired ? 'expired-text' : ''}`}>
                        {expired
                          ? `Истекла ${formatDate(link.expiresAt)}`
                          : `Истекает ${formatDate(link.expiresAt)}`}
                      </span>
                    )}
                    {!link.expiresAt && (
                      <span className="shared-link-expiry">Бессрочная</span>
                    )}
                    <span className="shared-link-downloads">
                      ↓ {link.downloadCount} скачиваний
                    </span>
                    {link.createdAt && (
                      <span className="shared-link-created">
                        Создана {formatDate(link.createdAt)}
                      </span>
                    )}
                  </div>
                </div>

                <div className="shared-link-actions">
                  <button
                    className={`shared-link-copy-btn ${copiedId === link.token ? 'copied' : ''}`}
                    onClick={() => handleCopy(link.token)}
                    title="Скопировать ссылку"
                  >
                    {copiedId === link.token ? 'Скопировано' : 'Копировать'}
                  </button>
                  <button
                    className="shared-link-delete-btn"
                    onClick={() => handleDelete(link)}
                    title="Удалить ссылку"
                    disabled={deleteMutation.isLoading}
                  >
                    Удалить
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export { SharedLinks };
export default SharedLinks;
