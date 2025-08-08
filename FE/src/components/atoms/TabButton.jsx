const TabButton = ({ 
    children, 
    isActive = false, 
    onClick, 
    className = '', 
    style = {} 
}) => {
    const baseStyle = {
        padding: '12px 24px',
        border: '1px solid #e0e0e0',
        backgroundColor: isActive ? '#ffffff' : '#f8f9fa',
        color: isActive ? '#333333' : '#666666',
        cursor: 'pointer',
        fontSize: '14px',
        fontWeight: isActive ? '600' : '400',
        borderRadius: '8px 8px 0 0',
        borderBottom: isActive ? '1px solid #ffffff' : '1px solid #e0e0e0',
        position: 'relative',
        zIndex: isActive ? 2 : 1,
        transition: 'all 0.2s ease',
        minWidth: '80px',
        textAlign: 'center',
        ...style
    }

    return (
        <button 
            onClick={onClick}
            className={className}
            style={baseStyle}
        >
            {children}
        </button>
    )
}

export default TabButton