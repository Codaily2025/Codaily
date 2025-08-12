// src/pages/MyPage/MyPage.jsx
import React, { useState, useEffect } from 'react';
import styles from './MyPage.module.css';
import ProgressSection from './ProgressSection';
import ProjectsSection from './ProjectsSection';
import ProfileEditModal from '../../components/ProfileEditModal';
import editIcon from '../../assets/edit_icon.png';
import githubIcon from '../../assets/github_icon.png';
import MyPageProductivityGraph from '../../components/MyPageProductivityGraph';
import { useGetProfileImageQuery } from '../../queries/useProfile';
import useModalStore from '../../store/modalStore';
import { useProfileQuery } from '../../queries/useProfile'; // 프로필 조회 훅
import { useGithubIdQuery, useGithubTechStackSyncMutation } from '../../queries/useGitHub';

const Mypage = () => {
  const { isOpen, modalType, openModal, closeModal } = useModalStore()
  const {
    data: profile,
    isLoading, isError,
    error
  } = useProfileQuery(); // 프로필 조회 훅

  const { data: githubId } = useGithubIdQuery();
  /* profile 객체에 githubId 추가 */
  // const githubIdStr = githubId?.githubId || '정보 없음';
  // profile.githubId = githubIdStr;
  // console.log('깃허브 아이디:', githubId);

  const githubTechStackSyncMutation = useGithubTechStackSyncMutation();
  const handleSyncTechStack = () => {
    githubTechStackSyncMutation.mutate();
  };

  // 깃허브 연동 여부
  const isGithubConnected = !!githubId?.githubId;
  // 기술 스택 배열
  const techStack = Array.isArray(profile?.techStack) ? profile?.techStack : [];
  // 깃허브 연동 여부 확인 -> 미연동이면 항상 빈 배열
  const displayTechStack = isGithubConnected ? techStack : [];
  // 기술 스택 비어있는지 확인
  const isTechStackEmpty = displayTechStack.length === 0;

  const { data: profileImage } = useGetProfileImageQuery();
  console.log('유저 프로필 이미지:', profileImage);
  // const profileImageStr = profileImage?.imageUrl || null;
  // profile.profileImage = profileImageStr;
  // console.log('유저 프로필 이미지:', profileImage);

  console.log('유저 프로필:', profile);

  if (isLoading) return <div>로딩 중...</div>;

  if (isError) return <div>에러 발생: {error.message}</div>;

  if (!profile) return <div>프로필 데이터가 없습니다.</div>;

  return (
    <div className={styles.mypageContainer}>
      <aside className={styles.sidebar}>
        <div className={styles.profileCard}>
          <div className={styles.profileImagePlaceholder}>
            {profileImage?.imageUrl ? (
              <img src={profileImage.imageUrl} alt="프로필" />
            ) : (
              <User size={32} className={styles.profileImageIcon} />
            )}
          </div>
          {/* <a onClick={() => setIsModalOpen(true)} className="edit-profile-link"> */}
          <button onClick={() => openModal('PROFILE_EDIT')} className={styles.editProfileLink}>
            <img src={editIcon} alt="edit icon" />
            회원정보 수정
          </button>
          <div className={styles.infoSection}>
            <label>닉네임</label>
            <div className={styles.infoBox}>{profile.nickname}</div>
          </div>
          <div className={styles.infoSection}>
            <label>사용 가능한 기술 스택</label>
            <div className={`${styles.infoBox} ${styles.techStack}`}>
              {!isGithubConnected ? ( // 깃허브 미연동일때
                <div className={styles.techStackEmpty}>
                  <span>GitHub에서 나의 기술 스택을 설정해보세요.</span>
                  <button
                    className={styles.syncTechStackBtn}
                    onClick={handleSyncTechStack}
                    disabled={githubTechStackSyncMutation.isPending}
                  >
                    {githubTechStackSyncMutation.isPending ? '동기화 중...' : 'GitHub 동기화'}
                  </button>
                </div>
              ) : 
              isTechStackEmpty ? ( // 기술스택 비어있을때
                <div className={styles.techStackEmpty}>
                  <span>GitHub에서 나의 기술 스택을 설정해보세요.</span>
                </div>
              ) : (
                <>
                  {displayTechStack.map((tech, index) => (
                    <span key={index} className={styles.techTag}>{tech}</span>
                  ))}
                </>
              )}
              {/* <span className="tech-tag">JavaScript</span>
              <span className="tech-tag">React</span>
              <span className="tech-tag">TypeScript</span>
              <span className="tech-tag">Next.js</span>
              <span className="tech-tag">Node.js</span> */}
            </div>
            {githubTechStackSyncMutation.isError && (
              <div className={styles.errorMessage}>
                동기화에 실패했습니다. 깃허브 연동을 확인하세요.
              </div>
            )}
            {githubTechStackSyncMutation.isSuccess && (
              <div className={styles.successMessage}>
                기술스택이 성공적으로 동기화되었습니다!
              </div>
            )}
          </div>
          <div className={styles.infoSection}>
            <label>GitHub 연동</label>
            <div className={`${styles.infoBox} ${styles.githubLink}`}>
              <img src={githubIcon} alt="github icon" />
              <span>@{githubId?.githubId}</span>
            </div>
          </div>
        </div>
      </aside>

      <main className={styles.mainColumn}>
        {/* 나중에 백엔드에서 프로그래스 로직 구현 되면 주석 해제 */}
        {/* <ProgressSection /> */}
        <MyPageProductivityGraph />
        <ProjectsSection />
        {/* 프로필 수정 모달 */}
        <ProfileEditModal
          nickname={profile.nickname}
          isOpen={isOpen && (modalType === 'PROFILE_EDIT')} // 모달 열림 여부
          // onClose={() => setIsModalOpen(false)} 
          onClose={() => closeModal()} // 모달 닫기
        />
      </main>
    </div>
  );
};

export default Mypage;