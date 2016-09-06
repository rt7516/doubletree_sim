/*
 * Request.java
 * 
 * Version : Java 1.8
 *
 */

import java.util.List;

/**
 * Class Request provides different variables to communicate the type of request
 * to servers
 * 
 * @author 
 * 
 */

public class Request {
	
	// "read" or "write"
	public String type;
	public String fileName;
	
	public String clientIp;
	public Node sender;
	public Node receiver;
	
	// "up" or "left" or "right"
	public String mode;
	
	public List<ServerStatic> path;
	public int length;
	

	
	public Request(String type, String fileName, String clientIp, Node sender, Node receiver, String mode) {
		this.type = type;
		this.fileName = fileName;
		this.clientIp = clientIp;
		this.mode = mode;
		length = 0;
	}
	
	public Request(String type, String fileName, List<ServerStatic> path, Node receiver, String mode) {
		this.type = type;
		this.fileName = fileName;
		this.path = path;
		this.receiver = receiver;
		this.mode = mode;
		length = 0;
	}
}