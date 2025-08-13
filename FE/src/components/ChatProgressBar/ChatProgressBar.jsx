import React from 'react';
import styles from './ChatProgressBar.module.css';

// 단계 인덱스를 props로 전달받음
const ChatProgressBar = ({ currentStep = 0 }) => {
  const getStepClass = (step) => {
    if (step < currentStep) return styles.completed;
    if (step === currentStep) return styles.active;
    return '';
  };

  const getLineClass = (step) => {
    return step < currentStep ? '' : styles.inactive;
  };

  const getIconContent = (step) => {
    if (step < currentStep) return '✓';
    return step + 1;
  };

  const getIconClass = (step) => {
    if (step < currentStep || step === currentStep) return styles.progressIcon;
    return `${styles.progressIcon} ${styles.inactive}`;
  };

  return (
    <div className={styles.progressBarContainer}>
        <div className={`${styles.progressStep} ${getStepClass(0)}`}>
          <div className={styles.progressIconWrapper}>
            <div className={`${styles.progressLine} ${getLineClass(0)}`} />
            <div className={getIconClass(0)}>{getIconContent(0)}</div>
          </div>
          <div className={styles.progressLabelWrapper}>
            <div className={styles.progressLabel}>일정 생성</div>
          </div>
        </div>
        <div className={`${styles.progressStep} ${getStepClass(1)}`}>
          <div className={styles.progressIconWrapper}>
            <div className={`${styles.progressLine} ${getLineClass(1)}`} />
            <div className={getIconClass(1)}>{getIconContent(1)}</div>
          </div>
          <div className={styles.progressLabelWrapper}>
            <div className={styles.progressLabel}>요구사항 명세서</div>
          </div>
        </div>
        <div className={`${styles.progressStep} ${getStepClass(2)}`}>
          <div className={styles.progressIconWrapper}>
            <div className={getIconClass(2)}>{getIconContent(2)}</div>
          </div>
          <div className={styles.progressLabelWrapper}>
            <div className={styles.progressLabel}>GitHub 연동</div>
          </div>
        </div>
      </div>
  );
};

export default ChatProgressBar;