import React, { useEffect, useRef, useMemo } from 'react';
import ReviewCard from '../../components/ReviewCard';
import ReviewDetailSidebar from '../../components/ReviewDetailSidebar/ReviewDetailSidebar';
import { useReviewStore } from '../../stores/reviewStore'; // Zustand 전역 상태 훅
import { useReviews, useProjectOptions } from '../../queries/reviewQueries'; // React Query 데이터 훅
import './CodeReview.css';
import caretUp from '../../assets/caret_up.svg';

const CodeReview = () => {
  // React Query를 통해 리뷰 데이터와 프로젝트 옵션을 가져옴
  const { data: reviews = [], isLoading: isLoadingReviews } = useReviews();
  const { data: projectOptions = [], isLoading: isLoadingProjects } = useProjectOptions();

  // Zustand 상태 및 액션 가져오기
  const {
    selectedProjectId,
    selectedFeature,
    searchQuery,
    selectedReviewId,
    visibleCount,
    openDropdown,
    setProject,
    setFeature,
    setSearchQuery,
    selectReview,
    closeSidebar,
    loadMore,
    toggleDropdown,
    closeDropdowns,
  } = useReviewStore();

  const dropdownRef = useRef(null);

  // 드롭다운 외부 클릭 시 닫기 위한 이벤트 등록
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        closeDropdowns();
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [closeDropdowns]);

  // 파생 상태 계산 (성능 향상을 위해 useMemo 사용)
  const {
    currentProject,
    featureOptions,
    filteredReviews,
    selectedReview,
    selectedProjectName
  } = useMemo(() => {
    const currentProject = projectOptions.find(p => p.id === selectedProjectId) || projectOptions[0];
    const featureOptions = currentProject?.features || ['전체'];

    const filtered = reviews.filter((review) => {
      const projectMatched = selectedProjectId === null || review.projectId === selectedProjectId;
      const featureMatched = selectedFeature === '전체' || review.category === selectedFeature;
      const titleMatched = review.title.toLowerCase().includes(searchQuery.toLowerCase());
      return projectMatched && featureMatched && titleMatched;
    });

    const selectedReview = reviews.find(r => r.id === selectedReviewId) || null;
    const selectedProjectName = currentProject?.name || '모든 프로젝트';

    return { currentProject, featureOptions, filteredReviews: filtered, selectedReview, selectedProjectName };
  }, [reviews, projectOptions, selectedProjectId, selectedFeature, searchQuery, selectedReviewId]);

  // 로딩 상태 처리
  if (isLoadingReviews || isLoadingProjects) {
    return <div>로딩 중...</div>;
  }

  // 더보기 버튼 클릭 시 카드 개수 증가
  const handleLoadMore = () => {
    loadMore();
  };

  // 카드 클릭 시 상세 사이드 페이지를 띄우기 위한 클릭 핸들러
  const handleCardClick = (review) => {
    selectReview(review.id);
  };

  // 상세 사이드 페이지 닫기
  const handleCloseSidebar = () => {
    closeSidebar();
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
            toggleDropdown(openDropdown === 'project' ? null : 'project')
          }
        >
          {selectedProjectName} 
          <img className="filter-icon" src={caretUp} alt="caret" />
          </button>
          {openDropdown === 'project' && (
          <div className="dropdown-menu">
            {projectOptions.map((option) => (
              <div
                key={option.id}
                className={`dropdown-option ${option.name === selectedProjectName ? 'selected' : ''}`}
                onClick={() => {
                  setProject(option.id); // 클릭하면 프로젝트 옵션 변경
                  toggleDropdown(null); // 드롭다운 닫기
                }}
              >
                <div className="option-icon">
                  {option.name === selectedProjectName && (
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
              toggleDropdown(openDropdown === 'feature' ? null : 'feature')
            }
          >
            {selectedFeature} 
            <img className="filter-icon" src={caretUp} alt="caret" />
          </button>
        {/* 기능 드롭다운 메뉴 */}
        {openDropdown === 'feature' && (
          <div className="dropdown-menu small">
            {featureOptions.map((option) => (
              <div
                key={option}
                className={`dropdown-option ${option === selectedFeature ? 'selected' : ''}`}
                onClick={() => {
                  setFeature(option);
                  toggleDropdown(null);
                }}
              >
                <div className="option-icon">
                  {option === selectedFeature && (
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
      {filteredReviews.length === 0 ? (
        <div className="no-reviews-container">
          <div className="no-reviews-content">
            <p className="no-reviews-title">생성한 코드 리뷰가 없어요.</p>
            <p className="no-reviews-subtitle">프로젝트의 기능을 구현하고 코드 리뷰를 받아보세요.</p>
            {/* <button className="create-project-btn" onClick={() => handleCreateProject()}>프로젝트 생성하기</button> */}
          </div>
        </div>
        ) : !selectedReview ? (
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
