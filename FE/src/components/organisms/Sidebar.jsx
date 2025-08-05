import React, { useState } from 'react'
import { X } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import ProjectItem from '@/components/molecules/ProjectItem'

const Sidebar = ({ isOpen, onClose, projects = [], isLoading = false }) => {
  const navigate = useNavigate()
  const [expandedProject, setExpandedProject] = useState(null)

  const handleProjectToggle = (projectName) => {
    setExpandedProject(expandedProject === projectName ? null : projectName)
  }

  const handleProjectClick = (projectId) => {
    navigate(`/project/${projectId}`)
    onClose() // 사이드바 닫기
  }

  return (
    <>
      {/* Overlay */}
      {isOpen && <div className="project-sidebar-overlay" onClick={onClose} />}
      
      {/* Sidebar */}
      <div className={`project-sidebar ${isOpen ? 'project-sidebar-open' : 'project-sidebar-closed'}`}>
        <div className="project-sidebar-header">
          <h2 className="project-sidebar-title">My Projects</h2>
          <button className="project-sidebar-close-btn" onClick={onClose}>
            <X size={20} />
          </button>
        </div>
        
        <div className="project-sidebar-content">
          {isLoading ? (
            <div className="sidebar-loading">프로젝트 목록을 불러오는 중...</div>
          ) : projects.length > 0 ? (
            projects.map((project) => (
              <ProjectItem
                key={project.id}
                project={{ ...project, name: project.title }}
                isExpanded={expandedProject === project.title}
                onToggle={() => handleProjectToggle(project.title)}
                onClick={() => handleProjectClick(project.id)}
              />
            ))
          ) : (
            <div className="no-projects">진행 중인 프로젝트가 없습니다.</div>
          )}
        </div>
      </div>
    </>
  )
}

export default Sidebar