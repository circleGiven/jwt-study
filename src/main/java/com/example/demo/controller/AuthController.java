package com.example.demo.controller;

import com.example.demo.domain.User;
import com.example.demo.payload.JwtAuthenticationResponse;
import com.example.demo.payload.Result;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    | Private Variables
    |-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenUtil tokenUtil;

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
     * 로그인
     * @param email
     * @return
     */
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestParam(value = "email") String email) {
        String jwt = tokenUtil.generateToken(email);
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

    /**
     * 회원가입
     * @param name
     * @param email
     * @param adminFlag
     * @return
     */
//    @PostMapping("/signup")
//    public Result registerUser(
//            @RequestParam(value = "name") String name,
//            @RequestParam(value = "email") String email,
//            @RequestParam(value = "adminFlag") Boolean adminFlag) {
//
//        // Creating user's account
//        User temp = new User(name, email, adminFlag);
//        User result = userRepository.save(temp);
//
//        URI location = ServletUriComponentsBuilder
//                .fromCurrentContextPath().path("/api/users/{name}")
//                .buildAndExpand(result.getName()).toUri();
//
//        return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully"));
//    }

    /**
     * 토큰 재발급
     * @param authorization
     * @return
     */
    @GetMapping("/refresh")
    public String getRefreshToken(@RequestHeader(value = "Authorization", required = true) String authorization,
                                  @RequestParam(value = "refresh_token") String refreshToken) {
        String userEmail = null;
        // token
        String token = tokenUtil.getJwtFromToken(authorization);
        try {
            userEmail = tokenUtil.getUserEmailFromJWT(token);
        } catch (Exception e) {

        } finally {
            if (null != userEmail) {
                return tokenUtil.generateToken(userEmail);
            } else {
                return null;
            }
        }
    }

    /*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    | Implement Method
    |-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

    /*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    | Override Method
    |-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

    /*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    | private Method
    |-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

    /*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    | Private Method
    |-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

    /*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    | Inner Class
    |-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
}
