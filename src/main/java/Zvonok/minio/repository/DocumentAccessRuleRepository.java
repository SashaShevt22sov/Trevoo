package Zvonok.minio.repository;

import Zvonok.minio.entity.AccessRule;
import Zvonok.minio.entity.DocumentAccessRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DocumentAccessRuleRepository extends JpaRepository<DocumentAccessRule, UUID> {
    Optional<DocumentAccessRule> findDocumentAccessRuleByAccessibilityRule(AccessRule accessibilityRule);
}
