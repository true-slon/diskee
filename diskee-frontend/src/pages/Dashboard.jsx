import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import FileUploader from '../components/FileUploader';
import FileList from '../components/FileList';
import StorageInfo from '../components/StorageInfo';
import Breadcrumbs from '../components/Breadcrumbs';

const Dashboard = () => {
  const { user, logout } = useAuth();
  const [currentFolderId, setCurrentFolderId] = useState(null);
  const [folderStack, setFolderStack] = useState([]);

  const handleFolderClick = (folderId) => {
    setFolderStack([...folderStack, currentFolderId]);
    setCurrentFolderId(folderId);
  };

  const handleNavigate = (folderId) => {
    setCurrentFolderId(folderId);
    if (folderId === null) {
      setFolderStack([]);
    }
  };

  const handleBack = () => {
    if (folderStack.length > 0) {
      const prevFolder = folderStack[folderStack.length - 1];
      setFolderStack(folderStack.slice(0, -1));
      setCurrentFolderId(prevFolder);
    }
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
            <button className="nav-item active" onClick={() => handleNavigate(null)}>📁 Мои файлы</button> 
            <button className="nav-item">🕒 Недавние</button>
            <button className="nav-item">⭐ Избранное</button>
            <Link to="/trash" className="nav-item">🗑️ Корзина</Link>
            <button className="nav-item">🔗 Общие ссылки</button>
          </nav>
        </aside>

        <main className="main-content">
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
            {currentFolderId && (
              <button onClick={handleBack} style={{ padding: '6px 12px', cursor: 'pointer' }}>
                ⬅️ Назад
              </button>
            )}
            <Breadcrumbs path={[]} onNavigate={handleNavigate} />
          </div>
          <FileUploader currentFolderId={currentFolderId} />
          <FileList 
            key={currentFolderId || 'root'}
            currentFolderId={currentFolderId} 
            onFolderClick={handleFolderClick}
          />
        </main>
      </div>
    </div>
  );
};

export default Dashboard;