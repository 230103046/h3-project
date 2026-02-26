package kz.h3project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.h3project.model.hospital.dto.AppointmentDto;
import kz.h3project.model.hospital.dto.CreateAppointmentRequest;
import kz.h3project.model.hospital.dto.HospitalCreateUpdateDto;
import kz.h3project.model.hospital.dto.HospitalDto;
import kz.h3project.model.hospital.entity.Appointment;
import kz.h3project.model.hospital.entity.Hospital;
import kz.h3project.model.user.data.TokenPrincipal;
import kz.h3project.model.user.entity.User;
import kz.h3project.repository.AppointmentRepository;
import kz.h3project.repository.HospitalRepository;
import kz.h3project.repository.UserRepository;
import kz.h3project.service.IAppointmentEventProducer;
import kz.h3project.service.HospitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Hospitals API", description = "Больницы и запись на приём")
@RequestMapping("/hospitals")
@RestController
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;
    private final HospitalRepository hospitalRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final IAppointmentEventProducer appointmentEventProducer;

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

    @Operation(summary = "Создать больницу", description = "Create/Update/Delete hospital — только ADMIN")
    @PreAuthorize("@authorization.hasAccessToWriteHospital()")
    @PostMapping("/create")
    @Transactional
    public ResponseEntity<HospitalDto> createHospital(@Valid @RequestBody HospitalCreateUpdateDto dto) {
        Hospital hospital = Hospital.builder()
                .name(dto.getName())
                .city(dto.getCity())
                .address(dto.getAddress())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .build();
        hospital = hospitalRepository.save(hospital);
        return ResponseEntity.ok(toHospitalDto(hospital, null));
    }

    @Operation(summary = "Обновить больницу", description = "Только ADMIN")
    @PreAuthorize("@authorization.hasAccessToWriteHospital()")
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<HospitalDto> updateHospital(
            @PathVariable Long id,
            @Valid @RequestBody HospitalCreateUpdateDto dto) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Больница не найдена: id=" + id));
        hospital.setName(dto.getName());
        hospital.setCity(dto.getCity());
        hospital.setAddress(dto.getAddress());
        hospital.setLatitude(dto.getLatitude());
        hospital.setLongitude(dto.getLongitude());
        hospitalRepository.save(hospital);
        return ResponseEntity.ok(toHospitalDto(hospital, null));
    }

    @Operation(summary = "Удалить больницу", description = "Только ADMIN")
    @PreAuthorize("@authorization.hasAccessToWriteHospital()")
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteHospital(@PathVariable Long id) {
        if (!hospitalRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        hospitalRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Мои записи на приём", description = "Список своих записей — любой авторизованный")
    @GetMapping("/appointments/me")
    @Transactional(readOnly = true)
    public ResponseEntity<List<AppointmentDto>> getMyAppointments(@AuthenticationPrincipal TokenPrincipal principal) {
        List<Appointment> appointments = appointmentRepository.findByUserIdWithUserAndHospitalOrderByScheduledAtDesc(principal.getUserId());
        List<AppointmentDto> dtos = appointments.stream()
                .map(HospitalController::toAppointmentDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Отменить запись", description = "Cancel appointment — USER только свою; WORKER/ADMIN любую")
    @PreAuthorize("@authorization.hasAccessToCancelAppointment()")
    @PostMapping("/appointments/{id}/cancel")
    @Transactional
    public ResponseEntity<AppointmentDto> cancelAppointment(
            @AuthenticationPrincipal TokenPrincipal principal,
            @PathVariable Long id) {
        Appointment appointment = appointmentRepository.findByIdWithUserAndHospital(id)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена: id=" + id));
        boolean canUpdateAny = principal.getPermissions().contains("UPDATE_APPOINTMENT_STATUS");
        if (!canUpdateAny && !appointment.getUser().getId().equals(principal.getUserId())) {
            return ResponseEntity.status(403).build();
        }
        appointment.setStatus("CANCELLED");
        appointment = appointmentRepository.save(appointment);
        return ResponseEntity.ok(toAppointmentDto(appointment));
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

    private static HospitalDto toHospitalDto(Hospital h, Double distanceKm) {
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

    static AppointmentDto toAppointmentDto(Appointment a) {
        return AppointmentDto.builder()
                .id(a.getId())
                .userId(a.getUser() != null ? a.getUser().getId() : null)
                .username(a.getUser() != null ? a.getUser().getUsername() : null)
                .hospitalId(a.getHospital().getId())
                .hospitalName(a.getHospital().getName())
                .scheduledAt(a.getScheduledAt())
                .status(a.getStatus())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
