import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;

public class SampleClient {

    public static void main(String[] theArgs) throws IOException {

        // Create a FHIR client
        FhirContext fhirContext = FhirContext.forR4();
        IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
        LoggingInterceptor logger = new LoggingInterceptor(false);
        client.registerInterceptor(logger);

        SampleClient sampleClient = new SampleClient();
        sampleClient.basicTasks(client);
        
        client.unregisterInterceptor(logger);
        
        sampleClient.intermediateTasks(client);
    }
    
    /* 1. Basic tasks
     * i) Modify SampleClient so that it prints the first and last name, and birth date of each Patient to the screen
     * ii) Sort the output so that the results are ordered by the patient's first name
     */
    public void basicTasks(IGenericClient client) {
    	 //Search for Patient resources
    	 Bundle response = client
                 .search()
                 .forResource("Patient")
                 .where(Patient.FAMILY.matches().value("SMITH"))
                 .returnBundle(Bundle.class)
                 .execute();
    	
    	if (response == null)
    		return;
    	 
    	List<Patient> patientList = response.getEntry().stream().map(e -> e.getResource()).filter(r -> r instanceof Patient).map(r -> (Patient) r).collect(Collectors.toList());
    	
    	System.out.println("Details of all patients:::");
        Collections.sort(patientList, (p1, p2) -> {
        									return p1.getNameFirstRep().getGivenAsSingleString().compareToIgnoreCase(p2.getNameFirstRep().getGivenAsSingleString()); 
        								});
        patientList.forEach(p -> {
        						System.out.print("Patient full name::"+p.getNameFirstRep().getNameAsSingleString());
        						System.out.println(", birth date::"+String.format("%1$tb %1$te, %1$tY %1$tI:%1$tM %1$Tp", p.getBirthDate()));
        					});
    }
    
    /* 2. Intermediate tasks
     * i) Create a text file containing 20 different last names
     * ii) Modify 'SampleClient' so that instead of searching for patients with last name 'SMITH', it reads in the contents of this file and for each last name queries for 
     * 	   patients with that last name
     * iii) Print the average response time for these 20 searches by implementing an IClientInterceptor that uses the requestStopWatch to determine the response time of 
     * 		each request.
     * iv)  Run this loop three times, printing the average response time for each loop. The first two times the loop should run as described above. 
     * 		The third time the loop of 20 searches is run, the searches should be performed with caching disabled.
     * v) If there is enough time between runs, you should expect to see loop 2 with a shorter average response time than loop 1 and 3.
     */
    public void intermediateTasks(IGenericClient client) throws IOException {
    	System.out.println("Avg response time of loop 1::"+displayAverageResponseTime(client, true));
    	System.out.println("Avg response time of loop 2::"+displayAverageResponseTime(client, true));
    	System.out.println("Avg response time of loop 3::"+displayAverageResponseTime(client, false));
    }
    
    public double displayAverageResponseTime(IGenericClient client, boolean isCacheEnabled) throws IOException {
    	Timer t = new Timer();
    	client.registerInterceptor(t);
    	
    	BufferedReader br = new BufferedReader(new FileReader("src/main/resources/lastNameData.txt"));
    	br.readLine();
    	
    	List<Long> responseTimeList = new ArrayList<>();
    	String lastName;
    	while ((lastName = br.readLine()) != null) {
    		if(isCacheEnabled)
    			client.search()
                    .forResource("Patient")
                    .where(Patient.FAMILY.matches().value(lastName))
                    .returnBundle(Bundle.class)
                    .execute();
    		else
    			client.search()
                .forResource("Patient")
                .where(Patient.FAMILY.matches().value(lastName))
                .returnBundle(Bundle.class)
                .cacheControl(new CacheControlDirective().setNoCache(true))
                .execute();
    		
    		responseTimeList.add(t.getResponseTime());
    	}
    	
    	br.close();
    	
    	OptionalDouble avgResponseTime = responseTimeList.stream().mapToLong(l -> l).average();
    	return avgResponseTime.getAsDouble();
    }
}
