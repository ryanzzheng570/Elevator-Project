package ElevatorTest;
import Elevator.*;
import Enum.*;
import Common.*;
import java.io.*;
import java.net.*;
import org.junit.runner.*;
import org.junit.runner.notification.Failure;
import org.junit.Test;
import org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Assert;
/**
 * Class:								ElevatorSubsystemTest
 * Description:							JUnit test controller for ElevatorSubsystem
 * @author								Zhexiao Huang
 * @version 							0.0.1
 * @since 								2019/01/31
 */
public class ElevatorSubsystemTest {
	private ElevatorSubsystem elevator;
	private ElevatorSubsystemTestable elevatorTestable;
	private DatagramSocket s_in;
	private DatagramSocket s_out;
	
	@BeforeClass
	public void beforeClass() {
		elevatorTestable = new ElevatorSubsystemTestable();
	}
	
	@Test
	public void connectionTest() {
		//TODO: OH GOD HOW DO WE EVEN TEST THIS ASYNC SYSTEM?
		try {
			s_in = new DatagramSocket(Constant.ELEVATOR_SYSTEM_PORT);
			s_out = new DatagramSocket();
			int floors = 21;
			String floorString = Integer.toString(floors);
			byte[] data = floorString.getBytes();
			DatagramPacket outPacket = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), Constant.ELEVATOR_MIN_PORT);
			s_out.send(outPacket);
			byte[] buffer = new byte[100];
			DatagramPacket inPacket = new DatagramPacket(data, data.length);
			s_in.setSoTimeout(2000);
			s_in.receive(inPacket);
			s_in.setSoTimeout(0);
			String response = new String(buffer,0,buffer.length).trim();
			Assert.assertTrue(response.equals("OK"));
			Assert.assertEquals(elevatorTestable.getCurrentFloor(), 1);
			Assert.assertEquals(elevatorTestable.getTopFloor(), floors);
		} catch(Exception e) {
			Assert.assertTrue(false);
		}
	}
	
	public static void main(String[] args) {
		Result result = JUnitCore.runClasses(ElevatorSubsystemTest.class);
		for(Failure f : result.getFailures()) {
			System.out.println("JUnit Test Failed: " + f.toString());
		}
		System.out.println("JUnit Sucess Result: " + result.wasSuccessful());
	}
}
