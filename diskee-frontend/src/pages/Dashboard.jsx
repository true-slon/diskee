import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import FileUploader from '../components/FileUploader';
import FileList from '../components/FileList';
import StorageInfo from '../components/StorageInfo';
import Breadcrumbs from '../components/Breadcrumbs';
import SharedLinks from '../components/SharedLinks';
import TrashBin from '../components/TrashBin';

const Dashboard = () => {
  const { user, logout } = useAuth();
  const [currentFolderId, setCurrentFolderId] = useState(null);
  const [folderStack, setFolderStack] = useState([]);
  const [activeSection, setActiveSection] = useState('files');

  const handleFolderClick = (folderId) => {
    setFolderStack([...folderStack, currentFolderId]);
    setCurrentFolderId(folderId);
  };

  const handleNavigate = (folderId) => {
    setCurrentFolderId(folderId);
    if (folderId === null) setFolderStack([]);
  };

  const handleBack = () => {
    if (folderStack.length > 0) {
      const prevFolder = folderStack[folderStack.length - 1];
      setFolderStack(folderStack.slice(0, -1));
      setCurrentFolderId(prevFolder);
    }
  };

  const navItems = [
    { id: 'files',   label: 'Мои файлы' },
    { id: 'recent',  label: 'Недавние' },
    { id: 'starred', label: 'Избранное' },
    { id: 'trash',   label: 'Корзина' },
    { id: 'shared',  label: 'Общие ссылки' },
  ];

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
            {navItems.map((item) => (
              <button
                key={item.id}
                className={`nav-item ${activeSection === item.id ? 'active' : ''}`}
                onClick={() => {
                  setActiveSection(item.id);
                  if (item.id === 'files') handleNavigate(null);
                }}
              >
                {item.label}
              </button>
            ))}
          </nav>
        </aside>

        <main className="main-content">
          {activeSection === 'shared' && <SharedLinks />}
          {activeSection === 'trash'  && <TrashBin />}
          {activeSection === 'files'  && (
            <>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
                {currentFolderId && (
                  <button onClick={handleBack} style={{ padding: '6px 12px', cursor: 'pointer' }}>
                    Назад
                  </button>
                )}
                {/* <Breadcrumbs path={[]} onNavigate={handleNavigate} /> */}
              </div>
              <FileUploader currentFolderId={currentFolderId} />
              <FileList
                key={currentFolderId || 'root'}
                currentFolderId={currentFolderId}
                onFolderClick={handleFolderClick}
              />
            </>
          )}
          {(activeSection === 'recent' || activeSection === 'starred') && (
            <div className="empty-state">
              <div className="empty-state-icon">
                {activeSection === 'recent' ? '🕒' : '⭐'}
              </div>
              <p>{activeSection === 'recent' ? 'Недавние файлы' : 'Избранное'}</p>
              <span>Раздел пока в разработке</span>
            </div>
          )}
        </main>
      </div>
    </div>
  );
};

export default Dashboard;
