import React, { useState } from 'react'
import ProfileAvatar from '@/components/atoms/ProfileAvatar'
import FormRow from '@/components/molecules/FormRow'
import InputGroup from '@/components/molecules/InputGroup'
import NicknameInputGroup from '@/components/molecules/NicknameInputGroup'
import GithubAccountInputGroup from '@/components/molecules/GithubAccountInputGroup'
// import AddressSection from '@/components/molecules/AddressSection'
import useModalStore from '@/store/modalStore'
import styles from './SignupForm.module.css'

const SignupForm = () => {
    const { openModal } = useModalStore()
    
    const [formData, setFormData] = useState({
        firstName: "",
        lastName: "",
        email: "",
        nickname: "",
        country: "",
        city: "",
        address: "",
        zipCode: "",
        phone: "",
    })

    const updateField = (field, value) => {
        setFormData(prev => ({
            ...prev,
            [field]: value
        }))
    }

    const handleSave = (e) => {
        if (e) e.preventDefault()
        console.log('사용자 입력값: ', formData)
    }

    const handleNicknameCheck = () => {
        openModal('NICKNAME_CHECK', {
            nickname: formData.nickname || 'user_nickname'
        })
    }

    const handleAvatarEdit = () => {
        console.log('Edit avatar clicked')
    }

    return (
        <div className={styles.profileSection}>
            <ProfileAvatar 
                src={null}
                editable={true}
                onEdit={handleAvatarEdit}
                className={styles.profileAvatarSection}
                avatarClassName={styles.profileAvatar}
                iconClassName={styles.cameraIcon}
            />

            <form>
                {/* <FormRow className={styles.formRow}>
                    <InputGroup 
                        label="First Name"
                        fieldName="firstName"
                        placeholder="First Name"
                        value={formData.firstName}
                        onChange={(e) => updateField('firstName', e.target.value)}
                        className={styles.formGroup}
                        labelClassName={styles.formLabel}
                        inputClassName={styles.formInput}
                    />
                    <InputGroup 
                        label="Last Name"
                        fieldName="lastName"
                        placeholder="Last Name"
                        value={formData.lastName}
                        onChange={(e) => updateField('lastName', e.target.value)}
                        className={styles.formGroup}
                        labelClassName={styles.formLabel}
                        inputClassName={styles.formInput}
                    />
                </FormRow> */}

                <FormRow className={styles.formRow}>
                    <InputGroup 
                        label="Email Address"
                        fieldName="email"
                        placeholder="소셜 로그인에 사용한 이메일로 자동 설정"
                        type="email"
                        value={formData.email}
                        onChange={(e) => updateField('email', e.target.value)}
                        className={styles.formGroupFullWidth}
                        labelClassName={styles.formLabel}
                        inputClassName={styles.formInput}
                    />
                </FormRow>

                <FormRow className={styles.formRow}>
                    <NicknameInputGroup 
                        fieldName="nickname"
                        placeholder="user_nickname"
                        value={formData.nickname}
                        onChange={(e) => updateField('nickname', e.target.value)}
                        onNicknameCheck={handleNicknameCheck}
                        className={styles.formGroup}
                        labelClassName={styles.formLabel}
                        inputClassName={styles.formInput}
                    />
                </FormRow>

                {/* TODO: github 계정 입력란 */}
                <FormRow className={styles.formRow}>
                    <GithubAccountInputGroup 
                        fieldName="githubAccount"
                        placeholder="사용자 github 계정"
                        value={formData.nickname}
                        onChange={(e) => updateField('nickname', e.target.value)}
                        onGithubAccountCheck={handleNicknameCheck}
                        className={styles.formGroup}
                        labelClassName={styles.formLabel}
                        inputClassName={styles.formInput}
                    />
                </FormRow>

                {/* <AddressSection 
                    formData={formData}
                    updateField={updateField}
                    className={styles.addressSection}
                    titleClassName={styles.sectionTitle}
                    rowClassName={styles.formRow}
                    groupClassName={styles.formGroup}
                    labelClassName={styles.formLabel}
                    inputClassName={styles.formInput}
                /> */}

                <div className={styles.formActions}>
                    <button
                        onClick={handleSave}
                        style={{
                            backgroundColor: '#5A597D',
                            color: '#fff',
                            border: 'none',
                            borderRadius: '12px',
                            padding: '8px 16px',
                            cursor: 'pointer',
                            marginTop: '16px',
                        }}
                    >
                        저장
                    </button>
                </div>
            </form>
        </div>
    )
}

export default SignupForm