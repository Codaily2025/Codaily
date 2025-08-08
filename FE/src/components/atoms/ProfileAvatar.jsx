import React from 'react'
import { Camera, User } from 'lucide-react';

const ProfileAvatar = ({ 
    src, 
    alt, 
    editable = true, 
    onEdit, 
    className = '',
    avatarClassName = '',
    iconClassName = ''
}) => {
    return (
        <div className={className}>
            <div className={avatarClassName} style={{
                backgroundImage: src ? `url(${src})` : 'none',
            }}>
                {!src && <User color='#6C6B93' size={48} />}
                {editable && (
                    <div className={iconClassName} onClick={onEdit}>
                        <Camera color="#F3F2FA" size={16} />
                    </div>
                )}
            </div>
        </div>
    )
}

export default ProfileAvatar