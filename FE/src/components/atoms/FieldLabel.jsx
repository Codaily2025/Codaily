import React from 'react'

const FieldLabel = ({ children, htmlFor, className = '' }) => {
    return (
        <label 
            htmlFor={htmlFor}
            className={className}
        >
            {children}
        </label>
    )
}

export default FieldLabel
