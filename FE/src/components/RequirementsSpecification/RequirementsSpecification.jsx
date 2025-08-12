// FE/src/components/RequirementsSpecification/RequirementsSpecification.jsx
import React, { useState, useCallback, useEffect } from 'react';
import styles from './RequirementsSpecification.module.css';
import TechTag from './TechTag';
import Checkbox from './Checkbox';
import TimeIndicator from './TimeIndicator';
import PriorityBadge from './PriorityBadge';
import AddTaskModal from './AddTaskModal';
import { useSpecificationStore } from '../../stores/specificationStore'; // 스토어 임포트
import { addManualFeature, buildMainFeatureRequest, buildSubFeatureRequest, buildMainFeatureToFieldRequest } from '../../apis/chatApi';

// 초기 데이터 구조 정의
const initialRequirementsData = [
  {
    requirementId: 1,
    completionDate: '2025-08-23',
    projectOverview: {
      projectName: 'RAG 요리 레시피 챗봇',
      projectPurpose: '가정에서 쉽게 요리하고 싶은 사람들을 위한 AI 요리 도우미',
      projectDescription: '식품영양DB API를 활용한 사용자 맞춤형 메뉴 추천 플랫폼입니다. 사용자가 레시피를 요청하면 해당 요리의 레시피 정보를 알려줍니다. 사용자가 영양 정보를 요청하면 특정 요리의 영양 정보를 알려줍니다. 사용자가 사용하기를 원하는 재료를 입력하면 재료들을 활용할 수 있는 레시피를 알려줍니다. RAG 파이프라인을 사용해 AI 기반의 응답을 생성하여 사용자에게 모바일 웹 화면으로 보여줍니다.',
    },
    techStack: ['Python', 'FastAPI', 'RAG Pipeline', 'Vector DB', 'AWS EC2', 'AWS RDS', 'AWS S3'],
    mainFeatures: [
      {
        id: 1,
        name: '회원가입',
        priority: 'Low',
        hours: 5,
        checked: true,
        isOpen: false,
        subTasks: [
          { id: 11, name: '일반 회원가입', priority: 'Normal', hours: 2, checked: true, isOpen: false, subTasks: [] },
          { id: 12, name: '카카오톡 회원가입 연동', priority: 'Normal', hours: 3, checked: true, isOpen: false, subTasks: [] },
        ],
      },
      {
        id: 2,
        name: '데이터 수집 및 전처리',
        priority: 'High',
        hours: 8,
        checked: true,
        isOpen: false,
        subTasks: [
          {
            id: 21,
            name: '데이터 수집',
            priority: 'High',
            hours: 4,
            checked: true,
            isOpen: false,
            subTasks: [
              { id: 211, name: '웹페이지 크롤링', priority: 'Normal', hours: 2, checked: true, isOpen: false, subTasks: [] },
              { id: 212, name: 'API', priority: 'Normal', hours: 2, checked: true, isOpen: false, subTasks: [] },
            ],
          },
          {
            id: 22,
            name: '데이터 전처리',
            priority: 'High',
            hours: 4,
            checked: true,
            isOpen: false,
            subTasks: [
              { id: 221, name: '결측치 처리', priority: 'Normal', hours: 1, checked: true, isOpen: false, subTasks: [] },
              { id: 222, name: '데이터 정규화', priority: 'High', hours: 2, checked: true, isOpen: false, subTasks: [] },
              { id: 223, name: '텍스트 토큰화', priority: 'Normal', hours: 1, checked: true, isOpen: false, subTasks: [] },
            ],
          },
        ]
      },
      {
        id: 3,
        name: '배포',
        priority: 'Normal',
        hours: 5,
        checked: true,
        isOpen: false,
        subTasks: [
          { id: 31, name: 'EC2 인스턴스 설정', priority: 'High', hours: 2, checked: true, isOpen: false, subTasks: [] },
          { id: 32, name: 'RDS 데이터베이스 연결', priority: 'Normal', hours: 2, checked: true, isOpen: false, subTasks: [] },
          { id: 33, name: 'S3 버킷 생성 및 정적 파일 호스팅', priority: 'Low', hours: 1, checked: true, isOpen: false, subTasks: [] },
        ],
      },
    ]
  }];

// AddNewTaskButton 컴포넌트
const AddNewTaskButton = ({ onClick, text = "새 작업 추가" }) => (
  <div className={styles.addNewTaskSection} style={{ cursor: 'pointer' }}>
    <div className={styles.addNewTaskButton} onClick={onClick}>
      <div className={styles.addIconContainer}>
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M8 3.33325V12.6666" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round" />
          <path d="M3.33594 8H12.6693" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      </div>
      <div className={styles.addNewTaskText}>
        <div className={styles.addNewTaskTextContent}>{text}</div>
      </div>
    </div>
  </div>
);

const SecondSubTaskItem = ({ task, onToggleOpen, onToggleChecked, level = 0, parentId }) => {
  return (
    /* 클릭했을 때 상위 task의 드롭다운이 닫히면 안됨, 이벤트 막기 */
    <div className={styles.expandedSectionItem} onClick={(e) => {
      e.stopPropagation();
      // onToggleOpen(parentId);
    }}>
      <div className={styles.subTaskLeft}>
        <Checkbox 
        checked={task.checked} 
        onChange={(e) => {
          e.stopPropagation();
          onToggleChecked(task.id);
        }} />
        <div className={styles.subTaskNameContainer}>
          <div className={styles.subTaskName}>{task.name}</div>
        </div>
        <PriorityBadge level={task.priority} />
      </div>
      <div className={styles.subTaskRight} style={{ marginRight: '12px' }}>
        <div className={styles.subTaskActions}>
          <TimeIndicator hours={task.hours} />
        </div>
      </div>
    </div>
  );
};

const SubTaskItem = ({ task, onToggleOpen, onToggleChecked, onAddSubTask, level = 0 }) => {
  // SVG 아이콘 컴포넌트
  const ExpandIcon = ({ isOpen }) => (
    <div className={styles.expandIconContainer} style={{ transform: isOpen ? 'rotate(180deg)' : 'rotate(0deg)', transition: 'transform 0.2s' }}>
      <svg width="20" height="21" viewBox="0 0 20 21" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M15 8L10 13L5 8" stroke="#6C757D" strokeWidth="2.08333" strokeLinecap="round" strokeLinejoin="round" />
      </svg>
    </div>
  );
  const hasSecondSubTasks = task.subTasks && task.subTasks.length > 0;
  return (
    <>
      {hasSecondSubTasks && task.isOpen ? (
        <div className={styles.expandedSection} onClick={() => onToggleOpen(task.id)}>
          <div className={styles.expandedSectionHeader}>
            <div className={styles.expandedSectionHeaderInner}>
              <div className={styles.subTaskLeft}>
                <Checkbox 
                checked={task.checked} 
                onChange={(e) => {
                  e.stopPropagation();
                  onToggleChecked(task.id);
                }}
                />
                <div className={styles.subTaskNameContainer}>
                  <div className={styles.subTaskName}>{task.name}</div>
                </div>
                <PriorityBadge level={task.priority} />
              </div>
              <div className={styles.mainFeatureHeaderRight}>
                <TimeIndicator hours={task.hours} />
                <div className={styles.expandIconContainer} style={{ transform: task.isOpen ? 'rotate(0deg)' : 'rotate(180deg)', transition: 'transform 0.2s' }}>
                  <svg width="20" height="21" viewBox="0 0 20 21" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M15 13L10 8L5 13" stroke="#6C757D" strokeWidth="2.08333" strokeLinecap="round" strokeLinejoin="round" />
                  </svg>
                </div>
              </div>
            </div>
          </div>
          <div className={styles.expandedSectionBody}>
            <div className={styles.expandedSectionItems}>
              {task.subTasks.map(subTask => (
                <SecondSubTaskItem key={subTask.id} task={subTask} onToggleOpen={onToggleOpen} onToggleChecked={onToggleChecked} level={level + 1} parentId={task.id}
                />
              ))}
              {/* 상세 기능 추가 버튼 */}
              <AddNewTaskButton 
                onClick={(e) => {
                  e.stopPropagation();
                  onAddSubTask(task);
                }} 
                text="상세 기능 추가" 
              />
            </div>
          </div>
        </div>
      ) : (
        <div className={styles.subTaskItem} onClick={() => onToggleOpen(task.id)}>
          <div className={styles.subTaskLeft}>
            <Checkbox 
            checked={task.checked} 
            onChange={(e) => {
              e.stopPropagation();
              onToggleChecked(task.id);
            }} />
            <div className={styles.subTaskNameContainer}>
              <div className={styles.subTaskName}>{task.name}</div>
            </div>
            <PriorityBadge level={task.priority} />
          </div>
          {hasSecondSubTasks ? (
            <div className={styles.mainFeatureHeaderRight}>
              <TimeIndicator hours={task.hours} />
              <div className={styles.expandIconContainer} style={{ transform: task.isOpen ? 'rotate(0deg)' : 'rotate(180deg)', transition: 'transform 0.2s' }}>
                <svg width="20" height="21" viewBox="0 0 20 21" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M15 13L10 8L5 13" stroke="#6C757D" strokeWidth="2.08333" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
              </div>
            </div>
          ) : (
            <div className={styles.subTaskRight}>
              <div className={styles.subTaskActions}>
                <TimeIndicator hours={task.hours} />
              </div>
            </div>
          )}
        </div>
      )}
    </>
  );
};

// 작업을 렌더링하는 컴포넌트
const TaskItem = ({ task, onToggleOpen, onToggleChecked, onAddSubTask, onOpenModal, level = 0 }) => {
  const hasSubTasks = task.subTasks && task.subTasks.length > 0;

  // SVG 아이콘 컴포넌트
  const ExpandIcon = ({ isOpen }) => (
    <div className={styles.expandIconContainer} style={{ transform: isOpen ? 'rotate(180deg)' : 'rotate(0deg)', transition: 'transform 0.2s' }}>
      <svg width="20" height="21" viewBox="0 0 20 21" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M15 8L10 13L5 8" stroke="#6C757D" strokeWidth="2.08333" strokeLinecap="round" strokeLinejoin="round" />
      </svg>
    </div>
  );


  return (
    <div className={level === 0 ? styles.mainFeatureCard : styles.subTaskItem}>
      <div className={styles.mainFeatureHeader} onClick={() => hasSubTasks && onToggleOpen(task.id)}>
        <div className={styles.mainFeatureHeaderLeft}>
          <Checkbox 
          checked={task.checked} 
          onChange={(e) => {
            e.stopPropagation();
            onToggleChecked(task.id);
          }} />
          <div className={styles.mainFeatureName}>{task.name}</div>
          <PriorityBadge level={task.priority} />
        </div>
        <div className={styles.mainFeatureHeaderRight}>
          <TimeIndicator hours={task.hours} />
          {hasSubTasks && <ExpandIcon isOpen={task.isOpen} />}
        </div>
      </div>
      {hasSubTasks && task.isOpen && (
        <div className={styles.mainFeatureContent}>
          <div className={styles.mainFeatureItems}>
            {task.subTasks.map(subTask => (
              <SubTaskItem
                key={subTask.id}
                task={subTask}
                onToggleOpen={onToggleOpen}
                onToggleChecked={onToggleChecked}
                level={level + 1}
                parentId={task.id}
                onAddSubTask={onAddSubTask}
              />
            ))}
            <AddNewTaskButton 
              onClick={() => onOpenModal('main', task)} 
              text="주 기능 추가" 
            />
          </div>
        </div>
      )}
    </div>
  );
};


const RequirementsSpecification = () => {
  const { 
    projectOverview, 
    mainFeatures, 
    techStack, 
    rawData, 
    projectId,
    specId,
    processSpecData, 
    resetSpecification,
    debugPrintSpecification,
    addMainFeatureManually,
    addSubFeatureManually,
    addMainFeatureToField
  } = useSpecificationStore();
  
  const tags = ['Python', 'FastAPI', 'RAG Pipeline', 'Vector DB', 'AWS EC2', 'AWS RDS', 'AWS S3'];
  const [requirements] = useState(initialRequirementsData);
  // mainFeatures가 있으면 사용하고, 없으면 초기 데이터 사용
  const [features, setFeatures] = useState(mainFeatures && mainFeatures.length > 0 ? mainFeatures : initialRequirementsData[0].mainFeatures);

  // 모달 상태 관리 -> 상세 기능 추가 모달 열기 위해 필요
  const [modalState, setModalState] = useState({
    isOpen: false,
    taskType: null,
    parentTask: null
  });
  const [isAddTaskModalOpen, setIsAddTaskModalOpen] = useState(false);

  const handleAddTask = async (taskData) => {
    try {
      if (!projectId) {
        throw new Error('프로젝트 ID가 없습니다.');
      }

      console.log('작업 추가 시작 - 프로젝트 정보:', { projectId, specId });
      console.log('modalState:', modalState);
      console.log('taskData:', taskData);

      let requestData;
      let response;

      if (modalState.taskType === 'main') {
        // 주 기능 추가 - 필드 이름을 사용하여 해당 필드 안에 추가
        const fieldName = modalState.parentTask ? modalState.parentTask.name : 'Custom Feature';
        console.log('주 기능 추가 - fieldName:', fieldName);
        requestData = buildMainFeatureToFieldRequest(taskData, projectId, fieldName);
        console.log('주 기능 추가 API 요청 데이터:', requestData);
        response = await addManualFeature(projectId, requestData);

        // 스토어에 추가 - 필드 안의 주 기능으로 추가
        addMainFeatureToField(fieldName, {
          ...taskData,
          id: response.featureId // API 응답에서 featureId 사용
        });
      } else { // 상세 기능(secondSubTask) 추가
        console.log('상세 기능 추가 - parentTask:', modalState.parentTask);
        console.log('상세 기능 추가 - parentTask.id:', modalState.parentTask.id);
        console.log('상세 기능 추가 - taskData:', taskData);
        requestData = buildSubFeatureRequest(taskData, projectId, modalState.parentTask.id);
        console.log('상세 기능 추가 API 요청 데이터:', requestData);
        console.log('상세 기능 추가 API 요청 데이터 JSON:', JSON.stringify(requestData, null, 2));
        response = await addManualFeature(projectId, requestData);

        // 스토어에 추가
        addSubFeatureManually(modalState.parentTask.id, {
          ...taskData,
          id: response.featureId // API 응답에서 featureId 사용
        });
      }
      console.log('작업 추가 성공:', requestData);
      console.log('API 응답:', response);
      closeModal();
    } catch (error) {
      console.error('수동 기능 추가 실패:', error);
    }
  };

  // rawData가 있으면 콘솔에 출력하여 구조 확인
  useEffect(() => {
    if (rawData) {
      console.log('RequirementsSpecification - Raw data received:', rawData);
      // API 응답 데이터 처리
      processSpecData(rawData);
    }
  }, [rawData, processSpecData]);

  // mainFeatures가 업데이트되면 로컬 state도 업데이트
  useEffect(() => {
    console.log('mainFeatures 업데이트됨:', mainFeatures);
    if (mainFeatures && mainFeatures.length > 0) {
      setFeatures(mainFeatures);
    }
  }, [mainFeatures]);

  // 테스트용: 브라우저 콘솔에서 직접 호출할 수 있는 함수들
  useEffect(() => {
    // 테스트 데이터 처리 함수
    window.testSpecData = () => {
      const currentProjectId = projectId || 1; // 현재 프로젝트 ID 사용, 없으면 기본값
      const currentSpecId = specId || 1; // 현재 스펙 ID 사용, 없으면 기본값
      
      const testData = {
        projectId: currentProjectId,
        specId: currentSpecId,
        field: "배포 환경: AWS, Vercel, Netlify 등 클라우드 서비스",
        mainFeature: {
          id: 964,
          title: "결제 시스템 연동",
          description: "사용자가 안전하게 상품 결제할 수 있음",
          estimatedTime: 5,
          priorityLevel: null
        },
        subFeature: [
          {
            id: 965,
            title: "결제 시스템 연동",
            description: "사용자가 안전하게 상품을 결제할 수 있도록 PG사 API 또는 결제 플랫폼 연동 구현",
            estimatedTime: 3,
            priorityLevel: 2
          },
          {
            id: 966,
            title: "배포 환경 구축",
            description: "개발 완료된 플랫폼을 AWS, Vercel, 또는 Netlify와 같은 클라우드 서비스에 배포하여 안정적 운영 환경 마련",
            estimatedTime: 2,
            priorityLevel: 4
          }
        ]
      };
      processSpecData(testData);
      console.log('테스트 데이터 처리 완료:', testData);
    };

    // 프로젝트 요약 정보 테스트
    window.testProjectSummary = () => {
      const currentProjectId = projectId || 1; // 현재 프로젝트 ID 사용, 없으면 기본값
      const currentSpecId = specId || 1; // 현재 스펙 ID 사용, 없으면 기본값
      
      const testSummary = {
        projectTitle: "온라인 쇼핑몰 플랫폼 개발",
        specTitle: "온라인 쇼핑몰 플랫폼 명세서",
        projectDescription: "사용자들이 온라인으로 상품을 구매할 수 있는 쇼핑몰 웹사이트를 개발하는 프로젝트입니다.",
        projectId: currentProjectId,
        specId: currentSpecId
      };
      processSpecData(testSummary);
      console.log('프로젝트 요약 정보 테스트 완료:', testSummary);
    };

    // 상세 기능 추가 테스트
    window.testSubFeature = () => {
      const testSubFeature = {
        parentFeatureId: 964,
        featureSaveItem: {
          id: 967,
          title: "포인트 사용 선택 인터페이스 표시",
          description: "사용자가 결제 시 포인트를 사용할 수 있도록 선택할 수 있는 옵션을 화면에 표시",
          estimatedTime: 2,
          priorityLevel: 7
        }
      };
      processSpecData(testSubFeature);
      console.log('상세 기능 추가 테스트 완료:', testSubFeature);
    };

    // 명세서 초기화 테스트
    window.resetSpec = () => {
      resetSpecification();
      console.log('명세서 초기화 완료');
    };

    // 현재 상태 출력
    window.printSpec = () => {
      debugPrintSpecification();
    };
    
    return () => {
      delete window.testSpecData;
      delete window.testProjectSummary;
      delete window.testSubFeature;
      delete window.resetSpec;
      delete window.printSpec;
    };
  }, [processSpecData, resetSpecification, debugPrintSpecification]);


  // 모달 열기/닫기
  const openModal = (taskType, parentTask = null) => {
    console.log('openModal 호출됨:', { taskType, parentTask });
    setModalState({
      isOpen: true,
      taskType,
      parentTask
    });
  }

  const closeModal = () => {
    setModalState({
      isOpen: false,
      taskType: 'main', // 기본값
      parentTask: null
    });
  }
  // 열림/닫힘 상태를 토글하는 함수
  const handleToggleOpen = useCallback((taskId) => {
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
    setFeatures(prevFeatures => toggleOpen(prevFeatures));
  }, []);

  // 체크 상태를 토글하는 함수
  const handleToggleChecked = useCallback((taskId) => {
    // console.log('토글 호출, taskId:', taskId);
    let newState;

    const toggleAndPropagate = list =>
      list.map(item => {
        if (item.id === taskId) {
          const newChecked = !item.checked;
          return {
            ...item,
            checked: newChecked,
            subTasks: item.subTasks?.map(st => ({ ...st, checked: newChecked, subTasks: st.subTasks ? /* 재귀 */ [] : [] })) ?? []
          };
        }
        if (item.subTasks) {
          return { ...item, subTasks: toggleAndPropagate(item.subTasks) };
        }
        return item;
      });
  
    // 2) 부모 체크는 자식 전부 체크되어야만 true
    const updateParents = list =>
      list.map(item => {
        if (item.subTasks && item.subTasks.length) {
          const updatedSubs = updateParents(item.subTasks);
          const allChecked = updatedSubs.every(st => st.checked);
          return { ...item, subTasks: updatedSubs, checked: allChecked };
        }
        return item;
      });
  
    setFeatures(prev => {
      newState = toggleAndPropagate(prev);
      return updateParents(newState);
    });

  }, []);

   // 하위 작업 추가 핸들러
   const handleAddSubTask = (parentTask) => {
    // 이 함수는 "상세 기능 추가" 버튼을 클릭할 때만 호출됨
    // 따라서 항상 taskType은 'sub'여야 함
    const taskType = 'sub';
    console.log('handleAddSubTask - parentTask:', parentTask, 'taskType:', taskType);
    openModal(taskType, parentTask);
  };

  // 디버깅 버튼 클릭 핸들러
  const handleDebugPrint = () => {
    debugPrintSpecification();
  };

  // 초기화 버튼 클릭 핸들러
  const handleReset = () => {
    resetSpecification();
    setFeatures([]);
  };

  return (
    <div className={styles.requirementsSidebar}>
      <div className={styles.container}>
        {/* 헤더 */}
        <div className={styles.header}>
          <div className={styles.headerTitleContent}>
            <div className={styles.title}>요구사항 명세서</div>
          </div>
          <div style={{ display: 'flex', gap: '8px' }}>
            {/* 디버깅 버튼 */}
            <button 
              className={styles.pdfDownloadWrapper}
              onClick={handleDebugPrint}
              style={{ backgroundColor: '#007bff', color: 'white' }}
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M8 1V15M1 8H15" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
              <div className={styles.pdfText}>디버그</div>
            </button>
            
            {/* 초기화 버튼 */}
            <button 
              className={styles.pdfDownloadWrapper}
              onClick={handleReset}
              style={{ backgroundColor: '#dc3545', color: 'white' }}
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M8 3V1L3 6L8 11V9C11.866 9 15 12.134 15 16C15 12.134 11.866 9 8 9Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
              <div className={styles.pdfText}>초기화</div>
            </button>

            {/* PDF 다운로드 버튼 */}
            <button className={styles.pdfDownloadWrapper}>
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="25" viewBox="0 0 24 25" fill="none">
                <path d="M21 15.5V19.5C21 20.0304 20.7893 20.5391 20.4142 20.9142C20.0391 21.2893 19.5304 21.5 19 21.5H5C4.46957 21.5 3.96086 21.2893 3.58579 20.9142C3.21071 20.5391 3 20.0304 3 19.5V15.5M7 10.5L12 15.5M12 15.5L17 10.5M12 15.5V3.5" stroke="#1E1E1E" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
              <div className={styles.pdfText}>PDF 내보내기</div>
            </button>
          </div>
        </div>

        {/* 프로젝트 정보 표시 */}
        {(projectId || specId) && (
          <div className={styles.card}>
            <div className={styles.cardHeader}>
              <div className={styles.cardTitle}>프로젝트 정보</div>
            </div>
            <div className={styles.projectOverview}>
              <div className={styles.overviewItem}>
                <div className={styles.bullet}>•</div>
                <div className={styles.itemContent}>
                  <span className={styles.itemLabel}>Project ID: </span>
                  <span className={styles.itemValue}>{projectId || 'N/A'}</span>
                </div>
              </div>
              <div className={styles.overviewItem} style={{borderBottom: 'none'}}>
                <div className={styles.bullet}>•</div>
                <div className={styles.itemContent}>
                  <span className={styles.itemLabel}>Spec ID: </span>
                  <span className={styles.itemValue}>{specId || 'N/A'}</span>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* 프로젝트 개요 */}
        <div className={styles.card}>
          <div className={styles.cardHeader}>
            <div className={styles.cardTitle}>프로젝트 개요</div>
          </div>
          <div className={styles.projectOverview}>
            <div className={styles.overviewItem}>
              <div className={styles.bullet}>•</div>
              <div className={styles.itemContent}>
                <span className={styles.itemLabel}>프로젝트명: </span>
                <span className={styles.itemValue}>{projectOverview.projectName || 'N/A'}</span>
              </div>
            </div>
            <div className={styles.overviewItem} style={{borderBottom: 'none'}}>
              <div className={styles.bullet}>•</div>
              <div className={styles.itemContent}>
                <span className={styles.itemLabel} style={{marginBottom: '10px'}}>설명 </span>
                <span className={styles.itemValue}>{projectOverview.projectDescription || 'N/A'}</span>
              </div>
            </div>
          </div>
        </div>

        {/* 주요 기능 */}
        <div className={styles.card}>
          <div className={styles.mainFeaturesHeader}>
            <div className={styles.mainFeaturesTitle}>주요 기능</div>
          </div>
          <div className={styles.mainFeaturesList}>
            {features?.length > 0 ? (
              features.map(feature => (
                <TaskItem
                  key={feature.id}
                  task={feature}
                  onToggleOpen={handleToggleOpen}
                  onToggleChecked={handleToggleChecked}
                  onAddSubTask={handleAddSubTask}
                  // onOpenModal={(type, parentTask = null) => openModal(type, parentTask)}
                  onOpenModal={openModal}
                />
              ))
            ) : (
              <div style={{ padding: '20px', textAlign: 'center', color: '#6C757D' }}>
                아직 기능이 추가되지 않았습니다.
              </div>
            )}
            {/* 필드는 아직 수동 추가 기능 없음, 막기 */}
            {/* <AddNewTaskButton onClick={() => setIsAddTaskModalOpen(true)} /> */}
            {/* <AddNewTaskButton 
              onClick={() => openModal('main')} 
              text="주 기능 추가" 
            /> */}
          </div>
        </div>

        {/* 디버깅용 Raw Data 표시 */}
        {rawData && (
          <div className={styles.card}>
            <div className={styles.cardHeader}>
              <div className={styles.cardTitle}>🔍 Raw Data (디버깅용)</div>
            </div>
            <div className={styles.projectOverview}>
              <pre style={{ 
                fontSize: '12px', 
                backgroundColor: '#f5f5f5', 
                padding: '10px', 
                borderRadius: '4px',
                overflow: 'auto',
                maxHeight: '200px'
              }}>
                {JSON.stringify(rawData, null, 2)}
              </pre>
            </div>
          </div>
        )}
      </div>
      <AddTaskModal
        isOpen={modalState.isOpen}
        onClose={closeModal}
        onSubmit={handleAddTask}
        taskType={modalState.taskType}
        parentTask={modalState.parentTask}
      />
    </div>
  );
};

export default RequirementsSpecification;