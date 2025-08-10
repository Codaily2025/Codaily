// FE/src/queries/reviewQueries.js
// 코드 리뷰 데이터 조회 쿼리
import { useQuery } from '@tanstack/react-query';
import { fetchCodeReviewList, generateProjectOptions } from '../apis/codeReview';

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
    projectId: 1,
    id: 102,
    category: '회원 관리',
    title: '소셜 로그인 기능 구현',
    details: '2025/07/28 14:32 · 4개 파일',
    tags: [
      { text: '높음 5', type: 'high' },
      { text: '중간 5', type: 'medium' },
      { text: '낮음 2', type: 'low' },
    ],
    score: 30,
    scoreColor: 'red',
  },
  {
    projectId: 2,
    id: 103,
    category: '회원 관리',
    title: '회원 탈퇴 기능 구현',
    details: '2025/07/28 14:32 · 3개 파일',
    tags: [{ text: '낮음 4', type: 'low' }],
    score: 91,
    scoreColor: 'green',
  },
  {
    projectId: 2,
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
    id: 1,
    name: '레시피 챗봇 서비스',
    features: ['전체', '회원 관리'],
  },
  {
    id: 2,
    name: '싸피 출석 알림 서비스',
    features: ['전체', '회원 관리', '배포'],
  },
  {
    id: 3,
    name: '음악 스트리밍 구독 서비스',
    features: ['전체'],
  },
];

const mockReviewDetail = [
  {
    reviewId: 101,
    projectId: 1,
    projectName: '레시피 챗봇 서비스',
    featureCategory: '회원 관리',
    reviewName: '일반 로그인 기능 구현',
    reviewDate: '2025/07/28 14:32',
    reviewScore: 61,
    filesChanged: 3,
    codeReview: {
      convention: {
        summary: '일관성 있는 네이밍 확인됨',
        issues: [
          {
            file: 'login.js',
            line: '20',
            description: '함수명 소문자 시작 권장',
            level: 'low',
          }
        ]
      },
      performance: {},
      security: {},
      complexity: {},
      bugRisk: {},
      refactoring: {},
    }
  },
  {
    reviewId: 102,
    projectId: 1,
    projectName: '레시피 챗봇 서비스',
    featureCategory: '회원 관리',
    reviewName: '소셜 로그인 기능 구현',
    reviewDate: '2025/07/28 14:32',
    reviewScore: 30,
    filesChanged: 4,
    codeReview: {
      convention: {
        summary: '일관성 있는 네이밍, 일부 주석 누락',
        issues: [
          {
            file: 'login.js',
            line: '45',
            description: '변수명 혼용 (camelCase 권장)',
            level: 'medium',
          },
          {
            file: 'auth.js',
            line: '12',
            description: '주석 없음',
            level: 'low',
          }
        ]
      },
      performance: {
        summary: '불필요한 반복문 사용',
        issues: [
          {
            file: 'auth.js',
            line: '88',
            description: '중복된 DB 호출',
            level: 'high',
          },
          {
            file: 'utils.js',
            line: '21',
            description: '불필요한 객체 생성',
            level: 'medium',
          }
        ]
      },
      security: {
        summary: '하드코딩된 비밀번호 감지',
        issues: [
          {
            file: 'config.js',
            line: '55',
            description: 'key="abcd" (하드코딩된 키)',
            level: 'high',
          },
          {
            file: 'auth.js',
            line: '72',
            description: '입력 검증 누락',
            level: 'medium',
          }
        ]
      },
      complexity: {
        summary: 'login() 함수가 복잡함',
        issues: [
          {
            file: 'login.js',
            line: '30-60',
            description: '분기 5개 이상 (권장: 3개 이하)',
            level: 'high',
          }
        ]
      },
      bugRisk: {
        summary: 'NPE 가능성 존재',
        issues: [
          {
            file: 'login.js',
            line: '28',
            description: 'null 체크 없음',
            level: 'high',
          },
          {
            file: 'utils.js',
            line: '156',
            description: '배열 인덱스 범위 확인 누락',
            level: 'medium',
          }
        ]
      },
      refactoring: {
        summary: '메서드 분리 필요',
        issues: [
          {
            file: 'login.js',
            line: '',
            description: 'login() 함수 내부 분리 권장',
            level: 'high',
          },
          {
            file: 'auth.js',
            line: '72',
            description: '입력 검증 누락',
            level: 'medium',
          },
          {
            file: 'utils.js',
            line: '',
            description: '헬퍼 함수 분리 권장',
            level: 'low',
          }
        ]
      }
    }
  },
  {
    reviewId: 103,
    projectId: 2,
    projectName: '쇼핑몰 구축 프로젝트',
    featureCategory: '회원 관리',
    reviewName: '회원 탈퇴 기능 구현',
    reviewDate: '2025/07/28 14:32',
    reviewScore: 91,
    filesChanged: 3,
    codeReview: {
      security: {
        summary: '입력 검증 및 에러 핸들링 확인됨',
        issues: []
      },
      convention: {},
      performance: {},
      complexity: {},
      bugRisk: {},
      refactoring: {},
    }
  },
  {
    reviewId: 104,
    projectId: 2,
    projectName: '쇼핑몰 구축 프로젝트',
    featureCategory: '배포',
    reviewName: 'AWS 서버 구현',
    reviewDate: '2025/07/28 14:32',
    reviewScore: 30,
    filesChanged: 3,
    codeReview: {
      performance: {
        summary: 'S3 업로드 로직 비효율적',
        issues: [
          {
            file: 'deploy.js',
            line: '34',
            description: 'Promise 중첩 사용 개선 필요',
            level: 'medium',
          }
        ]
      },
      security: {
        summary: 'IAM 권한 과다 설정',
        issues: [
          {
            file: 'iam-config.json',
            line: '',
            description: '최소 권한 원칙 적용 필요',
            level: 'high',
          }
        ]
      },
      convention: {},
      complexity: {},
      bugRisk: {},
      refactoring: {},
    }
  },

  // CI/CD 연결
  {
    reviewId: 105,
    projectId: 1,
    projectName: '레시피 챗봇 서비스',
    featureCategory: '배포',
    reviewName: 'CI/CD 연결',
    reviewDate: '2025/07/28 14:32',
    reviewScore: 91,
    filesChanged: 3,
    codeReview: {
      refactoring: {
        summary: '스크립트 구조 개선 필요',
        issues: [
          {
            file: 'ci-cd.yml',
            line: '',
            description: '단일 파이프라인 분리 권장',
            level: 'medium',
          }
        ]
      },
      convention: {},
      performance: {},
      security: {},
      complexity: {},
      bugRisk: {},
    }
  }
]

// --- 목업 API 호출 함수 ---

// 모든 프로젝트 옵션을 가져오는 함수 (API 호출 시뮬레이션)
const fetchProjectOptions = async () => {
  console.log('프로젝트 옵션을 가져오는 중...');
  try {
    // 실제 코드 리뷰 데이터를 가져와서 프로젝트 옵션 생성
    const codeReviewData = await fetchCodeReviewList();
    const projectOptions = generateProjectOptions(codeReviewData);
    return projectOptions;
  } catch (error) {
    console.error('프로젝트 옵션 생성 실패:', error);
    // 에러 발생 시 기본 옵션 반환
    return [
      {
        id: null,
        name: '모든 프로젝트',
        features: ['전체']
      }
    ];
  }
};

// 특정 리뷰의 상세 정보를 가져오는 함수
const fetchReviewDetail = (reviewId) => {
  console.log('리뷰 상세 정보를 가져오는 중...');
  return new Promise(resolve => {
    setTimeout(() => {
      const detail = mockReviewDetail.find(review => review.reviewId === reviewId);
      resolve(detail);
    }, 300);
  });
};

// React Query 커스텀 훅

// 모든 코드 리뷰 데이터를 가져오는 커스텀 훅
export const useReviews = () => {
  const fetchReviews = async () => {
    const response = await fetchCodeReviewList();
    return response;
  }
  return useQuery({
    queryKey: ['reviews'],        // 이 쿼리를 구분하는 고유 키
    queryFn: fetchReviews,        // 데이터를 가져오는 함수
    staleTime: 1000 * 60 * 5,     // 5분 동안 데이터를 신선한 상태로 유지
  });
};

// 프로젝트 필터 옵션을 가져오는 커스텀 훅
export const useProjectOptions = () => {
  return useQuery({
    queryKey: ['projectOptions'],
    queryFn: fetchProjectOptions,
    staleTime: Infinity, // 이 데이터는 변하지 않으므로 만료되지 않도록 설정
  });
};

// 특정 리뷰의 상세 정보를 가져오는 커스텀 훅
export const useReviewDetail = (reviewId) => {
  return useQuery({
    // reviewId별로 쿼리 결과를 캐싱하기 위해 queryKey에 포함
    queryKey: ['reviewDetail', reviewId],
    // reviewId를 fetch 함수에 전달
    queryFn: () => fetchReviewDetail(reviewId),
    // reviewId가 있을 때만 쿼리 실행
    enabled: !!reviewId,
    // 5분 동안 데이터를 신선한 상태로 유지
    staleTime: 1000 * 60 * 5,
  });
};
