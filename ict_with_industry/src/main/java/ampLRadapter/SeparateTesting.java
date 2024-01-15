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
    		Label label = getResponse3(obs);
        }
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
