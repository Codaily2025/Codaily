// FE/src/components/ReviewDetailSidebar/AnalysisCard.jsx
import React from 'react';
import styles from './ReviewDetailSidebar.module.css';
import Tag from './Tag';
import { ChevronDownIcon, ChevronUpIcon } from './icons';
import IssueItem from './IssueItem';

// 각 평가 항목 아코디언 컴포넌트
const AnalysisCard = ({ icon, title, description, tags, issues, defaultOpen = false, isOpen, onToggle }) => {
  // const [isOpen, setIsOpen] = useState(defaultOpen);

  // 태그를 렌더링할 레벨 순서
  const LEVEL_ORDER = ['high', 'medium', 'low'];

  // 레벨별 한글 라벨 매핑
  const LABEL_KR = {
    high: '높음',
    medium: '중간',
    low: '낮음'
  };

  // 이슈 중요도 정렬
  // 우선순위 정의
  const PRIORITY_ORDER = {
    high: 3,
    medium: 2,
    low: 1
  };

  // 이슈 정렬
  const sortedIssues = issues.slice().sort((a, b) => {
    return PRIORITY_ORDER[b.level.toLowerCase()] - PRIORITY_ORDER[a.level.toLowerCase()];
  })

  return (
    <div className={styles.card}>
      <div className={styles.cardHeader} onClick={onToggle}>
        <div className={styles.cardTitleGroup}>
          {icon}
          <div className={styles.cardTitleText}>
            <h3 className={styles.cardTitle}>{title}</h3>
            <p className={styles.cardDescription}>{description}</p>
          </div>
        </div>
        <div className={styles.cardControls}>
          <div className={styles.tagGroup}>
            {LEVEL_ORDER.map(levelKey => {
              const count = tags[levelKey] || 0;
              if (count === 0) return null;
              return (
                <Tag
                  key={levelKey}
                  text={`${LABEL_KR[levelKey]} ${count}`}
                  level={levelKey}
                />
              );
            })}
          </div>
          {isOpen ? <ChevronUpIcon /> : <ChevronDownIcon />}
        </div>
      </div>
      {isOpen && (
        <div className={styles.cardBodyWrapper}>
          <div className={styles.cardBody}>
            {sortedIssues.map((issue, index) => (
              <IssueItem
                key={index}
                file={issue.file}
                line={issue.line}
                description={issue.description}
                level={issue.level}
                // 이슈 색상(빨강, 주황, 녹색)
                indicatorColor={
                  issue.level.toLowerCase() === 'high'
                    ? '#EF4444'
                    : issue.level.toLowerCase() === 'medium'
                      ? '#EAB308'
                      : '#22C55E'
                }
              />
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default AnalysisCard;