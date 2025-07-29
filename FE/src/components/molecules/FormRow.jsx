// 여러 InputGroup 가로로 배치하는 컨테이너
import React from 'react'

const FormRow = ({ children }) => {
    return (
        <div className='form-row'>
            {children}
        </div>
    )
}

export default FormRow