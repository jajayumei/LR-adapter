package ampLRadapter;

import static agents.TestSettings.USE_INSTRUMENT;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import agents.tactics.GoalLib;
import agents.tactics.TacticLib;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import game.LabRecruitsTestServer;
import world.BeliefState;
import world.LabEntity;

import static nl.uu.cs.aplib.AplibEDSL.*;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.mainConcepts.Goal;

//IKKE
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.iv4xr.framework.spatial.Vec3; //I ADDED
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import world.LabWorldModel;


// MAYBE WRITE MY ONW VERSION GoalStructure G = GoalLib.entityInCloseRange2();
// BECAUSE OF THE healWhenSeeingAFlag() BEHAVIOUR(?)

/**
 * This class will connect to a running Lab Recruits client and load a level.
 * The class provides several high-level interaction with the game.
 */
public class SUTLabRecruits {
	private static Logger logger =
	        LoggerFactory.getLogger(SUTLabRecruits.class);
	

	public static String levelName = "maze" ;

	public static String agentId = "agent1" ;
	
	/**
	 * The maximum number of agent-update-cycles to execution a single
	 * AMP-transition.
	 */
	public static int singleTransitionBudget = 100 ; //USED TO BE 300
	public static int sleepTimeBetweenUpdates = 50 ;
	public static String doorNamePrefix = "door" ;
	public static String buttonNamePrefix = "button" ;
	public static String goalFlagName = "leveldone" ;

    private LabRecruitsTestServer labRecruitsTestServer;
    
    public LabRecruitsTestAgent testAgent ;
	
    public SUTLabRecruits() { 
    	// Use a running LR:
    	TestSettings.USE_SERVER_FOR_TEST = false ;
    	// Uncomment this to make the game's graphic visible:
//    	TestSettings.USE_GRAPHICS = true ;
    	String labRecruitesExeRootDir = System.getProperty("user.dir") ;
    	labRecruitsTestServer = TestSettings.start_LabRecruitsTestServer(labRecruitesExeRootDir) ;   	
    }
      
    public synchronized void start() {
    	logger.info("started");
		this.reset();
    }
    
    public synchronized void reset() {  
    	logger.info("reset; reloading level");
    	// Create a fresh environment ; this will reload the level to test:
    	var config = new LabRecruitsConfig(levelName) ;
    	//config.light_intensity = 0.5f ;
    	var environment = new LabRecruitsEnvironment(config);
    	// Create a fresh agentL
    	testAgent = new LabRecruitsTestAgent(agentId) // matches the ID in the CSV file
    		    . attachState(new BeliefState())
    		    . attachEnvironment(environment)
    		    . setTestDataCollector(new TestDataCollector())
    		    ;
    }
    
    public synchronized void stop() { 
    	logger.info("stop");
    	if(labRecruitsTestServer!=null) 
    		labRecruitsTestServer.close(); 
    }
    
    /**
     * This will wipe the agent's memory on seen navigation nodes and game-entities,
     * and then it will explore the level as much as it can, and without interacting
     * with any button.
     * It returns the BeliefState at the end of the exploration.
     */
    public synchronized BeliefState explore() throws InterruptedException {
    	logger.info("exploring...");
    	// wipe memorized navigation nodes and entities:
    	testAgent.getState().pathfinder().wipeOutMemory();
    	testAgent.getState().worldmodel().elements.clear();
    	Goal g = goal("keep exploring...") 
    			.toSolve_(S -> false)
    			.withTactic(
    				FIRSTof(
    				  TacticLib.explore(), 
    				  ABORT()
    				)
    			 ) ;
    	GoalStructure G = FIRSTof(
    			g.lift(),
    			SUCCESS()
    		) ;
    	runAgent(G,"exploring") ;
    	return observe() ;
    }
    
    /**
     * Run the agent to try to solve the given goal-structure. The budget to
     * do this (in terms of the maximum number of agent-update-cycles. Is
     * limited by {@link #singleTransitionBudget}.
     */
    void runAgent(GoalStructure G, 
    		String goalStructureName) 
    	throws InterruptedException {
    	logger.info("Deploying a goal-structure: " + goalStructureName);
    	testAgent.setGoal(G) ;
    	int i = 0 ;
    	while(G.getStatus().inProgress()) {
    		// TEMP VERWIJDERD
    		System.out.println("*** " + i + ", " + testAgent.state().id + " @" + testAgent.state().worldmodel.position) ;
            Thread.sleep(sleepTimeBetweenUpdates);
            i++ ;
        	testAgent.update();
        	if (i>singleTransitionBudget) {
        		break ;
        	}
    	}
    	if (G.getStatus().success()) {
    		logger.info("" + goalStructureName + " was succesful.");
    	}
    	else {
    		logger.info("The agent COULD NOT solve: " + goalStructureName);
    	}
    }
    
    public BeliefState observe() {
    	logger.info("observe");
    	
    	var temp = testAgent.getState() ;
    	
//    	System.out.println("HERE IS YOUR SCORE: " + testAgent.state().worldmodel().score);
//    	System.out.println("HERE IS YOUR STATE: " + temp);
    	
//    	var flags = temp.knownEntities();
//    	for(WorldEntity e : flags) {
//    		System.out.println("HERE IS AN ENTITY: " + e);
//    		System.out.println("THIS IS THE ID: " + e.id);
//    		System.out.println("THESE ARE THE PROPERTIES: " + e.properties);
//    		System.out.println("THIS IS THE TYPE: " + e.type);
//    		System.out.println();
//    	}

    	return temp;
    }
    
    /**
     * This will make the agent to go to the given button, and interact with it.
     * Note that the agent can only go to a button that is physically reachable
     * for it.
     * It returns the agent's BeliefState at the end.
     */
    public synchronized BeliefState pushButton(String button) throws InterruptedException {
    	logger.info("going to push button: " + button);
    	GoalStructure G = GoalLib.entityInteracted(button) ;
    	runAgent(G,"push-button" + button) ;
    	return observe() ;
    }
    
    public synchronized BeliefState pushButton(int button) throws InterruptedException {
    	logger.info("going to push button" + button);
    	GoalStructure G = GoalLib.entityInteracted(buttonNamePrefix + button) ;
    	runAgent(G,"push-button" + button) ;
    	return observe() ;
    }
    
    /**
     * This will make the agent to approach the given door, until the agent can see it.
     * Note that the agent can only go to a door that is physically reachable
     * for it.
     * It returns the agent's BeliefState at the end.
     */
    public synchronized BeliefState approachOpenDoor(String door) throws InterruptedException {
    	logger.info("going to approach door: " + door);
    	GoalStructure G = GoalLib.entityInCloseRange2(door) ;
    	runAgent(G,"approach-door" + door) ;
    	return observe() ;
    }
    
    public synchronized BeliefState approachOpenDoor(int door) throws InterruptedException {
    	logger.info("approaching door" + door);
		GoalStructure G = GoalLib.entityInCloseRange2(doorNamePrefix + door) ;

    	runAgent(G,"approach-door " + door) ;
    	return observe() ;
    }
    
    /**
     * This should make the agent to go to a/the goal-flag and touch it.
     * Note that the agent can only go to a door that is physically reachable
     * for it.
     * It returns the agent's BeliefState at the end.
     */
    public synchronized BeliefState touchGoalFlag() throws InterruptedException {
    	logger.info("approaching goal flag");
		GoalStructure G = GoalLib.entityInCloseRange2("Finish");

		runAgent(G,"approach-goalflag") ;
    	return observe() ;
    }

	public static GoalStructure flagReached(String entityId) {
		//the first goal is to navigate to the entity:
		var goal1 =
				goal(String.format("This entity is in interaction distance: [%s]", entityId))
						. toSolve((BeliefState belief) -> {
							var e = (LabEntity) belief.worldmodel.getElement(entityId) ;
							// bug .. .should be distsq:
							// return e!=null && Vec3.dist(belief.worldmodel.getFloorPosition(), e.getFloorPosition()) < 0.35 ;
							// System.out.print("entityinteracted: navigate to" + e);
							return e!=null && Vec3.sub(belief.worldmodel().getFloorPosition(), e.getFloorPosition()).lengthSq() <= 1 ;
						})
						. withTactic(
								FIRSTof( //the tactic used to solve the goal
										TacticLib.navigateTo(entityId), //try to move to the entity
										TacticLib.explore(), //find the entity
										ABORT()))
						. lift();
		
		return SEQ(goal1, SUCCESS());// adding success() to force observation after the interaction goal2,
	}
}
