import React from 'react'
import './Badge.css' 

const Badge = ({ content, color = 'red' }) => {
    return (
        <span className={`badge badge-${color}`}>
            {content}
        </span>
    )
}

export default Badge