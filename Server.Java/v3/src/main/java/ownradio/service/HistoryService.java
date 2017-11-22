package ownradio.service;

import ownradio.domain.History;
import ownradio.domain.HistoryArray;

import java.util.UUID;

/**
 * Интерфейс сервиса, для работы с историей прослушанных треков
 *
 * @author Alpenov Tanat
 */
public interface HistoryService {
	void save(History history, Boolean isNewHistoryRec);

	void save2(History history);

	History getById(UUID uuid);
}
