const NicknameCheckButton = ({ 
  onClick, 
  className = '', 
  disabled = false, 
  style = {},
  children = '연동'
}) => {
  const baseStyle = {
    backgroundColor: '#5A597D',
    color: '#fff',
    border: 'none',
    borderRadius: '12px',
    padding: '12px 16px',
    cursor: disabled ? 'not-allowed' : 'pointer',
    fontSize: '14px',
    height: '44px',
    whiteSpace: 'nowrap',
    flexShrink: 0,
    opacity: disabled ? 0.6 : 1,
    ...style // 외부에서 전달받은 스타일로 오버라이드
  }

  return (
    <button
      type="button"
      onClick={disabled ? undefined : onClick}
      disabled={disabled}
      className={className}
      style={baseStyle}
    >
      {children}
    </button>
  )
}

export default NicknameCheckButton