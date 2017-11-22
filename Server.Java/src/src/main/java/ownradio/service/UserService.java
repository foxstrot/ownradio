package ownradio.service;

import ownradio.domain.User;

import java.util.UUID;

/**
 * Интерфейс сервиса, для работы с пользовательскими данными
 *
 * @author Alpenov Tanat
 */
public interface UserService {
	User getById(UUID id);

	User save(User user);
}
