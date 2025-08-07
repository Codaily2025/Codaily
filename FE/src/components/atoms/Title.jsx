import React from 'react'

const Title = ({ children, level = 2, className = '', style = {} }) => {
  const Tag = `h${level}`
  
  return (
    <Tag className={className} style={style}>
      {children}
    </Tag>
  )
}

export default Title
