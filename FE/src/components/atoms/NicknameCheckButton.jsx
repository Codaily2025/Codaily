const NicknameCheckButton = ({ onClick }) => {
  return (
    <button
      type="button"
      onClick={onClick}
      style={{
        backgroundColor: '#5A597D',
        color: '#fff',
        border: 'none',
        borderRadius: '12px',
        padding: '8px 16px',
        cursor: 'pointer',
        marginTop: '16px',
      }}
    >
      중복체크
    </button>
  )
}

export default NicknameCheckButton