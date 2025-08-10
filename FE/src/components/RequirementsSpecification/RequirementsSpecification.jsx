// FE/src/components/RequirementsSpecification/RequirementsSpecification.jsx
import React, { useState, useCallback, useEffect } from 'react';
import styles from './RequirementsSpecification.module.css';
import TechTag from './TechTag';
import Checkbox from './Checkbox';
import TimeIndicator from './TimeIndicator';
import PriorityBadge from './PriorityBadge';
import { useSpecificationStore } from '../../stores/specificationStore'; // 스토어 임포트


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
      <div className={styles.subTaskRight}>
        <div className={styles.subTaskActions}>
          <TimeIndicator hours={task.hours} />
        </div>
      </div>
    </div>
  );
};

const SubTaskItem = ({ task, onToggleOpen, onToggleChecked, level = 0 }) => {
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
const TaskItem = ({ task, onToggleOpen, onToggleChecked, level = 0 }) => {
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

              />
            ))}
            <div className={styles.addNewTaskSection}>
              <div className={styles.addNewTaskButton}>
                <div className={styles.addIconContainer}>
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M8 3.33325V12.6666" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round" />
                    <path d="M3.33594 8H12.6693" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round" />
                  </svg>
                </div>
                <div className={styles.addNewTaskText}>
                  <div className={styles.addNewTaskTextContent}>새 작업 추가</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};


const RequirementsSpecification = () => {
  const { projectOverview, mainFeatures, techStack } = useSpecificationStore();
  const tags = ['Python', 'FastAPI', 'RAG Pipeline', 'Vector DB', 'AWS EC2', 'AWS RDS', 'AWS S3'];
  const [requirements] = useState(initialRequirementsData);
  // const [features, setFeatures] = useState(initialRequirementsData[0].mainFeatures);

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


  return (
    <div className={styles.requirementsSidebar}>
      <div className={styles.container}>
        {/* 헤더 */}
        <div className={styles.header}>
          <div className={styles.headerTitleContent}>
            <div className={styles.title}>요구사항 명세서</div>
          </div>
          <button className={styles.pdfDownloadWrapper}>
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="25" viewBox="0 0 24 25" fill="none">
              <path d="M21 15.5V19.5C21 20.0304 20.7893 20.5391 20.4142 20.9142C20.0391 21.2893 19.5304 21.5 19 21.5H5C4.46957 21.5 3.96086 21.2893 3.58579 20.9142C3.21071 20.5391 3 20.0304 3 19.5V15.5M7 10.5L12 15.5M12 15.5L17 10.5M12 15.5V3.5" stroke="#1E1E1E" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
            <div className={styles.pdfText}>PDF 내보내기</div>
          </button>
        </div>

        {/* 예상 작업 완료일 */}
        <div className={styles.card}>
          <div className={styles.cardHeader}>
            <div className={styles.cardTitle}>📈 예상 작업 완료일 : {requirements[0].completionDate}</div>
          </div>
        </div>

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
                <span className={styles.itemValue}>{projectOverview.projectName}</span>
              </div>
            </div>
            <div className={styles.overviewItem}>
              <div className={styles.bullet}>•</div>
              <div className={styles.itemContent}>
                <span className={styles.itemLabel}>목적:</span>
                <span className={styles.itemValue}> {projectOverview.projectPurpose}</span>
              </div>
            </div>
            <div className={styles.descriptionContainer}>
              <div className={styles.descriptionHeader}>
                <div className={styles.bullet}>•</div>
                <div className={styles.itemLabel}>설명</div>
              </div>
              <div className={styles.descriptionText}>
                {projectOverview.projectDescription}
              </div>
            </div>
          </div>
        </div>

        {/* 기술 스택 */}
        <div className={styles.card}>
          <div className={styles.techStackHeader}>
            <div className={styles.techStackTitle}>
              <div className={styles.cardTitle}>기술 스택</div>
            </div>
            <div className={styles.addTechButton}>
              <div className={styles.addTechText}>기술 추가하기</div>
            </div>
          </div>
          <div className={styles.techTags}>
            {techStack.map((tag, index) => (
              <TechTag key={index} label={tag} />
            ))}
          </div>
        </div>

        {/* 주요 기능 */}
        <div className={styles.card}>
          <div className={styles.mainFeaturesHeader}>
            <div className={styles.mainFeaturesTitle}>주요 기능</div>
          </div>
          <div className={styles.mainFeaturesList}>
            {mainFeatures.map(feature => (
              <TaskItem
                key={feature.id}
                task={feature}
                onToggleOpen={handleToggleOpen}
                onToggleChecked={handleToggleChecked}
              />
            ))}
            <div className={styles.addNewTaskSection}>
              <div className={styles.addNewTaskButton}>
                <div className={styles.addIconContainer}>
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M8 3.3335V12.6668" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round" />
                    <path d="M3.33594 8H12.6693" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round" />
                  </svg>
                </div>
                <div className={styles.addNewTaskText}>
                  <div className={styles.addNewTaskTextContent}>새 작업 추가</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RequirementsSpecification;