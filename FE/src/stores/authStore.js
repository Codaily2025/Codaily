// src/stores/authStore.js
import { create } from 'zustand';

export const useAuthStore = create((set) => ({
  token: localStorage.getItem('authToken') || null,
  isAuthenticated: !!localStorage.getItem('authToken'),
  setToken: (token) => {
    if (token) {
      localStorage.setItem('authToken', token);
      set({ token, isAuthenticated: true });
      console.log('Token saved to store and localStorage');
    } else {
      console.warn('Attempted to set null or empty token');
    }
  },
  logout: () => {
    localStorage.removeItem('authToken');
    set({ token: null, isAuthenticated: false });
    console.log('Logged out, token removed');
  },
}));
