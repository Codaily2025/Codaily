// src/components/TechTag/TechTag.jsx
import React from 'react';
import styles from './TechTag.module.css';

const TechTag = ({ label }) => (
  <div className={styles.techTag}>
    <span className={styles.techTagText}>{label}</span>
  </div>
);

export default TechTag;
