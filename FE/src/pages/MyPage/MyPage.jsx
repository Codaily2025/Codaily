import React, { useState } from 'react';
import './MyPage.css';
import ProgressSection from './ProgressSection';
import ProjectsSection from './ProjectsSection';
import ProfileEditModal from '../../components/ProfileEditModal';
import editIcon from '../../assets/edit_icon.png';
import githubIcon from '../../assets/github_icon.png';

// 히트맵 그래프를 위한 더미 데이터
// level: 0 (활동 없음), 1 (적음), 2 (중간), 3 (많음)
// const heatmapData = Array.from({ length: 365 }, (_, i) => {
//   const level = Math.floor(Math.random() * 4);
//   return { date: new Date(2025, 0, i + 1), level };
// });

const Mypage = () => {
  const [activeFilter, setActiveFilter] = useState('전체'); // 필터 기본값 : 전체
  // 실제 애플리케이션에서는 API로부터 프로젝트 데이터를 받아와야 함
  const [projects, setProjects] = useState([
    {
      id: 1,
      title: '팀 협업 칸반보드 제작',
      duration: '2025/06/10 ~ 2025/09/30',
      progress: 75,
      stack: ['React', 'WebSocket', 'Express'],
      disabled: false,
      timeByDay: {
        '월': 0,
        '화': 0,
        '수': 0,
        '목': 0,
        '금': 3,
        '토': 0,
        '일': 0
      },
      repoUrl: 'https://github.com/sample1.git'
    },
    {
      id: 2,
      title: 'Next.js 기반 기술 블로그',
      duration: '2025/07/01 ~ 2025/08/01',
      progress: 20,
      stack: ['Next.js', 'TailwindCSS'],
      disabled: false,
      timeByDay: {
        '월': 0,
        '화': 0,
        '수': 3,
        '목': 4,
        '금': 3,
        '토': 0,
        '일': 0
      },
      repoUrl: 'https://github.com/sample2.git'
    },
    {
      id: 3,
      title: '개인 포트폴리오 사이트',
      duration: '2025/04/05 ~ 2025/05/20',
      progress: 100,
      stack: ['HTML', 'CSS', 'JavaScript'],
      disabled: true,
      timeByDay: {
        '월': 0,
        '화': 0,
        '수': 0,
        '목': 0,
        '금': 0,
        '토': 0,
        '일': 0
      },
      repoUrl: 'https://github.com/sample3.git'
    }
  ]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  return (
    <div className="mypage-container">
      <aside className="sidebar">
      <div className="profile-card">
          <div className="profile-image-placeholder"></div>
          <a onClick={() => setIsModalOpen(true)} className="edit-profile-link">
            <img src={editIcon} alt="edit icon" />
            회원정보 수정
          </a>
          <div className="info-section">
            <label>닉네임</label>
            <div className="info-box">CodeMaster</div>
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
              <span>@hiabc</span>
            </div>
          </div>
        </div>
      </aside>

      <main className="main-column">
        <ProgressSection />
        <ProjectsSection
          projects={projects}
          activeFilter={activeFilter}
          setActiveFilter={setActiveFilter}
        />
        <ProfileEditModal 
          isOpen={isModalOpen} 
          onClose={() => setIsModalOpen(false)} 
        />
      </main>
    </div>
  );
};

export default Mypage;