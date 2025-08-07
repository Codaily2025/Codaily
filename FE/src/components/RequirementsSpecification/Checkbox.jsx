import React from 'react';
import styles from './Checkbox.module.css';

const Checkbox = ({ checked, onChange }) => {
  return (
    <div 
    className={styles.checkbox}
    onClick={onChange} // 클릭 시 부모의 onChange 호출
    role="checkbox"
    aria-checked={checked}
    style={{ cursor: 'pointer' }}
    >

      <div className={styles.checkboxIcon}>
    {checked ? (
          <svg width="14" height="15" viewBox="0 0 14 15" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M11.6693 4L5.2526 10.4167L2.33594 7.5" stroke="#5A597D" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
        ) : (
          <svg width="14" height="15" viewBox="0 0 14 15" fill="none" xmlns="http://www.w3.org/2000/svg">
          </svg>
        )}
      </div>
    </div>
  )
}
export default Checkbox;