import { useState, useRef, useEffect } from 'react'
import { Calendar as CalendarIcon } from 'lucide-react'
import Title from '@/components/atoms/Title'
import Button from '@/components/atoms/Button'
import styles from './ProjectScheduleSetter.module.css'
// import { saveProjectSchedule } from '@/apis/projectScheduleApi'
import { useSaveProjectSchedule } from '@/hooks/useProjectScheduleMutation'

const ProjectScheduleSetter = () => {
    
    const [formData, setFormData] = useState({
        startDate: '',
        endDate: '',
        availableDates: [],
        timeByDay: {
            MONDAY: 0, TUESDAY: 0, WEDNESDAY: 0, THURSDAY: 0, FRIDAY: 0, SATURDAY: 0, SUNDAY: 0
        }
    })

    // 날짜 선택 관련 상태
    const [showStartCalendar, setShowStartCalendar] = useState(false)
    const [showEndCalendar, setShowEndCalendar] = useState(false)

    // 작업 가능 일자 상태
    const [availableWorkDays, setAvailableWorkDays] = useState(new Set())

    // 요일별 투자 시간 상태
    const [activeDay, setActiveDay] = useState('월') // 현재 선택된 요일
    const [step, setStep] = useState(0) // 0.5시간 단위 step (0~20, 총 10시간)
    const maxStep = 20

    const days = ['월', '화', '수', '목', '금', '토', '일']

    // 
    const saveProjectSchedule = useSaveProjectSchedule()

    // startDate ~ endDate 사이 날짜 계산
    const getDatesBetween = (startDate, endDate) => {
        if (!startDate || !endDate) return []
        
        const start = new Date(startDate.replace(/\//g, '-'))
        const end = new Date(endDate.replace(/\//g, '-'))
        const dates = []
        
        const currentDate = new Date(start)
        while (currentDate <= end) {
            dates.push(new Date(currentDate))
            currentDate.setDate(currentDate.getDate() + 1)
        }
        
        return dates
    }

    // 작업 가능 일자 자동 업데이트 (기간 설정 시)
    useEffect(() => {
        if (formData.startDate && formData.endDate) {
            const dateRange = getDatesBetween(formData.startDate, formData.endDate)
            const newAvailableDays = new Set(dateRange.map(date => date.toDateString()))
            setAvailableWorkDays(newAvailableDays)
        }
    }, [formData.startDate, formData.endDate])

    // 작업 가능 일자 토글 핸들러
    const toggleAvailableWorkDay = (date) => {
        const dateString = date.toDateString()
        const newAvailableDays = new Set(availableWorkDays)     // Set으로 관리
        
        if (newAvailableDays.has(dateString)) {
            newAvailableDays.delete(dateString)
        } else {
            newAvailableDays.add(dateString)
        }
        
        setAvailableWorkDays(newAvailableDays)
    }

    // // 저장 버튼 핸들러
    // const handleSaveDates = () => {
    //     const periodData = {
    //         startDate: formData.startDate,
    //         endDate: formData.endDate
    //     }
    //     console.log('설정 기간:', periodData)
    // }

    // 생성하기 버튼 클릭 시
    // TODO: api 요청 여부에 따라 상태 관리 코드 달라질 예정
    const handleCreate = () => {
        // 작업 가능 일자 - YYYY-MM-DD 형식으로 변환
        const availableWorkDaysList = Array.from(availableWorkDays).map(dateString => {
            const date = new Date(dateString)
            const year = date.getFullYear()
            const month = String(date.getMonth() + 1).padStart(2, '0')
            const day = String(date.getDate()).padStart(2, '0')
            return `${year}-${month}-${day}`
        }).sort()

        // 요일별 작업 시간 - 영어 요일명으로 변환하고 0시간인 요일 제외
        const dayMapping = {
            '월': 'MONDAY',
            '화': 'TUESDAY', 
            '수': 'WEDNESDAY',
            '목': 'THURSDAY',
            '금': 'FRIDAY',
            '토': 'SATURDAY',
            '일': 'SUNDAY'
        }

        const workingHours = {}
        Object.entries(formData.timeByDay).forEach(([koreanDay, hours]) => {
            if (hours > 0) {
                const englishDay = dayMapping[koreanDay]
                if (englishDay) {
                    workingHours[englishDay] = hours
                }
            }
        })

        // 시작일/종료일 YYYY-MM-DD 형식으로 변환
        const formatDate = (dateStr) => {
            if (!dateStr) return ''
            const date = new Date(dateStr.replace(/\//g, '-'))
            const year = date.getFullYear()
            const month = String(date.getMonth() + 1).padStart(2, '0')
            const day = String(date.getDate()).padStart(2, '0')
            return `${year}-${month}-${day}`
        }

        // POST 요청용 formData 구성
        const postFormData = {
            startDate: formatDate(formData.startDate),
            endDate: formatDate(formData.endDate),
            availableDates: availableWorkDaysList,
            workingHours: workingHours
        }
        
        console.log('프로젝트 생성 내 일정 설정: ', JSON.stringify(postFormData, null, 2))

        // const response = saveProjectSchedule(postFormData)
        saveProjectSchedule.mutate({ formData: postFormData })
    }

    // '작업 가능 일자' 컴포넌트에서 날짜 선택시
    const handleDateSelect = (date, type) => {
        const formattedDate = date.toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit'
        }).replace(/\. /g, '/').replace(/\.$/, '')

        const newData = {
            ...formData,
            [type === 'start' ? 'startDate' : 'endDate']: formattedDate
        }
        setFormData(newData)
        
        if (type === 'start') {
            setShowStartCalendar(false)
        } else {
            setShowEndCalendar(false)
        }

    }

    // activeDay 변경 시 step 업데이트
    useEffect(() => {
        const dayTime = formData.timeByDay[activeDay] || 0
        setStep(dayTime * 2) // 0.5시간 단위 → 1시간은 step 2
    }, [activeDay, formData.timeByDay])

    // 슬라이더 변경 핸들러
    const handleSliderChange = (newStep) => {
        setStep(newStep)
        const newTimeByDay = {
            ...formData.timeByDay,
            [activeDay]: newStep * 0.5
        }
        const newData = {
            ...formData,
            timeByDay: newTimeByDay
        }
        setFormData(newData)
    }

    // 마우스 이벤트 처리
    const sliderRef = useRef(null)
    const handleMouseDown = (e) => {
        const onMouseMove = (e) => {
            if (!sliderRef.current) return
            const rect = sliderRef.current.getBoundingClientRect()
            const x = Math.min(Math.max(e.clientX - rect.left, 0), rect.width)
            const percent = x / rect.width
            const newStep = Math.round(percent * maxStep)
            handleSliderChange(newStep)
        }

        const onMouseUp = () => {
            document.removeEventListener('mousemove', onMouseMove)
            document.removeEventListener('mouseup', onMouseUp)
        }

        document.addEventListener('mousemove', onMouseMove)
        document.addEventListener('mouseup', onMouseUp)
    }

    // 시간 포맷팅
    const formatTime = (step) => {
        const totalMinutes = step * 30
        const hours = Math.floor(totalMinutes / 60)
        const minutes = totalMinutes % 60
        return `${hours}시간 ${minutes > 0 ? minutes + '분' : ''}`
    }

    // 슬라이더 진행률
    const percent = (step / maxStep) * 100

    return (
        <div className={styles.container}>
            <div className={styles.section}>
                <Title className={styles.sectionTitle}>일정 설정</Title>

                {/* 기간 설정 */}
                <div className={styles.dateSection}>
                    <label className={styles.label}>기간*</label>
                    <div className={styles.dateRangeContainer}>
                        <div className={styles.dateRangeWrapper}>
                            <div className={styles.dateInputWrapper} onClick={() => setShowStartCalendar(true)}>
                                <CalendarIcon className={styles.calendarIcon} size={17} />
                                <input 
                                    type="text" 
                                    className={styles.dateInput}
                                    value={formData.startDate} 
                                    placeholder="시작일"
                                    readOnly
                                />
                                {showStartCalendar && (
                                    <Calendar 
                                        onDateSelect={(date) => handleDateSelect(date, 'start')} 
                                        onClose={() => setShowStartCalendar(false)} 
                                        selectedDate={formData.startDate ? new Date(formData.startDate.replace(/\//g, '-')) : null} 
                                    />
                                )}
                            </div>
                            <div className={styles.dateSeparator}>~</div>
                            <div className={styles.dateInputWrapper} onClick={() => setShowEndCalendar(true)}>
                                <CalendarIcon className={styles.calendarIcon} size={17} />
                                <input 
                                    type="text" 
                                    className={styles.dateInput}
                                    value={formData.endDate} 
                                    placeholder="종료일"
                                    readOnly
                                />
                                {showEndCalendar && (
                                    <Calendar 
                                        onDateSelect={(date) => handleDateSelect(date, 'end')} 
                                        onClose={() => setShowEndCalendar(false)} 
                                        selectedDate={formData.endDate ? new Date(formData.endDate.replace(/\//g, '-')) : null} 
                                    />
                                )}
                            </div>
                        </div>
                        {/* <Button 
                            type="secondary" 
                            onClick={handleSaveDates}
                            className={styles.saveButton}
                        >
                            저장
                        </Button> */}
                    </div>
                </div>

                {/* 작업 가능 일자 설정 */}
                <div className={styles.availableDaysSection}>
                    <label className={styles.label}>작업 가능 일자*</label>
                    <div className={styles.availableDaysCalendarWrapper}>
                        <AvailableDaysCalendar 
                            startDate={formData.startDate}
                            endDate={formData.endDate}
                            availableWorkDays={availableWorkDays}
                            onToggleDate={toggleAvailableWorkDay}
                        />
                    </div>
                </div>

                {/* 요일별 투자 시간 */}
                <div className={styles.timeSection}>
                    <label className={styles.label}>요일별 작업 시간*</label>
                    <div className={styles.daySelector}>
                        {days.map((day) => {
                            let dayClassName = styles.dayChip
                            if (day === activeDay) {
                                dayClassName += ` ${styles.active}`
                            } else if (formData.timeByDay[day] > 0) {
                                dayClassName += ` ${styles.inactive}`
                            } else {
                                dayClassName += ` ${styles.disabled}`
                            }

                            return (
                                <Button
                                    key={day}
                                    className={dayClassName}
                                    onClick={() => setActiveDay(day)}
                                    style={{}}
                                >
                                    {day}
                                </Button>
                            )
                        })}
                    </div>

                    <div className={styles.sliderWrapper}>
                        <div className={styles.sliderContainer} ref={sliderRef}>
                            <div className={styles.sliderTrack}>
                                <div 
                                    className={styles.sliderProgress} 
                                    style={{width: `${percent}%`}}
                                ></div>
                                <div 
                                    className={styles.sliderThumb} 
                                    style={{left: `calc(${percent}% - 11px)`}}
                                    onMouseDown={handleMouseDown}
                                >
                                    <svg width="25" height="25" viewBox="0 0 30 30" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <g filter="url(#slider-thumb-shadow)">
                                            <rect x="1" y="1" width="22" height="22" rx="11" fill="#F5F5F5"/>
                                        </g>
                                        <defs>
                                            <filter id="slider-thumb-shadow" x="0" y="0" width="30" height="30" filterUnits="userSpaceOnUse" colorInterpolationFilters="sRGB">
                                                <feFlood floodOpacity="0" result="BackgroundImageFix"/>
                                                <feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/>
                                                <feOffset dx="3" dy="3"/>
                                                <feGaussianBlur stdDeviation="2"/>
                                                <feComposite in2="hardAlpha" operator="out"/>
                                                <feColorMatrix type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.25 0"/>
                                                <feBlend mode="normal" in2="BackgroundImageFix" result="effect1_dropShadow_461_4148"/>
                                                <feBlend mode="normal" in="SourceGraphic" in2="effect1_dropShadow_461_4148" result="shape"/>
                                            </filter>
                                        </defs>
                                    </svg>
                                </div>
                            </div>
                        </div>
                        <div className={styles.sliderTextWrapper}>
                            <div className={styles.sliderText}>{formatTime(step)}</div>
                        </div>
                    </div>
                </div>

                {/* 생성 버튼 */}
                <div className={styles.actionButtons}>
                    <Button type='primary' onClick={handleCreate}>다음으로</Button>
                </div>

            </div>
        </div>
    )
}

// Calendar 컴포넌트
const Calendar = ({ onDateSelect, onClose, selectedDate }) => {
    const [currentMonth, setCurrentMonth] = useState(new Date())
    
    const getDaysInMonth = (date) => {
        const year = date.getFullYear()
        const month = date.getMonth()
        const firstDay = new Date(year, month, 1)
        const lastDay = new Date(year, month + 1, 0)
        const daysInMonth = lastDay.getDate()
        const startingDay = firstDay.getDay()
        
        const days = []
        for (let i = 0; i < startingDay; i++) {
            days.push(null)
        }
        for (let i = 1; i <= daysInMonth; i++) {
            days.push(new Date(year, month, i))
        }
        return days
    }

    const days = getDaysInMonth(currentMonth)
    const weekDays = ['일', '월', '화', '수', '목', '금', '토']
    
    const prevMonth = () => {
        setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() - 1, 1))
    }

    const nextMonth = () => {
        setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1, 1))
    }

    return (
        <div className={styles.calendarOverlay} onClick={onClose}>
            <div className={styles.calendarContainer} onClick={(e) => e.stopPropagation()}>
                <div className={styles.calendarHeader}>
                    <button onClick={prevMonth} className={styles.calendarNavButton}>&lt;</button>
                    <span className={styles.calendarTitle}>{currentMonth.getFullYear()}년 {currentMonth.getMonth() + 1}월</span>
                    <button onClick={nextMonth} className={styles.calendarNavButton}>&gt;</button>
                </div>
                <div className={styles.calendarWeekdays}>
                    {weekDays.map(day => (
                        <div key={day} className={styles.calendarWeekday}>{day}</div>
                    ))}
                </div>
                <div className={styles.calendarDays}>
                    {days.map((day, index) => (
                        <div
                            key={index}
                            className={`${styles.calendarDay} ${!day ? styles.calendarDayEmpty : ''} ${selectedDate && day && day.toDateString() === selectedDate.toDateString() ? styles.calendarDaySelected : ''}`}
                            onClick={() => day && onDateSelect(day)}
                        >
                            {day ? day.getDate() : ''}
                        </div>
                    ))}
                </div>
            </div>
        </div>
    )
}

// AvailableDaysCalendar 컴포넌트
const AvailableDaysCalendar = ({ startDate, endDate, availableWorkDays, onToggleDate }) => {
    const [currentMonth, setCurrentMonth] = useState(new Date())
    
    // 날짜 범위 계산
    const getDatesBetween = (startDate, endDate) => {
        if (!startDate || !endDate) return []
        
        const start = new Date(startDate.replace(/\//g, '-'))
        const end = new Date(endDate.replace(/\//g, '-'))
        const dates = []
        
        const currentDate = new Date(start)
        while (currentDate <= end) {
            dates.push(new Date(currentDate))
            currentDate.setDate(currentDate.getDate() + 1)
        }
        
        return dates
    }

    const dateRangeDates = getDatesBetween(startDate, endDate)
    const dateRangeSet = new Set(dateRangeDates.map(date => date.toDateString()))
    
    const getDaysInMonth = (date) => {
        const year = date.getFullYear()
        const month = date.getMonth()
        const firstDay = new Date(year, month, 1)
        const lastDay = new Date(year, month + 1, 0)
        const daysInMonth = lastDay.getDate()
        const startingDay = firstDay.getDay()
        
        const days = []
        for (let i = 0; i < startingDay; i++) {
            days.push(null)
        }
        for (let i = 1; i <= daysInMonth; i++) {
            days.push(new Date(year, month, i))
        }
        return days
    }

    const days = getDaysInMonth(currentMonth)
    const weekDays = ['일', '월', '화', '수', '목', '금', '토']
    
    const prevMonth = () => {
        setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() - 1, 1))
    }

    const nextMonth = () => {
        setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1, 1))
    }

    const handleDateClick = (date) => {
        const dateString = date.toDateString()
        // 설정된 기간 내의 날짜만 클릭 가능
        if (dateRangeSet.has(dateString)) {
            onToggleDate(date)
        }
    }

    const getDayClassName = (day) => {
        if (!day) return `${styles.availableCalendarDay} ${styles.availableCalendarDayEmpty}`
        
        const dateString = day.toDateString()
        let className = styles.availableCalendarDay
        
        // 설정된 기간 내의 날짜인지 확인
        const isInRange = dateRangeSet.has(dateString)
        const isAvailable = availableWorkDays.has(dateString)
        
        if (isInRange) {
            className += ` ${styles.availableCalendarDayInRange}`
            if (isAvailable) {
                className += ` ${styles.availableCalendarDaySelected}`
            } else {
                className += ` ${styles.availableCalendarDayDeselected}`
            }
        } else {
            className += ` ${styles.availableCalendarDayDisabled}`
        }
        
        return className
    }

    return (
        <div className={styles.availableCalendarContainer}>
            <div className={styles.availableCalendarHeader}>
                <button onClick={prevMonth} className={styles.availableCalendarNavButton}>&lt;</button>
                <span className={styles.availableCalendarTitle}>{currentMonth.getFullYear()}년 {currentMonth.getMonth() + 1}월</span>
                <button onClick={nextMonth} className={styles.availableCalendarNavButton}>&gt;</button>
            </div>
            <div className={styles.availableCalendarWeekdays}>
                {weekDays.map(day => (
                    <div key={day} className={styles.availableCalendarWeekday}>{day}</div>
                ))}
            </div>
            <div className={styles.availableCalendarDays}>
                {days.map((day, index) => (
                    <div
                        key={index}
                        className={getDayClassName(day)}
                        onClick={() => day && handleDateClick(day)}
                    >
                        {day ? day.getDate() : ''}
                    </div>
                ))}
            </div>
            <div className={styles.availableCalendarLegend}>
                <div className={styles.legendItem}>
                    <div className={`${styles.legendColor} ${styles.legendSelected}`}></div>
                    <span>작업 가능</span>
                </div>
                <div className={styles.legendItem}>
                    <div className={`${styles.legendColor} ${styles.legendDeselected}`}></div>
                    <span>작업 불가</span>
                </div>
            </div>
        </div>
    )
}

export default ProjectScheduleSetter