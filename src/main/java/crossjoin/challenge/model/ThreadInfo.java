package crossjoin.challenge.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThreadInfo {

	private String timestampDay;
	private String timestampHour;
	private String timestampMinute;
	private String timestampSecond;
	private String threadType;
	private String threadName;
	private String threadState;
	private String lastCall;
	private String lastCustomCall;
	private boolean contention;

	private String threadId;
	private String waitingOn;
	
}
