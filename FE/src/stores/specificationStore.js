// src/stores/specificationStore.js
import { create } from 'zustand';

// API 응답 데이터 구조에 맞춰 초기 상태 정의
const initialState = {
  projectOverview: {
    projectName: '',
    projectDescription: '',
    projectPurpose: '', // 필요시 추가
  },
  mainFeatures: [], // { id, name, description, subTasks: [...] }
  techStack: [],
};

export const useSpecificationStore = create((set) => ({
  ...initialState,
  
  // 명세서 요약 정보 설정
  setProjectSummary: (summary) => set((state) => ({
    ...state,
    projectOverview: {
      projectName: summary.projectTitle,
      projectDescription: summary.projectDescription,
      projectPurpose: state.projectOverview.projectPurpose, // 기존 값 유지 또는 업데이트
    },
  })),

  // 명세서 전체 또는 필드 단위로 기능 추가/업데이트
  setFeatures: (features) => set({ mainFeatures: features }),
  
  // 기존 명세서에 새 기능 그룹(필드) 추가
  addFeatureField: (featureGroup) => set((state) => {
     // API 데이터 구조를 UI 데이터 구조로 변환
     const newFeature = {
        id: featureGroup.mainFeature.id,
        name: featureGroup.mainFeature.title,
        description: featureGroup.mainFeature.description,
        hours: featureGroup.mainFeature.estimatedTime,
        priority: featureGroup.mainFeature.priorityLevel,
        isOpen: true,
        checked: true,
        subTasks: featureGroup.subFeature.map(sub => ({
            id: sub.id,
            name: sub.title,
            description: sub.description,
            hours: sub.estimatedTime,
            priority: sub.priorityLevel,
            checked: true,
            isOpen: false,
            subTasks: [],
        })),
     };
     return { mainFeatures: [...state.mainFeatures, newFeature] };
  }),

  // 기존 주기능에 상세 기능 추가
  addSubFeature: (payload) => set((state) => {
    const newSubFeature = {
        id: payload.featureSaveItem.id,
        name: payload.featureSaveItem.title,
        description: payload.featureSaveItem.description,
        hours: payload.featureSaveItem.estimatedTime,
        priority: payload.featureSaveItem.priorityLevel,
        checked: true,
        isOpen: false,
        subTasks: [],
    };
    
    const newMainFeatures = state.mainFeatures.map(feature => {
        if (feature.id === payload.parentFeatureId) {
            return {
                ...feature,
                subTasks: [...feature.subTasks, newSubFeature]
            };
        }
        return feature;
    });

    return { mainFeatures: newMainFeatures };
  }),

  // 명세서 재생성
  regenerateSpec: (features) => set({ mainFeatures: features }),
}));