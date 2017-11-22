package ownradio.service;

import ownradio.domain.User;
import ownradio.domain.UsersRating;

import java.util.List;
import java.util.UUID;

/**
 * Интерфейс сервиса, для работы с пользовательскими данными
 *
 * @author Alpenov Tanat
 */
public interface UserService {
	User getById(UUID id);

	User save(User user);

	List<UsersRating> getUsersRating(Integer countRows);

	List<UsersRating> getLastUsers(Integer countRows);
}
