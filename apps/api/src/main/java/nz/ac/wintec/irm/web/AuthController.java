package nz.ac.wintec.irm.web;

import jakarta.validation.Valid;
import nz.ac.wintec.irm.security.AuthenticatedUser;
import nz.ac.wintec.irm.service.AuthService;
import nz.ac.wintec.irm.web.dto.AppUserIdRequest;
import nz.ac.wintec.irm.web.dto.ForgotPasswordChangeRequest;
import nz.ac.wintec.irm.web.dto.ForgotPasswordRequest;
import nz.ac.wintec.irm.web.dto.ForgotPasswordVerifyRequest;
import nz.ac.wintec.irm.web.dto.LoginRequest;
import nz.ac.wintec.irm.web.dto.RegistrationRequest;
import nz.ac.wintec.irm.web.dto.ServerRefRequest;
import nz.ac.wintec.irm.web.dto.VerificationRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registration")
    AuthService.RegistrationResult register(@Valid @RequestBody RegistrationRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    AuthService.LoginResult login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/emailVerification")
    Map<String, String> verifyEmail(@Valid @RequestBody VerificationRequest request) {
        authService.verifyEmail(request.serverRef(), request.otp());
        return Map.of("description", "Email verification successful. Please Sign In");
    }

    @PostMapping("/sendOTP")
    Map<String, String> resendOtp(@Valid @RequestBody ServerRefRequest request) {
        authService.resendOtp(request.serverRef());
        return Map.of("description", "OTP Sent Successfully");
    }

    @PostMapping("/forgotPassRequest")
    Map<String, String> requestPasswordReset(@Valid @RequestBody ForgotPasswordRequest request) {
        String serverRef = authService.requestPasswordReset(request.email());
        return Map.of(
                "server_ref", serverRef,
                "description", "Password reset OTP sent"
        );
    }

    @PostMapping("/forgotPassVerify")
    Map<String, String> verifyPasswordReset(@Valid @RequestBody ForgotPasswordVerifyRequest request) {
        authService.verifyPasswordReset(request);
        return Map.of("description", "OTP Matched");
    }

    @PostMapping("/forgotPassChange")
    Map<String, String> changePassword(@Valid @RequestBody ForgotPasswordChangeRequest request) {
        authService.changePassword(request);
        return Map.of("description", "Password changing successful. Please Sign In");
    }

    @PostMapping("/getTokenExpirationDate")
    AuthService.TokenExpiration getTokenExpirationDate(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody AppUserIdRequest request) {
        return authService.tokenExpiration(principal, request.appUserId());
    }
}
