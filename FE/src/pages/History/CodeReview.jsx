import React from 'react';
import ReviewCard from '../../components/ReviewCard';
import './CodeReview.css';
import caretUp from '../../assets/caret_up.svg';

// 프로젝트 데이터 예시 
const reviews = [
  {
    category: '회원',
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
    category: '회원',
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
    category: '회원',
    title: '회원 탈퇴 기능 구현',
    details: '2025/07/28 14:32 · 3개 파일',
    tags: [{ text: '낮음 4', type: 'low' }],
    score: 91,
    scoreColor: 'green'
  },
  {
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


const CodeReview = () => {
  return (      
      <main className="main-content">
        <div className="tabs">
          <button className="tab-button active">코드리뷰</button>
          <button className="tab-button">회고</button>
        </div>
        
        <div className="controls">
          <div className="filters">
            <button className="filter-button">
              모든 프로젝트 <img src={caretUp} alt="caret" />
            </button>
            <button className="filter-button">
              전체 <img src={caretUp} alt="caret" />
            </button>
          </div>
          <div className="search-bar">
            <input type="text" placeholder="기능명 조회" />
            <div className="search-icon"></div>
          </div>
        </div>
        
        <div className="review-grid">
           {/* 첫 번째 행 */}
          <div className="review-row">
            <ReviewCard {...reviews[0]} highlight={false} />
            <ReviewCard {...reviews[1]} highlight={false} />
            <ReviewCard {...reviews[2]} highlight={false} />
          </div>
           {/* 두 번째 행 */}
          <div className="review-row">
            <ReviewCard {...reviews[0]} highlight={true} /> {/* 예시로 첫 번째 카드에 highlight 적용 */}
            <ReviewCard {...reviews[3]} highlight={false} />
            <ReviewCard {...reviews[4]} highlight={false} />
          </div>
        </div>

        <button className="load-more-button">더보기</button>
      </main>
  );
};

export default CodeReview;