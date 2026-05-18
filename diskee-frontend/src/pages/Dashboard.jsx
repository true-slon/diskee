import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import FileUploader from '../components/FileUploader';
import FileList from '../components/FileList';
import StorageInfo from '../components/StorageInfo';
import Breadcrumbs from '../components/Breadcrumbs';
import SharedLinks from '../components/SharedLinks';
import TrashBin from '../components/TrashBin';
import { fileApi } from '../api/fileApi';

const Dashboard = () => {
  const { user, logout } = useAuth();
  const [currentFolderId, setCurrentFolderId] = useState(null);
  const [folderStack, setFolderStack] = useState([]);
  const [activeSection, setActiveSection] = useState('files');

  // Поиск
  const [searchQuery, setSearchQuery] = useState('');
  const [searchCategory, setSearchCategory] = useState('');
  const [searchInCurrentFolder, setSearchInCurrentFolder] = useState(false);
  const [searchResults, setSearchResults] = useState(null);

  const handleFolderClick = (folderId) => {
    setFolderStack([...folderStack, currentFolderId]);
    setCurrentFolderId(folderId);
    setSearchResults(null);
  };

  const handleNavigate = (folderId) => {
    setCurrentFolderId(folderId);
    if (folderId === null) setFolderStack([]);
    setSearchResults(null);
  };

  const handleBack = () => {
    if (folderStack.length > 0) {
      const prevFolder = folderStack[folderStack.length - 1];
      setFolderStack(folderStack.slice(0, -1));
      setCurrentFolderId(prevFolder);
    }
  };

  const handleSearch = async () => {
    if (!searchQuery.trim() && !searchCategory) {
      setSearchResults(null);
      return;
    }
    const folderId = searchInCurrentFolder ? currentFolderId : null;
    const res = await fileApi.searchFiles(searchQuery, folderId, searchCategory);
    setSearchResults(res.data);
  };

  useEffect(() => {
    if (searchQuery.length >= 2 || searchCategory) {
      const timer = setTimeout(handleSearch, 300);
      return () => clearTimeout(timer);
    } else {
      setSearchResults(null);
    }
  }, [searchQuery, searchCategory, searchInCurrentFolder]);

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
              <div style={{ marginBottom: 16, padding: 12, background: '#f5f5f5', borderRadius: 12 }}>
                <div style={{ display: 'flex', gap: 8, marginBottom: 8 }}>
                  <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder="Поиск файлов..."
                    style={{
                      flex: 1, padding: '10px 14px', borderRadius: 8,
                      border: '1px solid #ddd', fontSize: 14, outline: 'none'
                    }}
                  />
                  <select
                     value={searchCategory}
                     onChange={(e) => {
                        setSearchCategory(e.target.value);
                        const folderId = searchInCurrentFolder ? currentFolderId : null;
                        fileApi.searchFiles(searchQuery, folderId, e.target.value).then(res => {
                            setSearchResults(res.data);
                        });
                    }}
                    style={{
                      padding: '10px 12px', borderRadius: 8,
                      border: '1px solid #ddd', fontSize: 14, background: 'white'
                    }}
                  >
                    <option value="">Все типы</option>
                    <option value="image">Изображения</option>
                    <option value="video">Видео</option>
                    <option value="audio">Аудио</option>
                    <option value="document">Документы</option>
                    <option value="archive">Архивы</option>
                  </select>
                </div>
                <label style={{ fontSize: 13, color: '#666', display: 'flex', alignItems: 'center', gap: 6 }}>
                  <input
                      type="checkbox"
                      checked={searchInCurrentFolder}
                      onChange={(e) => {
                          setSearchInCurrentFolder(e.target.checked);
                          const folderId = e.target.checked ? currentFolderId : null;
                          fileApi.searchFiles(searchQuery, folderId, searchCategory).then(res => {
                              setSearchResults(res.data);
                          });
                      }}
                  />
                  Искать только в текущей папке
                </label>
              </div>

              {searchResults && (
                <div style={{ marginBottom: 16, padding: 12, background: '#fff', borderRadius: 12, border: '1px solid #e0e0e0' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                    <strong>Результаты поиска ({searchResults.length})</strong>
                    <button onClick={() => { setSearchResults(null); setSearchQuery(''); setSearchCategory(''); }}
                      style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#999' }}>
                      ✕ Закрыть
                    </button>
                  </div>
                  {searchResults.length === 0 ? (
                    <p style={{ color: '#999', textAlign: 'center', padding: 20 }}>Ничего не найдено</p>
                  ) : (
                    searchResults.map((file) => (
                      <div key={file.id}
                        style={{
                          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                          padding: '8px 12px', borderRadius: 8, marginBottom: 4,
                          background: '#f9f9f9', cursor: 'pointer'
                        }}
                        onClick={() => {
                          if (file.parentFolderId) {
                            setFolderStack([...folderStack, currentFolderId]);
                            handleNavigate(file.parentFolderId);
                          }
                        }}
                      >
                        <div>
                          <span style={{ fontWeight: 500 }}>{file.fileName}</span>
                          <span style={{ color: '#999', fontSize: 12, marginLeft: 8 }}>
                            {file.fileExtension?.toUpperCase()}
                          </span>
                        </div>
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            setFolderStack([...folderStack, currentFolderId]);
                            handleNavigate(file.parentFolderId);
                            setSearchResults(null);
                          }}
                          style={{
                            padding: '4px 10px', borderRadius: 6, border: '1px solid #ddd',
                            background: 'white', cursor: 'pointer', fontSize: 12
                          }}
                        >
                          Перейти в папку
                        </button>
                      </div>
                    ))
                  )}
                </div>
              )}

              <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
                {currentFolderId && (
                  <button onClick={handleBack} style={{ padding: '6px 12px', cursor: 'pointer' }}>
                    Назад
                  </button>
                )}
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