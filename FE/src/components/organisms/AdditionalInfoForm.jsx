// 여러 InputGroup들이 모인 전체 입력 폼
import React from 'react'
import InputGroup from '@/components/molecules/InputGroup'
import FormRow from '@/components/molecules/FormRow'
import ProfileAvatar from '@/components/atoms/ProfileAvatar'
import SaveButton from '@/components/atoms/SaveButton'
import NicknameCheckButton from '@/components/atoms/NicknameCheckButton'

const AdditionalInfoForm = ({ formData, onInputChange, onNicknameCheck, onSave }) => {
    return (
        <div className='profile-section'>
            <ProfileAvatar />

            <form onSubmit={onSave}>
                <FormRow>
                    <InputGroup 
                        label="First Name"
                        inputProps={{
                            value: formData.firstName,
                            placeholder: "firstName",
                            onChange: (e) => onInputChange('firstName', e.target.value)
                        }}
                    />
                    <InputGroup 
                        label="Last Name"
                        inputProps={{
                            value: formData.lastName,
                            placeholder: "lastName",
                            onChange: (e) => onInputChange('lastName', e.target.value)
                        }}
                    />
                </FormRow>

                <FormRow>
                    <InputGroup 
                        label="Nickname"
                        // className="form-group full-width"
                        inputProps={{
                            value: formData.nickname,
                            placeholder: "user_nickname",
                            onChange: (e) => onInputChange('nickname', e.target.value)
                        }}
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