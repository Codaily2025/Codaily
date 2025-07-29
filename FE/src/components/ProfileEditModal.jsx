import React from 'react';
import './ProfileEditModal.css';

const ProfileEditModal = ({ isOpen, onClose }) => {
  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <h2>회원정보 수정</h2>
        <p>여기에 사용자 정보 수정 폼을 넣을 수 있어요.</p>
        <button onClick={onClose}>닫기</button>
      </div>
    </div>
  );
};

export default ProfileEditModal;
