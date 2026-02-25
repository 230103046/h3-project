package kz.h3project.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentCreatedEvent {

    private Long appointmentId;
    private Long userId;
    private Long hospitalId;
    private String hospitalName;
    private LocalDateTime scheduledAt;
    private LocalDateTime createdAt;
}
