package ownradio.service;

import ownradio.domain.Rating;

/**
 * Интерфейс сервиса, для работы с рейтингами прослушанных треков
 *
 * @author Alpenov Tanat
 */
public interface RatingService {
	void save(Rating rating);
}
