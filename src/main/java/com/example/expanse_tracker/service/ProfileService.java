package com.example.expanse_tracker.service;

import com.example.expanse_tracker.dto.AuthDTO;
import com.example.expanse_tracker.dto.ProfileDTO;
import com.example.expanse_tracker.entity.ProfileEntity;
import com.example.expanse_tracker.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${app.activation.url}")
    private String activationUrl;

    public ProfileDTO registerProfile(ProfileDTO profileDTO) {
        ProfileEntity newProfile = toEntity(profileDTO);
        newProfile.setActivationToken(UUID.randomUUID().toString());
        newProfile = profileRepository.save(newProfile);

        // Send activation email logic
        String activationLink = activationUrl+"/activate?token=" + newProfile.getActivationToken();
        String subject = "Activate your account";
        String body = "Click the following link to activate your account: " + activationLink;
        emailService.sendActivationEmail(
                newProfile.getEmail(),
                newProfile.getFullName(),
                activationLink
        );
//         emailService.sendEmail(newProfile.getEmail(), subject, body);
        return toDTO(newProfile); // Placeholder return
    }

    public ProfileEntity toEntity(ProfileDTO profileDTO) {
        return ProfileEntity.builder()
                .id(profileDTO.getId())
                .fullName(profileDTO.getFullName())
                .email(profileDTO.getEmail())
                .password(passwordEncoder.encode(profileDTO.getPassword()))
                .profileImageUrl(profileDTO.getProfileImageUrl())
                .createdAt(profileDTO.getCreatedAt())
                .updatedAt(profileDTO.getUpdatedAt())
                .build(); // Placeholder return
    }

    public ProfileDTO toDTO(ProfileEntity profileEntity) {
        return ProfileDTO.builder()
                .id(profileEntity.getId())
                .fullName(profileEntity.getFullName())
                .email(profileEntity.getEmail())
                .profileImageUrl(profileEntity.getProfileImageUrl())
                .createdAt(profileEntity.getCreatedAt())
                .updatedAt(profileEntity.getUpdatedAt())
                .build(); // Placeholder return
    }

    public boolean activateProfile(String token){
        return profileRepository.findByActivationToken(token)
                .map(profile -> {
                    profile.setIsActive(true);
                profileRepository.save(profile);
                return true;
        }).orElse(false);
    }

    public boolean isAccountActive(String email){
        return profileRepository.findByEmail(email)
                .map(ProfileEntity::getIsActive)
                .orElse(false);
    }

    public ProfileEntity getCurrentProfile()
    {
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        String email=authentication.getName();
        return profileRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("User not found with email: " + email));
    }

    public ProfileDTO getPublicProfile(String email){
        ProfileEntity currentUser= null;
        if(email==null)
        {
            currentUser=getCurrentProfile();
        }
        else {
            currentUser= profileRepository.findByEmail(email)
                    .orElseThrow(()-> new UsernameNotFoundException("User not found with email: " + email));
        }
        return ProfileDTO.builder()
                .id(currentUser.getId())
                .fullName(currentUser.getFullName())
                .email(currentUser.getEmail())
                .profileImageUrl(currentUser.getProfileImageUrl())
                .createdAt(currentUser.getCreatedAt())
                .updatedAt(currentUser.getUpdatedAt())
                .build();
    }

    public Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO) {
        String token=jwtService.generateToken(authDTO.getEmail());
        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authDTO.getEmail(),authDTO.getPassword()));
            return Map.of(
                    "token",token,
                    "user", getPublicProfile(authDTO.getEmail())
            );
        }catch (Exception e){
            throw new RuntimeException("Invalid email or password.");
        }
    }
}
