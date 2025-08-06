const CloseButton = ({ onClick }) => {
  return (
    <button
      onClick={onClick}
      style={{
        backgroundColor: '#8483AB',
        color: '#fff',
        border: 'none',
        borderRadius: '12px',
        padding: '8px 16px',
        cursor: 'pointer',
        marginTop: '16px',
      }}
    >
      닫기
    </button>
  )
}

export default CloseButton