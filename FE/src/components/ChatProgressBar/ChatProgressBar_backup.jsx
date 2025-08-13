import React from 'react';
import styles from './ChatProgressBar.module.css';

const ChatProgressBar = () => {
  return (
    <div className={styles.progressBarContainer}>
        <div className={`${styles.progressStep} ${styles.completed}`}>
          <div className={styles.progressIconWrapper}>
            <div className={styles.progressLine} />
            <div className={styles.progressIcon}>✓</div>
          </div>
          <div className={styles.progressLabelWrapper}>
            <div className={styles.progressLabel}>일정 생성</div>
          </div>
        </div>
        <div className={`${styles.progressStep} ${styles.active}`}>
          <div className={styles.progressIconWrapper}>
            <div className={`${styles.progressLine} ${styles.inactive}`} />
            <div className={styles.progressIcon}>2</div>
          </div>
          <div className={styles.progressLabelWrapper}>
            <div className={styles.progressLabel}>요구사항 명세서</div>
          </div>
        </div>
        <div className={styles.progressStep}>
          <div className={styles.progressIconWrapper}>
            <div className={`${styles.progressIcon} ${styles.inactive}`}>3</div>
          </div>
          <div className={styles.progressLabelWrapper}>
            <div className={styles.progressLabel}>GitHub 연동</div>
          </div>
        </div>
      </div>
  );
};

export default ChatProgressBar;