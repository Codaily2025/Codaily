// src/apis/chatApi.js
import { authInstance } from './axios';
// import { useSpecificationStore } from '../stores/specificationStore';

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

// 채팅 기록 조회
export const fetchChatHistory = async () => {
  if (useMock1) {
    await new Promise((r) => setTimeout(r, 300));
    return dummyHistory;
  }
  // 실제 연결했을 때 초기 메세지
  return [{
    id: 'bot-1',
    sender: 'bot',
    text: `👋 안녕하세요! 프로젝트 관리 도우미 Codaily 입니다. 시작하고 싶은 프로젝트 아이디어를 알려주세요.

💡 명세서 작성 가이드:
  아래 예시처럼 단계별로 요청해 보세요.

• 전체 명세 생성
   "쇼핑몰 앱 명세서 만들어줘." 

• 새로운 그룹 추가
   "사용자 관리 기능 넣고 싶어." 

• 주 기능 추가
   "사용자 관리에 로그인 기능 추가해줘." 

• 상세 기능 추가
   "로그인 기능 아래에 '소셜 로그인 기능' 
   추가해줘. 사용자 관리 필드야."`
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
  onSpecData, // 명세서 데이터 처리용 콜백 추가
}) => {
  let specNotificationSent = false; // 요구사항 명세서 알림이 한 번만 전송되도록 플래그

  // projectId와 projectSpecId가 전달되지 않았으면 에러
  if (!projectId || !projectSpecId) {
    console.error('projectId와 projectSpecId가 필요합니다:', { projectId, projectSpecId });
    onError?.(new Error('projectId와 projectSpecId가 필요합니다.'));
    return { close: () => { } };
  }

  const eventSourceUrl =
    `${import.meta.env.VITE_BASE_URL}chat/stream` +
    `?message=${encodeURIComponent(userText)}` +
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

  const es = new EventSource(eventSourceUrl, { withCredentials: true });

  // const { showSidebar } = useSpecificationStore((state) => state);
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

    try {
      const msg = JSON.parse(event.data);
      console.log('!!!! SSE 메시지 수신:', msg.type, msg);

      // 명세서 관련 데이터 처리
      if (
        msg?.type === 'spec' ||
        msg?.type === 'spec:regenerate' ||
        msg?.type === 'project:summarization' ||
        msg?.type === 'spec:add:feature:sub' ||
        msg?.type === 'spec:add:feature:main' ||
        msg?.type === 'spec:add:field'
      ) {
        // 요구사항 명세서 관련 작업이므로 "요구사항 명세서를 확인해주세요" 메시지 한 번만 출력
        if (!specNotificationSent) {
          onMessage?.({ type: 'chat', content: '요구사항 명세서를 확인해주세요' });
          // showSidebar()
          specNotificationSent = true;
        }

        // 명세서 데이터 처리 콜백 호출
        if (onSpecData && msg?.content) {
          onSpecData({ type: msg.type, content: msg.content });
        }

        // 실제 데이터는 원본 타입과 함께 전달
        // onMessage?.({ type: msg?.type, content: msg?.content });
      } else if (msg?.type === 'chat') {
        // 일반 대화 데이터
        onMessage?.({ type: msg?.type, content: msg?.content });
      } else {
        console.log('알 수 없는 메시지 타입:', msg?.type, msg?.content);
        onMessage?.({ type: 'error', content: msg?.content });
      }

    } catch (e) {
      console.error('파싱 실패:', event.data);
      onMessage?.({ type: event?.type, content: event.data }); // 원문 전달
    }
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
  { onMessage, onOpen, onError, onClose, onSpecData } = {}
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
    console.error('postUserMessage: projectId와 projectSpecId가 필요합니다:', { projectId, projectSpecId });
    throw new Error('projectId와 projectSpecId가 필요합니다.');
  }

  console.log('postUserMessage 호출 - 프로젝트 정보:', { projectId, projectSpecId, userText });

  // SSE 연결 시작
  const es = streamChatResponse({
    userText,
    projectId,
    projectSpecId,
    onMessage,
    onOpen,
    onError,
    onClose,
    onSpecData, // 명세서 데이터 처리 콜백 전달
  });

  // 호출자에서 필요 시 es.close()로 종료
  return es;
};

/**
 * 명세서 기능 수동 추가 API
 * @param {number} projectId - 프로젝트 ID
 * @param {Object} taskData - 작업 데이터
 * @returns {Promise} - API 응답
 */
// /api/projects/{projectId}/features
// 파라미터 : projectId
// request body : {
//   "title": "string",
//   "description": "string",
//   "field": "string",
//   "category": "string",
//   "priorityLevel": 0,
//   "estimatedTime": 0,
//   "isCustom": true,
//   "projectId": 0,
//   "parentFeatureId": 0
// }
export const addManualFeature = async (projectId, taskData) => {
  try {
    console.log('수동 기능 추가 요청:', taskData);
    console.log('수동 기능 추가 요청 JSON:', JSON.stringify(taskData, null, 2));
    console.log('수동 기능 추가 요청 URL:', `projects/${projectId}/features`);
    const response = await authInstance.post(`projects/${projectId}/features`, taskData);
    console.log('수동 기능 추가 응답:', response.data);
    console.log('수동 기능 추가 응답 JSON:', JSON.stringify(response.data, null, 2));
    return response.data;
  } catch (error) {
    console.error('수동 기능 추가 실패:', error);
    console.error('수동 기능 추가 실패 응답:', error.response?.data);
    throw error;
  }
};
// 응답 형식
// {
//   "featureId": 0,
//   "title": "string",
//   "description": "string",
//   "field": "string",
//   "category": "string",
//   "status": "string",
//   "priorityLevel": 0,
//   "estimatedTime": 0,
//   "isSelected": true,
//   "isCustom": true,
//   "isReduced": true,
//   "projectId": 0,
//   "specificationId": 0,
//   "parentFeatureId": 0,
//   "childFeatures": [
//     "string"
//   ]
// }

const mapPriority = (p) => {
  if (p === 'high') return 1;
  if (p === 'medium') return 4;
  if (p === 'low') return 8;
  return p; // 이미 숫자면 그대로
};

/**
 * 주 기능 추가를 위한 API 요청 데이터 구성
 * @param {Object} formData - 폼 데이터
 * @param {number} projectId - 프로젝트 ID
 * @param {string} field - 필드명 (카테고리)
 * @returns {Object} - API 요청용 데이터
 */
export const buildMainFeatureRequest = (formData, projectId, field = 'Custom Feature') => {
  return {
    title: formData.title,
    description: formData.description,
    field: field, // 주 기능의 경우 field 필요
    category: field, // 카테고리는 field와 동일하게 설정
    priorityLevel: mapPriority(formData.priorityLevel),
    estimatedTime: formData.estimatedTime,
    isCustom: true,
    projectId: projectId
  };
};

/**
 * 필드 안의 주 기능 추가를 위한 API 요청 데이터 구성
 * @param {Object} formData - 폼 데이터
 * @param {number} projectId - 프로젝트 ID
 * @param {string} fieldName - 필드 이름
 * @returns {Object} - API 요청용 데이터
 */
export const buildMainFeatureToFieldRequest = (formData, projectId, fieldName) => {
  return {
    title: formData.title,
    description: formData.description,
    field: fieldName, // 필드 이름을 field로 설정
    category: fieldName, // 카테고리도 필드 이름과 동일하게 설정
    priorityLevel: mapPriority(formData.priorityLevel),
    estimatedTime: formData.estimatedTime,
    isCustom: true,
    projectId: projectId
  };
};

/**
 * 상세 기능 추가를 위한 API 요청 데이터 구성
 * @param {Object} formData - 폼 데이터
 * @param {number} projectId - 프로젝트 ID
 * @param {number} parentFeatureId - 부모 기능 ID
 * @returns {Object} - API 요청용 데이터
 */
export const buildSubFeatureRequest = (formData, projectId, parentFeatureId) => {
  return {
    title: formData.title,
    description: formData.description,
    priorityLevel: mapPriority(formData.priorityLevel),
    estimatedTime: formData.estimatedTime,
    isCustom: true,
    projectId: projectId,
    parentFeatureId: parentFeatureId
    // field와 category는 상세 기능 추가 시에는 포함하지 않음
  };
};