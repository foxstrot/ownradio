package ownradio.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ownradio.domain.User;
import ownradio.service.UserService;

/**
 * Класс инициализирует БД первоначальными данными
 *
 * @author Alpenov Tanat
 */
@Slf4j
@Profile("dev")
@Component
public class InitDb implements CommandLineRunner {
	@Autowired
	private UserService userService;

	@Override
	public void run(String... strings) throws Exception {
		User user = userService.save(new User());
		log.debug("User recid: {}", user.getRecid());
	}
}
