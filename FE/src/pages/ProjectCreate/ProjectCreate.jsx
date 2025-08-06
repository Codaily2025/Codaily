import React from 'react';
import './ProjectCreate.css';
import { useNavigate } from 'react-router-dom';

const ProjectCreate = () => {
  const navigate = useNavigate();
  return (
    <div className="project-create-container">
      <h1>프로젝트 생성</h1>
      <button onClick={() => {
        navigate('/project/create/step2');
      }}>
        다음으로
      </button>
    </div>
  );
};

export default ProjectCreate; 