// 여러 InputGroup들이 모인 전체 입력 폼
import React from 'react'
import InputGroup from '@/components/molecules/InputGroup'
import FormRow from '@/components/molecules/FormRow'
import ProfileAvatar from '@/components/atoms/ProfileAvatar'
import SaveButton from '@/components/atoms/SaveButton'
import NicknameCheckButton from '@/components/atoms/NicknameCheckButton'
import useFormStore from '@/store/formStore'

const AdditionalInfoForm = ({ onNicknameCheck, onSave }) => {
  const { formData } = useFormStore()

  return (
    <div className='profile-section'>
      <ProfileAvatar />

      <form onSubmit={onSave}>
        <FormRow>
          <InputGroup 
            label="First Name"
            fieldName="firstName"
            placeholder="firstName"
          />
          <InputGroup 
            label="Last Name"
            fieldName="lastName"
            placeholder="lastName"
          />
        </FormRow>

        <FormRow>
          <InputGroup 
            label="Nickname"
            fieldName="nickname"
            placeholder="user_nickname"
          >
            <NicknameCheckButton onClick={() => onNicknameCheck(formData.nickname)} />
          </InputGroup>
        </FormRow>

        <div className='form-actions'>
          <SaveButton />
        </div>
      </form>
    </div>
  ) 
}

export default AdditionalInfoForm