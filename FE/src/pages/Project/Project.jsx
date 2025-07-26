import React from 'react';
import { useNavigate } from 'react-router-dom'; // 👈 추가
import './Project.css';

const Project = () => {
  const navigate = useNavigate(); // 👈 navigate 함수 생성

  const handleCreateProject = () => {
    navigate('/project/create'); // 👈 /project/create로 이동
  };

  return (
    <div className="project-container">
      <h1>프로젝트</h1>
      <button className="create-project-button" onClick={handleCreateProject}>
        + 새 프로젝트 만들기
      </button>
    </div>
  );
};

export default Project;
