import React, { useState } from 'react'
import Button from '@/components/atoms/Button'
import TextInput from '@/components/atoms/TextInput'
import Textarea from '@/components/atoms/Textarea'
import Select from '@/components/atoms/Select'
import Range from '@/components/atoms/Range'
import FieldLabel from '@/components/atoms/FieldLabel'
import CloseButton from '@/components/atoms/CloseButton'
import useProjectStore from '@/stores/projectStore'
import { useAddFeatures } from '@/hooks/useFeaturesMutation'
import styles from './AddFeatureContent.module.css'

const AddFeatureContent = ({ 
    onClose, 
    initialData = null
}) => {
    const { parentFeatures, currentProject } = useProjectStore()
    const { mutateAsync: addFeaturesManually, isLoading: isMutationLoading } = useAddFeatures()
    const [formData, setFormData] = useState({
        parentFeatureId: initialData?.parentFeatureId || '',
        category: initialData?.category || '',
        title: initialData?.title || '',
        description: initialData?.description || '',
        priorityLevel: initialData?.priorityLevel || 5,
        estimatedTime: initialData?.estimatedTime || 1
    })

    const [errors, setErrors] = useState({})
    const [isSubmitting, setIsSubmitting] = useState(false)

    const handleChange = (field, value) => {
        setFormData(prev => ({
            ...prev,
            [field]: value
        }))
        
        if (errors[field]) {
            setErrors(prev => ({
                ...prev,
                [field]: ''
            }))
        }
    }

    const validateForm = () => {
        const newErrors = {}
        
        if (!formData.category.trim()) {
            newErrors.category = '카테고리를 입력해주세요.'
        }
        
        if (!formData.title.trim()) {
            newErrors.title = '제목을 입력해주세요.'
        }
        
        if (!formData.description.trim()) {
            newErrors.description = '설명을 입력해주세요.'
        }
        
        if (formData.estimatedTime <= 0) {
            newErrors.estimatedTime = '예상 시간은 0시간 이하일 수 없습니다.'
        }

        setErrors(newErrors)
        return Object.keys(newErrors).length === 0
    }

    const handleSubmit = async (e) => {
        e.preventDefault()
        
        if (!validateForm()) {
            return
        }

        setIsSubmitting(true)
        
        try {
            const projectId = currentProject?.projectId || currentProject?.id
            
            // console.log(`currentProject: ${currentProject.projectId}, projectId: ${projectId}`)
            // console.log('수동으로 추가할 기능: ', formData)

            await addFeaturesManually({
                projectId,
                formData
            })
  
            onClose()
        } catch (error) {
            console.error('기능 추가 실패:', error)
        } finally {
            setIsSubmitting(false)
        }
    }

    const parentFeatureOptions = parentFeatures.map(feature => ({
        value: feature.id,
        label: feature.title
    }))

    return (
        <div className={styles.container}>
            <div className={styles.header}>
                <h2 className={styles.title}>기능 추가</h2>
                {/* <CloseButton onClick={onClose} /> */}
            </div>

            <form onSubmit={handleSubmit} className={styles.form}>
                <div className={styles.formGroup}>
                    <FieldLabel htmlFor="parentFeatureId" className={styles.label}>
                        상위 기능 <span className={styles.required}>*</span>
                    </FieldLabel>
                    <Select
                        id="parentFeatureId"
                        value={formData.parentFeatureId}
                        onChange={(e) => handleChange('parentFeatureId', e.target.value)}
                        options={parentFeatureOptions}
                        placeholder="상위 기능을 선택하세요 (선택사항)"
                        className={styles.input}
                    />
                </div>

                <div className={styles.formGroup}>
                    <FieldLabel htmlFor="category" className={styles.label}>
                        카테고리 <span className={styles.required}>*</span>
                    </FieldLabel>
                    <TextInput
                        id="category"
                        value={formData.category}
                        onChange={(e) => handleChange('category', e.target.value)}
                        placeholder="카테고리를 입력하세요"
                        className={`${styles.input} ${errors.category ? styles.error : ''}`}
                    />
                    {errors.category && <span className={styles.errorMessage}>{errors.category}</span>}
                </div>

                <div className={styles.formGroup}>
                    <FieldLabel htmlFor="title" className={styles.label}>
                        제목 <span className={styles.required}>*</span>
                    </FieldLabel>
                    <TextInput
                        id="title"
                        value={formData.title}
                        onChange={(e) => handleChange('title', e.target.value)}
                        placeholder="기능 제목을 입력하세요"
                        className={`${styles.input} ${errors.title ? styles.error : ''}`}
                    />
                    {errors.title && <span className={styles.errorMessage}>{errors.title}</span>}
                </div>

                <div className={styles.formGroup}>
                    <FieldLabel htmlFor="description" className={styles.label}>
                        설명 <span className={styles.required}>*</span>
                    </FieldLabel>
                    <Textarea
                        id="description"
                        value={formData.description}
                        onChange={(e) => handleChange('description', e.target.value)}
                        placeholder="기능 설명을 입력하세요"
                        rows={4}
                        className={`${styles.textarea} ${errors.description ? styles.error : ''}`}
                    />
                    {errors.description && <span className={styles.errorMessage}>{errors.description}</span>}
                </div>

                <div className={styles.formRow}>
                    <div className={styles.formGroup}>
                        <FieldLabel htmlFor="priorityLevel" className={styles.label}>
                            우선순위 ({formData.priorityLevel}) <span className={styles.required}>*</span>
                        </FieldLabel>
                        <Range
                            id="priorityLevel"
                            value={formData.priorityLevel}
                            onChange={(e) => handleChange('priorityLevel', parseInt(e.target.value))}
                            min={1}
                            max={10}
                            className={styles.range}
                        />
                        <div className={styles.rangeLabels}>
                            <span>높음(1)</span>
                            <span>낮음(10)</span>
                        </div>
                    </div>

                    <div className={styles.formGroup}>
                        <FieldLabel htmlFor="estimatedTime" className={styles.label}>
                            예상 시간 (시간) <span className={styles.required}>*</span>
                        </FieldLabel>
                        <TextInput
                            id="estimatedTime"
                            type="number"
                            value={formData.estimatedTime}
                            onChange={(e) => handleChange('estimatedTime', parseInt(e.target.value) || 1)}
                            min="1"
                            max="100"
                            className={`${styles.input} ${errors.estimatedTime ? styles.error : ''}`}
                        />
                        {errors.estimatedTime && <span className={styles.errorMessage}>{errors.estimatedTime}</span>}
                    </div>
                </div>

                <div className={styles.actions}>
                    <Button
                        type="secondary"
                        htmlType="button"
                        onClick={onClose}
                        disabled={isSubmitting}
                    >
                        취소
                    </Button>
                    <Button
                        type="primary"
                        htmlType="submit"
                        disabled={isSubmitting || isMutationLoading}
                    >
                        {isSubmitting || isMutationLoading ? '추가 중...' : '추가'}
                    </Button>
                </div>
            </form>
        </div>
    )
}

export default AddFeatureContent