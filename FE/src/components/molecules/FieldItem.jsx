import React from 'react'
import Icon from '../atoms/Icon'
import FieldLabel from '../atoms/FieldLabel'

const FieldItem = ({ icon, label, children, className = '', style = {} }) => {
  const baseStyle = {
    display: 'flex',
    alignItems: 'center',
    marginBottom: '8px',
    minHeight: '48px',
    ...style
  }

  // 아이콘 + 라벨 섹션에 고정 너비를 주어 값들을 일렬 정렬
  const labelSectionStyle = {
    display: 'flex',
    alignItems: 'center',
    width: '180px', // 고정 너비 통해 정렬 구현
    flexShrink: 0
  }

  const contentStyle = {
    flex: 1,
    display: 'flex',
    alignItems: 'center'
  }

  return (
    <div className={`field-item ${className}`} style={baseStyle}>
      <div style={labelSectionStyle}>
        {icon && (
          <Icon style={{ marginRight: '16px' }}>
            {icon}
          </Icon>
        )}
        <FieldLabel style={{ color: '#9ca3af', fontSize: '16px', fontWeight: 500 }}>
          {label}
        </FieldLabel>
      </div>
      <div style={contentStyle}>
        {children}
      </div>
    </div>
  )
}

export default FieldItem