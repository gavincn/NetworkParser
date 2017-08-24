package de.uniks.networkparser.ext.petaf.proxy;

import de.uniks.networkparser.ext.petaf.NodeProxy;
import de.uniks.networkparser.ext.petaf.NodeProxyType;

//TODO add functionality
public class NodeProxyGoogleCloud extends NodeProxy{
//	private Connection connection;
	private String emailAccount;
	@Override
	public int compareTo(NodeProxy o) {
		return 0;
	}

	@Override
	public String getKey() {
		return emailAccount;
	}

	@Override
	public boolean close() {
		return true;
	}

	@Override
	protected boolean initProxy() {
		withType(NodeProxyType.OUT);
		return true;
	}

	@Override
	public boolean isSendable() {
		return emailAccount != null;
	}

	@Override
	public Object getSendableInstance(boolean prototyp) {
		return new NodeProxyGoogleCloud();
	}
}
