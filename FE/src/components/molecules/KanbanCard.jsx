import React, { memo } from 'react'
import { useDrag } from 'react-dnd'

const KanbanCard = memo(({ 
  featureId, 
  category, 
  title, 
  description, 
  field, 
  estimatedTime, 
  priorityLevel, 
  status,
  cardClassName = '',
  infoClassName = '',
  categoryClassName = '',
  titleClassName = '',
  detailsClassName = '',
  footerClassName = '',
  fieldClassName = '',
  timeClassName = '',
  priorityClassName = '',
  onClick 
}) => {
  const [{ isDragging }, drag] = useDrag({
    type: 'KANBAN_CARD',
    item: { featureId, title, status },
    collect: (monitor) => ({
      isDragging: monitor.isDragging(),
    }),
  })

  const dragCardClass = `${cardClassName} ${isDragging ? 'dragging' : ''}`

  return (
    <div ref={drag} className={dragCardClass} onClick={onClick}>
      <div className={infoClassName}>
        <span className={categoryClassName}>{category}</span>
        <span className={titleClassName}>{title}</span>
        <p className={detailsClassName}>{description}</p>
        <div className={footerClassName}>
          <span className={fieldClassName}>{field}</span>
          <span className={timeClassName}>예상 시간: {estimatedTime}h</span>
          <span className={priorityClassName}>우선순위: {priorityLevel}</span>
        </div>
      </div>
    </div>
  )
})

export default KanbanCard