import Button from '@/components/atoms/Button'
import ModalOverlay from '../molecules/ModalOverlay'
import ModalContainer from '../molecules/ModalContainer'
import EventDetailContent from '../molecules/EventDetailContent'

const EventDetailModal = ({ data, onClose }) => {
  const event = data?.event
  console.log(event)

  if (!event) return null

  const buttonStyle = {
    backgroundColor: '#e9ecef',
    color: '#6c757d',
    marginTop: '60px',
    padding: '12px 24px',
    fontSize: '16px',
    fontWeight: 500,
    borderRadius: '12px',
    width: '100%',
    border: 'none',
    cursor: 'pointer'
  }


  return (
    <ModalOverlay onClick={onClose}>
      <div onClick={(e) => e.stopPropagation()}>
        <ModalContainer>
          <EventDetailContent
            event={event}
          />
          <Button
            onClick={onClose}
            style={buttonStyle}
          >
            닫기
          </Button>
        </ModalContainer>
      </div>
    </ModalOverlay>
  )
}

export default EventDetailModal