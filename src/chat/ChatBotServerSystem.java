package chat;

import java.io.IOException;
import java.sql.SQLException;

public class ChatBotServerSystem {
	
	public static void main(String[] args) throws IOException, SQLException {
		
		try (ChatBotServer server = new ChatBotServer("localhost", 
						   10743, 
						   "localhost", 
						   "root", 
						   "root", 
						   "chat_bot")) {
			server.on();
		}
		
	}

}
