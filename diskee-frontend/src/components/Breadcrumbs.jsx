import React from 'react';

const Breadcrumbs = ({ path, onNavigate }) => {
  return (
    <div className="breadcrumbs">
      <button onClick={() => onNavigate(null)} className="breadcrumb-home">
        🏠 Мои файлы
      </button>
      {path.map((folder, index) => (
        <React.Fragment key={folder.id}>
          <span className="breadcrumb-separator">/</span>
          <button
            onClick={() => onNavigate(folder.id)}
            className="breadcrumb-item"
          >
            {folder.name}
          </button>
        </React.Fragment>
      ))}
    </div>
  );
};

export default Breadcrumbs;