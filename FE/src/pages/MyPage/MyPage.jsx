import React, { useState, useEffect } from 'react';
import './MyPage.css';
import ProgressSection from './ProgressSection';
import ProjectsSection from './ProjectsSection';
import ProfileEditModal from '../../components/ProfileEditModal';
import editIcon from '../../assets/edit_icon.png';
import githubIcon from '../../assets/github_icon.png';
import MyPageProductivityGraph from '../../components/MyPageProductivityGraph';

import useModalStore from '../../store/modalStore';
// import { fetchProfile } from '../../apis/profile'; // 서버 호출
// import { useQuery, useQueryClient } from '@tanstack/react-query'; // 쿼리 클라이언트 사용
// import { useProfileQuery, useNicknameQuery } from '../../queries/useProfile'; // 프로필 조회 훅
import { useProfileQuery } from '../../queries/useProfile'; // 프로필 조회 훅
import { useProfileStore } from '../../stores/profileStore'; // 프로필 스토어 사용
import { useProjectsQuery } from '../../queries/useProjectsQuery'; // 프로젝트 조회 훅
import { useProjectStore } from '../../stores/mypageProjectStore'; // 프로젝트 스토어 사용

// 히트맵 그래프를 위한 더미 데이터
// level: 0 (활동 없음), 1 (적음), 2 (중간), 3 (많음)
// const heatmapData = Array.from({ length: 365 }, (_, i) => {
//   const level = Math.floor(Math.random() * 4);
//   return { date: new Date(2025, 0, i + 1), level };
// });

const Mypage = () => {
  // const [isModalOpen, setIsModalOpen] = useState(false);
  const { isOpen, modalType, openModal, closeModal } = useModalStore()
  const setProfile = useProfileStore((state) => state.setProfile); // 프로필 저장
  const profile = useProfileStore((state) => state.profile); // 프로필 가져오기
  // const { data: fetchedProfile, isLoading, isError, error } = useProfileQuery(); // 프로필 조회 훅
  // const { data: nicknameData, isLoading: nicknameLoading } = useNicknameQuery(1); // 닉네임 조회 (userId 1 사용)
  const { 
    data: fetchedProfile, 
    isLoading, isError, 
    error 
  } = useProfileQuery(); // 프로필 조회 훅
  console.log('fetchedProfile:', fetchedProfile);
  // {profileImage: null, nickname: 'google_t1g026xmgb4vsk', email: 'code@example.com', githubAccount: 'hiabc'}
  const userId = fetchedProfile?.userId;
  const {
    data: fetchedProjects,
    isLoading: isLoadingProjects,
    isError: isErrorProjects,
    error: projectsError
  } = useProjectsQuery(userId);

  console.log('userId:', userId); // undefined
  console.log('fetchedProjects:', fetchedProjects);

  // Zustand 스토어에 프로젝트 데이터 설정
  const setProjects = useProjectStore((state) => state.setProjects);

  const [activeFilter, setActiveFilter] = useState('전체'); // 필터 기본값 : 전체

  // 조회된 프로필 데이터를 Zustand 스토어에 저장
  useEffect(() => {
    if (fetchedProfile) {
      setProfile(fetchedProfile);
    }
  }, [fetchedProfile, setProfile]);

  // 닉네임 데이터가 로드되면 프로필 업데이트
  // useEffect(() => {
  //   if (nicknameData) {
  //     const current = useProfileStore.getState().profile;
  //     console.log('닉네임 데이터:', nicknameData);
  //     setProfile({
  //       ...current,
  //       nickname: nicknameData.additionalProp1 || 'TempNickname'
  //     });
  //   }
  // }, [nicknameData, setProfile]);

  // 조회된 프로젝트 데이터를 Zustand 스토어에 저장
  useEffect(() => {
    if (fetchedProjects) {
      setProjects(fetchedProjects);
    }
  }, [fetchedProjects, setProjects]);

  if (isLoading || isLoadingProjects) return <div>로딩 중...</div>;

  if (isError) return <div>에러 발생: {error.message}</div>;
  if (isErrorProjects) return <div>에러 발생: {projectsError.message}</div>;

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
              <span className="tech-tag">JavaScript</span>
              <span className="tech-tag">React</span>
              <span className="tech-tag">TypeScript</span>
              <span className="tech-tag">Next.js</span>
              <span className="tech-tag">Node.js</span>
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
        <ProgressSection />
        <MyPageProductivityGraph />
        <ProjectsSection
          // projects={fetchedProjects}
          // activeFilter={activeFilter}
          // setActiveFilter={setActiveFilter}
        />
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