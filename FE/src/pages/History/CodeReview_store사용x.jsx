import React, { useState, useEffect, useRef, useMemo } from 'react';
import ReviewCard from '../../components/ReviewCard';
import ReviewDetailSidebar from '../../components/ReviewDetailSidebar';
import { useReviewStore } from '../../stores/reviewStore'; // Zustand 스토어
import { useReviews, useProjectOptions } from '../../queries/reviewQueries';
import './CodeReview.css';
import caretUp from '../../assets/caret_up.svg';

// 프로젝트 데이터 예시 
const reviews = [
  {
    projectId: 1,
    category: '회원 관리',
    title: '일반 로그인 기능 구현',
    details: '2025/07/28 14:32 · 3개 파일',
    tags: [
      { text: '중간 1', type: 'medium' },
      { text: '낮음 4', type: 'low' }
    ],
    score: 61,
    scoreColor: 'orange'
  },
  {
    projectId: 2,
    category: '회원 관리',
    title: '소셜 로그인 기능 구현',
    details: '2025/07/28 14:32 · 3개 파일',
    tags: [
      { text: '높음 4', type: 'high' },
      { text: '중간 3', type: 'medium' },
      { text: '낮음 4', type: 'low' }
    ],
    score: 30,
    scoreColor: 'red'
  },
  {
    projectId: 3,
    category: '회원 관리',
    title: '회원 탈퇴 기능 구현',
    details: '2025/07/28 14:32 · 3개 파일',
    tags: [{ text: '낮음 4', type: 'low' }],
    score: 91,
    scoreColor: 'green'
  },
  {
    projectId: 3,
    category: '배포',
    title: 'AWS 서버 구현',
    details: '2025/07/28 14:32 · 3개 파일',
    tags: [
        { text: '높음 4', type: 'high' },
        { text: '중간 3', type: 'medium' },
        { text: '낮음 4', type: 'low' }
    ],
    score: 30,
    scoreColor: 'red',
    highlight: false // 두 번째 줄 카드들은 highlight가 없음
  },
  {
    projectId: 1,
    category: '배포',
    title: 'CI/CD 연결',
    details: '2025/07/28 14:32 · 3개 파일',
    tags: [{ text: '낮음 4', type: 'low' }],
    score: 91,
    scoreColor: 'green',
    highlight: false
  },
  // ... 다른 프로젝트 데이터
];

const projectOptions = [
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

const CodeReview = () => {
  const [selectedProjectOption, setSelectedProjectOption] = useState('모든 프로젝트');
  const [selectedFeatureOption, setSelectedFeatureOption] = useState('전체');
  const [openDropdown, setOpenDropdown] = useState(null); // 'project' | 'feature' | null

  const dropdownRef = useRef(null); // 드롭다운 참조 -> 외부 클릭 시 드롭다운 닫기

  // 프로젝트 선택 시 기능 옵션 업데이트
  const currentProject = projectOptions.find(p => p.name === selectedProjectOption);
  const featureOptions = currentProject?.features || ['전체'];

  // 검색 기능 상태
  const [searchQuery, setSearchQuery] = useState('');

  // 외부 클릭 시 드롭다운 닫기
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setOpenDropdown(null);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // 실제로 드롭다운 필터링 구현하기
  // 필터링된 리뷰들
  const filteredReviews = reviews.filter((review) => {
      
    const projectMatched = // 드롭다운에서 필터링된 프로젝트 옵션 카드들
      selectedProjectOption === '모든 프로젝트' ||
      currentProject?.id === review.projectId; // 프로젝트 옵션 선택 시 프로젝트 카드 필터링
    
    const featureMatched =
      selectedFeatureOption === '전체' || 
      review.category === selectedFeatureOption;

      const titleMatched = review.title
      .toLowerCase()
      .includes(searchQuery.toLowerCase());

      // 필터링 영향 x : 검색어가 있을 때는 검색 결과만 필터링
      // if (searchQuery.trim() !== '') {
      //  return titleMatched;
      //}

      return projectMatched && featureMatched && titleMatched;
    });


  // 필터 변경 시 visibleCount 초기화
  useEffect(() => {
    setVisibleCount(3);
  }, [selectedProjectOption, selectedFeatureOption]);

  // 필터 변경 시 자동 초기화
  useEffect(() => {
    setVisibleCount(3);
    setSearchQuery(''); // 검색창 입력 초기화
    setSelectedFeatureOption('전체');
  }, [selectedProjectOption]);
  useEffect(() => {
    setSearchQuery(''); // 검색창 입력 초기화
  }, [selectedFeatureOption]);

  // 리뷰 선택 상태
  const [selectedReview, setSelectedReview] = useState(null);

  // 카드 클릭 시 상세 사이드 페이지 띄우기
  const handleCardClick = (review) => {
    setSelectedReview(review);
  };

  // 상세 사이드 페이지 닫기
  const handleCloseSidebar = () => {
    setSelectedReview(null);
  };

  // 더보기 버튼: 더 볼 카드 개수 관리
  const [visibleCount, setVisibleCount] = useState(3);

  // 더보기 버튼 클릭 시 카드 개수 증가
  const handleLoadMore = () => {
    setVisibleCount(prevCount => prevCount + 3);
  };

  return (      
  <>
    <div className="controls" ref={dropdownRef}>
      <div className="filters">
        <div className="dropdown-wrapper">
          <button 
          className={`filter-button ${
            openDropdown === 'project' ? 'open' : ''
          }`}
          // 프로젝트 드롭다운 열기
          // 프로젝트 드롭다운 열려있을 때 닫기(null로 변경)
          onClick={() =>
            setOpenDropdown(openDropdown === 'project' ? null : 'project')
          }
        >
          {selectedProjectOption} 
          <img className="filter-icon" src={caretUp} alt="caret" />
          </button>
          {openDropdown === 'project' && (
          <div className="dropdown-menu">
            {projectOptions.map((option) => (
              <div
                key={option.id}
                className={`dropdown-option ${option.name === selectedProjectOption ? 'selected' : ''}`}
                onClick={() => {
                  setSelectedProjectOption(option.name); // 클릭하면 프로젝트 옵션 변경
                  setOpenDropdown(null); // 드롭다운 닫기
                }}
              >
                <div className="option-icon">
                  {option.name === selectedProjectOption && (
                    <svg
                      width="16"
                      height="16"
                      viewBox="0 0 16 16"
                      fill="none"
                      xmlns="http://www.w3.org/2000/svg"
                    >
                      <path
                        d="M13.3346 4L6.0013 11.3333L2.66797 8"
                        stroke="#5A597D"
                        strokeWidth="1.33333"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                      />
                    </svg>
                  )}
                </div>
                <span className="option-label">{option.name}</span>
              </div>
            ))}
          </div>
          )}
        </div>
        {/* 기능 드롭다운 */}
        <div className="dropdown-wrapper">
          <button 
            className={`filter-button ${
              openDropdown === 'feature' ? 'open' : ''
            }`}
            
            onClick={() =>
              setOpenDropdown(openDropdown === 'feature' ? null : 'feature')
            }
          >
            {selectedFeatureOption} 
            <img className="filter-icon" src={caretUp} alt="caret" />
          </button>
        {/* 기능 드롭다운 메뉴 */}
        {openDropdown === 'feature' && (
          <div className="dropdown-menu small">
            {featureOptions.map((option) => (
              <div
                key={option}
                className={`dropdown-option ${option === selectedFeatureOption ? 'selected' : ''}`}
                onClick={() => {
                  setSelectedFeatureOption(option);
                  setOpenDropdown(null);
                }}
              >
                <div className="option-icon">
                  {option === selectedFeatureOption && (
                    <svg
                      width="16"
                      height="16"
                      viewBox="0 0 16 16"
                      fill="none"
                      xmlns="http://www.w3.org/2000/svg"
                    >
                      <path
                        d="M13.3346 4L6.0013 11.3333L2.66797 8"
                        stroke="#5A597D"
                        strokeWidth="1.33333"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                      />
                    </svg>
                  )}
                </div>
                <span className="option-label">{option}</span>
              </div>
            ))}
          </div>
        )}
        </div>
      </div>
      {/* 검색창 */}
      <div className="search-bar">
        <input 
          type="text" 
          // placeholder="상세기능명 조회"
          placeholder="코드리뷰명 조회"
          value={searchQuery} // 검색 쿼리 상태
          onChange={(e) => setSearchQuery(e.target.value)} // 검색 쿼리 변경 핸들러
           />
        <div className="search-icon"></div>
      </div>
    </div>
    
    {/* 카드 목록 */}
    <div className={`review-container ${selectedReview ? 'with-sidebar' : ''}`}>
      {!selectedReview ? (
        // 카드가 선택되지 않았을 때: 3개씩 가로로 나열
        <div className="review-grid">
          {Array.from({ length: Math.ceil(visibleCount / 3) }).map((_, rowIndex) => (
            <div className="review-row" key={rowIndex}>
              {filteredReviews
                .slice(rowIndex * 3, rowIndex * 3 + 3)
                .map((review, index) => (
                  <ReviewCard
                    key={rowIndex * 3 + index}
                    {...review}
                    onCardClick={() => handleCardClick(review)}
                  />
                ))}
            </div>
          ))}

          {visibleCount < filteredReviews.length && (
            <button className="load-more-button large" onClick={handleLoadMore}>
              더보기
            </button>
          )}
        </div>

      ) : (
        // 카드가 선택되었을 때: 세로로 나열 + 사이드 페이지(카드 상세)
        <>
          <div className="review-list">
            {filteredReviews.slice(0, visibleCount).map((review, index) => (
              <ReviewCard 
                key={index}
                {...review} 
                isSelected={selectedReview === review}
                onCardClick={() => handleCardClick(review)}
              />
            ))}

            {visibleCount < filteredReviews.length && (
              <button className="load-more-button small" onClick={handleLoadMore}>
                더보기
              </button>
            )}
          </div>          
          <ReviewDetailSidebar 
            review={selectedReview} 
            onClose={handleCloseSidebar}
          />
        </>
      )}
    </div>
  </>
  );
};

export default CodeReview;