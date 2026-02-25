package kz.h3project.service;

import kz.h3project.model.hospital.dto.HospitalDto;
import kz.h3project.model.hospital.entity.Hospital;
import kz.h3project.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HospitalService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final HospitalRepository hospitalRepository;

    /**
     * Больницы по городу, при переданных latitude/longitude — сортировка по расстоянию (ближайшие первые).
     */
    public List<HospitalDto> findHospitalsByCity(String city, Double latitude, Double longitude) {
        List<Hospital> hospitals = hospitalRepository.findByCityIgnoreCaseOrderByName(city);

        return hospitals.stream()
                .map(h -> toDto(h, latitude, longitude))
                .sorted(Comparator.comparing(dto -> dto.getDistanceKm() != null ? dto.getDistanceKm() : Double.MAX_VALUE))
                .collect(Collectors.toList());
    }

    private static HospitalDto toDto(Hospital h, Double userLat, Double userLon) {
        Double distanceKm = null;
        if (userLat != null && userLon != null && h.getLatitude() != null && h.getLongitude() != null) {
            distanceKm = haversineKm(userLat, userLon, h.getLatitude(), h.getLongitude());
        }
        return HospitalDto.builder()
                .id(h.getId())
                .name(h.getName())
                .city(h.getCity())
                .address(h.getAddress())
                .latitude(h.getLatitude())
                .longitude(h.getLongitude())
                .distanceKm(distanceKm)
                .build();
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
