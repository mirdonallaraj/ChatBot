package util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import asnwer.Answers;
import user.User;

public class Util {

	/**
	 * Retrieves a list of all users from the user table.
	 *
	 * @param mysql The MySQLConnector object for database operations.
	 * @return an ArrayList of User objects representing the users.
	 */
	public static ArrayList<User> getUsers(MySQLConnector mysql){
		ArrayList<User> users = new ArrayList<>();
		try {
			ResultSet rs = mysql.executeQuery("SELECT * FROM `user`");
			while (rs.next()) {
				User user = new User(rs.getString("username"), User.decryptPsw(rs.getString("password")));
				user.setId(rs.getInt("user_id"));
				users.add(user);
			}
				return users;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Checks if the given username exists in the user table.
	 *
	 * @param username The username to check.
	 * @param mysql    The MySQLConnector object for database operations.
	 * @return true if the username exists, false otherwise.
	 */
	public static boolean existUsername(String username, MySQLConnector mysql) {
		if (username == null) {
			return false;
		}
		ArrayList<User> users = getUsers(mysql); 
		for (User user : users) {
			if (user.getUsername().equalsIgnoreCase(username)) {
				return true;
			}
		}
		return false;
	} 
	
	/**
	 * Checks if the provided username and password match the user's credentials.
	 *
	 * @param username The username to check.
	 * @param password The password to check.
	 * @param mysql    The MySQLConnector object for database operations.
	 * @return true if the username and password match the user's credentials, false otherwise.
	 */
	public static boolean isCorrectUserPassword(String username, String password, MySQLConnector mysql) {
		if (username == null && password == null) {
			return false;
		}
		ArrayList<User> users = getUsers(mysql); 
		for (User user : users) {
			if (user.getUsername().equalsIgnoreCase(username) && 
				User.decryptPsw(user.getEncryptedPassword()).equalsIgnoreCase(password)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Retrieves a list of all admins from the admins table.
	 *
	 * @param mysql The MySQLConnector object for database operations.
	 * @return an ArrayList of User objects representing the admins.
	 */
	public static ArrayList<User> getAdmins(MySQLConnector mysql) {
		ArrayList<User> users = new ArrayList<>();
		try {
			ResultSet rs = mysql.executeQuery("SELECT * FROM `user`"
					+ "JOIN `admins` ON `user`.`user_id` = `admins`.`user_id`");
			while (rs.next()) {
				users.add(new User(rs.getString("username"), User.decryptPsw(rs.getString("password"))));
			}
			return users;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * Checks if the given username exists in the admins table.
	 *
	 * @param username The username to check.
	 * @param mysql    The MySQLConnector object for database operations.
	 * @return true if the username exists, false otherwise.
	 */
	public static boolean existAdmin(String username, MySQLConnector mysql) {
		if (username == null) {
			return false;
		}
		ArrayList<User> users = getAdmins(mysql);
		for (User user : users) {
			if (user.getUsername().equalsIgnoreCase(username)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if the provided username and password match the admin's credentials.
	 *
	 * @param username The username to check.
	 * @param password The password to check.
	 * @param mysql    The MySQLConnector object for database operations.
	 * @return true if the username and password match the user's credentials, false otherwise.
	 */
	public static boolean isCorrectAdminPassword(String username, String password, MySQLConnector mysql) {
		if (username == null && password == null) {
			return false;
		}
		ArrayList<User> users = getAdmins(mysql); 
		for (User user : users) {
			if (user.getUsername().equalsIgnoreCase(username) && 
				User.decryptPsw(user.getEncryptedPassword()).equalsIgnoreCase(password)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gets the user ID based on the provided username.
	 *
	 * @param username The username to search for.
	 * @param mysql    The MySQLConnector object for database operations.
	 * @return the user ID if the username exists, -1 otherwise.
	 */
	public static int getUserId(String username, MySQLConnector mysql) {
		for(User user : getUsers(mysql)) {
			if (user.getUsername().trim().equalsIgnoreCase(username)) {
				return user.getId();
			}
		}
		return -1;
	}
	
	/**
	 * Retrieves the chat history as a string for the given username.
	 *
	 * @param username The username to retrieve the chat history for.
	 * @param mysql    The MySQLConnector object for database operations.
	 * @return a string representing the chat history for the given username.
	 */
	public static String getChatHistoryString(String username, MySQLConnector mysql) {
		int id1 = getUserId(username, mysql);
		int id2 = getUserId("MirdoBot", mysql);
		try {
			ResultSet rs = mysql.executeQuery("SELECT * FROM `message` WHERE (`receiver_id` = '" + id2 + "' AND"
																		   + "`sender_id` = '" + id1 + "') OR "
																		  + "(`sender_id` = '" + id2 + "' AND "
																		   + "`receiver_id` = '" + id1 + "') "
																			+ "ORDER BY `date` ASC");
			StringBuilder sb = new StringBuilder();
			while (rs.next()) {
				sb.append("Date:  " + rs.getTimestamp("date") + "\n");
				if (rs.getInt("receiver_id") == id1) {
					sb.append("Message from MirdoBot:");
				} else {
					sb.append("Message from " + username + ":");
				}
				sb.append(rs.getString("text") + "\n\n");
			}
			return sb.toString();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Retrieves the question-answer pairs from the answer_store table.
	 *
	 * @return The map of question-answer pairs.
	 */
	public static Map<String, String> getQuestionAnswer(MySQLConnector mysql) {
		Answers result = new Answers();
		try {
			ResultSet rs = mysql.executeQuery("SELECT `question`, `answer` FROM `answer_store`");
			while (rs.next()) {
				result.addAnswer(rs.getString("question").toLowerCase(), rs.getString("answer").toLowerCase());
			}
			return result.getAnswers();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
