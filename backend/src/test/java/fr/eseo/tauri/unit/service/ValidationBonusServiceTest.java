package fr.eseo.tauri.unit.service;

import fr.eseo.tauri.model.Bonus;
import fr.eseo.tauri.model.User;
import fr.eseo.tauri.model.ValidationBonus;
import fr.eseo.tauri.repository.ValidationBonusRepository;
import fr.eseo.tauri.service.AuthService;
import fr.eseo.tauri.service.BonusService;
import fr.eseo.tauri.service.UserService;
import fr.eseo.tauri.service.ValidationBonusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidationBonusServiceTest {


    @Mock
    ValidationBonusRepository validationBonusRepository;

    @Mock
    UserService userService;

    @Mock
    BonusService bonusService;

    @InjectMocks
    ValidationBonusService validationBonusService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getValidationBonusByAuthorIdShouldReturnBonusWhenAuthorized() {
        when(validationBonusRepository.findByAuthorIdAndBonusId(anyInt(), anyInt())).thenReturn(new ValidationBonus());

        ValidationBonus result = validationBonusService.getValidationBonusByAuthorId("token", 1, 1);

        assertNotNull(result);
    }

    @Test
    void getAllValidationBonusesShouldReturnBonusesWhenAuthorized() {
        when(validationBonusRepository.findAllByBonusId(anyInt())).thenReturn(Arrays.asList(new ValidationBonus(), new ValidationBonus()));

        List<ValidationBonus> result = validationBonusService.getAllValidationBonuses("token", 1);

        assertEquals(2, result.size());
    }

    @Test
    void getAllValidationBonusesShouldReturnEmptyListWhenNoBonuses() {
        when(validationBonusRepository.findAllByBonusId(anyInt())).thenReturn(Collections.emptyList());

        List<ValidationBonus> result = validationBonusService.getAllValidationBonuses("token", 1);

        assertTrue(result.isEmpty());
    }

    @Test
    void createValidationBonusShouldSaveBonusWhenAuthorized() {
        when(bonusService.getBonusById(anyInt())).thenReturn(new Bonus());
        when(userService.getUserById(anyInt())).thenReturn(new User());
        ValidationBonus validationBonus = new ValidationBonus();
        validationBonus.bonusId(1);
        validationBonus.authorId(1);

        validationBonusService.createValidationBonus("token", validationBonus);

        verify(validationBonusRepository, times(1)).save(any(ValidationBonus.class));
    }

    @Test
    void deleteAllValidationBonusesShouldDeleteBonusesWhenAuthorized() {
        validationBonusService.deleteAllValidationBonuses("token", 1);

        verify(validationBonusRepository, times(1)).deleteAllByBonusId(anyInt());
    }
}
