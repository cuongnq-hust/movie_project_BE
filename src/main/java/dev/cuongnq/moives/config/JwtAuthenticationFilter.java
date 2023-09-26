package dev.cuongnq.moives.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.cuongnq.moives.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter { // xác thực JWT kế thừa OncePerRequestFilter
    @Autowired
    private final UserRepository userRepository;
    @Value("123") // đọc biến
    private String secretKey;
    @Override // xác thực JWT và xử lý yêu cầu
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION); // kiểm tra xem có header trong yêu cầu và có bắt đầu bằng bearer ko
        //Nếu có, nó trích xuất chuỗi JWT và sử dụng SecretKey để xác minh chữ ký của JWT
        if (authorizationHeader!=null && authorizationHeader.startsWith("Bearer ")){
            try {
                //Nếu xác minh thành công, nó lấy tên người dùng từ JWT và tìm người dùng tương ứng trong UserRepository.
                // Sau đó, nó lấy danh sách vai trò từ JWT và tạo một danh sách các SimpleGrantedAuthority
                String token = authorizationHeader.substring("Bearer ".length());
                Algorithm algorithm = Algorithm.HMAC256(secretKey.getBytes());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT =verifier.verify(token);
                String username = decodedJWT.getSubject();
                userRepository.findByEmail(username).orElseThrow(()->new Exception("Invalid Token"));
                String[] roles = decodedJWT.getClaim("roles").asArray(String.class);
                Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                Arrays.stream(roles).forEach(role->{
                    authorities.add(new SimpleGrantedAuthority(role));
                });
                // Tiếp theo, nó tạo một đối tượng UsernamePasswordAuthenticationToken đại diện cho xác thực thành công và đặt nó vào SecurityContextHolder.
                // Sau đó, nó gọi filterChain để tiếp tục xử lý yêu cầu.
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username,null,authorities);
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                filterChain.doFilter(request,response);
            }catch (Exception e){
                //Nếu xác minh JWT không thành công hoặc xảy ra lỗi, nó tạo một đối tượng ErrorResponse chứa thông báo lỗi và trả lời yêu cầu với thông tin lỗi.
                ErrorResponse errorResponse = new ErrorResponse(FORBIDDEN,e.getMessage());
                response.setContentType(APPLICATION_JSON_VALUE);
                response.setStatus(errorResponse.getStatusCodeValue());
                new ObjectMapper().writeValue(response.getOutputStream(),errorResponse);
            }
        }else {
            //Nếu không có header Authorization, nó chuyển tiếp yêu cầu cho filterChain để tiếp tục xử lý.
            filterChain.doFilter(request,response);
        }
    }
}
