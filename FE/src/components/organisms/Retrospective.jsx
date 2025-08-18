import { useState, useEffect, useRef, useCallback, memo } from 'react'
import { useLocation } from 'react-router-dom'
import { BarChart3, CheckCircle, ClipboardList } from 'lucide-react'
import { useAllRetrospectives, useProjectRetrospectives, useCreateRetrospectiveMutation } from '@/hooks/useRetrospectives'
import useProjectStore from '@/stores/projectStore'
import useModalStore from '@/store/modalStore'
import styles from './Retrospective.module.css'
import caretUp from '../../assets/caret_up.svg'

const Retrospective = memo(() => {
  const location = useLocation()
  const [selectedProjectId, setSelectedProjectId] = useState(null)
  const [expandedCards, setExpandedCards] = useState(new Set())
  const [isDropdownOpen, setIsDropdownOpen] = useState(false)
  const [currentYearMonth, setCurrentYearMonth] = useState('')
  
  const { activeProjects } = useProjectStore()
  const { openModal } = useModalStore()
  const loadMoreRef = useRef()
  
  // 회고 수동 생성 mutation
  const createRetrospectiveMutation = useCreateRetrospectiveMutation()

  // URL 쿼리 파라미터에서 projectId 읽어서 초기 선택 상태 설정
  useEffect(() => {
    const urlParams = new URLSearchParams(location.search)
    const projectIdFromUrl = urlParams.get('projectId')
    
    if (projectIdFromUrl) {
      // 숫자로 변환 (api에서 숫자 형태로 오는 경우 대비)
      const projectId = parseInt(projectIdFromUrl, 10)
      setSelectedProjectId(projectId)
    }
  }, [location.search])

  // 선택된 프로젝트에 따라 적절한 훅 사용
  // !!true -> false 설정 시 실제 api 호출!!
  const allRetrospectivesQuery = useAllRetrospectives(true)
  const projectRetrospectivesQuery = useProjectRetrospectives(selectedProjectId, true)
  
  const query = selectedProjectId ? projectRetrospectivesQuery : allRetrospectivesQuery

  const {
    data,
    error,
    fetchNextPage,
    hasNextPage,
    isFetching,
    isFetchingNextPage,
    isLoading,
    isError
  } = query

  // 모든 페이지의 아이템을 평탄화
  const allItems = data?.pages?.flatMap(page => page.items) || []

  // 년/월별로 그룹화
  const groupedByMonth = useCallback(() => {
    const grouped = {}
    allItems.forEach(item => {
      const date = new Date(item.date)
      const yearMonth = `${date.getFullYear()}년 ${date.getMonth() + 1}월`
      if (!grouped[yearMonth]) {
        grouped[yearMonth] = []
      }
      grouped[yearMonth].push(item)
    })
    return grouped
  }, [allItems])

  const monthlyData = groupedByMonth()

  // Intersection Observer를 사용한 무한 스크롤
  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        const target = entries[0]
        if (target.isIntersecting && hasNextPage && !isFetchingNextPage) {
          fetchNextPage()
        }
      },
      { threshold: 0.1 }
    )

    if (loadMoreRef.current) {
      observer.observe(loadMoreRef.current)
    }

    return () => {
      if (loadMoreRef.current) {
        observer.unobserve(loadMoreRef.current)
      }
    }
  }, [fetchNextPage, hasNextPage, isFetchingNextPage])

  // 스크롤 위치에 따른 현재 년월 업데이트
  useEffect(() => {
    const handleScroll = () => {
      const timelineItems = document.querySelectorAll('[data-year-month]')
      const scrollTop = window.scrollY + 100

      let current = ''
      timelineItems.forEach(item => {
        const rect = item.getBoundingClientRect()
        const elementTop = rect.top + window.scrollY
        
        if (elementTop <= scrollTop) {
          current = item.getAttribute('data-year-month')
        }
      })
      
      if (current && current !== currentYearMonth) {
        setCurrentYearMonth(current)
      }
    }

    window.addEventListener('scroll', handleScroll)
    return () => window.removeEventListener('scroll', handleScroll)
  }, [currentYearMonth])

  const handleCardClick = (itemDate) => {
    setExpandedCards(prev => {
      const newSet = new Set(prev)
      if (newSet.has(itemDate)) {
        newSet.delete(itemDate)
      } else {
        newSet.add(itemDate)
      }
      return newSet
    })
  }

  const handleProjectSelect = (projectId) => {
    setSelectedProjectId(projectId)
    setIsDropdownOpen(false)
    setExpandedCards(new Set()) // 프로젝트 변경 시 확장된 카드 초기화
  }

  // 회고 수동 생성 버튼 클릭 핸들러
  const handleCreateRetrospective = () => {
    if (!selectedProjectId) return
    
    createRetrospectiveMutation.mutate(selectedProjectId, {
      onSuccess: () => {
        openModal('RETROSPECTIVE_RESULT', {
          success: true,
          message: '회고가 성공적으로 생성되었습니다.'
        })
      },
      onError: (error) => {
        openModal('RETROSPECTIVE_RESULT', {
          success: false,
          message: error.message || '회고 생성 중 오류가 발생했습니다.'
        })
      }
    })
  }

  const getSelectedProjectName = () => {
    if (!selectedProjectId) return '모든 프로젝트'
    const project = activeProjects.find(p => p.id === selectedProjectId || p.projectId === selectedProjectId)
    return project?.title || '프로젝트'
  }

  const formatDate = (dateString) => {
    const date = new Date(dateString)
    return `${date.getMonth() + 1}월 ${date.getDate()}일`
  }

  const renderRetrospectiveCard = useCallback((item) => {
    const isExpanded = expandedCards.has(item.date)
    const projectTitle = activeProjects.find(p => 
      p.id === item.projectId || p.projectId === item.projectId
    )?.title || `프로젝트 ${item.projectId}`

    return (
      <div
        key={`${item.date}-${item.projectId}`}
        className={`${styles.retrospectiveCard} ${isExpanded ? styles.expanded : ''}`}
        onClick={() => handleCardClick(item.date)}
      >
        <div className={styles.cardHeader}>
          <div className={styles.cardLeft}>
            <div className={styles.cardTitleSection}>
              <div className={styles.cardTitle}>
                {item.date} 일일 회고
              </div>
              <span className={styles.projectBadge}>
                {projectTitle}
              </span>
            </div>
            
            <div className={styles.metrics}>
              <div className={styles.metric}>
                <div className={styles.metricValue}>
                  {item.productivityMetrics.codeQuality}
                </div>
                <div className={styles.metricLabel}>Code Quality</div>
              </div>
              <div className={styles.metric}>
                <div className={styles.metricValue}>
                  {item.productivityMetrics.productivityScore}
                </div>
                <div className={styles.metricLabel}>Productivity</div>
              </div>
              <div className={styles.metric}>
                <div className={styles.metricValue}>
                  {item.productivityMetrics.completedFeatures}
                </div>
                <div className={styles.metricLabel}>Features</div>
              </div>
              <div className={styles.metric}>
                <div className={styles.metricValue}>
                  {item.productivityMetrics.totalCommits}
                </div>
                <div className={styles.metricLabel}>Commits</div>
              </div>
            </div>
          </div>
          
          <div className={styles.cardRight}>
            <span className={`${styles.triggerBadge} ${styles[item.triggerType.toLowerCase()]}`}>
              {item.triggerType}
            </span>
          </div>
        </div>

        {isExpanded && (
          <div className={styles.expandedContent}>
            {/* 요약 섹션 */}
            <div className={styles.summarySection}>
              <div className={styles.summaryTitle}>
                <BarChart3 size={16} />
                Overall
              </div>
              <div className={styles.summaryItem}>
                <div className={styles.summaryLabel}>Overall</div>
                <div className={styles.summaryText}>{item.summary.overall}</div>
              </div>
              <div className={styles.summaryItem}>
                <div className={styles.summaryLabel}>Strengths</div>
                <div className={`${styles.summaryText} ${styles.strengths}`}>
                  {item.summary.strengths}
                </div>
              </div>
              <div className={styles.summaryItem}>
                <div className={styles.summaryLabel}>Improvements</div>
                <div className={`${styles.summaryText} ${styles.improvements}`}>
                  {item.summary.improvements}
                </div>
              </div>
              <div className={styles.summaryItem}>
                <div className={styles.summaryLabel}>Risks</div>
                <div className={`${styles.summaryText} ${styles.risks}`}>
                  {item.summary.risks}
                </div>
              </div>
            </div>

            {/* 완료된 기능 섹션 */}
            {item.completedFeatures && item.completedFeatures.length > 0 && (
              <div className={styles.featuresSection}>
                <div className={styles.summaryTitle}>
                  <CheckCircle size={16} />
                  완료된 기능
                </div>
                {item.completedFeatures.map(feature => (
                  <div key={feature.featureId} className={styles.featureItem}>
                    <div className={styles.featureHeader}>
                      <div className={styles.featureTitle}>{feature.title}</div>
                      <div className={styles.featureField}>{feature.field}</div>
                    </div>
                    <div className={styles.checklistProgress}>
                      체크리스트: {feature.checklistDoneCount}/{feature.checklistCount}
                    </div>
                    <div className={styles.qualityScore}>
                      코드 품질: {feature.codeQualityScore}
                    </div>
                    <div className={styles.summaryText}>{feature.summary}</div>
                  </div>
                ))}
              </div>
            )}

            {/* 액션 아이템 */}
            {item.actionItems && item.actionItems.length > 0 && (
              <div className={styles.actionItems}>
                <div className={styles.summaryTitle}>
                  <ClipboardList size={16} />
                  Action Items
                </div>
                <ul className={styles.actionItemsList}>
                  {item.actionItems.map((action, index) => (
                    <li key={index} className={styles.actionItem}>
                      {action}
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        )}
      </div>
    )
  }, [expandedCards, activeProjects])

  if (isLoading) {
    return (
      <div className={styles.retrospectiveContainer}>
        <div className={styles.loadingContainer}>
          <div className={styles.loadingText}>회고 데이터를 불러오는 중...</div>
        </div>
      </div>
    )
  }

  if (isError) {
    return (
      <div className={styles.retrospectiveContainer}>
        <div className={styles.errorContainer}>
          <div className={styles.errorText}>
            {error?.message || '회고 데이터를 불러오는데 실패했습니다.'}
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className={styles.retrospectiveContainer}>
      <div className={styles.header}>
        
        <div className={styles.projectSelector}>
          <div 
            className={`${styles.dropdown} ${isDropdownOpen ? styles.open : ''}`}
            onClick={() => setIsDropdownOpen(!isDropdownOpen)}
          >
            <span>{getSelectedProjectName()}</span>
            <img className={styles.dropdownArrow} src={caretUp} alt="caret" />
          </div>
          
          {/* 회고 수동 생성 버튼 - 특정 프로젝트 선택 시에만 표시 */}
          {/* TODO: 버튼 위치 조정하기 */}
          {selectedProjectId && (
            <button 
              className={styles.createRetrospectiveBtn}
              onClick={handleCreateRetrospective}
              disabled={createRetrospectiveMutation.isPending}
            >
              {createRetrospectiveMutation.isPending ? '생성 중...' : '회고 수동 생성'}
            </button>
          )}
          
          {isDropdownOpen && (
            <div className={styles.dropdownMenu}>
              <div 
                className={`${styles.dropdownItem} ${!selectedProjectId ? styles.selected : ''}`}
                onClick={() => handleProjectSelect(null)}
              >
                <div className={styles.dropdownItemIcon}>
                  {!selectedProjectId && (
                    <svg
                      width="16"
                      height="16"
                      viewBox="0 0 16 16"
                      fill="none"
                      xmlns="http://www.w3.org/2000/svg"
                    >
                      <path
                        d="M13.3346 4L6.0013 11.3333L2.66797 8"
                        stroke="#5A597D"
                        strokeWidth="1.33333"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                      />
                    </svg>
                  )}
                </div>
                <span className={styles.dropdownItemLabel}>모든 프로젝트</span>
              </div>
              {activeProjects.map(project => (
                <div
                  key={project.id || project.projectId}
                  className={`${styles.dropdownItem} ${
                    selectedProjectId === (project.id || project.projectId) ? styles.selected : ''
                  }`}
                  onClick={() => handleProjectSelect(project.id || project.projectId)}
                >
                  <div className={styles.dropdownItemIcon}>
                    {selectedProjectId === (project.id || project.projectId) && (
                      <svg
                        width="16"
                        height="16"
                        viewBox="0 0 16 16"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path
                          d="M13.3346 4L6.0013 11.3333L2.66797 8"
                          stroke="#5A597D"
                          strokeWidth="1.33333"
                          strokeLinecap="round"
                          strokeLinejoin="round"
                        />
                      </svg>
                    )}
                  </div>
                  <span className={styles.dropdownItemLabel}>{project.title}</span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {allItems.length === 0 ? (
        <div className={styles.emptyContainer}>
          <div className={styles.emptyText}>회고 데이터가 없습니다</div>
          <div className={styles.emptySubtext}>
            {selectedProjectId ? '선택한 프로젝트에 대한' : '전체'} 회고 데이터가 아직 없습니다.
          </div>
        </div>
      ) : (
        <div className={styles.timeline}>
          {Object.entries(monthlyData).map(([yearMonth, items]) => (
            <div 
              key={yearMonth} 
              className={styles.timelineItem}
              data-year-month={yearMonth}
            >
              <div className={styles.timelineDate}>
                <div className={styles.monthIndicator}>{yearMonth}</div>
              </div>
              
              <div className={styles.timelineContent}>
                {items.map(item => 
                  renderRetrospectiveCard(item)
                )}
              </div>
            </div>
          ))}
          
          {/* 무한 스크롤 트리거 */}
          {hasNextPage && (
            <div ref={loadMoreRef} className={styles.loadMoreTrigger}>
              {isFetchingNextPage && (
                <div className={styles.loadingContainer}>
                  <div className={styles.loadingText}>더 많은 데이터를 불러오는 중...</div>
                </div>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  )
})

export default Retrospective