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
  // 디버깅을 위한 로그
  console.log('AddTaskModal - parentTask:', parentTask);
  console.log('AddTaskModal - taskType:', taskType);
  if (parentTask) {
    console.log('AddTaskModal - parentTask keys:', Object.keys(parentTask));
    console.log('AddTaskModal - parentTask.name:', parentTask.name);
    console.log('AddTaskModal - parentTask.title:', parentTask.title);
    console.log('AddTaskModal - parentTask.field:', parentTask.field);
  }
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    priorityLevel: 'medium',
    estimatedHours: 0,
    estimatedMinutes: 0
  });

  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [openDropdown, setOpenDropdown] = useState(null);

  // 10분 단위 분 옵션 생성
  const minuteOptions = Array.from({ length: 6 }, (_, i) => i * 10);

  // 모달이 열릴 때마다 폼 초기화
  useEffect(() => {
    if (isOpen) {
      setFormData({
        title: '',
        description: '',
        priorityLevel: 'medium',
        estimatedHours: '',
        estimatedMinutes: 0
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

  // 시간과 분을 시간 단위로 환산하는 함수
  const convertToHours = (hours, minutes) => {
    const totalMinutes = hours * 60 + minutes;
    const totalHours = totalMinutes / 60;
    // 소수점 한 자릿수까지 반올림
    return Math.round(totalHours * 10) / 10;
  };

  // 드롭다운 토글 함수
  const toggleDropdown = (dropdownName) => {
    setOpenDropdown(openDropdown === dropdownName ? null : dropdownName);
  };

  // 입력 값 변경 핸들러
  const handleChange = (e) => {
    const { name, value } = e.target;

    // 시간 입력은 타이핑 중에 ''(빈값) 허용 + 숫자만 받기
    if (name === 'estimatedHours') {
      // 숫자만 통과 (최대 3자리), 빈 문자열 허용
      if (value === '' || /^\d{0,3}$/.test(value)) {
        setFormData(prev => ({ ...prev, estimatedHours: value }));
        if (errors.estimatedHours) {
          setErrors(prev => ({ ...prev, estimatedHours: '' }));
        }
      }
      return;
    }

    setFormData(prev => ({
      ...prev,
      // [name]: name === 'estimatedHours' ? parseInt(value) || 1 : 
      // name === 'estimatedMinutes' ? parseInt(value) || 0 : value
      [name]: name === 'estimatedMinutes' ? parseInt(value) || 0 : value
    }));

    // 에러 메시지 제거
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  // 시간 입력 시 블러 이벤트 핸들러
  const handleHoursBlur = () => {
    const raw = formData.estimatedHours;
    if (raw === '') {
      // 분만 입력하는 경우를 위해 빈 값은 0시간으로 확정
      setFormData(prev => ({ ...prev, estimatedHours: '0' }));
      return;
    }
    const n = Number(raw);
    setFormData(prev => ({
      ...prev,
      estimatedHours: Number.isFinite(n) && n >= 0 ? String(n) : '0'
    }));
  };

  // 폼 유효성 검사
  const validateForm = () => {
    const newErrors = {};

    if (!formData.title.trim()) {
      newErrors.title = '제목을 입력해주세요.';
    }

    // 설명은 선택사항이므로 유효성 검사에서 제외

    const hoursNum = Number(formData.estimatedHours || 0);
    const minutesNum = Number(formData.estimatedMinutes || 0);
    if (!Number.isFinite(hoursNum) || hoursNum < 0) {
      newErrors.estimatedHours = '시간은 0 이상이어야 합니다.';
    }
    if (hoursNum === 0 && minutesNum === 0) {
      newErrors.estimatedHours = '예상 시간은 0보다 커야 합니다.'; // 필요시 "최소 10분" 등으로 변경
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
      const hoursNum = Number(formData.estimatedHours || 0);

      // 시간과 분을 시간 단위로 환산
      const estimatedTimeInHours = convertToHours(hoursNum, formData.estimatedMinutes);

      // API 요청용 데이터 구성
      const requestData = {
        title: formData.title.trim(),
        description: formData.description.trim(),
        priorityLevel: convertPriorityToNumber(formData.priorityLevel),
        estimatedTime: estimatedTimeInHours,
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

  // ESC 키로 모달 닫기 및 드롭다운 외부 클릭 시 닫기
  useEffect(() => {
    const handleEsc = (e) => {
      if (e.key === 'Escape' && isOpen) {
        onClose();
      }
    };

    const handleClickOutside = (e) => {
      if (openDropdown && !e.target.closest(`.${styles.dropdownWrapper}`)) {
        setOpenDropdown(null);
      }
    };

    document.addEventListener('keydown', handleEsc);
    document.addEventListener('mousedown', handleClickOutside);
    
    return () => {
      document.removeEventListener('keydown', handleEsc);
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isOpen, onClose, openDropdown]);

  if (!isOpen) return null;

  return (
    <div className={styles.modalOverlay} onClick={onClose}>
      <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
        <div className={styles.modalHeader}>
          <h2 className={styles.modalTitle}>
            {taskType === 'sub' ? '상세 기능 추가' : '주 기능 추가'}
          </h2>
          {/* <button className={styles.closeButton} onClick={onClose}> */}
                     <button onClick={onClose}
             style={{
               backgroundColor: 'transparent',
               border: 'none',
               cursor: 'pointer',
               width: '40px',
               height: '40px',
               alignItems: 'center',
               justifyContent: 'center',
               outline: 'none',
             }}>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
          </button>
        </div>

        {parentTask && (
          <div className={styles.parentTaskInfo}>
            <span className={styles.parentTaskLabel}>
              {taskType === 'main' ? '필드:' : '상위 작업:'}
            </span>
            <span className={styles.parentTaskName}>
              {taskType === 'main' ? parentTask.field : (parentTask.name || parentTask.title)}
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
              설명
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

          <div className={styles.formRow} style={{ marginBottom: '0px' }}>
            <div className={styles.formGroup}>
              <label htmlFor="priorityLevel" className={styles.formLabel}>
                우선순위 <span className={styles.required}>*</span>
              </label>
              <div className={styles.dropdownWrapper}>
                <button
                  type="button"
                  className={`${styles.dropdownButton} ${openDropdown === 'priority' ? styles.open : ''}`}
                  onClick={() => toggleDropdown('priority')}
                >
                  {formData.priorityLevel === 'high' ? 'High' : 
                   formData.priorityLevel === 'medium' ? 'Medium' : 'Low'}
                  <img className={styles.dropdownIcon} src="/src/assets/caret_up.svg" alt="caret" />
                </button>
                {openDropdown === 'priority' && (
                  <div className={styles.dropdownMenu}>
                    {['high', 'medium', 'low'].map((option) => (
                      <div
                        key={option}
                        className={`${styles.dropdownOption} ${option === formData.priorityLevel ? styles.selected : ''}`}
                        onClick={() => {
                          setFormData(prev => ({ ...prev, priorityLevel: option }));
                          setOpenDropdown(null);
                        }}
                      >
                        <div className={styles.optionIcon}>
                          {option === formData.priorityLevel && (
                            <svg
                              width="16"
                              height="16"
                              viewBox="0 0 16 16"
                              fill="none"
                              xmlns="http://www.w3.org/2000/svg"
                            >
                              <path
                                d="M13.3346 4L6.0013 11.3333L2.66797 8"
                                stroke="#5A597D"
                                strokeWidth="1.33333"
                                strokeLinecap="round"
                                strokeLinejoin="round"
                              />
                            </svg>
                          )}
                        </div>
                        <span className={styles.optionLabel}>
                          {option === 'high' ? 'High' : 
                           option === 'medium' ? 'Medium' : 'Low'}
                        </span>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>

            <div className={styles.formGroup}>
              <label className={styles.formLabel}>
                예상 소요 시간 <span className={styles.required}>*</span>
              </label>
              <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                <input
                  type="number"
                  name="estimatedHours"
                  value={formData.estimatedHours}
                  onChange={handleChange}
                  onBlur={handleHoursBlur}
                  min="0"
                  max="999"
                  step="1"
                  inputMode="numeric"
                  pattern="[0-9]*"
                  className={`${styles.formInput} ${errors.estimatedHours ? styles.error : ''}`}
                  style={{ width: '80px' }}
                />
                <span style={{ fontSize: '14px', color: '#666' }}>시간</span>
                <div className={styles.dropdownWrapper} style={{ width: '80px' }}>
                  <button
                    type="button"
                    className={`${styles.dropdownButton} ${openDropdown === 'minutes' ? styles.open : ''}`}
                    onClick={() => toggleDropdown('minutes')}
                    style={{ width: '100%' }}
                  >
                    {formData.estimatedMinutes}
                    <img className={styles.dropdownIcon} src="/src/assets/caret_up.svg" alt="caret" />
                  </button>
                  {openDropdown === 'minutes' && (
                    <div className={styles.dropdownMenu} style={{ width: '80px' }}>
                      {minuteOptions.map((minute) => (
                        <div
                          key={minute}
                          className={`${styles.dropdownOption} ${minute === formData.estimatedMinutes ? styles.selected : ''}`}
                          onClick={() => {
                            setFormData(prev => ({ ...prev, estimatedMinutes: minute }));
                            setOpenDropdown(null);
                          }}
                        >
                          <div className={styles.optionIcon}>
                            {minute === formData.estimatedMinutes && (
                              <svg
                                width="16"
                                height="16"
                                viewBox="0 0 16 16"
                                fill="none"
                                xmlns="http://www.w3.org/2000/svg"
                              >
                                <path
                                  d="M13.3346 4L6.0013 11.3333L2.66797 8"
                                  stroke="#5A597D"
                                  strokeWidth="1.33333"
                                  strokeLinecap="round"
                                  strokeLinejoin="round"
                                />
                              </svg>
                            )}
                          </div>
                          <span className={styles.optionLabel}>{minute}</span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
                <span style={{ fontSize: '14px', color: '#666' }}>분</span>
              </div>
              {errors.estimatedHours && <span className={styles.errorMessage}>{errors.estimatedHours}</span>}
            </div>
          </div>

          <div className={styles.modalActions} style={{ marginTop: '10px' }}>
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