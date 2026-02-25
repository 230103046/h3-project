package kz.h3project.repository;

import kz.h3project.model.hospital.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByUserIdOrderByScheduledAtDesc(Long userId);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.user JOIN FETCH a.hospital WHERE a.user.id = :userId ORDER BY a.scheduledAt DESC")
    List<Appointment> findByUserIdWithUserAndHospitalOrderByScheduledAtDesc(@Param("userId") Long userId);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.user JOIN FETCH a.hospital WHERE a.id = :id")
    Optional<Appointment> findByIdWithUserAndHospital(@Param("id") Long id);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.user JOIN FETCH a.hospital ORDER BY a.createdAt DESC")
    List<Appointment> findAllWithUserAndHospitalOrderByCreatedAtDesc();
}
