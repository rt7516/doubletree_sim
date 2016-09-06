/*
 * Util.java
 * 
 * Version : Java 1.8
 * 
 */

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Random;

/**
 * class Util provides the util functions used by servers and clients
 * 
 * @author
 * 
 */
public class Util {

	public static String SALT = "23IshitaMalhotra";
	public static String FUNCTION_NAME = "SHA-512";
	private int D = 30;
	private int d = 2;

	/**
	 * clacServerHash function calculates the hash value based in the file name,
	 * level ,position
	 * 
	 * @param fileName
	 *            name of the file
	 * @param level
	 *            level of the node in the lookup tree
	 * @param pos
	 *            position of the node in the lookup tree
	 * @return hash value
	 */
	public int[] calcServerHash(String fileName, int level, int pos) {
		
		int degree = D;
		int[] res = new int[2];
		if (level == 0) {
			res[0] = 0;
		} else {
			int num = 0;
			for (int i = level - 1; i >= 0; i--) {
				num += Math.pow(degree, i);
			}
			num += pos;
			res[0] = num - 1;
		}

		// calculating the has value
		res[1] = fileHash(fileName) % MasterStatic.getNumberOfServer();
		
		//System.out.println("res[0] : " + res[0] + "res[1] : " + res[1]);

		// returning hash value
		return res;

	}

	/**
	 * fileHash function is used to compute the hash value of file name using
	 * SHA function
	 * 
	 * @param fileName
	 *            name of the file
	 * @return int hash Value
	 */
	public int fileHash(String fileName) {
		try {

			// convert string to bytes
			MessageDigest digest = MessageDigest.getInstance(FUNCTION_NAME);
			digest.update(SALT.getBytes("UTF-8"));
			byte[] out = digest.digest((fileName + SALT).getBytes("UTF-8"));

			// compute the hex string from the bytes
			String hex = hexToString(out);
			BigInteger val = new BigInteger(hex, 16);
			int mappedValue = val.intValue();

			// return int value
			return mappedValue;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}

	/**
	 * Convert the hex result of a hash function to string
	 * 
	 * @param byte[]
	 * @return String hex in the form of String
	 */
	public String hexToString(byte[] output) {
		char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };

		StringBuffer buf = new StringBuffer();

		for (int j = 0; j < output.length; j++) {
			buf.append(hexDigit[(output[j] >> 4) & 0x0f]);
			buf.append(hexDigit[output[j] & 0x0f]);
		}

		return buf.toString();
	}

	/**
	 * To get the precise leaf count with respect to the number of nodes in the
	 * tree
	 * 
	 * @param int number of nodes in the tree
	 * @return int number of leaves in the tree
	 */

	public int getLeafCount(int size, String type) {
		int degree = (type.equals("rep")) ? d : D;
		int exp = -1;
		while (!((size - Math.pow((double) degree, (++exp))) <= (double) (0))) {
			size = (int) ((size - Math.pow((double) degree, (exp))));
		}
		// System.out.println("leaf count ....." + size);
		return size;
	}

	/**
	 * To get the number of nodes in the level for a general binary tree
	 * 
	 * @param int level in the tree
	 * @return int number of nodes in the tree at the level
	 */

	public int getNodesInLevel(int level, String type) {
		int degree = (type.equals("rep")) ? d : D;
		return (int) (Math.pow(degree, level));
	}

	/**
	 * To get the precise level count for the nodes available if we were to
	 * construct a binary tree
	 * 
	 * @param int number of total nodes in the tree
	 * @return int number of levels in the tree
	 */

	public int getLevels(int size, String type) {
		int degree = (type.equals("rep")) ? d : D;
		int exp = -1;
		while (!((size - Math.pow((double) degree, (++exp))) <= (double) (0))) {
			size = (int) ((size - Math.pow((double) degree, (exp))));
		}
		return exp;
	}

	/**
	 * To get a random in a range of values
	 * 
	 * @param int minimum value
	 * @param int maximum value or upto
	 * @return int random value in the range
	 */
	public int getRandom(int min, int max) {
		Random r = new Random();
		max = max + 1;
		return r.nextInt(max - min) + min;
	}

	/**
	 * selectReplicationChild function computes the random child from the
	 * replication tree which has this node as root
	 * 
	 * @param server
	 *            object of server
	 * @return position of the randomly chosen child in the lookup tree
	 */
	public int selectReplicationChild(ServerStatic server) {
		int size = MasterStatic.getNumberOfServer();
		int degree = D;
		int level = server.cLevelLookup;
		int pos = server.cPosLookup;
		int sum = 0;

		// computing the number of children
		for (int i = level; i >= 0; i--) {
			sum += Math.pow(degree, i);
		}
		size = size - sum;
		sum = 0;
		for (int i = 1; i < pos; i++) {
			sum += degree;
		}
		degree = d;
		int numOfChild;
		if (degree <= (size - sum)) {
			numOfChild = degree;
		} else {
			numOfChild = (size - sum);
		}

		// selecting random child
		return getRandom(sum + 1, sum + numOfChild);

	}

	/**
	 * selectMyChild function chooses randomly the child of this node in the
	 * replication tree
	 * 
	 * @param server
	 *            Object of this server
	 * @return int position of the node in the lookup tree
	 */
	public int selectMyChild(ServerStatic server) {
		int size = MasterStatic.getNumberOfServer();
		int degree = D;
		int pos = server.cPosLookup;
		int sum = 0;
		for (int i = server.parentLevel; i >= 0; i--) {
			sum += Math.pow(degree, i);
		}
		size = size - sum;

		// calculating the number of child
		sum = 0;
		for (int i = 1; i < server.parentPos; i++) {
			sum += degree;
		}

		size = size - sum;
		int numOfChild;
		if (degree <= size) {
			numOfChild = degree;
		} else {
			numOfChild = size;
		}

		// choosing the child randomly
		int maxLimit = sum + numOfChild;
		int min = (d * (pos - sum)) + 1;
		min += sum;
		int max = (d * (pos - sum)) + d;
		max += sum;

		if (max <= maxLimit) {
			return getRandom(min, max);
		} else {
			return getRandom(min, maxLimit);
		}
	}

	/**
	 * isLeafRep function is used to find whether the node is leaf in the
	 * replication tree
	 * 
	 * @param server
	 *            object of the server
	 * 
	 * @return boolean
	 */
	public boolean isLeafRep(ServerStatic server) {
		int size = MasterStatic.getNumberOfServer();
		int degree = D;
		int pos = server.cPosLookup;

		// computing the number of child
		int sum = 0;
		for (int i = server.parentLevel; i >= 0; i--) {
			sum += Math.pow(degree, i);
		}
		size = size - sum;
		sum = 0;
		for (int i = 1; i < server.parentPos; i++) {
			sum += degree;
		}

		size = size - sum;
		int numOfChild;
		if (degree <= size) {
			numOfChild = degree;
		} else {
			numOfChild = size;
		}

		int maxLimit = sum + numOfChild;
		int min = (d * (pos - sum)) + 1;
		min += sum;
		int max = (d * (pos - sum)) + d;
		max += sum;
		if (min <= maxLimit || max <= maxLimit) {
			return false;
		} else {
			return true;
		}

	}

	/**
	 * isLeaf function is used to find whether the node is leaf in the lookup
	 * tree
	 * 
	 * @param server
	 *            object of the server
	 * 
	 * @return boolean
	 */
	public boolean isLeaf(ServerStatic server, String fileName, String type) {
		int size = MasterStatic.getNumberOfServer();
		int levels = getLevels(size, type);
		int degree;
		int level;
		int pos;
		degree = D;
		level = server.cLevelLookup;
		pos = server.cPosLookup;

		if (levels == level) {
			return true;
		}
		int sum = 0;
		for (int i = level; i >= 0; i--) {
			sum += Math.pow(degree, i);
		}
		for (int i = 1; i < pos; i++) {
			sum += degree;
		}
		if (sum <= size) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * selectLeftParent is used to choose the left parent which is parent of
	 * node in the replication tree.
	 * 
	 * @param server Object of server
	 * 
	 * @return Object of node which has the position and level of node
	 */
	
	public Node selectLeftParent(ServerStatic server) {
		int temp = server.cPosLookup % D;

		// computing the position of parent node
		int parentPos = (temp == 0) ? server.cPosLookup / D
				: (server.cPosLookup / D) + 1;
		
		int parentLevel = server.cLevelLookup -1;
		
		ArrayList<Node> arr = new ArrayList<>();
		
		arr.add(new Node(parentLevel, parentPos));
		
		int size = MasterStatic.getNumberOfServer();
		int degree = D;
		int level = parentLevel;
		int pos = parentPos;
		int sum = 0;

		// computing the number of children
		for (int i = level; i >= 0; i--) {
			sum += Math.pow(degree, i);
		}
		size = size - sum;
		sum = 0;
		for (int i = 1; i < pos; i++) {
			sum += degree;
		}
		
		
		int numOfChild;
		if (degree <= (size - sum)) {
			numOfChild = degree;
		} else {
			numOfChild = (size - sum);
		}
		
		int min = sum+1;
		int max = sum + numOfChild;
		int index = -1;
		
		for(int i = min; i <= max; i++) {
			arr.add(new Node(parentLevel+1, i));
			if(i == server.cPosLookup) {
				index = arr.size()-1;
				break;
			}
		}
		if(index == -1) {
			System.out.println("Serious error");
			System.exit(0);
		}
		
		int value = (index - 1) / d;
		
		return arr.get(value);
	}
}