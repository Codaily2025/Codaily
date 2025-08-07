// 입력 필드 컴포넌트
import React from "react"
import useFormField from "@/hooks/useFormField"

const Input = ({ type="text", fieldName, placeholder, id }) => {
    const field = useFormField(fieldName)
    return (
        <input 
            type={type}
            placeholder={placeholder}
            id={id}
            className="form-input"
            onChange={field.onChange}
        />
    )
}

export default Input