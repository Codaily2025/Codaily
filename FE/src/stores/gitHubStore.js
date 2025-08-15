// src/stores/gitHubStore.js
import { create } from 'zustand';

export const useGitHubStore = create((set, get) => ({
  // 상태
  repositories: [],
  isLoading: false,
  error: null,

  // 액션
  setRepositories: (repositories) => set({ repositories }),
  
  addRepository: (repository) => set((state) => ({
    repositories: [...state.repositories, repository]
  })),
  
  removeRepository: (repoId) => set((state) => ({
    repositories: state.repositories.filter(repo => repo.repoId !== repoId)
  })),
  
  setLoading: (isLoading) => set({ isLoading }),
  
  setError: (error) => set({ error }),
  
  clearError: () => set({ error: null }),
}));