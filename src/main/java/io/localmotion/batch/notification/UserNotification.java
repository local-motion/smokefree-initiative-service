package io.localmotion.batch.notification;

import io.localmotion.chatbox.ChatMessage;
import io.localmotion.personaldata.PersonalDataRecord;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UserNotification {
	private PersonalDataRecord personalDataRecord;
	private Map<String, List<ChatMessage>> missedConversions;
}

/*                                           QUEUE
                                     -------------------------
   Batch Job  ---- PUSH -----------> |N|N|N|N|N|N|N|N|N|N|N|      --- PULL ---------> Email Service Thread
							         -------------------------
*/
