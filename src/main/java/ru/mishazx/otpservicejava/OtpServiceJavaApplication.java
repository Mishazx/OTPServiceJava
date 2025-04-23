package ru.mishazx.otpservicejava;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OtpServiceJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(OtpServiceJavaApplication.class, args);
    }
}

//        try {@SpringBootApplication
//public class OtpServiceJavaApplication implements ApplicationContextInitializer<ConfigurableApplicationContext> {
//
//    public static void main(String[] args) {
//        SpringApplication app = new SpringApplication(OtpServiceJavaApplication.class);
//        app.addInitializers(new OtpServiceJavaApplication());
//        app.run(args);
//    }
//
//    @Override
//    public void initialize(ConfigurableApplicationContext applicationContext) {
//        ConfigurableEnvironment environment = applicationContext.getEnvironment();
//
//    }
//            // Check if .env file exists
//            File dotEnvFile = new File(".env");
//            if (dotEnvFile.exists()) {
//                // Load .env file
//                Dotenv dotenv = Dotenv.configure().load();
//                Properties props = new Properties();
//
//                // Add mail properties that we need
//                if (dotenv.get("MAIL_USERNAME") != null) {
//                    props.put("MAIL_USERNAME", dotenv.get("MAIL_USERNAME"));
//                }
//                if (dotenv.get("MAIL_PASSWORD") != null) {
//                    props.put("MAIL_PASSWORD", dotenv.get("MAIL_PASSWORD"));
//                }
//                if (dotenv.get("MAIL_HOST") != null) {
//                    props.put("MAIL_HOST", dotenv.get("MAIL_HOST"));
//                }
//                if (dotenv.get("MAIL_PORT") != null) {
//                    props.put("MAIL_PORT", dotenv.get("MAIL_PORT"));
//                }
//
//                // Add other properties as needed
//
//                // Add properties to environment
//                environment.getPropertySources().addFirst(new PropertiesPropertySource("dotenv", props));
//                System.out.println("Loaded environment variables from .env file");
//            } else {
//                System.out.println(".env file not found. Using default or system environment variables.");
//            }
//        } catch (Exception e) {
//            System.err.println("Error loading .env file: " + e.getMessage());
//        }
//    }
//}
