import { React, useState, useEffect, useRef } from "react"
import FullCalendar from "@fullcalendar/react"
import dayGridPlugin from "@fullcalendar/daygrid"
import useModalStore from "../../store/modalStore"
import { transformSchedulesToEvents, useProjectScheduleByMonth } from "../../hooks/useSchedules"
import useProjectStore from '@/stores/projectStore'
import styles from './ProjectCalendar.module.css'

const ProjectCalendar = () => {
  const { openModal } = useModalStore()
  const { currentProject } = useProjectStore()
  
  const currentDate = new Date()
  const [currentYear, setCurrentYear] = useState(currentDate.getFullYear())
  const [currentMonth, setCurrentMonth] = useState(currentDate.getMonth() + 1)
  const [tooltip, setTooltip] = useState({ show: false, content: '', x: 0, y: 0 })
  
  const calendarRef = useRef(null)

  const handleEventClick = (info) => {
    openModal('EVENT_DETAIL', { event: info.event })
  }

  // 달력에서 이전/다음 달 버튼 클릭 시 useUserScheduleByMonth 호출해서 받아온 데이터를 달력에 렌더링
  const handleDatesSet = (arg) => {
    const year = arg.view.currentStart.getFullYear()
    const month = arg.view.currentStart.getMonth() + 1
    setCurrentYear(year)
    setCurrentMonth(month)
  }

  // useProjectStore에서 관리 중인 currentProject id 가져오기
  const projectId = currentProject?.projectId

  const {
    data: schedules,
    isLoading: isScheduleLoading,
    error: scheduleError
  } = useProjectScheduleByMonth(projectId, currentYear.toString(), currentMonth.toString())

  // API 데이터를 FullCalendar events 형식으로 변환
  console.log(`원본 schedules 데이터: `, schedules)
  const events = transformSchedulesToEvents(schedules)
  console.log(`Fullcalendar events 형식으로 변환: `, events)
  console.log(`events 타입: `, typeof events, `isArray: `, Array.isArray(events))
  
  // 날짜별 이벤트 그룹핑 (dot 표시 및 툴팁용)
  const eventsByDate = {}
  if (events && Array.isArray(events)) {
    events.forEach(event => {
      if (event && event.start && event.title) {
        // start가 문자열인지 Date 객체인지 확인
        let dateKey
        if (typeof event.start === 'string') {
          dateKey = event.start.split('T')[0] // YYYY-MM-DD 형식으로 변환
        } else if (event.start instanceof Date) {
          dateKey = event.start.toISOString().split('T')[0]
        } else {
          return // 유효하지 않은 데이터는 건너뛰기
        }
        
        if (!eventsByDate[dateKey]) {
          eventsByDate[dateKey] = []
        }
        eventsByDate[dateKey].push(event.title)
      }
    })
  }
  
  console.log(`날짜별 이벤트 그룹핑: `, eventsByDate)

  // 달력 렌더링 후 커스텀 dot 추가 및 이벤트 핸들러 설정
  useEffect(() => {
    const addDotsAndEvents = () => {
      if (!calendarRef.current) return

      const calendarEl = calendarRef.current.elRef.current
      if (!calendarEl) return

      console.log('Adding dots for dates:', Object.keys(eventsByDate))

      // 기존 dot들과 이벤트 리스너 제거
      const existingDots = calendarEl.querySelectorAll('.event-dot')
      existingDots.forEach(dot => dot.remove())

      // 각 날짜 셀에 dot 추가
      Object.keys(eventsByDate).forEach(dateKey => {
        const dayEl = calendarEl.querySelector(`[data-date="${dateKey}"]`)
        console.log(`Looking for date ${dateKey}, found element:`, dayEl)
        
        if (dayEl) {
          // 날짜 번호 요소를 찾아서 그 부모에 dot 추가
          const dayNumberEl = dayEl.querySelector('.fc-daygrid-day-number')
          
          if (dayNumberEl) {
            // 날짜 번호 요소의 부모를 상대적 위치로 설정
            const parentEl = dayNumberEl.parentElement
            if (parentEl.style.position !== 'relative') {
              parentEl.style.position = 'relative'
            }

            const dot = document.createElement('div')
            dot.className = 'event-dot'
            dot.style.cssText = `
              width: 6px;
              height: 6px;
              background-color: #3498db;
              border-radius: 50%;
              position: absolute;
              top: calc(100% + 2px);
              left: 50%;
              transform: translateX(-50%);
              z-index: 2;
            `
            dayNumberEl.appendChild(dot)
            console.log(`Added dot to ${dateKey}`)
          }
        }
      })

      // 마우스 이벤트 핸들러 추가
      const handleMouseEnter = (e) => {
        const dayEl = e.currentTarget
        const dateAttr = dayEl.getAttribute('data-date')
        
        if (dateAttr && eventsByDate[dateAttr]) {
          const rect = dayEl.getBoundingClientRect()
          const containerRect = calendarEl.getBoundingClientRect()
          
          setTooltip({
            show: true,
            content: eventsByDate[dateAttr].join(', '),
            x: rect.left + rect.width / 2 - containerRect.left,
            y: rect.top - containerRect.top - 10
          })
        }
      }

      const handleMouseLeave = () => {
        setTooltip({ show: false, content: '', x: 0, y: 0 })
      }

      // 이벤트가 있는 날짜 셀에만 이벤트 리스너 추가
      Object.keys(eventsByDate).forEach(dateKey => {
        const dayEl = calendarEl.querySelector(`[data-date="${dateKey}"]`)
        if (dayEl) {
          dayEl.addEventListener('mouseenter', handleMouseEnter)
          dayEl.addEventListener('mouseleave', handleMouseLeave)
          dayEl.style.cursor = 'pointer'
        }
      })

      // 클린업 함수를 위한 참조 저장
      calendarEl._cleanup = () => {
        Object.keys(eventsByDate).forEach(dateKey => {
          const dayEl = calendarEl.querySelector(`[data-date="${dateKey}"]`)
          if (dayEl) {
            dayEl.removeEventListener('mouseenter', handleMouseEnter)
            dayEl.removeEventListener('mouseleave', handleMouseLeave)
          }
        })
      }
    }

    // 타이머를 사용해 달력이 완전히 렌더링된 후 실행
    const timer = setTimeout(addDotsAndEvents, 100)

    // 클린업 함수
    return () => {
      clearTimeout(timer)
      if (calendarRef.current?.elRef.current?._cleanup) {
        calendarRef.current.elRef.current._cleanup()
      }
    }
  }, [eventsByDate, currentYear, currentMonth])

  return (
    <div className={styles.calendarContainer}>
      <FullCalendar
        ref={calendarRef}
        initialView="dayGridMonth"
        plugins={[dayGridPlugin]}
        displayEventTime={false}
        events={[]} // 빈 배열로 설정하여 기본 이벤트 렌더링 방지
        eventClick={handleEventClick}
        datesSet={handleDatesSet}
        headerToolbar={{
          left: 'prev',
          center: 'title',
          right: 'next'
        }}
        height="auto"
      />
      
      {/* 커스텀 툴팁 */}
      {tooltip.show && (
        <div 
          className={styles.tooltip}
          style={{
            left: tooltip.x,
            top: tooltip.y,
            transform: 'translateX(-50%) translateY(-100%)'
          }}
        >
          {tooltip.content}
        </div>
      )}
    </div>
  )
}

export default ProjectCalendar