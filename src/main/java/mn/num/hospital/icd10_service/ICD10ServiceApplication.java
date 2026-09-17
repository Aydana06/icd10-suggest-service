package mn.num.hospital.icd10_service;

import java.awt.GraphicsEnvironment;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import lombok.extern.slf4j.Slf4j;
import mn.num.hospital.icd10_service.repo.ChapterRepository;
import mn.num.hospital.icd10_service.service.DiagnosisService;
import mn.num.hospital.icd10_service.ui.DiagnosisSearchUI;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
@Slf4j
public class ICD10ServiceApplication {

	@Value("${DESKTOP_UI_ENABLED:false}")
	private static final boolean desktopUiEnabled = false;

	private static void maybeLaunchDesktopUi(ConfigurableApplicationContext context) {
	    ICD10ServiceApplication self =
	            context.getBean(ICD10ServiceApplication.class);

	    if (GraphicsEnvironment.isHeadless()) {
	        log.info("Headless орчин илрэв — Swing desktop UI алгасав.");
	        return;
	    }

	    if (!self.desktopUiEnabled) {
	        log.info("Swing desktop UI идэвхгүй.");
	        return;
	    }

	    var service = context.getBean(DiagnosisService.class);
	    var chapterRepo = context.getBean(ChapterRepository.class);

	    javax.swing.SwingUtilities.invokeLater(() ->
	            new DiagnosisSearchUI(service, chapterRepo)
	    );
	}
	
	public static void main(String[] args) {
	    System.setProperty("java.awt.headless", "false");

	    var context = SpringApplication.run(ICD10ServiceApplication.class, args);

	    log.info("ICD-10 Diagnosis Service эхэллээ...");
	    log.info("API документ: http://localhost:8080/api/swagger-ui.html");
	    log.info("Health check: http://localhost:8080/api/diagnosis/health");

	    maybeLaunchDesktopUi(context);
	}

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
    
    @Bean
    public ObjectMapper objectMapper() {
        return  new ObjectMapper();
    }
    
}