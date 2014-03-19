package quoridor.backend.states;

import java.awt.Image;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import quoridor.backend.containers.Player;
import quoridor.backend.pieces.Pawn;
import quoridor.gui.board.GameBoard;
import quoridor.main.Quoridor;

/**
 * @author Team 4 Men And A Cripple
 * 
 * This class represents a state that begins a game of Quoridor.
 */
public class Init implements State {

    /**
     * The transitions that exist within this state.
     */
    private HashMap<Boolean, State> transitions;

    /**
     * The possible starting positions for pawns.
     */
    private String[] startingPos;
    
    /**
     * The images to assign to each pawn.
     */
    private final Image [] pawns = new Image[4];

    /**
     * Constructs a new init state and loads resources.
     */
    public Init() {
        transitions = new HashMap<Boolean, State>();
        startingPositions();
        transitions.put(true, new Turn());
        pawns[0] = Toolkit.getDefaultToolkit().createImage("res/pawn_dark.png");
		pawns[1] = Toolkit.getDefaultToolkit().createImage("res/pawn_light.png");
		pawns[2] = Toolkit.getDefaultToolkit().createImage("res/pawn_blue.png");
		pawns[3] = Toolkit.getDefaultToolkit().createImage("res/pawn_red.png");
    }
    
    /*
     * Checks how many players are playing, setting the starting positions
     */
    public void startingPositions(){
    	if(Quoridor.getHosts().length == 4){
    		startingPos = new String[4];
    		startingPos[0] = "E1";
    		startingPos[1] = "I5";
    		startingPos[2] = "E9";
    		startingPos[3] = "A5";
    	}
    	else{
    		startingPos = new String[2];
    		startingPos[0] = "E1";
    		startingPos[1] = "E9";
    	}
    }
    
    /*
     * Checks if the player name was too big and only takes the substring(0,15)
     * if the player doesn't enter a name, a default name is given
     *
     *@param String name to test against
     *@param int playerNumber to give the player name a number 
     *
     */
    public String checkPlayerName(String name, int playerNumber){
    	if(name.equals(""))
    		return "Player " + playerNumber;
    	else if(name.length() > 20)
    		return name.substring(0,15);
    	else
    		return name;
    }

    /* (non-Javadoc)
     * @see quoridor.backend.states.State#execute()
     */
    @Override
    public boolean execute() {
        Quoridor.newGameState();
        // TODO: Some indication that it is trying to connect to hosts
        Queue<String> rejected = new LinkedList<String>();
        for(int i = 0; i < Quoridor.getHosts().length; i++) {
            Pawn p = new Pawn(startingPos[i],pawns[i]);
            if(p.startNetwork(Quoridor.getHosts()[i])){
                Quoridor.getGameState().addPawn(p);
                p.getClient().sendString("Enter Player Name");
                String name = p.getClient().getString();
                name = checkPlayerName(name,i+1);
                Player player = new Player(name,p.getPosition().toString(),Quoridor.getHosts().length);
                Quoridor.getGameState().addPlayer(player);
            }
            else
                rejected.add(Quoridor.getHosts()[i]);
        }
        Quoridor.getGUI().setPanel(new GameBoard(Quoridor.getHosts().length));
        while(!rejected.isEmpty())
            Quoridor.getGUI().getPanel().writeToConsole("Connection to " +
                                                         rejected.remove() +
                                                         " could not be established.");
        return true;
    }

    /* (non-Javadoc)
     * @see quoridor.backend.states.State#transition(boolean)
     */
    @Override
    public State transition(boolean b) {
        return transitions.get(b);
    }

}
