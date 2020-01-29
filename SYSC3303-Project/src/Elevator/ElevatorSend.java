package Elevator;
import java.io.Serializable;
import Enum.*;
/**
 * Class:								ElevatorCommand
 * Description:							Serializable object representing outgoing communications from Elevator
 * 
 * @author								Ryan Fournier, Zhexiao Huang
 * @version 							0.0.1
 * @since 								2019/01/31
 */
public class ElevatorSend implements Serializable {
	public int floor;
	public Toggle[] buttons;
	public ElevatorState state;

	/**
	 * Method:							ElevatorSend
	 * Description:						Constructor
	 * @param f							current floor
	 * @param state						elevator state
	 */
	public ElevatorSend(int f, ElevatorState s, Toggle[] b) {
		this.floor = f;
		this.state = s;
		this.buttons = b;
	}
	
	/**
	 * Method:							toString
	 * Description:						Serialize object to string for logging
	 * @return							String representation of object
	 */
	public String toString() {
		return "Floor = " + floor + ", buttons = " + buttons.toString() + ", state = " + state;
	}
}
