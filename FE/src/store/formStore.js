import { create } from 'zustand'

const useFormStore = create((set) => ({
    formData: {
    firstName: "firstName",
    lastName: "lastName",
    email: "user_email@email.com",
    nickname: "user_nickname",
    country: "country",
    city: "city",
    address: "address",
    zipCode: "180000",
  },

  updateField: (field, value) => set((state) => ({
    formData: {
        ...state.formData,
        [field]: value
    }
  })),

  // 추가 메서드 (필요시)
  // ...
}))

export default useFormStore