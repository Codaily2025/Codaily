import React, { useState } from 'react';
import './ProjectEditModal.css';

const ProjectEditModal = ({ project, onClose, onSave }) => {
  const [formData, setFormData] = useState({
    projectName: project.title,
    projectDesc: project.description || '',
    projectPeriod: project.duration,
    timeByDay: project.timeByDay || {},
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSave = () => {
    onSave({
      ...project,
      title: formData.projectName,
      description: formData.projectDesc,
      duration: formData.projectPeriod,
      timeByDay: formData.timeByDay,
    });
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <h2>프로젝트 설정 수정</h2>
        <form onSubmit={(e) => { e.preventDefault(); handleSave(); }}>
          <label htmlFor="projectName">프로젝트 이름</label>
          <input type="text" id="projectName" name="projectName" value={formData.projectName} onChange={handleChange} />

          <label htmlFor="projectDesc">설명</label>
          <textarea id="projectDesc" name="projectDesc" rows="3" value={formData.projectDesc} onChange={handleChange} />

          <label htmlFor="projectPeriod">기간</label>
          <input type="text" id="projectPeriod" name="projectPeriod" placeholder="예: 2025.07.28 - 2025.08.31" value={formData.projectPeriod} onChange={handleChange} />

          <label>요일별 투자 시간</label>
          <div className="time-input-group">
            {['월', '화', '수', '목', '금', '토', '일'].map((day) => (
              <div key={day}>
                <label>{day}</label>
                <input
                  type="number"
                  min="0"
                  max="24"
                  value={formData.timeByDay?.[day] || ''}
                  onChange={(e) => {
                    const value = e.target.value;
                    setFormData(prev => ({
                      ...prev,
                      timeByDay: {
                        ...prev.timeByDay,
                        [day]: value,
                      },
                    }));
                  }}
                />
              </div>
            ))}
          </div>

          <div className="modal-actions">
            <button type="button" className="btn-close" onClick={onClose}>취소</button>
            <button type="submit" className="btn-save">저장</button>
          </div>
        </form>
        <button className="modal-close-btn" aria-label="닫기" onClick={onClose}>&times;</button>
      </div>
    </div>
  );
};

export default ProjectEditModal;
