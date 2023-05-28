package user.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import user.model.Auth;
import user.service.CustomUserDetailsService;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class JwtProvider {
    @Value("${jwt.secret.key}")
    private String salt;
    private Key secretKey;
    // 만료시간 : 1Hour
    @Value("${token.access-expired-time}")
    private final long ACCESS_EXPIRED_TIME = 1000L * 60 * 60;
    @Value("${token.refresh-expired-time}")
    private final long REFRESH_EXPIRED_TIME = 1000L * 60 * 60;
    @Autowired
    CustomUserDetailsService customUserDetailsService;
    @PostConstruct
    protected void init() {
        secretKey = Keys.hmacShaKeyFor(salt.getBytes(StandardCharsets.UTF_8));
    }
    // 토큰 생성
    public String createToken(String domainCd, String userCd, String userId, List<Auth> roles) {
        Claims claims = Jwts.claims().setSubject(userId);
        claims.put("domainCd", domainCd);
        claims.put("userCd", userCd);
        //claims.put("roles", roles);
        return Jwts.builder()
            .setClaims(claims)
            .setExpiration(new Date(System.currentTimeMillis()  + ACCESS_EXPIRED_TIME))
            .setIssuedAt(new Date())
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
    }

    public String createRefreshToken() {
        Claims claims = Jwts.claims();
        claims.put("value", UUID.randomUUID());
        return Jwts.builder()
                .addClaims(claims)
                .setExpiration(
                    new Date(System.currentTimeMillis() + REFRESH_EXPIRED_TIME)
                )
                .setIssuedAt(new Date())
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
    // 권한정보 획득
    // Spring Security 인증과정에서 권한확인을 위한 기능
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(this.getAccount(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
    // 토큰에 담겨있는 유저 account 획득
    public String getAccount(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();
    }
    // Authorization Header를 통해 인증을 한다.
    public String resolveToken(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }
    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            // Bearer 검증
            if (!token.substring(0, "BEARER ".length()).equalsIgnoreCase("BEARER ")) {
                return false;
            } else {
                token = token.split(" ")[1].trim();
            }
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            // 만료되었을 시 false
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}