package cn.edu.uestc.cbir;

import java.io.Serializable;

public class Message implements Serializable {
	private static final long serialVersionUID = -7506722296355586543L;

	public MessageType messageType;
	public Object messageContent;


	public Message(MessageType messageType, Object messageContent) {
		this.messageContent = messageContent;
		this.messageType = messageType;
	}
}
