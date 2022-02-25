package hoangnd.web.app.security;

import java.text.ParseException;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
public class JwtUtils {

    private final JwtEncoder  encoder;
    private final JwtDecoder  decoder;
    private final long        expire;
    private final JWSVerifier jwsVerifier;

    public String generateJwtToken (final Authentication authentication) {
        Instant now = Instant.now();
        String scope = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(" "));
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("self")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(expire))
            .subject(authentication.getName())
            .claim("scope", scope)
            .build();
        return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public boolean validate (final String token) {
        try {
            var signedJWT = SignedJWT.parse(token);
            return signedJWT.verify(jwsVerifier);
        }
        catch (ParseException | JOSEException e) {
            return false;
        }
    }

    public User getUser (final String token) {
        Jwt decode = decoder.decode(token);
        var scope = decode.getClaimAsString("scope");
        return new User(decode.getSubject(), "",
                        Optional.ofNullable(scope)
                        .stream().flatMap(e -> Stream.of(e.split(" ")))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList()));
    }

}
