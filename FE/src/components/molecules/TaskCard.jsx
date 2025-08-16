import React from 'react'

const TaskCard = ({ category, title, details, tags, score, scoreColor }) => {
  const scoreClass = `card-score score-${scoreColor}`
  
  // 태그 타입을 CSS 클래스명에 맞게 매핑하는 함수
  const getTagClass = (type) => {
    switch (type) {
      case '높음':
        return 'high';
      case '중간':
        return 'medium';
      case '낮음':
        return 'low';
      default:
        return 'medium'; // 기본값
    }
  };
  
  return (
    <div className="task-card">
      <div className="card-info">
        <span className="card-category">{category}</span>
        <span className="card-title">{title}</span>
        <p className="card-details">{details}</p>
        <div className="card-tags">
          {tags.map((tag, index) => (
            <span key={index} className={`tag tag-${getTagClass(tag.type)}`}>
              {tag.text}
            </span>
          ))}
        </div>
      </div>
    </div>
  )
}

export default TaskCard