import React, { useState } from 'react';
import Calendar from '../organisms/Calendar'
import TaskCard from '@/components/molecules/TaskCard'
import KanbanBoard from '@/components/organisms/KanbanBoard'
import Sidebar from '@/components/organisms/Sidebar'
import { Menu } from 'lucide-react'


// 확인용 하드코딩 데이터
const mockTasks = [
  {
    id: 1,
    category: '회원',
    title: '일반 로그인 구현',
    details: 'JwtTokenProvider 클래스 구현\nJwtAuthenticationFilter 구현',
    tags: [
      { text: '매우높음', type: 'high' }
    ]
  },
  {
    id: 2,
    category: '회원',
    title: '일반 로그인 구현',
    details: 'JwtTokenProvider 클래스 구현\nJwtAuthenticationFilter 구현',
    tags: [
      { text: '매우높음', type: 'high' }
    ]
  },
  {
    id: 3,
    category: '회원',
    title: '일반 로그인 구현',
    details: 'JwtTokenProvider 클래스 구현\nJwtAuthenticationFilter 구현',
    tags: [
      { text: '매우높음', type: 'high' }
    ]
  }
]

const ProjectTemplate = () => {
  // 사이드바 
  const [sidebarOpen, setSidebarOpen] = useState(false)

  return (
    <div className='project-template'>
      <button
        className='menu-button'
        onClick={() => setSidebarOpen(true)}
      >
        <Menu size={20} />
      </button>

      <Sidebar
        isOpen={sidebarOpen}
        onClose={() => setSidebarOpen(false)}
      />

      {/* 상단 영역 - 2개 컬럼 */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: '2fr 1fr',
        gap: '20px',
        marginBottom: '20px'
      }}>
        {/* 첫 번째 영역 - 일정별 호출 */}
        <div style={{
          boxShadow: '1px 1px 1px rgba(0, 0, 0, 0.25)',
          borderRadius: '16px',
          padding: '20px',
          backgroundColor: '#FAF9FD',
          minHeight: '400px'
        }}>
          {/* 달력 렌더링 */}
          <Calendar />
        </div>

        {/* 두 번째 영역 - 오늘 할 일 */}
        <div style={{
          boxShadow: '1px 1px 1px rgba(0, 0, 0, 0.25)',
          borderRadius: '16px',
          padding: '20px',
          backgroundColor: '#FAF9FD',
          minHeight: '400px'
        }}>
          <h2 style={{ margin: '0 0 20px 0', fontSize: '18px', fontWeight: 'bold' }}>
            오늘 할 일
          </h2>
          {/* TaskCard 리스트 렌더링 */}
          <div className="task-list">
            {mockTasks.map((task) => (
              <TaskCard
                key={task.id}
                category={task.category}
                title={task.title}
                details={task.details}
                tags={task.tags}
                score={task.score}
                scoreColor={task.scoreColor}
              />
            ))}
          </div>
        </div>
      </div>

      {/* 세 번째 영역 - 칸반 보드 */}
      <div style={{
        boxShadow: '1px 1px 1px rgba(0, 0, 0, 0.25)',
        borderRadius: '16px',
        padding: '20px',
        backgroundColor: '#FAF9FD',
        minHeight: '300px'
      }}>
        <h2 style={{ margin: '0 0 20px 0', fontSize: '18px', fontWeight: 'bold' }}>
          DB 구축 | 로그인 | 회원가입
        </h2>
        {/* 칸반 보드 렌더링 */}
        <KanbanBoard />
      </div>


    </div>
  );
};

export default ProjectTemplate;