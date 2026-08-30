package nz.ac.wintec.irm;

import nz.ac.wintec.irm.domain.ApplicationUser;
import nz.ac.wintec.irm.domain.Role;
import nz.ac.wintec.irm.domain.UserStatus;
import nz.ac.wintec.irm.domain.VerificationPurpose;
import nz.ac.wintec.irm.mail.MailPort;
import nz.ac.wintec.irm.repository.ApplicationUserRepository;
import nz.ac.wintec.irm.repository.AuthenticationTokenRepository;
import nz.ac.wintec.irm.repository.StudentRepository;
import nz.ac.wintec.irm.repository.UserVerificationRepository;
import nz.ac.wintec.irm.service.OtpGenerator;
import nz.ac.wintec.irm.web.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.nullValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Import(ApiIntegrationTest.TestBeans.class)
class ApiIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationUserRepository userRepository;

    @Autowired
    private UserVerificationRepository verificationRepository;

    @Autowired
    private AuthenticationTokenRepository tokenRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CountingPasswordEncoder passwordResetCost;

    @Autowired
    private CapturingMailPort mailPort;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        tokenRepository.deleteAllInBatch();
        verificationRepository.deleteAllInBatch();
        studentRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        mailPort.clear();
    }

    @Test
    void exposesRootAndHealthWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string("hi, egg"));

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void registrationVerificationAndLoginPreserveTheSnakeCaseContractAndConsumeOtp() throws Exception {
        String email = "student1@student.wintec.ac.nz";
        Registration registration = register(email, "StrongPass1", "Student");
        var storedChallenge = verificationRepository.findByServerRef(registration.serverRef()).orElseThrow();
        assertThat(storedChallenge.getCode()).isNull();
        assertThat(storedChallenge.getCodeHash()).startsWith("$2").doesNotContain(registration.otp());

        login(email, "StrongPass1")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("OTP sent, Email verification required"))
                .andExpect(jsonPath("$.server_ref").value(registration.serverRef()));

        mockMvc.perform(post("/api/emailVerification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("server_ref", registration.serverRef(), "otp", registration.otp()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description")
                        .value("Email verification successful. Please Sign In"));

        mockMvc.perform(post("/api/emailVerification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("server_ref", registration.serverRef(), "otp", registration.otp()))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid OTP. Please try again"));

        MvcResult login = login(email, "StrongPass1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auth_key").isString())
                .andExpect(jsonPath("$.profile_data.email").value(email))
                .andExpect(jsonPath("$.profile_data.user_type").value("Student"))
                .andReturn();
        JsonNode loginBody = read(login);
        int appUserId = loginBody.path("profile_data").path("app_uid").asInt();
        assertThat(appUserId).isPositive();
        assertThat(tokenRepository.findByAppUserId(appUserId).orElseThrow().getToken())
                .hasSize(64)
                .isNotEqualTo(loginBody.path("auth_key").asText());
    }

    @Test
    void loginMigratesALegacyPlaintextPasswordToBcrypt() throws Exception {
        ApplicationUser legacy = userRepository.save(new ApplicationUser(
                "legacy",
                "legacy@student.wintec.ac.nz",
                "LegacyPass1",
                Role.STUDENT,
                UserStatus.ACTIVE,
                LocalDateTime.now()
        ));

        login(legacy.getEmail(), "LegacyPass1")
                .andExpect(status().isOk());

        String migratedPassword = userRepository.findById(legacy.getId()).orElseThrow().getPassword();
        assertThat(migratedPassword).startsWith("$2");
        assertThat(passwordEncoder.matches("LegacyPass1", migratedPassword)).isTrue();
    }

    @Test
    void safelyMigratesALegacyPlaintextPasswordLongerThanBcryptsInputLimit() throws Exception {
        String longPassword = "LongPassword1" + "x".repeat(100);
        ApplicationUser legacy = userRepository.save(new ApplicationUser(
                "longlegacy",
                "longlegacy@student.wintec.ac.nz",
                longPassword,
                Role.STUDENT,
                UserStatus.ACTIVE,
                LocalDateTime.now()
        ));

        login(legacy.getEmail(), longPassword).andExpect(status().isOk());
        String migratedPassword = userRepository.findById(legacy.getId()).orElseThrow().getPassword();
        assertThat(migratedPassword).startsWith("{bcrypt-sha256}$2");

        login(legacy.getEmail(), longPassword).andExpect(status().isOk());
        login(legacy.getEmail(), longPassword + "wrong").andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsBadJwtAndEnforcesStudentIndustryAndAdminRoles() throws Exception {
        mockMvc.perform(get("/api/allStudents")
                        .header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error")
                        .value("Authentication failed: Invalid or expired token"));

        AuthenticatedAccount student = createActiveAccount(
                "student2@student.wintec.ac.nz", "StudentPass1", Role.STUDENT);
        mockMvc.perform(get("/api/allStudents")
                        .header("Authorization", bearer(student.token())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied"));

        AuthenticatedAccount industry = createActiveAccount(
                "industry@example.com", "IndustryPass1", Role.INDUSTRY);
        mockMvc.perform(get("/api/allStudents")
                        .header("Authorization", bearer(industry.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.students").isArray());

        AuthenticatedAccount admin = createActiveAccount(
                "admin@example.com", "AdminPass1", Role.ADMIN);
        mockMvc.perform(get("/api/allStudents")
                        .header("Authorization", bearer(admin.token())))
                .andExpect(status().isOk());
    }

    @Test
    void locksAnOtpAfterFiveFailuresAndThrottlesAReplacement() throws Exception {
        String email = "locked@student.wintec.ac.nz";
        Registration registration = register(email, "StrongPass1", "Student");

        for (int attempt = 0; attempt < 5; attempt++) {
            mockMvc.perform(post("/api/emailVerification")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(Map.of("server_ref", registration.serverRef(), "otp", "000000"))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid OTP. Please try again"));
        }

        mockMvc.perform(post("/api/emailVerification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("server_ref", registration.serverRef(), "otp", registration.otp()))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/sendOTP")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("server_ref", registration.serverRef()))))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("Please wait before requesting another OTP"));
        assertThat(verificationRepository.findByServerRef(registration.serverRef()).orElseThrow().getCodeHash())
                .isNull();
    }

    @Test
    void studentApplicationIsUpsertedAndProfileAccessIsBoundToTheAuthenticatedUser() throws Exception {
        AuthenticatedAccount owner = createActiveAccount(
                "student3@student.wintec.ac.nz", "StudentPass1", Role.STUDENT);

        mockMvc.perform(post("/api/completeApplication")
                        .header("Authorization", bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applicationJson("First Name")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Application saved"));

        mockMvc.perform(post("/api/completeApplication")
                        .header("Authorization", bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applicationJson("Unsafe Name")
                                .replace("https://example.com/cv", "javascript:alert(1)")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Profile links must be absolute HTTP or HTTPS URLs"));

        mockMvc.perform(post("/api/userProfileData")
                        .header("Authorization", bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("user_id", owner.id()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.student.name").value("First Name"))
                .andExpect(jsonPath("$.student.reference").value("Tutor One, Tutor Two"))
                .andExpect(jsonPath("$.student.internship_options")
                        .value("[\"Development\",\"Analysis\"]"))
                .andExpect(jsonPath("$.student.preferred_companies")
                        .value("[\"Company A\",\"Company B\"]"))
                .andExpect(jsonPath("$.student.speciality").value(nullValue()))
                .andExpect(jsonPath("$.student.intern_status").value(nullValue()));

        mockMvc.perform(post("/api/completeApplication")
                        .header("Authorization", bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applicationJson("Updated Name")))
                .andExpect(status().isOk());
        assertThat(studentRepository.count()).isEqualTo(1);
        assertThat(studentRepository.findByAppUserId(owner.id()).orElseThrow().getName())
                .isEqualTo("Updated Name");

        AuthenticatedAccount anotherStudent = createActiveAccount(
                "student4@student.wintec.ac.nz", "StudentPass1", Role.STUDENT);
        mockMvc.perform(post("/api/userProfileData")
                        .header("Authorization", bearer(anotherStudent.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("user_id", owner.id()))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/getTokenExpirationDate")
                        .header("Authorization", bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("app_user_id", anotherStudent.id()))))
                .andExpect(status().isForbidden());

        AuthenticatedAccount industry = createActiveAccount(
                "viewer@example.com", "IndustryPass1", Role.INDUSTRY);
        mockMvc.perform(post("/api/userProfileData")
                        .header("Authorization", bearer(industry.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("user_id", owner.id()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.student.name").value("Updated Name"));
    }

    @Test
    void verifiedPasswordResetIsSingleUseAndRevokesTheExistingToken() throws Exception {
        String email = "student5@student.wintec.ac.nz";
        AuthenticatedAccount account = createActiveAccount(email, "OldPassword1", Role.STUDENT);

        MvcResult resetRequest = mockMvc.perform(post("/api/forgotPassRequest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Password reset OTP sent"))
                .andReturn();
        String serverRef = read(resetRequest).path("server_ref").asText();
        String otp = mailPort.otpFor(email);

        mockMvc.perform(post("/api/forgotPassVerify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("server_ref", serverRef, "email", email, "otp", otp))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("OTP Matched"));

        mockMvc.perform(post("/api/forgotPassVerify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("server_ref", serverRef, "email", email, "otp", otp))))
                .andExpect(status().isUnauthorized());

        String changeBody = json(Map.of(
                "server_ref", serverRef,
                "email", email,
                "otp", otp,
                "password", "NewPassword2"
        ));
        mockMvc.perform(post("/api/forgotPassChange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changeBody))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/getTokenExpirationDate")
                        .header("Authorization", bearer(account.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("app_user_id", account.id()))))
                .andExpect(status().isUnauthorized());

        login(email, "OldPassword1").andExpect(status().isUnauthorized());
        login(email, "NewPassword2").andExpect(status().isOk());

        mockMvc.perform(post("/api/forgotPassChange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changeBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void passwordResetRequestAndResendDoNotRevealWhetherAnEmailIsRegistered() throws Exception {
        String registeredEmail = "resettable@student.wintec.ac.nz";
        String unknownEmail = "unknown@example.com";
        createActiveAccount(registeredEmail, "OldPassword1", Role.STUDENT);
        mailPort.clear();

        MvcResult firstRegisteredRequest = mockMvc.perform(post("/api/forgotPassRequest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", registeredEmail))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.server_ref").isString())
                .andExpect(jsonPath("$.description").value("Password reset OTP sent"))
                .andReturn();
        MvcResult secondRegisteredRequest = mockMvc.perform(post("/api/forgotPassRequest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", registeredEmail))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.server_ref").isString())
                .andExpect(jsonPath("$.description").value("Password reset OTP sent"))
                .andReturn();
        MvcResult firstUnknownRequest = mockMvc.perform(post("/api/forgotPassRequest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", unknownEmail))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.server_ref").isString())
                .andExpect(jsonPath("$.description").value("Password reset OTP sent"))
                .andReturn();
        MvcResult secondUnknownRequest = mockMvc.perform(post("/api/forgotPassRequest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", unknownEmail))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.server_ref").isString())
                .andExpect(jsonPath("$.description").value("Password reset OTP sent"))
                .andReturn();

        String firstRegisteredRef = read(firstRegisteredRequest).path("server_ref").asText();
        String secondRegisteredRef = read(secondRegisteredRequest).path("server_ref").asText();
        String firstUnknownRef = read(firstUnknownRequest).path("server_ref").asText();
        String secondUnknownRef = read(secondUnknownRequest).path("server_ref").asText();
        assertThat(firstRegisteredRef).isNotEqualTo(secondRegisteredRef);
        assertThat(firstUnknownRef).isNotEqualTo(secondUnknownRef);
        assertThat(mailPort.sendCountFor(registeredEmail)).isEqualTo(1);
        assertThat(mailPort.sendCountFor(unknownEmail)).isZero();

        for (String resetRef : new String[]{firstRegisteredRef, firstUnknownRef}) {
            mockMvc.perform(post("/api/sendOTP")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(Map.of("server_ref", resetRef))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.description").value("OTP Sent Successfully"));
        }
        assertThat(mailPort.sendCountFor(registeredEmail)).isEqualTo(1);
        assertThat(mailPort.sendCountFor(unknownEmail)).isZero();
    }

    @Test
    void passwordResetRequestUsesEquivalentExpensiveCostForEveryAccountState() throws Exception {
        String registeredEmail = "timing@student.wintec.ac.nz";
        createActiveAccount(registeredEmail, "OldPassword1", Role.STUDENT);
        mailPort.clear();

        passwordResetCost.resetEncodeCount();
        mockMvc.perform(post("/api/forgotPassRequest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", registeredEmail))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Password reset OTP sent"));
        int registeredCost = passwordResetCost.encodeCount();

        passwordResetCost.resetEncodeCount();
        mockMvc.perform(post("/api/forgotPassRequest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", registeredEmail))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Password reset OTP sent"));
        int cooldownCost = passwordResetCost.encodeCount();

        long verificationRowsBeforeUnknownRequest = verificationRepository.count();
        passwordResetCost.resetEncodeCount();
        mockMvc.perform(post("/api/forgotPassRequest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "not-registered@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Password reset OTP sent"));
        int unknownCost = passwordResetCost.encodeCount();

        assertThat(registeredCost).isEqualTo(1);
        assertThat(cooldownCost).isEqualTo(registeredCost);
        assertThat(unknownCost).isEqualTo(registeredCost);
        assertThat(verificationRepository.count()).isEqualTo(verificationRowsBeforeUnknownRequest);
    }

    @Test
    void passwordResetRequestDoesNotRevealMailDeliveryFailureForAKnownAccount() throws Exception {
        String registeredEmail = "mail-failure@student.wintec.ac.nz";
        createActiveAccount(registeredEmail, "OldPassword1", Role.STUDENT);
        mailPort.clear();
        mailPort.failDelivery();

        mockMvc.perform(post("/api/forgotPassRequest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", registeredEmail))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.server_ref").isString())
                .andExpect(jsonPath("$.description").value("Password reset OTP sent"));
        assertThat(mailPort.awaitDeliveryFinished(2, TimeUnit.SECONDS)).isTrue();

        mockMvc.perform(post("/api/forgotPassRequest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "mail-failure-unknown@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.server_ref").isString())
                .andExpect(jsonPath("$.description").value("Password reset OTP sent"));
    }

    @Test
    void passwordResetResponseDoesNotWaitForMailDelivery() throws Exception {
        String registeredEmail = "slow-mail@student.wintec.ac.nz";
        createActiveAccount(registeredEmail, "OldPassword1", Role.STUDENT);
        mailPort.clear();
        mailPort.blockDelivery();

        ExecutorService httpCaller = Executors.newSingleThreadExecutor();
        Future<MvcResult> response = httpCaller.submit(() -> mockMvc.perform(post("/api/forgotPassRequest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", registeredEmail))))
                .andReturn());
        try {
            assertThat(mailPort.awaitDeliveryStarted(2, TimeUnit.SECONDS)).isTrue();
            MvcResult result = response.get(2, TimeUnit.SECONDS);
            assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
            assertThat(read(result).path("description").asText()).isEqualTo("Password reset OTP sent");
        } finally {
            mailPort.releaseDelivery();
            httpCaller.shutdownNow();
        }
        assertThat(mailPort.awaitDeliveryFinished(2, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void passwordResetResendUsesEquivalentExpensiveCostForKnownAndUnknownAccounts() throws Exception {
        String registeredEmail = "resend-timing@student.wintec.ac.nz";
        createActiveAccount(registeredEmail, "OldPassword1", Role.STUDENT);

        MvcResult registeredRequest = mockMvc.perform(post("/api/forgotPassRequest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", registeredEmail))))
                .andExpect(status().isOk())
                .andReturn();
        MvcResult unknownRequest = mockMvc.perform(post("/api/forgotPassRequest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "resend-timing-unknown@example.com"))))
                .andExpect(status().isOk())
                .andReturn();

        passwordResetCost.resetEncodeCount();
        mockMvc.perform(post("/api/sendOTP")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("server_ref", read(registeredRequest).path("server_ref").asText()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("OTP Sent Successfully"));
        int registeredCost = passwordResetCost.encodeCount();

        long verificationRowsBeforeUnknownResend = verificationRepository.count();
        passwordResetCost.resetEncodeCount();
        mockMvc.perform(post("/api/sendOTP")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("server_ref", read(unknownRequest).path("server_ref").asText()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("OTP Sent Successfully"));
        int unknownCost = passwordResetCost.encodeCount();

        assertThat(registeredCost).isEqualTo(1);
        assertThat(unknownCost).isEqualTo(registeredCost);
        assertThat(verificationRepository.count()).isEqualTo(verificationRowsBeforeUnknownResend);
    }

    @Test
    void passwordResetResendDoesNotWaitForOrExposeMailDelivery() throws Exception {
        String registeredEmail = "slow-resend@student.wintec.ac.nz";
        createActiveAccount(registeredEmail, "OldPassword1", Role.STUDENT);
        MvcResult resetRequest = mockMvc.perform(post("/api/forgotPassRequest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", registeredEmail))))
                .andExpect(status().isOk())
                .andReturn();
        String resetRef = read(resetRequest).path("server_ref").asText();

        verificationRepository.deleteAllInBatch();
        mailPort.clear();
        mailPort.blockDelivery();

        ExecutorService httpCaller = Executors.newSingleThreadExecutor();
        Future<MvcResult> response = httpCaller.submit(() -> mockMvc.perform(post("/api/sendOTP")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("server_ref", resetRef))))
                .andReturn());
        try {
            assertThat(mailPort.awaitDeliveryStarted(2, TimeUnit.SECONDS)).isTrue();
            MvcResult result = response.get(2, TimeUnit.SECONDS);
            assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
            assertThat(read(result).path("description").asText()).isEqualTo("OTP Sent Successfully");
        } finally {
            mailPort.releaseDelivery();
            httpCaller.shutdownNow();
        }
        assertThat(mailPort.awaitDeliveryFinished(2, TimeUnit.SECONDS)).isTrue();

        verificationRepository.deleteAllInBatch();
        mailPort.clear();
        mailPort.failDelivery();
        mockMvc.perform(post("/api/sendOTP")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("server_ref", resetRef))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("OTP Sent Successfully"));
        assertThat(mailPort.awaitDeliveryFinished(2, TimeUnit.SECONDS)).isTrue();
    }

    private Registration register(String email, String password, String type) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", password, "type", type))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.server_ref").isString())
                .andExpect(jsonPath("$.description").value("Registration successful and OTP sent"))
                .andReturn();
        return new Registration(read(result).path("server_ref").asText(), mailPort.otpFor(email));
    }

    private AuthenticatedAccount createActiveAccount(String email, String password, Role role) throws Exception {
        if (role == Role.STUDENT || role == Role.INDUSTRY) {
            Registration registration = register(email, password, role.apiValue());
            mockMvc.perform(post("/api/emailVerification")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(Map.of("server_ref", registration.serverRef(), "otp", registration.otp()))))
                    .andExpect(status().isOk());
        } else {
            userRepository.save(new ApplicationUser(
                    email.substring(0, email.indexOf('@')),
                    email,
                    passwordEncoder.encode(password),
                    role,
                    UserStatus.ACTIVE,
                    LocalDateTime.now()
            ));
        }
        MvcResult result = login(email, password).andExpect(status().isOk()).andReturn();
        JsonNode body = read(result);
        return new AuthenticatedAccount(
                body.path("profile_data").path("app_uid").asInt(),
                body.path("auth_key").asText()
        );
    }

    private org.springframework.test.web.servlet.ResultActions login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("email", email, "password", password))));
    }

    private String applicationJson(String name) throws Exception {
        return json(Map.ofEntries(
                Map.entry("name", name),
                Map.entry("wintec_id", "1234567"),
                Map.entry("gender", "Female"),
                Map.entry("student_type", "Domestic Student"),
                Map.entry("personal_email", "personal@example.com"),
                Map.entry("student_email", "student3@student.wintec.ac.nz"),
                Map.entry("phone_number", "0210000000"),
                Map.entry("personal_statement", "I enjoy building useful software."),
                Map.entry("cv_link", "https://example.com/cv"),
                Map.entry("linkedin_link", "https://linkedin.example/student"),
                Map.entry("portfolio_link", "https://portfolio.example/student"),
                Map.entry("github_link", "https://github.example/student"),
                Map.entry("average_grade", "A"),
                Map.entry("programme_of_study", "Bachelor of Applied IT"),
                Map.entry("area_of_study", "Software Engineering"),
                Map.entry("internship_options", new String[]{"Development", "Analysis"}),
                Map.entry("preferred_companies", new String[]{"Company A", "Company B"}),
                Map.entry("first_preference", "Internship"),
                Map.entry("second_preference", "Industry Project"),
                Map.entry("skills", "Java, SQL"),
                Map.entry("favourite_courses", "Software Engineering"),
                Map.entry("references", "Tutor One, Tutor Two")
        ));
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private JsonNode read(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsByteArray());
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private record Registration(String serverRef, String otp) {
    }

    private record AuthenticatedAccount(Integer id, String token) {
    }

    @TestConfiguration
    static class TestBeans {

        @Bean
        @Primary
        OtpGenerator deterministicOtpGenerator() {
            return () -> "123456";
        }

        @Bean
        @Primary
        CapturingMailPort capturingMailPort() {
            return new CapturingMailPort();
        }

        @Bean
        @Primary
        CountingPasswordEncoder countingPasswordEncoder() {
            return new CountingPasswordEncoder();
        }
    }

    static final class CountingPasswordEncoder implements PasswordEncoder {
        private final PasswordEncoder delegate = new BCryptPasswordEncoder();
        private final AtomicInteger encodeCount = new AtomicInteger();

        @Override
        public String encode(CharSequence rawPassword) {
            encodeCount.incrementAndGet();
            return delegate.encode(rawPassword);
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return delegate.matches(rawPassword, encodedPassword);
        }

        int encodeCount() {
            return encodeCount.get();
        }

        void resetEncodeCount() {
            encodeCount.set(0);
        }
    }

    static final class CapturingMailPort implements MailPort {
        private final Map<String, String> otpByRecipient = new ConcurrentHashMap<>();
        private final Map<String, AtomicInteger> sendCountByRecipient = new ConcurrentHashMap<>();
        private volatile boolean deliveryFails;
        private volatile CountDownLatch deliveryStarted = new CountDownLatch(0);
        private volatile CountDownLatch deliveryRelease = new CountDownLatch(0);
        private volatile CountDownLatch deliveryFinished = new CountDownLatch(0);

        @Override
        public void sendOtp(String recipient, String otp, VerificationPurpose purpose) {
            deliveryStarted.countDown();
            try {
                deliveryRelease.await();
                if (deliveryFails) {
                    throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Email delivery is unavailable");
                }
                otpByRecipient.put(recipient, otp);
                sendCountByRecipient.computeIfAbsent(recipient, ignored -> new AtomicInteger()).incrementAndGet();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Email delivery is unavailable");
            } finally {
                deliveryFinished.countDown();
            }
        }

        String otpFor(String recipient) {
            String otp = otpByRecipient.get(recipient);
            assertThat(otp).as("captured OTP for %s", recipient).isNotBlank();
            return otp;
        }

        int sendCountFor(String recipient) {
            AtomicInteger count = sendCountByRecipient.get(recipient);
            return count == null ? 0 : count.get();
        }

        void clear() {
            deliveryRelease.countDown();
            otpByRecipient.clear();
            sendCountByRecipient.clear();
            deliveryFails = false;
            deliveryStarted = new CountDownLatch(0);
            deliveryRelease = new CountDownLatch(0);
            deliveryFinished = new CountDownLatch(0);
        }

        void failDelivery() {
            deliveryFails = true;
            deliveryStarted = new CountDownLatch(1);
            deliveryFinished = new CountDownLatch(1);
        }

        void blockDelivery() {
            deliveryStarted = new CountDownLatch(1);
            deliveryRelease = new CountDownLatch(1);
            deliveryFinished = new CountDownLatch(1);
        }

        boolean awaitDeliveryStarted(long timeout, TimeUnit unit) throws InterruptedException {
            return deliveryStarted.await(timeout, unit);
        }

        void releaseDelivery() {
            deliveryRelease.countDown();
        }

        boolean awaitDeliveryFinished(long timeout, TimeUnit unit) throws InterruptedException {
            return deliveryFinished.await(timeout, unit);
        }
    }
}
