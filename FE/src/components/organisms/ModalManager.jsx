import useModalStore from "../../store/modalStore"
import ProfileEditModal from "../ProfileEditModal"
import ProjectEditModal from "../ProjectEditModal"
import EventDetailModal from './EventDetailModal'
import NicknameCheckModal from './NicknameCheckModal'
import TaskDetailModal from './TaskDetailModal'
import { useAuthStore } from "../../stores/authStore"


// modal 타입별 컴포넌트 맵핑
const MODAL_COMPONENTS = {
    EVENT_DETAIL: EventDetailModal,
    NICKNAME_CHECK: NicknameCheckModal,
    PROFILE_EDIT: ProfileEditModal,
    PROJECT_EDIT: ProjectEditModal,
    TASK_DETAIL: TaskDetailModal,
    // CONFIRMATION: ConfirmationModal,
    // DELETION: DeleteModal,
    // ...

}

const ModalManager = () => {
    const { isOpen, modalType, modalData, closeModal } = useModalStore()
    // const { user } = useAuthStore()
    // const userId = user?.userId || 1
    const userId = 1 // 임시로 하드코딩

    if (!isOpen || !modalType) return null

    const ModalComponent = MODAL_COMPONENTS[modalType]

    if (!ModalComponent) {
        console.log(`모달 없엉: ${modalType}`)
        return null
    }

    // ProjectEditModal에는 userId도 전달
    if (modalType === 'PROJECT_EDIT') {
        return (
            <ModalComponent 
                data={modalData}
                onClose={closeModal}
                userId={userId}
            />
        )
    }

    return (
        <ModalComponent 
            data={modalData}
            onClose={closeModal}
        />
    )
}

export default ModalManager