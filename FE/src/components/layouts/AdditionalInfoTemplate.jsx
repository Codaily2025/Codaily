// 비즈니스 로직과 상태 관리 담당
import React from "react"
import AdditionalInfoForm from "@/components/organisms/AdditionalInfoForm"
import useModalStore from "@/store/modalStore"
import useFormStore from "@/store/formStore"

const AdditionalInfoTemplate = () => {
  const { openModal } = useModalStore()
  const { formData } = useFormStore()

  const handleNicknameCheck = (nickname) => {
    openModal("NICKNAME_CHECK", { nickname })
  }

  return (
    <div className="container">
      <div className="main-content">
        <AdditionalInfoForm
          onNicknameCheck={handleNicknameCheck}
        />
      </div>
    </div>
  )
}

export default AdditionalInfoTemplate