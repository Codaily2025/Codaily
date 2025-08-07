import React from 'react';
import styles from './ChatUserMessage.module.css';
import chatuserAvatar from '../../assets/chatuser_avartar.png';

const ChatUserMessage = ({ children, isSmall = false }) => (
  <div className={`${styles.messageRow} ${styles.user}`}>
    <div className={`${styles.messageBubble} ${styles.userBubble} ${isSmall ? styles.small : ''}`}>
      <div className={styles.messageText}>{children}</div>
    </div>
    {/* User Avatar는 오른쪽에 있음 */}
    <div className={styles.avatar}>
      <img src={chatuserAvatar} alt="User Avatar" />
    </div>
  </div>
);

export default ChatUserMessage;