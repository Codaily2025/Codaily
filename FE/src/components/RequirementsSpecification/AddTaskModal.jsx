// FE/src/components/RequirementsSpecification/AddTaskModal.jsx
import React, { useState, useEffect } from 'react';
import styles from './AddTaskModal.module.css';

const AddTaskModal = ({ 
  isOpen, 
  onClose, 
  onSubmit, 
  parentTask = null, // 부모 작업 (상세 기능 추가 시 사용)
  taskType = 'main' // 'main' | 'sub' (주 기능 또는 상세 기능)
}) => {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    priorityLevel: 'medium',
    estimatedTime: 1
  });

  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // 모달이 열릴 때마다 폼 초기화
  useEffect(() => {
    if (isOpen) {
      setFormData({
        title: '',
        description: '',
        priorityLevel: 'medium',
        estimatedTime: 1
      });
      setErrors({});
    }
  }, [isOpen]);

  // Priority 변환 함수
  const convertPriorityToNumber = (priority) => {
    switch (priority) {
      case 'high': return 1;
      case 'medium': return 4;
      case 'low': return 8;
      default: return 4;
    }
  };

  // 입력 값 변경 핸들러
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'estimatedTime' ? parseInt(value) || 1 : value
    }));
    
    // 에러 메시지 제거
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  // 폼 유효성 검사
  const validateForm = () => {
    const newErrors = {};
    
    if (!formData.title.trim()) {
      newErrors.title = '제목을 입력해주세요.';
    }
    
    if (!formData.description.trim()) {
      newErrors.description = '설명을 입력해주세요.';
    }
    
    if (formData.estimatedTime < 1) {
      newErrors.estimatedTime = '예상 시간은 1시간 이상이어야 합니다.';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // 제출 핸들러
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    setIsSubmitting(true);
    
    try {
      // API 요청용 데이터 구성
      const requestData = {
        title: formData.title.trim(),
        description: formData.description.trim(),
        priorityLevel: convertPriorityToNumber(formData.priorityLevel),
        estimatedTime: formData.estimatedTime,
        isCustom: true
      };

      // 상세 기능 추가인 경우 parentFeatureId 추가
      if (taskType === 'sub' && parentTask) {
        requestData.parentFeatureId = parentTask.id;
      }

      await onSubmit(requestData);
      onClose();
    } catch (error) {
      console.error('작업 추가 실패:', error);
      // 에러 처리 로직 추가 가능
    } finally {
      setIsSubmitting(false);
    }
  };

  // ESC 키로 모달 닫기
  useEffect(() => {
    const handleEsc = (e) => {
      if (e.key === 'Escape' && isOpen) {
        onClose();
      }
    };
    
    document.addEventListener('keydown', handleEsc);
    return () => document.removeEventListener('keydown', handleEsc);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  return (
    <div className={styles.modalOverlay} onClick={onClose}>
      <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
        <div className={styles.modalHeader}>
          <h2 className={styles.modalTitle}>
            {taskType === 'sub' ? '상세 기능 추가' : '주 기능 추가'}
          </h2>
          <button className={styles.closeButton} onClick={onClose}>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </button>
        </div>

        {parentTask && (
          <div className={styles.parentTaskInfo}>
            <span className={styles.parentTaskLabel}>
              {taskType === 'main' ? '필드:' : '상위 작업:'}
            </span>
            <span className={styles.parentTaskName}>
              {taskType === 'main' ? parentTask.field : parentTask.name}
            </span>
          </div>
        )}

        <form onSubmit={handleSubmit} className={styles.modalForm}>
          <div className={styles.formGroup}>
            <label htmlFor="title" className={styles.formLabel}>
              제목 <span className={styles.required}>*</span>
            </label>
            <input
              type="text"
              id="title"
              name="title"
              value={formData.title}
              onChange={handleChange}
              placeholder="작업 제목을 입력하세요"
              className={`${styles.formInput} ${errors.title ? styles.error : ''}`}
            />
            {errors.title && <span className={styles.errorMessage}>{errors.title}</span>}
          </div>

          <div className={styles.formGroup}>
            <label htmlFor="description" className={styles.formLabel}>
              설명 <span className={styles.required}>*</span>
            </label>
            <textarea
              id="description"
              name="description"
              value={formData.description}
              onChange={handleChange}
              placeholder="작업 설명을 입력하세요"
              rows="4"
              className={`${styles.formTextarea} ${errors.description ? styles.error : ''}`}
            />
            {errors.description && <span className={styles.errorMessage}>{errors.description}</span>}
          </div>

          <div className={styles.formRow}>
            <div className={styles.formGroup}>
              <label htmlFor="priorityLevel" className={styles.formLabel}>
                우선순위 <span className={styles.required}>*</span>
              </label>
              <select
                id="priorityLevel"
                name="priorityLevel"
                value={formData.priorityLevel}
                onChange={handleChange}
                className={styles.formSelect}
              >
                <option value="high">High</option>
                <option value="medium">Medium</option>
                <option value="low">Low</option>
              </select>
            </div>

            <div className={styles.formGroup}>
              <label htmlFor="estimatedTime" className={styles.formLabel}>
                예상 시간 (시간) <span className={styles.required}>*</span>
              </label>
              <input
                type="number"
                id="estimatedTime"
                name="estimatedTime"
                value={formData.estimatedTime}
                onChange={handleChange}
                min="1"
                max="100"
                className={`${styles.formInput} ${errors.estimatedTime ? styles.error : ''}`}
              />
              {errors.estimatedTime && <span className={styles.errorMessage}>{errors.estimatedTime}</span>}
            </div>
          </div>

          <div className={styles.modalActions}>
            <button
              type="button"
              onClick={onClose}
              className={styles.cancelButton}
              disabled={isSubmitting}
            >
              취소
            </button>
            <button
              type="submit"
              className={styles.submitButton}
              disabled={isSubmitting}
            >
              {isSubmitting ? '추가 중...' : '추가'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AddTaskModal;