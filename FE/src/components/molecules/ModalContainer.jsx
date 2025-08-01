const ModalContainer = ({ children }) => {
  return (
    <div
      style={{
        backgroundColor: '#f5efee',
        padding: '24px',
        borderRadius: '12px',
        minWidth: '300px',
        maxWidth: '90%',
        boxShadow: '0 4px 12px rgba(0,0,0,0.2)',
      }}
    >
      {children}
    </div>
  )
}

export default ModalContainer