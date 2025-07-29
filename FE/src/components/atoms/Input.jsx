// 입력 필드 컴포넌트
import React from "react"

// onChange도 추가?
const Input = ({ type="text", value, placeholder, id }) => {
    return (
        <input 
            type={type} 
            value={value}
            placeholder={placeholder}
            id={id}
            className="form-input"
        />
    )
}

export default Input