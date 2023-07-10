package message;

import java.util.GregorianCalendar;
import java.util.Objects;

public class ConversationMessage extends Message {

	private int senderId;
	private int receiverId;

	public ConversationMessage(int senderId, 
							   int receiverId, 
							   int id, 
							   GregorianCalendar dateAndTime, 
							   String text) {
		super(id, dateAndTime, text);
		this.senderId = senderId;
		this.receiverId = receiverId;
	}
	
	public int getSenderId() {
		return senderId;
	}
	
	public int getReceiverId() {
		return receiverId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(receiverId, senderId);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof ConversationMessage)) {
			return false;
		}
		ConversationMessage other = (ConversationMessage) obj;
		return receiverId == other.receiverId && senderId == other.senderId;
	}

	@Override
	public String toString() {
		return "ConversationMessage [senderId=" + senderId + 
								  ", receiverId=" + receiverId + 
								  ", toString()=" + super.toString() + "]";
	} 
	
}
