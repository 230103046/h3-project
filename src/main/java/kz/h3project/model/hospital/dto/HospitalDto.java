package kz.h3project.model.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalDto {

    private Long id;
    private String name;
    private String city;
    private String address;
    private Double latitude;
    private Double longitude;
    private Double distanceKm; // опционально, если передан пункт (lat/lon)
}
