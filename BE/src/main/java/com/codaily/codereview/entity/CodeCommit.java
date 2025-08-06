package com.codaily.codereview.entity;

import com.codaily.project.entity.Project;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CodeCommit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commitId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    private String commitHash;
    private String author;
    private String message;

    @ElementCollection
    @CollectionTable(
            name = "code_commit_feature",
            joinColumns = @JoinColumn(name = "commit_id")
    )

    @Column(name = "feature_name")
    private List<String> featureNames = new ArrayList<>();

    private LocalDateTime fieldCommittedAt;

    public void addFeatureName(String featureName) {
        this.featureNames.add(featureName);
    }
}


//package com.codaily.codereview.entity;
//
//import com.codaily.project.entity.Project;
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.CreationTimestamp;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "code_commits")
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@AllArgsConstructor
//@Builder
//@Getter
//public class CodeCommit {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long commitId;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "project_id")
//    private Project project;
//
//    @Column(name = "commit_hash", nullable = false)
//    private String commitHash;
//
//    @Column
//    private String author;
//
//    @Column
//    private String message;
//
//    @Column
//    private String featureName;
//
//    @CreationTimestamp
//    @Column(name = "FieldCommited_at")
//    private LocalDateTime fieldCommitedAt;
//
//    public static CodeCommit of(Project project, String commitHash, String author, String message, String featureName) {
//        return CodeCommit.builder()
//                .project(project)
//                .commitHash(commitHash)
//                .author(author)
//                .message(message)
//                .featureName(featureName)
//                .build();
//    }
//
//}
