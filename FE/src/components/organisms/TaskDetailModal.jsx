import ModalOverlay from '../molecules/ModalOverlay'
import ModalContainer from '../molecules/ModalContainer'
import TaskDetailContent from '@/components/molecules/TaskDetailContent'
import CloseButton from '../atoms/CloseButton'

const TaskDetailModal = ({ data, onClose }) => {
  const task = data?.event
  if (!task) return null

  return (
    <ModalOverlay onClick={onClose}>
      <div onClick={(e) => e.stopPropagation()}>
        <ModalContainer>
          <TaskDetailContent task={task} />
          <CloseButton onClick={onClose} />
        </ModalContainer>
      </div>
    </ModalOverlay>
  )
}

export default TaskDetailModal