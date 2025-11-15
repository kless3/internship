package com.internship.auth_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreationRollbackEvent {
    private String sagaId;
    private String email;
    private String reason;

    public static UserCreationRollbackEvent of(String sagaId, String email, String reason) {
        UserCreationRollbackEvent event = new UserCreationRollbackEvent();
        event.setSagaId(sagaId);
        event.setEmail(email);
        event.setReason(reason);
        return event;
    }
}