import React from 'react'
import { ChevronUp, ChevronDown, FolderOpen, Calendar, BarChart3, Settings } from 'lucide-react'

const ProjectItem = ({ project, isExpanded, onToggle }) => {
  const subMenuItems = [
    { icon: Calendar, label: 'history' },
    { icon: BarChart3, label: 'insight' },
    { icon: Settings, label: 'settings' }
  ];

  return (
    // className 구체화
    <div className="project-sidebar-item">
      <div className="project-sidebar-header" onClick={onToggle}>
        <div className="project-sidebar-info">
          <FolderOpen size={20} className="project-icon" />
          <span className="project-sidebar-name">{project.name}</span>
        </div>
        {isExpanded ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
      </div>
      
      {isExpanded && (
        <div className="project-sidebar-submenu">
          {subMenuItems.map((item) => (
            <div key={item.label} className="submenu-item">
              <item.icon size={16} className="submenu-icon" />
              <span className="submenu-label">{item.label}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default ProjectItem