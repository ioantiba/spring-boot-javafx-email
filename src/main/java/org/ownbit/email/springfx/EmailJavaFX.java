package org.ownbit.email.springfx;

import java.io.File;

import org.ownbit.email.springfx.view.EmailView;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;

@SpringBootApplication
@Configuration
@Log4j2
public class EmailJavaFX extends AbstractJavaFxApplicationSupport {

	@Override
	public void start(Stage stage) throws Exception {
		super.start(stage);
	}
	
	public static void main(String[] args) {
		launch(EmailJavaFX.class, EmailView.class, args);
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();

		File jarPath = new File(EmailJavaFX.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		String emailExternal = new File(jarPath.getParentFile().getParent()).getParent() + "/email.properties";
		FileSystemResource externalEmailProperties = new FileSystemResource(emailExternal.replaceAll("file:", ""));
		log.debug("trying to load external email properties: " + externalEmailProperties.getPath());

		configurer.setLocations(new ClassPathResource("application.yaml"), externalEmailProperties);
		configurer.setIgnoreUnresolvablePlaceholders(true);
		configurer.setIgnoreResourceNotFound(true);
		return configurer;
	}
}
