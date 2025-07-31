import { create } from 'zustand';

export const useReviewStore = create((set) => ({
  // 상태값 정의
  selectedProjectId: null,     // 선택된 프로젝트 ID, 전체일 경우 null
  selectedFeature: '전체',      // 선택된 기능명
  searchQuery: '',            // 검색어
  selectedReviewId: null,      // 상세보기로 선택된 리뷰 ID
  visibleCount: 3,            // 화면에 보이는 카드 수
  openDropdown: null,         // 열려있는 드롭다운: 'project' | 'feature' | null

  // 액션 함수 정의

  /**
   * 프로젝트 선택 시 관련 필터 초기화
   * @param {number | null} projectId - 선택할 프로젝트 ID (null은 전체)
   */
  setProject: (projectId) => set({
    selectedProjectId: projectId,
    selectedFeature: '전체',  // 기능 필터 초기화
    searchQuery: '',         // 검색어 초기화
    visibleCount: 3,         // 카드 개수 초기화
    openDropdown: null,      // 드롭다운 닫기
  }),

  /**
   * 기능(카테고리) 선택 시 관련 필터 초기화
   * @param {string} feature - 선택할 기능명
   */
  setFeature: (feature) => set({
    selectedFeature: feature,
    searchQuery: '',         // 검색어 초기화
    visibleCount: 3,         // 카드 개수 초기화
    openDropdown: null,      // 드롭다운 닫기
  }),

  /**
   * 검색어 설정
   * @param {string} query - 입력한 검색어
   */
  setSearchQuery: (query) => set({ searchQuery: query }),

  /**
   * 상세보기로 보여줄 리뷰 선택
   * @param {number} reviewId - 선택된 리뷰 ID
   */
  selectReview: (reviewId) => set({ selectedReviewId: reviewId }),

  /**
   * 상세보기 사이드바 닫기
   */
  closeSidebar: () => set({ selectedReviewId: null }),

  /**
   * 더보기 버튼 클릭 시, 보여줄 카드 개수 증가
   */
  loadMore: () => set((state) => ({ visibleCount: state.visibleCount + 3 })),

  /**
   * 특정 드롭다운을 열거나 닫기
   * @param {'project' | 'feature' | null} dropdownName - 열거나 닫을 드롭다운 이름
   */
  toggleDropdown: (dropdownName) => set((state) => ({
    openDropdown: state.openDropdown === dropdownName ? null : dropdownName,
  })),

  /**
   * 모든 드롭다운 닫기
   */
  closeDropdowns: () => set({ openDropdown: null }),
}));
