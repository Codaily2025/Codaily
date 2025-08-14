// src/pages/ProjectCreate/ProjectCreateStep2.jsx
import React, { useEffect, useState, useRef } from 'react';
import './ProjectCreateStep2.css';
import { useNavigate, useSearchParams } from 'react-router-dom';

import { useChatStore } from '../../stores/chatStore';
import { useSpecificationStore } from '../../stores/specificationStore';
import { useChatHistoryQuery, useSendUserMessage } from '../../queries/useChat';
import { useGetRequirementsSpecification } from '../../queries/useRequirementsSpecification';
import { finalizeSpecification, toggleReduceFlag } from '../../apis/requirementsSpecification';

import ChatProgressBar from '../../components/ChatProgressBar/ChatProgressBar';
import ChatbotMessage from '../../components/ChatbotMessage/ChatbotMessage';
import ChatUserMessage from '../../components/ChatUserMessage/ChatUserMessage';
import ChatInputBar from '../../components/ChatInputBar/ChatInputBar';
import RequirementsSidebar from '../../components/RequirementsSpecification/RequirementsSpecification';

const ProjectCreateStep2 = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  // URL 파라미터에서 projectId와 specId 가져오기
  const projectId = searchParams.get('projectId');
  const specId = searchParams.get('specId');
  
  // zustand 스토어의 showSidebar 상태 가져오기
  const isSidebarVisible = useSpecificationStore((s) => s.isSidebarVisible);
  const isSpecPolling    = useSpecificationStore((s) => s.isSpecPolling);
  const startSpecPolling = useSpecificationStore((s) => s.startSpecPolling);
  const stopSpecPolling  = useSpecificationStore((s) => s.stopSpecPolling);
  const mainFeatures = useSpecificationStore((state) => state.mainFeatures);
  const toggleSidebar    = useSpecificationStore((s) => s.toggleSidebar);
  const hideSidebar      = useSpecificationStore((s) => s.closeSidebar);
  const showSidebarAction= useSpecificationStore((s) => s.openSidebar);

  const setFeatures = useSpecificationStore((state) => state.setFeatures);
  const setProjectInfo = useSpecificationStore((state) => state.setProjectInfo);
  const setProjectSummary = useSpecificationStore((state) => state.setProjectSummary);
  const hasSpecification = useSpecificationStore((s) => s.hasSpecification);
  const extendSpecPolling = useSpecificationStore(s => s.extendSpecPolling);

  const finalizeSpecificationStore = useSpecificationStore((state) => state.finalizeSpecification);
  
  // 사이드바 표시 조건: isSidebarVisible이 true이거나 mainFeatures가 있을 때
  const shouldShowSidebar = isSidebarVisible || (mainFeatures && mainFeatures.length > 0);
  const specExists = hasSpecification();

  // 디버깅을 위한 로그 추가
  useEffect(() => {
    console.log('isSidebarVisible 상태 변경:', isSidebarVisible);
    console.log('mainFeatures 상태:', mainFeatures);
    console.log('hasSpecification:', specExists);
  }, [projectId, specId, isSidebarVisible, mainFeatures, shouldShowSidebar, specExists]);

  console.log('ProjectCreateStep2 진입 - projectId:', projectId, 'specId:', specId);

  // 페이지 로드 시 사이드바를 명시적으로 숨김 (mainFeatures가 없을 때만)
  useEffect(() => {
    if (!mainFeatures || mainFeatures.length === 0) {
      hideSidebar();
    }
  }, [hideSidebar, mainFeatures]);

  // projectId나 specId가 없으면 이전 페이지로 이동
  useEffect(() => {
    if (!projectId || !specId) {
      console.error('projectId 또는 specId가 없습니다.');
      alert('프로젝트 정보가 올바르지 않습니다. 다시 시도해주세요.');
      navigate('/project/create');
    }
    return () => {
      // 페이지 떠날 때 폴링 중이면 정리
      try { stopSpecPolling(); } catch {}
    };
  }, [projectId, specId, navigate, stopSpecPolling]);

  // 채팅 기록 관리
  // Zustand에서 메세지 목록 가져오기
  const messages = useChatStore((state) => state.messages);
  const setMessages = useChatStore((state) => state.setMessages);

  // 백엔드 (또는 dummy data) 채팅 기록 로드
  const { data, isLoading, isError } = useChatHistoryQuery()

  useEffect(() => {
    if (data) {
      setMessages(data);
    }
  }, [data, setMessages]);

  // 메세지 추가될 때마다 스크롤 내리기
  useEffect(() => {
    const scrollArea = document.querySelector('.chat-scroll-area');
    if (scrollArea) {
      scrollArea.scrollTop = scrollArea.scrollHeight;
    }
  }, [messages]);

  // 사용자 메세지 전송 훅 - projectId와 specId 전달
  const sendUserMessage = useSendUserMessage(projectId, specId);

  const processRequirementsSpecification = (data) => {
    if (!data || !data.features) return [];
  
    const fieldMap = new Map();
  
    data.features.forEach((feature) => {
      const { field, isReduced, projectId, specId, mainFeature, subFeature, estimatedTime } = feature;
  
      if (!fieldMap.has(field)) {
        fieldMap.set(field, {
          field,
          isReduced: true, // 기본값을 true로 설정하고, 나중에 계산
          projectId,
          specId,
          estimatedTime,
          mainFeature: []
        });
      }
  
      const processedMainFeature = {
        ...mainFeature,
        subFeature: subFeature || []
      };
  
      fieldMap.get(field).mainFeature.push(processedMainFeature);
    });
  
    // 각 field의 isReduced 상태를 계산
    fieldMap.forEach((fieldData, fieldName) => {
      const allMainFeaturesReduced = fieldData.mainFeature.every(mf => mf.isReduced);
      fieldData.isReduced = allMainFeaturesReduced;
    });
  
    return Array.from(fieldMap.values());
  };

  const handleFinalizeSpecification = async () => {
    if (!specExists) {
      throw new Error('확정할 요구사항 명세서가 없습니다.');
    }

    console.log('=== 확정 처리 시작 ===');
    console.log('확정 전 최신 데이터 가져오기...');
    await refetchRequirementsSpecification();
    
    // 잠시 기다려서 상태 업데이트 완료 보장
    await new Promise(resolve => setTimeout(resolve, 500));

    const currentData = specData; // refetch 후 최신 specData 사용
    
    console.log('=== 최신 데이터 확인 ===');
    console.log('currentData:', currentData);
    console.log('currentData.features:', currentData?.features);
    console.log('currentData.features 길이:', currentData?.features?.length);
    
    if (!currentData || !currentData.features) {
      throw new Error('명세서 데이터가 없습니다.');
    }

    const checkedFeatureIds = [];
    const uncheckedFeatureIds = [];
    
    // 각 feature 분석
    currentData.features.forEach((feature, index) => {
      console.log(`=== Feature ${index} 분석 ===`);
      console.log('feature.field:', feature.field);
      console.log('feature.mainFeature:', feature.mainFeature);
      console.log('feature.subFeature:', feature.subFeature);
      
      const { mainFeature, subFeature } = feature;
      
      // mainFeature 확인
      if (mainFeature) {
        console.log(`MainFeature: ${mainFeature.title} (${mainFeature.id}) - isReduced: ${mainFeature.isReduced}`);
        if (mainFeature.isReduced === true) {
          uncheckedFeatureIds.push(mainFeature.id);
        } else if (mainFeature.isReduced === false) {
          checkedFeatureIds.push(mainFeature.id);
        }
      }
      
      // subFeature 확인
      if (subFeature && Array.isArray(subFeature) && subFeature.length > 0) {
        subFeature.forEach((sub, subIndex) => {
          console.log(`  SubFeature: ${sub.title} (${sub.id}) - isReduced: ${sub.isReduced}`);
          if (sub.isReduced === true) {
            uncheckedFeatureIds.push(sub.id);
          } else if (sub.isReduced === false) {
            checkedFeatureIds.push(sub.id);
          }
        });
      }
    });
    
    console.log('=== 최종 결과 ===');
    console.log('체크된 기능 ID들 (isReduced=false):', checkedFeatureIds);
    console.log('체크 해제된 기능 ID들 (isReduced=true):', uncheckedFeatureIds);
    console.log('체크된 기능 개수:', checkedFeatureIds.length);
    
    if (checkedFeatureIds.length === 0) {
      console.error('=== 오류: 선택된 기능이 없음 ===');
      throw new Error('선택된 기능이 없습니다. 최소 하나 이상의 기능을 선택해주세요.');
    }

    try {
      console.log('=== 현재 상태 그대로 확정 API 호출 ===');
      console.log(`체크된 기능 ${checkedFeatureIds.length}개, 해제된 기능 ${uncheckedFeatureIds.length}개로 확정 진행`);
      
      await finalizeSpecification(projectId);
      finalizeSpecificationStore();
      
      console.log('요구사항 명세서 확정 완료 ✅');
      console.log('=== 확정 처리 완료 ===');
      
    } catch (error) {
      console.error('확정 처리 중 오류 발생 ❌:', error);
      throw error;
    }
  };

  // 다음으로 버튼 클릭 핸들러
  const handleNextClick = async () => {
    // 요구사항 명세서가 없으면 클릭 무시
    if (!specExists) {
      return;
    }
    
    try {
      // 확정 처리 실행
      await handleFinalizeSpecification();
      
    // 확정 성공 시 다음 단계로 이동
    // 수정자: yeongenn - 요구사항 명세서 받은 후 github 연동 페이지로 네비게이션
    navigate(`/project/create/step4?projectId=${projectId}&specId=${specId}`)
    
  } catch (error) {
    console.error('요구사항 명세서 확정 실패:', error);
    alert(error.message || '요구사항 명세서 확정 중 오류가 발생했습니다. 다시 시도해주세요.');
  }
};

  const {
    data: specData,
    isLoading: specLoading,
    isError: specError,
    refetch: refetchRequirementsSpecification,
  } = useGetRequirementsSpecification(projectId,
    {
      // 사이드바가 아직 안 보일 때만 Step2가 폴링 담당
      polling: isSpecPolling && !shouldShowSidebar,
      intervalMs: 1800 + Math.floor(Math.random() * 600), // 1.8초~2.4초 간격
    }
  );

  // 가공된 데이터
  const refinedFeaturesStructure = processRequirementsSpecification(specData);
  // requirementsSpecification은 specData와 동일하게 설정
  const requirementsSpecification = specData;

  // 우선순위 변환 (스토어 함수와 동일 로직의 지역 헬퍼)
  const convertNumberToPriority = (priorityLevel) => {
    if (priorityLevel === null || priorityLevel === undefined) return 'Normal';
    if (priorityLevel < 3) return 'High';
    if (priorityLevel < 7) return 'Normal';
    return 'Low';
  };

  // API 응답을 스토어 mainFeatures 형태로 변환
  const mapSpecToStoreFeatures = (spec) => {
    if (!spec?.features || spec.features.length === 0) return [];
    const fieldMap = new Map();

    for (const item of spec.features) {
      const field = item.field;
      if (!fieldMap.has(field)) {
        fieldMap.set(field, {
          id: `field_${field}`,
          name: field,
          description: field,
          hours: 0,
          priority: convertNumberToPriority(item?.mainFeature?.priorityLevel),
          isOpen: false,
          checked: !item.isReduced,
          subTasks: [],
        });
      }

      const subTasks = (item.subFeature || []).map((sub) => ({
        id: sub.id,
        name: sub.title,
        description: sub.description,
        hours: sub.estimatedTime || 0,
        priority: convertNumberToPriority(sub.priorityLevel),
        checked: !sub.isReduced,
        isOpen: false,
        subTasks: [],
      }));

      const main = item.mainFeature;
      const mainFeature = {
        id: main.id,
        name: main.title,
        description: main.description,
        hours: main.estimatedTime || 0,
        priority: convertNumberToPriority(main.priorityLevel),
        checked: !main.isReduced,
        isOpen: false,
        subTasks,
      };

      const fieldObj = fieldMap.get(field);
      fieldObj.subTasks.push(mainFeature);
      fieldObj.hours += (main.estimatedTime || 0) + subTasks.reduce((s, t) => s + (t.hours || 0), 0);

      // field 체크 상태 보정(하위에 하나라도 체크면 true 유지)
      if (mainFeature.checked || subTasks.some((t) => t.checked)) {
        fieldObj.checked = true;
      }
    }

    return Array.from(fieldMap.values());
  };

  // StrictMode의 중복 실행 방지
  const hydratedRef = useRef(null);

  useEffect(() => {
    if (specLoading) return;
    if (specError) return;
    if (!specData?.features) return;
    if (!projectId) return;

    // 같은 projectId로 이미 하이드레이트했으면 스킵
    if (hydratedRef.current === projectId) return;

    const features = mapSpecToStoreFeatures(specData);
    if (features.length > 0) {
      // 스토어 정보 세팅
      setFeatures(features);

      // 프로젝트 요약/ID도 스토어에 세팅 (있으면)
      if (specData.project) {
        setProjectSummary({
          projectTitle: specData.project.projectTitle,
          specTitle: specData.project.specTitle,
          projectDescription: specData.project.projectDescription,
          projectPurpose: specData.project.projectPurpose,
          projectId: specData.project.projectId,
          specId: specData.project.specId,
        });
      } else {
        // 요약이 없으면 최소 ID라도
        setProjectInfo(Number(projectId), Number(specId));
      }

      // 사이드바 켜기
      showSidebarAction();
      hydratedRef.current = projectId;
    }
  }, [
    specLoading,
    specError,
    specData,
    projectId,
    specId,
    setFeatures,
    setProjectInfo,
    setProjectSummary,
    showSidebarAction,
  ]);

  // projectId나 specId가 없으면 로딩 표시
  if (!projectId || !specId) {
    return <div className="loading-message">프로젝트 정보를 불러오는 중입니다...</div>;
  }

  return (
    <div className="chat-page-container">
      {/* 스텝바 */}
      <ChatProgressBar currentStep={1} />

      {/* 채팅 영역 */}
      <div className="main-content-container">
        <div className={`chat-main-content`}>
          <div className={`chat-window-container ${(shouldShowSidebar) ? 'split-view' : ''}`}>
            <div className="chat-window">
              <div className="chat-scroll-area">
                {messages.map((message) => (
                  message.sender === 'bot' ? (
                    <ChatbotMessage
                      key={message.id}
                      isSmall={shouldShowSidebar ? true : false}>
                      {message.text}
                    </ChatbotMessage>
                  ) : (
                    <ChatUserMessage
                      key={message.id}
                      isSmall={shouldShowSidebar ? true : false}>
                      {message.text}
                    </ChatUserMessage>
                  )
                ))}
              </div>
            </div>
          </div>

          {/* 채팅 입력창 */}
          <ChatInputBar
            onSend={text => sendUserMessage.mutate(text)} // 사용자 메세지 전송 함수 호출
            isSending={sendUserMessage.isLoading} // 메세지 전송 중인지 표시
            isSplitView={shouldShowSidebar} // 분할 화면 상태 전달
          />
          {isLoading && <div className="loading-message">채팅 기록을 불러오는 중입니다...</div>}

          {isError && <div className="error-message">채팅 기록을 불러오는 중 오류가 발생했습니다.</div>}
          {/* 요구사항 명세서 영역 (분할 화면일 때만 표시) */}

        </div>
        {shouldShowSidebar && (
          <RequirementsSidebar />
        )}
      </div>

      {/* 이전/다음으로 네비게이션 버튼 */}
      <div className="nav-buttons-container">
        <button
          className="nav-button prev-button"
          onClick={() => navigate('/project/create')}>
          이전으로
        </button>
        <button
          className={`nav-button next-button ${!specExists ? 'disabled' : ''}`}
          onClick={handleNextClick}
          disabled={!specExists}>
          다음으로
        </button>
      </div>
    </div>
  );
};

export default ProjectCreateStep2;