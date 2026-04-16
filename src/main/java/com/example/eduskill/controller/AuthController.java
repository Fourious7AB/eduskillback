package com.example.eduskill.controller;


import com.example.eduskill.dto.LoginRequest;
import com.example.eduskill.dto.RefreshTokenRequest;
import com.example.eduskill.dto.TokenResponse;
import com.example.eduskill.dto.UserDto;
import com.example.eduskill.entity.RefreshToken;
import com.example.eduskill.entity.User;
import com.example.eduskill.repository.RefreshTokenRepository;
import com.example.eduskill.repository.UserRepository;
import com.example.eduskill.security.CookieService;
import com.example.eduskill.security.JwtService;
import com.example.eduskill.service.AuthService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepositories;
    private final JwtService jwtService;
    private final ModelMapper mapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieService cookieService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDto));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse response
    ){

        //authanticate
        Authentication authentication=authenticate(loginRequest);
        User user = userRepositories.findByEmployeeCode(loginRequest.employeeCode())
                .orElseThrow(() -> new BadCredentialsException("Invalid Username "));

        if(!user.isEnabled()){
            throw   new DisabledException("User is disabled");
        }

        String jti= UUID.randomUUID().toString();
        var refreshTokenDB= RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createdAt(Instant.now())
                .expiredAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSecond()))
                .revoked(false)
                .build();
        //save rerfreshToken(ID) save in->DB
        refreshTokenRepository.save(refreshTokenDB);

//genarate token-->access token
        String accessToken= jwtService.generateAccessToken(user);
        String refreshToken=jwtService.genarateRefreshToken(user,refreshTokenDB.getJti());

        //use cookie service to attach refresh token
        cookieService.attachRefreshCookie(response,refreshToken,(int)jwtService.getRefreshTtlSecond());
        cookieService.addNoStoreHeader(response);

        TokenResponse tokenResponse= TokenResponse.bearer(
                accessToken,
                refreshToken,
                jwtService.getAccessTtlSecond(),
                mapper.map(user, UserDto.class)
        );
        System.out.println("LOGIN ATTEMPT");
        System.out.println("EmployeeCode: " + loginRequest.employeeCode());
        System.out.println("Password raw: " + loginRequest.password());


        return ResponseEntity.ok(tokenResponse);

    }

    private Authentication authenticate(LoginRequest loginRequest) {

        try{
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.employeeCode(),loginRequest.password()));

        }catch (Exception e){
            throw new BadCredentialsException("Invalid Username and Password");

        }
    }
    //access and refresh token renew
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> registerUser(@RequestBody(required = false) RefreshTokenRequest body,
                                                      HttpServletRequest request,
                                                      HttpServletResponse response){

        String refreshToken=readRefreshTOkebFromRequest(body,request).orElseThrow(()->new BadCredentialsException("Refresh Token is Missing"));

        if(!jwtService.isRefreshToken(refreshToken)){
            throw new BadCredentialsException("Invalid Refresh Token");
        }
        String newjit=jwtService.getJti(refreshToken);
        UUID userId=jwtService.getUserId(refreshToken);
        RefreshToken storeRefreshToken=refreshTokenRepository.findByJti(newjit).orElseThrow(()->new BadCredentialsException("REfresh Token not Recogniged"));

        //Thread.sleep(5000);//it take time too refresh for 5second
        if(storeRefreshToken.isRevoked()){
            throw new BadCredentialsException("Refresh token is revoked");
        }
        if(storeRefreshToken.getExpiredAt().isBefore(Instant.now())){
            throw new BadCredentialsException("REfresh token expired");
        }
        if(!storeRefreshToken.getUser().getId().equals(userId)){
            throw  new BadCredentialsException("REfresh token does not belong to this user");
        }
        //refresh token rotation
        storeRefreshToken.setRevoked(true);
        String newJti=UUID.randomUUID().toString();
        refreshTokenRepository.save(storeRefreshToken);

        User user=storeRefreshToken.getUser();
        var refreshTokenDB= RefreshToken.builder()
                .jti(newJti)
                .user(user)
                .createdAt(Instant.now())
                .expiredAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSecond()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshTokenDB);
        String newAccessToken= jwtService.generateAccessToken(user);
        String newRefreshToken=jwtService.genarateRefreshToken(user,refreshTokenDB.getJti());

        //use cookie service to attach refresh token
        cookieService.attachRefreshCookie(response,newRefreshToken,(int)jwtService.getRefreshTtlSecond());
        cookieService.addNoStoreHeader(response);


        TokenResponse tokenResponse= TokenResponse.bearer(
                newAccessToken,
                newRefreshToken,
                jwtService.getAccessTtlSecond(),
                mapper.map(user, UserDto.class)
        );
        return ResponseEntity.ok(tokenResponse);


    }

    private Optional<String> readRefreshTOkebFromRequest(RefreshTokenRequest body, HttpServletRequest request) {
        //1.prefer read-in refresh token from cookie
        if(request.getCookies()!=null){
            Optional<String> fromCookie= Arrays.stream(
                            request.getCookies()
                    ).filter(c->cookieService.getRefreshTokenCookieName().equals(c.getName()))
                    .map(Cookie::getValue)
                    .filter(v-> !v.isBlank())
                    .findFirst();
            if(fromCookie.isPresent()){
                return fromCookie;
            }
        }
        //2.body
        if(body!=null && body.refreshToken()!=null && !body.refreshToken().isBlank()){
            return Optional.of(body.refreshToken());
        }
        //3.coustom header
        String refreshHeader=request.getHeader(("x-Refresh-token"));
        if(refreshHeader!=null && !refreshHeader.isBlank()){
            return Optional.of(refreshHeader.trim());
        }
        return Optional.empty();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response){
        readRefreshTOkebFromRequest(null,request).ifPresent( token->{
            try{
                if(jwtService.isRefreshToken(token)){
                    String jti=jwtService.getJti(token);
                    refreshTokenRepository.findByJti(jti).ifPresent(rt->{
                        rt.setRevoked(true);
                        refreshTokenRepository.save(rt);
                    });
                }
            }catch (JwtException ignored){

            }
        });
        //Use CookieUtil (same behavior)
        cookieService.clearRefreshCookie(response);
        cookieService.addNoStoreHeader(response);
        SecurityContextHolder.clearContext();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
