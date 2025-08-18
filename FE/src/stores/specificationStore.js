// src/stores/specificationStore.js
import { create } from 'zustand';

// priorityLevel을 priority로 변환하는 헬퍼 함수
const convertNumberToPriority = (priorityLevel) => {
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
    projectPurpose: '',
  },
  mainFeatures: [], // { id, name, description, subTasks: [...] }
  techStack: [],
  isSidebarVisible: false, // 요구사항 명세서 사이드바 표시 여부
  rawData: null, // 디버깅용 raw data
  projectId: null,
  specId: null,
  userUncheckedIds: new Set(),
};

// 명세서 초기화용 데이터 구조
const specInitialData = {
  projectOverview: {
    projectName: '',
    projectDescription: '',
    projectPurpose: '',
  },
  mainFeatures: [],
  techStack: [],
  showSidebar: false,
  rawData: null,
  projectId: null,
  specId: null,
  userUncheckedIds: new Set(),
};

export const useSpecificationStore = create((set, get) => ({
  ...initialState,

  // --- 폴링 제어 상태 ---
  isSpecPolling: false,
  _pollingTimer: null,

  // 명세서 초기화
  resetSpecification: () => set(specInitialData),

  // 프로젝트 정보 설정
  setProjectInfo: (projectId, specId) => set({ projectId, specId }),

  setProjectId: (projectId) => set({ projectId }),
  setSpecId: (specId) => set({ specId }),

  // 명세서 요약 정보 설정 (project:summarization)
  setProjectSummary: (summary) => set((state) => ({
    ...state,
    projectOverview: {
      projectName: summary.specTitle || summary.projectTitle || '',
      projectDescription: summary.projectDescription || '',
      projectPurpose: summary.projectPurpose || '',
    },
    projectId: summary.projectId || state.projectId,
    specId: summary.specId || state.specId,
  })),

  // 명세서 전체 또는 필드 단위로 기능 추가/업데이트
  setFeatures: (features) => set({ mainFeatures: features }),

  // 기능의 isOpen 상태 토글
  toggleFeatureOpen: (taskId) => set((state) => {
    const toggleOpen = (tasks) => {
      return tasks.map(task => {
        if (task.id === taskId) {
          return { ...task, isOpen: !task.isOpen };
        }
        if (task.subTasks) {
          return { ...task, subTasks: toggleOpen(task.subTasks) };
        }
        return task;
      });
    };

    return {
      mainFeatures: toggleOpen(state.mainFeatures)
    };
  }),

  // API 응답 데이터 처리 - 모든 spec 관련 타입 처리
  processSpecData: (data) => set((state) => {
    console.log('요구사항 명세서 데이터 처리 중:', data);

    // project:summarization 처리
    if (data.projectTitle || data.specTitle || data.projectDescription) {
      return {
        ...state,
        projectOverview: {
          projectName: data.specTitle || data.projectTitle || state.projectOverview.projectName,
          projectDescription: data.projectDescription || state.projectOverview.projectDescription,
          projectPurpose: data.projectPurpose || state.projectOverview.projectPurpose,
        },
        projectId: data.projectId || state.projectId,
        specId: data.specId || state.specId,
        rawData: data
      };
    }

    // spec, spec:regenerate, spec:add:field 처리
    if (data.field && data.mainFeature && data.subFeature) {
      // field를 최상위 기능으로, mainFeature를 subTask로, subFeature를 secondSubTask로 매핑
      const newFeature = {
        id: `field_${data.field}`, // field는 이름 기반 ID 사용 (DB에서 field는 이름으로 식별)
        name: data.field, // field를 최상위 기능명으로 사용
        description: data.field, // field를 description으로도 사용
        hours: (data.mainFeature.estimatedTime || 0) + data.subFeature.reduce((sum, sub) => sum + (sub.estimatedTime || 0), 0), // 전체 시간 합계
        priority: convertNumberToPriority(data.mainFeature.priorityLevel),
        isOpen: true,
        checked: true,
        subTasks: [
          {
            id: data.mainFeature.id,
            name: data.mainFeature.title,
            description: data.mainFeature.description,
            hours: data.mainFeature.estimatedTime || 0,
            priority: convertNumberToPriority(data.mainFeature.priorityLevel),
            checked: true,
            isOpen: true, // 기본적으로 열려있도록 설정
            subTasks: data.subFeature.map(sub => ({
              id: sub.id,
              name: sub.title,
              description: sub.description,
              hours: sub.estimatedTime || 0,
              priority: convertNumberToPriority(sub.priorityLevel),
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

    // spec:add:feature:sub 처리 (상세 기능 추가)
    if (data.parentFeatureId && data.featureSaveItem) {
      const newSubFeature = {
        id: data.featureSaveItem.id,
        name: data.featureSaveItem.title,
        description: data.featureSaveItem.description,
        hours: data.featureSaveItem.estimatedTime || 0,
        priority: convertNumberToPriority(data.featureSaveItem.priorityLevel),
        checked: true,
        isOpen: false,
        subTasks: [],
      };

      // parentFeatureId로 해당 기능을 찾아서 subTasks에 추가
      const newMainFeatures = state.mainFeatures.map(feature => {
        // 최상위 기능에서 찾기
        if (feature.id === data.parentFeatureId) {
          return {
            ...feature,
            subTasks: [...feature.subTasks, newSubFeature]
          };
        }

        // subTasks에서 찾기
        const updatedSubTasks = feature.subTasks.map(subTask => {
          if (subTask.id === data.parentFeatureId) {
            return {
              ...subTask,
              subTasks: [...subTask.subTasks, newSubFeature]
            };
          }
          return subTask;
        });

        return {
          ...feature,
          subTasks: updatedSubTasks
        };
      });

      return {
        mainFeatures: newMainFeatures,
        rawData: data
      };
    }

    // spec:add:feature:main 처리 (주 기능 추가)
    if (data.field && data.featureSaveItem) {
      const newMainFeature = {
        id: data.featureSaveItem.id,
        name: data.featureSaveItem.title,
        description: data.featureSaveItem.description,
        hours: data.featureSaveItem.estimatedTime || 0,
        priority: convertNumberToPriority(data.featureSaveItem.priorityLevel),
        checked: true,
        isOpen: false,
        subTasks: [],
      };

      // field 이름으로 해당 필드를 찾아서 subTasks에 추가
      const newMainFeatures = state.mainFeatures.map(feature => {
        if (feature.name === data.field) {
          return {
            ...feature,
            subTasks: [...feature.subTasks, newMainFeature]
          };
        }
        return feature;
      });

      return {
        mainFeatures: newMainFeatures,
        rawData: data
      };
    }

    return { ...state, rawData: data };
  }),

  // 명세서 재생성
  regenerateSpec: (features) => set({ mainFeatures: features }),

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

  openSidebar: () => set({ isSidebarVisible: true }),
  closeSidebar: () => set({ isSidebarVisible: false }),
  toggleSidebar: () => set((s) => ({ isSidebarVisible: !s.isSidebarVisible })),

  // 디버깅용: 현재 명세서 상태 출력
  debugPrintSpecification: () => {
    const state = get();
    console.log('=== 현재 명세서 상태 ===');
    console.log('프로젝트 정보:', {
      projectId: state.projectId,
      specId: state.specId
    });
    console.log('프로젝트 개요:', state.projectOverview);
    console.log('주요 기능:', state.mainFeatures);
    console.log('기술 스택:', state.techStack);
    console.log('Raw Data:', state.rawData);
    console.log('========================');
    return state;
  },
  // 수동으로 주 기능 추가, 페이지에서는 subTask로 추가
  addMainFeatureManually: (featureData) => set((state) => {
    const newFeature = {
      id: Date.now(), // 임시 ID (실제로는 API 응답에서 받아야 함)
      name: featureData.title,
      description: featureData.description,
      hours: featureData.estimatedTime || 0,
      priority: convertNumberToPriority(featureData.priorityLevel),
      isOpen: false,
      checked: true,
      subTasks: [],
    };

    return {
      mainFeatures: [...state.mainFeatures, newFeature]
    };
  }),

  // 필드 이름 기반으로 주 기능 추가 (필드 안의 subTasks에 추가)
  addMainFeatureToField: (fieldName, featureData) => set((state) => {
    console.log('addMainFeatureToField 호출됨:', { fieldName, featureData });

    const newSubFeature = {
      id: featureData.id || Date.now(), // API 응답에서 받은 featureId 사용
      name: featureData.title,
      description: featureData.description,
      hours: featureData.estimatedTime || 0,
      priority: convertNumberToPriority(featureData.priorityLevel),
      checked: true,
      isOpen: false,
      subTasks: [],
    };

    console.log('새로 추가할 주 기능:', newSubFeature);

    // 필드 이름으로 해당 필드를 찾아서 subTasks에 추가
    const newMainFeatures = state.mainFeatures.map(feature => {
      if (feature.name === fieldName) {
        console.log('필드 찾음:', feature.name, '기존 subTasks:', feature.subTasks);
        return {
          ...feature,
          subTasks: [...feature.subTasks, newSubFeature]
        };
      }
      return feature;
    });

    console.log('업데이트된 mainFeatures:', newMainFeatures);

    return {
      mainFeatures: newMainFeatures
    };
  }),

  // 수동으로 상세 기능 추가, 페이지에서는 secondSubTask로 추가
  addSubFeatureManually: (parentFeatureId, featureData) => set((state) => {
    console.log('addSubFeatureManually 호출됨:', { parentFeatureId, featureData });

    const newSubFeature = {
      id: featureData.id || Date.now(), // API 응답에서 받은 featureId 사용
      name: featureData.title,
      description: featureData.description,
      hours: featureData.estimatedTime || 0,
      priority: convertNumberToPriority(featureData.priorityLevel),
      checked: true,
      isOpen: false,
      subTasks: [],
    };

    console.log('새로 추가할 상세 기능:', newSubFeature);

    // 재귀적으로 부모 찾기 및 추가
    const addToFeature = (features, targetId) => {
      return features.map(feature => {
        if (feature.id === targetId) {
          console.log('부모 기능 찾음:', feature.name, '기존 subTasks:', feature.subTasks);
          return {
            ...feature,
            subTasks: [...feature.subTasks, newSubFeature]
          };
        }

        if (feature.subTasks && feature.subTasks.length > 0) {
          return {
            ...feature,
            subTasks: addToFeature(feature.subTasks, targetId)
          };
        }

        return feature;
      });
    };

    const updatedFeatures = addToFeature(state.mainFeatures, parentFeatureId);
    console.log('업데이트된 mainFeatures:', updatedFeatures);

    return {
      mainFeatures: updatedFeatures
    };
  }),

  // API 응답으로 실제 ID 업데이트
  updateFeatureId: (tempId, realId) => set((state) => {
    const updateIdRecursive = (features) => {
      return features.map(feature => {
        if (feature.id === tempId) {
          return { ...feature, id: realId };
        }

        if (feature.subTasks && feature.subTasks.length > 0) {
          return {
            ...feature,
            subTasks: updateIdRecursive(feature.subTasks)
          };
        }

        return feature;
      });
    };

    return {
      mainFeatures: updateIdRecursive(state.mainFeatures)
    };
  }),

  // 특정 기능을 ID로 찾기 (부모-자식 관계 확인용)
  findFeatureById: (featureId) => {
    const state = get();

    const searchFeature = (features, targetId, parentId = null) => {
      for (const feature of features) {
        if (feature.id === targetId) {
          return { feature, parentId };
        }

        if (feature.subTasks && feature.subTasks.length > 0) {
          const result = searchFeature(feature.subTasks, targetId, feature.id);
          if (result) {
            return result;
          }
        }
      }
      return null;
    };

    return searchFeature(state.mainFeatures, featureId);
  },

  // 체크박스 토글 상태 업데이트 (API 호출 후 UI 상태 동기화용)
  toggleFeatureChecked:(taskId, newChecked, isUserAction = true) => set((state) => {
    console.log(`=== 체크박스 토글 (사용자 액션: ${isUserAction}): ${taskId} -> ${newChecked} ===`);
    
    // userUncheckedIds를 Set으로 보장
    let newUserUncheckedIds = state.userUncheckedIds instanceof Set 
      ? new Set(state.userUncheckedIds)
      : new Set(state.userUncheckedIds || []);
    
    // 사용자가 직접 클릭한 경우에만 추적 (field_ 제외)
    if (isUserAction && !taskId.toString().startsWith('field_')) {
      if (newChecked) {
        // 체크 시: 해제 목록에서 제거
        console.log(`사용자 해제 목록에서 제거 시도: ${taskId}`);
        const deleted = newUserUncheckedIds.delete(taskId);
        console.log(`제거 성공: ${deleted}`);
      } else {
        // 해제 시: 해제 목록에 추가
        console.log(`사용자 해제 목록에 추가: ${taskId}`);
        newUserUncheckedIds.add(taskId);
      }
    }
  
    const updateFeatureRecursive = (features) => {
      return features.map(feature => {
        if (feature.id === taskId) {
          console.log(`대상 기능 찾음: ${feature.name} (${feature.id})`);
          
          const hasChildren = feature.subTasks && feature.subTasks.length > 0;
          
          if (hasChildren) {
            // 부모 기능 토글 - 모든 자식들도 같은 상태로 변경
            console.log('부모 기능 토글 - 모든 자식들도 같은 상태로 변경');
            
            const updateAllChildren = (subTasks) => {
              return subTasks.map(subTask => {
                // 부모가 해제되면 자식들을 사용자 해제 목록에서 제거
                if (!newChecked && !subTask.id.toString().startsWith('field_')) {
                  newUserUncheckedIds.delete(subTask.id);
                }
                
                return {
                  ...subTask,
                  checked: newChecked,
                  subTasks: subTask.subTasks ? updateAllChildren(subTask.subTasks) : []
                };
              });
            };
  
            return {
              ...feature,
              checked: newChecked,
              subTasks: updateAllChildren(feature.subTasks || [])
            };
          } else {
            // 자식 기능 토글 - 해당 자식만 변경
            console.log('자식 기능 토글 - 개별 변경');
            return {
              ...feature,
              checked: newChecked
            };
          }
        }
        
        if (feature.subTasks && feature.subTasks.length > 0) {
          return {
            ...feature,
            subTasks: updateFeatureRecursive(feature.subTasks)
          };
        }
        
        return feature;
      });
    };
  
    const updatedFeatures = updateFeatureRecursive(state.mainFeatures);
    
    // 부모들의 체크 상태를 자식들 상태에 따라 업데이트
    const updateParentStates = (features) => {
      return features.map(feature => {
        if (feature.subTasks && feature.subTasks.length > 0) {
          const updatedSubs = updateParentStates(feature.subTasks);
          
          const noChildrenChecked = updatedSubs.every(st => !st.checked);
          const someChildrenChecked = updatedSubs.some(st => st.checked);
          
          let newParentChecked = feature.checked;
          
          if (someChildrenChecked && !feature.checked) {
            // 자식이 체크되면 부모도 체크
            newParentChecked = true;
            console.log(`부모 ${feature.name} 자동 체크 (자식 중 하나라도 체크됨)`);
          } else if (noChildrenChecked && feature.checked) {
            // 모든 자식이 해제되면 부모도 해제
            newParentChecked = false;
            console.log(`부모 ${feature.name} 자동 해제 (모든 자식이 해제됨)`);
          }
          
          return { 
            ...feature, 
            subTasks: updatedSubs, 
            checked: newParentChecked 
          };
        }
        return feature;
      });
    };
  
    let finalFeatures = updateParentStates(updatedFeatures);
    finalFeatures = updateParentStates(finalFeatures);
    
    console.log('=== 최종 결과 ===');
    console.log('사용자 해제 목록:', Array.from(newUserUncheckedIds));
    
    return {
      mainFeatures: finalFeatures,
      userUncheckedIds: newUserUncheckedIds
    };
  }),

  // 요구사항 명세서가 있는지 확인하는 헬퍼 함수
  hasSpecification: () => {
    const state = get();
    return state.mainFeatures && state.mainFeatures.length > 0;
  },

  // 요구사항 명세서의 기능 개수 반환
  getSpecificationCount: () => {
    const state = get();
    return state.mainFeatures ? state.mainFeatures.length : 0;
  },

  // --- 폴링 제어 함수 ---
  startSpecPolling: (ms = 20000) => {
    const { _pollingTimer } = get();
    if (_pollingTimer) clearTimeout(_pollingTimer);
    const timer = setTimeout(() => {
      set({ isSpecPolling: false, _pollingTimer: null });
    }, ms);
    set({ isSpecPolling: true, _pollingTimer: timer });
  },
  extendSpecPolling: (ms = 20000) => {
    // 실행 중이면 남은 시간을 리셋(연장)
    get().startSpecPolling(ms);
  },
  stopSpecPolling: () => {
    const { _pollingTimer } = get();
    if (_pollingTimer) clearTimeout(_pollingTimer);
    set({ isSpecPolling: false, _pollingTimer: null });
  },
  finalizeSpecification: () => set({ isSpecificationFinalized: true }),
}));