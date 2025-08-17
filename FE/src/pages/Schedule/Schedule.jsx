import React from 'react';
import Calendar from '../../components/organisms/Calendar';
import './Schedule.css';

const Schedule = () => {
  return (
    <div className="schedule-container">
      {/* <h1>일정</h1> */}
        <Calendar />
    </div>
  );
};

export default Schedule; 