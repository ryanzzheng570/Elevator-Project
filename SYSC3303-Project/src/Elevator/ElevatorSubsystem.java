package Elevator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import Common.*;
import Enum.*;
/**
 * Class:								ElevatorSubsystem
 * Description:							Controller module for a single elevator car
 * @author								Ryan Fournier, Zhexiao Huang
 * @version 							0.0.1
 * @since 								2019/01/30
 */
public class ElevatorSubsystem {
	protected static final int SCHEDULER_PORT = 1000;
	//protected static final int TOP_FLOOR = 20;
	protected static final int TIME_TO_CHANGE_FLOORS = 5000;
	protected static final int TIME_FOR_DOORS_TO_OPEN = 1000;
	protected static final int TIME_DOORS_OPEN = 3000;
	protected DatagramSocket receiveSocket;
	protected DatagramSocket sendSocket;
	protected Direction dir;
	protected ElevatorState state;
	protected int currentFloor;
	protected int topFloor;
	protected int port = Constant.ELEVATOR_MIN_PORT; 		//functionally, port can also act as elevator ID
	protected Toggle[] floorButtons; 		//each one represents a floor's button in the elevator (as well as its lamp)
	/**
	 * Method:							ElevatorSubsystem
	 * Description:						Constructor
	 */
	public ElevatorSubsystem() {
		//initialize base states
		currentFloor = 1;
		topFloor = 0;
		state = ElevatorState.STANDBY;
		//create send socket
		try {
			sendSocket = new DatagramSocket();
			System.out.println("Elevator: Successfully created send socket");
		} catch (SocketException e) {
			System.out.println("Elevator: Failed to create send socket");
			e.printStackTrace();
			System.exit(1);
		}
		//create receive socket
		while(port <= Constant.ELEVATOR_MAX_PORT) {
			try {
				//attempt socket creation on current possibly free port in range
				receiveSocket = new DatagramSocket(port);
				System.out.println("Elevator: Successfully created receive socket on PORT=" + port);
				break;
			} catch (SocketException e) {
				//if there are ports left in range, attempt next port
				if(port < Constant.ELEVATOR_MAX_PORT) {
					System.out.println("Elevator: Port " + port + " unavailable, trying " + (port + 1) + "...");
					port++;
				}
				//if there are no ports left in range, socket creation has failed
				else {
					System.out.println("Elevator: Failed to create receive socket on ALL PORTS="+Constant.ELEVATOR_MIN_PORT+"~"+Constant.ELEVATOR_MAX_PORT);
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		//Await knock from Scheduler
		try {
			//Listen for Floor total count from Scheduler as handshake initiation
			System.out.println("Elevator: Awaiting connection with Scheduler");
			byte[] buffer = new byte[10];
			DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
			receiveSocket.receive(inPacket);
			String floorString = new String(buffer,0,buffer.length);
			topFloor = Integer.parseInt(floorString.trim());
			Thread.sleep(1000);
			//Return OK response to complete handshake
			String responseString = "OK";
			byte[] data = responseString.getBytes();
			DatagramPacket outPacket = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), Constant.ELEVATOR_SYSTEM_PORT);
			sendSocket.send(outPacket);
			System.out.println("Elevator: Successfully connected with Scheduler managing Floor with NUMBER=" + topFloor);
		} catch(Exception e) {
			System.out.println("Elevator: Failed to connect with Scheduler managing Floor with NUMBER=" + topFloor);
			e.printStackTrace();
			System.exit(1);
		}
		//create Elevator buttons
		floorButtons = new Toggle[topFloor];
		for(Toggle t : floorButtons) {
			t = Toggle.OFF;
		}
	}

	/**
	 * Method:							standby
	 * Description:						State change for standby mode
	 * @param e							ElevatorSubsystem object to be changed
	 */
	protected void standby(ElevatorSubsystem e) {
		e.send();
		ElevatorCommand data = e.receive();

		if (data.doorCmd == ElevatorDoorCommand.OPEN) {
			state = ElevatorState.OPEN;
			e.OK();
		} else if (data.direction == Direction.UP || data.direction == Direction.DOWN) {
			dir = data.direction;
			e.OK();
		}
	}

	/**
	 * Method:							waitingToStart
	 * Description:						State change for waitingToStart mode
	 * @param e							ElevatorSubsystem object to be changed
	 */
	protected void waitingToStart(ElevatorSubsystem e) {
		e.send();
		
		ElevatorCommand data = e.receive();

		if (data.motorCmd == ElevatorMotorCommand.START) {
			state = ElevatorState.MOVING;
			e.OK();
		}
	}

	/**
	 * Method:							moving
	 * Description:						State change for moving mode
	 * @param e							ElevatorSubsystem object to be changed
	 */
	protected void moving(ElevatorSubsystem e) throws InterruptedException {
		Thread.sleep(TIME_TO_CHANGE_FLOORS);

		if (dir == Direction.UP) {
			currentFloor++;
		} else {
			currentFloor--;
		}

		e.send();
		ElevatorCommand data = e.receive();

		if (data.motorCmd == ElevatorMotorCommand.STOP) {
			state = ElevatorState.STANDBY;
			e.OK();
		}
	}

	/**
	 * Method:							open
	 * Description:						State change for open mode
	 * @param e							ElevatorSubsystem object to be changed
	 */
	protected void open(ElevatorSubsystem e) throws InterruptedException {
		Thread.sleep(TIME_FOR_DOORS_TO_OPEN);
		//ButtonPresser press = new ButtonPresser(e, e.manual);
		//press.start();
		Thread.sleep(TIME_DOORS_OPEN);
		e.send();
		ElevatorCommand data = e.receive();

		if (data.doorCmd == ElevatorDoorCommand.CLOSE) {
			Thread.sleep(TIME_FOR_DOORS_TO_OPEN);
			state = ElevatorState.WAITING_TO_START;
			e.OK();
		}
	}

	/**
	 * Method:							receive
	 * Description:						Receives command data from Scheduler
	 * @return							Command object
	 */
	protected ElevatorCommand receive() {
		System.out.println("RECEIVING");
		//ElevatorCommand msgReceive = new ElevatorCommand(Direction.UP);
		byte[] inData = new byte[256];
		DatagramPacket incoming = new DatagramPacket(inData, inData.length);
		try {
			receiveSocket.receive(incoming);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ElevatorCommand data = toEC(incoming.getData());
		return data;
	}

	/**
	 * Method:							send
	 * Description:						Sends state data to Scheduler
	 */
	protected void send() {
		System.out.println("SENDING "+ state);
		ElevatorSend msgToSend = new ElevatorSend(currentFloor, state, floorButtons);
		DatagramPacket outgoing = null;
		byte[] outData = toStream(msgToSend);
		try {
			outgoing = new DatagramPacket(outData, outData.length, InetAddress.getLocalHost(), Constant.ELEVATOR_SYSTEM_PORT);
		} catch (UnknownHostException e2) {
			e2.printStackTrace();
			System.exit(1);
		}

		boolean success = false;
		while (success == false) {
			success = true;
			try {
				sendSocket.send(outgoing);
			} catch (IOException e1) {
				success = false;
				System.out.println("Failed to send packet");
			}
		}
	}

	/**
	 * Method:							OK
	 * Description:						Send OK Flag to Scheduler
	 */
	protected void OK() {
		System.out.println("SENDING OK");
		DatagramPacket outgoing = null;
		try {
			outgoing = new DatagramPacket("OK".getBytes(), "OK".getBytes().length, InetAddress.getLocalHost(), Constant.ELEVATOR_SYSTEM_PORT);
		} catch (UnknownHostException e2) {
			e2.printStackTrace();
			System.exit(1);
		}

		try {
			sendSocket.send(outgoing);
		} catch (IOException e1) {
			System.out.println("Failed to send packet");
		}
	}

	/**
	 * Method:							toStream
	 * Description:						Serialize state objects to send to Scheduler
	 * @param ES						Object data to be prepared
	 */
	private static byte[] toStream(Object ES) {
		byte[] stream = null;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);) {
			oos.writeObject(ES);
			stream = baos.toByteArray();
			oos.close();
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stream;
	}

	/**
	 * Method:							toEC
	 * Description:						Deserialize command objects received from Scheduler
	 * @param stream					Byte data received from socket
	 */
	protected static ElevatorCommand toEC(byte[] stream) {
		ElevatorCommand ES = null;
		try (ByteArrayInputStream bais = new ByteArrayInputStream(stream);
				ObjectInputStream ois = new ObjectInputStream(bais);) {
			ES = (ElevatorCommand) ois.readObject();
			System.out.println(ES.toString());
			ois.close();
			bais.close();
		} catch (IOException e) {
			// Error in de-serialization
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// You are converting an invalid stream to Student
			e.printStackTrace();
		}
		return ES;
	}

	/**
	 * Method:							run
	 * Description:						State-based execution loop for the program
	 */
	protected void run() {
		while (true) {
			switch (state) {
			case STANDBY:
				this.standby(this);
				break;
			case WAITING_TO_START:
				this.waitingToStart(this);
				break;
			case MOVING:
				try {
					this.moving(this);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				break;
			case OPEN:
				try {
					this.open(this);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				break;
			}
		}
	}

	/**
	 * Method:							main
	 * Description:						Entry point of the program
	 * @param args						Execution command arguments
	 */
	public static void main(String[] args) {
		ElevatorSubsystem e;
		e = new ElevatorSubsystem();
		e.run();
	}
}
