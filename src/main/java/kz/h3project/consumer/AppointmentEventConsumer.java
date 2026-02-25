package kz.h3project.consumer;

import kz.h3project.events.AppointmentCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static kz.h3project.config.KafkaTopicConfig.TOPIC_APPOINTMENTS;

@Slf4j
@Component
public class AppointmentEventConsumer {

    @KafkaListener(topics = TOPIC_APPOINTMENTS, groupId = "h3-project-consumer")
    public void onAppointmentCreated(AppointmentCreatedEvent event) {
        log.info("Event received: appointment created -> appointmentId={}, userId={}, hospital={}, scheduledAt={}",
                event.getAppointmentId(), event.getUserId(), event.getHospitalName(), event.getScheduledAt());
        // Здесь можно добавить обработку: уведомление, аналитика, синхронизация с другими сервисами и т.д.
    }
}
