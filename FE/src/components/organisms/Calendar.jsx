import { React, useState } from "react"
import FullCalendar from "@fullcalendar/react"
import dayGridPlugin from "@fullcalendar/daygrid"
import useModalStore from "../../store/modalStore"
import { useUserScheduleByMonth, transformSchedulesToEvents } from "../../hooks/useSchedules"

const Calendar = () => {
  const { openModal } = useModalStore()
  
  const currentDate = new Date()
  const [currentYear, setCurrentYear] = useState(currentDate.getFullYear())
  const [currentMonth, setCurrentMonth] = useState(currentDate.getMonth() + 1)

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

  const {
    data: schedules,
    isLoading: isScheduleLoading,
    error: scheduleError
  } = useUserScheduleByMonth(currentYear.toString(), currentMonth.toString())

  // API 데이터를 FullCalendar events 형식으로 변환
  const events = transformSchedulesToEvents(schedules)
  // console.log(events)


  return (
      <FullCalendar
        initialView="dayGridMonth"
        plugins={[dayGridPlugin]}
        displayEventTime={false}
        events={events}
        eventClick={handleEventClick}
        datesSet={handleDatesSet}
      />
  )
}

export default Calendar