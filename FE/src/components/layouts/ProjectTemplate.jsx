import React, { useState, useMemo } from 'react';
import Calendar from '../organisms/Calendar'
import TaskCard from '@/components/molecules/TaskCard'
import KanbanBoard from '@/components/organisms/KanbanBoard'
import Sidebar from '@/components/organisms/Sidebar'
import Badge from '@/components/atoms/Badge'
import { Menu } from 'lucide-react'

const ProjectTemplate = ({ currentProject, projects = [] }) => {
  // 사이드바 
  const [sidebarOpen, setSidebarOpen] = useState(false)

  // 프로젝트 데이터에서 오늘 할 일 추출
  const todayTasks = useMemo(() => {
    if (!currentProject?.features) return []
    
    const tasks = []
    currentProject.features.forEach(feature => {
      if (feature.tasks) {
        feature.tasks.forEach(task => {
          // 오늘 날짜와 비교하여 오늘 할 일만 필터링 (데이터 전처리 로직으로 분리 예정)
          if (task.status === 'todo' || task.status === 'in_progress') {
            tasks.push({
              id: task.id,
              category: task.category,
              title: task.title,
              details: task.details,
              tags: [
                { text: task.status === 'in_progress' ? '진행중' : '할일', type: task.status === 'in_progress' ? 'medium' : 'low' }
              ]
            })
          }
        })
      }
    })
    return tasks
  }, [currentProject])

  // 현재 진행 중인 기능 이름들
  const currentFeatures = useMemo(() => {
    if (!currentProject?.features) return "프로젝트 기능"
    
    return currentProject.features
      .filter(feature => feature.status === 'in_progress' || feature.status === 'todo')
      .map(feature => feature.name)
      .join(' | ') || currentProject?.title || "프로젝트 기능"
  }, [currentProject])

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
        projects={projects}     // TODO: props가 아니라 전역에서 가져오기
        isLoading={false}
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
            <Badge content={todayTasks.length} />
          </h2>
          {/* TaskCard 리스트 렌더링 */}
          <div className="task-list">
            {todayTasks.length > 0 ? (
              todayTasks.map((task) => (
                <TaskCard
                  key={task.id}
                  category={task.category}
                  title={task.title}
                  details={task.details}
                  tags={task.tags}
                  score={task.score}
                  scoreColor={task.scoreColor}
                />
              ))
            ) : (
              <div style={{ padding: '20px', textAlign: 'center', color: '#666' }}>
                오늘 할 일이 없습니다.
              </div>
            )}
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
          {currentFeatures}
        </h2>
        {/* 칸반 보드 렌더링 */}
        <KanbanBoard />
      </div>


    </div>
  );
};

export default ProjectTemplate;