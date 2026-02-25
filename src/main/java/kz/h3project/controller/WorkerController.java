package kz.h3project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.h3project.model.hospital.dto.AppointmentDto;
import kz.h3project.model.hospital.dto.UpdateAppointmentStatusRequest;
import kz.h3project.model.hospital.entity.Appointment;
import kz.h3project.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Worker API", description = "API воркера: список заявок и приём (смена статуса)")
@RequestMapping("/worker")
@RestController
@RequiredArgsConstructor
public class WorkerController {

    private final AppointmentRepository appointmentRepository;

    @Operation(summary = "Список заявок на приём", description = "Для воркера: все заявки, новые первыми")
    @PreAuthorize("@authorization.hasAccessToReadAppointments()")
    @GetMapping("/appointments")
    @Transactional(readOnly = true)
    public ResponseEntity<List<AppointmentDto>> listAppointments() {
        List<Appointment> appointments = appointmentRepository.findAllWithUserAndHospitalOrderByCreatedAtDesc();
        List<AppointmentDto> dtos = appointments.stream()
                .map(HospitalController::toAppointmentDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Сменить статус заявки", description = "Принять (CONFIRMED), отменить (CANCELLED) и т.д.")
    @PreAuthorize("@authorization.hasAccessToUpdateAppointmentStatus()")
    @PatchMapping("/appointments/{id}/status")
    @Transactional
    public ResponseEntity<AppointmentDto> updateAppointmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAppointmentStatusRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена: id=" + id));
        appointment.setStatus(request.getStatus().trim().toUpperCase());
        appointment = appointmentRepository.save(appointment);
        return ResponseEntity.ok(HospitalController.toAppointmentDto(appointment));
    }
}
