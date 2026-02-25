package kz.h3project.model.hospital.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateAppointmentRequest {

    @NotNull(message = "hospitalId обязателен")
    private Long hospitalId;

    @NotNull(message = "scheduledAt обязателен")
    private LocalDateTime scheduledAt;
}
