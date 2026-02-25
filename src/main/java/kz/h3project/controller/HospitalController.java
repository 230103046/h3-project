package kz.h3project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.h3project.model.hospital.dto.AppointmentDto;
import kz.h3project.model.hospital.dto.CreateAppointmentRequest;
import kz.h3project.model.hospital.dto.HospitalDto;
import kz.h3project.model.hospital.entity.Appointment;
import kz.h3project.model.hospital.entity.Hospital;
import kz.h3project.model.user.data.TokenPrincipal;
import kz.h3project.model.user.entity.User;
import kz.h3project.repository.AppointmentRepository;
import kz.h3project.repository.HospitalRepository;
import kz.h3project.repository.UserRepository;
import kz.h3project.service.AppointmentEventProducer;
import kz.h3project.service.HospitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Hospitals API", description = "Больницы и запись на приём")
@RequestMapping("/hospitals")
@RestController
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;
    private final HospitalRepository hospitalRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final AppointmentEventProducer appointmentEventProducer;

    @Operation(summary = "Список больниц по городу", description = "При передаче latitude/longitude возвращаются ближайшие первыми")
    @PreAuthorize("@authorization.hasAccessToReadHospital()")
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<HospitalDto>> getHospitalsByCity(
            @RequestParam String city,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {
        List<HospitalDto> hospitals = hospitalService.findHospitalsByCity(city, latitude, longitude);
        return ResponseEntity.ok(hospitals);
    }

    @Operation(summary = "Запись на приём")
    @PreAuthorize("@authorization.hasAccessToWriteAppointment()")
    @PostMapping("/appointments")
    @Transactional
    public ResponseEntity<AppointmentDto> createAppointment(
            @AuthenticationPrincipal TokenPrincipal principal,
            @Valid @RequestBody CreateAppointmentRequest request) {
        Hospital hospital = hospitalRepository.findById(request.getHospitalId())
                .orElseThrow(() -> new IllegalArgumentException("Больница не найдена: id=" + request.getHospitalId()));

        User user = userRepository.getReferenceById(principal.getUserId());
        Appointment appointment = Appointment.builder()
                .user(user)
                .hospital(hospital)
                .scheduledAt(request.getScheduledAt())
                .status("PENDING")
                .build();
        appointment = appointmentRepository.save(appointment);
        appointmentEventProducer.sendAppointmentCreated(appointment);

        AppointmentDto dto = toAppointmentDto(appointment);
        return ResponseEntity.ok(dto);
    }

    private static AppointmentDto toAppointmentDto(Appointment a) {
        return AppointmentDto.builder()
                .id(a.getId())
                .hospitalId(a.getHospital().getId())
                .hospitalName(a.getHospital().getName())
                .scheduledAt(a.getScheduledAt())
                .status(a.getStatus())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
