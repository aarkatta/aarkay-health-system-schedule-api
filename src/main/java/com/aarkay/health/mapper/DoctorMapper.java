package com.aarkay.health.mapper;

import com.aarkay.health.model.Doctor;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

/**
 * Mapper class to convert between Doctor entity and FHIR Practitioner resource.
 */
@Component
public class DoctorMapper {

    /**
     * Converts FHIR Practitioner resource to Doctor entity.
     *
     * @param fhirPractitioner FHIR Practitioner resource
     * @return Doctor entity
     */
    public Doctor toEntity(Practitioner fhirPractitioner) {
        if (fhirPractitioner == null) {
            return null;
        }

        Doctor doctor = new Doctor();

        // Map ID - FHIR uses String IDs, convert to Long
        if (fhirPractitioner.hasIdElement()) {
            try {
                doctor.setId(Long.parseLong(fhirPractitioner.getIdElement().getIdPart()));
            } catch (NumberFormatException e) {
                // If FHIR ID is not numeric, keep as null
                doctor.setId(null);
            }
        }

        // Map Name - extract first and last name from HumanName
        if (fhirPractitioner.hasName() && !fhirPractitioner.getName().isEmpty()) {
            HumanName name = fhirPractitioner.getName().get(0);
            if (name.hasGiven() && !name.getGiven().isEmpty()) {
                doctor.setFirstName(name.getGiven().get(0).getValue());
            }
            if (name.hasFamily()) {
                doctor.setLastName(name.getFamily());
            }
        }

        // Map Specialty - extract from qualification
        if (fhirPractitioner.hasQualification() && !fhirPractitioner.getQualification().isEmpty()) {
            Practitioner.PractitionerQualificationComponent qualification = fhirPractitioner.getQualification().get(0);
            if (qualification.hasCode() && qualification.getCode().hasCoding() && !qualification.getCode().getCoding().isEmpty()) {
                Coding coding = qualification.getCode().getCoding().get(0);
                if (coding.hasDisplay()) {
                    doctor.setSpecialty(coding.getDisplay());
                } else if (coding.hasCode()) {
                    doctor.setSpecialty(coding.getCode());
                }
            }
        }

        // Map Zipcode - extract from address
        if (fhirPractitioner.hasAddress() && !fhirPractitioner.getAddress().isEmpty()) {
            Address address = fhirPractitioner.getAddress().get(0);
            if (address.hasPostalCode()) {
                doctor.setZipcode(address.getPostalCode());
            }
        }

        // Map Contact Information - extract phone and email from telecom
        if (fhirPractitioner.hasTelecom()) {
            for (ContactPoint telecom : fhirPractitioner.getTelecom()) {
                if (telecom.hasSystem()) {
                    if (telecom.getSystem() == ContactPoint.ContactPointSystem.PHONE && telecom.hasValue()) {
                        doctor.setPhone(telecom.getValue());
                    } else if (telecom.getSystem() == ContactPoint.ContactPointSystem.EMAIL && telecom.hasValue()) {
                        doctor.setEmail(telecom.getValue());
                    }
                }
            }
        }

        return doctor;
    }

    /**
     * Converts Doctor entity to FHIR Practitioner resource.
     *
     * @param doctor Doctor entity
     * @return FHIR Practitioner resource
     */
    public Practitioner toFhirResource(Doctor doctor) {
        if (doctor == null) {
            return null;
        }

        Practitioner fhirPractitioner = new Practitioner();

        // Map ID - convert Long to String
        if (doctor.getId() != null) {
            fhirPractitioner.setId(doctor.getId().toString());
        }

        // Map Name - combine first and last name into HumanName
        if (doctor.getFirstName() != null || doctor.getLastName() != null) {
            HumanName name = new HumanName();
            if (doctor.getFirstName() != null) {
                name.addGiven(doctor.getFirstName());
            }
            if (doctor.getLastName() != null) {
                name.setFamily(doctor.getLastName());
            }
            fhirPractitioner.addName(name);
        }

        // Map Specialty to Qualification
        if (doctor.getSpecialty() != null) {
            Practitioner.PractitionerQualificationComponent qualification =
                new Practitioner.PractitionerQualificationComponent();

            CodeableConcept qualificationCode = new CodeableConcept();
            Coding coding = new Coding();
            coding.setDisplay(doctor.getSpecialty());
            coding.setCode(doctor.getSpecialty());
            coding.setSystem("http://terminology.hl7.org/CodeSystem/practitioner-specialty");
            qualificationCode.addCoding(coding);
            qualificationCode.setText(doctor.getSpecialty());

            qualification.setCode(qualificationCode);
            fhirPractitioner.addQualification(qualification);
        }

        // Map Zipcode to Address
        if (doctor.getZipcode() != null) {
            Address address = new Address();
            address.setPostalCode(doctor.getZipcode());
            address.setUse(Address.AddressUse.WORK);
            fhirPractitioner.addAddress(address);
        }

        // Map Phone to ContactPoint
        if (doctor.getPhone() != null) {
            ContactPoint phone = new ContactPoint();
            phone.setSystem(ContactPoint.ContactPointSystem.PHONE);
            phone.setValue(doctor.getPhone());
            phone.setUse(ContactPoint.ContactPointUse.WORK);
            fhirPractitioner.addTelecom(phone);
        }

        // Map Email to ContactPoint
        if (doctor.getEmail() != null) {
            ContactPoint email = new ContactPoint();
            email.setSystem(ContactPoint.ContactPointSystem.EMAIL);
            email.setValue(doctor.getEmail());
            email.setUse(ContactPoint.ContactPointUse.WORK);
            fhirPractitioner.addTelecom(email);
        }

        // Set practitioner as active
        fhirPractitioner.setActive(true);

        return fhirPractitioner;
    }
}
