import React from 'react'
import ProfileAvatar from '@/components/atoms/ProfileAvatar'
import SaveButton from '@/components/atoms/SaveButton'
import FormRow from '@/components/molecules/FormRow'
import InputGroup from '@/components/molecules/InputGroup'
import NicknameInputGroup from '@/components/molecules/NicknameInputGroup'
import AddressSection from '@/components/molecules/AddressSection'
import useModalStore from '@/store/modalStore'
import useFormStore from '@/store/formStore'
import styles from './SignupForm.module.css'

const SignupForm = () => {
    const { openModal } = useModalStore()
    const { formData } = useFormStore()

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
                <FormRow className={styles.formRow}>
                    <InputGroup 
                        label="First Name"
                        fieldName="firstName"
                        placeholder="First Name"
                        className={styles.formGroup}
                        labelClassName={styles.formLabel}
                        inputClassName={styles.formInput}
                    />
                    <InputGroup 
                        label="Last Name"
                        fieldName="lastName"
                        placeholder="Last Name"
                        className={styles.formGroup}
                        labelClassName={styles.formLabel}
                        inputClassName={styles.formInput}
                    />
                </FormRow>

                <FormRow className={styles.formRow}>
                    <InputGroup 
                        label="Email Address"
                        fieldName="email"
                        placeholder="Email Address"
                        type="email"
                        className={styles.formGroupFullWidth}
                        labelClassName={styles.formLabel}
                        inputClassName={styles.formInput}
                    />
                </FormRow>

                <FormRow className={styles.formRow}>
                    <NicknameInputGroup 
                        fieldName="nickname"
                        placeholder="user_nickname"
                        onNicknameCheck={handleNicknameCheck}
                        className={styles.formGroup}
                        labelClassName={styles.formLabel}
                        inputClassName={styles.formInput}
                    />
                </FormRow>

                <AddressSection 
                    className={styles.addressSection}
                    titleClassName={styles.sectionTitle}
                    rowClassName={styles.formRow}
                    groupClassName={styles.formGroup}
                    labelClassName={styles.formLabel}
                    inputClassName={styles.formInput}
                />

                <div className={styles.formActions}>
                    <SaveButton />
                </div>
            </form>
        </div>
    )
}

export default SignupForm