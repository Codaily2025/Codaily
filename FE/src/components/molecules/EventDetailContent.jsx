const EventDetailContent = ({ event }) => {
  const start = event.start?.toLocaleTimeString("ko-KR", {
    hour: "2-digit", minute: "2-digit",
  })
  const end = event.end?.toLocaleTimeString("ko-KR", {
    hour: "2-digit", minute: "2-digit",
  })

  return (
    <>
      <h2 style={{ color: '#404040', marginBottom: '12px' }}>{event.title}</h2>
      <p>프로젝트: {event.extendedProps?.project}</p>
      <p>시간: {start} ~ {end}</p>
    </>
  )
}

export default EventDetailContent