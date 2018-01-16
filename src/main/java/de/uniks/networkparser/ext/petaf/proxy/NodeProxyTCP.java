package de.uniks.networkparser.ext.petaf.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import de.uniks.networkparser.IdMap;
import de.uniks.networkparser.buffer.ByteBuffer;
import de.uniks.networkparser.ext.petaf.Message;
import de.uniks.networkparser.ext.petaf.NodeProxy;
import de.uniks.networkparser.ext.petaf.NodeProxyType;
import de.uniks.networkparser.ext.petaf.Server_TCP;
import de.uniks.networkparser.ext.petaf.messages.ConnectMessage;
import de.uniks.networkparser.interfaces.ObjectCondition;

public class NodeProxyTCP extends NodeProxy {
	public static final String PROPERTY_URL = "url";
	public static final String PROPERTY_PORT = "port";
	protected int port;
	protected String url;
	public static final String LOCALHOST = "127.0.0.1";
	protected Server_TCP serverSocket;
	protected boolean allowAnswer = false;

	/**
	 * Fallback Executor for Simple Using Serverclasses
	 */
	private ObjectCondition listener;

	public NodeProxyTCP() {
		this.property.addAll(PROPERTY_URL, PROPERTY_PORT);
		this.propertyUpdate.addAll(PROPERTY_URL, PROPERTY_PORT);
		this.propertyInfo.addAll(PROPERTY_URL, PROPERTY_PORT);
	}

	public String getUrl() {
		return url;
	}

	public NodeProxyTCP withUrl(String value) {
		String oldValue = value;
		this.url = value;
		firePropertyChange(PROPERTY_URL, oldValue, value);
		return this;
	}

	public NodeProxyTCP withURLPort(String url, int port) {
		withUrl(url);
		withPort(port);
		return this;
	}

	@Override
	public String getKey() {
		return url + ":" + port;
	}

	public Integer getPort() {
		return port;
	}

	public NodeProxyTCP withAllowAnswer(boolean value) {
		this.allowAnswer = value;
		return this;
	}

	public boolean isAllowAnswer() {
		return allowAnswer;
	}

	public NodeProxyTCP withPort(int value) {
		int oldValue = value;
		this.port = value;
		firePropertyChange(PROPERTY_PORT, oldValue, value);
		return this;
	}

	@Override
	public Object getValue(Object element, String attrName) {
		if(element instanceof NodeProxyTCP ) {
			NodeProxyTCP nodeProxy = (NodeProxyTCP) element;
			if (PROPERTY_URL.equals(attrName)) {
				return nodeProxy.getUrl();
			}
			if (PROPERTY_PORT.equals(attrName)) {
				return nodeProxy.getPort();
			}
		}
		return super.getValue(element, attrName);
	}

	@Override
	public boolean setValue(Object element, String attrName, Object value, String type) {
		if(element instanceof NodeProxyTCP) {
			NodeProxyTCP nodeProxy = (NodeProxyTCP) element;
			if (PROPERTY_URL.equals(attrName)) {
				nodeProxy.withUrl((String) value);
				return true;
			}
			if (PROPERTY_PORT.equals(attrName)) {
				nodeProxy.withPort((Integer) value);
				return true;
			}
		}
		return super.setValue(element, attrName, value, type);
	}

	public Message readFromInputStream(Socket socket) throws IOException {
		ByteBuffer buffer=new ByteBuffer();

		byte[] messageArray = new byte[BUFFER];
		InputStream is = socket.getInputStream();
		int bytesRead;
		while (-1 != (bytesRead = is.read(messageArray, 0, BUFFER))) {
			buffer.with(new String(messageArray, 0, bytesRead, Charset.forName("UTF-8")));
			if(bytesRead != BUFFER && allowAnswer) {
				break;
			}
		}

		Message msg=null;
		if(this.space != null) {
			IdMap map = this.space.getMap();
			Object element = map.decode(buffer);
			this.space.updateNetwork(NodeProxyType.IN, this);
			if(element instanceof Message) {
				msg = (Message) element;
				NodeProxy receiver = msg.getReceiver();
				if(element instanceof ConnectMessage) {
					receiver.updateReceive(buffer.size(), false);
				} else {
					receiver.updateReceive(buffer.size(), true);
				}
				
				// Let my Know about the new Receiver
				if(receiver != null) {
					this.space.with(receiver);
				}
			}
		}
		if(msg == null){
			msg=new Message();
		}
		msg.withMessage(buffer.flip(false));
		msg.withSession(socket);
		msg.withAddToReceived(this);
		if(this.listener != null) {
			this.listener.update(msg);
		}
		if(allowAnswer) {
			getExecutor().handleMsg(msg);
		}else {
			socket.close();
			getExecutor().handleMsg(msg);
		}
		return msg;
	}

	@Override
	protected boolean sending(Message msg) {
		if (super.sending(msg)) {
			return true;
		}
		boolean success = false;
		try {
			if (url != null && (msg.isSendAnyHow() || isOnline())) {
				InetAddress addr = InetAddress.getByName(url);
				Socket requestSocket = new Socket(addr, port);
				if (msg.getTimeOut() > Message.TIMEOUTDEFAULT) {
					requestSocket.setSoTimeout(msg.getTimeOut());
				}
				OutputStream os = requestSocket.getOutputStream();
				byte[] buffer;
				if(this.space != null) {
 					buffer = this.space.convertMessage(msg).getBytes();
				} else {
					buffer = msg.toString().getBytes();
				}
				int start = 0;
				int size = BUFFER;
				while (true) {
					int end = start + BUFFER;
					if (end > buffer.length) {
						size = buffer.length - start;
						os.write(buffer, start, size);
						break;
					} else {
						os.write(buffer, start, size);
					}
					start = end;
				}
				os.flush();
				if(allowAnswer) {
					readFromInputStream(requestSocket);
				}
				setSendTime(buffer.length);
				requestSocket.close();
				success = true;
			}
		} catch (IOException ioException) {
			// could not reach the proxy, mark it as offline
			this.withOnline(false);
			success = false;
		}
		return success;
	}

	public boolean start() {
		return initProxy();
	}

	@Override
	public boolean close() {
		if (this.serverSocket != null) {
			this.serverSocket.close();
			this.serverSocket = null;
		}
		return true;
	}

	@Override
	protected boolean initProxy() {
		boolean isInput = NodeProxyType.isInput(getType());
		if (url == null && getType() == null || isInput) {
			if(serverSocket != null) {
				return true;
			}
			// Incoming Proxy
			if(isInput == false) {
				withType(NodeProxyType.IN);
			}
			serverSocket = new Server_TCP(this);
			if (url == null) {
				try {
					String url = InetAddress.getLocalHost().getHostAddress();
					if (LOCALHOST.equals(url) == false) {
						this.url = url;
					}
				} catch (UnknownHostException e) {
				}
			}
		} else {
			withType(NodeProxyType.OUT);
			if (url == null) {
				try {
					url = InetAddress.getLocalHost().getHostAddress();
				} catch (UnknownHostException e) {
				}
				// NodeProxyTCP result = createProxy(url, port);
			}
		}
		return true;
	}

	@Override
	public boolean isSendable() {
		return url != null;
	}

	public static NodeProxyTCP create(String url, int port) {
		NodeProxyTCP proxy = new NodeProxyTCP().withURLPort(url, port);
		return proxy;
	}

	public static NodeProxyTCP createServer(int port) {
		NodeProxyTCP proxy = new NodeProxyTCP();
		proxy.withPort(port);
		proxy.withType(NodeProxyType.INOUT);
		return proxy;
	}

	@Override
	public NodeProxyTCP getSendableInstance(boolean reference) {
		return new NodeProxyTCP();
	}

	public NodeProxyTCP withListener(ObjectCondition condition) {
		this.listener = condition;
		this.allowAnswer = true;
		return this;
	}
}
