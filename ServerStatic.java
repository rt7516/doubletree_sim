/*
 * ServerStatic.java
 * 
 * Version : Java 1.8
 */

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * class ServerStatic is used to simulate the server which handles the requests
 * from the clients
 * 
 * @author
 * 
 */
public class ServerStatic {

	public static int totalRequestsServed = 0;
	private int id;
	private MasterStatic master;
	private Util util;
	ArrayList<String> files;
	ArrayList<Request> sQ;
	HashMap<String, Integer> popular;
	HashMap<String, Integer> filePopular;

	HashMap<String, Integer> countUp;
	HashMap<String, Integer> countLeft;

	public int cLevelLookup;
	public int cPosLookup;
	public int numOfReplicas;
	public int parentPos;
	public int parentLevel;

	public static final int BUSY_THRESHOLD = 6;

	public static final int upThreshold1 = 1;
	public static final int upThreshold2 = 2;

	public static final int POPULAR_THRESHOLD = 4;

	public static final int leftThreshold = 1;

	private int tickCount = 0;

	public static int pathLength = 0;
	public static int maxPathLength = -1;
	public static int total = 0;

	int D = 30, d = 2;

	// constructor
	public ServerStatic(int serial) throws UnknownHostException {
		id = serial;
		sQ = new ArrayList<>();
		files = new ArrayList<>();
		popular = new HashMap<>();
		master = new MasterStatic();
		util = new Util();
		cLevelLookup = -1;
		cPosLookup = -1;
		parentPos = -1;
		parentLevel = -1;
		countUp = new HashMap<>();
		countLeft = new HashMap<>();
		numOfReplicas = 0;
		filePopular = new HashMap<>();
	}

	/**
	 * getServerHash computes the hash value and the target server ID
	 * 
	 * @param fileName
	 *            name of the file
	 * @param level
	 *            level of the node in the lookup tree
	 * @param pos
	 *            position of the node in the replication tree
	 * @return server ID
	 */
	public ServerStatic getServerHash(String fileName, int level, int pos) {
		ServerStatic server = master.hashedServer(util.calcServerHash(fileName,
				level, pos));

		server.cLevelLookup = level;
		server.cPosLookup = pos;

		return server;
	}

	/**
	 * getServerID is used to return the id of this server
	 * 
	 * @return int server ID
	 */
	public int getServerId() {
		return id;
	}

	/**
	 * receiveRequest is the function used to process the request received in
	 * the server
	 * 
	 * @param rq
	 *            request object
	 * 
	 * @return None
	 */
	public void receiveRequest(Request rq) {

		// Read request
		if (rq.type.equals("read")) {

			if (filePopular.containsKey(rq.fileName)) {
				int num = filePopular.get(rq.fileName);
				filePopular.put(rq.fileName, num + 1);
			} else {
				filePopular.put(rq.fileName, 1);
			}

			// if mode is "up"
			if (rq.mode.equals("up")) {

				if (countUp.containsKey(rq.fileName)) {
					int num = countUp.get(rq.fileName);
					countUp.put(rq.fileName, num + 1);
				} else {
					countUp.put(rq.fileName, 1);
				}

				// file not available in the server
				if (!files.contains(rq.fileName)) {

					if (countUp.get(rq.fileName) <= upThreshold1) {

						int temp = cPosLookup % D;

						// computing the position of parent node
						int parentPos = (temp == 0) ? cPosLookup / D
								: (cPosLookup / D) + 1;

						// computing the server id of parent
						ServerStatic parent = getServerHash(rq.fileName,
								cLevelLookup - 1, parentPos);

						rq.sender = new Node(cLevelLookup, cPosLookup);
						rq.receiver = new Node(cLevelLookup - 1, parentPos);

						rq.length += 1;
						// forwarding in the up direction
						parent.receiveRequest(rq);
					} else if (countUp.get(rq.fileName) <= upThreshold2) {

						// if the popularity of UP is exceeded

						// choose the left parent and forward the request
						Node leftParent = util.selectLeftParent(this);
						ServerStatic parent = getServerHash(rq.fileName,
								leftParent.level, leftParent.pos);

						rq.sender = new Node(cLevelLookup, cPosLookup);
						rq.receiver = new Node(leftParent.level, leftParent.pos);

						// mode is changed to "left"
						rq.mode = "left";
						rq.length += 1;
						parent.receiveRequest(rq);
					} else {

						// construct write request and forward to the left
						// parent
						ArrayList<ServerStatic> list = new ArrayList<>();
						list.add(this);

						// constructing request
						Request req = new Request("write", rq.fileName, list,
								null, "left");

						Node leftParent = util.selectLeftParent(this);
						ServerStatic parent = getServerHash(rq.fileName,
								leftParent.level, leftParent.pos);

						parent.receiveRequest(req);
						
						// service C
						sQ.add(rq);
						popular.put(rq.fileName, popular.get(rq.fileName) + 1);

					}
				} else {

					// if countUp is less than popular threshold or it is a leaf
					// node
					// service the request

					// choose the left parent of the sender node and forward
					// the request

					if (countUp.get(rq.fileName) <= POPULAR_THRESHOLD
							|| util.isLeaf(this, rq.fileName, "lookup")) {

						sQ.add(rq);
						popular.put(rq.fileName, popular.get(rq.fileName) + 1);

					} else {

						ServerStatic sender = getServerHash(rq.fileName,
								rq.sender.level, rq.sender.pos);

						Node leftParent = util.selectLeftParent(sender);
						ServerStatic parent = getServerHash(rq.fileName,
								leftParent.level, leftParent.pos);

						if (parent.getServerId() == this.getServerId()) {

							sQ.add(rq);
							popular.put(rq.fileName,
									popular.get(rq.fileName) + 1);

						} else {

							rq.receiver = new Node(leftParent.level,
									leftParent.pos);

							// mode is changed from "up" to "left"
							rq.mode = "left";

							parent.receiveRequest(rq);

							int num = countUp.get(rq.fileName);
							num = num - 1;
							countUp.put(rq.fileName, num);
						}
					}
				}
			} else if (rq.mode.equals("left")) {

				// if read request has mode as "left"

				if (countLeft.containsKey(rq.fileName)) {
					int num = countLeft.get(rq.fileName);
					countLeft.put(rq.fileName, num + 1);
				} else {
					countLeft.put(rq.fileName, 1);
				}

				// if it doesn't have the file
				if (!files.contains(rq.fileName)) {

					// if popularity of countLeft is not exceeded
					if (countLeft.get(rq.fileName) <= leftThreshold) {

						// forwarding request to left parent
						Node leftParent = util.selectLeftParent(this);
						ServerStatic parent = getServerHash(rq.fileName,
								leftParent.level, leftParent.pos);

						rq.sender = new Node(cLevelLookup, cPosLookup);
						rq.receiver = new Node(leftParent.level, leftParent.pos);

						rq.length += 1;
						parent.receiveRequest(rq);

					} else {

						// initiating write request
						ArrayList<ServerStatic> list = new ArrayList<>();
						list.add(this);

						Request req = new Request("write", rq.fileName, list,
								null, "left");

						// sending it to the left parent
						Node leftParent = util.selectLeftParent(this);
						ServerStatic parent = getServerHash(rq.fileName,
								leftParent.level, leftParent.pos);

						parent.receiveRequest(req);
						
						// service request
						sQ.add(rq);

						popular.put(rq.fileName, popular.get(rq.fileName) + 1);
					}
				} else {

					// service request
					sQ.add(rq);

					popular.put(rq.fileName, popular.get(rq.fileName) + 1);
				}

			} else {

				// Incorrect request mode
				System.out.println("Incorrect request type");
			}
		} else if (rq.type.equals("write")) {

			// write request with mode as "left"
			if (rq.mode.equals("left")) {

				// if server has a file
				if (files.contains(rq.fileName)) {

					// replicate the file to the last node in the list
					// remove the last node and forward the write request to the
					// popped node
					int length = rq.path.size();
					length -= 1;
					ServerStatic obj = rq.path.remove(length);
					obj.files.add(rq.fileName);
					obj.popular.put(rq.fileName, 0);
					obj.numOfReplicas++;

					// if list is non empty forward the write request with type
					// as "right"
					if (rq.path.size() != 0) {
						rq.mode = "right";
						obj.receiveRequest(rq);
					}
				} else {

					// if file is not available, add itself to the list
					// forward the request to the left parent
					rq.path.add(this);

					Node leftParent = util.selectLeftParent(this);
					ServerStatic parent = getServerHash(rq.fileName,
							leftParent.level, leftParent.pos);

					parent.receiveRequest(rq);
				}
			} else if (rq.mode.equals("right")) {

				// write request received with mode "right"
				int length = rq.path.size();

				// replicate the file to the last node in the list
				// remove the last node and forward the write request to the
				// popped node
				length -= 1;
				ServerStatic obj = rq.path.remove(length);
				obj.files.add(rq.fileName);
				obj.popular.put(rq.fileName, 0);
				obj.numOfReplicas++;

				// if list is non empty forward the write request with type as
				// "right"
				if (rq.path.size() != 0) {
					obj.receiveRequest(rq);
				}
			} else {
				System.out.println("Invalid MODE");
			}
		} else {
			System.out.println("Invalid TYPE");
		}
	}

	/**
	 * receiveRequestNoRep function is used to handle the requests received in
	 * the servers for the max storage case
	 * 
	 * @param rq
	 *            Request to be processed
	 * @return None
	 */
	public void receiveRequestNoRep(Request rq) {

		if (files.contains(rq.fileName)) {
			sQ.add(rq);
		}
	}

	/**
	 * receiveRequestCase6 function is used to handle the requests received in
	 * the servers for the case 6 one copy and no replications
	 * 
	 * @param rq
	 *            request to be processed
	 * @return None
	 */

	public void receiveRequestCase6(Request rq) {

		// max storage case
		sQ.add(rq);
	}

	/**
	 * serveRequest is used to process request in the queue
	 * 
	 * @param value
	 *            number of requests to be processed
	 * 
	 * @return None
	 */
	public void serveRequest(int value) {

		tickCount++;
		while (value > 0) {
			if (sQ.size() != 0) {
				Request rq = sQ.remove(0);
				total += 1;
				pathLength += rq.length;

				if (maxPathLength < rq.length) {
					maxPathLength = rq.length;
				}

				totalRequestsServed += 1;
			}
			value--;
		}

		// logic to decrement the counter based on the popularity of file
		// resets the filepopular counter for the particular file to zero
		// if it remains zero in the next cycle, counters are decremented 
		// accordingly. once the counter becomes zero, file is removed from
		// the node only if it is not a root node
		if (tickCount == 100) {
			boolean flag1 = false;
			boolean flag2 = false;
			for (String key : filePopular.keySet()) {
				int num = filePopular.get(key);
				if (num == 0) {
					if (countUp.containsKey(key)) {
						num = countUp.get(key);
						if (num != 0) {
							num -= 1;
							countUp.put(key, num);
						} else {
							flag1 = true;
						}
					} else {
						flag1 = true;
					}

					if (countLeft.containsKey(key)) {
						num = countLeft.get(key);
						if (num != 0) {
							num -= 1;
							countLeft.put(key, num);
						} else {
							flag2 = true;
						}
					} else {
						flag2 = true;
					}

				}

				if (flag1 == true && flag2 == true) {
					if (files.contains(key) == true) {
						ServerStatic parent = getServerHash(key, 0, 1);
						if (this.getServerId() != parent.getServerId()) {
							files.remove(key);
						}
					}
				}

				filePopular.put(key, 0);
			}
			tickCount = 0;
		}
	}
}
