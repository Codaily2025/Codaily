import React from 'react';
import styles from './ChatbotMessage.module.css';
import chatbotAvatar from '../../assets/chatbot_avartar.png';

const ChatbotMessage = ({ children, isSmall = false }) => (
  <div className={`${styles.messageRow} ${styles.bot}`}>
    <div className={styles.avatar}>
      <img src={chatbotAvatar} alt="Bot Avatar" />
    </div>
    <div className={`${styles.messageBubble} ${styles.botBubble} ${isSmall ? styles.small : ''}`}>
      <div className={styles.messageText}>{children}</div>
    </div>
  </div>
);

export default ChatbotMessage;

