package controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.Message;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Color;

public class Messages extends Controller {
	
	private static Map<String, String> userColors = new ConcurrentHashMap<String, String>();
	
	private static List<Message> messages = new ArrayList<Message>();
	private static Set<String> mentionedUsers = new HashSet<String>();
	
	public static Result create(){
		Message message = Json.fromJson(request().body().asJson(), Message.class);
		messages.add(message);
		
		checkAndAssignUserColor(message);
		checkAndAddHashedUsers(message);
		return ok(Json.toJson(message));
	}
	
	private static void checkAndAssignUserColor(Message message) {
		String userName = message.getUserName();
		if (!userColors.containsKey(userName)){
			String color;
			do{
				color = Color.getRandomColor();
			}while(userColors.containsValue(color));
			
			userColors.put(userName, color);
			message.setColor(color);
		}else{
			message.setColor(userColors.get(userName));
		}
	}

	private static void checkAndAddHashedUsers(Message message) {
		String messageText = message.getMessage();
		Matcher matcher = Pattern.compile("#\\w+").matcher(messageText);
		 while(matcher.find()){
			mentionedUsers.add(matcher.group().substring(1)); 
		 }
	}

	public static Result getNewMessages(){
		response().setHeader("Access-Control-Allow-Origin", "*");
	
		List<Message> messagesToSend = new ArrayList<Message>(messages);
		messages.clear();

		if(messagesToSend.isEmpty()) return noContent();
		return ok(Json.toJson(messagesToSend)); 
	}
	
	public static Result isUserMention(String userName){
		response().setHeader("Access-Control-Allow-Origin", "*");
		
		while (mentionedUsers.iterator().hasNext()) {
			String user = mentionedUsers.iterator().next();
			if(user.equalsIgnoreCase(userName)){
				mentionedUsers.remove(user);
				return ok("true");
			}
		}
		return ok("false");
	}
}