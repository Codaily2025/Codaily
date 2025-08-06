// FE/src/components/ReviewDetailSidebar/icons.jsx
// SVG 아이콘 컴포넌트
import React from 'react';

// 아코디언 아래 화살표
export const ChevronDownIcon = () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <polyline points="6 9 12 15 18 9"></polyline>
    </svg>
  );
  
  // 아코디언 위 화살표
  export const ChevronUpIcon = () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <polyline points="18 15 12 9 6 15"></polyline>
    </svg>
  );
  
  // 코딩 컨벤션 아이콘
  export const FileIcon = ({ className }) => (
    <svg xmlns="http://www.w3.org/2000/svg" width="30" height="30" viewBox="0 0 30 30" fill="none">
      <path d="M20 22.5L27.5 15L20 7.5M10 7.5L2.5 15L10 22.5" stroke="#404040" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  );
  
  // 성능 최적화 아이콘
  export const PerformanceIcon = ({ className }) => (
    <svg xmlns="http://www.w3.org/2000/svg" width="26" height="27" viewBox="0 0 26 27" fill="none">
      <path d="M14.25 1L1.75 16H13L11.75 26L24.25 11H13L14.25 1Z" stroke="#404040" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  );
  
  // 보안 취약점 아이콘
  export const SecurityIcon = ({ className }) => (
    <svg xmlns="http://www.w3.org/2000/svg" width="28" height="30" viewBox="0 0 28 30" fill="none">
      <path d="M14.0008 29C14.0008 29 25.2008 23.4 25.2008 15V5.2L14.0008 1L2.80078 5.2V15C2.80078 23.4 14.0008 29 14.0008 29Z" stroke="#404040" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  );
  
  // 코드 복잡도 아이콘
  export const ComplexityIcon = ({ className }) => (
    <svg xmlns="http://www.w3.org/2000/svg" width="30" height="30" viewBox="0 0 30 30" fill="none">
      <path d="M22.5 25V12.5M15 25V5M7.5 25V17.5" stroke="#404040" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  );
  
  // 버그 위험도 아이콘
  export const BugRiskIcon = ({ className }) => (
    <svg xmlns="http://www.w3.org/2000/svg" width="30" height="30" viewBox="0 0 30 30" fill="none">
      <path d="M15.0014 11.25V16.25M15.0014 21.25H15.0139M12.8639 4.82501L2.27643 22.5C2.05814 22.878 1.94264 23.3066 1.94142 23.7432C1.94019 24.1797 2.05329 24.6089 2.26946 24.9882C2.48563 25.3674 2.79734 25.6834 3.17356 25.9048C3.54979 26.1262 3.97743 26.2452 4.41393 26.25H25.5889C26.0254 26.2452 26.4531 26.1262 26.8293 25.9048C27.2055 25.6834 27.5172 25.3674 27.7334 24.9882C27.9496 24.6089 28.0627 24.1797 28.0614 23.7432C28.0602 23.3066 27.9447 22.878 27.7264 22.5L17.1389 4.82501C16.9161 4.45764 16.6023 4.15391 16.2279 3.94312C15.8535 3.73232 15.4311 3.62158 15.0014 3.62158C14.5718 3.62158 14.1493 3.73232 13.7749 3.94312C13.4005 4.15391 13.0868 4.45764 12.8639 4.82501Z" stroke="#404040" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  );
  
  // 리팩토링 아이콘
  export const RefactoringIcon = ({ className }) => (
    <svg xmlns="http://www.w3.org/2000/svg" width="30" height="30" viewBox="0 0 30 30" fill="none">
      <path d="M21.25 1.25L26.25 6.25M26.25 6.25L21.25 11.25M26.25 6.25H8.75C7.42392 6.25 6.15215 6.77678 5.21447 7.71447C4.27678 8.65215 3.75 9.92392 3.75 11.25V13.75M8.75 28.75L3.75 23.75M3.75 23.75L8.75 18.75M3.75 23.75H21.25C22.5761 23.75 23.8479 23.2232 24.7855 22.2855C25.7232 21.3479 26.25 20.0761 26.25 18.75V16.25" stroke="#404040" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  );
  
  // 재분석 아이콘
  export const RefreshIcon = ({ className }) => (
    <svg className={className} xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <polyline points="23 4 23 10 17 10"></polyline>
        <polyline points="1 20 1 14 7 14"></polyline>
        <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
    </svg>
  );