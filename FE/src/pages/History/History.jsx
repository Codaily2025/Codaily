import React from 'react';
import './History.css';
import CodeReview from './CodeReview';
import Retrospective from './Retrospective';
import { useState } from 'react';

const History = () => {
  const [selectedTab, setSelectedTab] = useState('code-review');
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
