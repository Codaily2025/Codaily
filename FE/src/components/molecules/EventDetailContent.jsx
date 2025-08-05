const EventDetailContent = ({ event }) => {
  // 일자 정보 전처리 함수 
  // yyyy년 MM월 dd일 형식
  const formattedDate = (date) => {
    if (!(date instanceof Date)) return ''

    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')

    return `${year}년 ${month}월 ${day}일`
  }

  const start = formattedDate(event.start)
  
  // const end = event.end?.toLocaleTimeString("ko-KR", {
  //   hour: "2-digit", minute: "2-digit",
  // })
  

  return (
    <>
      <h2 style={{ color: '#404040', marginBottom: '12px' }}>{event.title}</h2>
      <p>일    자: {start}</p>
      <p>프로젝트: {event.extendedProps?.projectId}</p>
      <p>카테고리: {event.extendedProps?.category}</p>
      <p>기능설명: {event.extendedProps?.featureDescription}</p>
      <p>우선순위: {event.extendedProps?.priorityLevel}</p>
      <p>상    태: {event.extendedProps?.status}</p>
      <p>할당시간: {event.extendedProps?.allocatedHours} 시간</p>
    </>
  )
}

export default EventDetailContent