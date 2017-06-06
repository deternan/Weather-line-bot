/*
 * Copyright 2016 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.example.bot.spring.echo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

@SpringBootApplication
@LineMessageHandler
public class EchoApplication 
{
	// Key
	AIConfiguration configuration = new AIConfiguration("8f40587522484b42a2d070bf8783e870");
	AIDataService dataService = new AIDataService(configuration);
	
	public static void main(String[] args) 
    {        
    	SpringApplication.run(EchoApplication.class, args);
    }

    @EventMapping
    public TextMessage handleTextMessageEvent(MessageEvent<TextMessageContent> event) 
    {
        //System.out.println("event: " + event);
		// CJKV check
		//String get_return = CJKV_check(event.getMessage().getText());
        // Default (the same input)
        //String get_return = event.getMessage().getText();
        //return new TextMessage(get_return);
        String line;
        String get_return = "";
        // AI
        while (null != (line = event.getMessage().getText())) 
        {
        	try {
  	          AIRequest request = new AIRequest(line);
  	          AIResponse response = dataService.request(request);

  	          if (response.getStatus().getCode() == 200) {
  	            //System.out.println(response.getResult().getFulfillment().getSpeech());
  	            get_return = response.getResult().getFulfillment().getSpeech();
  	          } else {
  	            //System.err.println(response.getStatus().getErrorDetails());
  	        	get_return = response.getStatus().getErrorDetails();
  	          }
  	        } catch (Exception ex) {
  	          ex.printStackTrace();
  	        }        	
        }   
        return new TextMessage(get_return);
    }

    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        System.out.println("event: " + event);
    }
	
    
//	public String CJKV_check(String input_str)
//	{
//		boolean check;
//		String return_str = "";
//		check = input_str.codePoints().anyMatch(codepoint ->
//	            Character.UnicodeScript.of(codepoint) == Character.UnicodeScript.HAN);
//		
//		//System.out.println(check);
//		if(check == true){
//			return_str = "Non English";
//		}else{
//			return_str = "English";
//		}
//		
//		return return_str;
//	}
}
