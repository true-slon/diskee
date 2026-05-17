import React from 'react';

const DemoDashboard = () => {
  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <div className="logo">
          <h1>Diskee Cloud</h1>
        </div>
        <div className="user-info">
          <span className="user-name">Тестовый пользователь</span>
          <button onClick={() => window.location.reload()} className="logout-btn">
            Выйти
          </button>
        </div>
      </header>

      <div className="dashboard-content">
        <aside className="sidebar">
          <div className="storage-section">
            <h3>Хранилище</h3>
            <div className="storage-info">
              <div className="storage-stats">5.12 GB / 10 GB</div>
              <div className="storage-bar">
                <div className="storage-bar-fill" style={{ width: '51%' }}></div>
              </div>
              <div className="storage-percent">51% использовано</div>
            </div>
          </div>
          <nav className="sidebar-nav">
            <button className="nav-item active">📁 Мои файлы</button>
            <button className="nav-item">🕒 Недавние</button>
            <button className="nav-item">⭐ Избранное</button>
            <button className="nav-item">🗑️ Корзина</button>
          </nav>
        </aside>

        <main className="main-content">
          <div className="upload-area">
            <div className="upload-content">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path d="M12 3v12m0 0l-3-3m3 3l3-3M5 17h14" strokeWidth="2" strokeLinecap="round"/>
              </svg>
              <p>Перетащите файлы сюда или нажмите для выбора</p>
            </div>
          </div>

          <div className="files-container grid">
            <div className="file-item-grid">
              <div className="file-icon">📁</div>
              <div className="file-name">Документы</div>
            </div>
            <div className="file-item-grid">
              <div className="file-icon">📁</div>
              <div className="file-name">Фото</div>
            </div>
            <div className="file-item-grid">
              <div className="file-icon">📄</div>
              <div className="file-name">report.pdf</div>
              <div className="file-meta">
                <span>1.2 MB</span>
                <span>2 часа назад</span>
              </div>
            </div>
            <div className="file-item-grid">
              <div className="file-icon">🖼️</div>
              <div className="file-name">vacation.jpg</div>
              <div className="file-meta">
                <span>2.5 MB</span>
                <span>вчера</span>
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>
  );
};

export default DemoDashboard;