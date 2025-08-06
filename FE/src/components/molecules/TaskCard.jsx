import React from 'react'

const TaskCard = ({ category, title, details, tags, score, scoreColor }) => {
  const scoreClass = `card-score score-${scoreColor}`
  
  return (
    <div className="task-card">
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
    </div>
  )
}

export default TaskCard