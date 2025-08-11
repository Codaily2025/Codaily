import React from 'react'
import FieldItem from './FieldItem'
import Badge from '../atoms/Badge'
import Text from '../atoms/Text'
import Title from '../atoms/Title'
import { 
  Calendar, 
  Box, 
  Tag, 
  FileText, 
  RotateCcw 
} from 'lucide-react'

const EventDetailContent = ({ event, onClose, className = '', style = {} }) => {
  // 일자 정보 전처리 함수 
  const formattedDate = (date) => {
    if (!(date instanceof Date)) return ''
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: '2-digit'
    })
  }

  // 상태에 따른 Badge 색상 결정
  const getStatusColor = (status) => {
    switch(status) {
      case 'completed': return 'green'
      case 'in_progress': return 'red'
      case 'todo': return 'gray'
      case 'pending': return 'blue'
      default: return 'gray'
    }
  }

  // 카테고리에 따른 Badge 색상 결정
  const getCategoryColor = (category) => {
    // TODO: 실제 카테고리에 따른 색상 매핑 로직 필요
    return 'orange'
  }

  // 상태 텍스트 변환
  const getStatusText = (status) => {
    switch(status) {
      case 'completed': return '완료'
      case 'in_progress': return 'In Progress'
      case 'todo': return '예정'
      case 'pending': return '대기'
      default: return status
    }
  }

  const containerStyle = {
    width: '100%',
    ...style
  }

  const titleStyle = {
    fontSize: '28px',
    fontWeight: 700,
    color: '#1a1a1a',
    marginBottom: '40px',
    textAlign: 'left',
    lineHeight: 1.3,
    wordBreak: 'keep-all' // 한글 줄바꿈 최적화
  }

  const start = formattedDate(event.start)

  return (
    <div className={`event-detail-content ${className}`} style={containerStyle}>
      <Title level={1} style={titleStyle}>
        {event.title}
      </Title>
      
      <FieldItem 
        icon={<RotateCcw size={24} />}
        label="Status"
      >
        <Badge 
          content={getStatusText(event.extendedProps?.status)} 
          color={getStatusColor(event.extendedProps?.status)}
        />
      </FieldItem>

      <FieldItem 
        icon={<Calendar size={24} />}
        label="Date"
      >
        <Text style={{ fontSize: '16px', color: '#1a1a1a', fontWeight: 500 }}>
          {start}
        </Text>
      </FieldItem>

      <FieldItem 
        icon={<Box size={24} />}
        label="Project"
      >
        <Text style={{ 
          fontSize: '16px', 
          color: '#1a1a1a', 
          fontWeight: 500,
          textDecoration: 'underline'
        }}>
          {event.extendedProps?.projectName || event.extendedProps?.projectId}
        </Text>
      </FieldItem>

      <FieldItem 
        icon={<Tag size={24} />}
        label="Category"
      >
        <Badge 
          content={event.extendedProps?.category}
          color={getCategoryColor(event.extendedProps?.category)}
        />
      </FieldItem>

      <FieldItem 
        icon={<FileText size={24} />}
        label="Description"
      >
        <Text style={{ fontSize: '16px', color: '#1a1a1a', fontWeight: 500 }}>
          {event.extendedProps?.featureDescription}
        </Text>
      </FieldItem>
    </div>
  )
}

export default EventDetailContent