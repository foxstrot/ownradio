package ownradio;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Главный класс приложения
 *
 * @author Alpenov Tanat
 */
@Slf4j
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		final ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

		if (context.getEnvironment().getActiveProfiles().length > 0 &&
				"dev".equals(context.getEnvironment().getActiveProfiles()[0])) {
			log.info("Open in browser: " + context.getEnvironment().getProperty("this-url"));
		}
	}
}
