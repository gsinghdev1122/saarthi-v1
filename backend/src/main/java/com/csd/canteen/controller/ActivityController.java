package com.csd.canteen.controller;

import com.csd.canteen.dto.response.ActivityResponse;
import com.csd.canteen.service.ActivityService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
@Tag(name = "Activity")
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping
    public List<ActivityResponse> recent() {
        return activityService.listRecent();
    }
}
