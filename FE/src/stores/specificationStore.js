// src/stores/specificationStore.js
import { create } from 'zustand';

// priorityLevel을 priority로 변환하는 헬퍼 함수
const convertPriorityLevel = (priorityLevel) => {
  if (priorityLevel === null || priorityLevel === undefined) return 'Normal';
  if (priorityLevel < 3) return 'High';
  if (priorityLevel < 7) return 'Normal';
  return 'Low';
};

// API 응답 데이터 구조에 맞춰 초기 상태 정의
const initialState = {
  projectOverview: {
    projectName: '',
    projectDescription: '',
    projectPurpose: '', // 필요시 추가
  },
  mainFeatures: [], // { id, name, description, subTasks: [...] }
  techStack: [],
  showSidebar: false, // 요구사항 명세서 사이드바 표시 여부
  rawData: null, // 디버깅용 raw data
};

export const useSpecificationStore = create((set) => ({
  ...initialState,
  
  // 명세서 요약 정보 설정
  setProjectSummary: (summary) => set((state) => ({
    ...state,
    projectOverview: {
      // projectName: summary.projectTitle,
      projectName: summary.specTitle,
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
        hours: featureGroup.mainFeature.estimatedTime || 0,
        priority: convertPriorityLevel(featureGroup.mainFeature.priorityLevel),
        isOpen: true,
        checked: true,
        subTasks: featureGroup.subFeature.map(sub => ({
            id: sub.id,
            name: sub.title,
            description: sub.description,
            hours: sub.estimatedTime || 0,
            priority: convertPriorityLevel(sub.priorityLevel),
            checked: true,
            isOpen: false,
            subTasks: [],
        })),
     };
     return { mainFeatures: [...state.mainFeatures, newFeature] };
  }),

  // 새로운 spec 데이터 처리 (spec, spec:regenerate, spec:add:field)
  processSpecData: (data) => set((state) => {
    console.log('Processing spec data:', data);
    
    // field가 있는 경우 새로운 기능 그룹 추가
    if (data.field && data.mainFeature && data.subFeature) {
      // field를 최상위 기능으로, mainFeature를 subTask로, subFeature를 secondSubTask로 매핑
      const newFeature = {
        id: Date.now(), // field는 고유 ID가 없으므로 timestamp 사용
        name: data.field, // field를 최상위 기능명으로 사용
        description: data.field, // field를 description으로도 사용
        hours: (data.mainFeature.estimatedTime || 0) + data.subFeature.reduce((sum, sub) => sum + (sub.estimatedTime || 0), 0), // 전체 시간 합계
        priority: convertPriorityLevel(data.mainFeature.priorityLevel),
        isOpen: true,
        checked: true,
        subTasks: [
          {
            id: data.mainFeature.id,
            name: data.mainFeature.title,
            description: data.mainFeature.description,
            hours: data.mainFeature.estimatedTime || 0,
            priority: convertPriorityLevel(data.mainFeature.priorityLevel),
            checked: true,
            isOpen: true, // 기본적으로 열려있도록 설정
            subTasks: data.subFeature.map(sub => ({
              id: sub.id,
              name: sub.title,
              description: sub.description,
              hours: sub.estimatedTime || 0,
              priority: convertPriorityLevel(sub.priorityLevel),
              checked: true,
              isOpen: false,
              subTasks: [],
            })),
          }
        ],
      };
      
      // 기존에 같은 field명이 있는지 확인하고 있으면 업데이트, 없으면 추가
      const existingIndex = state.mainFeatures.findIndex(f => f.name === data.field);
      let newMainFeatures;
      
      if (existingIndex >= 0) {
        // 기존 기능 업데이트
        newMainFeatures = [...state.mainFeatures];
        newMainFeatures[existingIndex] = newFeature;
      } else {
        // 새 기능 추가
        newMainFeatures = [...state.mainFeatures, newFeature];
      }
      
      return { 
        mainFeatures: newMainFeatures,
        rawData: data // 디버깅용
      };
    }
    
    return { rawData: data }; // 디버깅용
  }),

  // 기존 주기능에 상세 기능 추가
  addSubFeature: (payload) => set((state) => {
    const newSubFeature = {
        id: payload.featureSaveItem.id,
        name: payload.featureSaveItem.title,
        description: payload.featureSaveItem.description,
        hours: payload.featureSaveItem.estimatedTime || 0,
        priority: convertPriorityLevel(payload.featureSaveItem.priorityLevel),
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

  // 새로운 데이터 구조를 처리하기 위한 임시 액션 (디버깅용)
  addRawSpecData: (data) => set((state) => {
    console.log('Raw spec data received:', data);
    // 임시로 데이터를 그대로 저장하여 구조 확인
    return {
      ...state,
      rawData: data
    };
  }),

  // 기술 스택 설정
  setTechStack: (techStack) => set({ techStack }),

  // 프로젝트 개요 설정
  setProjectOverview: (overview) => set((state) => ({
    ...state,
    projectOverview: {
      ...state.projectOverview,
      ...overview
    }
  })),

  // 사이드바 표시/숨김 제어
  showSidebar: () => {
    console.log('showSidebar 함수 호출됨');
    set({ showSidebar: true });
  },
  hideSidebar: () => {
    console.log('hideSidebar 함수 호출됨');
    set({ showSidebar: false });
  },
  toggleSidebar: () => set((state) => ({ showSidebar: !state.showSidebar })),
}));