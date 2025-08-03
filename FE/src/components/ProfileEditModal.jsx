import React, { useState } from 'react';
import { X, User, Camera, Mail, Github, AlertCircle, Check } from 'lucide-react';
import styles from './ProfileEditModal.module.css';

const ProfileEditModal = ({ isOpen, onClose }) => {
  if (!isOpen) return null; // 모달이 닫혀 있으면 렌더링 x

  // 프로필 데이터 관리
  const [profileData, setProfileData] = useState({
    profileImage: null,
    nickname: 'CodeMaster',
    email: 'code@example.com',
    githubAccount: 'hiabc'
  });

  // 이미지 미리보기
  const [previewImage, setPreviewImage] = useState(null);

  // 에러 메시지 -> 에러 발생 시 표시
  const [errors, setErrors] = useState({});

  // 이메일 인증 중 -> 이메일 인증 버튼 클릭 시 표시
  const [isEmailVerifying, setIsEmailVerifying] = useState(false);

  // 이메일 인증 상태 -> 이메일 인증 성공 시 표시
  const [isEmailVerified, setIsEmailVerified] = useState(false);

  // 깃허브 연동 중 -> 깃허브 연동 버튼 클릭 시 표시
  const [isGithubConnecting, setIsGithubConnecting] = useState(false);

  // 깃허브 연동 상태 -> 깃허브 연동 성공 시 표시
  const [isGithubReconnected, setIsGithubReconnected] = useState(false);

  // 이미지 업로드 핸들러
  const handleImageUpload = (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e) => {
        setPreviewImage(e.target.result);
        setProfileData(prev => ({
          ...prev,
          profileImage: file
        }));
      };
      reader.readAsDataURL(file);
    }
  };

  // 입력 필드 변경 핸들러
  const handleInputChange = (field, value) => {
    setProfileData(prev => ({
      ...prev,
      [field]: value
    }));
    validateField(field, value); // 입력 필드 유효성 검사
  };

  // 입력 필드 유효성 검사 -> 입력 필드 변경 시 호출
  const validateField = (field, value) => {
    const newErrors = { ...errors };

    // 닉네임 유효성 검사
    if (field === 'nickname') {
      if (!value.trim()) newErrors.nickname = '필수 입력란입니다';
      else if (value.length > 10) newErrors.nickname = '닉네임은 10자 이내로 작성해주세요';
      else delete newErrors.nickname;
    }

    // 이메일 유효성 검사
    if (field === 'email') {
      if (!value.trim()) newErrors.email = '필수 입력란입니다';
      else {
        delete newErrors.email;
        setIsEmailVerified(false); // 이메일 변경 시 인증 초기화
      }
    }

    // 에러 메시지 업데이트
    setErrors(newErrors);
  };

  // 이메일 인증 핸들러
  const handleEmailVerification = async () => {
    if (!profileData.email.trim()) {
      setErrors(prev => ({ ...prev, email: '필수 입력란입니다' }));
      return;
    }

    setIsEmailVerified(false); // 이메일 인증 초기화
    setIsEmailVerifying(true); // 이메일 인증 중 표시

    // 백엔드 API 호출 시뮬레이션
    setTimeout(() => {
      setIsEmailVerified(true); // 이메일 인증 성공 시 표시
      setIsEmailVerifying(false); // 이메일 인증 중 표시 종료
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
      setProfileData(prev => ({
        ...prev,
        githubAccount: 'new-github-account' // 실제 OAuth 콜백에서 받아온 값으로 교체
      }));
    }, 2000);
  };

  // 모든 필드 유효성 검사
  const validateAllFields = () => {
    const newErrors = {};

    if (!profileData.nickname.trim()) {
      newErrors.nickname = '필수 입력란입니다';
    } else if (profileData.nickname.length > 10) {
      newErrors.nickname = '닉네임은 10자 이내로 작성해주세요';
    }

    if (!profileData.email.trim()) {
      newErrors.email = '필수 입력란입니다';
    }

    if (!profileData.githubAccount.trim()) {
      newErrors.githubAccount = 'GitHub 계정 연동이 필요합니다';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // 저장 핸들러
  const handleSave = () => {
    // 모든 필드 유효성 검사
    if (!validateAllFields()) {
      return;
    }

    // 이메일 인증 여부 확인
    if (!isEmailVerified) {
      setErrors(prev => ({ ...prev, email: '이메일 인증이 필요합니다' }));
      return; // 저장 안 하고 리턴
    }

    // 데이터 저장 : 실제 저장 API 호출 시 사용
    console.log('Saved Data:', profileData);
    onClose(); // 모달 닫기
  };

  // 취소 핸들러
  const handleCancel = () => {
    // 초기 상태로 복원
    setProfileData({
      profileImage: null,
      nickname: 'CodeMaster',
      email: 'code@example.com',
      githubAccount: 'hiabc'
    });
    setPreviewImage(null); // 이미지 미리보기 초기화
    setIsEmailVerified(false); // 이메일 인증 상태 초기화
    setIsGithubReconnected(false); // 깃허브 연동 상태 초기화
    setErrors({}); // 에러 메시지 초기화
    onClose(); // 모달 닫기
  };

  return (
    <div className={styles.modalBackdrop}>
      <div className={styles.modal}>
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
                value={profileData.nickname}
                onChange={(e) => handleInputChange('nickname', e.target.value)}
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
                  value={profileData.email}
                  onChange={(e) => handleInputChange('email', e.target.value)}
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
                {isEmailVerifying ? '인증 중...' : isEmailVerified ? '재인증' : '인증'}
              </button>
            </div>
            {isEmailVerified && !errors.email && (
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
                  value={`@${profileData.githubAccount}`}
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
            onClick={handleCancel}
            className={`${styles.actionButton} ${styles.cancelButton}`}
          >
            취소
          </button>
          <button
            onClick={handleSave}
            className={`${styles.actionButton} ${styles.saveButton}`}
          >
            저장
          </button>
        </div>
      </div>
    </div>
  );
};
export default ProfileEditModal;

// <div className="modal-overlay" onClick={onClose}>
//   <div className="modal-content" onClick={(e) => e.stopPropagation()}>
//     <h2>회원정보 수정</h2>
//     <p>여기에 사용자 정보 수정 폼을 넣을 수 있어요.</p>
//     <button onClick={onClose}>닫기</button>
//   </div>
// </div>
