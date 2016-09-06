/*
 * MasterStatic.java
 * 
 * Version : Java 1.8
 * 
 */

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

// 0.1n = 2793
// 0.2n = 5586
// 0.25n = 6983
// 0.5n = 13965
// 0.9n = 25137
// 0.8n = 22345
// 0.4n = 11172

/**
 * MasterStatic class is defined for simulating the requests to the files stored
 * in the servers
 * 
 * @author
 * 
 */

public class MasterStatic {

	private static ArrayList<ServerStatic> servers = new ArrayList<>();
	private Util util;
	private String[] exts = { "ishita malhotra", "nishe kataria",
			"ashwani kumar", "neetu kapoor", "akshay" };
	private long startTime = 0, endTime = 0;
	static int numOfServers = 0;
	public HashMap<Integer, Integer> queueCount;
	public static int localCount = 0;

	// new version D and d
	int D = 30, d = 2;

	// constructor
	public MasterStatic() {
		util = new Util();
		queueCount = new HashMap<Integer, Integer>();
	}

	/**
	 * getNumberOfServer is a function used to return the number of servers in
	 * the simulation
	 * 
	 * @return int number of servers
	 * 
	 */
	public static int getNumberOfServer() {
		return servers.size();
	}

	/**
	 * hashedServer function is used to get the serverID from the hash value
	 * 
	 * @param hash
	 *            hash value
	 * 
	 * @return int server ID
	 */
	public ServerStatic hashedServer(int[] hash) {
		return servers.get(((Math.abs(hash[1]) % servers.size()) + hash[0])
				% servers.size());
	}

	// main function of the program
	public static void main(String args[]) throws UnknownHostException {

		// getting user input for the number of servers
		Scanner sc = new Scanner(System.in);
		System.out.println("How many servers do you need ?");

		numOfServers = sc.nextInt();

		// updating the server list
		for (int i = 1; i <= numOfServers; i++) {
			ServerStatic temp = new ServerStatic(i);
			servers.add(temp);
		}

		MasterStatic masterObj = new MasterStatic();

		// displaying options for the simulation
		masterObj.displayOptions(sc);

	}

	/**
	 * displayOptions function displays the set of options for simulation, gets
	 * the user input and displays the results
	 * 
	 * @param sc
	 *            scanner
	 * 
	 * @return None
	 */
	public void displayOptions(Scanner sc) {

		// displaying options
		System.out
				.println("\n*************************************************************");
		System.out.println("What do you want to do ?");
		System.out.println("1. Store a File");
		System.out.println("2. Search for a File");
		System.out.println("3. Dispaly Queue Lengths");
		System.out.println("4. Dispaly Stats");
		System.out.println("5. Random chosen servers :");
		System.out
				.println("*************************************************************\n");

		// getting user input for the option
		int option = sc.nextInt();

		switch (option) {
		case 1:

			// storing files

			// getting user input for the file name
			System.out.println("Enter the name of the file you want to store");
			String fileName = sc.next();

			// storing file
			storeFile(fileName);
			break;

		case 2:
			// search for a file, sending request
			sendRequests(sc);
			break;
		case 3:

			// displaying the statistics of queues
			displayQueueLengths(sc);
			break;
		case 4:

			// displaying the statistics of the node
			stats(sc);
			break;
		case 5:
			System.out.println("Randomly requested server count : ");
			for (Integer key : queueCount.keySet()) {
				System.out.println(key + " -------> " + queueCount.get(key));
			}
		default:
		}

	}

	/**
	 * displayQueueLengths displays the length of queue in each server
	 * 
	 * @param sc
	 *            Scanner
	 * 
	 * @return None
	 */
	public void displayQueueLengths(Scanner sc) {
		System.out
				.println("\n*************************************************************");

		// Iterating through the servers
		for (ServerStatic s : servers) {
			System.out.println(s.getServerId() + " ------> " + s.sQ.size());
		}
		System.out
				.println("*************************************************************\n");
		displayOptions(sc);
	}

	/**
	 * storeFile function is used to store the file in one of the randomly
	 * chosen servers
	 * 
	 * @param fileNum
	 *            number of files to be stored
	 * @return array of file stored
	 */
	public String[] storeFile(int fileNum) {
		String[] allFiles = new String[fileNum];

		// iterating through the files
		for (int i = 1; i <= fileNum; i++) {
			String fileName = i + "." + (exts[(i % 5)]);
			allFiles[i - 1] = fileName;

			// storing file
			storeFile(fileName);
		}
		return allFiles;
	}

	/**
	 * sendRequest function displays the set of options to send the request and
	 * sends the request
	 * 
	 * @param sc
	 *            Scanner
	 * 
	 * @return None
	 * 
	 */
	public void sendRequests(Scanner sc) {

		// displaying set of options for the simulation
		System.out.println("Select a Mode");
		System.out.println("1 --> 1 Request Per File");
		System.out.println("2 --> All Requests for 1 File");
		System.out.println("3 --> Multiple equally popular files");
		System.out.println("4 --> Random");
		System.out.println("5 --> Max Storage");
		System.out.println("6 --> One copy & No replications");

		// getting user input for the input
		int mode = sc.nextInt();

		// getting user input for the tick value
		System.out.println("Enter tick");
		int tick = sc.nextInt();

		// getting user input for the request rate
		System.out.println("Request rate ?");
		int rate = sc.nextInt();
		int limit = rate * tick;
		int tickCounter = 0;

		// getting user input for the request fraction
		System.out.println("Enter the lambda value : ");
		double lambda = sc.nextDouble();
		System.out.println("Number of requests : " + limit);

		switch (mode) {

		case 1:

			// one request per file
			System.out.println("Results for 1 Request per File : ");

			// storing the files in the servers
			String[] allFiles = storeFile(limit);

			// starting timer to calculate the average lookup
			startTime = System.currentTimeMillis();

			// send requests in each tick
			for (int counter = 0; counter < limit; counter += rate) {
				tickCounter += 1;

				// sending request
				doTask(rate, allFiles, mode, counter, limit, lambda);

				// Displaying the statistics at different ticks
				if (tickCounter == 1 || tickCounter == 10 || tickCounter == 100
						|| tickCounter == 200 || tickCounter == 400
						|| tickCounter == 600 || tickCounter == 800
						|| tickCounter == 1000) {
					System.out.println("*************************************");
					System.out.println("Resuls for tick : " + tickCounter);
					endTime = System.currentTimeMillis();
					stats(sc);
					System.out
							.println("**************************************");
				}
			}
			break;
		case 2:

			// All requests for one file
			System.out.println("All request for 1 file : ");
			localCount = 0;

			// storing one file
			storeFile("ishita.pdf");

			// starting timer to calculate average lookup time
			startTime = System.currentTimeMillis();

			// sending requests in each tick
			for (int counter = 0; counter < limit; counter += rate) {

				// sending request
				doTask(rate, null, mode, counter, limit, lambda);
				tickCounter += 1;

				// collecting statistics at different ticks
				if (tickCounter == 1 || tickCounter == 10 || tickCounter == 100
						|| tickCounter == 200 || tickCounter == 400
						|| tickCounter == 600 || tickCounter == 800
						|| tickCounter == 1000) {
					System.out.println("*************************************");
					System.out.println("Resuls for tick : " + tickCounter);
					endTime = System.currentTimeMillis();
					stats(sc);
					System.out
							.println("**************************************");
				}
			}
			break;
		case 3:

			// sending max threshold request to each file ( one of the worst
			// cases )
			System.out.println("Equally popular files : ");
			localCount = 0;
			int popularity = 3;

			// creating and storing the required files in the server
			ArrayList<String> files = new ArrayList<>();
			int numOfFiles = limit / popularity;
			if (limit % popularity != 0) {
				numOfFiles += 1;
			}
			String[] tempfiles = storeFile(numOfFiles);

			for (String str : tempfiles) {
				for (int i = 0; i < popularity; i++) {
					files.add(str);
				}
			}

			String[] allFiles1 = new String[files.size()];
			for (int i = 0; i < files.size(); i++) {
				allFiles1[i] = files.get(i);
			}
			startTime = System.currentTimeMillis();

			// iterating for each tick
			for (int counter = 0; counter < limit; counter += rate) {

				// sending requests in each tick
				doTask(rate, allFiles1, mode, counter, limit, lambda);
				tickCounter += 1;

				// collecting statistics at different ticks
				if (tickCounter == 1 || tickCounter == 10 || tickCounter == 100
						|| tickCounter == 200 || tickCounter == 400
						|| tickCounter == 600 || tickCounter == 800
						|| tickCounter == 1000) {
					System.out.println("*************************************");
					System.out.println("Resuls for tick : " + tickCounter);
					endTime = System.currentTimeMillis();
					stats(sc);
					System.out
							.println("**************************************");
				}

			}
			break;

		case 4:
			// Randomly sending requests to the files
			System.out.println("Randomly requested files :");

			// storing <2 * num of server> files
			int num = MasterStatic.getNumberOfServer() * 2;
			String[] allFiles2 = storeFile(num);
			startTime = System.currentTimeMillis();

			// iterating through each tick
			for (int counter = 0; counter < limit; counter += rate) {

				// sending request in each tick
				doTask(rate, allFiles2, mode, counter, limit, lambda);
				tickCounter += 1;

				// collecting statistics at different ticks
				if (tickCounter == 1 || tickCounter == 10 || tickCounter == 100
						|| tickCounter == 200 || tickCounter == 400
						|| tickCounter == 600 || tickCounter == 800
						|| tickCounter == 1000) {
					System.out.println("*************************************");
					System.out.println("Resuls for tick : " + tickCounter);
					endTime = System.currentTimeMillis();
					stats(sc);
					System.out
							.println("**************************************");
				}
			}

			break;
		case 5:

			// max storage case, all files are available in all the servers
			System.out.println("MAX storage case : ");

			// storing < 2 * num of servers > files in the servers
			int n = MasterStatic.getNumberOfServer();
			n = n * 2;
			String[] allFiles3 = storeFile(n);

			// starting timer to calculate average lookup time
			startTime = System.currentTimeMillis();

			// iterating through each tick
			for (int counter = 0; counter < limit; counter += rate) {

				// send request in each tick
				doTask(rate, allFiles3, mode, counter, limit, lambda);
				tickCounter += 1;

				// collecting statistics at different ticks
				if (tickCounter == 1 || tickCounter == 10 || tickCounter == 100
						|| tickCounter == 200 || tickCounter == 400
						|| tickCounter == 600 || tickCounter == 800
						|| tickCounter == 1000) {
					System.out.println("*************************************");
					System.out.println("Resuls for tick : " + tickCounter);
					endTime = System.currentTimeMillis();
					stats(sc);
					System.out
							.println("**************************************");
				}

			}

			break;
		case 6:

			// case where the replications doesn't happen ( worst case )
			System.out.println("One copy and No replications : ");

			// storing < 2 * num of servers > files in the servers
			int n1 = MasterStatic.getNumberOfServer();
			n1 = n1 * 2;
			String[] allFiles4 = storeFile(n1);
			startTime = System.currentTimeMillis();

			// iterating at each tick
			for (int counter = 0; counter < limit; counter += rate) {

				// sending request in each tick
				doTask(rate, allFiles4, mode, counter, limit, lambda);
				tickCounter += 1;

				// collecting statistics at different ticks
				if (tickCounter == 1 || tickCounter == 10 || tickCounter == 100
						|| tickCounter == 200 || tickCounter == 400
						|| tickCounter == 600 || tickCounter == 800
						|| tickCounter == 1000) {
					System.out.println("*************************************");
					System.out.println("Resuls for tick : " + tickCounter);
					endTime = System.currentTimeMillis();
					stats(sc);
					System.out
							.println("**************************************");
				}
			}
			break;

		default:
			System.out.println("Wrong Option");
			displayOptions(sc);
		}
	}

	/**
	 * doTask function is used to send the request in poisson distribution.
	 * 
	 * @param numOfReq
	 *            number of requests to be sent
	 * @param allFiles
	 *            the files in the server
	 * @param mode
	 *            mode of request
	 * @param counter
	 *            number of counts
	 * @param limit
	 *            max requests
	 * @param lambda
	 *            request rate fraction for poisson distribution
	 */
	private void doTask(int numOfReq, String[] allFiles, int mode, int counter,
			int limit, double lambda) {

		// calculating the number of nodes in the last level
		int nodesAtLastLevel = util.getLeafCount(servers.size(), "lookup");

		// calculating the last level
		int lastLevel = util.getLevels(servers.size(), "lookup");
		switch (mode) {
		case 1:

			// serving request in each tick ( poisson distribution )
			for (ServerStatic server : servers) {
				double r = Math.random();
				double factor1 = Math.exp(-1);
				double factor2 = 1;
				double p0 = factor1 * factor2;
				double p1;
				if (r >= p0) {
					for (int k = 1; k < 12; k++) {
						factor2 = factor2 / k;
						p1 = p0 + factor1 * factor2;
						if (r < p1) {
							server.serveRequest(k);
							break;
						}
						p0 = p1;
					}
				}
			}

			// sending request in each tick ( poisson distribution )
			for (int i = 1; i <= nodesAtLastLevel; i++) {
				int pos = i;
				double r = Math.random();
				double factor1 = Math.exp(-1 * lambda);
				double factor2 = 1;
				double p0 = factor1 * factor2;
				double p1;
				if (r >= p0) {
					for (int k = 1; k < 12; k++) {
						factor2 = factor2 * lambda / k;
						p1 = p0 + factor1 * factor2;
						if (r < p1) {

							for (int j = 0; j < k; j++) {

								// constructing request
								Request request = new Request("read",
										allFiles[localCount], null, null, null,
										"up");
								localCount++;

								// calculating the target server ID
								ServerStatic target = hashedServer(util
										.calcServerHash(request.fileName,
												lastLevel, pos));
								target.cLevelLookup = lastLevel;
								target.cPosLookup = pos;

								// received request by target server
								target.receiveRequest(request);
							}

							break;
						}
						p0 = p1;
					}
				}
			}
			break;

		case 2:

			// serving request in each tick ( poisson distribution )
			for (ServerStatic server : servers) {
				double r = Math.random();
				double factor1 = Math.exp(-1);
				double factor2 = 1;
				double p0 = factor1 * factor2;
				double p1;
				if (r >= p0) {
					for (int k = 1; k < 12; k++) {
						factor2 = factor2 / k;
						p1 = p0 + factor1 * factor2;
						if (r < p1) {
							server.serveRequest(k);
							break;
						}
						p0 = p1;
					}
				}
			}

			// sending request in each tick ( poisson distribution )
			for (int i = 1; i <= nodesAtLastLevel; i++) {
				int pos = i;
				double r = Math.random();
				double factor1 = Math.exp(-1 * lambda);
				double factor2 = 1;
				double p0 = factor1 * factor2;
				double p1;
				if (r >= p0) {
					for (int k = 1; k < 12; k++) {
						factor2 = factor2 * lambda / k;
						p1 = p0 + factor1 * factor2;
						if (r < p1) {

							for (int j = 0; j < k; j++) {

								// constructing request
								Request request = new Request("read",
										"ishita.pdf", null, null, null, "up");

								// calculating the target server ID
								ServerStatic target = hashedServer(util
										.calcServerHash(request.fileName,
												lastLevel, pos));

								target.cLevelLookup = lastLevel;
								target.cPosLookup = pos;
								target.receiveRequest(request);
							}

							break;
						}
						p0 = p1;
					}
				}
			}
			break;

		case 3:

			// serving request in each tick ( poisson distribution )
			for (ServerStatic server : servers) {
				double r = Math.random();
				double factor1 = Math.exp(-1);
				double factor2 = 1;
				double p0 = factor1 * factor2;
				double p1;
				if (r >= p0) {
					for (int k = 1; k < 12; k++) {
						factor2 = factor2 / k;
						p1 = p0 + factor1 * factor2;
						if (r < p1) {
							server.serveRequest(k);
							break;
						}
						p0 = p1;
					}
				}
			}

			// sending request in each tick ( poisson distribution )
			for (int i = 1; i <= nodesAtLastLevel; i++) {
				int pos = i;
				double r = Math.random();
				double factor1 = Math.exp(-1 * lambda);
				double factor2 = 1;
				double p0 = factor1 * factor2;
				double p1;
				if (r >= p0) {
					for (int k = 1; k < 12; k++) {
						factor2 = factor2 * lambda / k;
						p1 = p0 + factor1 * factor2;
						if (r < p1) {

							for (int j = 0; j < k; j++) {

								// constructing request
								Request request = new Request("read",
										allFiles[localCount], null, null, null,
										"up");
								localCount++;

								// calculating the target server ID
								ServerStatic target = hashedServer(util
										.calcServerHash(request.fileName,
												lastLevel, pos));
								target.cLevelLookup = lastLevel;
								target.cPosLookup = pos;
								target.receiveRequest(request);
							}

							break;
						}
						p0 = p1;
					}
				}
			}
			break;

		case 4:

			// serving request in each tick ( poisson distribution )
			for (ServerStatic server : servers) {
				double r = Math.random();
				double factor1 = Math.exp(-1);
				double factor2 = 1;
				double p0 = factor1 * factor2;
				double p1;
				if (r >= p0) {
					for (int k = 1; k < 12; k++) {
						factor2 = factor2 / k;
						p1 = p0 + factor1 * factor2;
						if (r < p1) {
							server.serveRequest(k);
							break;
						}
						p0 = p1;
					}
				}
			}

			/*
			 * for(ServerStatic server: servers) { server.serveRequest(1); }
			 */

			// sending request in each tick ( poisson distribution )
			for (int i = 1; i <= nodesAtLastLevel; i++) {
				int pos = i;
				double r = Math.random();
				double factor1 = Math.exp(-1 * lambda);
				double factor2 = 1;
				double p0 = factor1 * factor2;
				double p1;
				if (r >= p0) {
					for (int k = 1; k < 12; k++) {
						factor2 = factor2 * lambda / k;
						p1 = p0 + factor1 * factor2;
						if (r < p1) {

							for (int j = 0; j < k; j++) {
								Random rand = new Random();
								int val = rand.nextInt(allFiles.length);

								// constructing request
								Request request = new Request("read",
										allFiles[val], null, null, null, "up");

								// calculating the target server ID
								ServerStatic target = hashedServer(util
										.calcServerHash(request.fileName,
												lastLevel, pos));
								target.cLevelLookup = lastLevel;
								target.cPosLookup = pos;
								target.receiveRequest(request);
							}

							break;
						}
						p0 = p1;
					}
				}
			}
			break;
		case 5:

			// serving request in each tick ( poisson distribution )
			for (ServerStatic server : servers) {
				double r = Math.random();
				double factor1 = Math.exp(-1);
				double factor2 = 1;
				double p0 = factor1 * factor2;
				double p1;
				if (r >= p0) {
					for (int k = 1; k < 12; k++) {
						factor2 = factor2 / k;
						p1 = p0 + factor1 * factor2;
						if (r < p1) {
							server.serveRequest(k);
							break;
						}
						p0 = p1;
					}
				}
			}

			// sending request in each tick ( poisson distribution )
			for (int i = 1; i <= nodesAtLastLevel; i++) {
				int pos = i;
				double r = Math.random();
				double factor1 = Math.exp(-1 * lambda);
				double factor2 = 1;
				double p0 = factor1 * factor2;
				double p1;
				if (r >= p0) {
					for (int k = 1; k < 12; k++) {
						factor2 = factor2 * lambda / k;
						p1 = p0 + factor1 * factor2;
						if (r < p1) {

							for (int j = 0; j < k; j++) {
								Random rand = new Random();
								int val = rand.nextInt(allFiles.length);

								// constructing request
								Request request = new Request("read",
										allFiles[val], null, null, null, "up");

								// calculating the target server ID
								ServerStatic target = hashedServer(util
										.calcServerHash(request.fileName,
												lastLevel, pos));
								target.cLevelLookup = lastLevel;
								target.cPosLookup = pos;
								target.receiveRequestCase6(request);
							}

							break;
						}
						p0 = p1;
					}
				}
			}
			break;
		case 6:

			// serving request in each tick ( poisson distribution )
			for (ServerStatic server : servers) {
				double r = Math.random();
				double factor1 = Math.exp(-1);
				double factor2 = 1;
				double p0 = factor1 * factor2;
				double p1;
				if (r >= p0) {
					for (int k = 1; k < 12; k++) {
						factor2 = factor2 / k;
						p1 = p0 + factor1 * factor2;
						if (r < p1) {
							server.serveRequest(k);
							break;
						}
						p0 = p1;
					}
				}
			}

			// sending request in each tick ( poisson distribution )
			for (int i = 1; i <= nodesAtLastLevel; i++) {
				int pos = i;
				double r = Math.random();
				double factor1 = Math.exp(-1 * lambda);
				double factor2 = 1;
				double p0 = factor1 * factor2;
				double p1;
				if (r >= p0) {
					for (int k = 1; k < 12; k++) {
						factor2 = factor2 * lambda / k;
						p1 = p0 + factor1 * factor2;
						if (r < p1) {

							for (int j = 0; j < k; j++) {
								Random rand = new Random();
								int val = rand.nextInt(allFiles.length);

								// constructing request
								Request request = new Request("read",
										allFiles[val], null, null, null, "up");

								// calculating the target server ID
								ServerStatic target = hashedServer(util
										.calcServerHash(request.fileName, 0, 1));
								target.cLevelLookup = lastLevel;
								target.cPosLookup = pos;
								target.receiveRequestNoRep(request);
							}

							break;
						}
						p0 = p1;
					}
				}
			}
			break;
		default:
			System.out.println("Wrong Option.. Try Again");

		}

	}

	/**
	 * storeFile function is used to calculate the target server ID and store
	 * the files
	 * 
	 * @param fileName
	 *            Name of the file to be stored
	 * 
	 * @return boolean success or failure
	 * 
	 */
	public boolean storeFile(String fileName) {

		// calculating the target server ID

		ServerStatic root = hashedServer(util.calcServerHash(fileName, 0, 1));

		// System.out.println("Root server ID : " + root.getServerId());
		// adding file in the target server
		root.popular.put(fileName, 0);

		return root.files.add(fileName);
	}

	/**
	 * getServer function returns the server ID based on the IP
	 * 
	 * @param ip
	 *            ip address of the server
	 * 
	 * @return int server ID
	 */
	public ServerStatic getserver(int ip) {
		return servers.get(ip);
	}

	/**
	 * getServerPos function returns the position of the server
	 * 
	 * @param server
	 * @return int position
	 * 
	 */
	public int getserverPos(ServerStatic server) {
		return servers.indexOf(server);
	}

	/**
	 * stats function is used to collect the statistics from all the servers and
	 * to display the result.
	 * 
	 * @param sc
	 *            Scanner
	 * 
	 * @return None
	 */
	public void stats(Scanner sc) {
		int sum = 0;
		int count = 0;
		int max = 0;
		int zeroes = 0;
		// calculating the max Queue length
		for (ServerStatic server : servers) {
			sum += server.sQ.size();
			if (server.sQ.size() > 0) {
				count += 1;
				if (server.sQ.size() > max) {
					max = server.sQ.size();
				}
			} else {
				zeroes += 1;
			}
		}

		// calculating the average queue length
		int mean;
		if (count == 0)
			mean = 0;
		else
			mean = sum / count;
		System.out.println("Average Q Length : " + mean);

		// calculating standard deviation for the queue length
		int sd = 0;
		for (ServerStatic server : servers) {
			sd += ((server.sQ.size() - mean) * (server.sQ.size() - mean));
		}
		sd /= servers.size();
		sd = (int) Math.sqrt((double) sd);
		System.out.println("Standard Deviation : " + sd);
		System.out.println("Max Q Len : " + max);

		// displaying the average lookup time for the request
		System.out
				.println("Average LookUp Time : "
						+ (((double) endTime - (double) startTime) / (double) ServerStatic.totalRequestsServed));

		System.out.println("Zeroes : " + zeroes);
		System.out.println("Sum of all SQ sizes : " + sum);
		
		int avgPathLength = 0;
		if(ServerStatic.total != 0) {
        avgPathLength = ServerStatic.pathLength / ServerStatic.total;
		}
		System.out.println("total : " + ServerStatic.total);
        System.out.println("Average path length : " + avgPathLength);
        System.out.println("Max Path Length : " + ServerStatic.maxPathLength);
	}
}