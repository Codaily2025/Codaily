import React from 'react'
import Icon from '../atoms/Icon'
import FieldLabel from '../atoms/FieldLabel'

const FieldItem = ({ icon, label, children, className = '', style = {} }) => {
  const baseStyle = {
    display: 'flex',
    alignItems: 'center',
    marginBottom: '20px',
    minHeight: '48px',
    ...style
  }

  const contentStyle = {
    flex: 1,
    display: 'flex',
    alignItems: 'center',
    marginLeft: '32px'
  }

  return (
    <div className={`field-item ${className}`} style={baseStyle}>
      {icon && (
        <Icon style={{ marginRight: '24px' }}>
          {icon}
        </Icon>
      )}
      <FieldLabel>
        {label}
      </FieldLabel>
      <div style={contentStyle}>
        {children}
      </div>
    </div>
  )
}

export default FieldItem