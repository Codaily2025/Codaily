import React, { useMemo, useState, useCallback, memo } from 'react'
import { DndProvider, useDrop } from 'react-dnd'
import { HTML5Backend } from 'react-dnd-html5-backend'
import KanbanCard from '@/components/molecules/KanbanCard'
import KanbanTabs from '@/components/molecules/KanbanTabs'
import useModalStore from '@/store/modalStore'
import useProjectStore from '@/stores/projectStore'
import { useKanbanTabFields, useFeaturesByField, useUpdateFeatureItemStatus, useParentFeatures } from '@/hooks/useProjects'
import styles from './KanbanBoard.module.css'

const KanbanBoard = memo(() => {
  const { openModal } = useModalStore()
  const { currentProject, setParentFeatures } = useProjectStore()
  const updateStatusMutation = useUpdateFeatureItemStatus()

  // currentProject에서 projectId 추출
  // TODO: id/projectId 확인해서 수정하기
  const projectId = currentProject?.projectId || currentProject?.id

  // 전역 상태에서 현재 프로젝트의 칸반 탭 필드 가져오기
  const {
    data: kanbanTabFields,
    isLoading: isKanbanTabsLoading,
    error: kanbanTabsError
  } = useKanbanTabFields(projectId)
  
  // 탭 상태 관리
  const [activeTab, setActiveTab] = useState('')

  const {
    data: featuresByField,
    isLoading: isFeaturesByFieldLoading,
    error: featuresByFieldError
  } = useFeaturesByField(projectId, activeTab)

  // currentProject의 최상단(부모) 기능 리스트
  const {
    data: parentFeaturesList,
    isLoading: isParentFeatureLoading,
    error: parentFeaturesError
  } = useParentFeatures(projectId)

  // 첫 번째 탭을 기본으로 설정
  React.useEffect(() => {
    if (kanbanTabFields && kanbanTabFields.length > 0 && !activeTab) {
      setActiveTab(kanbanTabFields[0])
    }
  }, [kanbanTabFields, activeTab])

  // parentFeaturesList를 스토어에 저장
  React.useEffect(() => {
    if (parentFeaturesList) {
      setParentFeatures(parentFeaturesList.parentFeatures)
    }
  }, [parentFeaturesList, setParentFeatures])

  // 콘솔 디버깅 (필요시 주석 해제)
  // console.log('KanbanBoard - projectId (extracted):', projectId)
  // console.log('KanbanBoard - activeTab:', activeTab)
  // console.log('KanbanBoard - kanbanTabFields:', kanbanTabFields)
  // console.log(`필드 ${activeTab} 하위 기능들: `, featuresByField)

  const handleTaskClick = useCallback((cardData) => {
    console.log(`Task Clicked!: `, cardData)
    openModal('TASK_DETAIL', { event: cardData })
  }, [openModal])

  // 필드별 기능 데이터에서 칸반 데이터 추출
  const kanbanData = useMemo(() => {
    if (!featuresByField) {
      return { todo: [], inProgress: [], completed: [] }
    }

    const todo = featuresByField.TODO || []
    const inProgress = featuresByField.IN_PROGRESS || []
    const completed = featuresByField.DONE || []

    return { todo, inProgress, completed }
  }, [featuresByField])

  // 칸반 보드 내 칼럼
  const columns = useMemo(() => [
    { id: 'todo', title: 'To do', color: '#8B7EC8', cards: kanbanData.todo },
    { id: 'inProgress', title: 'In Progress', color: '#CCCBE4', cards: kanbanData.inProgress },
    { id: 'completed', title: 'Completed', color: '#8483AB', cards: kanbanData.completed }
  ], [kanbanData])

  // 기능 추가 버튼 클릭 핸들러
  const handleAddFeature = useCallback(() => {
    openModal('ADD_FEATURE', {
      initialData: null
    })
  }, [openModal])

  // 탭 변경 핸들러
  const handleTabChange = useCallback((tab) => {
    setActiveTab(tab)
    console.log('Active tab changed to:', tab)
  }, [])

  // 유효한 드롭 여부 확인
  const isValidDrop = useCallback((fromStatus, toStatus) => {
    const validTransitions = {
      'TODO': ['IN_PROGRESS', 'DONE'],
      'IN_PROGRESS': ['DONE']
    }
    return validTransitions[fromStatus]?.includes(toStatus) || false
  }, [])

  // 드롭 핸들러
  const handleDrop = useCallback((item, targetStatus) => {
    const { featureId, status: fromStatus, title } = item
    
    if (fromStatus === targetStatus || !isValidDrop(fromStatus, targetStatus)) {
      return
    }

    const confirmHandler = () => {
      updateStatusMutation.mutate({
        projectId,
        featureId,
        newStatus: targetStatus,
        currentField: activeTab
      })
    }
    
    // 확인 모달 열기
    openModal('STATUS_CONFIRM', {
      fromStatus,
      toStatus: targetStatus,
      featureTitle: title,
      onConfirm: confirmHandler
    })
  }, [isValidDrop, projectId, activeTab, updateStatusMutation, openModal])

  // 드롭 존 컴포넌트
  const DropZone = useCallback(({ children, status, onDrop }) => {
    const [{ isOver, canDrop }, drop] = useDrop({
      accept: 'KANBAN_CARD',
      canDrop: (item) => isValidDrop(item.status, status),
      drop: (item) => onDrop(item, status),
      collect: (monitor) => ({
        isOver: monitor.isOver(),
        canDrop: monitor.canDrop(),
      }),
    })

    const dropZoneClass = `${styles.kanbanColumnContent} ${
      isOver && canDrop ? styles.dropZoneActive : ''
    } ${canDrop ? styles.dropZoneValid : ''}`

    return (
      <div ref={drop} className={dropZoneClass}>
        {children}
      </div>
    )
  }, [isValidDrop])

  return (
    <DndProvider backend={HTML5Backend}>
      <div className={styles.kanbanBoard}>
        {/* 칸반 탭 */}
        {kanbanTabFields && kanbanTabFields.length > 0 && (
          <KanbanTabs
            tabs={kanbanTabFields}
            activeTab={activeTab}
            onTabChange={handleTabChange}
          />
        )}

        {/* 칸반 컬럼들 */}
        <div className={styles.kanbanColumns}>
          {columns.map((column) => (
            <div key={column.id} className={styles.kanbanColumn}>
              <div className={styles.kanbanColumnHeader} style={{ backgroundColor: column.color }}>
                <div className={styles.kanbanColumnInfo}>
                  <span className={styles.kanbanColumnCount}>{column.cards.length}</span>
                  <span className={styles.kanbanColumnTitle}>{column.title}</span>
                </div>
              </div>
              <DropZone 
                status={column.id === 'todo' ? 'TODO' : column.id === 'inProgress' ? 'IN_PROGRESS' : 'DONE'}
                onDrop={handleDrop}
              >
                {column.cards.length > 0 ? (
                  column.cards.map((card) => (
                    <KanbanCard
                      key={card.featureId}
                      featureId={card.featureId}
                      category={card.category}
                      title={card.title}
                      description={card.description}
                      field={card.field}
                      estimatedTime={card.estimatedTime}
                      priorityLevel={card.priorityLevel}
                      status={card.status}
                      cardClassName={styles.kanbanCard}
                      infoClassName={styles.kanbanCardInfo}
                      categoryClassName={styles.kanbanCardCategory}
                      titleClassName={styles.kanbanCardTitle}
                      detailsClassName={styles.kanbanCardDetails}
                      footerClassName={styles.kanbanCardFooter}
                      fieldClassName={styles.kanbanCardField}
                      timeClassName={styles.kanbanCardTime}
                      priorityClassName={styles.kanbanCardPriority}
                      onClick={() => handleTaskClick(card)}
                    />
                  ))
                ) : (
                  <div className={styles.kanbanEmptyMessage}>
                    {column.id === 'todo' && '해야할 작업이 없습니다'}
                    {column.id === 'inProgress' && '진행 중인 작업이 없습니다'}
                    {column.id === 'completed' && '완료한 작업이 없습니다'}
                  </div>
                )}
              </DropZone>
            </div>
          ))}
        </div>
        
        {/* 플로팅 기능 추가 버튼 */}
        <button 
          className={styles.floatingAddBtn}
          onClick={handleAddFeature}
          title="새 기능 추가"
        >
          +
        </button>
      </div>
    </DndProvider>
  )

})

export default KanbanBoard