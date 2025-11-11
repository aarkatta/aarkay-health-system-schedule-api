package com.aarkay.health.mapper;

import com.aarkay.health.model.Patient;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Mapper class to convert between Patient entity and FHIR Patient resource.
 */
@Component
public class PatientMapper {

    /**
     * Converts FHIR Patient resource to Patient entity.
     *
     * @param fhirPatient FHIR Patient resource
     * @return Patient entity
     */
    public Patient toEntity(org.hl7.fhir.r4.model.Patient fhirPatient) {
        if (fhirPatient == null) {
            return null;
        }

        Patient patient = new Patient();

        // Map ID - FHIR uses String IDs, convert to Long
        if (fhirPatient.hasIdElement()) {
            try {
                patient.setId(Long.parseLong(fhirPatient.getIdElement().getIdPart()));
            } catch (NumberFormatException e) {
                // If FHIR ID is not numeric, we'll handle this by keeping it as null
                // or you could hash it to a Long
                patient.setId(null);
            }
        }

        // Map Name - extract first and last name from HumanName
        if (fhirPatient.hasName() && !fhirPatient.getName().isEmpty()) {
            HumanName name = fhirPatient.getName().get(0);
            if (name.hasGiven() && !name.getGiven().isEmpty()) {
                patient.setFirstName(name.getGiven().get(0).getValue());
            }
            if (name.hasFamily()) {
                patient.setLastName(name.getFamily());
            }
        }

        // Map Age - calculate from birthDate
        if (fhirPatient.hasBirthDate()) {
            Date birthDate = fhirPatient.getBirthDate();
            LocalDate birthLocalDate = birthDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            LocalDate now = LocalDate.now();
            long age = ChronoUnit.YEARS.between(birthLocalDate, now);
            patient.setAge((int) age);
        }

        // Map Gender
        if (fhirPatient.hasGender()) {
            patient.setGender(fhirPatient.getGender().toCode());
        }

        // Map Contact Information - extract phone and email from telecom
        if (fhirPatient.hasTelecom()) {
            for (ContactPoint telecom : fhirPatient.getTelecom()) {
                if (telecom.hasSystem()) {
                    if (telecom.getSystem() == ContactPoint.ContactPointSystem.PHONE && telecom.hasValue()) {
                        patient.setPhone(telecom.getValue());
                    } else if (telecom.getSystem() == ContactPoint.ContactPointSystem.EMAIL && telecom.hasValue()) {
                        patient.setEmail(telecom.getValue());
                    }
                }
            }
        }

        return patient;
    }

    /**
     * Converts Patient entity to FHIR Patient resource.
     *
     * @param patient Patient entity
     * @return FHIR Patient resource
     */
    public org.hl7.fhir.r4.model.Patient toFhirResource(Patient patient) {
        if (patient == null) {
            return null;
        }

        org.hl7.fhir.r4.model.Patient fhirPatient = new org.hl7.fhir.r4.model.Patient();

        // Map ID - convert Long to String
        if (patient.getId() != null) {
            fhirPatient.setId(patient.getId().toString());
        }

        // Map Name - combine first and last name into HumanName
        if (patient.getFirstName() != null || patient.getLastName() != null) {
            HumanName name = new HumanName();
            if (patient.getFirstName() != null) {
                name.addGiven(patient.getFirstName());
            }
            if (patient.getLastName() != null) {
                name.setFamily(patient.getLastName());
            }
            fhirPatient.addName(name);
        }

        // Map Age to BirthDate - calculate birthDate from age
        if (patient.getAge() != null) {
            LocalDate birthDate = LocalDate.now().minusYears(patient.getAge());
            Date date = Date.from(birthDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            fhirPatient.setBirthDate(date);
        }

        // Map Gender
        if (patient.getGender() != null) {
            try {
                Enumerations.AdministrativeGender gender = Enumerations.AdministrativeGender.fromCode(patient.getGender().toLowerCase());
                fhirPatient.setGender(gender);
            } catch (Exception e) {
                // If gender code is invalid, set as unknown
                fhirPatient.setGender(Enumerations.AdministrativeGender.UNKNOWN);
            }
        }

        // Map Phone to ContactPoint
        if (patient.getPhone() != null) {
            ContactPoint phone = new ContactPoint();
            phone.setSystem(ContactPoint.ContactPointSystem.PHONE);
            phone.setValue(patient.getPhone());
            phone.setUse(ContactPoint.ContactPointUse.HOME);
            fhirPatient.addTelecom(phone);
        }

        // Map Email to ContactPoint
        if (patient.getEmail() != null) {
            ContactPoint email = new ContactPoint();
            email.setSystem(ContactPoint.ContactPointSystem.EMAIL);
            email.setValue(patient.getEmail());
            fhirPatient.addTelecom(email);
        }

        // Set patient as active
        fhirPatient.setActive(true);

        return fhirPatient;
    }
}
