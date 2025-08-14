import React from 'react'

const Textarea = ({ 
    placeholder, 
    fieldName,
    className = '',
    id,
    style,
    value = '',
    onChange,
    rows = 4,
    ...props 
}) => {
    return (
        <textarea
            placeholder={placeholder}
            value={value}
            onChange={onChange}
            className={className}
            id={id}
            style={style}
            rows={rows}
            {...props}
        />
    )
}

export default Textarea