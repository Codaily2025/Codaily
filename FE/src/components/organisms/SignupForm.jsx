import React, { useState, useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import ProfileAvatar from '@/components/atoms/ProfileAvatar'
import Button from '@/components/atoms/Button'
import FormRow from '@/components/molecules/FormRow'
import InputGroup from '@/components/molecules/InputGroup'
import NicknameInputGroup from '@/components/molecules/NicknameInputGroup'
import GithubAccountInputGroup from '@/components/molecules/GithubAccountInputGroup'
// import AddressSection from '@/components/molecules/AddressSection'
import useModalStore from '@/store/modalStore'
import styles from './SignupForm.module.css'
import { updateUserAdditionalInfo } from '@/apis/userApi'

const SignupForm = () => {
    const { openModal } = useModalStore()
    const navigate = useNavigate()
    const [searchParams] = useSearchParams()

    // const [formData, setFormData] = useState({
    //     firstName: "",
    //     lastName: "",
    //     email: "",
    //     nickname: "",
    //     country: "",
    //     city: "",
    //     address: "",
    //     zipCode: "",
    //     phone: "",
    // })

    const [formData, setFormData] = useState({
        nickname: '',
        // githubAccount: '',
        profileImage: null
    })

    const [errors, setErrors] = useState({})
    const [isSubmitting, setIsSubmitting] = useState(false)
    const [isGithubConnected, setIsGithubConnected] = useState(false)

    // 컴포넌트 마운트 시 localStorage에서 폼 데이터 복원 및 GitHub 상태 확인
    useEffect(() => {
        // 저장된 폼 데이터 복원
        const savedFormData = localStorage.getItem('signupFormData')
        if (savedFormData) {
            try {
                const parsedData = JSON.parse(savedFormData)
                setFormData(prev => ({
                    ...prev,
                    nickname: parsedData.nickname || '',
                    githubAccount: parsedData.githubAccount || ''
                }))
                console.log('폼 데이터 복원:', parsedData)
            } catch (error) {
                console.error('저장된 폼 데이터 파싱 오류:', error)
            }
        }

        // GitHub 연동 상태 확인
        const githubStatus = searchParams.get('github')
        console.log('GitHub 연동 상태:', githubStatus)
        
        if (githubStatus === 'connected') {
            setIsGithubConnected(true)
            console.log('GitHub 연동 완료!')
            
            // GitHub 연동 완료 후 URL 파라미터 정리
            const newUrl = window.location.pathname
            window.history.replaceState({}, document.title, newUrl)
        }
    }, [searchParams])

    const updateField = (field, value) => {
        const newFormData = {
            ...formData,
            [field]: value
        }
        setFormData(newFormData)
        
        // localStorage에 저장 (프로필 이미지 제외)
        const dataToSave = {
            nickname: newFormData.nickname,
            githubAccount: newFormData.githubAccount
        }
        localStorage.setItem('signupFormData', JSON.stringify(dataToSave))
    }

    // 폼 유효성 검사
    const validateForm = () => {
        const newErrors = {}

        // 닉네임 유효성 검사
        if (!formData.nickname.trim()) {
            newErrors.nickname = '닉네임을 입력해주세요'
        } 
        // else if (formData.nickname.length < 2) {
        //     newErrors.nickname = '닉네임은 2글자 이상이어야 합니다'
        // } else if (formData.nickname.length > 20) {
        //     newErrors.nickname = '닉네임은 20글자 이하여야 합니다'
        // } else if (!/^[a-zA-Z0-9가-힣_]+$/.test(formData.nickname)) {
        //     newErrors.nickname = '닉네임은 영문, 한글, 숫자, 언더바만 사용 가능합니다'
        // }

        // // GitHub 계정 유효성 검사
        // if (!formData.githubAccount.trim()) {
        //     newErrors.githubAccount = 'GitHub 계정을 입력해주세요'
        // } else if (!/^[a-zA-Z0-9][a-zA-Z0-9_-]*[a-zA-Z0-9]$|^[a-zA-Z0-9]$/.test(formData.githubAccount)) {
        //     newErrors.githubAccount = '올바른 GitHub 계정 형식이 아닙니다'
        // }

        setErrors(newErrors)
        return Object.keys(newErrors).length === 0
    }

    // const handleSave = (e) => {
    //     if (e) e.preventDefault()
    //     console.log('사용자 입력값: ', formData)
    // }

    // submit 핸들러
    const handleSubmit = async (e) => {
        e.preventDefault()

        if (!validateForm()) {
            return
        }

        setIsSubmitting(true)

        try {
            // FormData 생성해 파일과 텍스트 데이터 함께 전송
            const submitData = new FormData()
            submitData.append('nickname', formData.nickname)
            submitData.append('githubAccount', formData.githubAccount)

            if (formData.profileImage) {
                submitData.append('profileImage', formData.profileImage)
            }

            // 바로 api 요청
            await updateUserAdditionalInfo(submitData)

            // 성공 시 localStorage 정리 후 메인으로 이동
            localStorage.removeItem('signupFormData')
            navigate('/')
        } catch (error) {
            console.error('추가 정보 입력 실패: ', error)
            // throw new Error()
            
        } finally {
            setIsSubmitting(false)
        }
    }

    // 닉네임 중복 체크 진행 X
    // const handleNicknameCheck = () => {
    //     openModal('NICKNAME_CHECK', {
    //         nickname: formData.nickname || 'user_nickname'
    //     })
    // }

    const handleGithubConnect = () => {
        console.log('GitHub 연동 팝업 열기')
        
        // 현재 폼 데이터를 localStorage에 저장
        const dataToSave = {
            nickname: formData.nickname,
            githubAccount: formData.githubAccount
        }
        localStorage.setItem('signupFormData', JSON.stringify(dataToSave))
        
        // GitHub OAuth 팝업 열기
        const popup = window.open(
            'http://localhost:8081/oauth2/authorization/github',
            'github-oauth',
            'width=500,height=600,scrollbars=yes,resizable=yes'
        )
        
        if (!popup) {
            alert('팝업이 차단되었습니다. 팝업 차단을 해제해주세요.')
            return
        }
        
        // // 팝업 완료 확인 (주기적으로 체크)
        // const checkPopup = setInterval(() => {
        //     if (popup.closed) {
        //         clearInterval(checkPopup)
                
        //         // 팝업이 닫힌 후 잠시 대기 후 페이지 새로고침하여 상태 확인
        //         setTimeout(() => {
        //             window.location.reload()
        //         }, 500)
        //     }
        // }, 1000)
    }

    const handleAvatarEdit = () => {
        const input = document.createElement('input')
        input.type = 'file'
        input.accept = 'image/*'
        input.onchange = (e) => {
            const file = e.target.files[0]
            if (file) {
                handleFileChange(file)
            }
        }
        input.click()
    }

    const handleFileChange = (file) => {
        setFormData(prev => ({
            ...prev,
            profileImage: file
        }))
    }

    return (
        <div className={styles.profileSection}>
            <div className={styles.headerSection}>
                <h1 className={styles.formTitle}>추가 정보를 입력해주세요</h1>
                <p className={styles.formSubtitle}>원활한 서비스 이용을 위해 필요한 정보입니다</p>
            </div>

            <ProfileAvatar
                src={formData.profileImage ? URL.createObjectURL(formData.profileImage) : null}
                editable={true}
                onEdit={handleAvatarEdit}
                className={styles.profileAvatarSection}
                avatarClassName={styles.profileAvatar}
                iconClassName={styles.cameraIcon}
            />

            <form onSubmit={handleSubmit}>
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
                        // onChange={(e) => updateField('email', e.target.value)}
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
                        // onNicknameCheck={handleNicknameCheck}
                        className={styles.formGroup}
                        labelClassName={styles.formLabel}
                        inputClassName={styles.formInput}
                        error={errors.nickname}
                    />
                </FormRow>

                {/* TODO: github 계정 입력란 */}
                <FormRow className={styles.formRow}>
                    <GithubAccountInputGroup
                        fieldName="githubAccount"
                        placeholder="사용자 github 계정"
                        value={formData.githubAccount}
                        onChange={(e) => updateField('githubAccount', e.target.value)}
                        onGithubAccountCheck={handleGithubConnect}
                        className={styles.formGroup}
                        labelClassName={styles.formLabel}
                        inputClassName={styles.formInput}
                        error={errors.githubAccount}
                        isConnected={isGithubConnected}
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

                <div className={styles.buttonGroup}>
                    <Button
                        htmlType="submit"
                        disabled={isSubmitting}
                        className={styles.submitButton}
                    >
                        {isSubmitting ? '저장 중...' : '저장하고 시작하기'}
                    </Button>
                    {/* <button
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
                    </button> */}
                </div>
            </form>
        </div>
    )
}

export default SignupForm