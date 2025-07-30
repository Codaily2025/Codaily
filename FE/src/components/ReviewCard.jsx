import React from 'react';
import './ReviewCard.css';

const ReviewCard = ({ category, title, details, tags, score, scoreColor, highlight }) => {
  const scoreClass = `card-score score-${scoreColor}`;

  return (
    <div className="review-card">
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