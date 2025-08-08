// src/stores/authStore.js
import { create } from 'zustand';

export const useAuthStore = create((set) => ({
// export const useAuthStore = create((set, get) => ({
  token: localStorage.getItem('authToken') || null,
  isAuthenticated: !!localStorage.getItem('authToken'),
  // user: JSON.parse(localStorage.getItem('user')) || null,
  
  setToken: (token) => {
    if (token) {
      localStorage.setItem('authToken', token);
      set({ token, isAuthenticated: true });
      console.log('Token saved to store and localStorage');
    } else {
      console.warn('Attempted to set null or empty token');
    }
  },
  
  // setUser: (user) => {
  //   if (user) {
  //     localStorage.setItem('user', JSON.stringify(user));
  //     set({ user });
  //     console.log('User saved to store and localStorage');
  //   } else {
  //     console.warn('Attempted to set null or empty user');
  //   }
  // },
  
  // getUser: () => {
  //   return get().user;
  // },
  
  logout: () => {
    localStorage.removeItem('authToken');
    set({ token: null, isAuthenticated: false });
    console.log('Logged out, token removed');
    // localStorage.removeItem('user');
    // set({ token: null, isAuthenticated: false, user: null });
    // console.log('Logged out, token and user removed');
  },
}));
