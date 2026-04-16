package com.example.eduskill.security;


import com.example.eduskill.helper.UserHelper;
import com.example.eduskill.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepositories;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header=request.getHeader("Authorization");
        if(header!=null && header.startsWith("Bearer")){
            //token extract & validate then authentication then save that in security context

            String token=header.substring(7);

            try{
                //check for access token
                if(!jwtService.isAccessToken(token)){
                    //message pass
                    filterChain.doFilter(request,response);
                    return;
                }

                Jws<Claims> parse= jwtService.parse(token);
                Claims payload= parse.getPayload();
                String userId=payload.getSubject();
                UUID userUuid= UserHelper.parseUUID(userId);

                userRepositories.findById(userUuid)
                        .ifPresent(user->{
//user is enable then it check, User is authorized and authanticat or not
                            if(user.isEnabled()){
                                List<GrantedAuthority> authority=user.getRoles()==null ? List.of() : user.getRoles().stream()
                                        .map(role->new SimpleGrantedAuthority(role.getName()))
                                        .collect(Collectors.toList());

                                //Authanticated code
                                UsernamePasswordAuthenticationToken authenticationToken=new UsernamePasswordAuthenticationToken(
                                        user.getEmail(),
                                        null,
                                        authority
                                );
                                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                //save in a security-context
                                if(SecurityContextHolder.getContext().getAuthentication()==null)
                                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                            }
                        });

            }catch (MalformedJwtException e){
                request.setAttribute("error","Invalid Token");
                // e.printStackTrace();
            }catch (ExpiredJwtException e){
                request.setAttribute("error","Expired Token");
                //e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        filterChain.doFilter(request,response);
    }

    //not need that jwt in login and register api's
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getRequestURI().startsWith("/api/v1/auth/");
    }
}
