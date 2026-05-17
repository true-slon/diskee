package com.diskee.diskee_project.controller;

import com.diskee.diskee_project.dto.TrashItemDTO;
import com.diskee.diskee_project.sdk.data.TrashBinEntity;
import com.diskee.diskee_project.sdk.service.TrashService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trash")
@RequiredArgsConstructor
public class TrashController {

    private final TrashService trashService;

   
   @GetMapping
    public ResponseEntity<List<TrashItemDTO>> getTrash() {
        return ResponseEntity.ok(trashService.getTrashContents());
    }

   
    @PostMapping("/restore/{trashId}")
    public ResponseEntity<Map<String, String>> restore(@PathVariable Long trashId) {
        trashService.restoreFromTrash(trashId);
        return ResponseEntity.ok(Map.of("message", "Элемент восстановлен"));
    }

   
    @DeleteMapping("/permanent/{trashId}")
    public ResponseEntity<Map<String, String>> permanentDelete(@PathVariable Long trashId) {
        trashService.permanentDelete(trashId);
        return ResponseEntity.ok(Map.of("message", "Элемент удалён навсегда"));
    }

    
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearTrash() {
        trashService.clearTrash();
        return ResponseEntity.ok(Map.of("message", "Корзина очищена"));
    }
}