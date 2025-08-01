import React, { useState } from 'react'
import { X } from 'lucide-react'
import ProjectItem from '@/components/molecules/ProjectItem'

const Sidebar = ({ isOpen, onClose }) => {
  const [expandedProject, setExpandedProject] = useState('To do Project')
  
  // 확인용 하드코딩 데이터
  const projects = [
    { id: 1, name: 'Side Project' },
    { id: 2, name: 'Toy Project' },
    { id: 3, name: 'To do Project' }
  ];

  const handleProjectToggle = (projectName) => {
    setExpandedProject(expandedProject === projectName ? null : projectName)
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
          {projects.map((project) => (
            <ProjectItem
              key={project.id}
              project={project}
              isExpanded={expandedProject === project.name}
              onToggle={() => handleProjectToggle(project.name)}
            />
          ))}
        </div>
      </div>
    </>
  )
}

export default Sidebar