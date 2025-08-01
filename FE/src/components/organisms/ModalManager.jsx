import useModalStore from "../../store/modalStore"
import ProfileEditModal from "../ProfileEditModal"
import ProjectEditModal from "../ProjectEditModal"
import EventDetailModal from './EventDetailModal'
import NicknameCheckModal from './NicknameCheckModal'

// modal 타입별 컴포넌트 맵핑
const MODAL_COMPONENTS = {
    EVENT_DETAIL: EventDetailModal,
    NICKNAME_CHECK: NicknameCheckModal,
    PROFILE_EDIT: ProfileEditModal,
    PROJECT_EDIT: ProjectEditModal,
    // CONFIRMATION: ConfirmationModal,
    // DELETION: DeleteModal,
    // ...

}

const ModalManager = () => {
    const { isOpen, modalType, modalData, closeModal } = useModalStore()

    if (!isOpen || !modalType) return null

    const ModalComponent = MODAL_COMPONENTS[modalType]

    if (!ModalComponent) {
        console.log(`모달 없엉: ${modalType}`)
        return null
    }

    return (
        <ModalComponent 
            data={modalData}
            onClose={closeModal}
        />
    )
}

export default ModalManager