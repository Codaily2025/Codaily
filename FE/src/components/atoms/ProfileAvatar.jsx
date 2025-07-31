// 프로필 아바타 컴포넌트
import React from 'react'
import { Camera, User } from 'lucide-react';

const ProfileAvatar = () => {
    return (
        <div className='profile-avatar-section'>
            <div className='profile-avatar'>
                <User color='#6C6B93' size={48}></User>
                <div className='camera-icon'>
                    <Camera color="#F3F2FA" size={16} />
                </div>
            </div>
        </div>
    )
}

export default ProfileAvatar