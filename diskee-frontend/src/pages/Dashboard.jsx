import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import FileUploader from '../components/FileUploader';
import FileList from '../components/FileList';
import StorageInfo from '../components/StorageInfo';
import Breadcrumbs from '../components/Breadcrumbs';

const Dashboard = () => {
  const { user, logout } = useAuth();
  const [currentFolderId, setCurrentFolderId] = useState(null);
  const [folderPath, setFolderPath] = useState([]);

  const handleFolderClick = (folderId) => {
    // Здесь нужно получить информацию о папке и обновить путь
    setCurrentFolderId(folderId);
    // В реальном приложении нужно обновить folderPath на основе данных с сервера
  };

  const handleNavigate = (folderId) => {
    setCurrentFolderId(folderId);
    // Обновить путь соответственно
  };

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <div className="logo">
          <h1>Diskee Cloud</h1>
        </div>
        <div className="user-info">
          <span className="user-name">{user?.displayName || user?.email}</span>
          <button onClick={logout} className="logout-btn">Выйти</button>
        </div>
      </header>

      <div className="dashboard-content">
        <aside className="sidebar">
          <div className="storage-section">
            <h3>Хранилище</h3>
            <StorageInfo />
          </div>
          <nav className="sidebar-nav">
            <button className="nav-item active">📁 Мои файлы</button>
            <button className="nav-item">🕒 Недавние</button>
            <button className="nav-item">⭐ Избранное</button>
            <button className="nav-item">🗑️ Корзина</button>
            <button className="nav-item">🔗 Общие ссылки</button>
          </nav>
        </aside>

        <main className="main-content">
          <Breadcrumbs path={folderPath} onNavigate={handleNavigate} />
          <FileUploader currentFolderId={currentFolderId} />
          <FileList 
            currentFolderId={currentFolderId} 
            onFolderClick={handleFolderClick}
          />
        </main>
      </div>
    </div>
  );
};

export default Dashboard;