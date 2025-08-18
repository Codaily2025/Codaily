import React from 'react'
import { Check, X } from 'lucide-react'
import ModalOverlay from '@/components/molecules/ModalOverlay'
import ModalContainer from '@/components/molecules/ModalContainer'
import styles from './RetrospectiveResultModal.module.css'

const RetrospectiveResultModal = ({ data, onClose }) => {
  const { success, message } = data || {}
  
  return (
    <ModalOverlay onClick={onClose}>
      <ModalContainer>
        <div className={styles.modalContent} onClick={e => e.stopPropagation()}>
          <div className={styles.modalBody}>
            <div className={styles.iconContainer}>
              {success ? (
                <div className={styles.successIcon}>
                  <Check size={32} />
                </div>
              ) : (
                <div className={styles.errorIcon}>
                  <X size={28} />
                </div>
              )}
            </div>
            
            <div className={styles.messageContainer}>
              <h3 className={styles.title}>
                {success ? '회고 생성 완료' : '회고 생성 실패'}
              </h3>
              <p className={styles.message}>
                {message || (success ? '회고가 성공적으로 생성되었습니다.' : '회고 생성 중 오류가 발생했습니다.')}
              </p>
            </div>
          </div>

          <div className={styles.modalFooter}>
            <button 
              onClick={onClose}
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

export default RetrospectiveResultModal