import React from 'react';
import './ReviewDetailSidebar.css';

const ReviewDetailSidebar = ({ review, onClose }) => {
  if (!review) return null;

  return (
    <div className="review-detail-sidebar">
      <div className="sidebar-header">
        <h2>코드 리뷰 상세</h2>
        <button className="close-button" onClick={onClose}>×</button>
      </div>
      
      <div className="sidebar-content">
        <div className="detail-section">
          <div className="detail-category">{review.category}</div>
          <h3 className="detail-title">{review.title}</h3>
          <p className="detail-info">{review.details}</p>
        </div>

        <div className="detail-section">
          <h4>점수</h4>
          <div className={`detail-score score-${review.scoreColor}`}>
            {review.score}점
          </div>
        </div>

        <div className="detail-section">
          <h4>태그</h4>
          <div className="detail-tags">
            {review.tags.map((tag, index) => (
              <span key={index} className={`tag tag-${tag.type}`}>
                {tag.text}
              </span>
            ))}
          </div>
        </div>

        <div className="detail-section">
          <h4>리뷰 내용</h4>
          <div className="review-content">
            <p>여기에 실제 리뷰 내용이 들어갑니다. 코드 품질, 개선 사항, 보안 이슈 등에 대한 상세한 피드백을 제공합니다.</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ReviewDetailSidebar; 