package de.uniks.networkparser.ext.petaf.messages;

import de.uniks.networkparser.ext.petaf.Message;

public class PingMessage extends Message {
	@Override
	public Object getSendableInstance(boolean prototyp) {
		return new PingMessage();
	}
}