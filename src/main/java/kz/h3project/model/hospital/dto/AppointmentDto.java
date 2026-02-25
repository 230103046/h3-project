package kz.h3project.model.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentDto {

    private Long id;
    private Long hospitalId;
    private String hospitalName;
    private LocalDateTime scheduledAt;
    private String status;
    private LocalDateTime createdAt;
}
