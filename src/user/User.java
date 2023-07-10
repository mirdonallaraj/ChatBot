package user;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import message.ConversationMessage;
import message.Message;

/**
 * Represents a user in the chat system.
 */
public class User {
	
	private int id;
	private String username;
	private String password;

	private List<ConversationMessage> chatHistory;
	
	  /**
     * Constructs a new User object with the specified username and password.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     */
	public User(String username, String password) {
		this.username = username;
		this.password = password;
		chatHistory = new ArrayList<>();
	}
	
	  /**
     * Retrieves the chat history of the user.
     *
     * @return The chat history of the user.
     */
	public List<ConversationMessage> getChatHistory() {
		return chatHistory;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getUsername() {
		return username;
	}
	
	private static class AES {

		private static final String ALGORITHM = "AES";
		private static final byte[] keyValue = "1234567891234567".getBytes();

		private static Key generateKey() throws Exception {
			Key key = new SecretKeySpec(keyValue, ALGORITHM);
			return key;
		}

		/**
		 * Encrypts the given value using the provided key.
		 *
		 * @param valueToEnc The value to encrypt.
		 * @param key        The encryption key.
		 * @return The encrypted value.
		 * @throws Exception if an error occurs during encryption.
		 */
		public static String encrypt(String valueToEncrypt, Key key) throws Exception {

			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, key);

			byte[] encValue = cipher.doFinal(valueToEncrypt.getBytes());
			byte[] encryptedByteValue = new Base64().encode(encValue);

	        return new String(encryptedByteValue);
	    }
		
		/**
		 * Decrypts the given encrypted value using the provided key.
		 *
		 * @param encryptedValue The encrypted value to decrypt.
		 * @param key            The encryption key.
		 * @return The decrypted value.
		 * @throws Exception if an error occurs during decryption.
		 */
	    public static String decrypt(String encryptedValue, Key key) throws Exception {
	         Cipher cipher = Cipher.getInstance(ALGORITHM);
	         cipher.init(Cipher.DECRYPT_MODE, key);
	          
	         byte[] decodedBytes = new Base64().decode(encryptedValue.getBytes());
	  
	         byte[] enctVal = cipher.doFinal(decodedBytes);
	         
	         return new String(enctVal);
	     }
	}
	
	/**
	 * Retrieves the encrypted password.
	 *
	 * @return The encrypted password.
	 */
	public String getEncryptedPassword() {
		return encryptPsw(password);
	}

	/**
	 * Encrypts the provided password.
	 *
	 * @param password The password to encrypt.
	 * @return The encrypted password.
	 */
	public static String encryptPsw(String password) {
		try {
			Key key = AES.generateKey();
			return AES.encrypt(password, key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Decrypts the provided password.
	 *
	 * @param password The password to decrypt.
	 * @return The decrypted password.
	 */
	public static String decryptPsw(String password) {
		try {
			Key key = AES.generateKey();
			return AES.decrypt(password, key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Adds a new message to the chat history with the specified receiver ID.
	 *
	 * @param receiverId The ID of the message receiver.
	 * @param message    The message to add.
	 */
	public void addNewMessage(int receiverId, Message message) {
		if (message == null) {
			return;
		}
		chatHistory.add(new ConversationMessage(this.id, receiverId, message.getId(), message.getDateAndTime(), message.getText()));
	}

	@Override
	public int hashCode() {
		return Objects.hash(chatHistory, id, password, username);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		return Objects.equals(chatHistory, other.chatHistory) && id == other.id
				&& Objects.equals(password, other.password) && Objects.equals(username, other.username);
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", username=" + username + ", chatHistory=" + chatHistory + "]";
	}
	
}
