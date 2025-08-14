package com.codaily.project.service;

import com.codaily.codereview.dto.FeatureChecklistFeatureDto;
import com.codaily.codereview.dto.FeatureChecklistRequestDto;
import com.codaily.codereview.dto.FeatureChecklistResponseDto;
import com.codaily.codereview.entity.FeatureItemChecklist;
import com.codaily.codereview.repository.FeatureItemChecklistRepository;
import com.codaily.codereview.dto.*;
import com.codaily.global.exception.ProjectNotFoundException;
import com.codaily.management.entity.FeatureItemSchedule;
import com.codaily.management.repository.DaysOfWeekRepository;
import com.codaily.management.repository.FeatureItemSchedulesRepository;
import com.codaily.project.dto.*;
import com.codaily.codereview.entity.FeatureItemChecklist;
import com.codaily.codereview.repository.FeatureItemChecklistRepository;
import com.codaily.project.dto.FeatureSaveContent;
import com.codaily.project.dto.FeatureSaveItem;
import com.codaily.project.dto.FeatureSaveRequest;
import com.codaily.project.dto.FeatureSaveResponse;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.entity.Project;
import com.codaily.project.entity.Specification;
import com.codaily.project.exception.FeatureNotFoundException;
import com.codaily.project.repository.FeatureItemRepository;
import com.codaily.project.repository.ProjectRepository;
import com.codaily.project.repository.ScheduleRepository;
import com.codaily.project.repository.SpecificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureItemServiceImpl implements FeatureItemService {

    private static final Integer BATCH_SIZE = 1_000;

    private final ScheduleService scheduleService;
    private final ProjectRepository projectRepository;
    private final SpecificationRepository specificationRepository;
    private final FeatureItemRepository featureItemRepository;
    private final FeatureItemSchedulesRepository featureItemScheduleRepository;
    private final DaysOfWeekRepository daysOfWeekRepository;
    private final ScheduleRepository scheduleRepository;
    private final FeatureItemChecklistRepository checklistRepository;
    @Qualifier("langchainWebClient")   // ★ LangChain(FastAPI) 호출용
    private final WebClient langchainWebClient;
    private final AsyncScheduleService asyncScheduleService;
    private final FeatureItemSchedulesRepository featureItemSchedulesRepository;

    @Override
    public FeatureItemResponse createFeature(Long projectId, FeatureItemCreateRequest featureItem) {
        if (projectId == null || featureItem == null) {
            throw new IllegalArgumentException("프로젝트 ID와 생성 정보는 필수입니다.");
        }
        if (featureItem.getTitle() == null || featureItem.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("기능 제목은 필수입니다.");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        FeatureItem feature = FeatureItem.builder()
                .title(featureItem.getTitle().trim())
                .description(featureItem.getDescription())
                .field(featureItem.getField())
                .category(featureItem.getCategory())
                .priorityLevel(featureItem.getPriorityLevel())
                .estimatedTime(featureItem.getEstimatedTime())
                .isCustom(true)
                .isSelected(true)
                .isReduced(false)
                .project(project)
                .build();

        // 부모 기능 설정
        if (featureItem.getParentFeatureId() != null) {
            FeatureItem parentFeature = featureItemRepository.findByProject_ProjectIdAndFeatureId(
                            projectId, featureItem.getParentFeatureId())
                    .orElseThrow(() -> new FeatureNotFoundException(featureItem.getParentFeatureId()));

            feature.setParentFeature(parentFeature);
            feature.setField(parentFeature.getField());

            log.info("부모 기능 선택됨 - 부모 ID: {}, 부모 field: {}",
                    parentFeature.getFeatureId(), parentFeature.getField());
        } else {
            feature.setField(featureItem.getField());
        }

        FeatureItem savedFeature = featureItemRepository.save(feature);

        if (featureItem.getEstimatedTime() != null && featureItem.getEstimatedTime() > 0) {
            asyncScheduleService.rescheduleFromFeatureCreateAsync(projectId, savedFeature);
        }

        log.info("기능 생성 완료 - 프로젝트 ID: {}, 기능 ID: {}", projectId, savedFeature.getFeatureId());

        return convertToResponseDto(feature);
    }


    @Override
    public FeatureItemResponse getFeature(Long projectId, Long featureId) {
        if (projectId == null || featureId == null) {
            throw new IllegalArgumentException("프로젝트 ID와 기능 ID는 필수입니다.");
        }

        FeatureItem feature = featureItemRepository.findByProject_ProjectIdAndFeatureId(projectId, featureId)
                .orElseThrow(() -> new FeatureNotFoundException(featureId));

        return convertToResponseDto(feature);
    }

    public FeatureItemResponse getFeature(Long featureId) {
        FeatureItem item = featureItemRepository.getFeatureItemByFeatureId(featureId);
        return FeatureItemResponse.builder()
                .featureId(item.getFeatureId())
                .title(item.getTitle())
                .description(item.getDescription())
                .estimatedTime(item.getEstimatedTime())
                .priorityLevel(item.getPriorityLevel())
                .status(item.getStatus())
                .build();
    }

    @Override
    @Transactional
    public FeatureItemResponse updateFeature(Long projectId, Long featureId, FeatureItemUpdateRequest update) {
        if (projectId == null || featureId == null || update == null) {
            throw new IllegalArgumentException();
        }

        if (!projectRepository.existsById(projectId)) {
            throw new ProjectNotFoundException(projectId);
        }

        FeatureItem feature = featureItemRepository.findByProject_ProjectIdAndFeatureId(projectId, featureId)
                .orElseThrow(() -> new FeatureNotFoundException(featureId));

        Integer oldPriorityLevel = feature.getPriorityLevel();
        Double oldEstimatedTime = feature.getEstimatedTime();
        boolean needsRescheduling = false;
        String oldStatus = feature.getStatus();

        if (update.getStatus() != null) {
            String newStatus = update.getStatus();
            if ("DONE".equals(newStatus) && !"DONE".equals(oldStatus)) {
                List<FeatureItemSchedule> schedules = featureItemSchedulesRepository.findByFeatureItem_FeatureIdOrderByScheduleDateAsc(featureId);

                if (!schedules.isEmpty()) {
                    LocalDate lastScheduledDate = schedules.get(schedules.size() - 1).getScheduleDate();
                    LocalDate today = LocalDate.now();

                    // 예정된 마지막 날보다 일찍 완료된 경우에만 재스케줄링
                    if (today.isBefore(lastScheduledDate)) {
                        featureItemSchedulesRepository.deleteByFeatureItem_FeatureIdAndScheduleDateAfter(
                                featureId, today);

                        asyncScheduleService.rescheduleProjectAsync(feature.getProjectId())
                                .whenComplete((result, throwable) -> {
                                    if (throwable != null) {
                                        log.error("기능 완료 후 일정 재조정 실패 - 기능: {}, 프로젝트: {}",
                                                featureId, feature.getProjectId(), throwable);
                                    } else {
                                        log.info("기능 완료 후 일정 재조정 성공 - 기능: {}, 프로젝트: {}",
                                                featureId, feature.getProjectId());
                                    }
                                });
                    }
                }
            }
        }
                if (update.getPriorityLevel() != null) {
                    Integer oldPriority = feature.getPriorityLevel();
                    feature.setPriorityLevel(update.getPriorityLevel());
                    if (!java.util.Objects.equals(oldPriority, update.getPriorityLevel())) {
                        needsRescheduling = true;
                    }
                }
                if (update.getEstimatedTime() != null) {
                    if (update.getEstimatedTime() < 0) {
                        throw new IllegalArgumentException("예상 시간은 0 이상이어야 합니다.");
                    }
                    Double oldTime = feature.getEstimatedTime();
                    feature.setEstimatedTime(update.getEstimatedTime());
                    if (!java.util.Objects.equals(oldTime, update.getEstimatedTime())) {
                        needsRescheduling = true;
                    }
                }

                if (needsRescheduling) {
                    asyncScheduleService.rescheduleFromFeatureUpdateAsync(projectId, feature, oldPriorityLevel, oldEstimatedTime);
                }

                log.info("기능 수정 완료 - 프로젝트 ID: {}, 기능 ID: {}", projectId, featureId);
                return convertToResponseDto(feature);

    }


    @Override
    @Transactional
    public void deleteFeature(Long projectId, Long featureId) {
        if (projectId == null || featureId == null) {
            throw new IllegalArgumentException("프로젝트 ID와 기능 ID는 필수입니다.");
        }

        FeatureItem feature = featureItemRepository.findByProject_ProjectIdAndFeatureId(projectId, featureId)
                .orElseThrow(() -> new FeatureNotFoundException(featureId));

        // 연관된 스케줄들 삭제
        if (featureItemScheduleRepository != null) {
            featureItemScheduleRepository.deleteByFeatureItemFeatureId(featureId);
            log.info("기능 관련 스케줄 삭제 완료 - 기능 ID: {}", featureId);
        }

        // 하위 기능들의 부모 참조 제거
        feature.getChildFeatures().forEach(child -> child.setParentFeature(null));

        featureItemRepository.delete(feature);
        asyncScheduleService.rescheduleFromFeatureDeleteAsync(projectId, feature);
        log.info("기능 삭제 완료 - 프로젝트 ID: {}, 기능 ID: {}", projectId, featureId);
    }

    private FeatureItemResponse convertToResponseDto(FeatureItem feature) {
        return FeatureItemResponse.builder()
                .featureId(feature.getFeatureId())
                .title(feature.getTitle())
                .description(feature.getDescription())
                .field(feature.getField())
                .category(feature.getCategory())
                .status(feature.getStatus())
                .priorityLevel(feature.getPriorityLevel())
                .estimatedTime(feature.getEstimatedTime())
                .isSelected(feature.getIsSelected())
                .isCustom(feature.getIsCustom())
                .isReduced(feature.getIsReduced())
                .projectId(feature.getProject().getProjectId())
                .specificationId(feature.getSpecification() != null ? feature.getSpecification().getSpecId() : null)
                .parentFeatureId(feature.getParentFeature() != null ? feature.getParentFeature().getFeatureId() : null)
                .build();
    }
    private final FeatureItemChecklistRepository featureItemChecklistRepository;


    @Override
    @Transactional
    public FeatureSaveResponse saveSpecChunk(FeatureSaveRequest chunk, Long projectId, Long specId, String type) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid projectId"));
        Specification spec = specificationRepository.findById(specId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid specId"));

        // 1. 상세 기능들의 예상시간 총합 계산
        double totalEstimatedTime = chunk.getSubFeature().stream()
                .mapToDouble(sub -> sub.getEstimatedTime() != null ? sub.getEstimatedTime() : 0)
                .sum();

        // 2. 주 기능 저장
        FeatureItem mainFeature = FeatureItem.builder()
                .title(chunk.getMainFeature().getTitle())
                .description(chunk.getMainFeature().getDescription())
                .field(chunk.getField())
                .project(project)
                .specification(spec)
                .estimatedTime(totalEstimatedTime)
                .isCustom(false)
                .build();

        FeatureItem savedMain = featureItemRepository.save(mainFeature);

        FeatureSaveItem mainFeatureDto = FeatureSaveItem.builder()
                .id(savedMain.getFeatureId())
                .title(savedMain.getTitle())
                .isReduced(false)
                .description(savedMain.getDescription())
                .estimatedTime(savedMain.getEstimatedTime())
                .priorityLevel(null)
                .build();

        // 3. 상세 기능 저장
        List<FeatureSaveItem> subFeatureDtos = chunk.getSubFeature().stream().map(sub -> {
            FeatureItem subFeature = FeatureItem.builder()
                    .title(sub.getTitle())
                    .description(sub.getDescription())
                    .field(savedMain.getField())
                    .project(project)
                    .isReduced(false)
                    .specification(spec)
                    .priorityLevel(sub.getPriorityLevel())
                    .parentFeature(savedMain)
                    .estimatedTime(sub.getEstimatedTime())
                    .isCustom(false)
                    .build();
            FeatureItem savedSub = featureItemRepository.save(subFeature);

            return FeatureSaveItem.builder()
                    .id(savedSub.getFeatureId())
                    .title(savedSub.getTitle())
                    .isReduced(false)
                    .description(savedSub.getDescription())
                    .estimatedTime(savedSub.getEstimatedTime())
                    .priorityLevel(savedSub.getPriorityLevel())
                    .build();
        }).toList();

        FeatureSaveContent content = FeatureSaveContent.builder()
                .projectId(projectId)
                .specId(specId)
                .field(chunk.getField())
                .isReduced(false)
                .mainFeature(mainFeatureDto)
                .subFeature(subFeatureDtos)
                .build();


        return FeatureSaveResponse.builder()
                .type(type)
                .content(content)
                .build();
    }

//    @Override
//    @Transactional
//    public void updateFeatureItem(FeatureSaveItem request) {
//        FeatureItem item = featureItemRepository.findById(request.getId())
//                .orElseThrow(() -> new IllegalArgumentException("해당 기능이 존재하지 않습니다."));
//
//        item.setTitle(request.getTitle());
//        item.setDescription(request.getDescription());
//        item.setEstimatedTime(request.getEstimatedTime());
//        item.setPriorityLevel(request.getPriorityLevel());
//    }

    @Override
    @Transactional
    public FeatureSaveResponse regenerateSpec(FeatureSaveRequest chunk, Long projectId, Long specId) {
        // 1. 기존 명세 항목 전부 삭제
        featureItemRepository.deleteBySpecification_SpecId(specId);

        // 2. 새로 들어온 chunk 저장
        return saveSpecChunk(chunk, projectId, specId, "spec:regenerate"); // 기존 저장 메서드 재사용
    }

    @Override
    @Transactional
    public void deleteBySpecId(Long specId) {
        featureItemRepository.deleteBySpecification_SpecId(specId);
    }

    @Override
    @Transactional
    public int calculateTotalEstimatedTime(Long specId) {
        Integer total = featureItemRepository.getTotalEstimatedTimeBySpecId(specId);
        return total != null ? total : 0;
    }

    @Override
    @Transactional
    public List<FeatureItem> getAllMainFeature(Long projectId) {
        return featureItemRepository.findMainFeaturesByProjectId(projectId);
    }

    @Override
    @Transactional
    public SubFeatureSaveResponse saveSubFeatureChunk(SubFeatureSaveRequest request, Long projectId, Long specId) {
        log.info("saveSubFeatureChunk... {}", request);
        // 1. 상위 엔티티 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다."));
        Specification spec = specificationRepository.findById(specId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 명세입니다."));
        FeatureItem parentFeature = featureItemRepository.findById(request.getFeatureId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주 기능입니다."));

        // 2. 서브 기능 생성
        FeatureItem subFeature = FeatureItem.builder()
                .project(project)
                .specification(spec)
                .field(request.getField())
                .parentFeature(parentFeature)
                .title(request.getSubFeature().getTitle())
                .description(request.getSubFeature().getDescription())
                .estimatedTime(request.getSubFeature().getEstimatedTime())
                .priorityLevel(request.getSubFeature().getPriorityLevel())
                .build();

        // 3. 저장
        FeatureItem saved = featureItemRepository.save(subFeature);

        // 4. 응답 DTO 구성
        return SubFeatureSaveResponse.builder()
                .type("spec:add:feature:sub")
                .content(
                        SubFeatureSaveResponse.SubFeatureItem.builder()
                                .projectId(projectId)
                                .specId(specId)
                                .parentFeatureId(request.getFeatureId())
                                .featureSaveItem(
                                        FeatureSaveItem.builder()
                                                .id(saved.getFeatureId())
                                                .title(saved.getTitle())
                                                .isReduced(false)
                                                .description(saved.getDescription())
                                                .estimatedTime(saved.getEstimatedTime())
                                                .priorityLevel(saved.getPriorityLevel())
                                                .build()
                                )
                                .build())
                .build();
    }

    @Override
    public FeatureItem findByProjectIdAndTitle(Long projectId, String featureName) {
        return featureItemRepository.findByProject_ProjectIdAndTitle(projectId, featureName)
                .orElseThrow(() -> new IllegalArgumentException(featureName + "의 기능을 찾을 수 없습니다"));
    }

    @Override
    public FeatureItem findById(Long featureId) {
        return featureItemRepository.findById(featureId)
                .orElseThrow(() -> new IllegalArgumentException("기능을 찾을 수 없습니다."));
    }

    @Override
    @Transactional
    public void generateFeatureItemChecklist(Long projectId) {
        List<FeatureItem> featureItems = featureItemRepository.findByProject_ProjectId(projectId);

        // 중복 방지: List -> Set
        Map<Long, java.util.LinkedHashSet<String>> checklistMap = new HashMap<>();

        List<FeatureChecklistFeatureDto> dtoList = featureItems.stream()
                .filter(item -> item.getParentFeature() != null)
                .map(item -> new FeatureChecklistFeatureDto(
                        item.getFeatureId(),
                        item.getTitle()))
                .toList();

        if (!dtoList.isEmpty()) {
            FeatureChecklistRequestDto request = FeatureChecklistRequestDto.builder()
                    .features(dtoList)
                    .build();

            try {
                FeatureChecklistResponseDto response = langchainWebClient
                        .post()
                        .uri("ai/api/generate-checklist")
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(FeatureChecklistResponseDto.class)
                        .block();

                if (response != null && response.getChecklistMap() != null) {
                    response.getChecklistMap().forEach((featureIdStr, items) -> {
                        Long fid = Long.parseLong(featureIdStr);
                        checklistMap
                                .computeIfAbsent(fid, k -> new java.util.LinkedHashSet<>())
                                .addAll(items); // Set으로 중복 자동 제거
                    });
                }
            } catch (Exception e) {
                log.error("Checklist 생성 실패", e);
            }
        }

        // 3) 저장 (선택) 존재 여부 체크로 중복 방지 2중 안전장치
        checklistMap.forEach((featureId, items) -> {
            FeatureItem featureItem = featureItemRepository.getReferenceById(featureId);
            items.forEach(content -> {
                    FeatureItemChecklist checklist = FeatureItemChecklist.builder()
                            .featureItem(featureItem)
                            .item(content)
                            .done(false)
                            .build();
                    featureItemChecklistRepository.save(checklist);

            });
        });
        log.info("체크리스트 저장 완료");
    }



    @Override
    public boolean generateExtraFeatureItemChecklist(Long featureId) {
        FeatureItem featureItem = featureItemRepository.getFeatureItemByFeatureId(featureId);
        Project project = projectRepository.getProjectByProjectId(featureItem.getProjectId());

        // 주 기능이면 return
        if (featureItem.getParentFeature() == null) {
            log.warn("해당 기능은 주기능. featureId = {}", featureId);
            return false;
        }
        FeatureChecklistFeatureDto dto = FeatureChecklistFeatureDto.builder()
                .featureId(featureId)
                .title(featureItem.getTitle())
                .build();

        FeatureChecklistExtraRequestDto request = FeatureChecklistExtraRequestDto.builder()
                .features(List.of(dto))
                .projectName(project.getTitle())
                .build();

        try {
            FeatureChecklistExtraResponseDto response = langchainWebClient
                    .post()
                    .uri("ai/api/generate-checklist")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(FeatureChecklistExtraResponseDto.class)
                    .block();

            Map<String, List<String>> checklistMap = response.getChecklistMap();
            Boolean valid = response.isValid();

            if(!valid) {
                return false;
            }

            if (checklistMap == null) {
                log.warn("Checklist 응답이 비어있습니다.");
                return false;
            }

            // checklist 저장
            checklistMap.forEach((featureIdStr, checklistItems) -> {
                List<FeatureItemChecklist> checklistList = checklistItems.stream()
                        .map(item -> FeatureItemChecklist.builder()
                                .featureItem(featureItem).item(item).done(false).build())
                        .toList();

                featureItemChecklistRepository.saveAll(checklistList);
            });

            log.info("Checklist 생성 및 저장 완료");
        } catch (WebClientResponseException e) {
            log.error("Checklist 생성 실패: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Checklist 생성 중 예외 발생", e);
        }
        return true;
    }

    @Override
    public ParentFeatureListResponse getParentFeatures(Long projectId) {
        List<FeatureItem> parentFeatures = featureItemRepository.findParentFeaturesByProject(projectId);

        List<ParentFeatureResponse> responseList = parentFeatures.stream()
                .map(feature -> {
                    return ParentFeatureResponse.builder()
                            .id(feature.getFeatureId())
                            .title(feature.getTitle())
                            .field(feature.getField())
                            .build();
                })
                .collect(Collectors.toList());

        return ParentFeatureListResponse.builder()
                .parentFeatures(responseList)
                .build();
    }

    @Override
    public Long getSpecIdByFeatureId(Long featureId) {
        if (featureId == null) return null;

        return featureItemRepository.findSpecIdByFeatureId(featureId);
    }

    @Override
    public boolean existsActive(Long specId) {
        if (specId == null) return false;
        return featureItemRepository.existsBySpecification_SpecId(specId);
    }

    @Transactional
    public void updateIsReduced(Long projectId, String field, Long featureId, Boolean isReduced) {
        if (isReduced == null) throw new IllegalArgumentException("isReduced 값이 필요합니다.");
        if ((field == null && featureId == null) || (field != null && featureId != null)) {
            throw new IllegalArgumentException("field 또는 featureId 중 하나만 지정해야 합니다.");
        }

        // 프로젝트 존재 확인 (권한/소유 검증은 컨트롤러/인터셉터에서 추가)
        if (!projectRepository.existsById(projectId)) {
            throw new IllegalArgumentException("존재하지 않는 프로젝트입니다. projectId=" + projectId);
        }

        if (field != null) {
            // 1) 프로젝트 + 필드 단위 벌크 업데이트 (조인 불필요)
            featureItemRepository.bulkUpdateIsReducedByField(projectId, field, isReduced);
            return;
        }

        // 2) featureId 모드: 해당 기능이 해당 프로젝트에 속하는지 확인
        if (!featureItemRepository.existsByFeatureIdAndProjectId(featureId, projectId)) {
            throw new IllegalArgumentException("해당 기능이 프로젝트에 속하지 않습니다. featureId=" + featureId);
        }

        // 3) 서브트리 모든 ID BFS 수집 (ID만 다룸 → 가볍고 빠름)
        Deque<Long> q = new ArrayDeque<>();
        List<Long> allIds = new ArrayList<>();
        q.add(featureId);
        while (!q.isEmpty()) {
            Long cur = q.pollFirst();
            allIds.add(cur);
            q.addAll(featureItemRepository.findChildIds(cur));
        }

        // 4) IN 벌크 업데이트 (대용량 대비 배치 처리)
        for (int i = 0; i < allIds.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, allIds.size());
            featureItemRepository.bulkUpdateIsReducedByIds(allIds.subList(i, end), isReduced);
        }
    }

    @Override
    @Transactional
    public SpecificationFinalizeResponse finalizeSpecification(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다. id=" + projectId));

        Specification spec = project.getSpecification();
        if (spec == null) {
            throw new IllegalStateException("프로젝트에 연결된 스펙이 없습니다. projectId=" + projectId);
        }

        int deleted = featureItemRepository.deleteReducedBySpecId(spec.getSpecId());

        return SpecificationFinalizeResponse.builder()
                .projectId(project.getProjectId())
                .specId(spec.getSpecId())
                .deletedCount(deleted)
                .build();
    }
}
