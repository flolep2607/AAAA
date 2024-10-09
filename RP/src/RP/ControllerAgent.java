package RP;

import static RP.Society.COMMUNITY;
import static RP.Society.GROUP;

import java.util.Random;
import java.util.logging.Level;

import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.Madkit;
import madkit.message.IntegerMessage;

public class ControllerAgent extends Agent {

    /**
     * Firstly, take a position in the artificial society
     */
    @Override
    public void activate() {
        getLogger().setLevel(Level.FINEST);
        createGroupIfAbsent(COMMUNITY, GROUP);
        requestRole(COMMUNITY, GROUP, "Controller");

        launchAgent(new CounterAgent());
        launchAgent(new CounterAgent());
        launchAgent(new EmitterAgent());
        launchAgent(new EmitterAgent());
        launchAgent(new EmitterAgent());
    }

    /**
     * Now ping the other agent with a message
     */
    @Override
    public void live() {
    	while(true) {
    		IntegerMessage msg = (IntegerMessage) waitNextMessage();
    		launchAgent(new CounterAgent(msg.getContent()));
        }
    }

    /**
     * @param args
     */
    public static void main(String[] argss) {
        executeThisAgent();
		//new Madkit("--launchAgents", ControllerAgent.class.getName() + ",true,1;","--launchAgents", CounterAgent.class.getName() + ",true,2;", EmitterAgent.class.getName() + ",true,2;");
        // or
        // String[] args = { "--launchAgents", PingAgent.class.getName() + ",true,2" };
        // Madkit.main(args);
    }

}