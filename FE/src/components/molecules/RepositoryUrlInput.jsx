import TextInput from '@/components/atoms/TextInput'

const RepositoryUrlInput = ({ show, value, onChange, className = '', inputClassName = '' }) => {
    return (
        <div className={className}>
            <TextInput 
                placeholder='레포지토리 url을 입력하세요.'
                value={value}
                onChange={onChange}
                className={inputClassName}
            />
        </div>
    )
}

export default RepositoryUrlInput