package Elevator;
import java.io.Serializable;
import Enum.*;
/**
 * Class:								ElevatorCommand
 * Description:							Serializable object representing incoming communications to the elevator
 * 
 * @author								Ryan Fournier, Zhexiao Huang
 * @version 							0.0.1
 * @since 								2019/01/31
 */
public class ElevatorCommand implements Serializable{
	public Direction direction;
	public ElevatorDoorCommand doorCmd;
	public ElevatorMotorCommand motorCmd;
	
	/**
	 * Method:							ElevatorCommand
	 * Description:						Constructor
	 * @param newDirection				Direction of movement
	 */
	public ElevatorCommand(Direction newDirection) {
		this.direction = newDirection;
	}

	/**
	 * Method:							ElevatorCommand
	 * Description:						Constructor
	 * @param cmd						Door command
	 */
	public ElevatorCommand(ElevatorDoorCommand cmd) {
		this.doorCmd = cmd;
	}

	
	/**
	 * Method:							ElevatorCommand
	 * Description:						Constructor
	 * @param cmd						Motor command
	 */
	public ElevatorCommand(ElevatorMotorCommand cmd) {
		this.motorCmd = cmd;
	}

	
	/**
	 * Method:							toString
	 * Description:						Serialize object to string for logging
	 * @return							String representation of object
	 */
	public String toString() {
		return "Direction = " + direction + ", Door = " + doorCmd + ", Motor = " + motorCmd;
	}
}