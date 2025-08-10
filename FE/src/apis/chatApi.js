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
async function checkServerStatus() {
  try {
    const userId = '1';
    const res = await fetch(
      `http://localhost:8081/api/chat/stream?userId=${userId}&message=test&projectId=1&specId=1`,
      { method: 'HEAD' }
    );
    console.log('서버 상태 확인:', res.status, res.headers.get('content-type'));
    return res.ok;
  } catch (err) {
    console.error('서버 연결 실패:', err);
    return false;
  }
}

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
  const eventSourceUrl =
    `http://localhost:8081/api/chat/stream` +
    `?userId=1` +
    `&message=${encodeURIComponent(userText)}` +
    `&projectId=${encodeURIComponent(projectId)}` +
    `&specId=${encodeURIComponent(projectSpecId)}`;

  console.log('SSE 연결 URL:', eventSourceUrl);

  // 서버 상태 확인은 비동기 로그용 (SSE는 곧바로 연결)
  checkServerStatus().then((ok) => {
    if (!ok) {
      console.error('백엔드 서버가 실행되지 않고 있습니다. 포트 8081에서 서버를 실행해주세요.');
    }
  });

  const es = new EventSource(eventSourceUrl);

  // 추가: 수신 여부/수동 종료 플래그
  let receivedAny = false;
  let closedManually = false;

  es.onopen = () => {
    console.log('SSE 연결결');
    onOpen?.();
  };

  es.onmessage = (event) => {
    // 종료 신호
    if (!event.data || event.data === '[DONE]') {
      es.close();
      onClose?.();
      return;
    }

    try {
      const { type, content } = JSON.parse(event.data);

      // 한 번이라도 수신함 플래그 설정
      receivedAny = true;

      // 팀원 테스트 로직과 동일한 타입 처리
      if (type === 'chat') {
        onMessage?.({ type, content });
      } else if (
        type === 'spec' ||
        type === 'spec:regenerate' ||
        type === 'project:summarization' ||
        type === 'spec:add:feature:sub' ||
        type === 'spec:add:feature:main' ||
        type === 'spec:add:field'
      ) {
        onMessage?.({ type, content: JSON.stringify(content, null, 4) + '\n\n' });
      } else {
        onMessage?.({ type, content: `[${type}]: ${JSON.stringify(content)}\n\n` });
      }
    } catch (e) {
      receivedAny = true;
      console.error('파싱 실패:', event.data);
      onMessage?.({ type: 'error', content: event.data }); // 원문 전달
    }
  };

  es.onerror = (error) => {
    if (closedManually) return;

    // 에러 전달
    onError?.(error);

    // 반드시 닫아서 자동 재연결 루프 끊기
    try { es.close(); } catch { }
    onClose?.();
  };

  // if (es.readyState === 2) {
  // const msg = '백엔드 서버에 연결할 수 없습니다. 서버가 실행되고 있는지 확인해주세요. (포트: 8081)';
  // onError?.(new Error(msg));
  // } else {
  // onError?.(error);
  // }
  // es.close();

  // 호출 측에서 수동 종료할 수 있도록 반환
  return {
    close: () => {
      closedManually = true;
      es.close();
      onClose?.();
    },
    _es: es,
  };
};

/**
 * 사용자 메시지 전송(프로젝트/명세서 생성 -> SSE 연결)
 * - 일정 정보 POST로 프로젝트/명세서 생성 로직을 그대로 사용
 * - 생성된 projectId/specId로 streamChatResponse를 호출해 SSE EventSource를 반환
 *
 * 사용법:
 * const es = await postUserMessage('안녕', {
 *   onOpen: () => {},
 *   onMessage: ({ type, content }) => {},
 *   onError: (err) => {},
 *   onClose: () => {},
 * });
 * // 필요 시 es.close();
 */
export const postUserMessage = async (
  userText,
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

  // 1) 일정 -> 프로젝트/명세서 생성 (기존 로직 유지)
  let projectId = null;
  let projectSpecId = null;

  try {
    const res = await authInstance.post('/projects', {
      startDate: '2025-08-10',
      endDate: '2025-09-20',
      availableDates: ['2025-08-12', '2025-08-15', '2025-08-20'],
      workingHours: {
        MONDAY: 4,
        WEDNESDAY: 6,
        FRIDAY: 2,
      },
    });
    console.log('일정 입력 후 받은 프로젝트, 명세서 아이디:', res);
    projectSpecId = res.data?.specId;
    projectId = res.data?.projectId;
  } catch (error) {
    console.error('postUserMessage Error:', error);
    throw new Error(error?.response?.data?.message || '사용자 메세지 전송 중 오류가 발생했습니다.');
  }

  if (!projectId || !projectSpecId) {
    throw new Error('projectId/specId 생성에 실패했습니다.');
  }

  // 2) 생성된 ID로 SSE 연결 시작
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
