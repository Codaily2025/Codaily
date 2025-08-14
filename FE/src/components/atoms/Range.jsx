import React from 'react'

const Range = ({ 
    min = 1,
    max = 10,
    step = 1,
    fieldName,
    className = '',
    id,
    style,
    value = 1,
    onChange,
    ...props 
}) => {
    return (
        <input
            type="range"
            min={min}
            max={max}
            step={step}
            value={value}
            onChange={onChange}
            className={className}
            id={id}
            style={style}
            {...props}
        />
    )
}

export default Range