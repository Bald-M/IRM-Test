package nz.ac.wintec.irm.service;

import nz.ac.wintec.irm.domain.ApplicationUser;
import nz.ac.wintec.irm.domain.AuthenticationToken;
import nz.ac.wintec.irm.domain.Role;
import nz.ac.wintec.irm.domain.UserStatus;
import nz.ac.wintec.irm.domain.UserVerification;
import nz.ac.wintec.irm.domain.VerificationPurpose;
import nz.ac.wintec.irm.mail.MailPort;
import nz.ac.wintec.irm.repository.ApplicationUserRepository;
import nz.ac.wintec.irm.repository.AuthenticationTokenRepository;
import nz.ac.wintec.irm.repository.UserVerificationRepository;
import nz.ac.wintec.irm.security.AuthenticatedUser;
import nz.ac.wintec.irm.security.JwtService;
import nz.ac.wintec.irm.security.PasswordResetReferenceService;
import nz.ac.wintec.irm.security.TokenDigest;
import nz.ac.wintec.irm.web.ApiException;
import nz.ac.wintec.irm.web.dto.ForgotPasswordChangeRequest;
import nz.ac.wintec.irm.web.dto.ForgotPasswordVerifyRequest;
import nz.ac.wintec.irm.web.dto.LoginRequest;
import nz.ac.wintec.irm.web.dto.RegistrationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    private static final java.time.Duration OTP_LIFETIME = java.time.Duration.ofMinutes(30);
    private static final java.time.Duration OTP_RESEND_COOLDOWN = java.time.Duration.ofSeconds(60);
    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final int NONEXISTENT_USER_ID = Integer.MIN_VALUE;
    private static final int BCRYPT_INPUT_LIMIT_BYTES = 72;
    private static final String BCRYPT_SHA256_PREFIX = "{bcrypt-sha256}";

    private final ApplicationUserRepository userRepository;
    private final UserVerificationRepository verificationRepository;
    private final AuthenticationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpGenerator otpGenerator;
    private final MailPort mailPort;
    private final JwtService jwtService;
    private final PasswordResetReferenceService resetReferenceService;
    private final Clock clock;
    private final TokenDigest tokenDigest;
    private final PasswordResetTimingGuard passwordResetTimingGuard;
    private final TransactionTemplate passwordResetTransaction;
    private final TaskExecutor passwordResetDeliveryExecutor;

    public AuthService(ApplicationUserRepository userRepository,
                       UserVerificationRepository verificationRepository,
                       AuthenticationTokenRepository tokenRepository,
                       PasswordEncoder passwordEncoder,
                       OtpGenerator otpGenerator,
                       MailPort mailPort,
                       JwtService jwtService,
                       PasswordResetReferenceService resetReferenceService,
                       Clock clock,
                       TokenDigest tokenDigest,
                       PasswordResetTimingGuard passwordResetTimingGuard,
                       PlatformTransactionManager transactionManager,
                       @Qualifier("applicationTaskExecutor") TaskExecutor passwordResetDeliveryExecutor) {
        this.userRepository = userRepository;
        this.verificationRepository = verificationRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpGenerator = otpGenerator;
        this.mailPort = mailPort;
        this.jwtService = jwtService;
        this.resetReferenceService = resetReferenceService;
        this.clock = clock;
        this.tokenDigest = tokenDigest;
        this.passwordResetTimingGuard = passwordResetTimingGuard;
        this.passwordResetTransaction = new TransactionTemplate(transactionManager);
        this.passwordResetDeliveryExecutor = passwordResetDeliveryExecutor;
    }

    @Transactional
    public RegistrationResult register(RegistrationRequest request) {
        String email = normalizeEmail(request.email());
        if (email.substring(0, email.indexOf('@')).length() > 40) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Your email address length is not legal");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Email already registered, Please use another email or Sign In");
        }

        Role role;
        try {
            role = Role.fromRegistration(request.type());
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Unsupported user role");
        }
        validateRegistrationRole(email, role);

        String username = email.substring(0, email.indexOf('@'));
        ApplicationUser user = userRepository.save(new ApplicationUser(
                username,
                email,
                encodePassword(request.password()),
                role,
                UserStatus.PENDING,
                now()
        ));

        UserVerification verification = new UserVerification(user.getId(), UUID.randomUUID().toString());
        issueOtp(verification, VerificationPurpose.EMAIL_VERIFICATION, user.getEmail(), false);
        return new RegistrationResult(verification.getServerRef(), "Registration successful and OTP sent");
    }

    @Transactional(noRollbackFor = PendingVerificationException.class)
    public LoginResult login(LoginRequest request) {
        ApplicationUser user = userRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));
        if (!passwordMatchesAndMigrate(user, request.password())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        if (user.userStatus() == UserStatus.PENDING) {
            UserVerification verification = verificationRepository.findByAppUserIdForUpdate(user.getId())
                    .orElseGet(() -> new UserVerification(user.getId(), UUID.randomUUID().toString()));
            requireMailDelivery();
            if (!verification.hasPurpose(VerificationPurpose.EMAIL_VERIFICATION)
                    || !verification.isActive()
                    || verification.getExpirationDate() == null
                    || !verification.getExpirationDate().isAfter(now())) {
                issueOtp(verification, VerificationPurpose.EMAIL_VERIFICATION, user.getEmail(), false);
            }
            throw new PendingVerificationException(verification.getServerRef());
        }
        if (user.userStatus() == UserStatus.BLOCKED || user.userStatus() == UserStatus.REMOVED) {
            throw new ApiException(HttpStatus.UNAUTHORIZED,
                    "Sorry, account terminated or temporarily blocked");
        }
        if (user.userStatus() != UserStatus.ACTIVE) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        JwtService.IssuedToken issued = jwtService.issue(user);
        AuthenticationToken token = tokenRepository.findByAppUserId(user.getId())
                .orElseGet(() -> new AuthenticationToken(user.getId()));
        token.rotate(
                tokenDigest.digest(issued.value()),
                LocalDateTime.ofInstant(issued.expiresAt(), ZoneOffset.UTC),
                now()
        );
        tokenRepository.save(token);
        return new LoginResult(
                issued.value(),
                new ProfileData(user.getUsername(), user.getEmail(), user.getId(), user.role().apiValue())
        );
    }

    @Transactional(noRollbackFor = OtpRejectedException.class)
    public void verifyEmail(String serverRef, String otp) {
        UserVerification verification = verificationRepository.findByServerRefForUpdate(serverRef)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED,
                        "Invalid Server Ref. Please try again"));
        validateActiveOtp(verification, otp, VerificationPurpose.EMAIL_VERIFICATION);
        ApplicationUser user = userRepository.findById(verification.getAppUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED,
                        "Invalid Server Ref. Please try again"));
        verification.consume(now());
        user.setStatus(UserStatus.ACTIVE);
    }

    public void resendOtp(String serverRef) {
        var resetReference = resetReferenceService.parseIfValid(serverRef);
        if (resetReference.isPresent()) {
            passwordResetTimingGuard.protect(() -> {
                issuePasswordResetOtpProtected(resetReference.orElseThrow().email());
                return null;
            });
            return;
        }

        passwordResetTransaction.executeWithoutResult(ignored -> resendEmailVerificationOtp(serverRef));
    }

    private void resendEmailVerificationOtp(String serverRef) {
        UserVerification verification = verificationRepository.findByServerRefForUpdate(serverRef)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid Server Ref"));
        ApplicationUser user = userRepository.findById(verification.getAppUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid Server Ref"));
        if (!verification.hasPurpose(VerificationPurpose.EMAIL_VERIFICATION)
                || user.userStatus() != UserStatus.PENDING) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid Server Ref");
        }
        issueOtp(verification, VerificationPurpose.EMAIL_VERIFICATION, user.getEmail(), true);
    }

    public String requestPasswordReset(String rawEmail) {
        return passwordResetTimingGuard.protect(() -> requestPasswordResetProtected(rawEmail));
    }

    @Transactional(noRollbackFor = OtpRejectedException.class)
    public void verifyPasswordReset(ForgotPasswordVerifyRequest request) {
        var resetReference = requireResetReference(
                request.serverRef(), "server_ref is invalid. It cannot match");
        if (!resetReference.email().equals(normalizeEmail(request.email()))) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Email address did not match");
        }
        ApplicationUser user = userRepository.findByEmailIgnoreCase(resetReference.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED,
                        "server_ref is invalid. It cannot match"));
        UserVerification verification = verificationRepository.findByAppUserIdForUpdate(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED,
                        "server_ref is invalid. It cannot match"));
        validateActiveOtp(verification, request.otp(), VerificationPurpose.FORGOT_PASSWORD);
        verification.markVerified(now());
    }

    @Transactional(noRollbackFor = OtpRejectedException.class)
    public void changePassword(ForgotPasswordChangeRequest request) {
        var resetReference = requireResetReference(
                request.serverRef(), "Invalid server_ref. Please try again");
        if (!resetReference.email().equals(normalizeEmail(request.email()))) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Email address did not match");
        }
        ApplicationUser user = userRepository.findByEmailIgnoreCase(resetReference.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Email address did not match"));
        UserVerification verification = verificationRepository.findByAppUserIdForUpdate(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED,
                        "Invalid server_ref. Please try again"));

        if (!verification.hasPurpose(VerificationPurpose.FORGOT_PASSWORD)
                || verification.isActive()
                || verification.getCodeHash() == null
                || verification.getExpirationDate() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid OTP. Please try again");
        }
        if (!verification.getExpirationDate().isAfter(now())) {
            verification.consume(now());
            throw new OtpRejectedException(HttpStatus.GONE, "Invalid OTP. OTP has expired");
        }
        if (!passwordEncoder.matches(request.otp(), verification.getCodeHash())) {
            rejectOtpAttempt(verification);
        }

        user.setPassword(encodePassword(request.password()));
        verification.consume(now());
        tokenRepository.deleteByAppUserId(user.getId());
    }

    @Transactional(readOnly = true)
    public TokenExpiration tokenExpiration(AuthenticatedUser principal, Integer requestedUserId) {
        if (!principal.id().equals(requestedUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
        }
        AuthenticationToken token = tokenRepository.findByAppUserId(requestedUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid app user id"));
        return new TokenExpiration(requestedUserId, token.getExpirationDate());
    }

    private void issueOtp(UserVerification verification, VerificationPurpose purpose, String email,
                          boolean enforceCooldown) {
        requireMailDelivery();
        LocalDateTime now = now();
        if (enforceCooldown
                && verification.hasPurpose(purpose)
                && verification.getLastSentDate() != null
                && verification.getLastSentDate().plus(OTP_RESEND_COOLDOWN).isAfter(now)) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Please wait before requesting another OTP");
        }
        String otp = otpGenerator.generate();
        verification.issue(passwordEncoder.encode(otp), purpose, now.plus(OTP_LIFETIME), now);
        verificationRepository.save(verification);
        mailPort.sendOtp(email, otp, purpose);
    }

    private String requestPasswordResetProtected(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        String resetReference = resetReferenceService.issue(email);
        issuePasswordResetOtpProtected(email);
        return resetReference;
    }

    private void issuePasswordResetOtpProtected(String email) {
        requireMailDelivery();
        String otp = otpGenerator.generate();
        String codeHash = passwordEncoder.encode(otp);
        Optional<PasswordResetDelivery> delivery = passwordResetTransaction.execute(
                ignored -> preparePasswordReset(email, otp, codeHash));
        delivery.ifPresent(this::dispatchPasswordResetOtp);
    }

    private Optional<PasswordResetDelivery> preparePasswordReset(String email, String otp, String codeHash) {
        ApplicationUser user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        int verificationUserId = user == null ? NONEXISTENT_USER_ID : user.getId();
        Optional<UserVerification> existingVerification =
                verificationRepository.findByAppUserIdForUpdate(verificationUserId);
        if (user == null) {
            return Optional.empty();
        }

        UserVerification verification = existingVerification
                .orElseGet(() -> new UserVerification(user.getId(), UUID.randomUUID().toString()));
        LocalDateTime currentTime = now();
        if (verification.hasPurpose(VerificationPurpose.FORGOT_PASSWORD)
                && verification.getLastSentDate() != null
                && verification.getLastSentDate().plus(OTP_RESEND_COOLDOWN).isAfter(currentTime)) {
            return Optional.empty();
        }
        verification.issue(codeHash, VerificationPurpose.FORGOT_PASSWORD,
                currentTime.plus(OTP_LIFETIME), currentTime);
        verificationRepository.save(verification);
        return Optional.of(new PasswordResetDelivery(user.getEmail(), otp));
    }

    private void dispatchPasswordResetOtp(PasswordResetDelivery delivery) {
        try {
            passwordResetDeliveryExecutor.execute(() -> sendPasswordResetOtp(delivery));
        } catch (RuntimeException exception) {
            LOGGER.warn("Password-reset email delivery could not be scheduled");
        }
    }

    private void sendPasswordResetOtp(PasswordResetDelivery delivery) {
        try {
            mailPort.sendOtp(delivery.email(), delivery.otp(), VerificationPurpose.FORGOT_PASSWORD);
        } catch (RuntimeException exception) {
            LOGGER.warn("Password-reset email delivery failed");
        }
    }

    private PasswordResetReferenceService.ResetReference requireResetReference(String value, String error) {
        try {
            return resetReferenceService.parse(value);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, error);
        }
    }

    private void validateActiveOtp(UserVerification verification, String candidate, VerificationPurpose purpose) {
        if (!verification.hasPurpose(purpose) || !verification.isActive() || verification.getCodeHash() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid OTP. Please try again");
        }
        if (verification.getExpirationDate() == null || !verification.getExpirationDate().isAfter(now())) {
            verification.consume(now());
            throw new OtpRejectedException(HttpStatus.GONE, "Invalid OTP. OTP has expired");
        }
        if (!passwordEncoder.matches(candidate, verification.getCodeHash())) {
            rejectOtpAttempt(verification);
        }
    }

    private void rejectOtpAttempt(UserVerification verification) {
        if (verification.recordFailedAttempt(now()) >= MAX_OTP_ATTEMPTS) {
            verification.consume(now());
        }
        throw new OtpRejectedException(HttpStatus.UNAUTHORIZED, "Invalid OTP. Please try again");
    }

    private void requireMailDelivery() {
        if (!mailPort.isAvailable()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Email delivery is disabled");
        }
    }

    private boolean passwordMatchesAndMigrate(ApplicationUser user, String candidate) {
        String storedPassword = user.getPassword();
        if (storedPassword == null) {
            return false;
        }
        if (storedPassword.startsWith(BCRYPT_SHA256_PREFIX)) {
            String bcryptHash = storedPassword.substring(BCRYPT_SHA256_PREFIX.length());
            return passwordEncoder.matches(tokenDigest.digest(candidate), bcryptHash);
        }
        if (isBcrypt(storedPassword)) {
            if (candidate.getBytes(StandardCharsets.UTF_8).length > BCRYPT_INPUT_LIMIT_BYTES) {
                return false;
            }
            return passwordEncoder.matches(candidate, storedPassword);
        }
        if (!constantTimeEquals(storedPassword, candidate)) {
            return false;
        }
        user.setPassword(encodePassword(candidate));
        tokenRepository.deleteByAppUserId(user.getId());
        tokenRepository.flush();
        return true;
    }

    private static boolean isBcrypt(String password) {
        return password != null && password.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    }

    private static boolean constantTimeEquals(String expected, String candidate) {
        if (expected == null || candidate == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                candidate.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private String encodePassword(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length <= BCRYPT_INPUT_LIMIT_BYTES) {
            return passwordEncoder.encode(password);
        }
        return BCRYPT_SHA256_PREFIX + passwordEncoder.encode(tokenDigest.digest(password));
    }

    private static void validateRegistrationRole(String email, Role role) {
        boolean isStudentEmail = email.endsWith("@student.wintec.ac.nz");
        if (role == Role.STUDENT && !isStudentEmail) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "The type selected is student, but your email is not associated with Wintec students. "
                            + "Please check that you have selected the correct type");
        }
        if (role == Role.INDUSTRY && isStudentEmail) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "The type selected is industry, but your email is associated with Wintec students. "
                            + "Please check that you have selected the correct type");
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    public record RegistrationResult(String serverRef, String description) {
    }

    public record LoginResult(String authKey, ProfileData profileData) {
    }

    public record ProfileData(String name, String email, Integer appUid, String userType) {
    }

    public record TokenExpiration(Integer appUid, LocalDateTime expirationDate) {
    }

    private record PasswordResetDelivery(String email, String otp) {
    }

    public static final class PendingVerificationException extends ApiException {
        private final String serverRef;

        public PendingVerificationException(String serverRef) {
            super(HttpStatus.FORBIDDEN, "OTP sent, Email verification required");
            this.serverRef = serverRef;
        }

        public String serverRef() {
            return serverRef;
        }
    }

    public static final class OtpRejectedException extends ApiException {
        public OtpRejectedException(HttpStatus status, String message) {
            super(status, message);
        }
    }
}
