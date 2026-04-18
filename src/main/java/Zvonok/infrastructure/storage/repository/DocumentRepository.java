package Zvonok.infrastructure.storage.repository;

import Zvonok.infrastructure.storage.entity.Document;
import Zvonok.features.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    Optional<Document> findByIdAndOwner(UUID id, User user);

    List<Document> findDocumentsByOwnerIdAndDocumentNameLikeIgnoreCase(Long ownerId, String documentName);
}
