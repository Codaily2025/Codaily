import useModalStore from "../../store/modalStore"
import ProfileEditModal from "../ProfileEditModal"
import ProjectEditModal from "../ProjectEditModal"
import EventDetailModal from './EventDetailModal'
import NicknameCheckModal from './NicknameCheckModal'
import TaskDetailModal from './TaskDetailModal'
import StatusConfirmModal from './StatusConfirmModal'
import { useAuthStore } from "../../stores/authStore"


// modal 타입별 컴포넌트 맵핑
const MODAL_COMPONENTS = {
    EVENT_DETAIL: EventDetailModal,
    NICKNAME_CHECK: NicknameCheckModal,
    PROFILE_EDIT: ProfileEditModal,
    PROJECT_EDIT: ProjectEditModal,
    TASK_DETAIL: TaskDetailModal,
    STATUS_CONFIRM: StatusConfirmModal,
    // CONFIRMATION: ConfirmationModal,
    // DELETION: DeleteModal,
    // ...

}

const ModalManager = () => {
    const { isOpen, modalType, modalData, closeModal } = useModalStore()
    // const { user } = useAuthStore()
    // const userId = user?.userId || 1
    const userId = 1 // 임시로 하드코딩

    // 콘솔 디버깅
    console.log('ModalManager 상태:', { isOpen, modalType, modalData })

    if (!isOpen || !modalType) {
        console.log('모달이 열리지 않았거나 type 확인 X')
        return null
    }

    const ModalComponent = MODAL_COMPONENTS[modalType]
    console.log('Modal 컴포넌트:', ModalComponent)

    if (!ModalComponent) {
        console.log(`모달 없엉: ${modalType}`)
        return null
    }

    console.log('렌더링되는 Modal 컴포넌트:', modalType)

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