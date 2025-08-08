import React from 'react'
import SignupForm from '@/components/organisms/SignupForm'
import './Signup.css'

const Signup = () => {
    return (
        <div style={{
            width: '550px',
            minHeight: '100vh'
        }}>
            <div style={{
                padding: '30px 40px'
            }}>
                <SignupForm />
            </div>
        </div>
    )
}

export default Signup