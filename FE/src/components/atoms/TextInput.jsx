const TextInput = ({ value, onChange, placeholder, className = '' }) => {
    return (
        <input 
            type="text" 
            value={value}
            onChange={onChange}
            placeholder={placeholder}
            // className={`text-input ${className}`.trim()}
            className={className}
        />
    )
}

export default TextInput