import React, { useState, useEffect } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useProfileStore } from '../stores/profileStore';
import { useProfileQuery, useUpdateNicknameMutation, useUpdateProfileMutation, useUploadProfileImageMutation } from '../queries/useProfile';
import { useDisconnectGithubMutation, useGithubIdQuery } from '../queries/usegitHub';
import { updateProfile, getProfileImage } from '../apis/profile'; // 서버 갱신
import { X, User, Camera, Mail, Github, AlertCircle, Check } from 'lucide-react';
import styles from './ProfileEditModal.module.css';
import { useGetProfileImageQuery } from '../queries/useProfile';
import { useDeleteUserMutation } from '../queries/useUser';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../stores/authStore';

// GitHub OAuth 설정
const GITHUB_CLIENT_ID = import.meta.env.VITE_GITHUB_CLIENT_ID;

const ProfileEditModal = ({ isOpen, onClose, nickname }) => {
  const navigate = useNavigate();
  const { logout } = useAuthStore();
  const queryClient = useQueryClient();
  const [imgBust, setImgBust] = useState(0);
  // React Query로 프로필 데이터 조회
  const { data: profileData } = useProfileQuery();
  const { data: profileImage } = useGetProfileImageQuery();
  const { data: githubId } = useGithubIdQuery();
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
  const disconnectGithubMutation = useDisconnectGithubMutation();
  const deleteUserMutation = useDeleteUserMutation();

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

      // 즉시 화면 반영: Object URL 프리뷰
      const objectUrl = URL.createObjectURL(file);
      setPreviewImage(objectUrl);
      updateFormField('profileImage', objectUrl);

      // 서버에 이미지 업로드
      uploadProfileImageMutation.mutate(file, {
        onSuccess: async (response) => {
          console.log('프로필 이미지 업로드 성공:', response);
          // 성공 메시지 표시
          // alert('프로필 이미지가 업로드되었습니다.');
          if (response?.imageUrl) {
            queryClient.setQueryData(['profileImage'], { imageUrl: response.imageUrl });
          }

          // 실제 데이터 재조회
          await Promise.all([
            queryClient.invalidateQueries({ queryKey: ['profileImage'] }),
            queryClient.invalidateQueries({ queryKey: ['profile'] }), // 프로필 API에 이미지 포함되면 같이 무효화
          ]);

          // 브라우저 캐시 우회 파라미터 갱신
          setImgBust(Date.now());

          // 메모리 누수 방지 (새 이미지로 전환된 다음 해제)
          setTimeout(() => URL.revokeObjectURL(objectUrl), 0);
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

  // 깃허브 연동 해제 핸들러
  const handleGithubDisconnect = async () => {
    try {
      await disconnectGithubMutation.mutateAsync();
      setGithubReconnected(false);
      // setGithubConnecting(false);
      setFormErrors({ githubAccount: '' }); // 에러 메시지 초기화
      setFormError('githubAccount', 'GitHub 계정 연동이 해제되었습니다');
    } catch (error) {
      console.error('깃허브 연동 해제 실패:', error);
    }
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

  // 회원 탈퇴 핸들러
  const handleDeleteUser = async () => {
    if (window.confirm('정말 회원 탈퇴하시겠습니까?')) {
      try {
        // 깃허브 연동 해제 (현재 연동되어 있는 경우에만)
        if (githubId?.githubId) {
          await disconnectGithubMutation.mutateAsync();
        }

        // 회원 탈퇴 API 호출
        await deleteUserMutation.mutateAsync();

        // 로그아웃 -> 로컬 스토리지, zustand 스토어 상태 초기화
        logout();

        // 쿼리 데이터 초기화 (React Query)
        queryClient.clear();

        // 홈페이지로 이동
        navigate('/');
      } catch (error) {
        console.error('회원 탈퇴 실패:', error);
        alert('회원 탈퇴에 실패했습니다.');
      }
    }
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
                {profileImage?.imageUrl ? (
                  <img src={profileImage.imageUrl} alt="프로필" className={styles.profileImagePreview} />
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
                  value={githubId?.githubId ? `@${githubId.githubId}` : ''}
                  readOnly
                  className={`${styles.inputField} ${styles.githubInputField} ${styles.readOnlyInput} ${formErrors.githubAccount ? styles.inputError : ''}`}
                  placeholder="연동 후 계정명이 표시됩니다"
                />
              </div>
              {/* 깃허브 연동 버튼 */}
              {/* isGithubReconnected: 깃허브 연동 여부 */}
              <button
                type="button"
                // githubId?.githubId가 있으면 연동된 상태이므로 연동 해제, 클릭 시 Disconnect 기능 실행
                // githubId?.githubId가 없으면 연동 안 된 상태이므로 연동 버튼, 클릭 시 Connect 기능 실행
                onClick={githubId?.githubId ? handleGithubDisconnect : handleGithubConnect}
                className={`${styles.actionButton} ${styles.githubButton}`}
              >
                {githubId?.githubId ? '연동 해제' : '연동'}
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
            <p onClick={handleDeleteUser} style={{ color: '#737373', fontSize: '15px', fontWeight: '500', marginTop: '0px', marginBottom: '0px' }}>회원 탈퇴</p>
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