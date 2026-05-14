import React, { createContext, useState, useContext, useEffect } from 'react';
import axios from '../api/axios';

const AuthContext = createContext();

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Временно отключаем проверку аутентификации для тестирования
    // const token = localStorage.getItem('accessToken');
    // if (token) {
    //   fetchUser();
    // } else {
    //   setLoading(false);
    // }
    
    // Для тестирования: автоматически логиним тестового пользователя
    setUser({ email: 'test@example.com', displayName: 'Test User' });
    setLoading(false);
  }, []);

  const fetchUser = async () => {
    try {
      const response = await axios.get('/auth/me');
      setUser(response.data);
    } catch (error) {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
    } finally {
      setLoading(false);
    }
  };

  const login = async (email, password) => {
    // Временно: просто логиним без API
    setUser({ email, displayName: email.split('@')[0] });
    localStorage.setItem('accessToken', 'fake-token');
    return { data: { accessToken: 'fake-token' } };
    
    // Реальный код:
    // const response = await axios.post('/auth/login', { email, password });
    // localStorage.setItem('accessToken', response.data.accessToken);
    // localStorage.setItem('refreshToken', response.data.refreshToken);
    // await fetchUser();
    // return response.data;
  };

  const register = async (email, password, displayName) => {
    // Временно: имитируем регистрацию
    return { data: { message: 'User registered' } };
    
    // Реальный код:
    // const response = await axios.post('/auth/register', { email, password, displayName });
    // return response.data;
  };

  const logout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};