import Badge from "@/components/atoms/Badge"
import CategoryLabel from "@/components/atoms/CategoryLabel"
import Title from "@/components/atoms/Title"
import Text from "@/components/atoms/Text"
import './TaskDetailContent.css'

const TaskDetailContent = ({ task }) => {
  return (
    <div className="task-detail-content">
      <div className="task-meta-header">
      <Badge content={task?.status || 'In Progress'} color='red' />
      <CategoryLabel className="meta-label">{task?.category}</CategoryLabel>
      </div>
      <Title className="task-title">{task?.title}</Title>
      <ul className="task-detail-list">
        <li><Text className="task-detail-text">{task?.details}</Text></li>
        <li><Text className="task-detail-text">마지막 작업 일자: {task?.dueDate}</Text></li>
      </ul>
    </div>
  )
}

export default TaskDetailContent