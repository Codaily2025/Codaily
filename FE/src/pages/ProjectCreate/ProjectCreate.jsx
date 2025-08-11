import React, { useState } from 'react';
import './ProjectCreate.css';
import { useNavigate } from 'react-router-dom';
import { authInstance } from '../../apis/axios';

const ProjectCreate = () => {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);

  const handleNextClick = async () => {
    setIsLoading(true);
    
    try {
      // 프로젝트와 명세서 생성
      const res = await authInstance.post('/projects', {
        startDate: '2025-08-10',
        endDate: '2025-09-20',
        availableDates: ['2025-08-12', '2025-08-15', '2025-08-20'],
        workingHours: {
          MONDAY: 4,
          WEDNESDAY: 6,
          FRIDAY: 2,
        },
      });
      
      console.log('프로젝트 생성 응답:', res.data);
      const projectId = res.data?.projectId;
      const specId = res.data?.specId;
      
      if (!projectId || !specId) {
        throw new Error('프로젝트 ID 또는 명세서 ID 생성에 실패했습니다.');
      }
      
      // 생성된 ID들을 URL 파라미터로 전달
      navigate(`/project/create/step2?projectId=${projectId}&specId=${specId}`);
      
    } catch (error) {
      console.error('프로젝트 생성 중 오류:', error);
      alert('프로젝트 생성 중 오류가 발생했습니다. 다시 시도해주세요.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="project-create-container">
      <h1>프로젝트 생성</h1>
      <button 
        onClick={handleNextClick}
        disabled={isLoading}
      >
        {isLoading ? '생성 중...' : '다음으로'}
      </button>
    </div>
  );
};

export default ProjectCreate; 