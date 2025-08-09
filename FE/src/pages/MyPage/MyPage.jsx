// src/pages/MyPage/MyPage.jsx
import React, { useState, useEffect } from 'react';
import './MyPage.css';
import ProgressSection from './ProgressSection';
import ProjectsSection from './ProjectsSection';
import ProfileEditModal from '../../components/ProfileEditModal';
import editIcon from '../../assets/edit_icon.png';
import githubIcon from '../../assets/github_icon.png';
import MyPageProductivityGraph from '../../components/MyPageProductivityGraph';

import useModalStore from '../../store/modalStore';
import { useProfileQuery } from '../../queries/useProfile'; // 프로필 조회 훅

const Mypage = () => {
  const { isOpen, modalType, openModal, closeModal } = useModalStore()
  const { 
    data: profile, 
    isLoading, isError, 
    error 
  } = useProfileQuery(); // 프로필 조회 훅
  console.log('유저 프로필:', profile);

  if (isLoading) return <div>로딩 중...</div>;

  if (isError) return <div>에러 발생: {error.message}</div>;

  if (!profile) return <div>프로필 데이터가 없습니다.</div>;

  return (
    <div className="mypage-container">
      <aside className="sidebar">
        <div className="profile-card">
          <div className="profile-image-placeholder">
            {profile.profileImage && (
              <img src={profile.profileImage} alt="profile" />
            )}
          </div>
          {/* <a onClick={() => setIsModalOpen(true)} className="edit-profile-link"> */}
          <button onClick={() => openModal('PROFILE_EDIT')} className="edit-profile-link">
            <img src={editIcon} alt="edit icon" />
            회원정보 수정
          </button>
          <div className="info-section">
            <label>닉네임</label>
            <div className="info-box">{profile.nickname}</div>
          </div>
          <div className="info-section">
            <label>사용 가능한 기술 스택</label>
            <div className="info-box tech-stack">
              { profile.techStack && profile.techStack.map((tech, index) => (
                <span key={index} className="tech-tag">{tech}</span>
              ))}
              { profile.techStack && profile.techStack.length === 0 && (
                <span>GitHub에서 나의 기술 스택을 설정해보세요.</span>
              )}
              {/* <span className="tech-tag">JavaScript</span>
              <span className="tech-tag">React</span>
              <span className="tech-tag">TypeScript</span>
              <span className="tech-tag">Next.js</span>
              <span className="tech-tag">Node.js</span> */}
            </div>
          </div>
          <div className="info-section">
            <label>GitHub 연동</label>
            <div className="info-box github-link">
              <img src={githubIcon} alt="github icon" />
              <span>@{profile.githubAccount}</span>
            </div>
          </div>
        </div>
      </aside>

      <main className="main-column">
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