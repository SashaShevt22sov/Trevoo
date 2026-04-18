package Zvonok.infrastructure.storage.repository;

import Zvonok.infrastructure.storage.entity.AccessRule;
import Zvonok.infrastructure.storage.entity.DocumentAccessRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DocumentAccessRuleRepository extends JpaRepository<DocumentAccessRule, UUID> {
    Optional<DocumentAccessRule> findDocumentAccessRuleByAccessibilityRule(AccessRule accessibilityRule);
}
