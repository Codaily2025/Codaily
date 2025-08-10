import React from 'react'
import ModalOverlay from '@/components/molecules/ModalOverlay'
import ModalContainer from '@/components/molecules/ModalContainer'
import styles from './StatusConfirmModal.module.css'

const STATUS_LABELS = {
  TODO: '해야할 작업',
  IN_PROGRESS: '진행 중인 작업', 
  DONE: '완료한 작업'
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
            <h3 className={styles.modalTitle}>상태 변경 확인</h3>
          </div>
          
          <div className={styles.modalBody}>
            <div className={styles.featureInfo}>
              <span className={styles.featureTitle}>{featureTitle}</span>
            </div>
            
            <div className={styles.statusChangeInfo}>
              <span className={styles.statusText}>
                <span className={styles.fromStatus}>[{STATUS_LABELS[fromStatus]}]</span>
                <span className={styles.arrow}>→</span>  
                <span className={styles.toStatus}>[{STATUS_LABELS[toStatus]}]</span>
              </span>
              <p className={styles.confirmText}>로 변경하시겠습니까?</p>
            </div>
          </div>

          <div className={styles.modalFooter}>
            <button 
              onClick={onClose}
              className={styles.cancelBtn}
            >
              취소
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