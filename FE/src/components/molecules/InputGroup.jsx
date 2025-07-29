// Label + Input
// full-width도 분리해야하남...
import React from 'react'
import Label from '@/components/atoms/Label'
import Input from '@/components/atoms/Input'

const InputGroup = ({ label, inputProps, children }) => {
    return (
        <div className='form-group'>
            {label && <Label htmlFor="">{label}</Label>}
            <Input />
            {children}
        </div>
    )
}

export default InputGroup

// DOING