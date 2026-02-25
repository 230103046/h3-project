package kz.h3project.model.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HospitalCreateUpdateDto {

    @NotBlank
    private String name;
    @NotBlank
    private String city;
    private String address;
    private Double latitude;
    private Double longitude;
}
