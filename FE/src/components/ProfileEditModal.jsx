import React, { useState, useEffect } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useProfileStore } from '../stores/profileStore';
import { updateProfile } from '../apis/profile'; // 서버 갱신
import { X, User, Camera, Mail, Github, AlertCircle, Check } from 'lucide-react';
import styles from './ProfileEditModal.module.css';

const ProfileEditModal = ({ isOpen, onClose }) => {

  const { profile: storeProfile, setProfile } = useProfileStore();
  const [formData, setFormData] = useState(storeProfile);
  const [previewImage, setPreviewImage] = useState(storeProfile.profileImage || null) // 이미지 미리보기
  const [errors, setErrors] = useState({}) // 에러 메시지
  const [isEmailVerifying, setIsEmailVerifying] = useState(false); // 이메일 인증 중
  const [isEmailVerified, setIsEmailVerified] = useState(false); // 이메일 인증됨
  const [isGithubConnecting, setIsGithubConnecting] = useState(false); // 깃허브 연동 중
  const [isGithubReconnected, setIsGithubReconnected] = useState(false); // 깃허브 연동됨
  const [hasVerifiedEmailOnce, setHasVerifiedEmailOnce] = useState(false); // 기존 이메일 인증 여부 체크

  const queryClient = useQueryClient(); // 쿼리 클라이언트 인스턴스 생성

  // 모달이 열릴 때마다 Zustand 프로필로 초기화
  useEffect(() => {
    if (isOpen) {
      setFormData(storeProfile);
      setPreviewImage(storeProfile.profileImage || null);
      setErrors({});
      // setIsEmailVerified(false);
      setIsEmailVerified(true); 
      setIsEmailVerifying(false);
      setIsGithubReconnected(false);
      setIsGithubConnecting(false);
      setHasVerifiedEmailOnce(false); // 이메일 인증 메세지_처음 들어왔을 때는 인증은 되어 있지만 메세지는 no
    }
  }, [isOpen, storeProfile]);

  // 프로필 업데이트 뮤테이션
  const {mutate, isLoading, isError, error} = useMutation({
    mutationFn: updateProfile, // 서버에 업데이트 요청할 함수
    onSuccess: (updated) => { // 성공시 콜백
      // React Query 캐시 업데이트
      queryClient.setQueryData(['profile'], updated);
      // Zustand 전역 상태 업데이터
      setProfile(updated);
      onClose();
    }
  })
  // 입력 필드 변경 핸들러
  const handleChange = (field) => (e) => {
    const value = e.target.value;
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
    validateField(field, value); // 입력 필드 유효성 검사
  };

  // 이미지 업로드 핸들러
  const handleImageUpload = (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (ev) => {
        setPreviewImage(ev.target.result);
        setFormData(prev => ({
          ...prev,
          // profileImage: file // 아직 백엔드 연동 안 됨
          profileImage: ev.target.result // 백엔드 연동 안 된 상태에서 base64를 formData에도 저장
        }));
      };
      reader.readAsDataURL(file); // 이미지 미리보기
    }
  };

  // 입력 필드 유효성 검사 -> 입력 필드 변경 시 호출
  const validateField = (field, value) => {
    const newErrors = { ...errors };

    // 닉네임 유효성 검사
    if (field === 'nickname') {
      if (!value.trim()) newErrors.nickname = '필수 입력란입니다';
      else if (value.length > 15) newErrors.nickname = '닉네임은 15자 이내로 작성해주세요';
      else delete newErrors.nickname;
    }

    // 이메일 유효성 검사
    if (field === 'email') {
      if (!value.trim()) newErrors.email = '필수 입력란입니다';
      else if (value !== storeProfile.email) {
        // 이메일이 현재 저장된 유저 이메일과 다를 때만 인증 요구
        setIsEmailVerified(false);
      }
      else {
        delete newErrors.email;
        setIsEmailVerified(true); // 원본 이메일과 같으면 다시 인증할 필요 없음
      }
    }

    // 에러 메시지 업데이트
    setErrors(newErrors);
  };

  // 이메일 인증 핸들러
  const handleEmailVerification = async () => {
    if (!formData.email.trim()) {
      setErrors(prev => ({ ...prev, email: '필수 입력란입니다' }));
      return;
    }

    setIsEmailVerifying(true); // 이메일 인증 중 표시

    // 백엔드 API 호출 시뮬레이션
    setTimeout(() => {
      setIsEmailVerified(true); // 이메일 인증 성공 시 표시
      setIsEmailVerifying(false); // 이메일 인증 중 표시 종료
      setHasVerifiedEmailOnce(true); // 실제 인증 버튼 클릭해 인증했으면 메세지 띄우기
    }, 2000);
  };

  // 깃허브 연동 핸들러
  const handleGithubConnect = async () => {
    setIsGithubConnecting(true); // 깃허브 연동 중 표시

    // Simulate GitHub OAuth flow
    setTimeout(() => {
      setIsGithubReconnected(true); // 깃허브 연동 성공 시 표시
      setIsGithubConnecting(false); // 깃허브 연동 중 표시 종료
      // GitHub 계정 변경
      setFormData(prev => ({
        ...prev,
        githubAccount: 'new-github-account' // 실제 OAuth 콜백에서 받아온 값으로 교체
      }));
    }, 2000);
  };

  // 모든 필드 유효성 검사
  const validateAllFields = () => {
    const newErrors = {};

    if (!formData.nickname.trim()) {
      newErrors.nickname = '필수 입력란입니다';
    } else if (formData.nickname.length > 15) {
      newErrors.nickname = '닉네임은 10자 이내로 작성해주세요';
    }

    if (!formData.email.trim()) {
      newErrors.email = '필수 입력란입니다';
    }

    if (!formData.githubAccount.trim()) {
      newErrors.githubAccount = 'GitHub 계정 연동이 필요합니다';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // 저장 핸들러
  const handleSave = (e) => {
    e.preventDefault();

    // 모든 필드 유효성 검사
    if (!validateAllFields()) {
      return;
    }

    // 이메일 인증 여부 확인
    if (!isEmailVerified) {
      setErrors(prev => ({ ...prev, email: '이메일 인증이 필요합니다' }));
      return; // 저장 안 하고 리턴
    }

    mutate(formData);
  };

  // 취소 핸들러
  const handleCancel = () => {
    // 초기 상태로 복원
    setFormData(storeProfile)
    setPreviewImage(storeProfile.profileImage || null) // 이미지 적용 / 초기화
    setErrors({}) // 에러 메시지 초기화
    setIsEmailVerified(false) // 이메일 인증 상태 초기화
    setIsGithubReconnected(false) // 깃허브 연동 상태 초기화
    onClose() // 모달 닫기
  };

  if (!isOpen) return null; // 모달이 닫혀 있으면 렌더링 x

  return (
    <div className={styles.modalBackdrop}>
      <form className={styles.modal} onSubmit={handleSave}>
        {/* Modal Header */}
        <div className={styles.modalHeader}>
          <h2 className={styles.modalTitle}>회원정보 수정</h2>
          <button
            onClick={handleCancel}
            className={styles.closeButton}
          >
            <X size={20} className={styles.closeButtonIcon} />
          </button>
        </div>

        {/* 모달 컨텐츠 */}
        <div className={styles.modalContent}>
          {/* 프로필 이미지 */}
          <div className={styles.profileImageContainer}>
            <div className={styles.profileImageWrapper}>
              <input
                type="file"
                accept="image/*"
                onChange={handleImageUpload}
                className={styles.fileInputOverlay}
              // className="hidden"
              />
              <div className={styles.profileImage}>
                {previewImage ? (
                  <img src={previewImage} alt="프로필" className={styles.profileImagePreview} />
                ) : (
                  <User size={32} className={styles.profileImageIcon} />
                )}
              </div>
              <label className={styles.cameraButton}>
                <Camera size={16} className={styles.cameraIcon} />
              </label>
            </div>
            {/* <p className={styles.imageUploadText}>프로필 사진 변경</p> */}
          </div>

          {/* 닉네임 */}
          <div className={styles.formGroup}>
            <label className={styles.formLabel}>닉네임*</label>
            <div className={styles.inputWrapper}>
              <User size={18} className={styles.inputIcon} />
              <input
                type="text"
                value={formData.nickname}
                onChange={handleChange('nickname')}
                className={`${styles.inputField} ${styles.nicknameInputField} ${errors.nickname ? styles.inputError : ''}`}
                placeholder="닉네임을 입력하세요"
              />
            </div>
            {errors.nickname && (
              <div className={styles.errorMessage}>
                <AlertCircle size={14} />
                <p>{errors.nickname}</p>
              </div>
            )}
          </div>

          {/* 이메일 */}
          <div className={styles.formGroup}>
            <label className={styles.formLabel}>이메일*</label>
            <div className={styles.inputGroup}>
              <div className={styles.inputWrapper}>
                <Mail size={18} className={styles.inputIcon} />
                <input
                  type="email"
                  value={formData.email}
                  onChange={handleChange('email')}
                  className={`${styles.inputField} ${styles.emailInputField} ${errors.email ? styles.inputError : ''}`}
                  placeholder="이메일을 입력하세요"
                />
              </div>
              <button
                onClick={handleEmailVerification}
                // disabled={isEmailVerifying || isEmailVerified}
                disabled={isEmailVerifying}
                className={`${styles.actionButton} ${styles.emailButton}`}
              // className={`${styles.actionButton} ${isEmailVerified ? styles.verifiedButton : ''}`}
              >
                {isEmailVerifying ? '인증 중...' : isEmailVerified ? '인증' : '인증'}
                {/* {isEmailVerifying ? '인증 중...' : isEmailVerified ? '재인증' : '인증'} */}
              </button>
            </div>
            {isEmailVerified && !errors.email && hasVerifiedEmailOnce && (
              <div className={styles.successMessage}>
                <Check size={16} />
                이메일 인증에 성공했습니다
              </div>
            )}
            {errors.email && (
              <div className={styles.errorMessage}>
                <AlertCircle size={14} />
                <p>{errors.email}</p>
              </div>
            )}
          </div>

          {/* 깃허브 연동 */}
          <div className={styles.formGroup}>
            <label className={styles.formLabel}>GitHub 계정*</label>
            <div className={styles.inputGroup}>
              <div className={styles.inputWrapper}>
                <Github size={18} className={styles.inputIcon} />
                <input
                  type="text"
                  value={`@${formData.githubAccount}`}
                  readOnly
                  className={`${styles.inputField} ${styles.githubInputField} ${styles.readOnlyInput} ${errors.githubAccount ? styles.inputError : ''}`}
                  placeholder="GitHub 연동 후 계정명이 표시됩니다"
                />
              </div>
              <button
                onClick={handleGithubConnect}
                disabled={isGithubConnecting}
                className={`${styles.actionButton} ${styles.githubButton}`}
              >
                {/* <Github size={16} /> */}
                {isGithubConnecting ? '연동 중...' : '변경'}
              </button>
            </div>
            {isGithubReconnected && (
              <div className={styles.successMessage}>
                <Check size={16} />
                GitHub 계정이 연동되었습니다
              </div>
            )}
            {errors.githubAccount && (
              <div className={styles.errorMessage}>
                <AlertCircle size={14} />
                <p>{errors.githubAccount}</p>
              </div>
            )}
          </div>
        </div>

        {/* 모달 푸터 */}
        <div className={styles.modalFooter}>
          <button
            type="button"
            onClick={handleCancel}
            className={`${styles.actionButton} ${styles.cancelButton}`}
          >
            취소
          </button>
          <button
            type="submit"
            disabled={isLoading}
            className={`${styles.actionButton} ${styles.saveButton}`}
          >
            저장
          </button>
        </div>
      </form>
    </div>
  );
};
export default ProfileEditModal;
