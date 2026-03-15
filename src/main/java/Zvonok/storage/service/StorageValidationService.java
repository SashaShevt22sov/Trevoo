package Zvonok.storage.service;

import Zvonok.common.exception.customException.storageException.StorageAccessDeniedException;
import Zvonok.storage.entity.Document;
import Zvonok.user.entity.User;
import Zvonok.userDetails.MyUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class StorageValidationService {

    public void validateUpdateRule(Document document, MyUserDetails currentUser) {
        if (!document.getOwner().getId().equals(currentUser.getId())) {
            throw new StorageAccessDeniedException();
        }
    }

    public void validatePreviewAccessDocument(Document document, MyUserDetails currentUser) {
        var allowedUser = document.getAllowedUsers();
        var user = currentUser == null ? null : currentUser.getUser();
        var owner = document.getOwner();
        switch (document.getAccessRule().getAccessibilityRule()) {
            case PRIVATE -> {
                if (user == null) {
                    throw new StorageAccessDeniedException();
                }
                if (!owner.getId().equals(user.getId())) {
                    throw new StorageAccessDeniedException();
                }
            }
            case PERSONAL -> {
                if (user == null) {
                    throw new StorageAccessDeniedException();
                }
                if (!owner.getId().equals(user.getId()) && !allowedUser.contains(user)) {
                    throw new StorageAccessDeniedException();
                }
            }
        }
    }

    public void validateDeleteAccessDocumet(Document document, MyUserDetails currentUser) {
        if (!document.getOwner().getId().equals(currentUser.getId())) {
            throw new StorageAccessDeniedException();
        }
    }
}
