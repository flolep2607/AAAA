package RP;

import static RP.Society.COMMUNITY;
import static RP.Society.GROUP;

import java.util.Random;
import java.util.logging.Level;

import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.Madkit;
import madkit.kernel.Message;
import madkit.message.StringMessage;

/**
 * Shows how agents exchange messages.
 *
 *
 *
 *
 *
 *
 * To exchange messages, agents have to exist in an artificial society. That is, agents have to create communities
 * containing groups and take roles within to interact with others. Doing so, agents get agent addresses which could be
 * used to send them messages. Here, two agents take a role and ping the other one.
 */
public class EmitterAgent extends Agent {

    /**
     * Firstly, take a position in the artificial society
     */
    @Override
    public void activate() {
        getLogger().setLevel(Level.FINEST);

        createGroupIfAbsent(COMMUNITY, GROUP);
        requestRole(COMMUNITY, GROUP, "Emitter");

    }

    /**
     * Now ping the other agent with a message
     */
    @Override
    public void live() {
        // method seen in organization's tutorial
        AgentAddress other = null;
        
        getLogger().info("\n\tI found someone !!\n" + other + "\n\n");
        int rand_int = 3+new Random().nextInt(5);
        for(int i=0;i<rand_int;i++) {
        	while (other == null) {
                other = getAgentWithRole(COMMUNITY, GROUP, "Counter");
                pause(10);
            }
            sendMessage(other, new Message());
            int rand_wait = new Random().nextInt(5);
            pause(rand_wait+1);
        }
        killAgent(this);
    }

}
