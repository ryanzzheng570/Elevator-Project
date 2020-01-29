package Elevator;
import Common.*;
import Enum.*;
public class ElevatorSubsystemTestable extends ElevatorSubsystem{
	public int getCurrentFloor() {
		return currentFloor;
	}
	public int getTopFloor() {
		return topFloor;
	}
	public Direction getDirection() {
		return dir;
	}
	public ElevatorState getState() {
		return state;
	}
	public int getPort() {
		return port;
	}
}
