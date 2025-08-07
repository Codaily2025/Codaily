import React from 'react'

const FieldLabel = ({ children, className = '', style = {} }) => {
  const labelStyle = {
    fontSize: '18px',
    fontWeight: 500,
    color: '#666',
    minWidth: '120px',
    ...style
  }

  return (
    <div className={`field-label ${className}`} style={labelStyle}>
      {children}
    </div>
  )
}

export default FieldLabel
