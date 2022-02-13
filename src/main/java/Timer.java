import java.io.IOException;

import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import ca.uhn.fhir.util.StopWatch;

public class Timer implements IClientInterceptor{
	private StopWatch timer;
	
	public Timer() {
		timer = new StopWatch();
	}
	
	@Override
	public void interceptResponse(IHttpResponse theResponse) throws IOException {
		timer.endCurrentTask();
	}
	
	@Override
	public void interceptRequest(IHttpRequest theRequest) {
		timer.startTask("Intermediate task");
	}
	
	public long getResponseTime() {
		return timer.getMillisAndRestart();
	}
}
