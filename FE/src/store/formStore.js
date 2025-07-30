import { create } from 'zustand'

const useFormStore = create((set, get) => ({
    // TODO: api 확인 후 필드 수정 예정
    formData: {
    firstName: "",
    lastName: "",
    email: "",
    nickname: "",
    address: "",
    phone: "",
  },

  updateField: (field, value) => set((state) => ({
    formData: {
        ...state.formData,
        [field]: value
    }
  })),

  handleSave: (e) => {
    if (e) e.preventDefault()
    const { formData } = get()
    console.log('사용자 입력값: ', formData)
  },

}))

export default useFormStore