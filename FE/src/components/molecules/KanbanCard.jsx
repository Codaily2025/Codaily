import React from 'react'

const KanbanCard = ({ category, title, details, dueDate, onClick }) => {
  return (
    <div className="kanban-card" onClick={onClick}>
      <div className="kanban-card-info">
        <span className="kanban-card-category">{category}</span>
        <span className="kanban-card-title">{title}</span>
        <p className="kanban-card-details">{details}</p>
        <div className="kanban-card-footer">
          <span className="kanban-card-date">마지막 작업 일자: {dueDate}</span>
        </div>
      </div>
    </div>
  )
}

export default KanbanCard