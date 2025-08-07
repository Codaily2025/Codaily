// Label + Input
import React from 'react'
import Label from '@/components/atoms/Label'
import Input from '@/components/atoms/Input'
import useFormField from '../../hooks/useFormField'

const InputGroup = ({ label, fieldName, placeholder, className="form-group", children }) => {
    // label 이름 - inputId 맵핑
    // const inputId = `input-${label?.toLowerCase().replace(/\s+/g, '-')}`
    // const field = useFormField(fieldName)
    const inputId = `input-${label?.toLowerCase()}`

    return (
        <div className={className}>
            {label && <Label htmlFor={inputId}>{label}</Label>}
            <Input 
                id={inputId}
                fieldName={fieldName}
                placeholder={placeholder}
            />
            {children}
        </div>
    )
}

export default InputGroup