import ModalOverlay from '../molecules/ModalOverlay'
import ModalContainer from '../molecules/ModalContainer'
import NicknameCheckContent from '../molecules/NicknameCheckContent'
import CloseButton from '../atoms/CloseButton'

const NicknameCheckModal = ({ data, onClose }) => {

    return (
    <ModalOverlay onClick={onClose}>
      <div onClick={(e) => e.stopPropagation()}>
        <ModalContainer>
          <NicknameCheckContent nickname={data.nickname} />
          <CloseButton onClick={onClose} />
        </ModalContainer>
      </div>
    </ModalOverlay>
    )
}

export default NicknameCheckModal