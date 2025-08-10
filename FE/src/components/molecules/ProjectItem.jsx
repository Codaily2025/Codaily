import React from 'react'
import { ChevronUp, ChevronDown, FolderOpen, Calendar, BarChart3, CodeXml, Settings } from 'lucide-react'
import styles from './ProjectItem.module.css'

const ProjectItem = ({ 
  project, 
  state = {}, // 상태 기반 방식: 복잡한 className props 대신 단순한 state 객체로 스타일 관리
  onToggle, 
  onClick,
  className = ''
}) => {
  const { isActive = false, isExpanded = false } = state
  
  const subMenuItems = [
    { icon: Calendar, label: 'history' },
    { icon: CodeXml, label: 'code reviews' },
    { icon: Settings, label: 'settings' }
  ]

  // 상태에 따른 스타일 결정
  const getItemClasses = () => {
    const baseClass = styles.projectItem
    const activeClass = isActive ? styles.projectItemActive : ''
    const customClass = className
    return [baseClass, activeClass, customClass].filter(Boolean).join(' ')
  }

  const getHeaderClasses = () => {
    return isActive ? styles.projectHeaderActive : styles.projectHeader
  }

  const getIconClasses = () => {
    return isActive ? styles.projectIconActive : styles.projectIcon
  }

  const getNameClasses = () => {
    return isActive ? styles.projectNameActive : styles.projectName
  }

  const getToggleBtnClasses = () => {
    return isActive ? styles.projectToggleBtnActive : styles.projectToggleBtn
  }

  return (
    <div className={getItemClasses()}>
      <div className={getHeaderClasses()}>
        <div className={styles.projectInfo} onClick={onClick}>
          <FolderOpen size={20} className={getIconClasses()} />
          <span className={getNameClasses()}>{project.name}</span>
        </div>
        <button 
          className={getToggleBtnClasses()}
          onClick={(e) => {
            e.stopPropagation()
            onToggle()
          }}
        >
          {isExpanded ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
        </button>
      </div>
      
      {isExpanded && (
        <div className={styles.projectSubmenu}>
          {subMenuItems.map((item) => (
            <div key={item.label} className={styles.submenuItem}>
              <item.icon size={16} className={styles.submenuIcon} />
              <span className={styles.submenuLabel}>{item.label}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default ProjectItem