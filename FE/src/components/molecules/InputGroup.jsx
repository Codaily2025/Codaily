import React from 'react'
import FieldLabel from '@/components/atoms/FieldLabel'
import TextInput from '@/components/atoms/TextInput'

const InputGroup = ({ 
    label, 
    fieldName, 
    placeholder, 
    type = 'text', 
    className = '', 
    labelClassName = '',
    inputClassName = '',
    children 
}) => {
    const inputId = `input-${fieldName}`

    return (
        <div className={className}>
            {label && (
                <FieldLabel htmlFor={inputId} className={labelClassName}>
                    {label}
                </FieldLabel>
            )}
            <TextInput 
                id={inputId}
                fieldName={fieldName}
                placeholder={placeholder}
                type={type}
                className={inputClassName}
            />
            {children}
        </div>
    )
}

export default InputGroup