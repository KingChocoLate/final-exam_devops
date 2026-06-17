package net.orderzone.idcard.service;

import lombok.RequiredArgsConstructor;
import net.orderzone.idcard.model.*;
import net.orderzone.idcard.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final PhotoStorageService photoStorageService;
    private final QRCodeService qrCodeService;
    private final BarcodeService barcodeService;

    public List<Profile> getAllProfiles() {
        return profileRepository.findAll();
    }

    public Optional<Profile> getById(Long id) {
        return profileRepository.findById(id);
    }

    public Optional<Profile> getByUuid(String uuid) {
        return profileRepository.findByUuid(uuid);
    }

    public List<Profile> getByType(ProfileType type) {
        return profileRepository.findByType(type);
    }

    public List<Profile> searchByName(String name) {
        return profileRepository.findByFullNameContainingIgnoreCase(name);
    }

    public Profile create(Profile profile, MultipartFile photo) throws Exception {
        // UUID
        profile.setUuid(UUID.randomUUID().toString());

        // Registration number: YEAR-DEPT-###
        profile.setRegistrationNumber(generateRegNumber(profile));

        // Photo
        if (photo != null && !photo.isEmpty()) {
            String fileName = photoStorageService.store(photo);
            profile.setPhotoFileName(fileName);
            profile.setPhotoContentType(photo.getContentType());
        }

        return profileRepository.save(profile);
    }

    public Profile update(Long id, Profile updated, MultipartFile photo) throws Exception {
        Profile existing = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        existing.setFullName(updated.getFullName());
        existing.setDepartment(updated.getDepartment());
        existing.setTitle(updated.getTitle());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setBloodGroup(updated.getBloodGroup());
        existing.setDateOfBirth(updated.getDateOfBirth());
        existing.setExpiryDate(updated.getExpiryDate());
        existing.setType(updated.getType());
        existing.setTemplate(updated.getTemplate());
        existing.setBarcodeType(updated.getBarcodeType());

        if (photo != null && !photo.isEmpty()) {
            // Delete old photo
            if (existing.getPhotoFileName() != null) {
                photoStorageService.delete(existing.getPhotoFileName());
            }
            String fileName = photoStorageService.store(photo);
            existing.setPhotoFileName(fileName);
            existing.setPhotoContentType(photo.getContentType());
        }

        return profileRepository.save(existing);
    }

    public void delete(Long id) throws Exception {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        if (profile.getPhotoFileName() != null) {
            photoStorageService.delete(profile.getPhotoFileName());
        }
        profileRepository.deleteById(id);
    }

    public byte[] getQRCode(Profile profile) throws Exception {
        String verifyUrl = "http://localhost:8080/verify/" + profile.getUuid();
        return qrCodeService.generateQRCode(verifyUrl, 200, 200);
    }

    public byte[] getBarcode(Profile profile) throws Exception {
        return barcodeService.generateBarcode(
                profile.getRegistrationNumber(),
                profile.getBarcodeType() != null ? profile.getBarcodeType() : BarcodeType.CODE_128);
    }

    // Batch create
    public List<Profile> batchCreate(List<Profile> profiles) throws Exception {
        List<Profile> saved = new java.util.ArrayList<>();
        for (Profile p : profiles) {
            saved.add(create(p, null));
        }
        return saved;
    }

    private String generateRegNumber(Profile profile) {
        String year = String.valueOf(Year.now().getValue());
        String dept = (profile.getDepartment() != null && !profile.getDepartment().isBlank())
                ? profile.getDepartment().substring(0, Math.min(3, profile.getDepartment().length())).toUpperCase()
                : "GEN";
        long count = profileRepository.count() + 1;
        return String.format("%s-%s-%03d", year, dept, count);
    }
}