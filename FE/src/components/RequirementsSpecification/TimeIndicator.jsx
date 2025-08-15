// src/components/RequirementsSpecification/TimeIndicator.jsx
import React from 'react';
import styles from './TimeIndicator.module.css';

// 10분 단위로 반올림된 시간을 시간 단위로 반환하는 함수
export const getRoundedHours = (hours) => {
  const totalMinutes = Math.round(hours * 60);
  
  // 5분 미만이면 10분으로 표시
  if (totalMinutes < 5) {
    return 10 / 60; // 10분을 시간으로 변환
  }
  
  // 10분 단위로 반올림
  const roundedMinutes = Math.round(totalMinutes / 10) * 10;
  return roundedMinutes / 60; // 분을 시간으로 변환
};

const TimeIndicator = ({ hours }) => {
  const formatTime = (hours) => {
    const totalMinutes = Math.round(hours * 60);
    
    // 5분 미만이면 10분으로 표시
    if (totalMinutes < 5) {
      return '10분';
    }
    
    // 10분 단위로 반올림
    const roundedMinutes = Math.round(totalMinutes / 10) * 10;
    const wholeHours = Math.floor(roundedMinutes / 60);
    const minutes = roundedMinutes % 60;
    
    if (wholeHours === 0) {
      return `${minutes}분`;
    } else if (minutes === 0) {
      return `${wholeHours}시간`;
    } else {
      return `${wholeHours}시간 ${minutes}분`;
    }
  };

  return (
    <div className={styles.timeIndicator}>
      <div className={styles.timeIconContainer}>
        <svg width="16" height="17" viewBox="0 0 16 17" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M8.0026 15.1666C11.6845 15.1666 14.6693 12.1818 14.6693 8.49992C14.6693 4.81802 11.6845 1.83325 8.0026 1.83325C4.32071 1.83325 1.33594 4.81802 1.33594 8.49992C1.33594 12.1818 4.32071 15.1666 8.0026 15.1666Z" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
          <path d="M8 4.5V8.5L10.6667 9.83333" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
      </div>
      <div className={styles.timeTextContainer}>
          <div className={styles.timeValueText}>{formatTime(hours)}</div>
      </div>
    </div>
  );
};

export default TimeIndicator;
