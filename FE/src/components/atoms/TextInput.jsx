import React from 'react'
import useFormField from '@/hooks/useFormField'

const TextInput = ({ 
    type = 'text', 
    placeholder, 
    fieldName,
    className = '',
    id,
    style,
    ...props 
}) => {
    const field = fieldName ? useFormField(fieldName) : null
    
    return (
        <input
            type={type}
            placeholder={placeholder}
            value={field?.value || ''}
            onChange={field?.onChange}
            className={className}
            id={id}
            style={style}
            {...props}
        />
    )
}

export default TextInput