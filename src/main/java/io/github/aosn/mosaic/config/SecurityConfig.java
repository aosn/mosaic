/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.config;

import io.github.aosn.mosaic.domain.model.auth.User;
import io.github.aosn.mosaic.domain.service.auth.UserService;
import io.github.aosn.mosaic.ui.ErrorUI;
import io.github.aosn.mosaic.ui.MainUI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.CompositeFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Spring Security configurations.
 *
 * @author mikan
 * @see <a href="https://github.com/spring-guides/tut-spring-boot-oauth2/tree/master/github">tut-spring-boot-oauth2</a>
 * @since 0.1
 */
@Configuration
@EnableWebSecurity
@EnableOAuth2Client
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String LOGIN_PATH_GITHUB = "/login/github";
    public static final String CSS_PATH = "/css/mosaic.css";
    private final OAuth2ClientContext oauth2ClientContext;
    private final UserService userService;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public SecurityConfig(OAuth2ClientContext oauth2ClientContext, UserService userService) {
        this.oauth2ClientContext = oauth2ClientContext;
        this.userService = userService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable(); // Use Vaadin's built-in CSRF protection instead
        http.httpBasic().disable();
        http.formLogin().disable();
        http.authorizeRequests()
                .antMatchers(MainUI.PATH, ErrorUI.PATH, CSS_PATH, LOGIN_PATH_GITHUB).permitAll()
                .antMatchers("/VAADIN/**", "/vaadinServlet/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .exceptionHandling().authenticationEntryPoint(loginPage()).accessDeniedPage(MainUI.PATH)
                .and()
                .logout().logoutSuccessUrl(MainUI.PATH).permitAll()
                .and()
                .addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class);
    }

    private LoginUrlAuthenticationEntryPoint loginPage() {
        return new LoginUrlAuthenticationEntryPoint(MainUI.PATH);
    }

    private Filter ssoFilter() {
        CompositeFilter filter = new CompositeFilter();
        List<Filter> filters = new ArrayList<>();
        filters.add(ssoFilter(github(), LOGIN_PATH_GITHUB));
        filter.setFilters(filters);
        return filter;
    }

    private Filter ssoFilter(ClientResources client, String path) {
        OAuth2ClientAuthenticationProcessingFilter filter = new OAuth2ClientAuthenticationProcessingFilter(path) {
            @Override
            protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                                    FilterChain chain, Authentication authResult)
                    throws IOException, ServletException {
                super.successfulAuthentication(request, response, chain, authResult);
                userService.recordLogin(authResult, User.Source.GITHUB);
            }
        };
        OAuth2RestTemplate oAuth2RestTemplate = new OAuth2RestTemplate(client.getClient(), oauth2ClientContext);
        filter.setRestTemplate(oAuth2RestTemplate);
        UserInfoTokenServices tokenServices = new UserInfoTokenServices(client.getResource().getUserInfoUri(),
                client.getClient().getClientId());
        tokenServices.setRestTemplate(oAuth2RestTemplate);
        filter.setTokenServices(tokenServices);
        return filter;
    }

    @Bean
    public FilterRegistrationBean oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        registration.setOrder(-100);
        return registration;
    }

    @Bean
    @ConfigurationProperties("github")
    public ClientResources github() {
        return new ClientResources();
    }

    private static class ClientResources {

        @NestedConfigurationProperty
        private AuthorizationCodeResourceDetails client = new AuthorizationCodeResourceDetails();

        @NestedConfigurationProperty
        private ResourceServerProperties resource = new ResourceServerProperties();

        @SuppressWarnings("WeakerAccess")
        public AuthorizationCodeResourceDetails getClient() {
            return client;
        }

        @SuppressWarnings("WeakerAccess")
        public ResourceServerProperties getResource() {
            return resource;
        }
    }
}
