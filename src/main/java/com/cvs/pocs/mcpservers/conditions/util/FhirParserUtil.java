package com.cvs.pocs.mcpservers.conditions.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Observation.ObservationReferenceRangeComponent;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.AllergyIntolerance.AllergyIntoleranceReactionComponent;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.Bundle;

public class FhirParserUtil {
     private static final Logger logger = LoggerFactory.getLogger(FhirParserUtil.class); 
     private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
     private static final FhirContext fhirContext = FhirContext.forR4();
     private static final IParser parser = fhirContext.newJsonParser();
     
     public static Patient parsePatient(String patientJson) {
         logger.info("Parsing patient JSON");
         return parser.parseResource(Patient.class, patientJson);
     }
     
     public static Observation parseObservation(String observationJson) {
         logger.info("Parsing observation JSON");
         return parser.parseResource(Observation.class, observationJson);
     }
    
    public static AllergyIntolerance parseAllergyIntolerance(String allergyIntoleranceJson) {
        logger.info("Parsing allergy intolerance JSON");
        return parser.parseResource(AllergyIntolerance.class, allergyIntoleranceJson);
    }
    public static Condition parseCondition(String conditionJson) {
        logger.info("Parsing condition JSON");
        return parser.parseResource(Condition.class, conditionJson);
    }
    public static MedicationRequest parseMedicationRequest(String medicationRequestJson) {
        logger.info("Parsing medication request JSON");
        return parser.parseResource(MedicationRequest.class, medicationRequestJson);
    }
    
    public static String formatPatient(Patient patient) { 
         StringBuilder sb = new StringBuilder(); 
         sb.append("PATIENT DEMOGRAPHICS:\n"); 
         // Name 
         if (patient.hasName() && !patient.getName().isEmpty()) { 
             HumanName name = patient.getNameFirstRep(); 
             sb.append("Name: "); 
             if (name.hasGiven()) { 
                 sb.append(name.getGivenAsSingleString()).append(" "); 
             } 
             if (name.hasFamily()) { 
                 sb.append(name.getFamily()); 
             } 
             sb.append("\n"); 
         } 
         // Gender and DOB 
         if (patient.hasGender()) { 
             sb.append("Gender: ").append(patient.getGender().getDisplay()).append("\n"); 
         } 
         if (patient.hasBirthDate()) { 
             sb.append("DOB: ").append(dateFormat.format(patient.getBirthDate())).append("\n"); 
         } 
         // Other identifiers 
         if (patient.hasIdentifier()) { 
             sb.append("MRN: ").append(patient.getIdentifierFirstRep().getValue()).append("\n"); 
         } 
         sb.append("\n"); 
         return sb.toString(); 
     }
     
     public static String formatObservation(Observation observation) { 
         StringBuilder sb = new StringBuilder(); 
         // Get the observation date 
         Date effectiveDate = null; 
         if (observation.hasEffectiveDateTimeType()) { 
             effectiveDate = observation.getEffectiveDateTimeType().getValue(); 
         } 
         // Get the observation name/code 
         String observationName = "Unknown Test"; 
         if (observation.hasCode() && observation.getCode().hasText()) { 
             observationName = observation.getCode().getText(); 
         } else if (observation.hasCode() && observation.getCode().hasCoding()) { 
             observationName = observation.getCode().getCodingFirstRep().getDisplay(); 
         } 
         // Get the value and unit 
         String valueText = ""; 
         if (observation.hasValueQuantity()) { 
             Quantity quantity = observation.getValueQuantity(); 
             valueText = String.format("%.2f %s", quantity.getValue().doubleValue(), quantity.getUnit() != null ? quantity.getUnit() : ""); 
         } else if (observation.hasValueStringType()) { 
             valueText = observation.getValueStringType().getValue(); 
         } else if (observation.hasValueCodeableConcept()) { 
             valueText = observation.getValueCodeableConcept().getText(); 
         } 
         // Get reference ranges 
         String referenceRange = ""; 
         if (observation.hasReferenceRange() && !observation.getReferenceRange().isEmpty()) { 
             ObservationReferenceRangeComponent range = observation.getReferenceRangeFirstRep(); 
             if (range.hasLow() && range.hasHigh()) { 
                 referenceRange = String.format("[%.2f - %.2f]", range.getLow().getValue().doubleValue(), range.getHigh().getValue().doubleValue()); 
             } 
         } 
         // Get interpretation (abnormal, etc.) 
         String interpretation = ""; 
         if (observation.hasInterpretation() && !observation.getInterpretation().isEmpty()) { 
             interpretation = observation.getInterpretationFirstRep().getText(); 
         } 
         // Format the output 
         sb.append(observationName).append(": ") 
             .append(valueText); 
         if (!referenceRange.isEmpty()) { 
             sb.append(" ").append(referenceRange); 
         } 
         if (!interpretation.isEmpty()) { 
             sb.append(" (").append(interpretation).append(")"); 
         } 
         if (effectiveDate != null) { 
             sb.append(" - ").append(dateFormat.format(effectiveDate)); 
         } 
         sb.append("\n"); 
         return sb.toString(); 
     }
     
     public static String formatCondition(Condition condition) { 
         StringBuilder sb = new StringBuilder(); 
         // Condition name/code 
         String conditionName = "Unknown Condition"; 
         if (condition.hasCode()) { 
             if (condition.getCode().hasText()) { 
                 conditionName = condition.getCode().getText(); 
             } else if (condition.getCode().hasCoding()) { 
                 conditionName = condition.getCode().getCodingFirstRep().getDisplay(); 
             } 
         } 
         // Status 
         String status = ""; 
         if (condition.hasVerificationStatus() && condition.getVerificationStatus().hasCoding() && !condition.getVerificationStatus().getCoding().isEmpty()) {
             status = condition.getVerificationStatus().getCodingFirstRep().getDisplay();
         }
         // Onset date 
         String onset = ""; 
         if (condition.hasOnsetDateTimeType()) { 
             onset = dateFormat.format(condition.getOnsetDateTimeType().getValue()); 
         } 
         // Format the output 
         sb.append(conditionName); 
         if (status != null && !status.isEmpty()) { 
             sb.append(" (").append(status).append(")"); 
         } 
         if (!onset.isEmpty()) { 
             sb.append(" - Onset: ").append(onset); 
         } 
         sb.append("\n"); 
         return sb.toString(); 
     }
     
     public static String formatMedication(MedicationRequest medicationRequest) { 
         StringBuilder sb = new StringBuilder(); 
         // Medication name 
         String medicationName = "Unknown Medication"; 
         if (medicationRequest.hasMedicationCodeableConcept()) { 
             if (medicationRequest.getMedicationCodeableConcept().hasText()) { 
                 medicationName = medicationRequest.getMedicationCodeableConcept().getText(); 
             } else if (medicationRequest.getMedicationCodeableConcept().hasCoding()) { 
                 medicationName = medicationRequest.getMedicationCodeableConcept().getCodingFirstRep().getDisplay(); 
             } 
         } 
         // Dosage 
         String dosage = ""; 
         if (medicationRequest.hasDosageInstruction() && !medicationRequest.getDosageInstruction().isEmpty()) { 
             // Get the text representation of the dosage instruction
             String dosageText = medicationRequest.getDosageInstructionFirstRep().getText();
             if (dosageText != null) {
                 dosage = dosageText;
             }
         } 
         // Format the output 
         sb.append(medicationName); 
         if (dosage != null && !dosage.isEmpty()) { 
             sb.append(": ").append(dosage); 
         } 
         sb.append("\n"); 
         return sb.toString(); 
     }
     
     public static String formatAllergy(AllergyIntolerance allergyIntolerance) { 
         StringBuilder sb = new StringBuilder(); 
         // Allergy substance 
         String substance = "Unknown Allergen"; 
         if (allergyIntolerance.hasCode()) { 
             if (allergyIntolerance.getCode().hasText()) { 
                 substance = allergyIntolerance.getCode().getText(); 
             } else if (allergyIntolerance.getCode().hasCoding()) { 
                 substance = allergyIntolerance.getCode().getCodingFirstRep().getDisplay(); 
             } 
         } 
         // Reaction 
         String reaction = ""; 
         if (allergyIntolerance.hasReaction() && !allergyIntolerance.getReaction().isEmpty()) { 
             AllergyIntoleranceReactionComponent reactionComponent = allergyIntolerance.getReactionFirstRep(); 
             if (reactionComponent.hasManifestation()) { 
                 reaction = reactionComponent.getManifestationFirstRep().getText(); 
             } 
         } 
         // Format the output 
         sb.append(substance); 
         if (!reaction.isEmpty()) { 
             sb.append(" (").append(reaction).append(")"); 
         } 
         sb.append("\n"); 
         return sb.toString(); 
     }
     
     public static Bundle parseBundle(String bundleJson) {
         logger.info("Parsing bundle JSON");
         return parser.parseResource(Bundle.class, bundleJson);
     }
} 


