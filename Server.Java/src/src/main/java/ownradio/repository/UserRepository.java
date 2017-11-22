package ownradio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ownradio.domain.User;

import java.util.UUID;

/**
 * Интерфейс репозитория, для хранения данных пользователя
 *
 * @author Alpenov Tanat
 */
public interface UserRepository extends JpaRepository<User, UUID> {
}
