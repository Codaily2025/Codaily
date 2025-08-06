import React from 'react';
import styles from './Checkbox.module.css';

const Checkbox = ({ checked }) => {
  return (
    (checked) ? (
      <div className={styles.checkbox}>
        <div className={styles.checkboxIcon}>
          <svg width="14" height="15" viewBox="0 0 14 15" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M11.6693 4L5.2526 10.4167L2.33594 7.5" stroke="#5A597D" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
        </div>
      </div>
    ) : (
      <div className={styles.checkbox}>
        <div className={styles.checkboxIcon}>
          <svg width="14" height="15" viewBox="0 0 14 15" fill="none" xmlns="http://www.w3.org/2000/svg">
            {/* <path d="M11.6693 4L5.2526 10.4167L2.33594 7.5" stroke="#5A597D" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/> */}
          </svg>
        </div>
      </div>
    )
  )
}
export default Checkbox;