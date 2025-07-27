import React, { useState } from 'react';
import './MyPage.css';

// 히트맵 그래프를 위한 더미 데이터
// level: 0 (활동 없음), 1 (적음), 2 (중간), 3 (많음)
const heatmapData = Array.from({ length: 365 }, (_, i) => {
  const level = Math.floor(Math.random() * 4);
  return { date: new Date(2025, 0, i + 1), level };
});

const Mypage = () => {
  // 실제 애플리케이션에서는 API로부터 프로젝트 데이터를 받아와야 함
  const [projects, setProjects] = useState([]);
  return (
    <div className="mypage-container">
      <aside className="sidebar">
      <div className="profile-card">
          <div className="profile-image-placeholder"></div>
          <a href="#" className="edit-profile-link">
            <img src="https://placehold.co/16x19" alt="edit icon" />
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

      <main className="main-content">
      <section className="progress-section">
          <h2>Progress</h2>
          <div className="heatmap-container">
            <div className="heatmap-header">
                <span>2025 Monday first</span>
                <div className="heatmap-legend">
                    <span>Less</span>
                    <div className="heatmap-cell level-1"></div>
                    <div className="heatmap-cell level-2"></div>
                    <div className="heatmap-cell level-3"></div>
                    <span>More</span>
                </div>
            </div>
            <div className="heatmap-grid">
                {heatmapData.map((data, index) => (
                    <div
                        key={index}
                        className={`heatmap-cell level-${data.level}`}
                        title={`Date: ${data.date.toDateString()}, Level: ${data.level}`}
                    ></div>
                ))}
            </div>
          </div>
        </section>
        <section className="projects-section">
          <div className="projects-header">
            <h2>Projects</h2>
            <div className="project-filters">
              <button className="filter-btn active">전체</button>
              <button className="filter-btn">진행 중</button>
              <button className="filter-btn">완료</button>
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
            {/* Project Cards */}
            <div className="project-card">
              <h3>팀 협업 칸반보드 제작</h3>
              <p className="project-duration">2025.06.10 ~ 2025.09.30</p>
              <div className="progress-bar-container">
                <span>진행률</span>
                <span>75%</span>
                <div className="progress-bar">
                  <div className="progress" style={{ width: '75%' }}></div>
                </div>
              </div>
              <div className="project-stack">
                <span className="tech-tag">React</span>
                <span className="tech-tag">WebSocket</span>
                <span className="tech-tag">Express</span>
              </div>
            </div>
            <div className="project-card">
              <h3>Next.js 기반 기술 블로그</h3>
              <p className="project-duration">2025.07.01 ~ 2025.08.01</p>
              <div className="progress-bar-container">
                <span>진행률</span>
                <span>20%</span>
                <div className="progress-bar">
                  <div className="progress" style={{ width: '20%' }}></div>
                </div>
              </div>
              <div className="project-stack">
                <span className="tech-tag">Next.js</span>
                <span className="tech-tag">TailwindCSS</span>
              </div>
            </div>
            <div className="project-card disabled">
              <h3>개인 포트폴리오 사이트</h3>
              <p className="project-duration">2025.04.05 ~ 2025.05.20</p>
              <div className="progress-bar-container">
                <span>진행률</span>
                <span>100%</span>
                <div className="progress-bar">
                  <div className="progress" style={{ width: '100%', backgroundColor: '#CCCBE4' }}></div>
                </div>
              </div>
              <div className="project-stack">
                <span className="tech-tag-disabled">HTML</span>
                <span className="tech-tag-disabled">CSS</span>
                <span className="tech-tag-disabled">JavaScript</span>
              </div>
            </div>
          </div>
          )}
        </section>
      </main>
    </div>
  );
};

export default Mypage;