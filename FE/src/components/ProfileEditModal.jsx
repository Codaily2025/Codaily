import React, { useState, useEffect } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useProfileStore } from '../stores/profileStore';
import { useProfileQuery, useUpdateNicknameMutation, useUpdateProfileMutation, useUploadProfileImageMutation } from '../queries/useProfile';
import { updateProfile } from '../apis/profile'; // 서버 갱신
import { X, User, Camera, Mail, Github, AlertCircle, Check } from 'lucide-react';
import styles from './ProfileEditModal.module.css';

// GitHub OAuth 설정
const GITHUB_CLIENT_ID = import.meta.env.VITE_GITHUB_CLIENT_ID;

const ProfileEditModal = ({ isOpen, onClose, nickname }) => {
  // React Query로 프로필 데이터 조회
  const { data: profileData } = useProfileQuery();

  // Zustand 스토어에서 폼 상태만 가져오기
  const {
    editFormData,
    previewImage,
    formErrors,
    isEmailVerifying,
    isEmailVerified,
    isGithubConnecting,
    isGithubReconnected,
    hasVerifiedEmailOnce,
    initializeFormData,
    updateFormField,
    setPreviewImage,
    setFormErrors,
    setFormError,
    clearFormErrors,
    setEmailVerifying,
    setEmailVerified,
    setHasVerifiedEmailOnce,
    setGithubConnecting,
    setGithubReconnected,
    resetFormState,
  } = useProfileStore();

  // React Query 뮤테이션 훅들
  const updateNicknameMutation = useUpdateNicknameMutation();
  const updateProfileMutation = useUpdateProfileMutation();
  const uploadProfileImageMutation = useUploadProfileImageMutation();
  
  // 모달이 열릴 때마다 서버 프로필 데이터로 폼 초기화
  useEffect(() => {
    if (isOpen && profileData) {
      initializeFormData(profileData);
    }
  }, [isOpen, profileData, initializeFormData]);

  // 뮤테이션 성공 후 콜백 설정
  const handleMutationSuccess = () => {
    resetFormState();
    onClose();
  };

  // 뮤테이션 에러 처리
  const handleNicknameError = (error) => {
    console.error('닉네임 수정 실패:', error);
    setFormError('nickname', '닉네임 수정에 실패했습니다.');
  };

  // 입력 필드 변경 핸들러
  const handleChange = (field) => (e) => {
    const value = e.target.value;
    updateFormField(field, value);
    validateField(field, value); // 입력 필드 유효성 검사
  };

  // 이미지 업로드 핸들러
  const handleImageUpload = (e) => {
    const file = e.target.files[0];
    if (file) {
      // 파일 크기 검증 (5MB 제한)
      if (file.size > 5 * 1024 * 1024) {
        alert('파일 크기는 5MB 이하여야 합니다.');
        return;
      }

      // 파일 타입 검증
      if (!file.type.startsWith('image/')) {
        alert('이미지 파일만 업로드 가능합니다.');
        return;
      }

      // 미리보기 설정
      const reader = new FileReader();
      reader.onload = (ev) => {
        setPreviewImage(ev.target.result);
        updateFormField('profileImage', ev.target.result);
      };
      reader.readAsDataURL(file);

      // 서버에 이미지 업로드
      uploadProfileImageMutation.mutate(file, {
        onSuccess: (response) => {
          console.log('프로필 이미지 업로드 성공:', response);
          // 성공 메시지 표시 (선택사항)
          // alert('프로필 이미지가 업로드되었습니다.');
        },
        onError: (error) => {
          console.error('프로필 이미지 업로드 실패:', error);
          alert('프로필 이미지 업로드에 실패했습니다.');
          // 미리보기 초기화
          setPreviewImage(null);
          updateFormField('profileImage', null);
        }
      });
    }
  };

  // 입력 필드 유효성 검사 -> 입력 필드 변경 시 호출
  const validateField = (field, value) => {
    const newErrors = { ...formErrors };

    // 닉네임 유효성 검사
    if (field === 'nickname') {
      if (!value.trim()) newErrors.nickname = '필수 입력란입니다';
      else if (value.length > 15) newErrors.nickname = '닉네임은 15자 이내로 작성해주세요';
      else delete newErrors.nickname;
    }

    // 이메일 유효성 검사
    if (field === 'email') {
      if (!value.trim()) newErrors.email = '필수 입력란입니다';
      else if (value !== profileData?.email) {
        // 이메일이 현재 저장된 유저 이메일과 다를 때만 인증 요구
        setEmailVerified(false);
      }
      else {
        delete newErrors.email;
        setEmailVerified(true); // 원본 이메일과 같으면 다시 인증할 필요 없음
      }
    }

    // 에러 메시지 업데이트
    setFormErrors(newErrors);
  };

  // 이메일 인증 핸들러
  const handleEmailVerification = async () => {
    if (!editFormData.email.trim()) {
      setFormError('email', '필수 입력란입니다');
      return;
    }

    setEmailVerifying(true); // 이메일 인증 중 표시

    // 백엔드 API 호출 시뮬레이션
    setTimeout(() => {
      setEmailVerified(true); // 이메일 인증 성공 시 표시
      setEmailVerifying(false); // 이메일 인증 중 표시 종료
      setHasVerifiedEmailOnce(true); // 실제 인증 버튼 클릭해 인증했으면 메세지 띄우기
    }, 2000);
  };

  // 깃허브 연동 핸들러
  const handleGithubConnect = async () => {
    // localStorage에서 JWT 토큰 가져오기
    const token = localStorage.getItem('authToken') || '';
    console.log('유저의 jwt token: ', token);

    const githubAuthUrl = `https://github.com/login/oauth/authorize?client_id=${GITHUB_CLIENT_ID}&scope=repo,user&redirect_uri=http://localhost:8081/oauth/github/callback`;
    console.log('githubAuthUrl: ', githubAuthUrl);

    // GitHub OAuth 페이지로 리다이렉트
    window.location.href = githubAuthUrl;
  };

  // 모든 필드 유효성 검사
  const validateAllFields = () => {
    const newErrors = {};

    if (!editFormData.nickname.trim()) {
      newErrors.nickname = '필수 입력란입니다';
    } else if (editFormData.nickname.length > 15) {
      newErrors.nickname = '닉네임은 15자 이내로 작성해주세요';
    }

    if (!editFormData.email.trim()) {
      newErrors.email = '필수 입력란입니다';
    }

    if (!editFormData.githubAccount.trim()) {
      newErrors.githubAccount = 'GitHub 계정 연동이 필요합니다';
    }

    setFormErrors(newErrors);
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
      setFormError('email', '이메일 인증이 필요합니다');
      return; // 저장 안 하고 리턴
    }

    // 닉네임이 변경되었는지 확인
    if (editFormData.nickname !== profileData?.nickname) {
      // 닉네임 수정 API 호출 (임시로 userId 1 사용)
      updateNicknameMutation.mutate(
        { userId: 1, nickname: editFormData.nickname },
        {
          onSuccess: handleMutationSuccess,
          onError: handleNicknameError,
        }
      );
    } else {
      // 다른 필드만 변경된 경우 기존 로직 사용
      updateProfileMutation.mutate(editFormData, {
        onSuccess: handleMutationSuccess,
      });
    }
  };

  // 취소 핸들러
  const handleCancel = () => {
    resetFormState(); // 폼 상태 초기화
    onClose(); // 모달 닫기
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
            {uploadProfileImageMutation.isPending && (
              <p className={styles.uploadStatus}>이미지 업로드 중...</p>
            )}
          </div>

          {/* 닉네임 */}
          <div className={styles.formGroup}>
            <label className={styles.formLabel}>닉네임*</label>
            <div className={styles.inputWrapper}>
              <User size={18} className={styles.inputIcon} />
              <input
                type="text"
                value={editFormData.nickname ?? ""}
                onChange={handleChange('nickname')}
                className={`${styles.inputField} ${styles.nicknameInputField} ${formErrors.nickname ? styles.inputError : ''}`}
                placeholder="닉네임을 입력하세요"
              />
            </div>
            {formErrors.nickname && (
              <div className={styles.errorMessage}>
                <AlertCircle size={14} />
                <p>{formErrors.nickname}</p>
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
                  value={editFormData.email ?? ""}
                  onChange={handleChange('email')}
                  className={`${styles.inputField} ${styles.emailInputField} ${formErrors.email ? styles.inputError : ''}`}
                  placeholder="이메일을 입력하세요"
                />
              </div>
              <button
                onClick={handleEmailVerification}
                disabled={isEmailVerifying}
                className={`${styles.actionButton} ${styles.emailButton}`}
              >
                {isEmailVerifying ? '인증 중...' : isEmailVerified ? '인증' : '인증'}
              </button>
            </div>
            {isEmailVerified && !formErrors.email && hasVerifiedEmailOnce && (
              <div className={styles.successMessage}>
                <Check size={16} />
                이메일 인증에 성공했습니다
              </div>
            )}
            {formErrors.email && (
              <div className={styles.errorMessage}>
                <AlertCircle size={14} />
                <p>{formErrors.email}</p>
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
                  value={`@${editFormData.githubAccount ?? ""}`}
                  readOnly
                  className={`${styles.inputField} ${styles.githubInputField} ${styles.readOnlyInput} ${formErrors.githubAccount ? styles.inputError : ''}`}
                  placeholder="GitHub 연동 후 계정명이 표시됩니다"
                />
              </div>
              <button
                type="button"
                onClick={handleGithubConnect}
                className={`${styles.actionButton} ${styles.githubButton}`}
              >
                변경
              </button>
            </div>
            {isGithubReconnected && (
              <div className={styles.successMessage}>
                <Check size={16} />
                GitHub 계정이 연동되었습니다
              </div>
            )}
            {formErrors.githubAccount && (
              <div className={styles.errorMessage}>
                <AlertCircle size={14} />
                <p>{formErrors.githubAccount}</p>
              </div>
            )}
          </div>
          <div style={{ cursor: 'pointer', display: 'flex', justifyContent: 'flex-end', alignItems: 'center', marginRight: '10px' }}>
            <p style={{ color: '#737373', fontSize: '15px', fontWeight: '500' }}>회원 탈퇴</p>
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
            onClick={handleSave}
            disabled={updateProfileMutation.isPending || updateNicknameMutation.isPending}
            className={`${styles.actionButton} ${styles.saveButton}`}
          >
            {updateProfileMutation.isPending || updateNicknameMutation.isPending ? '저장 중...' : '저장'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default ProfileEditModal;