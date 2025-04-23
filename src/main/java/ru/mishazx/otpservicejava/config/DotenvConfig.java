package ru.mishazx.otpservicejava.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.File;
import java.io.IOException;

@Configuration
public class DotenvConfig {
    
    /**
     * Load .env file manually to ensure it's available
     */
    @Bean
    public ResourcePropertySource dotenvPropertySource(ConfigurableEnvironment environment) {
        try {
            // Check if .env file exists in resources
            Resource resource = new ClassPathResource(".env");
            if (resource.exists()) {
                System.out.println(".env file found in resources directory");
                ResourcePropertySource propertySource = new ResourcePropertySource("dotenv", resource);
                MutablePropertySources propertySources = environment.getPropertySources();
                propertySources.addFirst(propertySource);
                return propertySource;
            } else {
                System.err.println(".env file NOT found in resources directory");
                // Check if .env file exists in project root
                File rootEnv = new File(".env");
                if (rootEnv.exists()) {
                    System.out.println(".env file found in project root directory");
                    ResourcePropertySource propertySource = new ResourcePropertySource("dotenv", String.valueOf(rootEnv.toURI().toURL()));
                    environment.getPropertySources().addFirst(propertySource);
                    return propertySource;
                } else {
                    System.err.println(".env file NOT found in project root directory either");
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading .env file: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Log the loaded environment variables for debugging after context is refreshed
     */
    @EventListener
    public void handleContextRefreshed(ContextRefreshedEvent event) {
        ConfigurableEnvironment environment = (ConfigurableEnvironment) event.getApplicationContext().getEnvironment();
        System.out.println("=== Environment Variables After Context Refresh ===");
        System.out.println("MAIL_USERNAME: " + environment.getProperty("MAIL_USERNAME"));
        System.out.println("MAIL_HOST: " + environment.getProperty("MAIL_HOST"));
        System.out.println("TG_TOKEN: " + environment.getProperty("TG_TOKEN"));
        System.out.println("TG_BOT_NAME: " + environment.getProperty("TG_BOT_NAME"));
        System.out.println("SERVER_PORT: " + environment.getProperty("SERVER_PORT"));
        // Don't log sensitive information like passwords or tokens
    }
}
