// src/components/RequirementsSpecification/TimeIndicator.jsx
import React from 'react';
import styles from './TimeIndicator.module.css';

const TimeIndicator = ({ hours }) => (
  <div className={styles.timeIndicator}>
    <div className={styles.timeIconContainer}>
      <svg width="16" height="17" viewBox="0 0 16 17" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M8.0026 15.1666C11.6845 15.1666 14.6693 12.1818 14.6693 8.49992C14.6693 4.81802 11.6845 1.83325 8.0026 1.83325C4.32071 1.83325 1.33594 4.81802 1.33594 8.49992C1.33594 12.1818 4.32071 15.1666 8.0026 15.1666Z" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
        <path d="M8 4.5V8.5L10.6667 9.83333" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
      </svg>
    </div>
    <div className={styles.timeTextContainer}>
        <div className={styles.timeValueText}>{hours}시간</div>
    </div>
  </div>
);

export default TimeIndicator;
