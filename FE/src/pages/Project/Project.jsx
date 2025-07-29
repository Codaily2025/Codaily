import React from 'react';
import { useNavigate, useParams } from 'react-router-dom'; // 추가
import './Project.css';

const Project = () => {
  const { id } = useParams(); // URL에서 id 파라미터 추출
  const navigate = useNavigate(); // navigate 함수 생성

  const handleCreateProject = () => {
    navigate('/project/create'); // /project/create로 이동
  };

  return (
    <div className="project-container">
      <h1>프로젝트 상세 페이지</h1>
      <p>프로젝트 ID: {id}</p>
      <button className="create-project-button" onClick={handleCreateProject}>
        + 새 프로젝트 만들기
      </button>
    </div>
  );
};

export default Project;
