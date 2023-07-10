package asnwer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Class to store and retrieve answers to specific questions.
 */
public class Answers{

    private Map<String, String> answers;  // A map to store the ready answers

    public Answers() {
        answers = new HashMap<>();
    }

    public void addAnswer(String question, String answer) {
        answers.put(question, answer);
    }

    public Set<String> getAvailableQuestions() {
        return answers.keySet();
    }

    public String getAnswer(String question) {
        return answers.get(question);
    }
    
    public Map<String, String> getAnswers() {
		return answers;
	}

	@Override
	public int hashCode() {
		return Objects.hash(answers);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Answers other = (Answers) obj;
		return Objects.equals(answers, other.answers);
	}
    
}
