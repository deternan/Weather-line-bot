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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
	// AI.API
	// Key
	private String key_str = "8f40587522484b42a2d070bf8783e870";
	// Parameters
	private String AI_Location;
	private String AI_TimeDescription;
	private String AI_Weather_query;
	private String AI_weather_action = "query.weather.action";

	// XML Parser
	// Get
	private final String USER_AGENT = "Mozilla/5.0";
	// CWB open data
	private String dataid = "O-A0003-001";;
	private String key = "CWB-2D3A0C17-2350-4B0D-83BA-E7F7A45BFF6F";
	// Tag
	private String TEMP = "TEMP"; 	// 溫度
	private String HUMD = "HUMD"; 	// 濕度
	private String PRES = "PRES"; 	// 氣壓
	private String _24R = "24R"; 	// 當日累積雨量
	private String WDSD = "WDSD"; 	// 風力
	// value
	private double TEMP_value;
	private double HUMD_value;
	private double PRES_value;
	private double _24R_value;
	private double WDSD_value;
	// Input
	private String input_location;
	
	// AI.Output
	private String get_return = "";
	
	// Key
	AIConfiguration configuration = new AIConfiguration(key_str);
	AIDataService dataService = new AIDataService(configuration);
	
	public static void main(String[] args) 
    {        
    	SpringApplication.run(EchoApplication.class, args);
    }

    @EventMapping
    public TextMessage handleTextMessageEvent(MessageEvent<TextMessageContent> event) 
    {
        System.out.println("event: " + event);		
        String line;
        
        // AI
        //while (null != (line = event.getMessage().getText())) 
//        {
//        	line = event.getMessage().getText();
//        	
//        	try {
//  	          AIRequest request = new AIRequest(line);
//  	          AIResponse response = dataService.request(request);
//
//  	          if (response.getStatus().getCode() == 200) {
//  	            //System.out.println(response.getResult().getFulfillment().getSpeech());
//  	            get_return = response.getResult().getFulfillment().getSpeech();
//  	          } else {
//  	            //System.err.println(response.getStatus().getErrorDetails());
//  	        	get_return = response.getStatus().getErrorDetails();
//  	          }
//  	        } catch (Exception ex) {
//  	          ex.printStackTrace();
//  	        }        	
//        }
        
		//while (null != (line = reader.readLine())) 
		{
			line = event.getMessage().getText();
			
			try {
				get_return = "";
				AIRequest request = new AIRequest(line);
				AIResponse response = dataService.request(request);

				if (response.getStatus().getCode() == 200) {
					
					if (response.getResult().getParameters().get("Location") != null) {
						AI_Location = response.getResult().getParameters().get("Location").toString();
						AI_Location = AI_Location.substring(1, AI_Location.length() - 1);
						input_location = Location_change(AI_Location);
						// System.out.println(AI_Location+" "+input_location);
					}
					if (response.getResult().getParameters().get("Time_description") != null) {
						AI_TimeDescription = response.getResult().getParameters().get("Time_description").toString();
					}
					if (response.getResult().getParameters().get("Weather_query") != null) {
						AI_Weather_query = response.getResult().getParameters().get("Weather_query").toString();
					}
					// System.out.println(AI_Location+" "+AI_TimeDescription+"	"+AI_Weather_query);
					// System.out.println(response.getResult().getFulfillment().getSpeech());

					if (response.getResult().getAction().toString().equalsIgnoreCase(AI_weather_action)) {
						// System.out.println(response.getResult().getAction());
						XML_parser();
						Weather_query_answer(AI_Weather_query);
					}
				} else {
					get_return = response.getStatus().getErrorDetails();
					System.err.println(response.getStatus().getErrorDetails());
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
        
        
        
        if(get_return.length() == 0){
        	return new TextMessage("機器人的反應有點慢");
        }else{
        	return new TextMessage(get_return);
        }
        //return new TextMessage("Auto: "+get_return);
    }

    private void XML_parser() throws Exception
	{
		String url = "http://opendata.cwb.gov.tw/opendataapi?dataid="+ dataid +"&authorizationkey="+key;

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		// optional default is GET
		con.setRequestMethod("GET");
		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = con.getResponseCode();
//		System.out.println("\nSending 'GET' request to URL : " + url);
//		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		Parser(response.toString(), input_location);
	}
	
	private void Parser(String input_xmlstr, String input_location) throws Exception 
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = dbf.newDocumentBuilder();
		InputStream inputStream = new ByteArrayInputStream(input_xmlstr.getBytes());
		Document doc = builder.parse(inputStream);

		Element root = doc.getDocumentElement();
		NodeList location = root.getElementsByTagName("location");
//		System.out.println("location.getLength()	"+location.getLength()+"	"+input_location);
		for (int i = 0; i < location.getLength(); i++) 
		{
			//
			Element ele_all = (Element) location.item(i);
			// location name
			NodeList names = ele_all.getElementsByTagName("locationName");
			Element ele = (Element) names.item(0);
			Node node = ele.getFirstChild();
			// System.out.println(node.getNodeValue());

			// location name
			if (node.getNodeValue().toString().equalsIgnoreCase(input_location)) {
				//System.out.println(node.getNodeValue());
				NodeList subnames = ele_all.getElementsByTagName("weatherElement");
				// System.out.println(subnames.getLength());

				for (int j = 0; j < subnames.getLength(); j++) {
					Element ele_all_ = (Element) subnames.item(j);
					NodeList names_ = ele_all_.getElementsByTagName("elementName");
					Element ele_ = (Element) names_.item(0);
					Node node_ = ele_.getFirstChild();
					// System.out.println(names_.getLength());
//					System.out.println(node_.getNodeValue());
					//
					NodeList ev = ele_all.getElementsByTagName("value");
					Element ev_ele = (Element) ev.item(j);
					Node ev_node = ev_ele.getFirstChild();
					if (node_.getNodeValue().toString().equalsIgnoreCase(TEMP)) {
						TEMP_value = Double.parseDouble(ev_node.getNodeValue().toString());
						//System.out.println("elementValue	" + ev_node.getNodeValue());
					}
					if (node_.getNodeValue().toString().equalsIgnoreCase(HUMD)) {
						HUMD_value = Double.parseDouble(ev_node.getNodeValue());
						//System.out.println("elementValue	" + ev_node.getNodeValue());
					}
					if (node_.getNodeValue().toString().equalsIgnoreCase(PRES)) {
						//System.out.println("elementValue	" + ev_node.getNodeValue());
					}
					if (node_.getNodeValue().toString().equalsIgnoreCase(_24R)) {
						_24R_value = Double.parseDouble(ev_node.getNodeValue());
						//System.out.println("elementValue	" + ev_node.getNodeValue());
					}
					if (node_.getNodeValue().toString().equalsIgnoreCase(WDSD)) {
						//System.out.println("elementValue	" + ev_node.getNodeValue());
					}
				}
			}
		}
	}
	
	private String Location_change(String input)
	{
		String change_str = "";
		
		if(input.trim().equalsIgnoreCase("台北")){
			change_str = "臺北";
		}

		//System.out.println(input.length()+"	"+change_str);
		return change_str;
	}
	
	private void Weather_query_answer(String query_input)
	{
		if(query_input.length() > 0){
			query_input = query_input.substring(1, query_input.length()-1);
		}
		
		if(query_input.equalsIgnoreCase("下雨")){
			get_return = AI_Location+"濕度"+(HUMD_value*100)+"%; 累積雨量:"+_24R_value;
			System.out.println(AI_Location+"濕度"+(HUMD_value*100)+"%; 累積雨量:"+_24R_value);
		}else if(query_input.equalsIgnoreCase("溫度")){
			get_return = "我只會看有沒有下雨而已";
			System.out.println("我只會看有沒有下雨而已");
		}
	}
    
    
    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        System.out.println("event: " + event);
    }
	
    
}
