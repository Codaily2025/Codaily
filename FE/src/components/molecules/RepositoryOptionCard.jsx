import Text from '@/components/atoms/Text'

const RepositoryOptionCard = ({ text, selected, onClick, className = '' }) => {
    return (
        <div
            className={`${selected ? 'selected' : ''} ${className}`.trim()}
            onClick={onClick}
        >
            <Text>{text}</Text>
        </div>
    )
}

export default RepositoryOptionCard