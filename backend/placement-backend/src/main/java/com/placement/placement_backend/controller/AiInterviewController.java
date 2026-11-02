package com.placement.placement_backend.controller;

import com.placement.placement_backend.dto.InterviewFeedbackRequest;
import com.placement.placement_backend.dto.InterviewFeedbackResponse;
import com.placement.placement_backend.service.InterviewFeedbackService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiInterviewController {
    private final InterviewFeedbackService feedbackService;

    public AiInterviewController(InterviewFeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/interview-feedback")
    public InterviewFeedbackResponse review(@Valid @RequestBody InterviewFeedbackRequest request) {
        return new InterviewFeedbackResponse(
                feedbackService.review(request.role(), request.question(), request.answer()));
    }
}
