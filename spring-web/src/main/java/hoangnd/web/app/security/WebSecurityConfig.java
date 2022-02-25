package hoangnd.web.app.security;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hoangnd.web.app.domain.dto.JsonToken;
import hoangnd.web.app.security.filter.JwtTokenFilter;
import hoangnd.web.app.security.filter.RequestBodyReaderAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true,
    prePostEnabled = true)
@RequiredArgsConstructor
@Log4j2
@SuppressFBWarnings({ "EI_EXPOSE_REP2", "EI_EXPOSE_REP" })
@Profile(value = {"dev", "production"})
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;
    private final ObjectMapper       objectMapper;

    @Value("${app.allow.mvc-paths:[]}")
    private final String[] mvcPaths;

    @Value("${jwt.public.key:}")
    private final RSAPublicKey publicKey;

    @Value("${jwt.private.key:}")
    private final RSAPrivateKey privateKey;

    @Value("${jwt.expire:36000}")
    private final long expire;

    @PostConstruct
    void init () {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @Bean
    JwtDecoder jwtDecoder () {
        return NimbusJwtDecoder.withPublicKey(this.publicKey).build();
    }

    @Bean
    JwtEncoder jwtEncoder () {
        var jwk = new RSAKey.Builder(this.publicKey).privateKey(this.privateKey).build();
        var jwkSet = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwkSet);
    }

    @Bean
    JWSVerifier verifier () {
        return new RSASSAVerifier(publicKey);
    }

    @Bean
    JwtUtils jwtUtils () {
        return new JwtUtils(jwtEncoder(), jwtDecoder(), expire, verifier());
    }


    @Bean
    PasswordEncoder passwordEncoder () {
        return new BCryptPasswordEncoder();
    }

    JwtTokenFilter jwtTokenFilter () {
        return new JwtTokenFilter(jwtUtils());
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean () throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure (final AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public RequestBodyReaderAuthenticationFilter authenticationFilter () throws Exception {
        RequestBodyReaderAuthenticationFilter authenticationFilter
            = new RequestBodyReaderAuthenticationFilter(this.objectMapper);
        authenticationFilter.setAuthenticationSuccessHandler(this::loginSuccessHandler);
        authenticationFilter.setAuthenticationFailureHandler(this::loginFailureHandler);
        authenticationFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/login", "POST"));
        authenticationFilter.setAuthenticationManager(authenticationManagerBean());
        return authenticationFilter;
    }


    @Override
    protected void configure (final HttpSecurity http) throws Exception {
        // @formatter:off
        http
            .csrf()
            .disable() //We don't need CSRF for this example

            // Set session management to stateless
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

            // Set permissions on endpoints
            .and()
            .authorizeRequests()
            .mvcMatchers(mvcPaths).permitAll()
            .anyRequest().authenticated()
            // public endpoint

            // all request requires a logged in user
            .and()
            .logout()
            .logoutUrl("/logout") //the URL on which the clients should post if they want to logout
            .logoutSuccessHandler(this::logoutSuccessHandler)
            .invalidateHttpSession(true)
            .and()
            .exceptionHandling() //default response if the client wants to get a resource unauthorized
            .authenticationEntryPoint(
                (request, response, ex) -> {
                    log.error("Unauthorized request - {}", ex.getMessage());
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
                }
            )
            .and()
            .addFilterBefore(jwtTokenFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(authenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        // @formatter:on

    }

    private void loginSuccessHandler (
        final HttpServletRequest request,
        final HttpServletResponse response,
        final Authentication authentication) throws IOException {

        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json");

        String token = this.jwtUtils().generateJwtToken(authentication);
        var json = new JsonToken(token);
        objectMapper.writeValue(response.getWriter(), json);
    }

    private void loginFailureHandler (
        final HttpServletRequest request,
        final HttpServletResponse response,
        final AuthenticationException e) throws IOException {

        log.error(e);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        objectMapper.writeValue(response.getWriter(), "Nopity nop!");
    }

    private void logoutSuccessHandler (
        final HttpServletRequest request,
        final HttpServletResponse response,
        final Authentication authentication) throws IOException {

        response.setStatus(HttpStatus.OK.value());
        objectMapper.writeValue(response.getWriter(), "Bye!");
    }


}
