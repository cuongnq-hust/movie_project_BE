package dev.cuongnq.moives.config;

import dev.cuongnq.moives.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig { // cau hinh chua cac phuong thuc va bean cho viec xác thực và mã hóa mật khẩu
    private final UserRepository userRepository;
    @Bean
    public UserDetailsService userDetailsService(){ // xác thực thông qua giao diện userdetaiService và tìm kiếm user dựa trên emai
        return username -> userRepository.findByEmail(username).orElseThrow(()->new UsernameNotFoundException("USer Not Found"));
    }
    @Bean
    public AuthenticationProvider authenticationProvider(){ // xác thực dựa trên ID
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{ //quản lý xác thực
        return configuration.getAuthenticationManager();
    }
    @Bean
    public PasswordEncoder passwordEncoder(){ // mã hóa mật khẩu
        return new BCryptPasswordEncoder();
    }
}
