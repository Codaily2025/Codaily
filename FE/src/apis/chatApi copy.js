// src/apis/chatApi.js
import axios from 'axios';
import { authInstance } from './axios';

// 모드 확인: .env에 VITE_USE_MOCK=true로 설정하면 더미 데이터 사용하기
// const useMock = import.meta.env.VITE_USE_MOCK === 'true';
const useMock1 = true;
const useMock2 = false;

// 더미 채팅 기록
const dummyHistory = [
  {
    id: 'bot-1',
    sender: 'bot',
    text: `안녕하세요! 프로젝트 관리 도우미 Codaily 입니다.
프로젝트 시작할 아이디어를 알려주세요.`
  },
  { id: 'user-1', sender: 'user', text: '요리 레시피를 알려주는 챗봇 만들고 싶어.' },
  {
    id: 'bot-2',
    sender: 'bot',
    text: `맞춤형 프로젝트 관리를 위해 프로젝트에 대해 구체적으로 설명해주세요.
기능 정의서나 유저 플로우 등 참고할 수 있는 파일을 첨부해주셔도 좋아요!`
  },
  { id: 'user-2', sender: 'user', text: 'RAG 파이프라인을 기반으로 클라우드 DB를 구축할거야.' },
  {
    id: 'bot-3',
    sender: 'bot',
    text: `주 사용자는 누구인가요?`
  },
  { id: 'user-3', sender: 'user', text: '주 사용자는 가정에서 쉽게 요리를 하고 싶은 사람들이야.' },
  {
    id: 'bot-4',
    sender: 'bot',
    text: `어떤 서비스를 제공하실 건가요?`
  },
  { id: 'user-4', sender: 'user', text: '세 가지 기능을 제공할거야\n먼저 사용자가 레시피를 물어보면 레시피 정보를 알려줄거야.\n그리고 사용자가 영양 정보를 물어보면 영양 정보를 알려줄거야.\n그리고 사용자가 냉장고에 있는 재료로 어떤 요리를 할 수 있는지 물어보면 레시피를 알려줄 생각이야.' },
  { id: 'bot-5', sender: 'bot', text: `요구사항 명세서를 생성했어요.\n버튼이나 채팅을 통해 원하는 기능을 추가하고 삭제해보세요.\n명세서 수정이 완료되었다면 화면 하단의 버튼을 클릭해 다음 단계로 이동해주세요.` },
];

// 채팅 기록 조회
export const fetchChatHistory = async () => {

  // 더미 데이터 사용 시
  if (useMock1) {
    // 지연 추가
    await new Promise(r => setTimeout(r, 300));
    // console.log('fetchChatHistory 완료, 반환값:', dummyHistory);
    return dummyHistory;
  }

  // 실제 호출 시
  // const { data } = await API_URL.get('/chat/history');
  // 예. [{ id, sender, text}, ...]
  return data;
}

export const streamChatResponse = ({
  userText,
  projectId,
  projectSpecId,
  onMessage, // 메시지 수신 시 호출될 콜백
  onOpen,    // 연결 성공 시 호출될 콜백
  onError,   // 에러 발생 시 호출될 콜백
  onClose,   // 연결 종료 시 호출될 콜백
}) => {
  // 토큰 가져오기
  const token = localStorage.getItem('authToken');
  console.log('SSE 연결 시 토큰 확인:', token ? '토큰 있음' : '토큰 없음');
  
  // 서버 상태 확인
  const checkServerStatus = async () => {
    try {
      const response = await fetch('https://i13a601.p.ssafy.io/api/chat/stream?userId=1&message=test&projectId=1&specId=1', {
        method: 'HEAD'
      });
      console.log('서버 상태 확인:', response.status, response.headers.get('content-type'));
      return response.ok;
    } catch (error) {
      console.error('서버 연결 실패:', error);
      return false;
    }
  };
  
  // 백엔드 팀원 테스트 코드와 정확히 일치하는 URL 구조로 변경 (8081 포트 사용)
  const eventSourceUrl = `'https://i13a601.p.ssafy.io/api/chat/stream?userId=1&message=${encodeURIComponent(userText)}&projectId=${projectId}&specId=${projectSpecId}`;
  console.log('SSE 연결 URL:', eventSourceUrl);
  
  // 서버 상태 확인 (비동기로 실행하되 SSE 연결은 바로 시작)
  checkServerStatus().then(isServerRunning => {
    if (!isServerRunning) {
      console.error('백엔드 서버가 실행되지 않고 있습니다. 포트 8081에서 서버를 실행해주세요.');
    }
  });
  
  const eventSource = new EventSource(eventSourceUrl);

  // 연결이 성공적으로 열렸을 때
  eventSource.onopen = () => {
    console.log("SSE connection established.");
    onOpen?.(); // onOpen 콜백 호출
  };

  // 서버로부터 메시지를 수신했을 때
  eventSource.onmessage = (event) => {
    // data가 비어있거나, 스트림 종료 신호([DONE])일 경우 연결 종료
    if (!event.data || event.data === '[DONE]') {
      eventSource.close();
      onClose?.(); // onClose 콜백 호출
      return;
    }

    try {
      const { type, content } = JSON.parse(event.data);
      console.log('SSE 수신 데이터:', event.data);
      
      // 백엔드 팀원 테스트 코드와 일치하는 응답 처리
      if (type === "chat") {
        onMessage({ type, content }); // 일반 채팅 메시지
      } else if (type === "spec" || type === "spec:regenerate" || type === "project:summarization"
        || type === 'spec:add:feature:sub' || type === 'spec:add:feature:main' || type === 'spec:add:field') {
        // 명세서 관련 응답은 JSON 형태로 전달
        onMessage({ type, content: JSON.stringify(content, null, 4) + "\n\n" });
      } else {
        // 기타 타입의 응답
        onMessage({ type, content: `[${type}]: ${JSON.stringify(content)}\n\n` });
      }
    } catch (e) {
      console.error("Failed to parse SSE data:", event.data);
      // 파싱 실패 시 원본 데이터를 그대로 전달
      onMessage({ type: 'error', content: event.data });
    }
  };

  // 에러 발생 시
  eventSource.onerror = (error) => {
    console.error("EventSource failed:", error);
    console.error("EventSource readyState:", eventSource.readyState);
    console.error("EventSource URL:", eventSourceUrl);
    
    // readyState 값 설명
    const readyStateMap = {
      0: 'CONNECTING',
      1: 'OPEN', 
      2: 'CLOSED'
    };
    console.error("EventSource readyState 설명:", readyStateMap[eventSource.readyState]);
    
    // 서버 연결 실패 시 더 명확한 에러 메시지
    if (eventSource.readyState === 2) { // CLOSED
      const errorMessage = '백엔드 서버에 연결할 수 없습니다. 서버가 실행되고 있는지 확인해주세요. (포트: 8081)';
      console.error(errorMessage);
      onError?.(new Error(errorMessage));
    } else {
      onError?.(error);
    }
    
    eventSource.close();
  };

  // EventSource 객체를 반환하여, 호출한 쪽에서 연결을 직접 닫을 수 있도록 함
  return eventSource;
};


// 사용자 메세지 전송 & 백엔드 챗봇 응답 받기
export const postUserMessage = async (userText) => {
  // 더미 데이터 사용 시
  if (useMock2) {
    // 지연 추가
    await new Promise(r => setTimeout(r, 300));
    return {
      id: `bot-${Date.now()}`,
      sender: 'bot',
      text: `에코: ${userText}`   // 임시 에코 응답
    };
  }

  let projectSpecId = 15;
  let projectId = 12;
  try {
    const response_projectSpecId = await authInstance.post('/projects', {
      "startDate": "2025-08-10",
      "endDate": "2025-09-20",
      "availableDates": ["2025-08-12", "2025-08-15", "2025-08-20"],
      "workingHours": {
        "MONDAY": 4,
        "WEDNESDAY": 6,
        "FRIDAY": 2
      }
    })
    console.log('일정 입력 후 받은 프로젝트, 명세서 아이디:', response_projectSpecId);
    projectSpecId = response_projectSpecId.data.specId;
    projectId = response_projectSpecId.data.projectId;
  } catch (error) {
    console.error('postUserMessage Error:', error)
    throw new Error(error.response?.data?.message || '사용자 메세지 전송 중 오류가 발생했습니다.')
  }

    // const response = await authInstance.get('/api/chat/stream', {
  //     userId: 1,
  //     message: userText,
  //     projectId: 1,
  //     specId: 1
  //   }
  // );

  console.log('projectId:', projectId);
  console.log('projectSpecId:', projectSpecId);

  // ProjectId와 ProjectSpecId가 null이 아닐 때 실행
  // 채팅 메세지 전송 및 응답 받기
  if (projectId && projectSpecId) {
    const response = await authInstance.get('/api/chat/stream', {
      params: {
        userId: '1',
        message: userText,
        projectId: projectId,
        specId: projectSpecId
      }
    });
    console.log('채팅 메세지 전송 후 받은 응답:', response.data);
  }

  /*
  Parameters : userId, message, projectId, specId
  // GET 요청에서는 파라미터를 쿼리스트링(query string)으로 전달해야 함
  // axios의 경우, 두 번째 인자에 { params: { ... } } 형태로 전달하면 됨
  */
  // 백엔드가 반환한 bot 응답 데이터 반환
  // 예. { id, sender: 'bot', text }

  return response.data;
}