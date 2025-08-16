// FE/src/components/ReviewDetailSidebar/IssueItem.jsx
import React from 'react';
import styles from './ReviewDetailSidebar.module.css';
import Tag from './Tag';

// 각 이슈 정보 컴포넌트 (예. 파일, 줄, 설명, 이슈도, 색상)
const IssueItem = ({ file, line, description, level, indicatorColor }) => {
  // level 값을 CSS 클래스명에 맞게 매핑하는 함수
  const getLevelClass = (level) => {
    const levelLower = level.toLowerCase();
    switch (levelLower) {
      case 'high':
      case '높음':
        return 'high';
      case 'medium':
      case '중간':
        return 'medium';
      case 'low':
      case '낮음':
        return 'low';
      default:
        return 'medium'; // 기본값
    }
  };

  // level 값을 한글 텍스트로 변환하는 함수
  const getLevelText = (level) => {
    const levelLower = level.toLowerCase();
    switch (levelLower) {
      case 'high':
      case '높음':
        return '높음';
      case 'medium':
      case '중간':
        return '중간';
      case 'low':
      case '낮음':
        return '낮음';
      default:
        return '중간'; // 기본값
    }
  };

  // level에 따라 indicator 색상을 결정하는 함수
  const getIndicatorColor = (level) => {
    const levelLower = level.toLowerCase();
    switch (levelLower) {
      case 'high':
      case '높음':
        return '#EF4444'; // 빨간색
      case 'medium':
      case '중간':
        return '#EAB308'; // 노란색
      case 'low':
      case '낮음':
        return '#22C55E'; // 초록색
      default:
        return '#EAB308'; // 기본값 (중간)
    }
  };

  return (
    <div className={styles.issueItem}>
      <div className={styles.issueIndicator} style={{ backgroundColor: getIndicatorColor(level) }} />
      <div className={styles.issueContent}>
        <div className={styles.issueHeader}>
          <span className={styles.issueFile}>{file}:{line}</span>
          <Tag 
            text={getLevelText(level)}
            level={getLevelClass(level)}
          />
        </div>
        <p className={styles.issueDescription}>{description}</p>
      </div>
    </div>
  );
};

export default IssueItem;