package net.orderzone.idcard.controller;

import lombok.RequiredArgsConstructor;
import net.orderzone.idcard.model.*;
import net.orderzone.idcard.service.*;
import net.orderzone.idcard.repository.TemplateRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final TemplateRepository templateRepository;
    private final PhotoStorageService photoStorageService;

    @GetMapping
    public String list(Model model,
                       @RequestParam(required = false) String search,
                       @RequestParam(required = false) ProfileType type) {
        List<Profile> profiles;
        if (search != null && !search.isBlank())
            profiles = profileService.searchByName(search);
        else if (type != null)
            profiles = profileService.getByType(type);
        else
            profiles = profileService.getAllProfiles();

        model.addAttribute("profiles", profiles);
        model.addAttribute("profileTypes", ProfileType.values());
        return "profile/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("profile", new Profile());
        model.addAttribute("profileTypes", ProfileType.values());
        model.addAttribute("barcodeTypes", BarcodeType.values());
        model.addAttribute("templates", templateRepository.findAll());
        return "profile/form";
    }

    @PostMapping
    public String create(@ModelAttribute Profile profile,
                         @RequestParam(value = "photo", required = false) MultipartFile photo,
                         @RequestParam(value = "templateId", required = false) Long templateId) {
        try {
            if (templateId != null) {
                templateRepository.findById(templateId).ifPresent(profile::setTemplate);
            }
            profileService.create(profile, photo);
            return "redirect:/profiles";
        } catch (Exception e) {
            return "redirect:/profiles/new?error=" + e.getMessage();
        }
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        Profile p = profileService.getById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
        model.addAttribute("profile", p);
        return "profile/view";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Profile p = profileService.getById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
        model.addAttribute("profile", p);
        model.addAttribute("profileTypes", ProfileType.values());
        model.addAttribute("barcodeTypes", BarcodeType.values());
        model.addAttribute("templates", templateRepository.findAll());
        return "profile/form";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id,
                         @ModelAttribute Profile profile,
                         @RequestParam(value = "photo", required = false) MultipartFile photo,
                         @RequestParam(value = "templateId", required = false) Long templateId) {
        try {
            if (templateId != null) {
                templateRepository.findById(templateId).ifPresent(profile::setTemplate);
            }
            profileService.update(id, profile, photo);
            return "redirect:/profiles/" + id;
        } catch (Exception e) {
            return "redirect:/profiles/" + id + "/edit?error=" + e.getMessage();
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) throws Exception {
        profileService.delete(id);
        return "redirect:/profiles";
    }

    // Serve photo file
    @GetMapping("/photo/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> photo(@PathVariable Long id) throws Exception {
        Profile p = profileService.getById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
        if (!p.hasPhoto()) return ResponseEntity.notFound().build();
        byte[] bytes = photoStorageService.load(p.getPhotoFileName());
        return ResponseEntity.ok()
                .header("Content-Type", p.getPhotoContentType())
                .body(bytes);
    }

    // Serve QR
    @GetMapping("/qr/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> qr(@PathVariable Long id) throws Exception {
        Profile p = profileService.getById(id).orElseThrow();
        return ResponseEntity.ok()
                .header("Content-Type", "image/png")
                .body(profileService.getQRCode(p));
    }

    // Serve Barcode
    @GetMapping("/barcode/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> barcode(@PathVariable Long id) throws Exception {
        Profile p = profileService.getById(id).orElseThrow();
        return ResponseEntity.ok()
                .header("Content-Type", "image/png")
                .body(profileService.getBarcode(p));
    }

    // Verify by UUID (QR scan landing)
    @GetMapping("/verify/{uuid}")
    public String verify(@PathVariable String uuid, Model model) {
        Profile p = profileService.getByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Invalid or expired ID"));
        model.addAttribute("profile", p);
        return "profile/view";
    }
}