// FE/src/components/ReviewDetailSidebar/Tag.jsx
import React from 'react';
import styles from './ReviewDetailSidebar.module.css';

const Tag = ({ text, level }) => {
  return (
    <span className={`${styles.tag} ${styles[`tag-${level}`]}`}>
      {text}
    </span>
  );
};

export default Tag;