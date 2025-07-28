import React, { useState } from 'react';
import './MyPage.css';
import ProgressSection from './ProgressSection';
import editIcon from '../../assets/edit_icon.png';

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
      duration: '2025.06.10 ~ 2025.09.30',
      progress: 75,
      stack: ['React', 'WebSocket', 'Express'],
      disabled: false
    },
    {
      id: 2,
      title: 'Next.js 기반 기술 블로그',
      duration: '2025.07.01 ~ 2025.08.01',
      progress: 20,
      stack: ['Next.js', 'TailwindCSS'],
      disabled: false
    },
    {
      id: 3,
      title: '개인 포트폴리오 사이트',
      duration: '2025.04.05 ~ 2025.05.20',
      progress: 100,
      stack: ['HTML', 'CSS', 'JavaScript'],
      disabled: true
    }
  ]);
  // 프로젝트 필터링링
  const filteredProjects = projects.filter((project) => {
    if (activeFilter === '전체') return true;
    if (activeFilter === '진행 중') return !project.disabled;
    if (activeFilter === '완료') return project.disabled;
    return true;
  });
  return (
    <div className="mypage-container">
      <aside className="sidebar">
      <div className="profile-card">
          <div className="profile-image-placeholder"></div>
          <a href="#" className="edit-profile-link">
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
              <img src="https://placehold.co/24x24" alt="github icon" />
              <span>@hiabc</span>
            </div>
          </div>
        </div>
      </aside>

      <main className="main-column">
        <ProgressSection />
        <section className="projects-section">
          <div className="projects-header">
            <h2>Projects</h2>
            <div className="project-filters">
              {['전체', '진행 중', '완료'].map((label) => (
                  <button
                    key={label}
                    className={`filter-btn ${activeFilter === label ? 'active' : ''}`}
                    onClick={() => setActiveFilter(label)}
                  >
                    {label}
                  </button>
                ))}
            </div>
          </div>
          {projects.length === 0 ? (
            <div className="no-projects-container">
              <div className="no-projects-content">
                <p className="no-projects-title">생성한 프로젝트가 아직 없어요.</p>
                <p className="no-projects-subtitle">지금 바로 프로젝트를 생성하고 관리해 보세요.</p>
                <button className="create-project-btn">프로젝트 생성하기</button>
              </div>
            </div>
          ) : (
            <div className="project-list">
              {/* 프로젝트 카드 */}
              {filteredProjects.map((project) => (
                <div
                  key={project.id}
                  className={`project-card ${project.disabled ? 'disabled' : ''}`}
                >
                  <h3>{project.title}</h3>
                  <p className="project-duration">{project.duration}</p>
                  <div className="progress-bar-container">
                    <span>진행률</span>
                    <span>{project.progress}%</span>
                    <div className="progress-bar">
                      <div
                        className="progress"
                        style={{
                          width: `${project.progress}%`,
                          backgroundColor: project.disabled ? '#CCCBE4' : undefined
                        }}
                      ></div>
                    </div>
                  </div>
                  {/* 기술 스택 */}
                  <div className="project-stack">
                    {project.stack.map((tech, index) => (
                      <span
                        key={index}
                        className={project.disabled ? 'tech-tag-disabled' : 'tech-tag'}
                      >
                        {tech}
                      </span>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>
      </main>
    </div>
  );
};

export default Mypage;