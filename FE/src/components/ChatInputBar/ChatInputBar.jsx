// src/components/ChatInputBar/ChatInputBar.jsx
import React, { useState, useRef, useEffect } from 'react';
import styles from './ChatInputBar.module.css';

const ChatInputBar = ({ onSend, isSending }) => {
  const [inputValue, setInputValue] = useState('');
  const fileInputRef = useRef(null);
  const textareaRef = useRef(null); // 텍스트 입력 길이에 따라 세로 길이 확장

  // textarea 높이 자동 조절
  const adjustTextareaHeight = () => {
    const ta = textareaRef.current;
    if (!ta) return;
    ta.style.height = 'auto'; // 초기화
    ta.style.height = `${ta.scrollHeight}px`; // scrollHeight 만큼 확장
  };

  // 값 변경 핸들러 자동 조절 호출
  const handleChange = (e) => {
    setInputValue(e.target.value);
    adjustTextareaHeight();
  };

    // 컴포넌트 마운트 시 자동 조절
    useEffect(() => {
      adjustTextareaHeight();
    }, []);

  const handleSend = () => {
    const trimmedText = inputValue.trim();
    if (trimmedText) {
      onSend(trimmedText);
      setInputValue('');
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const openFileInput = () => {
    fileInputRef.current.click();
  };

  const handleFileChange = (e) => {
    const file = e.target.files?.[0];
    if (file) {
      console.log('선택된 파일:', file);
      onSend(file);
      e.target.value = null; // 같은 파일 재선택 가능하도록 초기화
    }
  };

  return (
    <div className={styles.inputBarContainer}>
      <input
        type="file"
        ref={fileInputRef}
        className={styles.hiddenFileInput}
        onChange={handleFileChange}
      />
      {/* 파일 버튼 */}
      <div className={styles.iconWrapper}>
      <button
        type="button"
        className={`${styles.iconButton} ${styles.fileButton}`}
        onClick={openFileInput}
        title="파일 업로드"
      >
        <div className={styles.fileIcon}>
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="19" viewBox="0 0 18 19" fill="none">
            <path d="M15.75 11.75V14.75C15.75 15.1478 15.592 15.5294 15.3107 15.8107C15.0294 16.092 14.6478 16.25 14.25 16.25H3.75C3.35218 16.25 2.97064 16.092 2.68934 15.8107C2.40804 15.5294 2.25 15.1478 2.25 14.75V11.75" stroke="#6C6B93" strokeWidth="1.5" />
            <path d="M12.75 6.49976L9 2.74976L5.25 6.49976" stroke="#6C6B93" strokeWidth="1.5" />
            <path d="M9 2.74976V11.7498" stroke="#6C6B93" strokeWidth="1.5" />
          </svg>
        </div>
      </button>
      </div>

      {/* 텍스트 입력창 */}
      <textarea
        className={styles.textInput}
        placeholder="추가 정보나 요구사항을 입력해주세요..."
        value={inputValue}
        onChange={handleChange}
        ref={textareaRef}
        onKeyDown={handleKeyPress}
        rows={1}
        disabled={isSending}
      />
      {/* <div className={styles.textInputWrapper}>
        <div className={styles.placeholderText}>추가 정보나 요구사항을 입력해주세요...</div>
      </div> */}
      <div className={styles.iconWrapper}>
      <button
        type="button"
        className={`${styles.iconButton} ${styles.sendButton}`}
        onClick={handleSend}
        disabled={isSending || inputValue.trim() === ''}
        title="전송"
      >
        <div className={styles.sendIcon}>
          <svg xmlns="http://www.w3.org/2000/svg" width="19" height="20" viewBox="0 0 19 20" fill="none">
            <path d="M17.5 2.00024L9.25 10.2502" stroke="white" strokeWidth="1.5" />
            <path d="M17.5 2.00024L12.25 17.0002L9.25 10.2502L2.5 7.25024L17.5 2.00024Z" stroke="white" strokeWidth="1.5" />
          </svg>
        </div>
      </button>
      </div>
    </div>
  );
};

export default ChatInputBar;