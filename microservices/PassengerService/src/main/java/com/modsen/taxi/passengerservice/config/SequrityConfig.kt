package com.modsen.taxi.passengerservice.config

import com.modsen.taxi.passengerservice.error.CustomAuthenticationEntryPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain


@Configuration
@EnableMethodSecurity
open class SecurityConfig(
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint
) {

    @Bean
    open fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt(Customizer.withDefaults())
                    .authenticationEntryPoint(customAuthenticationEntryPoint)
            }
            .authorizeHttpRequests {authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/actuator/prometheus").permitAll()
                    .anyRequest().authenticated()

            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .build()
    }

    @Bean
    open fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val converter = JwtAuthenticationConverter()
        val authoritiesConverter = JwtGrantedAuthoritiesConverter()
        converter.setPrincipalClaimName("preferred_username")
        converter.setJwtGrantedAuthoritiesConverter { jwt ->
            val auth = authoritiesConverter.convert(jwt)
            val roles = (jwt.claims["realm_access"] as? Map<*, *>)?.get("roles") as? List<String>
            roles?.filter { it.startsWith("ROLE_") }?.map { SimpleGrantedAuthority(it) }?.toSet()
                ?: (emptySet<GrantedAuthority>() + auth!!)
        }
        return converter
    }
}
