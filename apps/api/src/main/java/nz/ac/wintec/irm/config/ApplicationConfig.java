package nz.ac.wintec.irm.config;

import nz.ac.wintec.irm.service.OtpGenerator;
import nz.ac.wintec.irm.service.SecureRandomOtpGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;

@Configuration
public class ApplicationConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    OtpGenerator otpGenerator() {
        return new SecureRandomOtpGenerator();
    }
}
