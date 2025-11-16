package com.internship.auth_service.event;

import com.internship.auth_service.dto.RegisterRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreationEvent {
    private String sagaId;
    private String email;
    private String name;
    private String surname;
    private LocalDate birthDate;

    public static UserCreationEvent of(String sagaId, String email, String name, String surname, LocalDate birthDate) {
        UserCreationEvent event = new UserCreationEvent();
        event.setSagaId(sagaId);
        event.setEmail(email);
        event.setName(name);
        event.setSurname(surname);
        event.setBirthDate(birthDate);
        return event;
    }

    public static UserCreationEvent fromRegisterRequest(RegisterRequest request, String sagaId) {
        return UserCreationEvent.of(
                sagaId,
                request.email(),
                request.name(),
                request.surname(),
                request.birthDate()
        );
    }
}