import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

public class AI_test 
{
	private static final String INPUT_PROMPT = "> ";
	private static final int ERROR_EXIT_CODE = 1;
	// Key
	private String key_str = "";
	
	public AI_test()
	{
		// API keys
		AIConfiguration configuration = new AIConfiguration(key_str);
	    AIDataService dataService = new AIDataService(configuration);
	    
	    String line;

	    try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
	      System.out.print(INPUT_PROMPT);
	      while (null != (line = reader.readLine())) 
	      {
	        try {
	          AIRequest request = new AIRequest(line);
	          AIResponse response = dataService.request(request);

	          if (response.getStatus().getCode() == 200) {
	            System.out.println(response.getResult().getFulfillment().getSpeech());
	          } else {
	            System.err.println(response.getStatus().getErrorDetails());
	          }
	        } catch (Exception ex) {
	          ex.printStackTrace();
	        }

	        System.out.print(INPUT_PROMPT);
	      }
	    } catch (IOException ex) {
	      ex.printStackTrace();
	    }
	    System.out.println("See ya!");
	}
	
	private static void showHelp(String errorMessage, int exitCode) 
	{
	    if (errorMessage != null && errorMessage.length() > 0) {
	      System.err.println(errorMessage);
	      System.err.println();
	    }

	    System.out.println("Usage: APIKEY");
	    System.out.println();
	    System.out.println("APIKEY  Your unique application key");
	    System.out.println("        See https://docs.api.ai/docs/key-concepts for details");
	    System.out.println();
	    System.exit(exitCode);
	  }
	
	public static void main(String args[])
	{
				
		AI_test ai = new AI_test();
	}
	
}
