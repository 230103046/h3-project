package kz.h3project.model.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateAppointmentStatusRequest {

    @NotBlank(message = "status обязателен")
    private String status;
}
