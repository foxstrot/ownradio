package ownradio.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ownradio.domain.Rating;
import ownradio.repository.RatingRepository;
import ownradio.service.RatingService;

@Service
public class RatingServiceImpl implements RatingService {

	private final RatingRepository ratingRepository;

	@Autowired
	public RatingServiceImpl(RatingRepository ratingRepository) {
		this.ratingRepository = ratingRepository;
	}


	@Override
	public void save(Rating rating) {
		ratingRepository.saveAndFlush(rating);
	}
}
