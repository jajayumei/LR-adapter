package ampLRadapter;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import PluginAdapter.Api.LabelOuterClass.Label;
import PluginAdapter.Api.LabelOuterClass.Label.Parameter;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import world.BeliefState;

public class SeparateTesting {
	public static SUTLabRecruits sut;
	
    public static void startRunning() {
    	sut = new SUTLabRecruits();
		sut.start();
        Scanner scanner = new Scanner(System.in);
        String instruction;
        BeliefState obs = null;
        int number;
        
        boolean invalidCommand = false;
        
        while (true) {
        	
//        	if (!invalidCommand) {
//        		try {
//            		sut.explore();
//            	} catch (Exception e) {
//            		System.out.println("Couldn't explore");
//                	e.printStackTrace();
//            	}
//        	}
//        	
        	invalidCommand = false;
        	
        	System.out.println("Enter an instruction: ");
            instruction = scanner.nextLine();
            System.out.println("The given instruction: " + instruction);
            
    		try {
    			switch (instruction) {
    				case "explore" : 
    					obs = sut.explore(); break;
    				case "button" :
    					System.out.println("Enter the button number: ");
    		            number = Integer.parseInt(scanner.nextLine());
    					obs = sut.pushButton(number); break;
    				case "door" :
    					System.out.println("Enter the door number: ");
    		            number = Integer.parseInt(scanner.nextLine());
    					obs = sut.approachOpenDoor(number); break;
					case "flag" :
						//System.out.println("Enter the flag number: ");
						//number = Integer.parseInt(scanner.nextLine());
						obs = sut.touchGoalFlag(); obs = sut.touchGoalFlag(); break;
    				default:
    					invalidCommand = true;
    			}
    		}
    		catch(Exception e) {
            	System.out.println("Found an error");
            	e.printStackTrace();
    		}
    		
    		if(!sut.testAgent.success()) {
    			System.out.println("The last goal was NOT solved.");
    		}
    		else {
//    			Label l = getResponse(obs);
//    			Label l2 = getResponse2(obs);
//    			Label l3 = getResponse3(obs);
    		}
    		Label l3 = getResponse3(obs);
        }
    }
    
    public static Label getResponse(BeliefState agentstate) {
    	
    	List<String> openDoors = new ArrayList<>() ;
    	List<String> buttons = new ArrayList<>() ;
    	boolean goalInSight = false;
    	
    	
    	for(WorldEntity e : agentstate.knownEntities()) {
    		
    		var entityType = e.type;
    		var entityId = e.id;
    		
    		switch (entityType) {
				case "Switch" : 
					buttons.add(entityId) ;
					break;
				case "Door" :
					if (agentstate.isOpen(entityId)) {
		    			openDoors.add(entityId) ;	    			
		    		}
					break;
				case "FireHazard" :
					break;
				case "Goal" :
					if (e.id.equals("Finish")) {
	    				System.out.println("YOU FOUND THE GOAL\n");
	    				goalInSight = true;
	        		}
					break;
    		}
    	}
		
		// converting to array:
    	String[] openDoors__ = new String[openDoors.size()] ; 
		int k = 0 ;
		for (String d : openDoors) {
			openDoors__[k] = d ;
			k++;
		}
		
    	String[] buttons__ = new String[buttons.size()] ;
		k = 0 ;
		for (String b : buttons) {
			buttons__[k] = b ;
			k++;
		}
		
		Map<String, String> parametersNamesAndTypes = new HashMap<>();
		parametersNamesAndTypes.put("_buttons", "[string]");
		parametersNamesAndTypes.put("_opendoors", "[string]");
		parametersNamesAndTypes.put("_health", "integer");
		parametersNamesAndTypes.put("_goal", "boolean");
		
		Map<String, Object> values = new HashMap<>();
		values.put("_buttons", buttons__);
		values.put("_opendoors", openDoors__);
		values.put("_health", agentstate.worldmodel().health);
		values.put("_goal", goalInSight);
		
		Label lab = AxiniProtobuf.createLabel2("observation", "agent", Label.LabelType.RESPONSE,
				parametersNamesAndTypes, values);
		
		System.out.println("\n1. THIS IS THE LABEL: ");
		System.out.println(lab + " \n");
		
//		return lab;
		return null;
	}
    
    public static Label getResponse2(BeliefState agentstate) {
    	
    	List<String> openDoors = new ArrayList<>() ;
    	List<String> buttons = new ArrayList<>() ;
    	boolean goalInSight = false;
    	
    	
    	for(WorldEntity e : agentstate.knownEntities()) {
    		
    		var entityType = e.type;
    		var entityId = e.id;
    		
    		switch (entityType) {
				case "Switch" : 
					buttons.add(entityId) ;
					break;
				case "Door" :
					if (agentstate.isOpen(entityId)) {
		    			openDoors.add(entityId) ;	    			
		    		}
					break;
				case "FireHazard" :
					break;
				case "Goal" :
					if (e.id.equals("Finish")) {
	    				System.out.println("YOU FOUND THE GOAL\n");
	    				goalInSight = true;
	        		}
					break;
    		}
    	}
		
    	List<Parameter.Value> doorList = new ArrayList<>();
		List<Parameter.Value> buttonList = new ArrayList<>();

		for (String d : openDoors) {
			var temp1 = AxiniProtobuf.createStringValue(d);
			doorList.add(temp1);
		}
		
		for (String b : buttons) {
			var temp2 = AxiniProtobuf.createStringValue(b);
			buttonList.add(temp2);
		}

		Parameter buttonParam = AxiniProtobuf.createArrayParameter("_buttons", buttonList);
		Parameter doorsParam = AxiniProtobuf.createArrayParameter("_opendoors", doorList);
		Parameter healthParam = AxiniProtobuf.createIntParameter("_health", agentstate.worldmodel().health);
		Parameter goalParam = AxiniProtobuf.createBooleanParameter("_goal", goalInSight);
		
		Label newLabel = Label.newBuilder()
				.setLabel("observation")
				.setType(Label.LabelType.RESPONSE)
				.setChannel("agent")
				.addParameters(buttonParam)
				.addParameters(doorsParam)
				.addParameters(healthParam)
				.addParameters(goalParam)
				.build();

		System.out.println("\n2. THIS IS THE OTHER LABEL: ");
		System.out.println(newLabel + " \n");

		return null;
	}
    
    public static Label getResponse3(BeliefState agentstate) {
    	
    	System.out.println(agentstate);
    	
    	var temp = agentstate.worldmodel().gameover; // true when goal reached or dead
    	var temp2 = agentstate.worldmodel().scoreGained;
    	var temp3 = agentstate.worldmodel().healthLost;
    	var temp4 = agentstate.changedEntities;
    	var temp5 = agentstate.usedHealingFlags;

    	
//    	System.out.println("HERE IS THE GAMEOVER: " + temp);
//    	System.out.println("HERE IS THE SCOREGAINED: " + temp2);
//    	System.out.println("HERE IS THE HEALTLOST: " + temp3);
//    	System.out.println("HERE IS THE SCORE: " + agentstate.worldmodel().score);
//    	System.out.println("HERE IS THE HEALTH: " + agentstate.worldmodel().health);
//    	System.out.println("HERE IS THE CHANGED ENTITIES: " + temp4);
//    	System.out.println("HERE IS THE USED HEALING FLAGS: " + temp5);

    	
    	List<Parameter.Value> openDoors = new ArrayList<>() ;
    	List<Parameter.Value> buttons = new ArrayList<>() ;
    	
    	boolean goalInSight = false;
    	
    	for(WorldEntity e : agentstate.knownEntities()) {
    		
    		System.out.println("ENTITY: " + e);
    		
    		var entityType = e.type;
    		var entityId = e.id;
    		
    		switch (entityType) {
				case "Switch" :
					buttons.add(AxiniProtobuf.createStringValue(entityId));
					break;
				case "Door" :
					if (agentstate.isOpen(entityId)) {
		    			openDoors.add(AxiniProtobuf.createStringValue(entityId));
		    		}
					break;
				case "FireHazard" :
					break;
				case "Goal" :
					if (e.id.equals("Finish")) {
	    				System.out.println("YOU FOUND THE GOAL\n");
	    				goalInSight = true;
	        		}
					break;
    		}
    	}
		
		Parameter buttonParam = AxiniProtobuf.createArrayParameter("_buttons", buttons);
		Parameter doorsParam = AxiniProtobuf.createArrayParameter("_opendoors", openDoors); 
		Parameter healthParam = AxiniProtobuf.createIntParameter("_health", agentstate.worldmodel().health);
		Parameter scoreParam = AxiniProtobuf.createIntParameter("_score", agentstate.worldmodel().score);
		Parameter goalParam = AxiniProtobuf.createBooleanParameter("_goal", goalInSight);
		
		Label newLabel = AxiniProtobuf.createLabel("observation", "agent", Label.LabelType.RESPONSE,
												   buttonParam, doorsParam, healthParam, goalParam);

//		System.out.println("\n3. THIS IS THE OTHER LABEL: ");
//		System.out.println(newLabel + " \n");

		return newLabel;
//		return null;
	}

    
    public static void main(String[] args) {
    	startRunning();
    }
}
