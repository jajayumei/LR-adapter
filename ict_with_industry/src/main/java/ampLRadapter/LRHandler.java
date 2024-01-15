package ampLRadapter;
// IMPLEMENT DEFAUTL CONFIG
// IMPLEMENT createArrayValue IN AXINIPROTOBUF
// AANSTUREN VAN SUT GEBEURT IN BESTAANDE HIER
// MAAR IK DENK DAT HET BETER IS DAT IN THE SUT ZELF TE DOEN

import java.util.*;

import java.net.URI;
import java.net.URISyntaxException;

//import com.axini.smartdooradapter.AxiniProtobuf;
//import com.axini.smartdooradapter.Configuration;
import com.google.protobuf.ByteString;

import PluginAdapter.Api.LabelOuterClass.*;
import PluginAdapter.Api.LabelOuterClass.Label.*;
import PluginAdapter.Api.ConfigurationOuterClass.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// I ADDED
import world.BeliefState;
import world.LabWorldModel;
//import ampPluginAdapter.KeyValuePair;
//import ampPluginAdapter.SUTLabRecruits;
//import ampPluginAdapter.protobuf.Api.ProtobufUtils;
//import ampPluginAdapter.protobuf.Api.LabelOuterClass.Label;
import eu.iv4xr.framework.mainConcepts.WorldEntity;

// SmartDoorHandler: SUT specific implementation of Handler.
public class LRHandler extends Handler {
    private static Logger logger = LoggerFactory.getLogger(LRHandler.class);

    public static final String RESET           = "RESET";
    public static final String RESET_PERFORMED = "RESET_PERFORMED";
    
    public SUTLabRecruits sut; //PROTECTED INSTEAD??
    
    public LRHandler() {
    	this.sut = new SUTLabRecruits();
    }
    
    private static final String SMARTDOOR_URL = "ws://localhost:3001";

    public Configuration defaultConfiguration() {
        Configuration.Item url =
            AxiniProtobuf.createItem("url",
                "WebSocket URL of SmartDoor SUT", SMARTDOOR_URL);
        List<Configuration.Item> items = new ArrayList<Configuration.Item>();
        items.add(url);
        return AxiniProtobuf.createConfiguration(items);
    }

    // Prepare to start testing.
    public void start() {
    	logger.info("HANDLER, STARTING THE PLUGIN ADAPTER");
    	sut.start();
    }

    // Stop testing.
    public void stop() {
        logger.info("HANDLER, STOPPING THE PLUGIN ADAPTER");
        sut.stop();
    }

    // Prepare for the next test case.
    public void reset() {
    	logger.info("HANDLER, RESETTING");
    	sut.reset();
        sendReadyToAmp();
    }

    // Stimulate the System Under Test with the stimulus.
    // Return the physical label.
    
    public Label stimulate(Label stimulus) {
        logger.info("Sending stimulus to SUT: " + stimulus.getLabel ());

        String stimulusName = stimulus.getLabel() ;
		BeliefState obs = null ;
		try {
			switch (stimulusName) {
			  case "explore" : 
				  obs = sut.explore() ; break ;
			  case "push_button" : 
				  String bnr = (String) stimulus.getParameters(0).getValue().getString() ;
				  obs = sut.pushButton(bnr);
				  break ;
			  case "pass_door" : 
				  String doornr = (String) stimulus.getParameters(0).getValue().getString() ;
				  obs = sut.approachOpenDoor(doornr);
				  break ;
			  case "finishlevel" :
				  sut.touchGoalFlag();
				  obs = sut.touchGoalFlag();
				  break ;
			}
		}
		// brokerConnection IS NOT PUBLIC
		catch(Exception e) {
			logger.info("LRHandler, The SUT-side throws an exception");
			e.printStackTrace();
			return null ;
		}
		if(!sut.testAgent.success()) {
			logger.info("LRHandler, The last goal is NOT solved.");
			
			// if agent dies
			if (obs.worldmodel().gameover) {
				System.out.println("YOU DIED");
				return getGameoverResponse(obs);
			}
			return null ;
		}
		// if goal flag was reached
		if (obs.worldmodel().gameover) {
			System.out.println("YOU FINISHED THE LEVEL");
		}
        return getTheDamnResponse(obs);
    }
    
    public Label getTheDamnResponse(BeliefState agentstate) {  	
    	List<Parameter.Value> openDoors = new ArrayList<>() ;
    	List<Parameter.Value> buttons = new ArrayList<>() ;
    	
    	boolean goalInSight = false;
    	
    	for(WorldEntity e : agentstate.knownEntities()) {
    		
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
//	    				System.out.println("YOU FOUND THE GOAL\n");
	    				goalInSight = true;
	        		}
					break;
    		}
    	}
    	
    	Collections.reverse(buttons);
		
		Parameter buttonParam = AxiniProtobuf.createArrayParameter("_buttons", buttons);
		Parameter doorsParam = AxiniProtobuf.createArrayParameter("_opendoors", openDoors); 
		Parameter healthParam = AxiniProtobuf.createIntParameter("_health", agentstate.worldmodel().health);
		Parameter scoreParam = AxiniProtobuf.createIntParameter("_score", agentstate.worldmodel().score);
		Parameter goalParam = AxiniProtobuf.createBooleanParameter("_goal", goalInSight);
		
		List<Parameter.Value> flagList = new ArrayList<>();
		for (String element : agentstate.usedHealingFlags) {
		    flagList.add(AxiniProtobuf.createStringValue(element));
		    System.out.println("HERE IS ONE OF YOUR FLAGSSSS: " + element);
		}
		Parameter flagParam = AxiniProtobuf.createArrayParameter("_usedflags", flagList);
		
		Label lab = AxiniProtobuf.createLabel("observation", "agent", Label.LabelType.RESPONSE,
												   buttonParam, doorsParam, healthParam, scoreParam, goalParam, flagParam);
		
		System.out.println("\nTHIS IS THE LABEL IM SENDING BACK: ");
		System.out.println(lab + " \n");
		return lab;
	}
    
    public Label getGameoverResponse(BeliefState agentstate) {
    	
    	Parameter healthParam = AxiniProtobuf.createIntParameter("_health", agentstate.worldmodel().health);
		Parameter scoreParam = AxiniProtobuf.createIntParameter("_score", agentstate.worldmodel().score);
		
		List<Parameter.Value> flagList = new ArrayList<>();
		for (String element : agentstate.usedHealingFlags) {
		    flagList.add(AxiniProtobuf.createStringValue(element));
		    System.out.println("HERE IS ONE OF YOUR FLAGSSSS: " + element);
		}
		
		Parameter flagParam = AxiniProtobuf.createArrayParameter("_usedflags", flagList);
		
		// Create response.
    	return AxiniProtobuf.createLabel("gameover", "agent", Label.LabelType.RESPONSE, healthParam, scoreParam, flagParam);
    }

    // Send Ready to AMP.
    public void sendReadyToAmp() {
        adapterCore.sendReady();
    }

    // Send an error to AMP.
    public void sendErrorToAmp(String message) {
        adapterCore.sendError(message);
    }

    // ----- Supported labels.
    public static final String[] STIMULI = { "explore", "finishlevel" };
    public static final String[] STIMULI_INT = {};
    public static final String[] STIMULI_STRING = { "push_button", "pass_door" };
    public static final String[] RESPONSES_DOUBLE_INT = { "gameover" };
    public static final String[] RESPONSES_INT = { "dummy" };
    public static final String[] RESPONSES_LISTS_AND_INT = { "observation" };

    // The labels supported by the adapter.
    public List<Label> getSupportedLabels() {
        String channel = "agent";
        List<Label> labels = new ArrayList<Label>();

        for (String name: STIMULI)
            labels.add(stimulus(name, channel));

        for (String name: STIMULI_INT)
            labels.add(stimulus(name, channel, intParameter()));
        
        for (String name: STIMULI_STRING)
            labels.add(stimulus(name, channel, stringParameter()));
        
        for (String name: RESPONSES_DOUBLE_INT)
            labels.add(response(name, channel, doubleInt_array_Parameter()));

        for (String name: RESPONSES_INT)
            labels.add(response(name, channel, intParameter()));
        
        for (String name: RESPONSES_LISTS_AND_INT)
            labels.add(response(name, channel, listsAndIntParameter()));

        // extra stimulus to reset the SUT
        labels.add(stimulus("reset", channel));

        return labels;
    }

    // Some helper methods to construct Protobuf objects for supported labels.

    private static Label stimulus(String name, String channel) {
        return AxiniProtobuf.createStimulus(name, channel);
    }

    private static Label stimulus(String name, String channel, List<Label.Parameter> params) {
        return AxiniProtobuf.createStimulus(name, channel, params);
    }
    
    private static Label response(String name, String channel, List<Label.Parameter> params) {
        return AxiniProtobuf.createResponse(name, channel, params);
    }

    private static List<Label.Parameter> intParameter() {
        List<Label.Parameter> list = new ArrayList<Label.Parameter>();
        Label.Parameter parameter =
            AxiniProtobuf.createParameter("_number", AxiniProtobuf.createIntValue(0)); // IS 0 DEFAULT??
        list.add(parameter);
        return list;
    }
    
    private static List<Label.Parameter> stringParameter() {
        List<Label.Parameter> list = new ArrayList<Label.Parameter>();
        Label.Parameter parameter =
            AxiniProtobuf.createParameter("_name", AxiniProtobuf.createStringValue("")); // IS 0 DEFAULT??
        list.add(parameter);
        return list;
    }
    
    private static List<Label.Parameter> doubleInt_array_Parameter() {
        List<Label.Parameter> list = new ArrayList<Label.Parameter>();
        
        Label.Parameter.Value zeroValue = AxiniProtobuf.createIntValue(0);
        Label.Parameter parameter1 = AxiniProtobuf.createParameter("_health", zeroValue); // IS 0 DEFAULT??
        Label.Parameter parameter2 = AxiniProtobuf.createParameter("_score", zeroValue);
        Label.Parameter parameter3 = AxiniProtobuf.createParameter("_usedflags", AxiniProtobuf.createArrayValue(AxiniProtobuf.createStringValue("")));
        
        list.add(parameter1);
        list.add(parameter2);
        list.add(parameter3);
        return list;
    }
    
    private static List<Label.Parameter> listsAndIntParameter() {
    	List<Label.Parameter> list = new ArrayList<Label.Parameter>();
        
        Label.Parameter.Value zeroValue = AxiniProtobuf.createIntValue(0);
        Label.Parameter.Value emptyString = AxiniProtobuf.createStringValue("");
        
        Label.Parameter param1 = AxiniProtobuf.createParameter("_buttons", AxiniProtobuf.createArrayValue(emptyString));
        Label.Parameter param2 = AxiniProtobuf.createParameter("_opendoors", AxiniProtobuf.createArrayValue(emptyString));
        Label.Parameter param3 = AxiniProtobuf.createParameter("_health", zeroValue);
        Label.Parameter param4 = AxiniProtobuf.createParameter("_score", zeroValue);
        Label.Parameter param5 = AxiniProtobuf.createParameter("_goal", AxiniProtobuf.createBooleanValue(false));
        Label.Parameter param6 = AxiniProtobuf.createParameter("_usedflags", AxiniProtobuf.createArrayValue(emptyString));
        
        list.add(param1);
        list.add(param2);
        list.add(param3);
        list.add(param4);
        list.add(param5);
        list.add(param6);
        
        return list;
    }
}
