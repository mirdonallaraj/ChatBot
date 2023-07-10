package chat;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Map;

import user.User;
import util.MySQLConnector;
import util.Util; 

public class ChatBotServer extends ServerSocket {
	
	private String servername;		 
	private MySQLConnector mysql;	
	
	/**
	 * Creates a new ChatBotServer instance.
	 *
	 * @param servername           The name of the server.
	 * @param port                 The port number to listen on.
	 * @param databaseServerName   The name of the database server.
	 * @param databaseUser         The username for the database connection.
	 * @param databasePassword     The password for the database connection.
	 * @param databaseName         The name of the database.
	 * @throws IOException  If an I/O error occurs.
	 * @throws SQLException If a SQL error occurs.
	 */
	public ChatBotServer(String servername,
						 int port,
						 String databaseServerName,
						 String databaseUser,
						 String databasePassword,
						 String databaseName) throws IOException, SQLException {
		super(port);
		this.servername = servername;
		mysql = new MySQLConnector(databaseServerName, databaseName, databaseUser, databasePassword);
	}
	
	public String getServername() {
		return servername;
	}
	  
	private String getUserMenu() {
		return "\nWrite option: \n"
			 + "  1. Chat with others \n"
			 + "  2. Chat with MirdoBot \n"
			 + "  3. Log out\n";
	}

	private String getAdminMenu() {
		return "\nWrite option: \n"
			 + "  1. Chat with others \n"
			 + "  2. Chat with MirdoBot \n"
			 + "  3. Add new Admin \n"
			 + "  4. Add new Users \n"
			 + "  5. Answers unknown questions \n"
			 + "  6. Log out";
	}
	
	private String chatBotMenu() {
		return  "Write option: \n" +
			    "   1. Learn about java classes \n" + 
				"   2. Learn generics in java \n" +
				"   3. Java Exceptions \n"+ 
				"   4. Collections in java \n" + 
				"   5. Learn IO and NIO \n" + 
				"   6. Other services \n" + 
				"   7. Exit \n";
	}
	
	private String logInMenu() {
		return "Write option: \n"
			 + "  1. Log In As Admin \n"
			 + "  2. Log In \n"
		     + "  3. Sign up \n"
		     + "  4. Exit ";
	}

	/**
	 * Creates the required database tables if they do not already exist
	 * and also adds MirdoBot into the user table.
	 *
	 * @param mysql The MySQLConnector object used to execute SQL statements.
	 */
	private static void createTables(MySQLConnector mysql) {
		try {
			mysql.executeUpdate("CREATE TABLE IF NOT EXISTS `user` ("
							+ "  `user_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,"
							+ "  `username` VARCHAR(64) NOT NULL,"
							+ "  `password` VARCHAR(100) NOT NULL,"
							+ "  PRIMARY KEY (`user_id`), "
							+ "  UNIQUE INDEX `id_UNIQUE` (`user_id` ASC) VISIBLE);");
			mysql.executeUpdate("CREATE TABLE IF NOT EXISTS `message` ("
							+ "  `message_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,"
							+ "  `sender_id` INT UNSIGNED NOT NULL,"
							+ "  `receiver_id` INT UNSIGNED NOT NULL,"
							+ "  `date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
							+ "  `text` VARCHAR(100) NOT NULL,"
							+ "  PRIMARY KEY (`message_id`),"
							+ "  UNIQUE INDEX `message_id_UNIQUE` (`message_id` ASC) VISIBLE,"
							+ "  INDEX `sender_id_idx` (`sender_id` ASC) VISIBLE,"
							+ "  INDEX `fk_receiver_id_idx` (`receiver_id` ASC) VISIBLE,"
							+ "  CONSTRAINT `fk_sender_id`"
							+ "    FOREIGN KEY (`sender_id`)"
							+ "    REFERENCES `chat_bot`.`user` (`user_id`)"
							+ "    ON DELETE NO ACTION"
							+ "    ON UPDATE NO ACTION,"
							+ "  CONSTRAINT `fk_receiver_id`"
							+ "    FOREIGN KEY (`receiver_id`)"
							+ "    REFERENCES `chat_bot`.`user` (`user_id`)"
							+ "    ON DELETE NO ACTION"
							+ "    ON UPDATE NO ACTION);");
			mysql.executeUpdate("CREATE TABLE IF NOT EXISTS `answer_store` ("
							+ "  `answer_store_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,"
							+ "  `question` VARCHAR(120) NOT NULL,"
							+ "  `answer` VARCHAR(150) NOT NULL,"
							+ "  PRIMARY KEY (`answer_store_id`),"
							+ "  UNIQUE INDEX `answer_store_id_UNIQUE` (`answer_store_id` ASC) VISIBLE);");
			
			mysql.executeUpdate("CREATE TABLE IF NOT EXISTS `admins` ("
					        + "`id` INT AUTO_INCREMENT PRIMARY KEY,"
					        + "`user_id` INT UNSIGNED NOT NULL,"
					        + "CONSTRAINT `fk_user_id`"
					        + "  FOREIGN KEY (`user_id`) "
					        + "  REFERENCES `user` (`user_id`)"
					        + ");");
			
			User user = new User("MirdoBot", "server");
			mysql.executeUpdate("INSERT IGNORE INTO `user` (`username`, `password`) "
			                + "VALUES('" + user.getUsername() +  "',"
			                + "'" + user.getEncryptedPassword() + "');");
			mysql.executeUpdate("INSERT IGNORE INTO `admins` (`user_id`) "
	                + "VALUES(1);");
			mysql.executeUpdate("INSERT IGNORE INTO `answer_store` (`question`, `answer`) "
									                + "VALUES('hi',"
									                + "'hello');");
			mysql.executeUpdate("INSERT IGNORE INTO `answer_store` (`question`, `answer`) "
												                + "VALUES('hello',"
												                + "'hi');");
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Starts the server and listens for incoming client connections.
	 *
	 * @throws IOException If an I/O error occurs.
	 */
	public void on() throws IOException {
		createTables(mysql);
		while(true) {
			Socket client = accept();
			new Thread() {
				public void run() {
					try {
						DataInputStream input = new DataInputStream(client.getInputStream());
						DataOutputStream output = new DataOutputStream(client.getOutputStream());
						output.writeUTF(logInMenu());
						output.flush();
						String option = input.readUTF().trim();
						boolean exit = false;
						while (!exit) {
							switch (option) {
							case "1":
								logInAdmin(client);
								break;
							case "2":
								logInUser(client);
								break;
							case "3":
								registerUser(client);
								break;
							case "4":
								exit = true;
								break;
							default:
									output.writeUTF("Wrong option!");
									output.flush();
									break;
							}
							if (!exit) {
								output.writeUTF(logInMenu());
								output.flush();
								option = input.readUTF().trim();
							}
						}
						client.close();
						output.close();
						input.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
	}
	
	/**
	 * Logs in as admin user.
	 *
	 * @param client The client socket.
	 * @throws IOException If an I/O error occurs.
	 */
	private void logInAdmin(Socket client) throws IOException {
		DataInputStream input = new DataInputStream(client.getInputStream());
		DataOutputStream output = new DataOutputStream(client.getOutputStream());
		output.writeUTF("Username: ");
		output.flush();
		String username = input.readUTF();
		while (!Util.existAdmin(username, mysql)) {
			output.writeUTF("Username doesn't exist!");
			output.flush();
			output.writeUTF("Write username again, or write 'stop' to exit: ");
			output.flush();
			username = input.readUTF().trim();
			if (username.equalsIgnoreCase("stop")) {
				return;
			}
		}
		output.writeUTF("Password: ");
		output.flush();
		String password = input.readUTF();
		while (!Util.isCorrectAdminPassword(username, password, mysql)) {
			output.writeUTF("Wrong password!");
			output.flush();
			output.writeUTF("Write password again, or write 'stop' to exit: ");
			output.flush();
			password = input.readUTF().trim();
			if (password.equalsIgnoreCase("stop")) {
				return;
			}
		}
		User loggedInAdmin = getUser(username, password);
		loggedInAdmin.setId(Util.getUserId(username, mysql));
		
		output.writeUTF(getAdminMenu());
		output.flush();
		String option = input.readUTF();
		while(true) {
		switch (option) {

		case "1":
			chat(loggedInAdmin, client);
			break;
		case "2":
			chatMirdoBot(loggedInAdmin, client);
			break;
		case "3":
			addNewAdmin(client);
			break;
		case "4":
			addNewUsers(client);
			break;
		case "5":
			addUnknownAsnwers(client);
			break;
		case "6":
			return;
		default:
			output.writeUTF("Wrong option!\n");
			output.flush();
			break;
		}
		output.writeUTF(getAdminMenu());
		output.flush();
		option = input.readUTF();
		}
	}
	
	/**
	 * Logs in as a common user.
	 *
	 * @param client The client socket.
	 * @throws IOException If an I/O error occurs.
	 */
	private void logInUser(Socket client) throws IOException {
		DataInputStream input = new DataInputStream(client.getInputStream());
		DataOutputStream output = new DataOutputStream(client.getOutputStream());
		output.writeUTF("Username: ");
		output.flush();
		String username = input.readUTF();
		while (!Util.existUsername(username, mysql)) {
			output.writeUTF("Username doesn't exist!");
			output.flush();
			output.writeUTF("Write username again, or write 'stop' to exit:");
			output.flush();
			username = input.readUTF().trim();
			if (username.equalsIgnoreCase("stop")) {
				return;
			}
		}
		output.writeUTF("Password: ");
		output.flush();
		String password = input.readUTF();
		while (!Util.isCorrectUserPassword(username, password, mysql)) {
			output.writeUTF("Wrong password!");
			output.flush();
			output.writeUTF("Write password again, or write 'stop' to exit: ");
			output.flush();
			password = input.readUTF().trim();
			if (password.equalsIgnoreCase("stop")) {
				return;
			}
		}
		User loggedInUser = getUser(username, password);
		loggedInUser.setId(Util.getUserId(username, mysql));

		output.writeUTF(getUserMenu());
		output.flush();
		String option = input.readUTF();

		while(true) {
			
		switch (option) {

			case "1":
				chat(loggedInUser, client);
				break;
			case "2":
				chatMirdoBot(loggedInUser, client);
				break;
			case "3":
				return;
			default:
				output.writeUTF("Wrong option!\n");
				output.flush();
				break;
		}
		output.writeUTF(getUserMenu());
		output.flush();
		option = input.readUTF();
		}
	}

	/**
	 * Initiates a chat between the logged-in user and another user.
	 *
	 * @param user   The User object representing the logged-in user.
	 * @param client The Socket representing the client connection.
	 * @throws IOException If an I/O error occurs while communicating with the client.
	 */
	private void chat(User user, Socket client) throws IOException {
		DataInputStream input = new DataInputStream(client.getInputStream());
		DataOutputStream output = new DataOutputStream(client.getOutputStream());
		output.writeUTF("Username: ");
		output.flush();
		String receiverUsername = input.readUTF();
		while(!Util.existUsername(receiverUsername, mysql)) {
			output.writeUTF("Username doesn't exists!");
			output.flush();
			output.writeUTF("Write username again, or write 'stop' to stop the chat: ");
			output.flush();
			receiverUsername = input.readUTF().trim();
			if(receiverUsername.equalsIgnoreCase("stop")) {
				return;
			}
		}
		output.writeUTF("\n" + getChatHistoryString(user.getUsername(), receiverUsername));
		output.flush();
		while(true) {
			output.writeUTF("Write new message , or 'stop' to stop the chat: ");
			output.flush();
			String newMessage = input.readUTF();
			if (!newMessage.equalsIgnoreCase("stop")) {
				sendNewMessage(user.getUsername(), receiverUsername, newMessage);
			}else {
				return;
			}
			output.writeUTF(getLastInsertedMessage(user, Util.getUserId(receiverUsername, mysql)));
			output.flush();
		}
	}
	
	/**
	 * Initiates a chat session with MirdoBot, showing all services of MirdoBot with options.
	 *
	 * @param loggedInUser The currently logged-in user.
	 * @param client       The client socket.
	 * @throws IOException If an I/O error occurs.
	 */
	private void chatMirdoBot(User loggedInUser, Socket client) throws IOException {
		DataInputStream input = new DataInputStream(client.getInputStream());
		DataOutputStream output = new DataOutputStream(client.getOutputStream());
		if (Util.getChatHistoryString(loggedInUser.getUsername(), mysql) != null || 
			!Util.getChatHistoryString(loggedInUser.getUsername(), mysql).isEmpty()) {
			output.writeUTF("Previous chat");
			output.flush();
			output.writeUTF(Util.getChatHistoryString(loggedInUser.getUsername(), mysql));
			output.flush();
		}
		output.writeUTF("Hi! I'm MirdoBot,  What would you like to do?");
		output.flush();
		
		while(true) {
			
			output.writeUTF(chatBotMenu());
			output.flush();
			String option = input.readUTF();
			 if (option.trim().equals("7")) {
				return;
			} else if (option.trim().equals("1") || option.trim().equals("2") || 
					   option.trim().equals("3") || option.trim().equals("4") || 
					   option.trim().equals("5") || option.trim().equals("6")){
				output.writeUTF(getAnswer(option, loggedInUser, client));
				output.flush();
			}else {
				output.writeUTF("Wrong option!");
				output.flush();
			}
			output.writeUTF("What would you like to do?");
			output.flush();
		}
	}
	
	/**
	 * Adds a new admin user to the system.
	 *
	 * @param client The client socket.
	 * @throws IOException If an I/O error occurs.
	 */
	private void addNewAdmin(Socket client) throws IOException {
		DataInputStream input = new DataInputStream(client.getInputStream());
		DataOutputStream output = new DataOutputStream(client.getOutputStream());
		output.writeUTF("Username: ");
		output.flush();
		String username = input.readUTF();
		while (!Util.existUsername(username, mysql)) {
			output.writeUTF("Username doesn't exist!");
			output.flush();
			output.writeUTF("Write username again, or write 'stop' to exit: ");
			output.flush();
			username = input.readUTF().trim();
			if (username.equalsIgnoreCase("stop")) {
				return;
			}
		}
		while (Util.existAdmin(username, mysql)) {
			output.writeUTF("Admin already exist!");
			output.flush();
			output.writeUTF("Write username again, or write 'stop' to exit: ");
			output.flush();
			username = input.readUTF().trim();
			if (username.equalsIgnoreCase("stop")) {
				return;
			}
		}
		if (Util.existUsername(username, mysql)) {
			output.writeUTF("Admin added sucessfully!");
			output.flush();
			try {
				mysql.executeUpdate("INSERT INTO `admins` (`user_id`) VALUES('" + Util.getUserId(username, mysql) + "')");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}else {
			output.writeUTF("Username doesn't exist!");
			output.flush();
		}
	}
	
	/**
	 * Adds new user(s) to the system.
	 *
	 * @param client The client socket.
	 * @throws IOException If an I/O error occurs.
	 */
	private void addNewUsers(Socket client) throws IOException {
		DataInputStream input = new DataInputStream(client.getInputStream());
		DataOutputStream output = new DataOutputStream(client.getOutputStream());
		output.writeUTF("How many users do you want to add? ");
		output.flush();
		String number = input.readUTF();
		while (!number.matches("[0-9]+")) {
			output.writeUTF("Wrong input! Write another number or 'stop' to exit:");
			output.flush();
			number = input.readUTF();
			if (number.equalsIgnoreCase("stop")) {
				return;
			}
		} 
		for (int i = 0; i < Integer.parseInt(number); i++) {
			registerUser(client);
		}
	}

	/**
	 * Registers a new user.
	 *
	 * @param client The client socket.
	 * @throws IOException If an I/O error occurs.
	 */
	private void registerUser(Socket client) throws IOException {
		DataInputStream input = new DataInputStream(client.getInputStream());
		DataOutputStream output = new DataOutputStream(client.getOutputStream());
		output.writeUTF("\nUsername: ");
		output.flush();
		String username = input.readUTF();
		while (Util.existUsername(username, mysql)) {
			output.writeUTF("Username already exist!");
			output.flush();
			output.writeUTF("Write username again, or write 'stop' to exit: ");
			output.flush();
			username = input.readUTF().trim();
			if (username.equalsIgnoreCase("stop")) {
				return;
			}
		}
		output.writeUTF("Password: ");
		output.flush();
		String password = input.readUTF();
		addUser(username, password, client);
		output.writeUTF("User registered sucessfully! ");
		output.flush();
	}
	
	/**
	 * Adds a new user to the database.
	 *
	 * @param username The username of the user.
	 * @param password The password of the user.
	 * @param client   The Socket representing the client connection.
	 */
	private void addUser(String username, String password, Socket client) {
			User user = new User(username, password);
			   try {
				mysql.executeUpdate("INSERT INTO `user` (`username`, `password`) "
				                + "VALUES('" + user.getUsername() +  "',"
				                        + "'" + user.getEncryptedPassword() + "');");
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}
	
	/**
	 * Adds answers for the questions that MirdoBot got but didn't have a response in the answer_store table.
	 *
	 * @param client The client socket.
	 * @throws IOException If an I/O error occurs.
	 */
	private void addUnknownAsnwers(Socket client) throws IOException{
		DataInputStream input = new DataInputStream(client.getInputStream());
		DataOutputStream output = new DataOutputStream(client.getOutputStream());
		Map<String, String> qa = Util.getQuestionAnswer(mysql);
//		if (!qa.containsValue("Sorry, I cannot answer this question!")) {
//			output.writeUTF("There are no new questions to answer!\n");
//			output.flush();
//			return ;
//		}
		for(String question : qa.keySet()) {
			if (qa.get(question).equalsIgnoreCase("Sorry, I cannot answer this question!")) {
				output.writeUTF("Give the answer for this question: " + question);
				output.flush();
				String answer = input.readUTF();
				if (answer != null) {
					qa.put(question, answer.toLowerCase());
					try {
						mysql.executeUpdate("UPDATE `answer_store` SET `answer` = '" + qa.get(question) + "' "
								+ "WHERE `question` = '" + question + "'");
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return ;
	}
	
	/**
	 * Retrieves the ID of the last inserted message in the given table for the specified users id.
	 *
	 * @param mysql     The MySQLConnector instance.
	 * @param tableName The name of the table.
	 * @param userId1   The ID of the first user.
	 * @param userId2   The ID of the second user.
	 * @return The ID of the last inserted message.
	 */
	private static int getLastInsertId(MySQLConnector mysql, String tableName, int userId1, int userId2) {
	    try {
	        String query = "SELECT LAST_INSERT_ID() FROM `" + tableName + "` WHERE (`sender_id` = '" + userId1 + "' AND"
	        																   + " `receiver_id` = '" + userId2 + "') OR"
	        																   	+ "(`sender_id` = '" + userId2 + "' AND"
	        																   	+ "`receiver_id` = '" + userId1 +"')";
	        ResultSet rs = mysql.executeQuery(query);
	        if (rs.next()) {
	            return rs.getInt(1);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return 0;
	}
	
	/**
	 * Retrieves the last inserted message for the specified user and second user.
	 *
	 * @param user    The user.
	 * @param userId2 The ID of the second user.
	 * @return The last inserted message as a string.
	 */
	private String getLastInsertedMessage(User user, int userId2) {
		int messageId = getLastInsertId(mysql, "message", user.getId(), userId2);
		try {
			ResultSet rs = mysql.executeQuery("SELECT * FROM `message` WHERE `message_id` = " +  messageId + "");
			StringBuilder sb = new StringBuilder();
			if (rs.next()) {
				sb.append("Date:  " + rs.getTimestamp("date") + "\n");
				if (rs.getInt("receiver_id") == user.getId()) {
					sb.append("Message from " + getUsername(userId2) + ": ");
				}else {
					sb.append("Message from " + user.getUsername() + ": ");
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
	 * Retrieves the username for the specified ID.
	 *
	 * @param id The ID of the user.
	 * @return The username associated with the ID.
	 */
	private String getUsername(int id) {
		try {
			ResultSet rs = mysql.executeQuery("SELECT `username` FROM `user` WHERE `user_id` = " + id + " ");
			if (rs.next()) {
				return rs.getString("username");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Retrieves the user with the given username and password from database.
	 *
	 * @param username The username of the user.
	 * @param password The password of the user.
	 * @return The user using the User class of null if no user exits.
	 */
	private User getUser(String username, String password) {
		if (username != null && password != null) {
			ArrayList<User> users = Util.getUsers(mysql); 
			for (User user : users) {
				if (user.getUsername().equalsIgnoreCase(username) && 
					User.decryptPsw(user.getEncryptedPassword()).equalsIgnoreCase(password)) {
					return user;
				}
			}
		}
		return null;
	}

	/**
	 * Retrieves the chat history between two users as a formatted string.
	 *
	 * @param username1 The username of the first user.
	 * @param username2 The username of the second user.
	 * @return The chat history as a formatted string.
	 */
	private String getChatHistoryString(String username1, String username2) {
		int id1 = Util.getUserId(username1, mysql);
		int id2 = Util.getUserId(username2, mysql);
		if (id1 != -1 && id2 != -1) {
			try {
				ResultSet rs = mysql.executeQuery("SELECT * FROM `message` WHERE (`receiver_id` = '" + id1 + "' AND"
																				+ "`sender_id` = '" + id2 + "') OR "
																				+ "(`sender_id` = '" + id1 + "' AND "
																				+ "`receiver_id` = '" + id2 + "') "
																				+ "ORDER BY `date` ASC");
				StringBuilder sb = new StringBuilder();
				while (rs.next()) {
					sb.append("Date:  " + rs.getTimestamp("date") + "\n");
					if (rs.getInt("receiver_id") == id1) {
						sb.append("Message from " + username2 + ": ");
					}else {
						sb.append("Message from " + username1 + ": ");
					}
					sb.append(rs.getString("text") + "\n\n");
				}
				return sb.toString();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Sends a new message from the sender to the receiver.
	 *
	 * @param senderUsername   The username of the message sender.
	 * @param receiverUsername The username of the message receiver.
	 * @param message          The text for the message.
	 */
	private void sendNewMessage(String senderUsername, 
							    String receiverUsername, 
							    String message) {
		if (senderUsername != null || receiverUsername != null || message != null) {
			try {
				int id1 = Util.getUserId(senderUsername, mysql);
				int id2 = Util.getUserId(receiverUsername, mysql);
				mysql.executeUpdate("INSERT INTO `message` (`sender_id`, `receiver_id`, `date`, `text`) "
						  + " VALUES ('" + id1 + "', '" + id2 + "', '"
						  + new Timestamp(new GregorianCalendar().getTimeInMillis()) + "', '" + message + "')");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Adds a new question to the answer store.
	 *
	 * @param question The question to be added.
	 */
	private void addNewQuestion(String question) {
		try {
			mysql.executeUpdate("INSERT INTO `answer_store` (`question`, `answer`) "
													+ "VALUES('" + question + "', "
					                                       + "'Sorry, I cannot answer this question!')");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retrieves an answer based on the selected option for the MirdoBot.
	 *
	 * @param option       The selected option.
	 * @param loggedInUser The currently logged-in user.
	 * @param client       The client socket.
	 * @return The answer as a string.
	 * @throws IOException If an I/O error occurs.
	 */
	public String getAnswer(String option, User loggedInUser, Socket client) throws IOException {
		DataInputStream input = new DataInputStream(client.getInputStream());
		DataOutputStream output = new DataOutputStream(client.getOutputStream());
		switch (option) {

		case "1":
			return readFile("data\\classes.txt");
		case "2":
			return readFile("data\\generics.txt");
		case "3":
			return readFile("data\\exceptions.txt");
		case "4":
			return readFile("data\\collections.txt");
		case "5":
			return readFile("data\\ioAndNio.txt");
		case "6":
			output.writeUTF("Question: ");
			output.flush();
			String question = input.readUTF().trim();
			ResultSet rs;
			String newMessage = null;
			try {
				rs = mysql.executeQuery("SELECT `answer` FROM `answer_store` WHERE `question` = '" + question + "'");
				if (rs.next()) {
					newMessage = rs.getString(1);
					sendNewMessage("MirdoBot", loggedInUser.getUsername(), newMessage);
					sendNewMessage(loggedInUser.getUsername(), "MirdoBot", question);
					return "MirdoBot: " + rs.getString(1) + "\n";
				}else {
					addNewQuestion(question);
					return "MirdoBot: Sorry, I cannot answer this question! \n";
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * Reads a file and returns its contents as a string.
	 *
	 * @param src The path to the file.
	 * @return The contents of the file as a string.
	 */
	private String readFile(String src) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(src));
			String s;
			StringBuilder sb = new StringBuilder();
			while((s = br.readLine()) != null) {
				sb.append(s + "\n");
			}
			br.close();
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
