import TabButton from '@/components/atoms/TabButton'

const KanbanTabs = ({ 
    tabs = [], 
    activeTab, 
    onTabChange, 
    className = '', 
    style = {} 
}) => {
    const containerStyle = {
        display: 'flex',
        gap: '0px',
        marginBottom: '20px',
        borderBottom: '1px solid #e0e0e0',
        position: 'relative',
        paddingLeft: '0px',
        ...style
    }

    if (!tabs || tabs.length === 0) {
        return null
    }

    return (
        <div className={className} style={containerStyle}>
            {tabs.map((tab, index) => (
                <TabButton
                    key={tab}
                    isActive={activeTab === tab}
                    onClick={() => onTabChange(tab)}
                >
                    {tab}
                </TabButton>
            ))}
        </div>
    )
}

export default KanbanTabs