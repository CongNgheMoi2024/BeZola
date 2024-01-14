package iuh.cnm.bezola.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import iuh.cnm.bezola.exceptions.InvalidParamException;
import iuh.cnm.bezola.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtTokenUtil {
    @Value("${jwt.expiration}")
    private int expiration;
    @Value("${jwt.refreshExpiration}")
    private int refreshExpiration;
    @Value("${jwt.secretKey}")
    private String secretKey;

    public String generateToken(User user) throws InvalidParamException {
        Map<String,Object> claims = new HashMap<>();
//        this.generateSecretKey();
        claims.put("phone",user.getPhone());
        try{
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(user.getPhone())
                    .setExpiration(new Date(System.currentTimeMillis()+expiration*1000L))
                    .signWith(getSigninKey(), SignatureAlgorithm.HS256)
                    .compact();
            return token;
        }catch(Exception e){
            throw new InvalidParamException("Cannot create jwt token,error "+e.getMessage());
        }
    }
    public String generateRefreshToken(Map<String,Object> extraClaims, User user) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getPhone())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration*1000L))
                .signWith(getSigninKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateSecretKey(){
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Encoders.BASE64.encode(bytes);
    }
    private Key getSigninKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigninKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public <T> T extractClaim(String token, Function<Claims,T> claimResolver){
        final Claims claims = extractClaims(token);
        return claimResolver.apply(claims);
    }
    public boolean isTokenExpired(String token){
        Date expirationDate = extractClaim(token,Claims::getExpiration);
        return expirationDate.before(new Date());
    }
    public String extractPhone(String token){
        return extractClaim(token,Claims::getSubject);
    }
    public boolean validateToken(String token, UserDetails user){
        final String phone = extractPhone(token);
        return phone.equals(user.getUsername()) && !isTokenExpired(token);
    }
}
