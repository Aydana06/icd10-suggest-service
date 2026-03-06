package mn.num.hospital.icd10_service;

import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class ICD10ServiceApplication implements CommandLineRunner {
    
    public static void main(String[] args) {
        SpringApplication.run(ICD10ServiceApplication.class, args);
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
    
    @Override
    public void run(String... args) throws Exception {
        log.info("ICD-10 Diagnosis Service эхэллээ...");
        log.info("API документ: http://localhost:8080/api/swagger-ui.html");
        log.info("Health check: http://localhost:8080/api/diagnosis/health");
    }
}