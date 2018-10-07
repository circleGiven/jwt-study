package com.example.demo.security;

import com.example.demo.domain.User;
import com.example.demo.service.UserService;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * Jwt token 관련 Util
 */
@Component
public class JwtTokenUtil {

    /*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    | Private Variables
    |-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.expirationTime}")
    private Long expirationTime;

    @Value("${jwt.refreshExpirationTime}")
    private Long refreshExpirationTime;

    @Autowired
    private UserService userService;

    /*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    | Public Variables
    |-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

    /*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    | Constructor
    |-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

    /*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    | Getter & Setter Method ( DI Method )
    |-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

    /*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    | Implement Method
    |-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

    /*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    | Override Method
    |-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

    /*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    | Public Method
    |-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

    /**
     * access 토큰 생성
     * @param userEmail
     * @return
     */
    public String generateToken(String userEmail) {
        // 현재 시간
        Date now = new Date();
        // 유저 정보 조회
        User user = userService.getUserByEmail(userEmail);
        // 토큰 정보
        Claims claims = Jwts.claims();
        claims.put("email", user.getEmail());
        claims.put("name", user.getName());
        claims.put("adminFl", user.getAdminFlag());
        claims.put("id", user.getId());
        // refresh token
        claims.put("refresh_token", generateRefreshToken(user, now));
        // token
        String token = Jwts.builder()
                // 토큰 이름 설정
                .setSubject("access_token")
                // JWT token에 내려줄 정보 설정
                .setClaims(claims)
                // token이 발급된 시간
                .setIssuedAt(new Date())
                // token 만료시간
                .setExpiration(new Date(now.getTime() + expirationTime))
                // token 암호화
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
        return token;
    }

    /**
     * refresh 토큰 생성
     * @param user
     * @param now
     * @return
     */
    private String generateRefreshToken(User user, Date now) {
        // refresh token 생성
        Claims claims = Jwts.claims();
        claims.put("email", user.getEmail());
        claims.put("id", user.getId());
        // 토큰에 리프레시 토큰 정보 추가
        return Jwts.builder()
                // 토큰 이름 설정
                .setSubject("refresh_token")
                // JWT token에 내려줄 정보 설정
                .setClaims(claims)
                // token이 발급된 시간
                .setIssuedAt(now)
                // token 만료시간
                .setExpiration(new Date(now.getTime() + refreshExpirationTime))
                // token 암호화
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    /**
     * Jwt 가져오기
     * @param token
     * @return
     */
    public String getJwtFromToken(String token) {
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            return token.substring(7, token.length());
        }
        return null;
    }

    /**
     * 토큰에서 사용자 아이디 얻기
     * @param token
     * @return
     */
    public String getUserIdFromToken(String token) {
        return (String) getAllClaimsFromToken(token).get("id");
    }

    /**
     * 토큰 검증
     * @param token
     * @return
     */
    public Boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            log.info("Invalid JWT signature.");
            log.trace("Invalid JWT signature trace: {}", e);
        } catch (MalformedJwtException e) {
            log.info("Invalid JWT token.");
            log.trace("Invalid JWT token trace: {}", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token.");
            log.trace("Expired JWT token trace: {}", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token.");
            log.trace("Unsupported JWT token trace: {}", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT token compact of handler are invalid.");
            log.trace("JWT token compact of handler are invalid trace: {}", e);
        }
        return false;
    }

    /**
     * Get authentication
     * @param token
     * @return
     */
    public Authentication getAuthentication(String token) {
        // 유저 아이디
        String userId = getUserIdFromJwt(token);
        // 어드민 여부
        Boolean adminFl = getUserAdminFlagFromJwt(token);
        // authorities
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        // admin 이라면
        if (adminFl) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        // 유저 정보
        User user = userService.getUserById(userId);
        return new UsernamePasswordAuthenticationToken(user, token, authorities);
    }

    /**
     * User id 가져오기
     * @param token
     * @return
     */
    public String getUserIdFromJwt(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return (String) claims.get("id");
    }

    /**
     * 이메일 가져오기
     * @param token
     * @return
     */
    public String getUserEmailFromJWT(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return (String) claims.get("email");
    }

    /**
     * 어드민 여부 가져오기
     * @param token
     * @return
     */
    public Boolean getUserAdminFlagFromJwt(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return (Boolean) claims.get("adminFl");
    }

    /**
     * 토큰에서 생성시간 얻기
     * @param token
     * @return
     */
    public Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }

    /**
     * 토큰에서 만료시간 얻기
     * @param token
     * @return
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 토큰에서 클래임 부분 얻기
     * @param token
     * @param claimsResolver
     * @param <T>
     * @return
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    | Private Method
    |-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

    /**
     * 토큰의 모든 클래임 얻기
     * @param token
     * @return
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    /*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    | Inner Class
    |-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
}
