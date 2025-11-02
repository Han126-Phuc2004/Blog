package security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Utility Class
 * 
 * Chức năng:
 * - Generate JWT Token từ username
 * - Validate JWT Token
 * - Extract thông tin từ JWT Token (username, expiration, claims)
 */
@Component
public class JwtUtil {

    // Secret key để ký JWT token (sẽ lấy từ application.properties)
    @Value("${jwt.secret:mySecretKeyForJWTAuthenticationBlog2024MustBe256BitsLongForHS256Algorithm}")
    private String secretKey;

    // Thời gian hết hạn token (milliseconds) - Default: 24 giờ
    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    /**
     * Tạo SecretKey từ string secret
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * Extract username từ JWT token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract expiration date từ JWT token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract một claim cụ thể từ JWT token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract tất cả claims từ JWT token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Kiểm tra token đã hết hạn chưa
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Generate JWT token cho user
     * 
     * @param userDetails Spring Security UserDetails object
     * @return JWT token string
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        
        // Có thể thêm custom claims vào đây
        // claims.put("role", userDetails.getAuthorities());
        // claims.put("email", userDetails.getEmail());
        
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Generate JWT token với custom claims
     */
    public String generateToken(String username, Map<String, Object> extraClaims) {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        return createToken(claims, username);
    }

    /**
     * Tạo JWT token với claims và subject
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validate JWT token
     * 
     * Kiểm tra:
     * 1. Username trong token có khớp với UserDetails không
     * 2. Token có hết hạn chưa
     * 
     * @param token JWT token
     * @param userDetails UserDetails từ database
     * @return true nếu token hợp lệ
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            // Token invalid (malformed, expired, signature mismatch, etc.)
            return false;
        }
    }

    /**
     * Validate token mà không cần UserDetails
     * Chỉ kiểm tra token có hợp lệ và chưa hết hạn
     */
    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}

