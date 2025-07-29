// Label + Input
// full-width도 분리해야하남...
import React from 'react'
import Label from '@/components/atoms/Label'
import Input from '@/components/atoms/Input'

const InputGroup = ({ label, inputProps, className="form-group", children }) => {
    // label 이름 - inputId 맵핑
    // const inputId = `input-${label?.toLowerCase().replace(/\s+/g, '-')}`
    const inputId = `input-${label?.toLowerCase()}`

    return (
        <div className={className}>
            {label && <Label htmlFor={inputId}>{label}</Label>}
            <Input id={inputId} {...inputProps} />
            {children}
        </div>
    )
}

export default InputGroup