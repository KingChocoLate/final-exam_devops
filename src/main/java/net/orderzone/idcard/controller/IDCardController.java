package net.orderzone.idcard.controller;

import lombok.RequiredArgsConstructor;
import net.orderzone.idcard.model.Profile;
import net.orderzone.idcard.model.ProfileType;
import net.orderzone.idcard.service.PDFExportService;
import net.orderzone.idcard.service.ProfileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/idcard")
@RequiredArgsConstructor
public class IDCardController {

    private final ProfileService profileService;
    private final PDFExportService pdfExportService;

    @GetMapping("/preview/{id}")
    public String preview(@PathVariable Long id, Model model) {
        Profile profile = profileService.getById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        model.addAttribute("profile", profile);
        return "idcard/preview";
    }

    @GetMapping("/export/pdf/{id}")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long id) throws Exception {
        Profile profile = profileService.getById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        byte[] pdf = pdfExportService.exportSingle(profile);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"idcard-" + profile.getRegistrationNumber() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/export/batch")
    public ResponseEntity<byte[]> exportBatch(
            @RequestParam(required = false) String type) throws Exception {
        List<Profile> profiles;
        if (type != null && !type.isBlank())
            profiles = profileService.getByType(ProfileType.valueOf(type));
        else
            profiles = profileService.getAllProfiles();
        byte[] pdf = pdfExportService.exportBatch(profiles);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"batch-idcards.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}