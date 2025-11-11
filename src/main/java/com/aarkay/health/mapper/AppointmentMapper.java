package com.aarkay.health.mapper;

import com.aarkay.health.model.Appointment;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Date;

/**
 * Mapper class to convert between Appointment entity and FHIR Appointment resource.
 */
@Component
public class AppointmentMapper {

    /**
     * Converts FHIR Appointment resource to Appointment entity.
     *
     * @param fhirAppointment FHIR Appointment resource
     * @return Appointment entity
     */
    public Appointment toEntity(org.hl7.fhir.r4.model.Appointment fhirAppointment) {
        if (fhirAppointment == null) {
            return null;
        }

        Appointment appointment = new Appointment();

        // Map ID - FHIR uses String IDs, convert to Long
        if (fhirAppointment.hasIdElement()) {
            try {
                appointment.setId(Long.parseLong(fhirAppointment.getIdElement().getIdPart()));
            } catch (NumberFormatException e) {
                // If FHIR ID is not numeric, keep as null
                appointment.setId(null);
            }
        }

        // Map Appointment Time - convert from Date to LocalDateTime
        if (fhirAppointment.hasStart()) {
            Date startDate = fhirAppointment.getStart();
            appointment.setAppointmentTime(
                startDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
            );
        }

        // Extract Doctor ID and Patient ID from participants
        if (fhirAppointment.hasParticipant()) {
            for (org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent participant : fhirAppointment.getParticipant()) {
                if (participant.hasActor() && participant.getActor().hasReference()) {
                    String reference = participant.getActor().getReference();

                    // Check if this is a Practitioner (Doctor) reference
                    if (reference.startsWith("Practitioner/")) {
                        String practitionerId = reference.substring("Practitioner/".length());
                        try {
                            appointment.setDoctorId(Long.parseLong(practitionerId));
                        } catch (NumberFormatException e) {
                            // Keep as null if not numeric
                        }
                    }
                    // Check if this is a Patient reference
                    else if (reference.startsWith("Patient/")) {
                        String patientId = reference.substring("Patient/".length());
                        try {
                            appointment.setPatientId(Long.parseLong(patientId));
                        } catch (NumberFormatException e) {
                            // Keep as null if not numeric
                        }
                    }
                }
            }
        }

        return appointment;
    }

    /**
     * Converts Appointment entity to FHIR Appointment resource.
     *
     * @param appointment Appointment entity
     * @return FHIR Appointment resource
     */
    public org.hl7.fhir.r4.model.Appointment toFhirResource(Appointment appointment) {
        if (appointment == null) {
            return null;
        }

        org.hl7.fhir.r4.model.Appointment fhirAppointment = new org.hl7.fhir.r4.model.Appointment();

        // Map ID - convert Long to String
        if (appointment.getId() != null) {
            fhirAppointment.setId(appointment.getId().toString());
        }

        // Map Appointment Time - convert LocalDateTime to Date
        if (appointment.getAppointmentTime() != null) {
            Date startDate = Date.from(
                appointment.getAppointmentTime()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
            );
            fhirAppointment.setStart(startDate);
        }

        // Set default status as "booked"
        fhirAppointment.setStatus(org.hl7.fhir.r4.model.Appointment.AppointmentStatus.BOOKED);

        // Add Doctor as Participant (Practitioner)
        if (appointment.getDoctorId() != null) {
            org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent doctorParticipant =
                new org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent();

            Reference doctorReference = new Reference();
            doctorReference.setReference("Practitioner/" + appointment.getDoctorId());
            doctorParticipant.setActor(doctorReference);

            // Set participant status as accepted
            doctorParticipant.setStatus(org.hl7.fhir.r4.model.Appointment.ParticipationStatus.ACCEPTED);

            // Set type as primary performer
            CodeableConcept participantType = new CodeableConcept();
            Coding typeCoding = new Coding();
            typeCoding.setSystem("http://terminology.hl7.org/CodeSystem/v3-ParticipationType");
            typeCoding.setCode("PPRF");
            typeCoding.setDisplay("primary performer");
            participantType.addCoding(typeCoding);
            doctorParticipant.addType(participantType);

            fhirAppointment.addParticipant(doctorParticipant);
        }

        // Add Patient as Participant
        if (appointment.getPatientId() != null) {
            org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent patientParticipant =
                new org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent();

            Reference patientReference = new Reference();
            patientReference.setReference("Patient/" + appointment.getPatientId());
            patientParticipant.setActor(patientReference);

            // Set participant status as accepted
            patientParticipant.setStatus(org.hl7.fhir.r4.model.Appointment.ParticipationStatus.ACCEPTED);

            fhirAppointment.addParticipant(patientParticipant);
        }

        // Set appointment description
        fhirAppointment.setDescription("Medical appointment");

        return fhirAppointment;
    }
}
