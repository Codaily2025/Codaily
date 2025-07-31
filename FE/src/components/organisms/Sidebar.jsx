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
      {isOpen && <div className="sidebar-overlay" onClick={onClose} />}
      
      {/* Sidebar */}
      <div className={`sidebar ${isOpen ? 'sidebar-open' : 'sidebar-closed'}`}>
        <div className="sidebar-header">
          <h2 className="sidebar-title">My Projects</h2>
          <button className="sidebar-close-btn" onClick={onClose}>
            <X size={20} />
          </button>
        </div>
        
        <div className="sidebar-content">
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