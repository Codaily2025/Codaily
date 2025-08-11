const Button = ({ children, onClick, type = 'primary', htmlType = 'button', disabled = false, className = '', style = {} }) => {
    // 기본 공통 스타일
    const baseStyle = {
        border: 'none',
        borderRadius: '12px',
        padding: '8px 16px',
        cursor: 'pointer',
        marginTop: '16px',
        // fontWeight: 'bold',
        fontSize: '14px',
        // transition: 'all 0.2s ease',
    }

    // 종류별 스타일
    // 색상 수정 언제든지 가능, 종류 추가 언제든지 가능
    const typeStyles = {
        primary: {
            backgroundColor: '#5A597D',
            color: '#fff',
        },
        secondary: {
            backgroundColor: '#9C9BC3',
            color: '#374151',
        },
        danger: {
            backgroundColor: '#CD7B9C',
            color: '#fff',
        },
    }

    // 스타일 적용
    // 우선순위: baseStyle << typeStyles
    const combinedStyle = {
        ...baseStyle,
        ...(typeStyles[type] || {}),
        ...style,
    }


    return (
        <button
            type={htmlType}
            onClick={onClick}
            disabled={disabled}
            className={className}
            style={combinedStyle}
        >
            {children}
        </button>
    )
}

export default Button
