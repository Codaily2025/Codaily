// src/queries/useChat.js
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
// React Query 훅
// useQuery의 역할 : 데이터 조회
// fetchChatHistory 함수 호출해 채팅 기록을 가져오고 내부 캐시에 저장
// useMutation의 역할 : 데이터 변경
// 사용자의 메세지를 postUserMessage 함수 호출해 백엔드로 전송
// API 호출 전 optimistic update로 사용자 메세지 먼저 추가(onMutate)
// optimistic update: 데이터 변경 전 미리 화면에 반영하는 기법
// 필요한 이유 : 사용자 메세지 입력 후 백엔드 응답 대기 중에도 메세지 화면에 표시되어 사용자 경험 향상
// 백엔드 응답을 받아 채팅 기록에 추가(onSuccess)
// 성공했던 실패했던 React Query 캐시 무효화(onSettled)
// useQueryClient의 역할 : 데이터 캐시 관리
// 전역 캐시 인스턴스를 가져와 훅 내부에서 invalidateQueries(역할: 캐시 무효화) 같은 메서드 호출

import { fetchChatHistory, postUserMessage, streamChatResponse } from '../apis/chatApi.js';
import { useChatStore } from '../stores/chatStore';
import { useSpecificationStore } from '../stores/specificationStore';

export const useChatHistoryQuery = () => {
  // console.log('훅 진입')
  // zustand 훅
  // 전역 상태 저장소(store)에 접근해 메세지 읽거나 갱신
  // store의 상태와 액션 가져오기
  const setMessages = useChatStore((state) => state.setMessages);

  const queryResult = useQuery({
    queryKey: ['chat', 'history'],
    queryFn: fetchChatHistory,
    onSuccess: (data) => {
      console.log('onSuccess 콜백 실행됨, data:', data);
      setMessages(data);
    },
    onError: (error) => {
      console.error('onError 콜백 실행됨, error:', error);
    },
  });

  return queryResult;
}

export const useSendUserMessage = (projectId, specId) => {
  const queryClient = useQueryClient(); // 데이터 캐시 관리
  const { addMessage, removeMessage, appendLastMessageContent } = useChatStore((state) => state); // 메세지 추가
  const { 
    processSpecData,
    showSidebar
  } = useSpecificationStore((state) => state);

  return useMutation({
    // Mutation 함수는 텍스트나 파일 객체를 받을 수 있음
    mutationFn: (userText) => {
      // projectId와 specId가 전달되지 않았으면 에러
      if (!projectId || !specId) {
        throw new Error('projectId와 specId가 필요합니다.');
      }

      return new Promise((resolve, reject) => {
        let botMessageAdded = false;

        streamChatResponse({
          userText,
          projectId,
          projectSpecId: specId,
          onOpen: () => {
            // 스트림이 시작되면 빈 봇 메시지를 추가
            addMessage({
              id: `bot-${Date.now()}`,
              sender: 'bot',
              text: '',
            });
            botMessageAdded = true;
          },
          onMessage: (data) => {
            // console.log("SSE Data Received: ", data);
            switch (data.type) {
              case 'chat':
                appendLastMessageContent(data.content);
                break;
              case 'project:summarization':
              case 'spec':
              case 'spec:regenerate':
              case 'spec:add:field':
              case 'spec:add:feature:main':
              case 'spec:add:feature:sub':
                // 모든 명세서 관련 데이터는 processSpecData로 통합 처리
                processSpecData(data.content);
                console.log('showSidebar 호출 - 명세서 데이터 수신:', data.type);
                showSidebar(); // 요구사항 명세서 사이드바 표시
                break;
              default:
                console.warn("Unknown SSE event type:", data.type);
            }
          },
          onSpecData: (specData) => {
            // 명세서 데이터 처리 콜백
            console.log('명세서 데이터 처리:', specData);
            processSpecData(specData.content);
            showSidebar();
          },
          onClose: () => {
            console.log("SSE stream closed.");
            resolve(); // Promise를 resolve하여 mutation을 성공 상태로 만듦
          },
          onError: (error) => {
            console.error("SSE Error:", error);
            reject(error); // Promise를 reject하여 mutation을 에러 상태로 만듦
          },
        });
      });
    },
    // Optimistic update 로직은 그대로 유지
    onMutate: async (message) => {
      await queryClient.cancelQueries({ queryKey: ['chat', 'history'] });
      const tempId = `user-${Date.now()}`;
      const newUserMessage = {
        id: tempId,
        sender: 'user',
        type: 'text',
        text: message,
      };
      addMessage(newUserMessage);
      return { tempId };
    },
    onError: (_err, _message, context) => {
      if (context?.tempId) {
        removeMessage(context.tempId);
      }
      addMessage({
        id: `bot-error-${Date.now()}`,
        sender: 'bot',
        text: '오류가 발생했습니다. 다시 시도해 주세요.',
      });
    },
  });
};