package hoangnd.web.app.security.filter;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.core.util.IOUtils;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hoangnd.web.app.domain.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
@SuppressFBWarnings({ "EI_EXPOSE_REP2", "EI_EXPOSE_REP" })
public class RequestBodyReaderAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private static final String ERROR_MESSAGE = "Something went wrong while parsing /login request body";

    private final ObjectMapper objectMapper;

    @Override
    public Authentication attemptAuthentication (final HttpServletRequest request, final HttpServletResponse response)
        throws AuthenticationException {
        String requestBody;
        try {
            requestBody = IOUtils.toString(request.getReader());
            LoginRequest authRequest = objectMapper.readValue(requestBody, LoginRequest.class);

            UsernamePasswordAuthenticationToken token
                = new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password());

            setDetails(request, token);

            return this.getAuthenticationManager().authenticate(token);
        }
        catch (IOException e) {
            log.error(ERROR_MESSAGE, e);
            throw new InternalAuthenticationServiceException(ERROR_MESSAGE, e);
        }
    }
}
