package Zvonok.minio.repository;

import Zvonok.minio.entity.Document;
import Zvonok.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    Optional<Document> findByIdAndOwner(UUID id, User user);
}
