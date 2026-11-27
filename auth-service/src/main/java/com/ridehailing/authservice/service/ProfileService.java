package com.ridehailing.authservice.service;

import com.ridehailing.authservice.dto.ProfileResponse;
import com.ridehailing.authservice.dto.UpdateProfileRequest;
import com.ridehailing.authservice.entity.User;
import com.ridehailing.authservice.exception.InvalidAvatarException;
import com.ridehailing.authservice.exception.UserNotFoundException;
import com.ridehailing.authservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class ProfileService {

    private static final long MAX_AVATAR_BYTES = 5L * 1024 * 1024;

    private static final Map<String, MediaType> EXTENSION_TYPES = Map.of(
            "jpg", MediaType.IMAGE_JPEG,
            "jpeg", MediaType.IMAGE_JPEG,
            "png", MediaType.IMAGE_PNG,
            "webp", MediaType.parseMediaType("image/webp")
    );

    private final UserRepository userRepository;
    private final Path uploadDir;

    public ProfileService(
            UserRepository userRepository,
            @Value("${app.upload-dir:uploads}") String uploadDir) throws IOException {
        this.userRepository = userRepository;
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadDir);
    }

    public ProfileResponse getProfile(String email) {
        return ProfileResponse.from(findByEmail(email));
    }

    @Transactional
    public ProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = findByEmail(email);
        user.setName(request.getName().trim());
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().isBlank() ? null : request.getPhone().trim());
        }
        return ProfileResponse.from(userRepository.save(user));
    }

    @Transactional
    public ProfileResponse saveAvatar(String email, MultipartFile file) {
        User user = findByEmail(email);

        if (file == null || file.isEmpty()) {
            throw new InvalidAvatarException("Avatar file is required");
        }
        if (file.getSize() > MAX_AVATAR_BYTES) {
            throw new InvalidAvatarException("Avatar must be 5 MB or smaller");
        }

        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        String ext = original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase()
                : "";
        MediaType expected = EXTENSION_TYPES.get(ext);
        if (expected == null) {
            throw new InvalidAvatarException("Only JPG, PNG or WEBP images are allowed");
        }

        String detectedExt = sniffImageType(file);
        if (detectedExt == null || !EXTENSION_TYPES.get(detectedExt).equals(expected)) {
            throw new InvalidAvatarException("File content does not match a supported image type");
        }

        String filename = user.getId() + "." + detectedExt;
        deleteExistingVariants(user.getId(), filename);

        Path target = uploadDir.resolve(filename).normalize();
        if (!target.startsWith(uploadDir)) {
            throw new InvalidAvatarException("Invalid avatar filename");
        }
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new InvalidAvatarException("Could not store the avatar");
        }

        user.setAvatarFilename(filename);
        return ProfileResponse.from(userRepository.save(user));
    }

    public AvatarResource loadAvatar(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (user.getAvatarFilename() == null) {
            throw new UserNotFoundException("No avatar set");
        }
        Path path = uploadDir.resolve(user.getAvatarFilename()).normalize();
        if (!path.startsWith(uploadDir) || !Files.exists(path)) {
            throw new UserNotFoundException("No avatar set");
        }
        String ext = user.getAvatarFilename()
                .substring(user.getAvatarFilename().lastIndexOf('.') + 1).toLowerCase();
        return new AvatarResource(new FileSystemResource(path), EXTENSION_TYPES.get(ext));
    }

    private void deleteExistingVariants(UUID userId, String keepFilename) {
        for (String ext : EXTENSION_TYPES.keySet()) {
            String candidate = userId + "." + ext;
            if (!candidate.equals(keepFilename)) {
                try {
                    Files.deleteIfExists(uploadDir.resolve(candidate));
                } catch (IOException e) {
                    log.warn("Could not delete old avatar variant {}", candidate);
                }
            }
        }
    }

    private String sniffImageType(MultipartFile file) {
        byte[] header = new byte[12];
        try (InputStream in = file.getInputStream()) {
            int read = in.read(header);
            if (read < 4) {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
        if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8) {
            return "jpg";
        }
        if ((header[0] & 0xFF) == 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) {
            return "png";
        }
        if (header[0] == 0x52 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x46
                && header[8] == 0x57 && header[9] == 0x45 && header[10] == 0x42 && header[11] == 0x50) {
            return "webp";
        }
        return null;
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public record AvatarResource(Resource resource, MediaType mediaType) {
    }
}
