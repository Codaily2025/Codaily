import ModalOverlay from '../molecules/ModalOverlay'
import ModalContainer from '../molecules/ModalContainer'
import EventDetailContent from '../molecules/EventDetailContent'
import CloseButton from '../atoms/CloseButton'

const EventDetailModal = ({ data, onClose }) => {
    const event = data?.event
    
  if (!event) return null

  return (
    <ModalOverlay onClick={onClose}>
      <div onClick={(e) => e.stopPropagation()}>
        <ModalContainer>
          <EventDetailContent event={event} />
          <CloseButton onClick={onClose} />
        </ModalContainer>
      </div>
    </ModalOverlay>
  )
}

export default EventDetailModal