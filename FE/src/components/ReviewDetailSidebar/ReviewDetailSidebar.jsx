// FE/src/components/ReviewDetailSidebar/ReviewDetailSidebar.jsx
// 리뷰 상세 사이드바
import React, { useState, useMemo, useEffect } from 'react';
import styles from './ReviewDetailSidebar.module.css';
import { FileIcon, PerformanceIcon, SecurityIcon, ComplexityIcon, BugRiskIcon, RefactoringIcon, RefreshIcon, ChevronDownIcon } from './icons';
import AnalysisCard from './AnalysisCard';
import { useReviewDetail } from '../../queries/reviewQueries';
import { useReviewStore } from '../../stores/reviewStore';

// 리뷰 상세 사이드바
const ReviewDetailSidebar = ({ review, onClose }) => {
  console.log('코드리뷰 목록 페이지에서 넘겨준 리뷰 데이터', review); // detail 정보와 프로젝트 이름 정보는 넘겨 받아서 사용
  // Zustand 스토어에서 선택된 리뷰 ID 가져오기
  const { selectedReviewId, closeSidebar } = useReviewStore();

  // React Query를 사용해 상세 리뷰 데이터 가져오기
  const {
    data: reviewDetail, // API로부터 받아온 데이터, 현재는 dummy data
    isLoading, // 로딩 상태
    isError, // 에러 상태
    isFetching, // 데이터 가져오는 중일 때 표시할 UI
    refetch, // 데이터 다시 가져오기, 수동 호출 시 사용
  } = useReviewDetail(selectedReviewId);

  // 모든 아코디언이 닫혔을 때 사이드바 높이 계산
  const [openMap, setOpenMap] = useState({});
  useEffect(() => {
    if (reviewDetail?.codeReview) {
      const init = {};
      order.forEach(k => {
        if (reviewDetail.codeReview[k]) init[k] = false;
      });
      // Object.keys(reviewDetail.codeReview).forEach(key => {
      //   init[key] = false;
      // });
      setOpenMap(init);
    }
  }, [reviewDetail]);

  // 하나라도 열려 있으면 true
  const anyOpen = Object.values(openMap).some(v => v);

  // 분석 필드 카드 출력 순서
  const order = ['convention', 'performance', 'security', 'complexity', 'bugRisk', 'refactoring'];

  // 얼리 리턴
  // 로딩 중일 때 표시할 UI
  if (isLoading) {
    return <div>로딩 중...</div>;
  }

  // 에러 발생 시 표시할 UI
  if (isError) {
    return <div>에러가 발생했습니다: {isError.message}</div>;
  }

  // 선택된 리뷰가 없거나 데이터를 찾지 못했을 경우 사이드바 렌더링 x
  if (!selectedReviewId || !reviewDetail) {
    return null;
  }

  // 레벨별 태그 개수 세기
  const calculateTagCounts = (issues) => {
    return issues.reduce((acc, issue) => {
      const level = issue.level.toLowerCase(); // hign, medium, low
      acc[level] = (acc[level] || 0) + 1;
      return acc;
    },
      { high: 0, medium: 0, low: 0 } // 레벨별 태그 개수 초기화
    );
  }

  return (
    <aside className={styles.reviewDetailSidebar}
      style={{
        maxHeight: anyOpen ? '3000px' : `721.06px`,
      }}
    >
      <header className={styles.header}>
        <div className={styles.headerContent}>
          <div className={styles.category}>{review.projectName} &gt; {reviewDetail.featureCategory}</div>
          <h2 className={styles.title}>{reviewDetail.reviewName}</h2>
          {/* <div className={styles.meta}>{reviewDetail.reviewDate} · {reviewDetail.filesChanged}개 파일 · {reviewDetail.reviewScore}점</div> */}
          <div className={styles.meta}>{review.details} · {reviewDetail.reviewScore}점</div>
        </div>
        <div className={styles.headerActions}>
          <button
            className={styles.actionButton}
            onClick={() => refetch()}
            disabled={isFetching} // 데이터 가져오는 중일 때 버튼 비활성화
          >
            {/* 로딩 중이면 rotating 클래스 추가*/}
            <RefreshIcon
              className={`${styles.actionIcon} 
                  ${isFetching ? styles.rotating : ''
                }`}
            />
            <span>{isFetching ? '분석 중' : '재분석'}</span>
          </button>
          {/* <button className={styles.iconButton}>
                      <MoreVerticalIcon className={styles.actionIcon} />
                </button> */}
          <button className={styles.closeButton} onClick={closeSidebar}>×</button>
        </div>
      </header>
      <main className={styles.mainContent}>
        {/* 코드 리뷰 카드 렌더링 */}
        {/* {Object.entries(reviewDetail.codeReview) */}
        {order.map(key => [key, reviewDetail?.codeReview?.[key]])
          // 분석 요소가 있고 summary가 있는 경우만 필터링
          .filter(([_, data]) => data && data.summary && data.summary.trim() !== '')
          .map(([itemTitle, data]) => {
            const hasIssues = Array.isArray(data.issues) && data.issues.length > 0;

            // 이슈가 없는 경우 (summary만 있는 경우)
            if (!hasIssues) {
              return (
                <div
                  key={itemTitle}
                  className={styles.card}
                >
                  <div className={styles.cardHeader} style={{ cursor: 'default' }}>
                    <div className={styles.cardTitleGroup}>
                      {itemTitle === 'convention' ? <FileIcon className={styles.cardIcon} /> :
                        itemTitle === 'performance' ? <PerformanceIcon className={styles.cardIcon} /> :
                          itemTitle === 'security' ? <SecurityIcon className={styles.cardIcon} /> :
                            itemTitle === 'complexity' ? <ComplexityIcon className={styles.cardIcon} /> :
                              itemTitle === 'bugRisk' ? <BugRiskIcon className={styles.cardIcon} /> :
                                <RefactoringIcon className={styles.cardIcon} />
                      }
                      <div className={styles.cardTitleText}>
                        <h3 className={styles.cardTitle}>
                          {itemTitle === 'convention' ? '코딩 컨벤션' :
                            itemTitle === 'performance' ? '성능 최적화' :
                              itemTitle === 'security' ? '보안 취약점' :
                                itemTitle === 'complexity' ? '코드 복잡도' :
                                  itemTitle === 'bugRisk' ? '버그 위험도' :
                                    '리팩토링'
                          }
                        </h3>
                        <p className={styles.cardDescription}>{data.summary}</p>
                      </div>
                    </div>
                    <div className={styles.cardControls}>
                      <div className={styles.tagGroup}>
                        {/* 태그를 숨기되 자리는 차지 */}
                      </div>
                      {/* 토글 버튼을 숨기되 자리는 차지 */}
                      <div style={{ visibility: 'hidden' }}>
                        <ChevronDownIcon />
                      </div>
                    </div>
                  </div>
                </div>
              );
            }

            // 이슈가 있는 경우 (기존 로직)
            return (
              <AnalysisCard
                key={itemTitle}
                // 부모가 열림 상태 제어
                isOpen={openMap[itemTitle]}
                // 토글할 때마다 상태 갱신
                onToggle={() => setOpenMap(prev => ({ ...prev, [itemTitle]: !prev[itemTitle] }))}
                icon={
                  itemTitle === 'convention' ? <FileIcon className={styles.cardIcon} /> :
                    itemTitle === 'performance' ? <PerformanceIcon className={styles.cardIcon} /> :
                      itemTitle === 'security' ? <SecurityIcon className={styles.cardIcon} /> :
                        itemTitle === 'complexity' ? <ComplexityIcon className={styles.cardIcon} /> :
                          itemTitle === 'bugRisk' ? <BugRiskIcon className={styles.cardIcon} /> :
                            <RefactoringIcon className={styles.cardIcon} />
                }
                title={
                  itemTitle === 'convention' ? '코딩 컨벤션' :
                    itemTitle === 'performance' ? '성능 최적화' :
                      itemTitle === 'security' ? '보안 취약점' :
                        itemTitle === 'complexity' ? '코드 복잡도' :
                          itemTitle === 'bugRisk' ? '버그 위험도' :
                            '리팩토링'
                }
                description={data.summary}
                tags={calculateTagCounts(data.issues)}
                issues={data.issues}
              // 처음에 열려 있게 할지 닫혀 있게 할지 결정
              // defaultOpen={true}
              />
            );
          })}
      </main>
    </aside>
  );
};

export default ReviewDetailSidebar; 