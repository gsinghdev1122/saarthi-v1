package com.csd.canteen.service;

import com.csd.canteen.dto.response.ActivityResponse;
import com.csd.canteen.mapper.CanteenMapper;
import com.csd.canteen.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor

public class ActivityService {

	private final ActivityRepository activityRepository;
	private final CanteenMapper mapper;

	@Transactional(readOnly = true)
	public List<ActivityResponse> listRecent() {
		return activityRepository.findAllByOrderByTimestampDesc(PageRequest.of(0, 12))
				.stream()
				.map(mapper::toResponse)
				.toList();
	}
}
