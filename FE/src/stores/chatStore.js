// src/stores/chatStore.js
import { create } from 'zustand';

export const useChatStore = create((set) => ({
  // 초기 메세지 (백엔드 응답 없이 페이지 로드 시 표시)
  messages: [ ],
  // 메세지 전체 교체
  // setMessages: (newMessages) => set({ messages: newMessages }),
  setMessages: (messages) => set({ messages }),
  // 메세지 추가
  // 예시 메시지 객체:
  // { id, sender, type: 'text', text: '...' }
  // { id, sender, type: 'file', text: '파일: example.pdf', file: { name, size, type } }
  addMessage: (message) => set((state) => ({ messages: [...state.messages, message] })),
 
  // 스트리밍 중인 텍스트를 마지막 메시지에 추가하는 액션
  appendLastMessageContent: (contentChunk) => set((state) => {
    const lastMessage = state.messages[state.messages.length - 1];
    if (lastMessage && lastMessage.sender === 'bot') {
      const updatedMessage = { ...lastMessage, text: lastMessage.text + contentChunk };
      return { messages: [...state.messages.slice(0, -1), updatedMessage] };
    }
    return state;
  }),
  
  // 메세지 삭제 -> 채팅 초기화
  removeMessage: (id) => set((state) => ({ messages: state.messages.filter((msg) => msg.id !== id) })),
}));