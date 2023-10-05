package com.open3hr.adeies.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {
    private static final String SECRET_KEY = "b3BlbjNocmJ5b3RzYW5kbWlrZXBhdmxvc3N0YW1hdGlzdGhvZG9yaXNrb25zdGFudGlub3NzaW1vc2thaW9wb2x5ZXRlbGlzbWVnYXNvbG9udHNpcmlkaXM=";

    public <T> T extractClaim(String token, Function<Claims,T> claimsResolver)
    {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String jwtToken)
    {
       return extractClaim(jwtToken,Claims :: getSubject);
    }

    public String generateToken(UserDetails userDetails)
    {
        return generateToken(new HashMap<>(),userDetails); // if i want to take a token from the userDetails
    }
    public String generateToken(Map<String,Object> extraClaims, UserDetails userDetails)
    {
       return Jwts.builder().setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .claim("roles",userDetails.getAuthorities())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30  ))
                .signWith(getSigninKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    private Claims extractAllClaims(String jwtToken)
    {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigninKey())
                .build()
                .parseClaimsJws(jwtToken).getBody();
    }

    public boolean isTokenValid(String token,UserDetails userDetails)
    {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));

    }

    private  Date extractExpiration(String token){

        return extractClaim(token,Claims::getExpiration);

    }

    public boolean isTokenExpired(String token)
    {
        return extractExpiration(token).before(new Date());
    }
    private Key getSigninKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);


        return Keys.hmacShaKeyFor(keyBytes);
    }


}
