// 폼 라벨 컴포넌트
import React from "react"

const Label = ({ children, htmlFor }) => {
    return (
        // htmlFor: 루프문 for와 겹침 방지
        <label htmlFor={htmlFor} className="form-label">
            {children}
        </label>
    )
}

export default Label