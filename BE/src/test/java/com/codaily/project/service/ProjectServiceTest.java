package com.codaily.project.service;

import com.codaily.auth.entity.User;
import com.codaily.auth.repository.UserRepository;
import com.codaily.management.entity.DaysOfWeek;
import com.codaily.management.entity.Schedule;
import com.codaily.management.repository.DaysOfWeekRepository;
import com.codaily.project.dto.FeatureItemReduceItem;
import com.codaily.project.dto.FeatureItemReduceResponse;
import com.codaily.project.dto.ProjectCreateRequest;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.entity.Project;
import com.codaily.project.entity.Specification;
import com.codaily.project.repository.FeatureItemRepository;
import com.codaily.project.repository.ProjectRepository;
import com.codaily.project.repository.ScheduleRepository;
import com.codaily.project.repository.SpecificationRepository;
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
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private DaysOfWeekRepository daysOfWeekRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FeatureItemRepository featureItemRepository;
    @Autowired
    private SpecificationRepository specificationRepository;

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
        Project savedProject = projectService.createProject(request, user);

        // then
        Project fetch = projectRepository.findByProjectId(savedProject.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트를 찾을 수 없습니다."));

        assertEquals("검색 엔진 프로젝트", fetch.getTitle());
        assertEquals(LocalDate.of(2025, 4, 1), fetch.getStartDate());

        List<Schedule> schedules = scheduleRepository.findAll();
        assertEquals(2, schedules.size());
        assertEquals(fetch.getProjectId(), schedules.get(0).getProject().getProjectId());

        List<DaysOfWeek> days = daysOfWeekRepository.findAll();
        assertEquals(2, days.size());

        List<String> dayNames = days.stream().map(DaysOfWeek::getDateName).toList();
        assertTrue(dayNames.contains("월"));
        assertTrue(dayNames.contains("수"));

        List<Integer> hours = days.stream().map(DaysOfWeek::getHours).toList();
        assertTrue(hours.contains(4));
        assertTrue(hours.contains(6));
    }

    @Test
    void 개발가능시간보다_기능예상시간이_많으면_일부기능이_축소된다() {
        // 1. 사용자 생성
        User user = userRepository.save(User.builder()
                .email("temp2@example.com")
                .nickname("기능축소테스터")
                .socialProvider("test")
                .build());

        // 2. 명세서 생성
        Specification spec = specificationRepository.save(Specification.builder()
                .title("기능 명세서")
                .build());

        // 3. 프로젝트 생성 요청 구성
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setTitle("기능 축소 테스트 프로젝트");
        request.setDescription("기능 예상 시간이 많으면 줄이기");
        request.setStartDate(LocalDate.of(2025, 8, 4));
        request.setEndDate(LocalDate.of(2025, 8, 5));
        request.setAvailableDates(List.of(
                LocalDate.of(2025, 8, 4),
                LocalDate.of(2025, 8, 5)
        ));
        request.setWorkingHours(Map.of(
                "MONDAY", 2,
                "TUESDAY", 1
        ));

        // 4. 프로젝트 생성 + 명세서 연동
        Project project = projectService.createProject(request, user);
        project.setSpecification(spec);  // Project가 spec_id를 가짐
        projectRepository.save(project);

        // 5. FeatureItem 여러 개 생성
        featureItemRepository.save(FeatureItem.builder()
                .specification(spec)
                .title("로그인")
                .description("로그인 처리")
                .estimatedTime(2.0)
                .priorityLevel(1)
                .project(project)
                .build());

        featureItemRepository.save(FeatureItem.builder()
                .specification(spec)
                .title("회원가입")
                .description("회원가입 처리")
                .estimatedTime(2.0)
                .priorityLevel(2)
                .project(project)
                .build());

        featureItemRepository.save(FeatureItem.builder()
                .specification(spec)
                .title("소셜 로그인")
                .description("소셜 로그인 연동")
                .estimatedTime(2.0)
                .priorityLevel(3)
                .project(project)
                .build());

        // 6. 기능 축소 수행
        FeatureItemReduceResponse result = projectService.reduceFeatureItemsIfNeeded(project.getProjectId(), spec.getSpecId());

        // 7. 검증
        assertEquals(6, result.getTotalEstimatedTime());
        assertEquals(3, result.getTotalAvailableTime());
        assertEquals(2, result.getReducedCount());
        assertEquals(1, result.getKeptCount());

        List<FeatureItemReduceItem> reducedItems = result.getFeatures();
        long trueCount = reducedItems.stream().filter(FeatureItemReduceItem::getIsReduced).count();
        long falseCount = reducedItems.stream().filter(i -> !i.getIsReduced()).count();

        assertEquals(2, trueCount);
        assertEquals(1, falseCount);
    }

}
