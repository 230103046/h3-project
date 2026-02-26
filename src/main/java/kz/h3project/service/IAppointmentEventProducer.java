package kz.h3project.service;

import kz.h3project.model.hospital.entity.Appointment;

public interface IAppointmentEventProducer {

    void sendAppointmentCreated(Appointment appointment);
}
