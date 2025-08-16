import React from 'react'
import ModalOverlay from '@/components/molecules/ModalOverlay'
import ModalContainer from '@/components/molecules/ModalContainer'
import styles from './StatusConfirmModal.module.css'

const STATUS_LABELS = {
  TODO: 'To Do',
  IN_PROGRESS: 'In Progress', 
  DONE: 'Completed'
}

const StatusConfirmModal = ({ data, onClose }) => {
  const { fromStatus, toStatus, featureTitle, onConfirm } = data || {}

  const handleConfirm = () => {
    if (typeof onConfirm === 'function') {
      onConfirm()
    }
    onClose()
  }

  return (
    <ModalOverlay onClick={onClose}>
      <ModalContainer>
        <div className={styles.modalContent} onClick={e => e.stopPropagation()}>
          <div className={styles.modalHeader}>
            <h3 className={styles.modalTitle}>작업 상태 변경 확인</h3>
          </div>
          
          <div className={styles.modalBody}>
            <div className={styles.featureInfo}>
              <span className={styles.featureTitle}>{featureTitle}</span>
            </div>
            
            <div className={styles.statusChangeInfo}>
              <div className={styles.statusText}>
                <span className={styles.fromStatus}>{STATUS_LABELS[fromStatus]}</span>
                <span className={styles.arrow}>→</span>  
                <span className={styles.toStatus}>{STATUS_LABELS[toStatus]}</span>
              </div>
              <p className={styles.confirmText}>상태를 변경하시겠습니까?</p>
              <p className={styles.noteText}>일정이 조정될 수 있습니다.</p>
            </div>
          </div>

          <div className={styles.modalFooter}>
            <button 
              onClick={onClose}
              className={styles.cancelBtn}
            >
              닫기
            </button>
            <button 
              onClick={handleConfirm}
              className={styles.confirmBtn}
            >
              확인
            </button>
          </div>
        </div>
      </ModalContainer>
    </ModalOverlay>
  )
}

export default StatusConfirmModal