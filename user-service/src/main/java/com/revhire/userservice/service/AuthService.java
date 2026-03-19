package com.revhire.userservice.service;

import com.revhire.userservice.dto.request.UserRegistrationRequest;
import com.revhire.userservice.model.*;
import com.revhire.userservice.repository.*;
import com.revhire.userservice.client.AuditLogClient;
import com.revhire.userservice.client.NotificationClient;
import com.revhire.userservice.dto.external.AuditLogRequest;
import com.revhire.userservice.dto.external.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogClient auditLogClient;
    private final NotificationClient notificationClient;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final OtpVerificationRepository otpVerificationRepository;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Transactional
    public void generateAndSendOtp(String email) {
        log.info("Generating OTP for: {}", email);
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already in use");
        }

        String otp = String.format("%06d", new Random().nextInt(999999));
        OtpVerification otpVerification = otpVerificationRepository.findByEmail(email)
                .map(existing -> {
                    existing.setOtp(otp);
                    existing.setExpiryDate(LocalDateTime.now().plusMinutes(5));
                    return existing;
                })
                .orElseGet(() -> new OtpVerification(email, otp, 5));

        otpVerificationRepository.save(otpVerification);
        
        NotificationRequest request = new NotificationRequest();
        request.setUserEmail(email);
        request.setMessage("Your OTP for RevHire registration is: " + otp);
        request.setSendEmail(true);
        request.setSubject("RevHire - OTP Verification");
        request.setEmailBody("Hello,\n\nYour OTP for registration is " + otp + ". It expires in 5 minutes.\n\nRegards,\nRevHire Team");
        
        try {
            notificationClient.createNotification(request);
        } catch (Exception e) {
            log.error("Failed to send OTP notification: {}", e.getMessage());
        }
    }

    public boolean verifyOtp(String email, String otp) {
        log.info("Verifying OTP for: {}", email);
        return otpVerificationRepository.findByEmail(email)
                .map(v -> v.getOtp().equals(otp) && v.getExpiryDate().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    @Transactional
    public void initiatePasswordReset(String email) {
        log.info("Initiating password reset for: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        String token = String.format("%06d", new Random().nextInt(999999));
        PasswordResetToken resetToken = passwordResetTokenRepository.findByUser(user)
                .map(existing -> {
                    existing.setToken(token);
                    existing.setExpiryDate(LocalDateTime.now().plusMinutes(5));
                    return existing;
                })
                .orElseGet(() -> new PasswordResetToken(token, user, 5));

        passwordResetTokenRepository.save(resetToken);

        NotificationRequest request = new NotificationRequest();
        request.setUserId(user.getId());
        request.setUserEmail(user.getEmail());
        request.setMessage("Your OTP for RevHire password reset is: " + token);
        request.setSendEmail(true);
        request.setSubject("RevHire - Password Reset");
        request.setEmailBody("Hello " + user.getName() + ",\n\nYour OTP for password reset is " + token + ". It expires in 5 minutes.\n\nRegards,\nRevHire Team");

        try {
            notificationClient.createNotification(request);
            log.info("Password reset OTP sent for {}", email);
        } catch (Exception ex) {
            log.error("Password reset notification failed for {}", email, ex);
            throw new RuntimeException("Failed to send reset notification", ex);
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        log.info("Resetting password with token");
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid password reset token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Password reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
        log.info("Password reset successfully for user: {}", user.getEmail());

        logAction("User", user.getId(), "PASSWORD_RESET", "Password reset via token", user.getId());
    }

    @Transactional
    public void updatePassword(User user, String oldPassword, String newPassword) {
        log.info("Updating password for user: {}", user.getEmail());
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid old password");
        }
        if (oldPassword.equals(newPassword)) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password updated successfully for user: {}", user.getEmail());

        logAction("User", user.getId(), "PASSWORD_UPDATED", "Password updated manually", user.getId());
    }

    @Transactional
    public User registerUser(UserRegistrationRequest registrationDto) {
        log.info("Attempting to register user with email: {}", registrationDto.getEmail());
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setName(registrationDto.getName());
        user.setEmail(registrationDto.getEmail());
        user.setPhone(registrationDto.getPhone());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setRole(registrationDto.getRole());

        User savedUser = userRepository.save(user);

        if (savedUser.getRole() == User.Role.JOB_SEEKER) {
            JobSeekerProfile profile = new JobSeekerProfile();
            profile.setUser(savedUser);
            profile.setLocation(registrationDto.getLocation());
            profile.setEmploymentStatus(registrationDto.getEmploymentStatus());
            jobSeekerProfileRepository.save(profile);
        } else if (savedUser.getRole() == User.Role.EMPLOYER) {
            EmployerProfile profile = new EmployerProfile();
            profile.setUser(savedUser);
            profile.setCompanyId(registrationDto.getCompanyId());
            profile.setCompanyName(registrationDto.getCompanyName());
            profile.setDesignation("HR / Admin");
            employerProfileRepository.save(profile);
        }

        logAction("User", savedUser.getId(), "USER_REGISTERED", "Role: " + savedUser.getRole().name(), savedUser.getId());

        NotificationRequest request = new NotificationRequest();
        request.setUserId(savedUser.getId());
        request.setUserEmail(savedUser.getEmail());
        request.setMessage("Welcome to RevHire, " + savedUser.getName() + "!");
        request.setSendEmail(true);
        request.setSubject("Welcome to RevHire");
        request.setEmailBody("Hello " + savedUser.getName() + ",\n\nThank you for joining RevHire. Your account has been created successfully.\n\nRegards,\nRevHire Team");

        try {
            notificationClient.createNotification(request);
        } catch (Exception e) {
            log.error("Failed to send welcome notification to {}", savedUser.getEmail(), e);
        }

        return savedUser;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void logAction(String entity, Long entityId, String action, String description, Long userId) {
        AuditLogRequest req = new AuditLogRequest();
        req.setEntityName(entity);
        req.setEntityId(entityId);
        req.setAction(action);
        req.setChangeDescription(description);
        req.setChangedById(userId);
        try {
            auditLogClient.createAuditLog(req);
        } catch (Exception e) {
            log.error("Failed to log audit action: {}", e.getMessage());
        }
    }
}

