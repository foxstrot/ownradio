package ownradio.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ownradio.domain.History;
import ownradio.domain.Rating;
import ownradio.repository.HistoryRepository;
import ownradio.repository.RatingRepository;
import ownradio.service.HistoryService;

@Service
public class HistoryServiceImpl implements HistoryService {

	private final HistoryRepository historyRepository;
	private final RatingRepository ratingRepository;

	@Autowired
	public HistoryServiceImpl(HistoryRepository historyRepository, RatingRepository ratingRepository) {
		this.historyRepository = historyRepository;
		this.ratingRepository = ratingRepository;
	}

	@Transactional
	@Override
	public void save(History history) {
		historyRepository.saveAndFlush(history);

		Rating rating = ratingRepository.findByUserAndTrack(history.getDevice().getUser(), history.getTrack());
		if(rating != null) {
			int ratingsum = rating.getRatingsum() + history.getIsListen();
			rating.setLastlisten(history.getLastListen());
			rating.setRatingsum(ratingsum);
			ratingRepository.saveAndFlush(rating);
		}
		else {
			rating = new Rating();
			rating.setUser(history.getDevice().getUser());
			rating.setTrack(history.getTrack());
			rating.setLastlisten(history.getLastListen());
			rating.setRatingsum(history.getIsListen());
			ratingRepository.saveAndFlush(rating);
		}
	}
}
