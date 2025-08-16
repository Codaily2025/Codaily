import './ModalContainer.css'

const ModalContainer = ({ children }) => {
  return (
    <div
      className='fade-in'
      style={{
        backgroundColor: '#fff',
        padding: '48px 60px 40px 60px',
        borderRadius: '16px',
        minWidth: '300px',
        maxWidth: '90%',
        boxShadow: '0 4px 12px rgba(0,0,0,0.2)'
      }}
    >
      {children}
    </div>
  )
}

export default ModalContainer