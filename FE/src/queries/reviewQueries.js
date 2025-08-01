import { useQuery } from '@tanstack/react-query';

// --- 목업 데이터 (나중에는 API에서 받아올 예정) ---
const mockReviews = [
  {
    projectId: 1,
    id: 101, // 각 리뷰의 고유 ID
    category: '회원 관리',
    title: '일반 로그인 기능 구현',
    details: '2025/07/28 14:32 · 3개 파일',
    tags: [
      { text: '중간 1', type: 'medium' },
      { text: '낮음 4', type: 'low' },
    ],
    score: 61,
    scoreColor: 'orange',
  },
  {
    projectId: 2,
    id: 102,
    category: '회원 관리',
    title: '소셜 로그인 기능 구현',
    details: '2025/07/28 14:32 · 3개 파일',
    tags: [
      { text: '높음 4', type: 'high' },
      { text: '중간 3', type: 'medium' },
      { text: '낮음 4', type: 'low' },
    ],
    score: 30,
    scoreColor: 'red',
  },
  {
    projectId: 3,
    id: 103,
    category: '회원 관리',
    title: '회원 탈퇴 기능 구현',
    details: '2025/07/28 14:32 · 3개 파일',
    tags: [{ text: '낮음 4', type: 'low' }],
    score: 91,
    scoreColor: 'green',
  },
  {
    projectId: 3,
    id: 104,
    category: '배포',
    title: 'AWS 서버 구현',
    details: '2025/07/28 14:32 · 3개 파일',
    tags: [
      { text: '높음 4', type: 'high' },
      { text: '중간 3', type: 'medium' },
      { text: '낮음 4', type: 'low' },
    ],
    score: 30,
    scoreColor: 'red',
  },
  {
    projectId: 1,
    id: 105,
    category: '배포',
    title: 'CI/CD 연결',
    details: '2025/07/28 14:32 · 3개 파일',
    tags: [{ text: '낮음 4', type: 'low' }],
    score: 91,
    scoreColor: 'green',
  },
];

const mockProjectOptions = [
  {
    id: null,
    name: '모든 프로젝트',
    features: ['전체', '회원 관리', '배포'],
  },
  {
    id: 2,
    name: '레시피 챗봇 서비스',
    features: ['전체', '회원 관리'],
  },
  {
    id: 3,
    name: '싸피 출석 알림 서비스',
    features: ['전체', '회원 관리', '배포'],
  },
  { 
    id: 4,
    name: '음악 스트리밍 구독 서비스',
    features: ['전체'],
  },
];


// --- 목업 API 호출 함수 ---

// 모든 리뷰를 가져오는 함수 (API 호출 시뮬레이션)
const fetchReviews = () => {
  console.log('리뷰 데이터를 가져오는 중...');
  return new Promise(resolve => setTimeout(() => resolve(mockReviews), 500));
};

// 모든 프로젝트 옵션을 가져오는 함수 (API 호출 시뮬레이션)
const fetchProjectOptions = () => {
  console.log('프로젝트 옵션을 가져오는 중...');
  return new Promise(resolve => setTimeout(() => resolve(mockProjectOptions), 500));
};


// --- React Query를 활용한 커스텀 훅 정의 ---

/**
 * 모든 코드 리뷰 데이터를 가져오는 커스텀 훅
 */
export const useReviews = () => {
  return useQuery({
    queryKey: ['reviews'],        // 이 쿼리를 구분하는 고유 키
    queryFn: fetchReviews,        // 데이터를 가져오는 함수
    staleTime: 1000 * 60 * 5,     // 5분 동안 데이터를 신선한 상태로 유지
  });
};

/**
 * 프로젝트 필터 옵션을 가져오는 커스텀 훅
 */
export const useProjectOptions = () => {
  return useQuery({
    queryKey: ['projectOptions'],
    queryFn: fetchProjectOptions,
    staleTime: Infinity, // 이 데이터는 변하지 않으므로 만료되지 않도록 설정
  });
};
