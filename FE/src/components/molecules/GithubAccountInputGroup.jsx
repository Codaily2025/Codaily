import React from 'react'
import FieldLabel from '@/components/atoms/FieldLabel'
import TextInput from '@/components/atoms/TextInput'
import NicknameCheckButton from '@/components/atoms/NicknameCheckButton'

const GithubAccountInputGroup = ({ 
    fieldName, 
    placeholder, 
    onGithubAccountCheck, 
    className = '', 
    labelClassName = '',
    inputClassName = '',
    value,
    onChange,
    error
}) => {
    const inputId = `input-${fieldName}`

    return (
        <div className={className}>
            <FieldLabel htmlFor={inputId} className={labelClassName}>
                Github Account
            </FieldLabel>
            <div style={{ 
                display: 'flex', 
                gap: '8px', 
                alignItems: 'stretch'
            }}>
                <TextInput 
                    id={inputId}
                    fieldName={value !== undefined ? null : fieldName}
                    placeholder={placeholder}
                    value={value}
                    onChange={onChange}
                    className={inputClassName}
                    style={{ flex: 1 }}
                />
                <NicknameCheckButton onClick={onGithubAccountCheck} />
            </div>
            {error && (
                <div style={{
                    color: '#ef4444',
                    fontSize: '12px',
                    marginTop: '4px'
                }}>
                    {error}
                </div>
            )}
        </div>
    )
}

export default GithubAccountInputGroup
