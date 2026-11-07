package com.shreejewellers.billing.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.web.SecurityFilterChain;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;
@Configuration
public class SecurityConfig {
 @Bean JwtDecoder jwtDecoder(@Value("${app.jwt.secret}") String secret,@Value("${app.jwt.issuer}") String issuer){
  if(secret.getBytes(StandardCharsets.UTF_8).length<32) throw new IllegalStateException("JWT_SECRET must be at least 32 UTF-8 bytes");
  NimbusJwtDecoder decoder=NimbusJwtDecoder.withSecretKey(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256")).macAlgorithm(MacAlgorithm.HS256).build();
  decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
  return decoder;
 }
 @Bean SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
  JwtAuthenticationConverter converter=new JwtAuthenticationConverter();
  converter.setJwtGrantedAuthoritiesConverter(jwt->{
   List<GrantedAuthority> authorities=new ArrayList<>();
   Object claim=jwt.getClaims().get("roles");
   if(claim instanceof Collection<?> roles) for(Object role:roles){
    String value=String.valueOf(role);
    authorities.add(new SimpleGrantedAuthority(value.startsWith("ROLE_")?value:"ROLE_"+value));
   }
   return authorities;
  });
  return http.csrf(AbstractHttpConfigurer::disable)
   .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(auth->auth
    .requestMatchers(HttpMethod.GET,"/api/billing/**").hasAnyRole("ADMIN","MANAGER","CASHIER")
    .requestMatchers(HttpMethod.POST,"/api/billing/invoices/*/cancel").hasAnyRole("ADMIN","MANAGER")
    .requestMatchers(HttpMethod.POST,"/api/billing/**").hasAnyRole("ADMIN","MANAGER","CASHIER")
    .anyRequest().denyAll())
   .oauth2ResourceServer(oauth->oauth.jwt(jwt->jwt.jwtAuthenticationConverter(converter)))
   .build();
 }
}
