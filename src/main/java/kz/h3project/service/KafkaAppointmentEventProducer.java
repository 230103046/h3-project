package kz.h3project.service;

import kz.h3project.events.AppointmentCreatedEvent;
import kz.h3project.model.hospital.entity.Appointment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static kz.h3project.config.KafkaTopicConfig.TOPIC_APPOINTMENTS;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class KafkaAppointmentEventProducer implements IAppointmentEventProducer {

    private final KafkaTemplate<String, AppointmentCreatedEvent> kafkaTemplate;

    @Override
    public void sendAppointmentCreated(Appointment appointment) {
        AppointmentCreatedEvent event = AppointmentCreatedEvent.builder()
                .appointmentId(appointment.getId())
                .userId(appointment.getUser().getId())
                .hospitalId(appointment.getHospital().getId())
                .hospitalName(appointment.getHospital().getName())
                .scheduledAt(appointment.getScheduledAt())
                .createdAt(appointment.getCreatedAt())
                .build();
        kafkaTemplate.send(TOPIC_APPOINTMENTS, String.valueOf(appointment.getId()), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send appointment-created event: appointmentId={}", appointment.getId(), ex);
                    } else {
                        log.info("Sent appointment-created event: appointmentId={}", appointment.getId());
                    }
                });
    }
}
