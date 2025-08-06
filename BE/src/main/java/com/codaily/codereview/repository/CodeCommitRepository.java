package com.codaily.codereview.repository;

import com.codaily.codereview.entity.CodeCommit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeCommitRepository extends JpaRepository<CodeCommit, Long> {

}
