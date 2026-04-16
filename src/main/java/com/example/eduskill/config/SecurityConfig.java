package com.example.eduskill.config;
import com.example.eduskill.dto.ApiError;
import com.example.eduskill.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;


import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity

public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthanticationFilter;


    public SecurityConfig(JwtAuthenticationFilter jwtAuthanticationFilter) {
        this.jwtAuthanticationFilter = jwtAuthanticationFilter;

    }

    @Bean
    public PasswordEncoder passwordEncoder(){

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http){
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )


         .authorizeHttpRequests(authorizeHttpRequest->
                        authorizeHttpRequest
                                .anyRequest().permitAll()


                )
                //Oauth2 login

                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex->ex.authenticationEntryPoint((request, response, authException ) -> {
                    //error message you are auothorize force to grab the api
                    //authException.printStackTrace();
                    response.setStatus(401);
                    response.setContentType("application/json");

                    String message="Unauthorized Access!"+authException.getMessage();
                    String error=(String) request.getAttribute("error");
                    if(error!=null){
                        message=error;
                    }

                    //  Map<String,String> errorMap=Map.of("message",message,"statusCode",Integer.toString(401));
                    var apiError= ApiError.of(HttpStatus.UNAUTHORIZED.value(), "Unautorized Access !!!",message,request.getRequestURI());
                    var objectMapper=new ObjectMapper();
                    response.getWriter().write(objectMapper.writeValueAsString(apiError));
                }))
                .addFilterBefore(jwtAuthanticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration){
        return configuration.getAuthenticationManager();
    }
    //CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.front_url}")String corsUrl
    ){

        String[] urls=corsUrl.trim().split(",");

        var config=new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(urls));
        config.setAllowedMethods(List.of("GET","POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));//in production not all everhing in headers
        config.setAllowCredentials(true);//for cookies

        var source=new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",config);
        return source;
    }
}
