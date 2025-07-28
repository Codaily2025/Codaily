import React, { useState } from "react"
import FullCalendar from "@fullcalendar/react"
import dayGridPlugin from "@fullcalendar/daygrid"
import EventDetailModal from "../organisms/EventDetailModal"

const Calendar = () => {
  const [selectedEvent, setSelectedEvent] = useState(null)

  const handleEventClick = (info) => {
    setSelectedEvent(info.event)
  }

  const handleClose = () => {
    setSelectedEvent(null)
  }

  // 확인용 하드코딩 데이터
  const myEventArray = [
    {
      title: "task 1",
      start: "2025-07-29T14:00:00",
      end: "2025-07-30T16:00:00",
      extendedProps: { project: "project 1" },
      backgroundColor: "#b4b3dc",
      borderColor: "#b4b3dc",
    },
    {
      title: "task 2",
      start: "2025-07-30T18:00:00",
      end: "2025-07-30T20:00:00",
      allDay: true,
      extendedProps: { project: "project 2" },
      backgroundColor: "#cd7b9c",
      borderColor: "#cd7b9c",
    },
  ]

  return (
    <div className="">
      <FullCalendar
        defaultView="dayGridMonth"
        plugins={[dayGridPlugin]}
        displayEventTime={false}
        events={myEventArray}
        eventClick={handleEventClick}
      />
      <EventDetailModal
        event={selectedEvent}
        onClose={handleClose}
      />
    </div>
  )
}

export default Calendar