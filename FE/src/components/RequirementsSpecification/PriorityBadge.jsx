// src/components/RequirementsSpecification/PriorityBadge.jsx
import React from 'react';
import styles from './PriorityBadge.module.css';

const levelMap = {
  Low: styles.priorityBadgeLow,
  Normal: styles.priorityBadgeNormal,
  High: styles.priorityBadgeHigh,
};

const priorityTextMap = {
  Low: styles.priorityTextLow,
  Normal: styles.priorityTextNormal,
  High: styles.priorityTextHigh,
};

const PriorityBadge = ({ level }) => (
  <div className={levelMap[level]}>
    <div className={priorityTextMap[level]}>
      <span>{level}</span>
    </div>
  </div>
);

export default PriorityBadge;
