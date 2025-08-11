// src/apis/chatApi.js
import { authInstance } from './axios';

// 더미 모드 (원하면 false로 바꾸세요)
const useMock1 = false;  // fetchChatHistory 용
const useMock2 = false; // postUserMessage / SSE 모의 응답

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
  { id: 'bot-3', sender: 'bot', text: `주 사용자는 누구인가요?` },
  { id: 'user-3', sender: 'user', text: '주 사용자는 가정에서 쉽게 요리를 하고 싶은 사람들이야.' },
  { id: 'bot-4', sender: 'bot', text: `어떤 서비스를 제공하실 건가요?` },
  {
    id: 'user-4',
    sender: 'user',
    text:
      '세 가지 기능을 제공할거야\n먼저 사용자가 레시피를 물어보면 레시피 정보를 알려줄거야.\n그리고 사용자가 영양 정보를 물어보면 영양 정보를 알려줄거야.\n그리고 사용자가 냉장고에 있는 재료로 어떤 요리를 할 수 있는지 물어보면 레시피를 알려줄 생각이야.'
  },
  {
    id: 'bot-5',
    sender: 'bot',
    text:
      `요구사항 명세서를 생성했어요.
버튼이나 채팅을 통해 원하는 기능을 추가하고 삭제해보세요.
명세서 수정이 완료되었다면 화면 하단의 버튼을 클릭해 다음 단계로 이동해주세요.`
  },
];

// 채팅 기록 조회(더미)
export const fetchChatHistory = async () => {
  if (useMock1) {
    await new Promise((r) => setTimeout(r, 300));
    return dummyHistory;
  }
  // 실제 구현 시 API 연동
  return [{
    id: 'bot-1',
    sender: 'bot',
    text: `안녕하세요! 프로젝트 관리 도우미 Codaily 입니다.
프로젝트 시작할 아이디어를 알려주세요.`
  }];
};

/**
 * 서버 상태 간단 확인 (선택)
 * - HEAD를 지원하지 않으면 실패 로그만 남고 동작엔 영향 없음
 */
// async function checkServerStatus() {
//   try {
//     const userId = '1';
//     const res = await fetch(
//       `http://localhost:8081/api/chat/stream?userId=${userId}&message=test&projectId=1&specId=1`,
//       { method: 'HEAD' }
//     );
//     console.log('서버 상태 확인:', res.status, res.headers.get('content-type'));
//     return res.ok;
//   } catch (err) {
//     console.error('서버 연결 실패:', err);
//     return false;
//   }
// }

/**
 * 팀원 테스트 코드와 동일한 SSE 처리 방식
 * - 서버가 보내는 { type, content }를 파싱해 타입별로 가공
 * - onMessage 콜백에 { type, content } 형태로 전달
 * - 필요 시 onOpen / onError / onClose 사용
 */
export const streamChatResponse = ({
  userText,
  projectId,
  projectSpecId,
  onMessage,
  onOpen,
  onError,
  onClose,
}) => {
  let specNotificationSent = false; // 요구사항 명세서 알림이 한 번만 전송되도록 플래그
  const eventSourceUrl =
    `http://localhost:8081/api/chat/stream` +
    `?userId=1` +
    `&message=${encodeURIComponent(userText)}` +
    `&projectId=${encodeURIComponent(projectId)}` +
    `&specId=${encodeURIComponent(projectSpecId)}`;

  console.log('SSE 연결 URL:', eventSourceUrl);

  // checkServerStatus().then((ok) => {
  //   if (!ok) {
  //     console.error('백엔드 서버가 실행되지 않고 있습니다. 포트 8081에서 서버를 실행해주세요.');
  //   }
  // });

  let fullContent = "";
  let ended = false; // 의도적 종료 플래그

  const es = new EventSource(eventSourceUrl);

  es.onopen = () => {
    console.log('SSE 연결');
    onOpen?.();
  };

  es.onmessage = (event) => {
    // 종료 신호
    if (!event.data || event.data === '[DONE]') {
      es.close();
      onClose?.();
      return;
    }

    // for (const jsonString of jsonStrings) {
    // if (jsonString.trim() === '') continue;

    try {
      // const { type, content } = JSON.parse(jsonString);
      const msg = JSON.parse(event.data);
      console.log('!!!!!!!!', msg.type, msg)
      // event.data
      // type이 chat일 때,
      // 
      if (msg?.type && msg.type === 'chat') {
        // 일반 대화 데이터
        onMessage?.({ type: msg?.type, content: msg?.content });
      } else if (
        msg?.type === 'spec' ||
        msg?.type === 'spec:regenerate' ||
        msg?.type === 'project:summarization' ||
        msg?.type === 'spec:add:feature:sub' ||
        msg?.type === 'spec:add:feature:main' ||
        msg?.type === 'spec:add:field'
      ) {
        // console.log({type: msg?.type, content: msg?.content})
        // 요구사항 명세서 관련 작업이므로 "요구사항 명세서를 확인해주세요" 메시지 한 번만 출력
        if (!specNotificationSent) {
          onMessage?.({ type: 'chat', content: '요구사항 명세서를 확인해주세요' });
          specNotificationSent = true;
        }
        // 실제 데이터는 원본 타입과 함께 전달
        onMessage?.({ type: msg?.type, content: msg?.content });
      } else {
        console.log({type: 'error', content: msg?.content})
        onMessage?.({ type: 'error', content: msg?.content });
      }

    } catch (e) {
      console.error('파싱 실패:', event.data);
      onMessage?.({ type: 'error', content: event.data }); // 원문 전달
    }
    // }
  };

  es.onerror = (error) => {
    // 에러 전달
    // onError?.(error);
    es.close();
    // 반드시 닫아서 자동 재연결 루프 끊기
    // try { es.close(); } catch { }
    // onClose?.();
  };

  // 호출 측에서 수동 종료할 수 있도록 반환
  return {
    close: () => {
      es.close();
      onClose?.();
    },
    _es: es,
  };
};

export const postUserMessage = async (
  userText,
  projectId,
  projectSpecId,
  { onMessage, onOpen, onError, onClose } = {}
) => {
  if (useMock2) {
    // 더미 모드: 간단히 콜백 호출
    await new Promise((r) => setTimeout(r, 300));
    onOpen?.();
    onMessage?.({ type: 'chat', content: `에코: ${userText}` });
    onClose?.();
    return { close: () => { } }; // mock 핸들
  }

  // projectId와 specId가 전달되지 않았으면 에러
  if (!projectId || !projectSpecId) {
    throw new Error('projectId와 projectSpecId가 필요합니다.');
  }

  // SSE 연결 시작
  const es = streamChatResponse({
    userText,
    projectId,
    projectSpecId,
    onMessage,
    onOpen,
    onError,
    onClose,
  });

  // 호출자에서 필요 시 es.close()로 종료
  return es;
};
