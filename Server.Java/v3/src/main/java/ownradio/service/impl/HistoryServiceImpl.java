package ownradio.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ownradio.domain.History;
import ownradio.domain.Rating;
import ownradio.repository.HistoryRepository;
import ownradio.repository.RatingRepository;
import ownradio.repository.RatioRepository;
import ownradio.service.HistoryService;

import java.util.UUID;

@Service
public class HistoryServiceImpl implements HistoryService {

	private final HistoryRepository historyRepository;
	private final RatingRepository ratingRepository;
	private final RatioRepository ratioRepository;

	@Autowired
	public HistoryServiceImpl(HistoryRepository historyRepository, RatingRepository ratingRepository, RatioRepository ratioRepository) {
		this.historyRepository = historyRepository;
		this.ratingRepository = ratingRepository;
		this.ratioRepository = ratioRepository;
	}

	@Override
	public History getById(UUID uuid) {
		return historyRepository.findOne(uuid);
	}

	@Transactional
	@Override
	public void save(History history, Boolean isNewHistoryRec) {
		historyRepository.saveAndFlush(history);

		if(isNewHistoryRec) {
			Rating rating = ratingRepository.findByUserAndTrack(history.getDevice().getUser(), history.getTrack());
			if (rating != null) {
				int ratingsum = rating.getRatingsum() + history.getIsListen();
				rating.setLastlisten(history.getLastListen());
				rating.setRatingsum(ratingsum);
				ratingRepository.saveAndFlush(rating);
			} else {
				rating = new Rating();
				rating.setUser(history.getDevice().getUser());
				rating.setTrack(history.getTrack());
				rating.setLastlisten(history.getLastListen());
				rating.setRatingsum(history.getIsListen());
				ratingRepository.saveAndFlush(rating);
			}

			try {
				ratioRepository.updateRatios(history.getDevice().getRecid());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void save2(History history) {
		historyRepository.saveAndFlush(history);
	}
}
