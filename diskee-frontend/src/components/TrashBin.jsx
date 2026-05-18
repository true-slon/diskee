import React, { useState, useEffect, useCallback } from 'react';
import { fileApi } from '../api/fileApi';
import { formatDistanceToNow, differenceInDays } from 'date-fns';
import { ru } from 'date-fns/locale';

const formatBytes = (bytes) => {
  if (!bytes) return '';
  if (bytes < 1024) return `${bytes} Б`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} КБ`;
  if (bytes < 1024 * 1024 * 1024) return `${(bytes / 1024 / 1024).toFixed(1)} МБ`;
  return `${(bytes / 1024 / 1024 / 1024).toFixed(2)} ГБ`;
};

const getItemIcon = (item) => {
  if (item.itemType === 'folder') return '📁';
  const name = item.name || '';
  const ext = name.includes('.') ? name.split('.').pop().toLowerCase() : '';
  if (['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(ext)) return '🖼️';
  if (['mp4', 'avi', 'mkv', 'mov'].includes(ext)) return '🎬';
  if (['mp3', 'wav', 'flac'].includes(ext)) return '🎵';
  if (ext === 'pdf') return '📄';
  if (['zip', 'rar', '7z', 'tar'].includes(ext)) return '🗜️';
  if (['doc', 'docx'].includes(ext)) return '📝';
  if (['xls', 'xlsx'].includes(ext)) return '📊';
  return '📎';
};

const AutoDeleteBadge = ({ autoDeleteAt }) => {
  if (!autoDeleteAt) return null;
  const days = differenceInDays(new Date(autoDeleteAt), new Date());
  let cls = 'auto-delete-badge';
  let text = '';
  if (days <= 0) {
    text = 'Удаляется сегодня';
    cls += ' danger';
  } else if (days <= 3) {
    text = `Удалится через ${days} дн.`;
    cls += ' danger';
  } else if (days <= 7) {
    text = `Удалится через ${days} дн.`;
    cls += ' warning';
  } else {
    text = `Удалится через ${days} дн.`;
    cls += ' normal';
  }
  return <span className={cls}>{text}</span>;
};

const TrashBin = () => {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actionLoading, setActionLoading] = useState(null);

  const fetchTrash = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fileApi.getTrash();
      setItems(res.data || []);
    } catch (err) {
      setError('Не удалось загрузить корзину');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchTrash();
  }, [fetchTrash]);

  const handleRestore = async (item) => {
    setActionLoading(`restore-${item.id}`);
    try {
      await fileApi.restoreFromTrash(item.id);
      await fetchTrash();
    } catch {
      alert('Ошибка при восстановлении');
    } finally {
      setActionLoading(null);
    }
  };

  const handleDeleteForever = async (item) => {
    const name = item.name || 'элемент';
    if (!window.confirm(`Удалить «${name}» навсегда? Это действие нельзя отменить.`)) return;
    setActionLoading(`delete-${item.id}`);
    try {
      await fileApi.permanentDelete(item.id);
      setItems((prev) => prev.filter((i) => i.id !== item.id));
    } catch {
      alert('Ошибка при удалении');
    } finally {
      setActionLoading(null);
    }
  };

  const handleClearAll = async () => {
    if (!window.confirm('Очистить всю корзину? Все файлы будут удалены навсегда.')) return;
    setActionLoading('clear');
    try {
      await fileApi.clearTrash();
      setItems([]);
    } catch {
      alert('Ошибка при очистке корзины');
    } finally {
      setActionLoading(null);
    }
  };

  return (
    <div className="trash-page">
      <div className="trash-header">
        <div className="trash-header-left">
          <h2>Корзина</h2>
          {items.length > 0 && (
            <span className="trash-count">{items.length} элем.</span>
          )}
        </div>
        {items.length > 0 && (
          <button
            className="trash-clear-btn"
            onClick={handleClearAll}
            disabled={actionLoading === 'clear'}
          >
            {actionLoading === 'clear' ? 'Очистка...' : 'Очистить всё'}
          </button>
        )}
      </div>

      {items.length > 0 && (
        <p className="trash-hint">
          Файлы в корзине автоматически удаляются через 30 дней после помещения
        </p>
      )}

      {loading ? (
        <div className="loading-container">
          <div className="spinner" />
          <p>Загрузка...</p>
        </div>
      ) : error ? (
        <div className="error-container">
          <p>{error}</p>
          <button onClick={fetchTrash}>Повторить</button>
        </div>
      ) : items.length === 0 ? (
        <div className="trash-empty">
          <div className="trash-empty-icon">🗑️</div>
          <p>Корзина пуста</p>
          <span>Удалённые файлы и папки появятся здесь</span>
        </div>
      ) : (
        <div className="trash-list">
          {items.map((item) => {
            const isRestoring = actionLoading === `restore-${item.id}`;
            const isDeleting = actionLoading === `delete-${item.id}`;
            const busy = isRestoring || isDeleting;

            return (
              <div key={item.id} className={`trash-item ${busy ? 'busy' : ''}`}>
                <div className="trash-item-icon">{getItemIcon(item)}</div>

                <div className="trash-item-info">
                  <div className="trash-item-name" title={item.name}>
                    {item.name || 'Без названия'}
                  </div>
                  <div className="trash-item-meta">
                    {item.size && (
                      <span className="trash-meta-size">{formatBytes(item.size)}</span>
                    )}
                    {item.itemType === 'folder' && (
                      <span className="trash-meta-type">Папка</span>
                    )}
                    {item.originalPath && (
                      <span className="trash-meta-path" title={item.originalPath}>
                        {item.originalPath}
                      </span>
                    )}
                    {item.deletedAt && (
                      <span className="trash-meta-date">
                        Удалён{' '}
                        {formatDistanceToNow(new Date(item.deletedAt), {
                          addSuffix: true,
                          locale: ru,
                        })}
                      </span>
                    )}
                    <AutoDeleteBadge autoDeleteAt={item.autoDeleteAt} />
                  </div>
                </div>

                <div className="trash-item-actions">
                  <button
                    className="trash-restore-btn"
                    onClick={() => handleRestore(item)}
                    disabled={busy}
                    title="Восстановить"
                  >
                    {isRestoring ? '...' : 'Восстановить'}
                  </button>
                  <button
                    className="trash-delete-btn"
                    onClick={() => handleDeleteForever(item)}
                    disabled={busy}
                    title="Удалить навсегда"
                  >
                    {isDeleting ? '...' : 'Удалить'}
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

export { TrashBin };
export default TrashBin;
