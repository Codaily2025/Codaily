// src/pages/ProjectCreate/ProjectCreateStep2.jsx
import React, { useEffect, useState } from 'react';
import './ProjectCreateStep2.css';
import { useNavigate, useSearchParams } from 'react-router-dom';

import { useChatStore } from '../../stores/chatStore';
import { useSpecificationStore } from '../../stores/specificationStore';
import { useChatHistoryQuery, useSendUserMessage } from '../../queries/useChat';

import ChatProgressBar from '../../components/ChatProgressBar/ChatProgressBar';
import ChatbotMessage from '../../components/ChatbotMessage/ChatbotMessage';
import ChatUserMessage from '../../components/ChatUserMessage/ChatUserMessage';
import ChatInputBar from '../../components/ChatInputBar/ChatInputBar';
import RequirementsSidebar from '../../components/RequirementsSpecification/RequirementsSpecification';

const ProjectCreateStep2 = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [isSplitView, setIsSplitView] = useState(false); // 분할 화면 상태
  
  // 요구사항 명세서 사이드바 표시 상태
  const showSidebar = useSpecificationStore((state) => state.showSidebar);
  
  // URL 파라미터에서 projectId와 specId 가져오기
  const projectId = searchParams.get('projectId');
  const specId = searchParams.get('specId');
  
  console.log('ProjectCreateStep2 진입 - projectId:', projectId, 'specId:', specId);
  
  // projectId나 specId가 없으면 이전 페이지로 이동
  useEffect(() => {
    if (!projectId || !specId) {
      console.error('projectId 또는 specId가 없습니다.');
      alert('프로젝트 정보가 올바르지 않습니다. 다시 시도해주세요.');
      navigate('/project/create');
    }
  }, [projectId, specId, navigate]);
  
  // 채팅 기록 관리
  // Zustand에서 메세지 목록 가져오기
  const messages = useChatStore((state) => state.messages);
  const setMessages = useChatStore((state) => state.setMessages);

  // 백엔드 (또는 dummy data) 채팅 기록 로드
  const { data, isLoading, isError } = useChatHistoryQuery()
  // const { isLoading: isHistoryLoading, isError, error } = useChatHistoryQuery();
  // 디버깅을 위한 로그 추가
  // console.log('ProjectCreateStep2 - useChatHistoryQuery 결과:', {
  //   data,
  //   messages
  // });

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

  // 다음으로 버튼 클릭 핸들러
  const handleNextClick = () => {
    setIsSplitView(true); // 수동으로 분할 화면 활성화
  };

  // projectId나 specId가 없으면 로딩 표시
  if (!projectId || !specId) {
    return <div className="loading-message">프로젝트 정보를 불러오는 중입니다...</div>;
  }

  return (
    <div className="chat-page-container">
      {/* 스텝바 */}
      <ChatProgressBar />

      {/* 채팅 영역 */}
      <div className="main-content-container">
      <div className={`chat-main-content`}>
        <div className={`chat-window-container ${(isSplitView || showSidebar) ? 'split-view' : ''}`}>
          <div className="chat-window">
            <div className="chat-scroll-area">
              {messages.map((message) => (
                message.sender === 'bot' ? (
                  <ChatbotMessage
                    key={message.id}
                    isSmall={(isSplitView || showSidebar) ? true : false}>
                    {message.text}
                  </ChatbotMessage>
                ) : (
                  <ChatUserMessage
                    key={message.id}
                    isSmall={(isSplitView || showSidebar) ? true : false}>
                    {message.text}
                  </ChatUserMessage>
                )
              ))}
            </div>
          </div>
          
        </div>

        {/* 채팅 입력창 */}
        {/* <ChatInputBar /> */}
        <ChatInputBar
          onSend={text => sendUserMessage.mutate(text)} // 사용자 메세지 전송 함수 호출
          isSending={sendUserMessage.isLoading} // 메세지 전송 중인지 표시
          isSplitView={(isSplitView || showSidebar)} // 분할 화면 상태 전달
        />
        {isLoading && <div className="loading-message">채팅 기록을 불러오는 중입니다...</div>}

        {isError && <div className="error-message">채팅 기록을 불러오는 중 오류가 발생했습니다.</div>}
        {/* 요구사항 명세서 영역 (분할 화면일 때만 표시) */}
        
      </div>
      {(isSplitView || showSidebar) && (
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
          className="nav-button next-button"
          onClick={handleNextClick}>
          다음으로
        </button>
      </div>
    </div>
  );
};

export default ProjectCreateStep2;
