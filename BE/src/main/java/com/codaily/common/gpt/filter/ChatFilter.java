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
        if (intent == MessageType.DELETE) {
            return new ChatFilterResult(
                    false,
                    "DELETE_NOT_SUPPORTED",
                    "채팅으로는 삭제가 불가능합니다. 명세서의 체크 박스를 명시적으로 해제해주세요."
            );
        }

        if(intent == MessageType.IGNORE_DROP) {
            return new ChatFilterResult(
                    false,
                    "OUT_OF_SCOPE",
                    "명세서 생성과 관련된 채팅 부탁드립니다."
            );
        }

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

