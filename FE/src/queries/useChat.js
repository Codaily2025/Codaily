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

import { fetchChatHistory, postUserMessage } from '../apis/chatApi';
import { useChatStore } from '../stores/chatStore';

export const useChatHistoryQuery = () => {
  console.log('훅 진입')
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

export const useSendUserMessage = () => {
  const queryClient = useQueryClient(); // 데이터 캐시 관리
  const addMessage = useChatStore((state) => state.addMessage); // 메세지 추가
  const removeMessage = useChatStore((state) => state.removeMessage); // 메세지 삭제

  return useMutation({
    // Mutation 함수는 텍스트나 파일 객체를 받을 수 있음
    mutationFn: (message) => postUserMessage(message),
    // API 호출 전에 실행 -> 사용자 메세지 먼저 추가
    onMutate: async (message) => {
      // 기존 쿼리를 취소하여 이전 데이터와 충돌하지 않도록 함
      // 취소 후 새로운 데이터 추가
      await queryClient.cancelQueries({ queryKey: ['chat', 'history'] });

      const tempId = `user-${Date.now()}`
      let newUserMessage;

      if (typeof message === 'string') {
        // 텍스트 메시지 객체 생성
        newUserMessage = {
          id: tempId,
          sender: 'user',
          type: 'text',
          text: message,
        };
      } else if (message instanceof File) {
        // 파일 메시지 객체 생성
        newUserMessage = {
          id: tempId,
          sender: 'user',
          type: 'file',
          // UI에 표시될 텍스트 (파일명과 크기)
          text: `파일: ${message.name} (${(message.size / 1024).toFixed(2)} KB)`,
          file: {
            name: message.name,
            size: message.size,
            type: message.type,
          },
        };
      }

      // 생성된 새 메시지를 전역 상태에 추가하여 UI에 즉시 표시
      if (newUserMessage) {
        addMessage(newUserMessage);
      }

      // 오류 발생 시 롤백에 사용할 임시 ID를 컨텍스트에 저장
      return { tempId };
    },
    onError: (_err, message, context) => {
      // onMutate에서 추가했던 임시 메시지를 전역 상태에서 제거
      if (context?.tempId) {
        removeMessage(context.tempId);
      }
      console.error('채팅 메시지 전송 중 오류가 발생했습니다.', error);
    },
    onSuccess: (botMsg) => {
      // 백엔드 응답(봇 메세지) 채팅 기록에 추가
      // addMessage(botMsg)
      addMessage({ ...botMsg, type: 'text' });
    },
    // 성공 여부 상관 없이 항상 실행됨
    onSettled: () => {
      // 캐시 무효화
      queryClient.invalidateQueries(['chat', 'history'])
    },
  });
}
