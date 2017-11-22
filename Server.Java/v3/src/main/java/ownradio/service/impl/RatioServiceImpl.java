package ownradio.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ownradio.domain.Ratio;
import ownradio.repository.RatioRepository;
import ownradio.service.RatioService;

import java.util.UUID;

/**
 * Created by a.polunina on 16.02.2017.
 */
public class RatioServiceImpl implements RatioService{

	private final RatioRepository ratioRepository;

	@Autowired
	public RatioServiceImpl(RatioRepository ratioRepository) {
		this.ratioRepository = ratioRepository;
	}

	@Override
	public void save(Ratio ratio) {
		ratioRepository.saveAndFlush(ratio);
	}

	@Override
	public void updateRatios(UUID deviceId){
		try {
			ratioRepository.updateRatios(deviceId);
		}catch (Exception ex){
			ex.printStackTrace();
		}

	}
}
