import React from 'react';
import './History.css';
import CodeReview from './CodeReview';
import Retrospective from './Retrospective';
import { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';

const History = () => {
  const location = useLocation();
  const [selectedTab, setSelectedTab] = useState('code-review');
  
  // 작성자: yeongenn
  // url 쿼리 파라미터에서 탭 정보 읽기
  useEffect(() => {
    const urlParams = new URLSearchParams(location.search);
    const tabFromUrl = urlParams.get('tab');
    
    if (tabFromUrl === 'retrospective') {
      setSelectedTab('retrospective');
    } else if (tabFromUrl === 'code-review') {
      setSelectedTab('code-review');
    }
  }, [location.search]);
  // ---
  
  const handleTabClick = (tab) => {
    setSelectedTab(tab);
  };
  return (
    <main className="review-section">
      <div className="tabs">
        <button 
          className={`tab-button ${selectedTab === 'code-review' ? 'active' : ''}`} 
          onClick={() => handleTabClick('code-review')}
        >
          코드리뷰
        </button>
        <button 
          className={`tab-button ${selectedTab === 'retrospective' ? 'active' : ''}`} 
          onClick={() => handleTabClick('retrospective')}
        >
          회고
        </button>
      </div>
      {selectedTab === 'code-review' && <CodeReview />}
      {selectedTab === 'retrospective' && <Retrospective />}
    </main>
  );
};

export default History; 
