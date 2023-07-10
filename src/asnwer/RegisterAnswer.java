package asnwer;

import java.sql.SQLException;
import java.util.Map;
import java.util.Scanner;

import util.MySQLConnector;
import util.Util;

/**
 * Class for manually registering answers for the unknown question that the chatBot has received in the chat system.
 */
public class RegisterAnswer {

	private MySQLConnector mysql;
	
	private static Scanner input = new Scanner(System.in);
	
	public RegisterAnswer() throws SQLException {
		mysql = new MySQLConnector("localhost", "chat_bot", "root", "root");
	}

	private Map<String, String> answerQuestions() {
		Map<String, String> qa = Util.getQuestionAnswer(mysql);
		for(String question : qa.keySet()) {
			if (qa.get(question).equalsIgnoreCase("Sorry, I cannot answer this question!")) {
				System.out.println("Give the answer for this question: " + question);
				String answer = nextLine();
				if (answer != null) {
					qa.put(question, answer.toLowerCase());
				}
				System.out.println();
			}
		}
		return qa;
	}

	private static String nextLine() {
		String line = input.nextLine().trim();
		while(line.isEmpty()) {
			line = input.nextLine().trim();
		}
		return line;
	}

	private void updateSqlAnswers(Map<String, String> updatedAnswers) {
		try {
			for (String question : updatedAnswers.keySet()) {
				mysql.executeUpdate("UPDATE `answer_store` SET `answer` = '" + updatedAnswers.get(question) + "' "
													+ "WHERE `question` = '" + question + "'");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws SQLException {
		RegisterAnswer rs = new RegisterAnswer();
		Map<String, String> updatedAnswers = rs.answerQuestions();
		
		rs.updateSqlAnswers(updatedAnswers);
	}

}
