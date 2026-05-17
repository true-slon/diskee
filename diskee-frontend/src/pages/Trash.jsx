import React, { useState, useEffect } from 'react';
import { fileApi } from '../api/fileApi';

const Trash = () => {
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchTrash();
    }, []);

    const fetchTrash = async () => {
        try {
            const res = await fileApi.getTrash();
            setItems(res.data);
        } catch (err) {
            console.error('Ошибка загрузки корзины:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleRestore = async (id) => {
        await fileApi.restoreFromTrash(id);
        fetchTrash();
    };

    const handleDeleteForever = async (id) => {
        if (window.confirm('Удалить навсегда?')) {
            await fileApi.permanentDelete(id);
            fetchTrash();
        }
    };

    const handleClearAll = async () => {
        if (window.confirm('Очистить всю корзину? Это действие нельзя отменить.')) {
            await fileApi.clearTrash();
            setItems([]);
        }
    };

    if (loading) return <div>Загрузка...</div>;

    return (
        <div className="trash-container">
            <h2>🗑️ Корзина</h2>

            {items.length === 0 ? (
                <p>Корзина пуста</p>
            ) : (
                <>
                    <button onClick={handleClearAll} className="btn-danger">
                        Очистить всё
                    </button>

                    {items.map(item => (
                        <div key={item.id} className="trash-item">
                            <div>
                                <strong>{item.file?.fileName || item.folder?.folderName}</strong>
                                <p>Исходный путь: {item.originalPath}</p>
                                <p>Удалён: {new Date(item.deletedAt).toLocaleString()}</p>
                                <p>Автоудаление: {new Date(item.autoDeleteAt).toLocaleString()}</p>
                            </div>
                            <div>
                                <button onClick={() => handleRestore(item.id)}>
                                    Восстановить
                                </button>
                                <button onClick={() => handleDeleteForever(item.id)} className="btn-danger">
                                    Удалить навсегда
                                </button>
                            </div>
                        </div>
                    ))}
                </>
            )}
        </div>
    );
};

export default Trash;