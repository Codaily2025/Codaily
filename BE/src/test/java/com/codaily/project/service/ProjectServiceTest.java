package com.codaily.project.service;

import com.codaily.auth.entity.User;
import com.codaily.auth.repository.UserRepository;
import com.codaily.project.dto.ProjectCreateRequest;
import com.codaily.project.entity.DaysOfWeek;
import com.codaily.project.entity.Project;
import com.codaily.project.repository.ProjectRepository;
import com.codaily.project.repository.ScheduleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
public class ProjectServiceTest {

    @Autowired
    private ProjectService projectService;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private ScheduleRepository scheduleRepository;
    @Autowired private DaysOfWeekRepository daysOfWeekRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void 프로젝트를_생성하면_DB에_정상적으로_저장된다() {
        // given
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setTitle("검색 엔진 프로젝트");
        request.setDescription("모든 프로젝트에서 사용할 수 있는 검색엔진 구현");
        request.setStartDate(LocalDate.of(2025, 4, 1));
        request.setEndDate(LocalDate.of(2025, 6, 30));
        request.setAvailableDates(List.of(
                LocalDate.of(2025, 4, 3),
                LocalDate.of(2025, 4, 4)
        ));
        request.setWorkingHours(Map.of(
                "월", 4,
                "수", 6
        ));

        // 임시 유저 생성 (테스트용)
        User user = userRepository.save(User.builder()
                .email("temp@example.com")
                .nickname("임시 사용자")
                .socialProvider("google")
                .build());

        // when
        projectService.createProject(request, user);

        // then
        List<Project> projects = projectRepository.findAll();
        assertEquals(1, projects.size());

        Project saved = projects.get(0);
        assertEquals("검색 엔진 프로젝트", saved.getTitle());
        assertEquals(LocalDate.of(2025, 4, 1), saved.getStartDate());

        List<Schedule> schedules = scheduleRepository.findAll();
        assertEquals(2, schedules.size());
        assertEquals(saved.getProjectId(), schedules.get(0).getProject().getProjectId());

        List<DaysOfWeek> days = daysOfWeekRepository.findAll();
        assertEquals(2, days.size());

        List<String> dayNames = days.stream().map(DaysOfWeek::getDayName).toList();
        assertTrue(dayNames.contains("월"));
        assertTrue(dayNames.contains("수"));

        List<Integer> hours = days.stream().map(DaysOfWeek::getHours).toList();
        assertTrue(hours.contains(4));
        assertTrue(hours.contains(6));
    }
}
