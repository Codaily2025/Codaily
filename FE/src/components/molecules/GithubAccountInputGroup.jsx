import React from 'react'
import FieldLabel from '@/components/atoms/FieldLabel'
import TextInput from '@/components/atoms/TextInput'
import Button from '@/components/atoms/Button'
import { Github } from 'lucide-react'

const GithubAccountInputGroup = ({ 
    fieldName, 
    placeholder, 
    onGithubAccountCheck, 
    className = '', 
    labelClassName = '',
    inputClassName = '',
    value,
    onChange,
    error,
    isConnected = false
}) => {
    const inputId = `input-${fieldName}`

    return (
        <div className={className}>
            <FieldLabel htmlFor={inputId} className={labelClassName}>
                Github Account
            </FieldLabel>
            <div style={{ 
                display: 'flex', 
                flexDirection: 'column',
                gap: '12px'
            }}>
                <Button
                    onClick={onGithubAccountCheck}
                    disabled={isConnected}
                    type={isConnected ? 'secondary' : 'primary'}
                    style={{
                        backgroundColor: isConnected ? '#10b981' : '#8483AB',
                        padding: '16px',
                        borderRadius: '8px',
                        height: '40px',
                        fontSize: '16px',
                        fontWeight: '600',
                        marginTop: '0',
                        opacity: isConnected ? 0.7 : 1,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        cursor: isConnected ? 'not-allowed' : 'pointer'
                    }}
                >
                    {isConnected ? (
                        <span>✓ GitHub 연동 완료</span>
                    ) : (
                        <>
                            <Github size={20} style={{ marginRight: '8px' }} />
                            GitHub 연동
                        </>
                    )}
                </Button>
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
