package ownradio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ownradio.domain.User;

import java.util.List;
import java.util.UUID;

/**
 * Интерфейс репозитория, для хранения данных пользователя
 *
 * @author Alpenov Tanat
 */
public interface UserRepository extends JpaRepository<User, UUID> {

	@Query(value = "select * from getusersrating(?1)", nativeQuery = true)
	List<Object[]> getUsersRating(Integer countRows);

	@Query(value = "select * from getlastusers(?1)", nativeQuery = true)
	List<Object[]> getLastUsers(Integer countRows);
}
