import React, { useEffect, useRef } from 'react';

export function ContextMenu({ x, y, items, onClose }) {
  const menuRef = useRef(null);

  useEffect(() => {
    const handler = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        onClose();
      }
    };
    document.addEventListener('click', handler);
    return () => document.removeEventListener('click', handler);
  }, [onClose]);

  return (
    <ul
      ref={menuRef}
      className="context-menu"
      style={{ position: 'fixed', left: x, top: y, zIndex: 1000 }}
    >
      {items.map((item, i) => (
        <li key={i} onClick={() => { item.action(); onClose(); }}>
          {item.label}
        </li>
      ))}
    </ul>
  );
}