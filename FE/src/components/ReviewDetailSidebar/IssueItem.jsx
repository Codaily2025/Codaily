// FE/src/components/ReviewDetailSidebar/IssueItem.jsx
import React from 'react';
import styles from './ReviewDetailSidebar.module.css';
import Tag from './Tag';

// 각 이슈 정보 컴포넌트 (예. 파일, 줄, 설명, 이슈도, 색상)
const IssueItem = ({ file, line, description, level, indicatorColor }) => (
  <div className={styles.issueItem}>
    <div className={styles.issueIndicator} style={{ backgroundColor: indicatorColor }} />
    <div className={styles.issueContent}>
      <div className={styles.issueHeader}>
        <span className={styles.issueFile}>{file}:{line}</span>
        <Tag 
          text={
            level.toLowerCase() === 'high'
            ? '높음'
            : level.toLowerCase() === 'medium'
            ? '중간'
            : '낮음'} 
          level={
            level.toLowerCase()
          } 
        />
      </div>
      <p className={styles.issueDescription}>{description}</p>
    </div>
  </div>
);

export default IssueItem;