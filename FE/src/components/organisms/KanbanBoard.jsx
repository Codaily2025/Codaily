import React, { useMemo } from 'react'
import KanbanCard from '@/components/molecules/KanbanCard'
import useModalStore from '@/store/modalStore'
import useProjectStore from '@/stores/projectStore'
import { useKanbanTabFields } from '@/hooks/useProjects'

const KanbanBoard = () => {
    const { openModal } = useModalStore()
    const { currentProject } = useProjectStore()
    
    // 전역 상태에서 현재 프로젝트의 칸반 탭 필드 가져오기
    const { 
        data: kanbanTabFields, 
        isLoading: isKanbanTabsLoading, 
        error: kanbanTabsError 
    } = useKanbanTabFields(currentProject?.id)
    
    console.log('KanbanBoard - currentProject:', currentProject)
    console.log('KanbanBoard - kanbanTabFields:', kanbanTabFields)
    
    const handleTaskClick = (cardData) => {
      console.log(`Task Clicked!`)
      openModal('TASK_DETAIL', { event: cardData })
    }

    // 프로젝트 데이터에서 칸반 데이터 추출
    const kanbanData = useMemo(() => {
      if (!currentProject?.features) {
        return { todo: [], inProgress: [], completed: [] }
      }

      const todo = []
      const inProgress = []
      const completed = []

      currentProject.features.forEach(feature => {
        if (feature.tasks) {
          feature.tasks.forEach(task => {
            const taskData = {
              id: task.id,
              category: task.category,
              title: task.title,
              details: task.details,
              dueDate: task.dueDate
            }

            switch (task.status) {
              case 'todo':
                todo.push(taskData)
                break
              case 'in_progress':
                inProgress.push(taskData)
                break
              case 'completed':
                completed.push(taskData)
                break
              default:
                todo.push(taskData)
            }
          })
        }
      })

      return { todo, inProgress, completed }
    }, [currentProject])

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