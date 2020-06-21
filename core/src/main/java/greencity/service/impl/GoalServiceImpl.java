package greencity.service.impl;

import greencity.dto.goal.GoalDto;
import greencity.dto.user.UserGoalResponseDto;
import greencity.entity.CustomGoal;
import greencity.entity.Goal;
import greencity.entity.UserGoal;
import greencity.exception.exceptions.GoalNotFoundException;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.CustomGoalRepo;
import greencity.repository.GoalRepo;
import greencity.repository.GoalTranslationRepo;
import greencity.service.GoalService;
import greencity.service.LanguageService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import static greencity.constant.ErrorMessage.CUSTOM_GOAL_NOT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.GOAL_NOT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.GOAL_NOT_FOUND_BY_LANGUAGE_CODE;

@RequiredArgsConstructor
@Service
public class GoalServiceImpl implements GoalService {
    private final GoalTranslationRepo goalTranslationRepo;
    private final CustomGoalRepo customGoalRepo;
    private final GoalRepo goalRepo;
    private final ModelMapper modelMapper;
    private final LanguageService languageService;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GoalDto> findAll(String language) {
        return goalTranslationRepo
            .findAllByLanguageCode(language)
            .stream()
            .map(g -> modelMapper.map(g, GoalDto.class))
            .collect(Collectors.toList());
    }

    @Override
    public UserGoalResponseDto getUserGoalResponseDtoFromPredefinedGoal(UserGoal userGoal) {
        UserGoalResponseDto userGoalResponseDto = modelMapper.map(userGoal, UserGoalResponseDto.class);
        String languageCode = languageService.extractLanguageCodeFromRequest();
        if (userGoal.getCustomGoal() == null) {
            Goal goal = goalRepo
                .findById(userGoal
                    .getGoal().getId()).orElseThrow(() -> new GoalNotFoundException(GOAL_NOT_FOUND_BY_ID));
            userGoalResponseDto.setText(goalTranslationRepo.findByGoalAndLanguageCode(goal, languageCode)
                .orElseThrow(() -> new GoalNotFoundException(GOAL_NOT_FOUND_BY_LANGUAGE_CODE)).getText());
        }
        return userGoalResponseDto;
    }

    @Override
    public UserGoalResponseDto getUserGoalResponseDtoFromCustomGoal(UserGoal userGoal) {
        UserGoalResponseDto userGoalResponseDto = modelMapper.map(userGoal, UserGoalResponseDto.class);
        if (userGoal.getGoal() == null) {
            CustomGoal customGoal = customGoalRepo.findById(userGoal
                .getCustomGoal().getId()).orElseThrow(() -> new NotFoundException(CUSTOM_GOAL_NOT_FOUND_BY_ID));
            userGoalResponseDto.setText(customGoal.getText());
        }
        return userGoalResponseDto;
    }
}