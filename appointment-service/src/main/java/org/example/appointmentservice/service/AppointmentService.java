package org.example.appointmentservice.service;

import org.example.appointmentservice.entity.Appointment;
import org.example.appointmentservice.repository.AppointmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AppointmentService {

    private static final String PATIENT_URL = "http://patient-service/api/v1/patients/";
    private static final String DOCTOR_URL  = "http://doctor-service/api/v1/doctors/";

    private final AppointmentRepository appointmentRepository;
    private final RestTemplate restTemplate;

    public AppointmentService(AppointmentRepository appointmentRepository, RestTemplate restTemplate) {
        this.appointmentRepository = appointmentRepository;
        this.restTemplate = restTemplate;
    }

    public Appointment create(Appointment appointment) {
        if (appointment.getPatientId() == null || appointment.getDoctorId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "patientId và doctorId là bắt buộc");
        }

        checkExists(PATIENT_URL + appointment.getPatientId(), "Bệnh nhân");
        checkExists(DOCTOR_URL + appointment.getDoctorId(), "Bác sĩ");

        appointment.setId(null);
        if (appointment.getStatus() == null) {
            appointment.setStatus("PENDING");
        }
        return appointmentRepository.save(appointment);
    }

    private void checkExists(String url, String name) {
        try {
            restTemplate.getForObject(url, String.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, name + " không tồn tại");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Không gọi được dịch vụ " + name + ": " + e.getMessage());
        }
    }
}