import React from 'react'

const Select = ({ 
    options = [],
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
        <select
            value={value}
            onChange={onChange}
            className={className}
            id={id}
            style={style}
            {...props}
        >
            {placeholder && (
                <option value="" disabled>
                    {placeholder}
                </option>
            )}
            {options.map((option, index) => (
                <option key={index} value={option.value}>
                    {option.label}
                </option>
            ))}
        </select>
    )
}

export default Select