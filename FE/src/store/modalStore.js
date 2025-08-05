import { create } from 'zustand'

const useModalStore = create((set) => ({
    // modal 상태
    isOpen: false,
    modalType: null,
    modalData: null,

    // modal 열기
    openModal: (type, data = null) => set({
        isOpen: true,
        modalType: type,
        modalData: data
    }),

    // modal 닫기
    closeModal: () => set({
        isOpen: false,
        modalType: null,
        modalData: null
    }),

    // modal 데이터 업데이트
    updateModalData: (data) => set((state) => ({
        modalData: { ...state.modalData, ...data }
    }))
}))

export default useModalStore