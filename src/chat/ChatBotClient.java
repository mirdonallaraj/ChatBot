package chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import asnwer.Answers;
import user.User;
import util.MySQLConnector;
import util.Util; 

	@SuppressWarnings("unused")
public class ChatBotClient extends Socket {
	
	private String servername;

	private int port;
	
	private User connectedUser;
	private MySQLConnector mysql;

	private static Scanner input = new Scanner(System.in);

	public ChatBotClient(String servername, int port) throws Exception {
		super(servername, port); 
		mysql = new MySQLConnector("localhost", "chat_bot", "root", "root");
	}
	
	/**
	 * Starts the chat server and handles client connections.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public void on() throws IOException {
		
		DataInputStream dataInput = new DataInputStream(getInputStream());
		DataOutputStream dataOutput = new DataOutputStream(getOutputStream());
		
		System.out.println(dataInput.readUTF());
		
		boolean exit = false;
		String username = null;
		String password= null;
		String option = nextLine();
		dataOutput.writeUTF(option);
		dataOutput.flush();
		while (!exit) {
			switch (option) {
			case "1":
				logInAdmin(this);
				break;
			case "2":
				logInUser(this);
				break;
			case "3":
				registerUser(this);
				break;
			case "4":
				exit = true;
				break;
			default:
				System.out.println(dataInput.readUTF());
				break;
			}
			if (!exit) {
				System.out.println(dataInput.readUTF());
				option = nextLine();
				dataOutput.writeUTF(option);
				dataOutput.flush();
			}
		}
		this.close();
		dataInput.close();
		dataOutput.close();
	}
	
	/**
	 * Performs the login process for an admin user.
	 *
	 * @param chatBotClient The ChatBotClient object representing the connected client.
	 * @throws IOException if an I/O error occurs.
	 */
	private void logInAdmin(ChatBotClient chatBotClient) throws IOException {
		DataInputStream dataInput = new DataInputStream(getInputStream());
		DataOutputStream dataOutput = new DataOutputStream(getOutputStream());
		System.out.print(dataInput.readUTF());
		String username = nextLine();
		dataOutput.writeUTF(username);
		dataOutput.flush();
		while (!Util.existAdmin(username, mysql)) {
			System.out.println(dataInput.readUTF());
			System.out.print(dataInput.readUTF());
			username = nextLine();
			dataOutput.writeUTF(username);
			dataOutput.flush();
			if (username.equalsIgnoreCase("stop")) {
				return;
			}
		}
		System.out.print(dataInput.readUTF());
		String password = nextLine();
		dataOutput.writeUTF(password);
		dataOutput.flush();
		while (!Util.isCorrectAdminPassword(username, password, mysql)) {
			System.out.println(dataInput.readUTF());
			System.out.print(dataInput.readUTF());
			password = nextLine();
			dataOutput.writeUTF(password);
			dataOutput.flush();
		}
		connectedUser = new User(username, password);
		
		System.out.println(dataInput.readUTF());
		String option = nextLine();
		dataOutput.writeUTF(option);
		dataOutput.flush();
		while (true) {
			switch (option) {

			case "1":
				chat(connectedUser, this);
				break;
			case "2":
				chatMirdoBot(connectedUser, this);
				break;
			case "3":
				addNewAdmin(this);
				break;
			case "4":
				addNewUsers(this);
				break;
			case "5":
				addUnknownAsnwers(this);
				break;
			case "6":
				return;
			default:
				dataOutput.writeUTF("Wrong option!");
				dataOutput.flush();
				break;
			}
			System.out.println(dataInput.readUTF());
			option = nextLine();
			dataOutput.writeUTF(option);
			dataOutput.flush();
		}
	}

	/**
	 * Performs the login process for a regular user.
	 *
	 * @param chatBotClient The ChatBotClient object representing the connected client.
	 * @throws IOException if an I/O error occurs.
	 */
	private void logInUser(ChatBotClient chatBotClient) throws IOException {

		DataInputStream dataInput = new DataInputStream(getInputStream());
		DataOutputStream dataOutput = new DataOutputStream(getOutputStream());
		System.out.print(dataInput.readUTF());
		String username = nextLine();
		dataOutput.writeUTF(username);
		dataOutput.flush();
		while (!Util.existUsername(username, mysql)) {
			System.out.println(dataInput.readUTF());
			System.out.print(dataInput.readUTF());
			username = nextLine();
			dataOutput.writeUTF(username);
			dataOutput.flush();
		}
		System.out.print(dataInput.readUTF());
		String password = nextLine();
		dataOutput.writeUTF(password);
		dataOutput.flush();
		
		while (!Util.isCorrectUserPassword(username, password, mysql)) {
			System.out.println(dataInput.readUTF());
			System.out.print(dataInput.readUTF());
			password = nextLine();
			dataOutput.writeUTF(password);
			dataOutput.flush();
		}
		connectedUser = new User(username, password);
		
		System.out.print(dataInput.readUTF());
		String option = nextLine();
		dataOutput.writeUTF(option);
		dataOutput.flush();
		while (true) {
			switch (option) {

			case "1":
				chat(connectedUser, this);
				break;
			case "2":
				chatMirdoBot(connectedUser, this);
				break;
			case "3":
				return;
			default:
				System.out.println(dataInput.readUTF());
				break;
			}
			System.out.print(dataInput.readUTF());
			option = nextLine();
			dataOutput.writeUTF(option);
			dataOutput.flush();
		}
	}
	
	/**
	 * Handles the chat functionality between two users.
	 *
	 * @param connectedUser The User object representing the connected user.
	 * @param client        The Socket object representing the client connection.
	 * @throws IOException if an I/O error occurs.
	 */
	private void chat(User connectedUser, Socket client) throws IOException {
		DataInputStream dataInput = new DataInputStream(getInputStream());
		DataOutputStream dataOutput = new DataOutputStream(getOutputStream());
		
		System.out.print(dataInput.readUTF());
		String username = nextLine();
		dataOutput.writeUTF(username);
		dataOutput.flush();
		
		while (!Util.existUsername(username, mysql)) {
			System.out.println(dataInput.readUTF());
			System.out.print(dataInput.readUTF());
			username = nextLine();
			dataOutput.writeUTF(username);
			dataOutput.flush();
			if (username.equalsIgnoreCase("stop")) {
				return;
			}
		}
		
		System.out.println(dataInput.readUTF());
		while(true) {
			System.out.print(dataInput.readUTF());
			String newMessage = nextLine();
			dataOutput.writeUTF(newMessage);
			dataOutput.flush();
			if (newMessage.equalsIgnoreCase("stop")) {
				return;
			}
			System.out.print(dataInput.readUTF());
		}
	}

	/**
	 * Handles the chat functionality with the MirdoBot.
	 *
	 * @param connectedUser The User object representing the connected user.
	 * @param client        The Socket object representing the client connection.
	 * @throws IOException if an I/O error occurs.
	 */
	private void chatMirdoBot(User connectedUser, Socket client) throws IOException {
		DataInputStream dataInput = new DataInputStream(getInputStream());
		DataOutputStream dataOutput = new DataOutputStream(getOutputStream());
		if (Util.getChatHistoryString(connectedUser.getUsername(), mysql) != null || 
			!Util.getChatHistoryString(connectedUser.getUsername(), mysql).isEmpty()) {
			System.out.println(dataInput.readUTF());
			System.out.println(dataInput.readUTF());
		}

		System.out.println(dataInput.readUTF());

		while(true) {
			System.out.print(dataInput.readUTF());
			String option = nextLine();
			dataOutput.writeUTF(option);
			dataOutput.flush();
			if (option.trim().equals("6")) {
				System.out.print(dataInput.readUTF());
				String question = nextLine();
				dataOutput.writeUTF(question);
				dataOutput.flush();
				System.out.println(dataInput.readUTF());
			}
			else if (option.equals("7")) {
				return;
			}else if (option.trim().equals("1") || option.trim().equals("2") || 
					   option.trim().equals("3") || option.trim().equals("4") || 
					   option.trim().equals("5")){
				System.out.println(dataInput.readUTF());
			}else {
				System.out.println(dataInput.readUTF());
			}
			System.out.println(dataInput.readUTF());
		}
	}

	/**
	 * Registers a new user.
	 *
	 * @param chatBotClient The ChatBotClient object representing the connected client.
	 * @throws IOException if an I/O error occurs.
	 */
	private void registerUser(ChatBotClient chatBotClient) throws IOException {
		DataInputStream dataInput = new DataInputStream(getInputStream());
		DataOutputStream dataOutput = new DataOutputStream(getOutputStream());
		System.out.print(dataInput.readUTF());
		String username = nextLine();
		dataOutput.writeUTF(username);
		dataOutput.flush();
		while(Util.existUsername(username, mysql)) {
			System.out.println(dataInput.readUTF());
			System.out.print(dataInput.readUTF());
			username = nextLine();
			dataOutput.writeUTF(username);
			dataOutput.flush();
			if (username.equalsIgnoreCase("stop")) {
				return;
			}
		}
		System.out.print(dataInput.readUTF());
		String password = nextLine();
		dataOutput.writeUTF(password);
		dataOutput.flush();
		System.out.println(dataInput.readUTF());
	}

	/**
	 * Adds answers for unknown questions to the answer_store table.
	 *
	 * @param chatBotClient The ChatBotClient object representing the connected client.
	 * @throws IOException if an I/O error occurs.
	 */
	private void addUnknownAsnwers(ChatBotClient chatBotClient) throws IOException {
		DataInputStream dataInput = new DataInputStream(getInputStream());
		DataOutputStream dataOutput = new DataOutputStream(getOutputStream());
		Map<String, String> qa = Util.getQuestionAnswer(mysql);
//		if (!qa.containsValue("Sorry, I cannot answer this question!")) {
//			System.out.print(dataInput.readUTF());
//			return;
//		}
		for (String question : qa.keySet()) {
			if (qa.get(question).equalsIgnoreCase("Sorry, I cannot answer this question!")) {
			System.out.println(dataInput.readUTF());
			String answer = nextLine();
			dataOutput.writeUTF(answer);
			dataOutput.flush();
			}
		}
	}

	/**
	 * Adds new users to the user table.
	 *
	 * @param chatBotClient The ChatBotClient object representing the connected client.
	 * @throws IOException if an I/O error occurs.
	 */
	private void addNewUsers(ChatBotClient chatBotClient) throws IOException {
		DataInputStream dataInput = new DataInputStream(getInputStream());
		DataOutputStream dataOutput = new DataOutputStream(getOutputStream());
		System.out.print(dataInput.readUTF());
		String number = nextLine();
		dataOutput.writeUTF(number);
		dataOutput.flush();
		while (!number.matches("[0-9]+")) {
			System.out.print(dataInput.readUTF());
			number = nextLine();
			dataOutput.writeUTF(number);
			dataOutput.flush();
			if (number.equalsIgnoreCase("stop")) {
				return;
			}
		}
		for (int i = 0; i < Integer.parseInt(number); i++) {
			registerUser(chatBotClient);
		}
	}

	/**
	 * Adds a new admin to the admins table.
	 *
	 * @param chatBotClient The ChatBotClient object representing the connected client.
	 * @throws IOException if an I/O error occurs.
	 */
	private void addNewAdmin(ChatBotClient chatBotClient) throws IOException {
		DataInputStream dataInput = new DataInputStream(getInputStream());
		DataOutputStream dataOutput = new DataOutputStream(getOutputStream());
		System.out.print(dataInput.readUTF());
		String username = nextLine();
		dataOutput.writeUTF(username);
		dataOutput.flush();
		while (!Util.existUsername(username, mysql)) {
			System.out.println(dataInput.readUTF());
			System.out.print(dataInput.readUTF());
			username = nextLine();
			dataOutput.writeUTF(username);
			dataOutput.flush();
			if (username.equalsIgnoreCase("stop")) {
				return;
			}
		}
		while (Util.existAdmin(username, mysql)) {
			System.out.println(dataInput.readUTF());
			System.out.print(dataInput.readUTF());
			username = nextLine();
			dataOutput.writeUTF(username);
			dataOutput.flush();
			if (username.equalsIgnoreCase("stop")) {
				return;
			}
		}
		System.out.println(dataInput.readUTF());
	}

	/**
	 * Reads the next non-empty line of text from the input.
	 *
	 * @return the next non-empty line of text.
	 */
	private static String nextLine() {
		String line = input.nextLine().trim();
		while(line.isEmpty()) {
			line = input.nextLine().trim();
		}
		return line;
	}
}
