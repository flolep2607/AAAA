package RP;

import static RP.Society.COMMUNITY;
import static RP.Society.GROUP;

import java.util.Random;
import java.util.logging.Level;

import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.Madkit;
import madkit.kernel.Message;
import madkit.message.IntegerMessage;
import madkit.message.StringMessage;

public class CounterAgent extends Agent {
	int counter;

	public CounterAgent(){
		super();
		counter = 0;
	}
	public CounterAgent(Integer value){
		super();
		counter = value;
	}

    /**
     * Firstly, take a position in the artificial society
     */
    @Override
    public void activate() {
        getLogger().setLevel(Level.FINEST);

        createGroupIfAbsent(COMMUNITY, GROUP);
        requestRole(COMMUNITY, GROUP, "Counter");

        pause(500);
    }

    /**
     * Now ping the other agent with a message
     */
    @Override
    public void live() {
        // method seen in organization's tutorial

        while(waitNextMessage(5000) != null) {
        	counter++;
        }
        
        AgentAddress other = null;
        while (other == null) {
            other = getAgentWithRole(COMMUNITY, GROUP, "Controller");
            pause(100);
        }
        
        getLogger().info("\n\tI send my counter to my supervisor :" + other +"counter:"+counter+"\n\n");
        sendMessage(other, new IntegerMessage(counter));
        pause(new Random().nextInt(500));
        launchAgent(new EmitterAgent());
        killAgent(this);
        //new Madkit("--launchAgents", CounterAgent.class.getName() + ",true,1;");
    }

}
