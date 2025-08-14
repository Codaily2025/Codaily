import React, { useEffect } from 'react'
import ModalOverlay from '@/components/molecules/ModalOverlay'
import ModalContainer from '@/components/molecules/ModalContainer'
import AddFeatureContent from '@/components/molecules/AddFeatureContent'

const AddFeatureModal = ({ 
    isOpen, 
    onClose, 
    initialData = null
}) => {
    useEffect(() => {
        const handleEsc = (e) => {
            if (e.key === 'Escape' && isOpen) {
                onClose()
            }
        }
        
        document.addEventListener('keydown', handleEsc)
        return () => document.removeEventListener('keydown', handleEsc)
    }, [isOpen, onClose])

    if (!isOpen) return null

    const handleOverlayClick = (e) => {
        if (e.target === e.currentTarget) {
            onClose()
        }
    }

    return (
        <ModalOverlay onClick={handleOverlayClick}>
            <ModalContainer>
                <AddFeatureContent
                    onClose={onClose}
                    initialData={initialData}
                />
            </ModalContainer>
        </ModalOverlay>
    )
}

export default AddFeatureModal