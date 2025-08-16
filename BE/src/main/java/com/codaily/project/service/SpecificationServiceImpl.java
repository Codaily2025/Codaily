package com.codaily.project.service;

import com.codaily.project.dto.FeatureSaveContent;
import com.codaily.project.dto.FeatureSaveItem;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.entity.Project;
import com.codaily.project.repository.FeatureItemRepository;
import com.codaily.project.repository.ProjectRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class SpecificationServiceImpl implements SpecificationService {

    private final ProjectRepository projectRepository;
    private final FeatureItemRepository featureItemRepository;

    private Font fontTitle;
    private Font fontSection;
    private Font fontBold;
    private Font font;
    private Font fontSmall;

    @PostConstruct
    public void initFonts() {
        try {
            // 1) 클래스패스에서 바이트 읽기
            byte[] regularBytes;
            byte[] boldBytes;
            {
                Resource regRes = new ClassPathResource("fonts/Pretendard-Regular.ttf");
                Resource boldRes = new ClassPathResource("fonts/Pretendard-Bold.ttf");
                try (var in = regRes.getInputStream()) { regularBytes = in.readAllBytes(); }
                try (var in = boldRes.getInputStream()) { boldBytes = in.readAllBytes(); }
            }

            // 2) BaseFont 생성 (바이트 기반)
            BaseFont base = BaseFont.createFont(
                    "Pretendard-Regular.ttf",
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED,
                    true,
                    regularBytes,
                    null
            );
            BaseFont baseBold = BaseFont.createFont(
                    "Pretendard-Bold.ttf",
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED,
                    true,
                    boldBytes,
                    null
            );

            // 3) Font 스타일 구성
            this.fontTitle   = new Font(baseBold, 24);
            this.fontSection = new Font(baseBold, 14);
            this.fontBold    = new Font(baseBold, 18);
            this.font        = new Font(base, 12);
            this.fontSmall   = new Font(base, 11);

            log.info("PDF 폰트 초기화 완료");
        } catch (Exception e) {
            log.error("PDF 폰트 초기화 실패", e);
            throw new RuntimeException("PDF 폰트 로드 실패", e);
        }
    }

    @Override
    public byte[] generateSpecDocument(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다."));

        String title = project.getTitle();
        String description = project.getDescription();

        // 1. 전체 feature 리스트 조회 (주 기능만 가져오고, 자식 기능은 내부에서 조립)
        List<FeatureItem> mainFeatures = featureItemRepository.findMainFeaturesByProjectId(projectId);

        List<FeatureSaveContent> specs = mainFeatures.stream()
                .map(main -> {
                    List<FeatureItem> subs = featureItemRepository.findByParentFeature(main);
                    return FeatureSaveContent.builder()
                            .mainFeature(toFeatureSaveItem(main))
                            .subFeature(subs.stream().map(this::toFeatureSaveItem).toList())
                            .build();
                }).toList();

        return generatePdf(title, description, specs);
    }

    private FeatureSaveItem toFeatureSaveItem(FeatureItem item) {
        return FeatureSaveItem.builder()
                .id(item.getFeatureId())
                .title(item.getTitle())
                .description(item.getDescription())
                .estimatedTime(item.getEstimatedTime())
                .priorityLevel(item.getPriorityLevel())
                .build();
    }

    private byte[] generatePdf(String title, String description, List<FeatureSaveContent> specs) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            document.open();

            // 제목
            Paragraph docTitle = new Paragraph("프로젝트 명세서", fontTitle);
            docTitle.setSpacingAfter(20);
            document.add(docTitle);

            // 프로젝트 정보
            Paragraph projTitle = new Paragraph("제목: " + title, fontBold);
            Paragraph projDesc  = new Paragraph("설명: " + (description != null ? description : "없음"), font);
            projDesc.setIndentationLeft(5);
            projDesc.setSpacingAfter(15);
            document.add(projTitle);
            document.add(projDesc);

            // 기능 출력
            for (FeatureSaveContent spec : specs) {
                FeatureSaveItem main = spec.getMainFeature();
                String mainTime = main.getEstimatedTime() + "h";
                String mainPriority = main.getPriorityLevel() != null ? String.valueOf(main.getPriorityLevel()) : "-";

                Paragraph mainTitle = new Paragraph("주 기능: " + main.getTitle() + " (" + mainTime + ")", fontSection);
                mainTitle.setSpacingBefore(10);
                mainTitle.setSpacingAfter(4);
                document.add(mainTitle);

                if (main.getDescription() != null && !main.getDescription().isBlank()) {
                    Paragraph desc = new Paragraph("설명: " + main.getDescription(), font);
                    desc.setIndentationLeft(10);
                    desc.setSpacingAfter(5);
                    document.add(desc);
                }

                for (FeatureSaveItem sub : spec.getSubFeature()) {
                    String subTime = sub.getEstimatedTime() + "h";
                    String subPriority = sub.getPriorityLevel() != null ? String.valueOf(sub.getPriorityLevel()) : "-";

                    Paragraph subTitle = new Paragraph(
                            "• 하위 기능: " + sub.getTitle() + " (" + subTime + ", 우선순위 " + subPriority + ")", font);
                    subTitle.setIndentationLeft(20);
                    document.add(subTitle);

                    if (sub.getDescription() != null && !sub.getDescription().isBlank()) {
                        Paragraph subDesc = new Paragraph(" - 설명: " + sub.getDescription(), fontSmall);
                        subDesc.setIndentationLeft(30);
                        document.add(subDesc);
                    }
                }

                document.add(Chunk.NEWLINE);
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("PDF 생성 중 오류 발생", e);
            throw new RuntimeException("PDF 생성 실패", e);
        }
    }
}

