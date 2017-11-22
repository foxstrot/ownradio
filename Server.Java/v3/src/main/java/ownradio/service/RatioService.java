package ownradio.service;

import ownradio.domain.Ratio;

import java.util.UUID;

/**
 *  Интерфейс сервиса, для работы с коэффициентами схожести интересов пользователей
 *
 * Created by a.polunina on 16.02.2017.
 */
public interface RatioService {
	void save (Ratio ratio);

	void updateRatios(UUID deviceId);
}
