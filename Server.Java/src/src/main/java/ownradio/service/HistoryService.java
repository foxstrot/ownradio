package ownradio.service;

import ownradio.domain.History;

/**
 * Интерфейс сервиса, для работы с историей прослушанных треков
 *
 * @author Alpenov Tanat
 */
public interface HistoryService {
	void save(History history);
}
