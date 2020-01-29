package Enum;

public enum ElevatorState {
	STANDBY(0),
	WAITING_TO_START(1),
	MOVING(3),
	OPEN(4);
	private final int state;
	ElevatorState(int state) { this.state = state; }
	public int getValue() { return this.state; }
}
