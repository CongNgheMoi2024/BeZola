package iuh.cnm.bezola.filters;

import iuh.cnm.bezola.models.User;
import iuh.cnm.bezola.utils.JwtTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor

public class JwtTokenFilter extends OncePerRequestFilter{
    @Value("${api.prefix}")
    private String apiPrefix;
    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws IOException {
        try {
            if(isBypassToken(request)) {
                filterChain.doFilter(request, response);
                return;
            }
            final String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "authHeader null or not started with Bearer");
                return;
            }
            final String token = authHeader.substring(7);
            final String phone = jwtTokenUtil.extractPhone(token);
            if (phone != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                User userDetails = (User) userDetailsService.loadUserByUsername(phone);
                if(jwtTokenUtil.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
            filterChain.doFilter(request, response);
        }catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(e.getMessage());
        }

    }

    private boolean isBypassToken(@NonNull HttpServletRequest request) {
        final List<Pair<String, String>> bypassTokens = Arrays.asList(
                Pair.of(String.format("%s/auth/login", apiPrefix), "POST"),
                Pair.of(String.format("%s/auth/register", apiPrefix), "POST"),
                Pair.of(String.format("%s/auth/refresh", apiPrefix), "POST"),
                Pair.of(String.format("%s/phoneNumber", apiPrefix), "POST"),
                Pair.of(String.format("%s/otp", apiPrefix), "POST"),
                Pair.of(String.format("%s/forget-password", apiPrefix), "POST"),
                Pair.of(String.format("%s/otp-forget/**", apiPrefix), "PUT"),
                Pair.of(String.format("%s/heartbeat", apiPrefix), "GET"),

                // Swagger
                Pair.of("/api-docs","GET"),
                Pair.of("/api-docs/**","GET"),
                Pair.of("/swagger-resources","GET"),
                Pair.of("/swagger-resources/**","GET"),
                Pair.of("/configuration/ui","GET"),
                Pair.of("/configuration/security","GET"),
                Pair.of("/swagger-ui/**","GET"),
                Pair.of("/swagger-ui.html", "GET"),
                Pair.of("/swagger-ui/index.html", "GET"),
                Pair.of("/swagger-api.html", "GET"),

                //Websocket
                Pair.of("/ws/**", "GET"),
                Pair.of("/ws/**", "POST"),
                Pair.of("/ws/**", "PUT"),
                Pair.of("/ws/**", "DELETE")
        );

        String requestPath = request.getServletPath();
        String requestMethod = request.getMethod();

        for (Pair<String, String> token : bypassTokens) {
            String path = token.getFirst();
            String method = token.getSecond();
            if (requestPath.matches(path.replace("**", ".*"))
                    && requestMethod.equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
    }
}
