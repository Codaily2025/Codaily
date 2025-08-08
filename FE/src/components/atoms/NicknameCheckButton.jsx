const NicknameCheckButton = ({ onClick, className = '' }) => {
  return (
    <button
      type="button"
      onClick={onClick}
      className={className}
      style={{
        backgroundColor: '#5A597D',
        color: '#fff',
        border: 'none',
        borderRadius: '12px',
        padding: '12px 16px', // input과 동일한 padding
        cursor: 'pointer',
        fontSize: '14px', // input과 동일한 폰트 사이즈
        height: '44px', // input 높이에 맞춤 (padding 12px * 2 + font line height)
        whiteSpace: 'nowrap',
        flexShrink: 0 // flex shrink 방지
      }}
    >
      중복체크
    </button>
  )
}

export default NicknameCheckButton