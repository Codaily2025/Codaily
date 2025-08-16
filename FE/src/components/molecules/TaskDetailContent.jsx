import React from 'react'
import FieldItem from './FieldItem'
import Badge from '../atoms/Badge'
import Text from '../atoms/Text'
import Title from '../atoms/Title'
import Button from '../atoms/Button'
import { 
  RotateCcw, 
  Calendar, 
  Package, 
  Tag, 
  FileText 
} from 'lucide-react'
import './TaskDetailContent.css'

const TaskDetailContent = ({ task }) => {

  console.log('')
  // 상태에 따른 Badge 색상 결정
  const getStatusColor = (status) => {
    switch(status) {
      case 'DONE': return 'green'
      case 'IN_PROGRESS': return 'red'
      case 'TODO': return 'red'
      default: return 'red'
    }
  }

  // 상태 텍스트 변환
  const getStatusText = (status) => {
    switch(status) {
      case 'DONE': return 'Completed'
      case 'IN_PROGRESS': return 'In Progress'
      default: return 'To Do'
    }
  }

  // 시간 포맷팅 함수
  const formatDateTime = (dateTimeString) => {
    if (!dateTimeString) {
      return '----년 --월 --일'
    }
    
    try {
      const date = new Date(dateTimeString)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}년 ${month}월 ${day}일`
    } catch (error) {
      return '----년 --월 --일'
    }
  }

  // 라벨 색상 결정
  const getLabelColor = () => {
    return 'orange'
  }

  const containerStyle = {
    width: '100%',
    padding: '0'
  }

  const titleContainerStyle = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: '32px',
    paddingBottom: '16px',
    borderBottom: '0.5px solid #938F99'
  }

  const titleStyle = {
    fontSize: '24px',
    fontWeight: 600,
    color: '#000000',
    lineHeight: '29px',
    margin: 0
  }

  const priorityBadgeStyle = {
    fontSize: '12px',
    fontWeight: 600,
    width: '32px',
    height: '32px',
    borderRadius: '50%',
    backgroundColor: '#F5EFEE',
    color: '#D55078',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center'
  }


  return (
    <div className="task-detail-content" style={containerStyle}>
      <div style={titleContainerStyle}>
        <Title level={1} style={titleStyle}>
          {task?.title || 'Task Title'}
        </Title>
        <div style={priorityBadgeStyle}>
          {task?.priority || '5'}
        </div>
      </div>
      
      <div className="task-fields">
        <FieldItem 
          icon={<RotateCcw size={20} color="#A6A6A6" />}
          label="Status"
        >
          <Badge 
            content={getStatusText(task?.status)} 
            color={getStatusColor(task?.status)}
          />
        </FieldItem>

        <FieldItem 
          icon={<Calendar size={20} color="#A6A6A6" />}
          label="Last worked at"
        >
          <Text style={{ fontSize: '12px', color: '#000000', fontWeight: 500 }}>
            {formatDateTime(task?.updatedAt)}
          </Text>
        </FieldItem>

        <FieldItem 
          icon={<Calendar size={20} color="#A6A6A6" />}
          label="Completed at"
        >
          <Text style={{ fontSize: '12px', color: '#000000', fontWeight: 500 }}>
            {formatDateTime(task?.completedAt)}
          </Text>
        </FieldItem>

        <FieldItem 
          icon={<Package size={20} color="#A6A6A6" />}
          label="Remained/Estimated"
        >
          <Text style={{ fontSize: '12px', color: '#000000', fontWeight: 500 }}>
            {/* {task?.remainedEstimated || '5 / 5 (시간)'} */}
            {task?.remainingTime}  /  {task?.estimatedTime}  (시간)
          </Text>
        </FieldItem>

        <FieldItem 
          icon={<Tag size={20} color="#A6A6A6" />}
          label="Label"
        >
          <div style={{ display: 'flex', gap: '6px' }}>
            {task?.labels?.map((label, index) => (
              <Badge 
                key={index}
                content={label}
                color={getLabelColor()}
              />
            )) || (
              <>
                <Badge content="Authentication" color="orange" />
                <Badge content="dont know" color="orange" />
              </>
            )}
          </div>
        </FieldItem>

        <FieldItem 
          icon={<FileText size={20} color="#A6A6A6" />}
          label="Description"
        >
          <Text style={{ fontSize: '12px', color: '#000000', fontWeight: 500 }}>
            {task?.description || '토큰 생성, 파싱, 검증 메서드 구현'}
          </Text>
        </FieldItem>
      </div>

    </div>
  )
}

export default TaskDetailContent