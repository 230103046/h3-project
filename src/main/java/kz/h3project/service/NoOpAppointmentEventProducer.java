package kz.h3project.service;

import kz.h3project.model.hospital.entity.Appointment;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "false")
public class NoOpAppointmentEventProducer implements IAppointmentEventProducer {

    @Override
    public void sendAppointmentCreated(Appointment appointment) {
        // Kafka отключена — событие не отправляется
    }
}
