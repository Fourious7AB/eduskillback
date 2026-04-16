package com.example.eduskill.security;


import com.example.eduskill.entity.Role;
import com.example.eduskill.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Data
public class JwtService {

    private final SecretKey key;
    private final long accessTtlSecond;
    private final long refreshTtlSecond;
    private final String issuer;


    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-ttl-seconds}") long accessTtlSecond,
            @Value("${security.jwt.refresh-ttl-seconds}") long refreshTtlSecond,
            @Value("${security.jwt.issuer}") String issuer) {

        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);

        this.accessTtlSecond = accessTtlSecond;
        this.refreshTtlSecond = refreshTtlSecond;
        this.issuer = issuer;
    }


    //genarate token
    public String generateAccessToken(User user){
        Instant now =Instant.now();
        List<String> roles=user.getRoles()==null ? List.of() : user.getRoles().stream().map(Role::getName).toList();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .issuedAt(Date.from(now))
                .issuer(issuer)
                .expiration(Date.from(now.plusSeconds(accessTtlSecond)))
                .claims(Map.of(
                        "email", user.getEmail(),
                        "roles", roles,
                        "typ", "access"
                ))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();


    }

    //generate refrash token
    public String genarateRefreshToken(User user, String jti){
        Instant now =Instant.now();
        return Jwts.builder()
                .id(jti)
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTtlSecond)))
                .claims(Map.of(
                        "email", user.getEmail(),
                        "typ", "refresh"
                ))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();


    }

    //parse the token
    public Jws<Claims> parse(String token){
        try{
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        }catch (JwtException e){
            throw e;
        }
    }
    public boolean isAccessToken(String token){
        Claims c=parse(token).getPayload();
        return "access".equals(c.get("typ"));
    }
    public boolean isRefreshToken(String token){
        Claims c=parse(token).getPayload();
        return "refresh".equals(c.get("typ"));
    }
    public UUID getUserId(String token){
        Claims c=parse(token).getPayload();
        return UUID.fromString(c.getSubject());
    }
    public String getJti(String token){
        return parse(token).getPayload().getId();
    }
    public List<String> getRole(String token){
        Claims c=parse(token).getPayload();
        return (List<String>) c.get("roles");
    }

    public String getEmail(String token){
        Claims c=parse(token).getPayload();
        return (String) c.get("email");
    }




}
