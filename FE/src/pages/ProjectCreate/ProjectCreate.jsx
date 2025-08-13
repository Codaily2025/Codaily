import React, { useState } from 'react'
import './ProjectCreate.css'
// import { useNavigate } from 'react-router-dom'
// import { authInstance } from '../../apis/axios'
import ChatProgressBar from '@/components/ChatProgressBar/ChatProgressBar'
import ProjectScheduleSetter from '@/components/organisms/ProjectScheduleSetter'

const ProjectCreate = () => {
  // const navigate = useNavigate()
  // const [isLoading, setIsLoading] = useState(false)

  return (
    <div className="project-create-container">
      <ChatProgressBar currentStep={0} />
      <ProjectScheduleSetter />
    </div>
  )
}

export default ProjectCreate