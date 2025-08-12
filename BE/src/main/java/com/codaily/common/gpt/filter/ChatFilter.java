package com.codaily.common.gpt.filter;

import com.codaily.common.gpt.dto.ChatFilterResult;
import com.codaily.common.gpt.handler.MessageType;
import com.codaily.project.service.FeatureItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatFilter {

    private final FeatureItemService featureItemService;


    public ChatFilterResult check(MessageType intent, Long specId, Long featureId, String field) {
        boolean hasFeature = (specId != null) && featureItemService.existsActive(specId);

        if (!hasFeature && (intent == MessageType.SPEC_ADD_FIELD
                || intent == MessageType.SPEC_ADD_FEATURE_MAIN
                || intent == MessageType.SPEC_ADD_FEATURE_SUB)) {
            return new ChatFilterResult(false, "NO_SPEC", "명세가 아직 없습니다. 먼저 명세서를 생성하세요.");
        }

        if (intent == MessageType.SPEC_ADD_FEATURE_MAIN) {
            if (field == null || field.trim().isEmpty()) {
                return new ChatFilterResult(false, "MISSING_FIELD", "주 기능을 추가하려면 field(기능 그룹명)가 필요합니다.");
            }
        }

        if (intent == MessageType.SPEC_ADD_FEATURE_SUB) {
            if (featureId == null) {
                return new ChatFilterResult(false, "MISSING_FEATURE_ID", "상세 기능을 추가하려면 주 기능이 필요합니다.");
            }
            Long ownerSpecId = featureItemService.getSpecIdByFeatureId(featureId);
            if (ownerSpecId == null || !ownerSpecId.equals(specId)) {
                return new ChatFilterResult(false, "FEATURE_NOT_IN_SPEC", "해당 주 기능은 현재 명세에 속하지 않습니다.");
            }
        }

        return new ChatFilterResult(true, "OK", "허용");
    }
}

