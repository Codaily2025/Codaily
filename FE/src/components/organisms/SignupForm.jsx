import React, { useState, useEffect, useCallback } from 'react'
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

    const [formData, setFormData] = useState({
        nickname: '',
        profileImage: null
    })

    const [errors, setErrors] = useState({})
    const [isSubmitting, setIsSubmitting] = useState(false)
    const [isGithubConnected, setIsGithubConnected] = useState(false)

    // 컴포넌트 마운트 시 GitHub 상태 확인
    useEffect(() => {
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
        setFormData(prev => ({
            ...prev,
            [field]: value
        }))
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

        setErrors(newErrors)
        return Object.keys(newErrors).length === 0
    }

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

            // 성공 시 메인으로 이동
            navigate('/')
        } catch (error) {
            console.error('추가 정보 입력 실패: ', error)
            // throw new Error()
            
        } finally {
            setIsSubmitting(false)
        }
    }

    const handleGithubConnect = useCallback(() => {
        console.log('GitHub 연동 팝업 열기')
        
        // GitHub OAuth URL 구성
        const clientId = import.meta.env.VITE_GITHUB_CLIENT_ID
        const scope = import.meta.env.VITE_GITHUB_SCOPE
        const redirectUri = encodeURIComponent(import.meta.env.VITE_GITHUB_REDIRECT_URI)
        const githubOAuthUrl = `https://github.com/login/oauth/authorize?client_id=${clientId}&scope=${scope}&redirect_uri=${redirectUri}`
        
        // GitHub OAuth 팝업 열기
        const popup = window.open(
            githubOAuthUrl,
            'github-oauth',
            'width=600,height=700,scrollbars=yes,resizable=yes'
        )
        
        if (!popup) {
            alert('팝업이 차단되었습니다. 팝업 차단을 해제해주세요.')
            return
        }
        
        // 팝업에서 메시지를 받아 GitHub 연동 완료 처리
        const handleMessage = (event) => {
            if (event.origin !== import.meta.env.VITE_BASE_URL_3) return
            
            if (event.data.type === 'GITHUB_CONNECTED') {
                setIsGithubConnected(true)
                popup.close()
                window.removeEventListener('message', handleMessage)
                console.log('GitHub 연동 완료!')
            } else if (event.data.type === 'GITHUB_POPUP_CLOSED') {
                window.removeEventListener('message', handleMessage)
                console.log('GitHub 연동 팝업이 닫혔습니다.')
            }
        }
        
        window.addEventListener('message', handleMessage)
    }, [])

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
                </FormRow> */}

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

                <div className={styles.buttonGroup}>
                    <Button
                        htmlType="submit"
                        disabled={isSubmitting}
                        style={{
                            borderRadius: '8px',
                            height: '40px',
                            backgroundColor: '#5A597D',
                            fontSize: '16px',
                            fontWeight: '600',
                            marginTop: '20px'
                        }}
                    >
                        {isSubmitting ? '저장 중...' : '저장하고 시작하기'}
                    </Button>
                </div>
            </form>
        </div>
    )
}

export default SignupForm