import React from 'react'

const Icon = ({ children, size = 24, color, opacity = 0.6, className = '', style = {} }) => {
  const iconStyle = {
    width: `${size}px`,
    height: `${size}px`,
    color: color,
    opacity: opacity,
    flexShrink: 0,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    ...style
  }

  return (
    <div className={`icon ${className}`} style={iconStyle}>
      {children}
    </div>
  )
}

export default Icon
