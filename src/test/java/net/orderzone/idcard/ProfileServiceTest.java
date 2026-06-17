package net.orderzone.idcard;

import net.orderzone.idcard.model.Profile;
import net.orderzone.idcard.model.ProfileType;
import net.orderzone.idcard.repository.ProfileRepository;
import net.orderzone.idcard.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfileServiceTest {

    @Mock ProfileRepository profileRepository;
    @Mock PhotoStorageService photoStorageService;
    @Mock QRCodeService qrCodeService;
    @Mock BarcodeService barcodeService;

    @InjectMocks ProfileService profileService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllProfiles() {
        when(profileRepository.findAll()).thenReturn(List.of(new Profile()));
        assertEquals(1, profileService.getAllProfiles().size());
    }

    @Test
    void testGetProfileById_NotFound() {
        when(profileRepository.findById(99L)).thenReturn(Optional.empty());
        assertTrue(profileService.getById(99L).isEmpty());
    }

    @Test
    void testGetProfilesByType() {
        Profile p = Profile.builder()
                .fullName("John")
                .type(ProfileType.STUDENT)
                .build();
        when(profileRepository.findByType(ProfileType.STUDENT)).thenReturn(List.of(p));
        List<Profile> result = profileService.getByType(ProfileType.STUDENT);
        assertEquals(1, result.size());
        assertEquals(ProfileType.STUDENT, result.get(0).getType());
    }

    @Test
    void testGetByUuid_NotFound() {
        when(profileRepository.findByUuid("fake-uuid")).thenReturn(Optional.empty());
        assertTrue(profileService.getByUuid("fake-uuid").isEmpty());
    }
}