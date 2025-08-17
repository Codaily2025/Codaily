import ModalOverlay from '../molecules/ModalOverlay'
import ModalContainer from '../molecules/ModalContainer'
import TaskDetailContent from '@/components/molecules/TaskDetailContent'

const TaskDetailModal = ({ data, onClose }) => {
  const task = data?.event
  if (!task) return null

  const buttonStyle = {
    padding: '12px',
    backgroundColor: '#E5E7EB',
    borderRadius: '8px',
    border: 'none',
    cursor: 'pointer',
    fontSize: '16px',
    color: '#303030',
    marginTop: '32px',
    width: '100%',
  }

  return (
    <ModalOverlay onClick={onClose}>
      <div onClick={(e) => e.stopPropagation()}>
        <ModalContainer>
          <TaskDetailContent task={task} />
          <div style={{ display: 'flex', justifyContent: 'center', marginTop: '32px' }}>
            <button style={buttonStyle} onClick={onClose}>
              닫기
            </button>
          </div>
        </ModalContainer>
      </div>
    </ModalOverlay>
  )
}

export default TaskDetailModal