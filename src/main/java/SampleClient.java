import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;

public class SampleClient {

    public static void main(String[] theArgs) {

        // Create a FHIR client
        FhirContext fhirContext = FhirContext.forR4();
        IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
        client.registerInterceptor(new LoggingInterceptor(false));

        // Search for Patient resources
        Bundle response = client
                .search()
                .forResource("Patient")
                .where(Patient.FAMILY.matches().value("SMITH"))
                .returnBundle(Bundle.class)
                .execute();

        /*1. Basic tasks
         Modify SampleClient so that it prints the first and last name, and birth date of each Patient to the screen
 		 Sort the output so that the results are ordered by the patient's first name */
        System.out.println("Details of all patients:::");
        List<Patient> patientList = new ArrayList<>();
        for (BundleEntryComponent bundleData : response.getEntry()) {
        	Resource r = bundleData.getResource();
        	if (r instanceof Patient) {
        		Patient p = (Patient) r;
        		patientList.add(p);
        	}
        }
        
        //Stream.of(response.getEntry()).filter(Patient.class::isInstance).map(r -> (Patient) r).collect(Collectors.toList()).forEach(System.out::println);
        Collections.sort(patientList, (p1, p2) -> {
        									return p1.getNameFirstRep().getGivenAsSingleString().compareToIgnoreCase(p2.getNameFirstRep().getGivenAsSingleString()); 
        								});
        patientList.forEach(p -> {
        						System.out.print("Patient full name::"+p.getNameFirstRep().getNameAsSingleString());
        						System.out.println(", birth date::"+String.format("%1$tb %1$te, %1$tY %1$tI:%1$tM %1$Tp", p.getBirthDate()));
        					});
    }
}
