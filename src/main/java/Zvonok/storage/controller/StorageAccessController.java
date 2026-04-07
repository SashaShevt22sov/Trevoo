package Zvonok.storage.controller;

import Zvonok.storage.dto.UpdateAccessDocumentRequestDto;
import Zvonok.storage.service.StorageService;
import Zvonok.auth.userDetails.MyUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/access-storage")
@RequiredArgsConstructor
@Log4j2
public class StorageAccessController {

    private final StorageService storageService;

    @GetMapping("/{documentId}")
    public ResponseEntity<List<Long>> getAccessUserForDocument(
            @PathVariable UUID documentId,
            @AuthenticationPrincipal MyUserDetails currentUser
            ){
        return ResponseEntity.ok(storageService.getAccessByDocumentUserIds(documentId, currentUser));
    }

    @PutMapping("/{documentId}")
    public ResponseEntity<?> setAccessUserForDocument(
            @PathVariable UUID documentId,
            @RequestBody UpdateAccessDocumentRequestDto request,
            @AuthenticationPrincipal MyUserDetails currentUser
    ){
        storageService.updateDocumentAccessRule(documentId,request.getRule(),request.getUserIds(),currentUser);
        return ResponseEntity.ok("Доступ успешно обновлён");
    }
}
