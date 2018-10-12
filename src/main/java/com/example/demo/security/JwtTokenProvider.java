package com.example.demo.security;

import com.example.demo.domain.User;
import io.jsonwebtoken.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    /*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    | Private Variables
    |-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    // JWT 암호화 key
    @Value("${app.jwtSecret}")
    private String jwtSecret;

    // access token 만료시간
    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    // refresh token 만료시간
    @Value("${app.jwtRefreshExpirationInMs}")
    private int jwtRefreshExpirationInMs;

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
    | Public Method
    |-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

    /**
     * 토큰 생성
     * @param authentication
     * @return
     */
    public String generateToken(Authentication authentication) {

        String userEmail = (String) authentication.getPrincipal();
        User user = null;

        Claims claim = Jwts.claims();
        claim.put("id", user.getId());
        // 리프래시 토큰
        claim.put("refresh_token", _generateRefreshToken(user));

        return Jwts.builder()
                // JWT token에 내려줄 user 정보 설정
                .setClaims(claim)
                .setSubject("access_token")
                // token이 발급된 시간
                .setIssuedAt(new Date())
                // token 만료시간 설정
                .setExpiration(new Date(jwtExpirationInMs))
                // 암호화
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                // 압축
                .compact();
    }

    /**
     *
     * @param token
     * @return
     */
    public Authentication getAuthentication(String token) {
        Claims claims = _getClaimsFromToken(token);
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        User principal = new User();
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }


    /**
     * 토큰의 이름 얻기
     * @param token
     * @return
     */
    public String getSubjectFromJWT(String token) {
        Claims claims = _getClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * 토큰에서 사용자 아이디 얻기
     * @param token
     * @return
     */
    public String getUserIdFromJWT(String token) {
        Claims claims = _getClaimsFromToken(token);
        return (String) claims.get("id");
    }

    /**
     * 토큰에서 사용자 이름 얻기
     * @param token
     * @return
     */
    public String getUserNameFromJWT(String token) {
        Claims claims = _getClaimsFromToken(token);
        return (String) claims.get("name");
    }

    /**
     * 토큰에서 사용자 이메일 얻기
     * @param token
     * @return
     */
    public String getUserEmailFromJWT(String token) {
        Claims claims = _getClaimsFromToken(token);
        return (String) claims.get("email");
    }

    /**
     * JWT 토큰 유효성 체크
     * @param authToken
     * @return
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            logger.error("시그너처 연산이 실패하였거나, JWT 토큰의 시그너처 검증이 실패하였습니다");
        } catch (MalformedJwtException ex) {
            logger.error("구조적인 문제가 있는 JWT 토큰입니다");
        } catch (ExpiredJwtException ex) {
            logger.error("유효 기간이 지난 JWT 토큰입니다");
        } catch (UnsupportedJwtException ex) {
            logger.error("JWT 토큰의 형식이 애플리케이션에서 원하는 형식과 맞지 않습니다");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT 토큰에 Claim이 비어있습니다");
        }
        return false;
    }

    /*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    | Implement Method
    |-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

    /*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    | Override Method
    |-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

    /*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    | Private Method
    |-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

    /**
     * 토큰으로부터 claims 얻기
     * @param token
     * @return
     */
    private Claims _getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 리프레시 JWT token 생성
     * @param user
     * @return
     */
    private String _generateRefreshToken(User user) {
        // 토큰 정보
        Claims claims = Jwts.claims();
        // 사용자 아이디
        claims.put("id", user.getId());
        return Jwts.builder()
                // JWT token에 내려줄 정보 설정
                .setClaims(claims)
                // 토큰의 이름 설정
                .setSubject("refresh_token")
                // token 만료시간
                .setExpiration(new Date(jwtRefreshExpirationInMs))
                // token 암호화
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                // 압축
                .compact();
    }

    /*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    | Inner Class
    |-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
}
