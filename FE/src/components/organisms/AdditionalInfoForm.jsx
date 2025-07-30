// 여러 InputGroup들이 모인 전체 입력 폼
import React from 'react'
import InputGroup from '@/components/molecules/InputGroup'
import FormRow from '@/components/molecules/FormRow'
import ProfileAvatar from '@/components/atoms/ProfileAvatar'
import SaveButton from '@/components/atoms/SaveButton'
import NicknameCheckButton from '@/components/atoms/NicknameCheckButton'
import useFormStore from '@/store/formStore'

const AdditionalInfoForm = ({ onNicknameCheck }) => {
  const { formData } = useFormStore()

  return (
    <div className='profile-section'>
      <ProfileAvatar />

      <form>
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
        <FormRow>
          <InputGroup 
            label="Email"
            fieldName="email"
            placeholder="example@email.com"
          />
        </FormRow>
        <FormRow>
          <InputGroup 
            label="Address"
            fieldName="address"
            placeholder="user_address"
          />
        </FormRow>
        <FormRow>
          <InputGroup 
            label="Phone"
            fieldName="phone"
            placeholder="000-0000-0000"
          />
        </FormRow>

        <div className='form-actions'>
          <SaveButton />
        </div>
      </form>
    </div>
  ) 
}

export default AdditionalInfoForm