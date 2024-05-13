package fr.eseo.tauri.controller;

import fr.eseo.tauri.model.ValidationFlag;
import fr.eseo.tauri.service.ValidationFlagService;
import fr.eseo.tauri.util.CustomLogger;
import fr.eseo.tauri.util.ResponseMessage;
import fr.eseo.tauri.util.valid.Update;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/flags/{flagId}/validation")
@Tag(name = "validationFlags")
public class ValidationFlagController {

    private final ValidationFlagService validationFlagService;
    private final ResponseMessage responseMessage = new ResponseMessage("validationFlag");

    @GetMapping("/{authorId}")
    public ResponseEntity<ValidationFlag> getValidationFlagByAuthorId(@RequestHeader("Authorization") String token, @PathVariable Integer authorId, @PathVariable Integer flagId) {
        ValidationFlag validationFlag = validationFlagService.getValidationFlagByAuthorId(token, flagId, authorId);
        return ResponseEntity.ok(validationFlag);
    }

    @GetMapping
    public ResponseEntity<List<ValidationFlag>> getAllValidationFlags(@RequestHeader("Authorization") String token, @PathVariable Integer flagId) {
        List<ValidationFlag> validationFlags = validationFlagService.getAllValidationFlags(token, flagId);
        return ResponseEntity.ok(validationFlags);
    }

    @PatchMapping("/{authorId}")
    public ResponseEntity<String> updateValidationFlag(@RequestHeader("Authorization") String token, @PathVariable Integer authorId, @PathVariable Integer flagId, @Validated(Update.class) @RequestBody ValidationFlag updatedValidationFlag) {
        validationFlagService.updateValidationFlag(token, flagId, authorId, updatedValidationFlag);
        CustomLogger.info(responseMessage.update());
        return ResponseEntity.ok(responseMessage.update());
    }

}