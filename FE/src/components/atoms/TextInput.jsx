import React from 'react'

const TextInput = ({ 
    type = 'text', 
    placeholder, 
    fieldName,
    className = '',
    id,
    style,
    value = '',
    onChange,
    ...props 
}) => {
    return (
        <input
            type={type}
            placeholder={placeholder}
            value={value}
            onChange={onChange}
            className={className}
            id={id}
            style={style}
            {...props}
        />
    )
}

export default TextInput