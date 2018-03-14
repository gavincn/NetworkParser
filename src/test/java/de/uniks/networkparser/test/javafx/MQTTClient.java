package de.uniks.networkparser.test.javafx;

import org.junit.Test;

import de.uniks.networkparser.SimpleEvent;
import de.uniks.networkparser.ext.io.Message;
import de.uniks.networkparser.ext.mqtt.MqttException;
import de.uniks.networkparser.ext.petaf.proxy.NodeProxyBroker;
import de.uniks.networkparser.ext.petaf.proxy.NodeProxyMQTT;
import de.uniks.networkparser.interfaces.SimpleEventCondition;

public class MQTTClient {
	public static void main(String[] args) throws MqttException {
		NodeProxyMQTT client = new NodeProxyMQTT("tcp://broker.hivemq.com:1883", NodeProxyMQTT.generateClientId());
		client.withCallback(new SimpleEventCondition() {
				@Override
				public boolean update(SimpleEvent event) {
					if(NodeProxyMQTT.EVENT_MESSAGE.equals(event.getType())) {
						System.out.println("TOPIC: "+event.getPropertyName());
						System.out.println("Message: "+event.getNewValue());
					}
					return true;
				}
			}
		);
		client.connect();
		client.subscribe("ALL");
		Message message = new Message("Hallo World 42".getBytes());
		client.publish("ALL", message);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		client.close();
	}
	
	
	@Test
	public void testMQTT() {
		NodeProxyBroker broker = new NodeProxyBroker("tcp://broker.hivemq.com:1883");
	}
}
