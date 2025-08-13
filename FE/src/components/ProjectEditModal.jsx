// FE/src/components/ProjectEditModal.jsx
import React, { useState, useRef, useEffect, memo } from 'react';
import styles from './ProjectEditModal.module.css';
import { useProjectStore } from '../stores/mypageProjectStore';
import { useUpdateProjectMutation } from '../queries/useProjectMutation';
import { useGetProjectDetailMutation } from '../queries/useProjectMutation';
import { useGithubRepositoriesQuery, useCreateNewGithubRepoMutation, useLinkGithubRepoMutation } from '../queries/useGitHub';

// SVG 아이콘들
const CloseIcon = () => (
  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
    <path d="M18 6L6 18M6 6L18 18" stroke="#737373" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
  </svg>
);

const CalendarIcon = () => (
  <svg width="17" height="18.68" viewBox="0 0 20 22" fill="none" xmlns="http://www.w3.org/2000/svg">
    <path fillRule="evenodd" clipRule="evenodd" d="M6.90698 21.9651C1.15698 21.9651 0 19.2591 0 14.8871V8.58707C0 4.03607 1.44702 2.02206 5.11603 1.60208V0.775085C5.1311 0.362122 5.47028 0.0351562 5.88348 0.0351562C6.29669 0.0351562 6.63586 0.362122 6.65094 0.775085L6.651 1.54108H13.302V0.775085C13.3171 0.362122 13.6563 0.0351562 14.0695 0.0351562C14.4827 0.0351562 14.8219 0.362122 14.837 0.775085V1.60208C18.505 2.02209 19.953 4.03607 19.953 8.58707V14.8871C19.953 19.2581 18.797 21.9651 13.046 21.9651H6.90698ZM6.90698 20.4321H13.047C16.479 20.4321 17.775 19.5321 18.216 17.4711H1.73798C2.17896 19.5331 3.47498 20.432 6.90698 20.432V20.4321ZM18.396 15.9391C18.412 15.6081 18.419 15.2581 18.419 14.8871V8.58707C18.419 4.56805 17.213 3.44107 14.838 3.14206V3.84207C14.8229 4.25504 14.4837 4.582 14.0705 4.582C13.6573 4.582 13.3181 4.25504 13.303 3.84207V3.07608H6.65204V3.84207C6.63696 4.25504 6.29779 4.582 5.88458 4.582C5.47137 4.582 5.1322 4.25504 5.11713 3.84207L5.11707 3.14206C2.75507 3.43707 1.53607 4.55307 1.53607 8.58905V14.889C1.53607 15.26 1.54309 15.611 1.55908 15.941L18.396 15.9391Z" fill="#737373" />
  </svg>
);

const FolderIcon = () => (
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M2.66634 13.3332C2.29967 13.3332 1.98579 13.2026 1.72467 12.9415C1.46356 12.6804 1.33301 12.3665 1.33301 11.9998V3.99984C1.33301 3.63317 1.46356 3.31928 1.72467 3.05817C1.98579 2.79706 2.29967 2.6665 2.66634 2.6665H6.66634L7.99967 3.99984H13.333C13.6997 3.99984 14.0136 4.13039 14.2747 4.3915C14.5358 4.65261 14.6663 4.9665 14.6663 5.33317V11.9998C14.6663 12.3665 14.5358 12.6804 14.2747 12.9415C14.0136 13.2026 13.6997 13.3332 13.333 13.3332H2.66634ZM2.66634 11.9998H13.333V5.33317H7.44967L6.11634 3.99984H2.66634V11.9998Z" fill="#6C6B93"/>
    </svg>
);

const CheckIcon = () => (
    <svg width="10" height="10" viewBox="0 0 10 10" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M8.33366 2.5L3.75033 7.08333L1.66699 5" stroke="white" strokeWidth="1.66667" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
);

const AddIcon = () => (
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M7.33301 11.3335H8.66634V8.66683H11.333V7.3335H8.66634V4.66683H7.33301V7.3335H4.66634V8.66683H7.33301V11.3335ZM7.99967 14.6668C7.07745 14.6668 6.21079 14.4946 5.39967 14.1502C4.58856 13.7946 3.88301 13.3168 3.28301 12.7168C2.68301 12.1168 2.20523 11.4113 1.84967 10.6002C1.50523 9.78905 1.33301 8.92238 1.33301 8.00016C1.33301 7.07794 1.50523 6.21127 1.84967 5.40016C2.20523 4.58905 2.68301 3.8835 3.28301 3.2835C3.88301 2.6835 4.58856 2.21127 5.39967 1.86683C6.21079 1.51127 7.07745 1.3335 7.99967 1.3335C8.9219 1.3335 9.78856 1.51127 10.5997 1.86683C11.4108 2.21127 12.1163 2.6835 12.7163 3.2835C13.3163 3.8835 13.7886 4.58905 14.133 5.40016C14.4886 6.21127 14.6663 7.07794 14.6663 8.00016C14.6663 8.92238 14.4886 9.78905 14.133 10.6002C13.7886 11.4113 13.3163 12.1168 12.7163 12.7168C12.1163 13.3168 11.4108 13.7946 10.5997 14.1502C9.78856 14.4946 8.9219 14.6668 7.99967 14.6668ZM7.99967 13.3335C9.48856 13.3335 10.7497 12.8168 11.783 11.7835C12.8163 10.7502 13.333 9.48905 13.333 8.00016C13.333 6.51127 12.8163 5.25016 11.783 4.21683C10.7497 3.1835 9.48856 2.66683 7.99967 2.66683C6.51079 2.66683 5.24967 3.1835 4.21634 4.21683C3.18301 5.25016 2.66634 6.51127 2.66634 8.00016C2.66634 9.48905 3.18301 10.7502 4.21634 11.7835C5.24967 12.8168 6.51079 13.3335 7.99967 13.3335Z" fill="#8483AB"/>
    </svg>
);

const RepoIcon = () => (
  <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
    <g clipPath="url(#clip0_498_8103)">
      <path d="M1.33301 7.99988L7.76116 11.214C7.84862 11.2577 7.89234 11.2795 7.93821 11.2882C7.97883 11.2958 8.02052 11.2958 8.06114 11.2882C8.10701 11.2795 8.15073 11.2577 8.23819 11.214L14.6663 7.99988M1.33301 11.3332L7.76116 14.5473C7.84862 14.591 7.89234 14.6129 7.93821 14.6215C7.97883 14.6291 8.02052 14.6291 8.06114 14.6215C8.10701 14.6129 8.15073 14.591 8.23819 14.5473L14.6663 11.3332M1.33301 4.66655L7.76116 1.45247C7.84862 1.40874 7.89234 1.38688 7.93821 1.37827C7.97883 1.37065 8.02052 1.37065 8.06114 1.37827C8.10701 1.38688 8.15073 1.40874 8.23819 1.45247L14.6663 4.66655L8.23819 7.88062C8.15073 7.92435 8.10701 7.94621 8.06114 7.95482C8.02052 7.96244 7.97883 7.96244 7.93821 7.95482C7.89234 7.94621 7.84862 7.92435 7.76116 7.88062L1.33301 4.66655Z" stroke="#8483AB" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
    </g>
    <defs>
      <clipPath id="clip0_498_8103">
        <rect width="16" height="16" fill="white"/>
      </clipPath>
    </defs>
  </svg>
);
const ProjectEditModal = ({ onClose, data, onSave, userId }) => {
  // console.log('project:', data?.title)
  
  // useProjectStore에서 프로젝트 정보 가져오기
  const { projects, projectDetail, getProjectDetail } = useProjectStore();
  
  // data prop으로 전달된 프로젝트 ID를 사용하여 스토어에서 프로젝트 정보 찾기
  const projectFromStore = data?.id ? projects.find(p => p.id === data.id) : null;
  
  // 스토어에서 가져온 정보가 있으면 사용하고, 없으면 data prop 사용
  const projectData = projectFromStore || data;

  // 프로젝트 상세 조회 뮤테이션
  const { mutate: fetchProjectDetail } = useGetProjectDetailMutation();

  // GitHub 레포지토리 목록 조회
  const { data: githubReposData, isLoading: isLoadingRepos } = useGithubRepositoriesQuery();
  
  // 새로운 GitHub 레포지토리 생성 뮤테이션
  const { mutate: createNewRepo, isPending: isCreatingRepo } = useCreateNewGithubRepoMutation((repoName) => {
    setSelectedRepoOption(0); // 현재 레포지토리로 변경
    setNewRepoName('');
    // 프로젝트 상세 정보를 다시 불러와서 최신 상태로 업데이트
    fetchProjectDetail(projectData.id);
    alert('새로운 레포지토리가 성공적으로 생성되었습니다.');
  });

  // 프로젝트 상세 조회 뮤테이션 실행 -> projectData가 변경될 때마다 실행
  // 뮤테이션이란? 데이터를 변경하는 함수
  useEffect(() => {
    if (projectData) {
      fetchProjectDetail(projectData.id);
    }
  }, [projectData, fetchProjectDetail]);

  // 프로젝트 수정 뮤테이션
  const { mutate, isPending } = useUpdateProjectMutation();
  
  // 스토어에서 상세 정보 가져오기
  const detailFromStore = getProjectDetail();
  
  // project가 없을 때 기본값 처리
  if (!projectData) {
    console.log('project is null')
    return (
      <div className={styles.modalOverlay} onClick={onClose}>
        <div className={styles.modalContainer} onClick={(e) => e.stopPropagation()}>
          <div className={styles.modalHeaderWrapper}>
            <div className={styles.modalHeader}>
              <div className={styles.modalHeaderText}>
                <div className={styles.modalTitle}>프로젝트 설정</div>
                <div className={styles.modalSubtitle}>프로젝트 정보를 불러올 수 없습니다.</div>
              </div>
            </div>
            <div className={styles.closeButtonWrapper} onClick={onClose}>
              <CloseIcon />
            </div>
          </div>
          <div className={styles.modalBody}>
            <div style={{ textAlign: 'center', padding: '20px' }}>
              프로젝트 정보가 없습니다.
            </div>
          </div>
          <div className={styles.modalFooter}>
            <div className={styles.buttonGroup}>
              <button className={styles.btnSecondary} onClick={onClose}>닫기</button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // 기존 duration을 시작일과 종료일로 분리
  const parseDuration = (duration) => {
    if (!duration) return { startDate: '', endDate: '' };
    
    // "2025-08-01 ~ 2025-11-30" 형식을 파싱
    const parts = duration.split(' ~ ');
    if (parts.length === 2) {
      return { startDate: parts[0].replace(/-/g, '.'), endDate: parts[1].replace(/-/g, '.') };
    }
    return { startDate: '', endDate: '' };
  };
  
  // console.log('프로젝트 기간:', projectData?.duration)  // project.duration: 2025-08-01 ~ 2025-11-30
  const { startDate: initialStartDate, endDate: initialEndDate } = parseDuration(projectData?.duration);

  // 상세 정보에서 시작일과 종료일 가져오기 (우선순위: 상세 정보 > 기존 데이터)
  const finalStartDate = detailFromStore?.startDate?.replace(/-/g, '.') || initialStartDate;
  const finalEndDate = detailFromStore?.endDate?.replace(/-/g, '.') || initialEndDate;
  
  // 상세 정보에서 요일별 투자 시간 가져오기 (우선순위: 상세 정보 > 기존 데이터)
  const finalTimeByDay = detailFromStore?.timeByDay || projectData?.timeByDay || { 월: 0, 화: 0, 수: 0, 목: 0, 금: 0, 토: 0, 일: 0 };

  const [projectDetails, setProjectDetails] = useState({
    projectName: detailFromStore?.title || projectData?.title || '', // 프로젝트 이름
    startDate: finalStartDate, // 시작일
    endDate: finalEndDate, // 종료일
    repoUrl: projectData?.repoUrl || '', // 저장소 URL
    timeByDay: finalTimeByDay, // 요일별 투자 시간
  });

  // console.log('현재프로젝트 정보:', projectDetails)
  console.log('스토어에서 가져온 상세 정보:', detailFromStore)

  // 상세 정보가 업데이트될 때마다 컴포넌트 상태 업데이트
  useEffect(() => {
    if (detailFromStore) {
      const finalStartDate = detailFromStore.startDate?.replace(/-/g, '.') || initialStartDate;
      const finalEndDate = detailFromStore.endDate?.replace(/-/g, '.') || initialEndDate;
      const finalTimeByDay = detailFromStore.timeByDay || { 월: 0, 화: 0, 수: 0, 목: 0, 금: 0, 토: 0, 일: 0 };

      setProjectDetails(prev => ({
        ...prev,
        projectName: detailFromStore.title || prev.projectName,
        startDate: finalStartDate,
        endDate: finalEndDate,
        timeByDay: finalTimeByDay,
      }));
    }
  }, [detailFromStore, initialStartDate, initialEndDate]);

  // 에러 상태 관리
  const [errors, setErrors] = useState({ projectName: false });

  // 날짜 선택 관련 상태
  const [showStartCalendar, setShowStartCalendar] = useState(false);
  const [showEndCalendar, setShowEndCalendar] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setProjectDetails(prev => ({ ...prev, [name]: value }));
    // setFormData(prev => ({ ...prev, [name]: value }));
  };

  // 날짜 선택 핸들러
  const handleDateSelect = (date, type) => {
    const formattedDate = date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    }).replace(/\.\s*/g, '.').replace(/\.$/, '');

    if (type === 'start') {
      setProjectDetails(prev => ({ ...prev, startDate: formattedDate }));
      setShowStartCalendar(false);
    } else {
      setProjectDetails(prev => ({ ...prev, endDate: formattedDate }));
      setShowEndCalendar(false);
    }
  };

  // 캘린더 컴포넌트
  const Calendar = ({ onDateSelect, onClose, selectedDate }) => {
    const [currentMonth, setCurrentMonth] = useState(new Date());
    
    const getDaysInMonth = (date) => {
      const year = date.getFullYear();
      const month = date.getMonth();
      const firstDay = new Date(year, month, 1);
      const lastDay = new Date(year, month + 1, 0);
      const daysInMonth = lastDay.getDate();
      const startingDay = firstDay.getDay();
      
      const days = [];
      for (let i = 0; i < startingDay; i++) {
        days.push(null);
      }
      for (let i = 1; i <= daysInMonth; i++) {
        days.push(new Date(year, month, i));
      }
      return days;
    };

    const days = getDaysInMonth(currentMonth);
    const weekDays = ['일', '월', '화', '수', '목', '금', '토'];
    
    const prevMonth = () => {
      setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() - 1, 1));
    };

    const nextMonth = () => {
      setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1, 1));
    };

    return (
      <div className={styles.calendarOverlay} onClick={onClose}>
        <div className={styles.calendarContainer} onClick={(e) => e.stopPropagation()}>
          <div className={styles.calendarHeader}>
            <button onClick={prevMonth}>&lt;</button>
            <span>{currentMonth.getFullYear()}년 {currentMonth.getMonth() + 1}월</span>
            <button onClick={nextMonth}>&gt;</button>
          </div>
          <div className={styles.calendarWeekdays}>
            {weekDays.map(day => (
              <div key={day} className={styles.calendarWeekday}>{day}</div>
            ))}
          </div>
          <div className={styles.calendarDays}>
            {days.map((day, index) => (
              <div
                key={index}
                className={`${styles.calendarDay} ${!day ? styles.empty : ''} ${selectedDate && day && day.toDateString() === selectedDate.toDateString() ? styles.selected : ''}`}
                onClick={() => day && onDateSelect(day)}
              >
                {day ? day.getDate() : ''}
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  };



  const days = ['월', '화', '수', '목', '금', '토', '일'];

  // 요일별 버튼 active 기본 상태 설정
  const [activeDay, setActiveDay] = useState('월'); // 현재 슬라이더 조정 중인 요일

  // 슬라이더 움직이기
  // 진행률 상태값
  // const [progress, setProgress] = useState(70); // 0~100 사이 숫자
  // const [step, setStep] = useState(13);
  const [step, setStep] = useState(0);
  const maxStep = 20; // 30분 단위, 총 10시간

  // activeDay 변경 시 step 업데이트
  useEffect(() => {
    const dayTime = projectDetails.timeByDay[activeDay] || 0;
    setStep(dayTime * 2); // 30분 단위 → 1시간은 step 2
  }, [activeDay, projectDetails.timeByDay]);
  
  // activeDay 기준으로 투자 시간 업데이트
  const handleSliderChange = (newStep) => {
    setStep(newStep); // UI 업데이트
    setProjectDetails(prev => ({
      ...prev,
      timeByDay: {
        ...prev.timeByDay,
        [activeDay]: newStep * 0.5, // 0.5시간 단위 (30분)
      }
    }));
  };

  // 마우스 이벤트
  const sliderRef = useRef(null);
  const handleMouseDown = (e) => {
    const onMouseMove = (e) => {
      if (!sliderRef.current) return;
      const rect = sliderRef.current.getBoundingClientRect();
      const x = Math.min(Math.max(e.clientX - rect.left, 0), rect.width);
      const percent = x / rect.width;
      const newStep = Math.round(percent * maxStep); // 0~20
      handleSliderChange(newStep); // step, timeByDay 모두 반영
      // setStep(newStep);
    };

    const onMouseUp = () => {
      document.removeEventListener('mousemove', onMouseMove);
      document.removeEventListener('mouseup', onMouseUp);
    };

    document.addEventListener('mousemove', onMouseMove);
    document.addEventListener('mouseup', onMouseUp);
  };

  // 슬라이더 현재 진행률 계산
  const percent = (step / maxStep) * 100;

  // 시간 단위로 현재 값(투자시간) 변환
  const formatTime = (step) => {
    const totalMinutes = step * 30;
    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;
    return `${hours}시간 ${minutes > 0 ? minutes + '분' : ''}`;
  };

  const handleSave = (e) => {
    e.preventDefault();

    if (projectDetails.projectName.trim() === '') {
      setErrors(prev => ({ ...prev, projectName: true }));
      return;
    }

    // 시작일과 종료일을 하나의 문자열로 결합
    // const duration = formData.startDate && formData.endDate 
    //   ? `${formData.startDate.replace(/\./g, '-')} ~ ${formData.endDate.replace(/\./g, '-')}`
    //   : '';

    // if (typeof onSave === 'function') {
    //   onSave({
    //     ...(projectData || {}),
    //     title: formData.projectName,
    //     duration: duration,
    //     timeByDay: timeByDay,
    //     repoUrl: formData.repoUrl
    //   });
    // }

    // mutate 함수를 호출하여 API 요청 실행
    mutate({
      userId: userId, // props로 전달받은 userId 사용
      projectId: projectData.id,
      projectData: {
        title: projectDetails.projectName,
        startDate: projectDetails.startDate,
        endDate: projectDetails.endDate,
        timeByDay: projectDetails.timeByDay,
        // repoUrl은 API 명세에 없으므로 여기서는 제외함
      },
      // 기존 DB의 schedules 데이터 전달
      existingSchedules: detailFromStore?.schedules || null,
    }, {
      // API 요청이 성공하면 모달 닫기
      onSuccess: () => {
        onClose();
      }
    });
  };
  // Github 저장소 선택
  const [selectedRepoOption, setSelectedRepoOption] = useState(0); // 0: 현재, 1: 새로 만들기, 2: 기존 연결
  const [newRepoName, setNewRepoName] = useState(''); // 새로운 레포지토리 이름
  const [selectedExistingRepo, setSelectedExistingRepo] = useState(''); // 선택된 기존 레포지토리

  // 새로운 레포지토리 생성 핸들러
  const handleCreateNewRepo = () => {
    if (!newRepoName.trim()) {
      alert('레포지토리 이름을 입력해주세요.');
      return;
    }
    
    createNewRepo({
      projectId: projectData.id,
      repoName: newRepoName.trim()
    });
  };

  // 기존 레포지토리 연결 뮤테이션
  const { mutate: linkExistingRepo, isPending: isLinkingRepo } = useLinkGithubRepoMutation((repoName) => {
    setSelectedRepoOption(0); // 현재 레포지토리로 변경
    setSelectedExistingRepo('');
    // 프로젝트 상세 정보를 다시 불러와서 최신 상태로 업데이트
    fetchProjectDetail(projectData.id);
    alert('기존 레포지토리가 성공적으로 연결되었습니다.');
  });

  // 기존 레포지토리 연결 핸들러
  const handleLinkExistingRepo = () => {
    if (!selectedExistingRepo) {
      alert('연결할 레포지토리를 선택해주세요.');
      return;
    }
    
    linkExistingRepo({
      projectId: projectData.id,
      repoName: selectedExistingRepo
    });
  };

  return (
    <>
      {/* 중앙 정렬용 래퍼 */}
      <div className={styles.modalOverlay} onClick={onClose}>
        <div className={styles.modalContainer} onClick={(e) => e.stopPropagation()}>
          <div className={styles.modalHeaderWrapper}>
            <div className={styles.modalHeader}>
              <div className={styles.modalHeaderText}>
                <div className={styles.modalTitle}>프로젝트 설정</div>
                <div className={styles.modalSubtitle}>프로젝트에 대한 기본 정보를 수정할 수 있어요.</div>
              </div>
            </div>
            <div className={styles.closeButtonWrapper} onClick={onClose}>
              <CloseIcon />
            </div>
            <div className={styles.modalHeaderSpacer} />
          </div>

          <div className={styles.modalBody}>
            <div className={styles.formContainer}>
              <div className={styles.formSection}>
                <div className={styles.labelContainer}>
                  <label className={styles.formLabel}>프로젝트명 *</label>
                  {errors.projectName && (
                    <div className={styles.errorText}>필수 입력란입니다.</div>
                  )}
                </div>
                <div className={`${styles.inputWrapper} ${errors.projectName ? styles.error : ''}`}>
                  <input 
                    type="text" 
                    id="projectName" 
                    name="projectName" 
                    className={styles.inputText} 
                    value={projectDetails.projectName} 
                    onChange={handleChange} 
                  />
                </div>
              </div>

              <div className={styles.formSection}>
                <label className={styles.formLabel}>기간*</label>
                <div className={styles.dateRangeWrapper}>
                  <div className={`${styles.inputWrapper} ${styles.dateInput}`}>
                    <div className={styles.inputWithIcon}>
                      <CalendarIcon />
                      <input 
                        type="text" 
                        id="startDate" 
                        name="startDate" 
                        className={styles.inputText} 
                        value={projectDetails.startDate} 
                        onChange={handleChange} 
                        onClick={() => setShowStartCalendar(true)}
                        readOnly
                      />
                      {showStartCalendar && (
                        <Calendar 
                          onDateSelect={(date) => handleDateSelect(date, 'start')} 
                          onClose={() => setShowStartCalendar(false)} 
                          selectedDate={projectDetails.startDate ? new Date(projectDetails.startDate.replace(/\./g, '-')) : null} 
                        />
                      )}
                    </div>
                  </div>
                  <div className={styles.dateRangeSeparator}>~</div>
                  <div className={`${styles.inputWrapper} ${styles.dateInput}`}>
                    <div className={styles.inputWithIcon}>
                      <CalendarIcon />
                      <input 
                        type="text" 
                        id="endDate" 
                        name="endDate" 
                        className={styles.inputText} 
                        value={projectDetails.endDate} 
                        onChange={handleChange} 
                        onClick={() => setShowEndCalendar(true)}
                        readOnly
                      />
                      {showEndCalendar && (
                        <Calendar 
                          onDateSelect={(date) => handleDateSelect(date, 'end')} 
                          onClose={() => setShowEndCalendar(false)} 
                          selectedDate={projectDetails.endDate ? new Date(projectDetails.endDate.replace(/\./g, '-')) : null} 
                        />
                      )}
                    </div>
                  </div>
                </div>
              </div>
              
              <div className={`${styles.formSection} ${styles.investmentSection}`}>
                <label className={styles.formLabel}>요일별 투자 시간*</label>
                <div className={styles.daySelector}>
                  {days.map((day) => {
                    let className = styles.dayChip;
                    if (day === activeDay) {
                      className += ` ${styles.active}`;
                    } else if (projectDetails.timeByDay[day] > 0) {
                      className += ` ${styles.inactive}`;
                    } else {
                      className += ` ${styles.disabled}`;
                    }

                    return (
                      <div
                        key={day}
                        className={className}
                        onClick={() => setActiveDay(day)}
                      >
                        {day}
                      </div>
                    );
                  })}

                </div>
                <div className={styles.sliderWrapper}>
                  <div className={styles.sliderContainer} ref={sliderRef}>
                      <div className={styles.sliderTrack}>
                          <div 
                            className={styles.sliderProgress} 
                            style={{width: `${percent}%`}}
                          ></div>
                          <div 
                            className={styles.sliderThumb} 
                            // style={{left: `calc(${percent}% - 15px)`}}
                            style={{left: `calc(${percent}% - 11px)`}}
                            onMouseDown={handleMouseDown}>
                            <svg width="25" height="25" viewBox="0 0 30 30" fill="none" xmlns="http://www.w3.org/2000/svg">
                              <g filter="url(#slider-thumb-shadow)">
                              <rect x="1" y="1" width="22" height="22" rx="11" fill="#F5F5F5"/>
                              </g>
                              <defs>
                              <filter id="slider-thumb-shadow" x="0" y="0" width="30" height="30" filterUnits="userSpaceOnUse" colorInterpolationFilters="sRGB">
                              <feFlood floodOpacity="0" result="BackgroundImageFix"/>
                              <feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/>
                              <feOffset dx="3" dy="3"/>
                              <feGaussianBlur stdDeviation="2"/>
                              <feComposite in2="hardAlpha" operator="out"/>
                              <feColorMatrix type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.25 0"/>
                              <feBlend mode="normal" in2="BackgroundImageFix" result="effect1_dropShadow_461_4148"/>
                              <feBlend mode="normal" in="SourceGraphic" in2="effect1_dropShadow_461_4148" result="shape"/>
                              </filter>
                              </defs>
                            </svg>
                          </div>
                      </div>
                  </div>
                </div>
                <div className={styles.sliderTextWrapper}>
                  <div className={styles.sliderText}>{formatTime(step)}</div>
                  {/* <div className="slider-text-subtext">총 투자 시간</div> */}
                </div>
              </div>

              <div className={styles.formSection}>
                <label className={styles.formLabel}>Github 연결*</label>
                <div className={styles.repoOptions}>
                  <div className={`${styles.repoOptionCard} ${selectedRepoOption === 0 ? styles.active : ''}`} onClick={() => setSelectedRepoOption(0)}>
                    <div className={styles.repoOptionContent}>
                      <div className={`${styles.repoIconWrapper} ${styles.folderIcon}`}>
                        <FolderIcon />
                      </div>
                      <div className={styles.repoTextContent}>
                        <div className={`${styles.repoTitle} ${selectedRepoOption === 0 ? '' : styles.repoTitleDark}`}>현재 레포지토리</div>
                        <div className={`${styles.repoUrl} ${selectedRepoOption === 0 ? '' : styles.repoTitleDark}`}>
                          {detailFromStore?.repositories && detailFromStore.repositories.length > 0 
                            ? detailFromStore.repositories[0].repoUrl 
                            : '연결된 레포지토리가 없습니다.'}
                        </div>
                      </div>
                    </div>
                    <div className={`${styles.radioCheckWrapper} ${selectedRepoOption === 0 ? styles.checked : ''}`}>
                      {selectedRepoOption === 0 && <CheckIcon />}
                    </div>
                  </div>
                  
                  <div className={`${styles.repoOptionCard} ${selectedRepoOption === 1 ? styles.active : ''}`} onClick={() => setSelectedRepoOption(1)}>
                    <div className={styles.repoOptionContent}>
                      <div className={`${styles.repoIconWrapper} ${styles.addIcon}`}>
                        <AddIcon />
                      </div>
                      <div className={styles.repoTextContent}>
                        <div className={`${styles.repoTitle} ${selectedRepoOption === 1 ? '' : styles.repoTitleDark}`}>새로운 레포지토리 만들기</div>
                      </div>
                    </div>
                    <div className={`${styles.radioCheckWrapper} ${selectedRepoOption === 1 ? styles.checked : ''}`}>
                      {selectedRepoOption === 1 && <CheckIcon />}
                    </div>
                  </div>

                  {/* 새로운 레포지토리 생성 입력 필드 */}
                  {selectedRepoOption === 1 && (
                    <div className={styles.repoInputSection}>
                      <div className={styles.inputWrapper}>
                        <input
                          type="text"
                          placeholder="새로운 레포지토리 이름을 입력하세요"
                          value={newRepoName}
                          onChange={(e) => setNewRepoName(e.target.value)}
                          className={styles.inputText}
                        />
                      </div>
                      <button 
                        className={`${styles.btn} ${styles.btnPrimary}`}
                        onClick={handleCreateNewRepo}
                        disabled={isCreatingRepo}
                      >
                        {isCreatingRepo ? '생성 중...' : '레포지토리 생성'}
                      </button>
                    </div>
                  )}

                  <div className={`${styles.repoOptionCard} ${selectedRepoOption === 2 ? styles.active : ''}`} onClick={() => setSelectedRepoOption(2)}>
                    <div className={styles.repoOptionContent}>
                      <div className={`${styles.repoIconWrapper} ${styles.repoIcon}`}>
                        <RepoIcon />
                      </div>
                      <div className={styles.repoTextContent}>
                        <div className={`${styles.repoTitle} ${selectedRepoOption === 2 ? '' : styles.repoTitleDark}`}>기존 레포지토리 연결하기</div>
                      </div>
                    </div>
                    <div className={`${styles.radioCheckWrapper} ${selectedRepoOption === 2 ? styles.checked : ''}`}>
                      {selectedRepoOption === 2 && <CheckIcon />}
                    </div>
                  </div>

                  {/* 기존 레포지토리 목록 */}
                  {selectedRepoOption === 2 && (
                    <div className={styles.repoListSection}>
                      {isLoadingRepos ? (
                        <div className={styles.loadingText}>레포지토리 목록을 불러오는 중...</div>
                      ) : githubReposData?.repositories?.length > 0 ? (
                        <div className={styles.repoList}>
                          {githubReposData.repositories.map((repo, index) => (
                            <div
                              key={index}
                              className={`${styles.repoListItem} ${selectedExistingRepo === repo.name ? styles.selected : ''}`}
                              onClick={() => setSelectedExistingRepo(repo.name)}
                            >
                              <div className={styles.repoItemContent}>
                                <div className={styles.repoItemName}>{repo.name}</div>
                                <div className={styles.repoItemUrl}>{repo.htmlUrl}</div>
                                {repo.description && (
                                  <div className={styles.repoItemDescription}>{repo.description}</div>
                                )}
                                <div className={styles.repoItemVisibility}>
                                  {repo.isPrivate ? 'Private' : 'Public'}
                                </div>
                              </div>
                              {selectedExistingRepo === repo.name && (
                                <div className={styles.repoItemCheck}>
                                  <CheckIcon />
                                </div>
                              )}
                            </div>
                          ))}
                        </div>
                      ) : (
                        <div className={styles.noReposText}>연결 가능한 레포지토리가 없습니다.</div>
                      )}
                      
                                             {selectedExistingRepo && (
                         <button 
                           className={`${styles.btn} ${styles.btnPrimary}`}
                           onClick={handleLinkExistingRepo}
                           disabled={isLinkingRepo}
                         >
                           {isLinkingRepo ? '연결 중...' : '선택한 레포지토리 연결'}
                         </button>
                       )}
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>

          <div className={styles.modalFooter}>
            <div className={styles.buttonGroup}>
              <button className={`${styles.btn} ${styles.btnSecondary}`} onClick={onClose}>취소</button>
              <button className={`${styles.btn} ${styles.btnPrimary}`} onClick={handleSave}>확인</button>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default memo(ProjectEditModal);
