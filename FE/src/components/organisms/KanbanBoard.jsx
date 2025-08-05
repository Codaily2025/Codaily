import React from 'react'
import KanbanCard from '@/components/molecules/KanbanCard'
import useModalStore from '@/store/modalStore'

const KanbanBoard = () => {
    const { openModal } = useModalStore()
    
    const handleTaskClick = (cardData) => {
      console.log(`Task Clicked!`)
      openModal('TASK_DETAIL', { event: cardData })
    }

    // 확인용 하드코딩 데이터
    const kanbanData = {
    todo: [
      {
        id: 1,
        category: '일반 로그인 구현',
        title: 'JwtTokenProvider 클래스 구현',
        details: '토큰 생성, 파싱, 검증 메서드 구현',
        dueDate: '2025/07/30 17:19'
      },
      {
        id: 2,
        category: '일반 로그인 구현',
        title: 'JwtTokenProvider 클래스 구현',
        details: '토큰 생성, 파싱, 검증 메서드 구현',
        dueDate: '2025/07/30 17:19'
      }
    ],
    inProgress: [
      {
        id: 3,
        category: '일반 로그인 구현',
        title: 'JwtTokenProvider 클래스 구현',
        details: '토큰 생성, 파싱, 검증 메서드 구현',
        dueDate: '2025/07/30 17:19'
      }
    ],
    completed: [
      {
        id: 4,
        category: '일반 로그인 구현',
        title: 'JwtTokenProvider 클래스 구현',
        details: '토큰 생성, 파싱, 검증 메서드 구현',
        dueDate: '2025/07/30 17:19'
      },
      {
        id: 5,
        category: '일반 로그인 구현',
        title: 'JwtTokenProvider 클래스 구현',
        details: '토큰 생성, 파싱, 검증 메서드 구현',
        dueDate: '2025/07/30 17:19'
      },
      {
        id: 6,
        category: '일반 로그인 구현',
        title: 'JwtTokenProvider 클래스 구현',
        details: '토큰 생성, 파싱, 검증 메서드 구현',
        dueDate: '2025/07/30 17:19'
      }
    ]
  }

  // 칸반 보드 내 칼럼
  const columns = [
    { id: 'todo', title: 'To do', color: '#8B7EC8', cards: kanbanData.todo },
    { id: 'inProgress', title: 'In Progress', color: '#CCCBE4', cards: kanbanData.inProgress },
    { id: 'completed', title: 'Completed', color: '#8483AB', cards: kanbanData.completed }
  ]

  // 칸반 칼럼 타이틀 내 '+' 버튼 클릭
  const handleAddCard = (columnId) => {
    console.log(`Add kanban card to ${columnId}`);
  }

  return (
    <div className="kanban-board">
      {columns.map((column) => (
        <div key={column.id} className="kanban-column">
          <div className="kanban-column-header" style={{ backgroundColor: column.color }}>
            <div className="kanban-column-info">
              <span className="kanban-column-count">{column.cards.length}</span>
              <span className="kanban-column-title">{column.title}</span>
            </div>
            <button 
              className="kanban-add-btn" 
              onClick={() => handleAddCard(column.id)}
            >
              +
            </button>
          </div>
          <div className="kanban-column-content">
            {column.cards.map((card) => (
              <KanbanCard
                key={card.id}
                category={card.category}
                title={card.title}
                details={card.details}
                dueDate={card.dueDate}
                onClick={() => handleTaskClick(card)}
              />
            ))}
          </div>
        </div>
      ))}
    </div>
  )

}

export default KanbanBoard