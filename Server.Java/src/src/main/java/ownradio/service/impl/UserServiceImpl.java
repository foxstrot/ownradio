package ownradio.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ownradio.domain.User;
import ownradio.repository.UserRepository;
import ownradio.service.UserService;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
	@Autowired
	private UserRepository userRepository;

	@Override
	public User getById(UUID id) {
		return userRepository.findOne(id);
	}

	@Override
	public User save(User user) {
		return userRepository.saveAndFlush(user);
	}
}
