// 비즈니스 로직과 상태 관리 담당
import React, { useState } from "react"
import AdditionalInfoForm from "@/components/organisms/AdditionalInfoForm"
import useModalStore from "@/store/modalStore"

const AdditionalInfoTemplate = () => {
  const { openModal } = useModalStore()

  const [formData, setFormData] = useState({
    firstName: "firstName",
    lastName: "lastName",
    email: "user_email@email.com",
    nickname: "user_nickname",
    country: "country",
    city: "city",
    address: "address",
    zipCode: "180000",
  })

  const handleInputChange = (field, value) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }))
  }

  const handleNicknameCheck = (nickname) => {
    openModal("NICKNAME_CHECK", { nickname })
  }

  const handleSave = (e) => {
    e.preventDefault()
    // 여따 로직 추가
    console.log("Saving form data:", formData);
  }

  return (
    <div className="container">
      <div className="main-content">
        <AdditionalInfoForm
          formData={formData}
          onInputChange={handleInputChange}
          onNicknameCheck={handleNicknameCheck}
          onSave={handleSave}
        />
      </div>
    </div>
  )
}

export default AdditionalInfoTemplate
