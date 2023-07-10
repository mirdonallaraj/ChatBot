package user;

import java.sql.SQLException;
import java.util.Scanner;

import util.MySQLConnector;
import util.Util;

/**
 * Class for manually registering users in the chat system.
 */
public class RegisterUser {
		
	private static MySQLConnector mysql;

	private static Scanner input = new Scanner(System.in);
	
	public RegisterUser() throws SQLException {
		mysql = new MySQLConnector("localhost", "chat_bot", "root", "root");
	}
	
	private static void registerUsers(int n) throws SQLException {
		for (int i = 0; i < n; i++) {

			System.out.print("Username: ");
			String username = input.nextLine();

			System.out.print("Password: ");
			String password = input.nextLine();
			System.out.println();
			
			User user = new User(username, password);
			if (!Util.existUsername(username, mysql)) {
			    mysql.executeUpdate("INSERT INTO `user` (`username`, `password`) "
			                + "VALUES('" + user.getUsername() +  "',"
			                        + "'" + user.getEncryptedPassword() + "');");
			}
		}
	}

	private static void createTables() {
		try {
			mysql.executeUpdate("CREATE TABLE IF NOT EXISTS `user` ("
									+ "  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,"
									+ "  `username` VARCHAR(64) NOT NULL,"
									+ "  `password` VARCHAR(100) NOT NULL,"
									+ "  PRIMARY KEY (`id`), "
									+ "  UNIQUE INDEX `id_UNIQUE` (`id` ASC) VISIBLE);");
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws SQLException {
		
		createTables();

		System.out.print("Write the number of users you want to register: ");

		int n = input.nextInt();
		input.nextLine();

		registerUsers(n);
		
		input.close();
	}
}
