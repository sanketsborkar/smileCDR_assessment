import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import ca.uhn.fhir.rest.client.api.IGenericClient;

@RunWith(MockitoJUnitRunner.class)  
public class SampleClientTest {

	@Mock
	SampleClient sampleClient;
	
	@Test
	public void testBasicTasks() {
	    Mockito.doNothing().when(sampleClient).basicTasks(Mockito.any(IGenericClient.class));
	    
	    IGenericClient iGenericClient = Mockito.mock(IGenericClient.class, Mockito.RETURNS_DEEP_STUBS);
	    Bundle response = Mockito.mock(Bundle.class, Mockito.RETURNS_DEEP_STUBS);
	    Mockito.lenient().when(iGenericClient
                 .search()
                 .forResource(Mockito.anyString())
                 .where(Patient.FAMILY.matches().value(Mockito.anyString()))
                 .returnBundle(Bundle.class)
                 .execute())
	    		 .thenReturn(response);
	    Mockito.lenient().when(response.getEntry().stream().map(e -> e.getResource()).filter(r -> r instanceof Patient).map(r -> (Patient) r).collect(Collectors.toList()))
	    	.thenReturn(new ArrayList<Patient>());
	    
	    sampleClient.basicTasks(iGenericClient);
	 
	    Mockito.verify(sampleClient, Mockito.times(1)).basicTasks(iGenericClient);
	}

	@Test
	public void testBasicTasksWithNoResponse() {
	    Mockito.doNothing().when(sampleClient).basicTasks(Mockito.any(IGenericClient.class));
	    
	    IGenericClient iGenericClient = Mockito.mock(IGenericClient.class, Mockito.RETURNS_DEEP_STUBS);
	    Mockito.lenient().when(iGenericClient
                 .search()
                 .forResource(Mockito.anyString())
                 .where(Patient.FAMILY.matches().value(Mockito.anyString()))
                 .returnBundle(Bundle.class)
                 .execute())
	    		 .thenReturn(null);
	    
	    sampleClient.basicTasks(iGenericClient);
	 
	    Mockito.verify(sampleClient, Mockito.times(1)).basicTasks(iGenericClient);
	}
	
	@Test
	public void testIntermediateTasks() throws IOException {
		Mockito.lenient().when(sampleClient.displayAverageResponseTime(Mockito.any(IGenericClient.class), Mockito.anyBoolean())).thenReturn(600.0);
		
		IGenericClient iGenericClient = Mockito.mock(IGenericClient.class);
		sampleClient.intermediateTasks(iGenericClient);
		 
	    Mockito.verify(sampleClient, Mockito.times(1)).intermediateTasks(iGenericClient);
		
	}
	
	@Test
	public void testDisplayAverageResponseTime() throws IOException {
		IGenericClient iGenericClient = Mockito.mock(IGenericClient.class, Mockito.RETURNS_DEEP_STUBS);
		Mockito.lenient().when(iGenericClient
                .search()
                .forResource(Mockito.anyString())
                .where(Patient.FAMILY.matches().value(Mockito.anyString()))
                .returnBundle(Bundle.class)
                .execute())
	    		.thenReturn(new Bundle());
		
		sampleClient.displayAverageResponseTime(iGenericClient, true);
	}
}
