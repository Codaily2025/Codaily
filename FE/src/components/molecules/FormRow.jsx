import React from 'react'

const FormRow = ({ children, className = '' }) => {
    return (
        <div className={className}>
            {children}
        </div>
    )
}

export default FormRow