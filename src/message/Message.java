package message;

import java.util.GregorianCalendar;
import java.util.Objects;

public class Message {

	private int id;
	private GregorianCalendar dateAndTime;
	private String text;
	
	public Message(int id, 
				   GregorianCalendar dateAndTime, 
				   String text) {
		super();
		this.id = id;
		this.dateAndTime = (GregorianCalendar) dateAndTime.clone();
		this.text = text;
	}

	public int getId() {
		return id;
	}

	public GregorianCalendar getDateAndTime() {
		return (GregorianCalendar) dateAndTime.clone();
	}

	public String getText() {
		return text;
	}

	@Override
	public int hashCode() {
		return Objects.hash(dateAndTime, id, text);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Message)) {
			return false;
		}
		Message other = (Message) obj;
		return Objects.equals(dateAndTime, other.dateAndTime) && id == other.id && Objects.equals(text, other.text);
	}

	@Override
	public String toString() {
		return "Message [id=" + id + ", dateAndTime=" + dateAndTime + ", text=" + text + "]";
	}
	
}
