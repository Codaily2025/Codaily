package com.codaily.codereview;

import com.codaily.codereview.dto.ChecklistEvaluationRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class GptRequestClient {

    private final RestTemplate restTemplate;

    // 체크리스트 구현 확인 요청 보내기
    public ChecklistEvaluationRequestDto requestChecklistEvaluation(ChecklistEvaluationRequestDto request) {
        String url = "http://localhost:8000/api/code-review/checklist-evaluation";
        ResponseEntity<ChecklistEvaluationRequestDto> response = restTemplate.postForEntity(url, request, ChecklistEvaluationRequestDto.class);
        return response.getBody();
    }
}
