import React from 'react';
import './ReviewCard.css';

const ReviewCard = ({ category, title, details, tags, score, scoreColor, highlight, onCardClick, isSelected }) => {
  const scoreClass = `card-score score-${scoreColor}`;

  // 카드 클릭 시 상세 사이드 페이지를 띄우기 위한 클릭 핸들러
  const handleClick = () => {
    if (onCardClick) {
      onCardClick(); // 카드 id가 필요하다면 prop으로 id를 추가해서 전달
    }
  };

  return (
    <div className={`review-card ${isSelected ? 'selected' : ''}`} onClick={handleClick}>
      <div className="card-info">
        <span className="card-category">{category}</span>
        <span className="card-title">{title}</span>
        <p className="card-details">{details}</p>
        <div className="card-tags">
          {tags.map((tag, index) => (
            <span key={index} className={`tag tag-${tag.type}`}>
              {tag.text}
            </span>
          ))}
        </div>
      </div>
      <div className={scoreClass}>
        {score}점
      </div>
    </div>
  );
};

export default ReviewCard;