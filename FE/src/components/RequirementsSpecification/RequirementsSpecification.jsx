// FE/src/components/RequirementsSpecification/RequirementsSpecification.jsx
import React, { useState, useCallback, useEffect } from 'react';
import styles from './RequirementsSpecification.module.css';
import TechTag from './TechTag';
import Checkbox from './Checkbox';
import TimeIndicator, { getRoundedHours } from './TimeIndicator';
import PriorityBadge from './PriorityBadge';
import AddTaskModal from './AddTaskModal';
import { useSpecificationStore } from '../../stores/specificationStore'; // 스토어 임포트
import { addManualFeature, buildMainFeatureRequest, buildSubFeatureRequest, buildMainFeatureToFieldRequest } from '../../apis/chatApi';
import { downloadSpecDocument, toggleReduceFlag } from '../../apis/requirementsSpecification';
import { useGetRequirementsSpecification } from '../../queries/useRequirementsSpecification';
import { useSearchParams } from 'react-router-dom';

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

const ConvertPriorityNumberToString = (priorityLevel) => {
  if (priorityLevel < 4) return 'High';
  else if (priorityLevel > 3 && priorityLevel < 7) return 'Normal';
  else return 'Low';
}

const SecondSubTaskItem = ({ task, onToggleChecked, level = 0, parentId }) => {
  return (
    /* 클릭했을 때 상위 task의 드롭다운이 닫히면 안됨, 이벤트 막기 */
    <div className={styles.expandedSectionItem} onClick={(e) => {
      e.stopPropagation();
      // 카드 클릭 시 체크박스 토글
      // console.log('SecondSubTaskItem - 카드 클릭:', task.id, '현재 isReduced:', task.isReduced);
      onToggleChecked(task.id);
    }}>
      <div className={styles.subTaskLeft}>
        <Checkbox
          checked={!task.isReduced}
          onChange={(e) => {
            e.stopPropagation();
            console.log('SecondSubTaskItem - subFeature 체크박스 클릭:', task.id, '현재 isReduced:', task.isReduced);
            onToggleChecked(task.id);
          }} />
        <div className={styles.subTaskNameContainer}>
          <div className={styles.subTaskName}>{task.title}</div>
        </div>
        {task.priorityLevel && <PriorityBadge level={ConvertPriorityNumberToString(task.priorityLevel)} />}
      </div>
      <div className={styles.subTaskRight} style={{ marginRight: '12px' }}>
        <div className={styles.subTaskActions}>
          {task.estimatedTime && <TimeIndicator hours={task.estimatedTime} />}
        </div>
      </div>
    </div>
  );
};

const SubTaskItem = ({ task, onToggleChecked, onAddSubTask, level = 0 }) => {
  const [isOpen, setIsOpen] = useState(false); // 로컬 상태로 isOpen 관리
  // console.log('주 기능(mainFeature):', task)
  // SVG 아이콘 컴포넌트
  const ExpandIcon = ({ isOpen }) => (
    <img 
      className={styles.dropdownIcon} 
      src="/src/assets/caret_up.svg" 
      alt="caret" 
      style={{ 
        transform: isOpen ? 'rotate(180deg)' : 'rotate(0deg)', 
        transition: 'transform 0.2s ease' 
      }}
    />
  );

  const hasSecondSubTasks = task.subFeature && task.subFeature.length > 0;

  // 카드 클릭 시 열림/닫힘 토글
  const handleCardClick = () => {
    if (hasSecondSubTasks) {
      setIsOpen(!isOpen);
    }
  };

  return (
    <>
      {hasSecondSubTasks && isOpen ? (
        <div className={styles.expandedSection} onClick={handleCardClick}>
          <div className={styles.expandedSectionHeader}>
            <div className={styles.expandedSectionHeaderInner}>
              <div className={styles.subTaskLeft}>
                <Checkbox
                  checked={!task.isReduced}
                  onChange={(e) => {
                    e.stopPropagation();
                    console.log('SubTaskItem - mainFeature 체크박스 클릭 (expanded):', task.id, '현재 isReduced:', task.isReduced);
                    onToggleChecked(task.id);
                  }}
                />
                <div className={styles.subTaskNameContainer}>
                  <div className={styles.subTaskName}>{task.title}</div>
                </div>
                {task.priorityLevel && <PriorityBadge level={ConvertPriorityNumberToString(task.priorityLevel)} />}
              </div>
              <div className={styles.mainFeatureHeaderRight}>
                {task.estimatedTime && <TimeIndicator hours={task.estimatedTime} />}
                <ExpandIcon isOpen={isOpen} />
              </div>
            </div>
          </div>
          <div className={styles.expandedSectionBody}>
            <div className={styles.expandedSectionItems}>
              {task.subFeature.map(subTask => (
                <SecondSubTaskItem key={subTask.id} task={subTask} onToggleChecked={onToggleChecked} level={level + 1} parentId={task.id}
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
        <div className={styles.subTaskItem} onClick={handleCardClick}>
          <div className={styles.subTaskLeft}>
            <Checkbox
              checked={!task.isReduced}
              onChange={(e) => {
                e.stopPropagation();
                console.log('SubTaskItem - mainFeature 체크박스 클릭 (collapsed):', task.id, '현재 isReduced:', task.isReduced);
                onToggleChecked(task.id);
              }} />
            <div className={styles.subTaskNameContainer}>
              <div className={styles.subTaskName}>{task.title}</div>
            </div>
            {task.priorityLevel && <PriorityBadge level={ConvertPriorityNumberToString(task.priorityLevel)} />}
          </div>
          {hasSecondSubTasks ? (
            <div className={styles.mainFeatureHeaderRight}>
              {task.estimatedTime && <TimeIndicator hours={task.estimatedTime} />}
              <ExpandIcon isOpen={isOpen} />
            </div>
          ) : (
            <div className={styles.subTaskRight}>
              <div className={styles.subTaskActions}>
                {task.estimatedTime && <TimeIndicator hours={task.estimatedTime} />}
              </div>
            </div>
          )}
        </div>
      )}
    </>
  );
};

// 작업을 렌더링하는 컴포넌트
const TaskItem = ({ task, onToggleChecked, onAddSubTask, onOpenModal, level = 0 }) => {
  const [isOpen, setIsOpen] = useState(false); // 로컬 상태로 isOpen 관리
  const hasSubTasks = task.mainFeature && task.mainFeature.length > 0;
  
  // 주 기능들의 estimatedTime만 더해서 totalTime 계산
  const totalTime = hasSubTasks ? task.mainFeature.reduce((total, mainFeature) => {
    const mainFeatureTime = mainFeature.estimatedTime ? getRoundedHours(mainFeature.estimatedTime) : 0;
    return total + mainFeatureTime;
  }, 0) : 0;
  
  // console.log('필드:', task)

  // SVG 아이콘 컴포넌트
  const ExpandIcon = ({ isOpen }) => (
    <img 
      className={styles.dropdownIcon} 
      src="/src/assets/caret_up.svg" 
      alt="caret" 
      style={{ 
        transform: isOpen ? 'rotate(180deg)' : 'rotate(0deg)', 
        transition: 'transform 0.2s ease' 
      }}
    />
  );

  // 카드 클릭 시 열림/닫힘 토글
  const handleCardClick = () => {
    if (hasSubTasks) {
      setIsOpen(!isOpen);
    }
  };

  const priorityStringLevel = ConvertPriorityNumberToString(task.priorityLevel);

  return (
    <div className={level === 0 ? styles.mainFeatureCard : styles.subTaskItem}>
      <div className={styles.mainFeatureHeader} onClick={handleCardClick}>
        <div className={styles.mainFeatureHeaderLeft}>
          <Checkbox
            checked={!task.isReduced} // isReduced가 true면 체크 박스 해제
            onChange={(e) => {
              e.stopPropagation();
              // console.log('TaskItem - field 체크박스 클릭:', task.field, '현재 isReduced:', task.isReduced);
              // console.log('TaskItem - task 객체:', task);
              onToggleChecked(task.field);
            }} />
          <div className={styles.mainFeatureName}>{task.field}</div>
          {task.priorityLevel && <PriorityBadge level={priorityStringLevel} />}
        </div>
        <div className={styles.mainFeatureHeaderRight}>
          {totalTime > 0 && <TimeIndicator hours={totalTime} />}
          {hasSubTasks && <ExpandIcon isOpen={isOpen} />}
        </div>
      </div>
      {hasSubTasks && isOpen && (
        <div className={styles.mainFeatureContent}>
          <div className={styles.mainFeatureItems}>
            {task.mainFeature.map(mainFeature => (
              <SubTaskItem
                key={mainFeature.id}
                task={mainFeature}
                onToggleChecked={onToggleChecked}
                level={level + 1}
                parentId={task.field}
                onAddSubTask={onAddSubTask}
              />
            ))}
            {/* <AddNewTaskButton
              onClick={() => onOpenModal('main', task)}
              text="주 기능 추가"
            /> */}
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
    addMainFeatureToField,
    toggleFeatureChecked,
    toggleFeatureOpen,
    setProjectId,
    setSpecId
  } = useSpecificationStore();

  const [search] = useSearchParams();

  // if (!projectId) {
  //   return <div>프로젝트 ID가 없습니다.</div>;
  // }

  useEffect(() => {
    if (!projectId) {
      const pid = search.get('projectId');
      if (pid) setProjectId(Number(pid));
    }
    if (!specId) {
      const sid = search.get('specId');
      if (sid) setSpecId(Number(sid));
    }
  }, [projectId, specId, search, setProjectId, setSpecId]);

  // const { data: requirementsSpecification, isLoading: isLoadingRequirementsSpecification, isError: isErrorRequirementsSpecification, refetch: refetchRequirementsSpecification } = useGetRequirementsSpecification(projectId);
  // 요구사항 명세서가 만들어지기 전에는 데이터가 없음
  // console.log('GET으로 페이지에 가져온 요구사항 명세서 데이터:', requirementsSpecification);
  const isSpecPolling = useSpecificationStore(s => s.isSpecPolling);
  const {
    data: requirementsSpecification,
    isLoading: isLoadingRequirementsSpecification,
    isError: isErrorRequirementsSpecification,
    refetch: refetchRequirementsSpecification
  } = useGetRequirementsSpecification(projectId, {
    polling: isSpecPolling,
    intervalMs: 1800 + Math.floor(Math.random() * 600), // 1.8초~2.4초 간격
  });
  // 출력 결과 예시
  //   "project": {
  //     "projectTitle": "Codaily",
  //     "projectDescription": "AI 기반 명세/회고 플랫폼",
  //     "specTitle": "v1.0 기능 명세",
  //     "projectId": 3,
  //     "specId": 10
  //   },
  //   "features": [
  //     {
  //       "projectId": 3,
  //       "specId": 10,
  //       "field": "회원",
  //       "isReduced": false, // 필드(회원)의 체크 박스 선택 여부
  //       "mainFeature": {
  //         "id": 101,
  //         "isReduced": false, // 주 기능(회원 가입)의 체크 박스 선택 여부
  //         "title": "회원 가입",
  //         "description": "이메일/소셜 가입을 지원",
  //         "estimatedTime": 2.5,
  //         "priorityLevel": 1
  //       },
  //       "subFeature": [
  //         {
  //           "id": 201,
  //           "isReduced": false, // 상세 기능(이메일 인증)의 체크 박스 선택 여부
  //           "title": "이메일 인증",
  //           "description": "토큰 기반 인증",
  //           "estimatedTime": 0.5,
  //           "priorityLevel": 1
  //         },
  //         {
  //           "id": 202,
  //           "isReduced": false,
  //           "title": "소셜 로그인",
  //           "description": "카카오/네이버/구글 로그인 지원",
  //           "estimatedTime": 1,
  //           "priorityLevel": 2
  //         }
  //       ]
  //     },
  //     {
  //       "projectId": 3,
  //       "specId": 10,
  //       "field": "사용자 간 화상 채팅 및 오디오 통신",
  //       "isReduced": false,
  //       "mainFeature": {
  //         "id": 102,
  //         "isReduced": false,
  //         "title": "화상 채팅 시작",
  //         "description": "사용자가 실시간으로 화상 채팅을 시작할 수 있음",
  //         "estimatedTime": 3,
  //         "priorityLevel": 2
  //       },
  //       "subFeature": [
  //         {
  //           "id": 203,
  //           "isReduced": false,
  //           "title": "WebRTC 연결 요청 전송",
  //           "description": "사용자가 화상 채팅 시작 시 서버에 연결 요청을 전달",
  //           "estimatedTime": 1.5,
  //           "priorityLevel": 1
  //         },
  //         {
  //           "id": 204,
  //           "isReduced": false,
  //           "title": "화상 채팅 시작 버튼 활성화",
  //           "description": "사용자가 화상 채팅을 시작하도록 버튼을 클릭할 수 있게 함",
  //           "estimatedTime": 1,
  //           "priorityLevel": 2
  //         }
  //       ]
  //     },
  //     {
  //       "projectId": 3,
  //       "specId": 10,
  //       "field": "사용자 간 화상 채팅 및 오디오 통신",
  //       "isReduced": true,
  //       "mainFeature": {
  //         "id": 2496,
  //         "isReduced": false,
  //         "title": "화상 채팅 시작",
  //         "description": "사용자가 실시간으로 화상 채팅을 시작할 수 있음",
  //         "estimatedTime": 3,
  //         "priorityLevel": 2
  //       },
  //       ... 생략 ...
  //     }
  //   ]
  // }

  // 데이터 가공 로직
  const processRequirementsSpecification = (data) => {
    if (!data || !data.features) return [];

    // console.log('원본 API 데이터:', data);
    // console.log('원본 features:', data.features);

    const fieldMap = new Map();

    data.features.forEach((feature, index) => {
      // console.log(`처리 중인 feature ${index}:`, feature);
      const { field, isReduced, projectId, specId, mainFeature, subFeature, estimatedTime } = feature;

      if (!fieldMap.has(field)) {
        // 새로운 필드인 경우 초기화
        fieldMap.set(field, {
          field,
          isReduced: true, // 기본값을 true로 설정하고, 나중에 계산
          projectId,
          specId,
          estimatedTime,
          mainFeature: []
        });
      }

      // mainFeature를 배열에 추가하고 subFeature를 포함
      const processedMainFeature = {
        ...mainFeature,
        // estimatedTime: mainFeature.estimatedTime || 0,
        subFeature: subFeature || []
      };

      fieldMap.get(field).mainFeature.push(processedMainFeature);
    });

    // 각 field의 isReduced 상태를 계산
    fieldMap.forEach((fieldData, fieldName) => {
      // field 내의 모든 mainFeature가 isReduced=true이면 field도 isReduced=true
      // 하나라도 isReduced=false가 있으면 field도 isReduced=false
      const allMainFeaturesReduced = fieldData.mainFeature.every(mf => mf.isReduced);
      fieldData.isReduced = allMainFeaturesReduced;

      // console.log(`Field "${fieldName}" 계산 결과:`, {
      //   mainFeatures: fieldData.mainFeature.map(mf => ({ id: mf.id, isReduced: mf.isReduced })),
      //   fieldIsReduced: fieldData.isReduced
      // });
    });

    const result = Array.from(fieldMap.values());
    // console.log('가공된 데이터 구조:', result);
    return result;
  };

  // 원데이터
  console.log('원데이터:', requirementsSpecification);
  // 가공된 데이터
  const refinedFeaturesStructure = processRequirementsSpecification(requirementsSpecification);
  console.log('가공된 요구사항 명세서 주요 기능 데이터:', refinedFeaturesStructure);


  const [specDocument, setSpecDocument] = useState(null); // pdf 다운로드 받기 위한 변수

  // 파일명 추출 
  function getFileName(headerValue) {
    if (!headerValue) return null;
    const match = /filename\*?=(?:UTF-8'')?["']?([^"';]+)["']?/i.exec(headerValue);
    return match ? decodeURIComponent(match[1]) : null;
  }
  // pdf 다운로드 받기 함수
  const handleDownloadSpecDocument = async () => {
    try {
      const response = await downloadSpecDocument(projectId);

      // 서버가 에러를 JSON으로 보낸 경우(예: 200인데 실제는 에러 바디)
      const ct = response.headers['content-type'] || '';
      if (ct.includes('application/json')) {
        const text = await response.data.text?.(); // blob → text
        console.error('서버 에러 바디:', text);
        alert('문서 생성 중 오류가 발생했습니다.');
        return;
      }

      const blob = new Blob([response.data], { type: ct || 'application/pdf' });
      setSpecDocument(blob);

      const url = URL.createObjectURL(blob);

      const a = document.createElement('a');
      a.href = url;
      const cd = response.headers['content-disposition'];
      a.download = getFileName(cd) || `specification_${projectId}.pdf`;

      document.body.appendChild(a);
      a.click();
      a.remove();

      URL.revokeObjectURL(url);
      console.log('프로젝트 요구사항 명세서 다운로드 성공');
    } catch (error) {
      console.error('프로젝트 요구사항 명세서 다운로드 실패:', error);
    }
  };

  const tags = ['Python', 'FastAPI', 'RAG Pipeline', 'Vector DB', 'AWS EC2', 'AWS RDS', 'AWS S3'];
  const [requirements] = useState(initialRequirementsData);
  // mainFeatures가 있으면 사용하고, 없으면 초기 데이터 사용
  const features = refinedFeaturesStructure && refinedFeaturesStructure.length > 0 ? refinedFeaturesStructure : initialRequirementsData[0].mainFeatures;

  // 모달 상태 관리 -> 상세 기능 추가 모달 열기 위해 필요
  const [modalState, setModalState] = useState({
    isOpen: false,
    taskType: null,
    parentTask: null
  });

  const extendSpecPolling = useSpecificationStore(s => s.extendSpecPolling);
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
        const fieldName = modalState.parentTask ? modalState.parentTask.field : 'Custom Feature';
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
      extendSpecPolling(10000); // 10초 연장
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
  }, [mainFeatures]);

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

  const handleToggleChecked = useCallback(async (taskId) => {
    console.log('=== 체크박스 토글 시작 ===');
    console.log('토글 호출, taskId:', taskId);
   
    if (!projectId) {
      console.error('프로젝트 ID가 없습니다.');
      return;
    }
   
    // task 찾기
    const findTask = (features, targetId) => {
      for (const field of features) {
        if (field.field === targetId) {
          return { 
            task: field, 
            level: 'field', 
            fieldData: field 
          };
        }
        
        if (field.mainFeature) {
          for (const mainFeature of field.mainFeature) {
            if (mainFeature.id === targetId) {
              return { 
                task: mainFeature, 
                level: 'mainFeature', 
                fieldData: field,
                mainFeatureData: mainFeature
              };
            }
            
            if (mainFeature.subFeature) {
              for (const subFeature of mainFeature.subFeature) {
                if (subFeature.id === targetId) {
                  return { 
                    task: subFeature, 
                    level: 'subFeature', 
                    fieldData: field,
                    mainFeatureData: mainFeature,
                    subFeatureData: subFeature
                  };
                }
              }
            }
          }
        }
      }
      return null;
    };
   
    const result = findTask(refinedFeaturesStructure, taskId);
    if (!result) {
      console.error('Task를 찾을 수 없습니다:', taskId);
      return;
    }
   
    const { task: currentTask, level, fieldData, mainFeatureData } = result;
    console.log('찾은 task:', currentTask);
    console.log('현재 isReduced 상태:', currentTask.isReduced);
   
    const newIsReduced = !currentTask.isReduced;
   
    try {
      const apiCalls = [];
   
      if (level === 'field') {
        const field = currentTask.field;
        console.log('필드 토글 - field:', field, 'newIsReduced:', newIsReduced);
   
        // 필드 토글
        apiCalls.push(toggleReduceFlag(projectId, field, null, newIsReduced));
   
        // 필드가 해제되면 모든 하위 항목도 해제
        if (newIsReduced) {
          if (fieldData.mainFeature) {
            for (const mainFeature of fieldData.mainFeature) {
              if (!mainFeature.isReduced) {
                apiCalls.push(toggleReduceFlag(projectId, null, mainFeature.id, true, true));
              }
            }
          }
        } else {
          // 필드가 선택되면 모든 하위 항목도 선택
          if (fieldData.mainFeature) {
            for (const mainFeature of fieldData.mainFeature) {
              if (mainFeature.isReduced) {
                apiCalls.push(toggleReduceFlag(projectId, null, mainFeature.id, false, true));
              }
            }
          }
        }
   
      } else if (level === 'mainFeature') {
        const featureId = taskId;
        console.log('주 기능 토글 - featureId:', featureId, 'newIsReduced:', newIsReduced);
   
        // 주 기능을 cascade=true로 토글 (상세기능들도 함께 변경)
        apiCalls.push(toggleReduceFlag(projectId, null, featureId, newIsReduced, true));
   
        // 상위 필드 상태 조정
        const shouldFieldBeChecked = !newIsReduced ||
          (fieldData.mainFeature && fieldData.mainFeature.some(mf =>
            mf.id !== featureId && !mf.isReduced
          ));
   
        if (fieldData.isReduced !== !shouldFieldBeChecked) {
          apiCalls.push(toggleReduceFlag(projectId, fieldData.field, null, !shouldFieldBeChecked));
        }
   
      } else if (level === 'subFeature') {
        const featureId = taskId;
        console.log('상세 기능 토글 - featureId:', featureId, 'newIsReduced:', newIsReduced);
   
        // 상세 기능 토글 (cascade=false, 개별 토글)
        apiCalls.push(toggleReduceFlag(projectId, null, featureId, newIsReduced, false));
   
        // 상세 기능 상태에 따라 상위 주 기능 상태 조정
        const shouldMainFeatureBeChecked = !newIsReduced ||
          (mainFeatureData.subFeature && mainFeatureData.subFeature.some(sf =>
            sf.id !== featureId && !sf.isReduced
          ));
   
        // 부모 주기능 상태 조정
        if (mainFeatureData.isReduced !== !shouldMainFeatureBeChecked) {
          apiCalls.push(toggleReduceFlag(projectId, null, mainFeatureData.id, !shouldMainFeatureBeChecked, false));
        }
   
        // 주 기능 상태에 따라 상위 필드 상태 조정
        const shouldFieldBeChecked = shouldMainFeatureBeChecked ||
          (fieldData.mainFeature && fieldData.mainFeature.some(mf =>
            mf.id !== mainFeatureData.id && !mf.isReduced
          ));
   
        // 필드 상태 조정
        if (fieldData.isReduced !== !shouldFieldBeChecked) {
          apiCalls.push(toggleReduceFlag(projectId, fieldData.field, null, !shouldFieldBeChecked));
        }
      }
   
      if (apiCalls.length > 0) {
        console.log(`실제 필요한 API 호출 개수: ${apiCalls.length}`);
        await Promise.all(apiCalls);
        console.log('필요한 API 호출만 완료');
   
        // API 호출이 있었으면 데이터 새로고침
        await refetchRequirementsSpecification();
        console.log('데이터 새로고침 완료');
      } else {
        console.log('변경이 필요한 항목이 없어 API 호출 생략');
      }
   
    } catch (error) {
      console.error('체크박스 토글 API 호출 실패:', error);
      alert('체크박스 상태 변경에 실패했습니다. 다시 시도해주세요.');
      
      try {
        await refetchRequirementsSpecification();
      } catch (refreshError) {
        console.error('데이터 새로고침도 실패:', refreshError);
      }
    }
    
    extendSpecPolling(6000);
    console.log('=== 체크박스 토글 완료 ===');
   
   }, [refinedFeaturesStructure, projectId, refetchRequirementsSpecification]);
  const handleAddSubTask = (parentTask) => {
    // 이 함수는 "상세 기능 추가" 버튼을 클릭할 때만 호출됨
    // 따라서 항상 taskType은 'sub'여야 함
    const taskType = 'sub';
    console.log('handleAddSubTask - parentTask:', parentTask, 'taskType:', taskType);
    console.log('handleAddSubTask - parentTask keys:', Object.keys(parentTask));
    console.log('handleAddSubTask - parentTask.name:', parentTask.name);
    console.log('handleAddSubTask - parentTask.title:', parentTask.title);
    openModal(taskType, parentTask);
  };

  // 디버깅 버튼 클릭 핸들러
  // const handleDebugPrint = () => {
  //   debugPrintSpecification();
  // };

  // 초기화 버튼 클릭 핸들러
  // const handleReset = () => {
  //   resetSpecification();
  // };

  // // 새로고침 버튼 클릭 핸들러
  // const handleRefresh = async () => {
  //   try {
  //     console.log('명세서 정보 새로고침 시작...');
  //     await refetchRequirementsSpecification();
  //     console.log('명세서 정보 새로고침 완료');
  //   } catch (error) {
  //     console.error('명세서 정보 새로고침 실패:', error);
  //   }
  // };

  return (
    <div className={styles.requirementsSidebar}>
      <div className={styles.container}>
        {/* 헤더 */}
        <div className={styles.header}>
          <div className={styles.headerTitleContent}>
            <div className={styles.title}>요구사항 명세서</div>
          </div>
          <div style={{ display: 'flex', gap: '8px' }}>
            {/* 새로고침 버튼 */}
            {/* <button
              className={styles.pdfDownloadWrapper}
              onClick={handleRefresh}
              style={{ backgroundColor: '#28a745', color: 'white' }}
              disabled={isLoadingRequirementsSpecification}
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M8 3V1L3 6L8 11V9C11.866 9 15 12.134 15 16C15 12.134 11.866 9 8 9Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
              <div className={styles.pdfText}>
                {isLoadingRequirementsSpecification ? '로딩중...' : '새로고침'}
              </div>
            </button> */}

            {/* 디버깅 버튼 */}
            {/* <button
              className={styles.pdfDownloadWrapper}
              onClick={handleDebugPrint}
              style={{ backgroundColor: '#007bff', color: 'white' }}
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M8 1V15M1 8H15" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
              <div className={styles.pdfText}>디버그</div>
            </button> */}

            {/* 초기화 버튼 */}
            {/* <button
              className={styles.pdfDownloadWrapper}
              onClick={handleReset}
              style={{ backgroundColor: '#dc3545', color: 'white' }}
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M8 3V1L3 6L8 11V9C11.866 9 15 12.134 15 16C15 12.134 11.866 9 8 9Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
              <div className={styles.pdfText}>초기화</div>
            </button> */}

            {/* PDF 다운로드 버튼 */}
            <button className={styles.pdfDownloadWrapper} onClick={handleDownloadSpecDocument}>
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
              <div className={styles.overviewItem} style={{ borderBottom: 'none' }}>
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
                <span className={styles.itemValue}>{requirementsSpecification?.project?.projectTitle || '값을 불러오지 못했어요'}</span>
              </div>
            </div>
            <div className={styles.overviewItem} style={{ borderBottom: 'none' }}>
              <div className={styles.bullet}>•</div>
              <div className={styles.itemContent}>
                <span className={styles.itemLabel} style={{ marginBottom: '10px' }}>설명 </span>
                <span className={styles.itemValue}>{requirementsSpecification?.project?.projectDescription || '값을 불러오지 못했어요'}</span>
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
            {refinedFeaturesStructure && refinedFeaturesStructure.length > 0 ? (
              refinedFeaturesStructure.map(feature => (
                <TaskItem
                  key={feature.field} // 필드는 id 값이 없음, 필드 이름으로 구분
                  task={feature}
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