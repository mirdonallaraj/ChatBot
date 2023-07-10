package chat;

public class ChatBotClientSystem {
	
	public static void main(String[] args) throws Exception {
		 
		try (ChatBotClient client = new ChatBotClient("localhost", 
						   10743)) {
			client.on();
		}
	}
}
