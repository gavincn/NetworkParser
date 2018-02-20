/*******************************************************************************
 * Copyright (c) 2009, 2016 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *    Dave Locke - initial API and implementation and/or initial documentation
 *    Ian Craggs - MQTT 3.1.1 support
 *    James Sutton - Automatic Reconnect & Offline Buffering
 */
package de.uniks.networkparser.ext.mqtt;

import de.uniks.networkparser.ext.petaf.proxy.NodeProxyMQTT;

/**
 * Thrown if an error occurs communicating with the server.
 * @author Paho Client
 */
public class MqttException extends Exception {
	private static final long serialVersionUID = 300L;

	/**
	 * Client encountered an exception.  Use the {@link #getCause()}
	 * method to get the underlying reason.
	 */
	public static final short REASON_CODE_DEFAULT 						= 0xffffffff;

	public static final short REASON_CODE_CLIENT_EXCEPTION              = 0x00;

	// CONNACK return codes
	/** The protocol version requested is not supported by the server. */
	public static final short REASON_CODE_INVALID_PROTOCOL_VERSION		= 0x01;
	/** The server has rejected the supplied client ID */
	public static final short REASON_CODE_INVALID_CLIENT_ID      		= 0x02;
	/** The broker was not available to handle the request. */
	public static final short REASON_CODE_BROKER_UNAVAILABLE             = 0x03;
	/** Authentication with the server has failed, due to a bad user name or password. */
	public static final short REASON_CODE_FAILED_AUTHENTICATION			= 0x04;
	/** Not authorized to perform the requested operation */
	public static final short REASON_CODE_NOT_AUTHORIZED				= 0x05;

	/** An unexpected error has occurred. */
	public static final short REASON_CODE_UNEXPECTED_ERROR				= 0x06;

	/** Error from subscribe - returned from the server. */
	public static final short REASON_CODE_SUBSCRIBE_FAILED				= 0x80;

	/**
	 * Client timed out while waiting for a response from the server.
	 * The server is no longer responding to keep-alive messages.
	 */
	public static final short REASON_CODE_CLIENT_TIMEOUT                = 32000;

	/**
	 * Internal error, caused by no new message IDs being available.
	 */
	public static final short REASON_CODE_NO_MESSAGE_IDS_AVAILABLE      = 32001;

	/**
	 * Client timed out while waiting to write messages to the server.
	 */
	public static final short REASON_CODE_WRITE_TIMEOUT                 = 32002;

	/**
	 * The client is already connected.
	 */
	public static final short REASON_CODE_CLIENT_CONNECTED              = 32100;

	/**
	 * The client is already disconnected.
	 */
	public static final short REASON_CODE_CLIENT_ALREADY_DISCONNECTED   = 32101;
	/**
	 * The client is currently disconnecting and cannot accept any new work.
	 * This can occur when waiting on a token, and then disconnecting the client.
	 * If the message delivery does not complete within the quiesce timeout
	 * period, then the waiting token will be notified with an exception.
	 */
	public static final short REASON_CODE_CLIENT_DISCONNECTING          = 32102;

	/** Unable to connect to server */
	public static final short REASON_CODE_SERVER_CONNECT_ERROR          = 32103;

	/**
	 * The client is not connected to the server.  The {@link NodeProxyMQTT#connect()} method must be called first.
	 */
	public static final short REASON_CODE_CLIENT_NOT_CONNECTED          = 32104;

	/**
	 * Server URI and supplied <code>SocketFactory</code> do not match.
	 * URIs beginning <code>tcp://</code> must use a <code>javax.net.SocketFactory</code>,
	 * and URIs beginning <code>ssl://</code> must use a <code>javax.net.ssl.SSLSocketFactory</code>.
	 */
	public static final short REASON_CODE_SOCKET_FACTORY_MISMATCH       = 32105;

	/**
	 * SSL configuration error.
	 */
	public static final short REASON_CODE_SSL_CONFIG_ERROR              = 32106;

	/**
	 * Thrown when an attempt to call {@link NodeProxyMQTT#close()} has been
	 * made.  These methods are invoked by the client's thread, and must not be used to control disconnection.
	 */
	public static final short REASON_CODE_CLIENT_DISCONNECT_PROHIBITED  = 32107;

	/**
	 * Protocol error: the message was not recognized as a valid MQTT packet.
	 * Possible reasons for this include connecting to a non-MQTT server, or
	 * connecting to an SSL server port when the client isn't using SSL.
	 */
	public static final short REASON_CODE_INVALID_MESSAGE				= 32108;

	/**
	 * The client has been unexpectedly disconnected from the server. The {@link #getCause() cause}
	 * will provide more details.
	 */
	public static final short REASON_CODE_CONNECTION_LOST               = 32109;

	/**
	 * A connect operation in already in progress, only one connect can happen at a time.
	 */
	public static final short REASON_CODE_CONNECT_IN_PROGRESS           = 32110;

	/**
	 * The client is closed - no operations are permitted on the client in this
	 * state.  New up a new client to continue.
	 */
	public static final short REASON_CODE_CLIENT_CLOSED		           = 32111;

	/**
	 * A request has been made to use a token that is already associated with
	 * another action.  If the action is complete the reset() can ve called on the
	 * token to allow it to be reused.
	 */
	public static final short REASON_CODE_TOKEN_INUSE		           = 32201;

	/**
	 * A request has been made to send a message but the maximum number of inflight
	 * messages has already been reached. Once one or more messages have been moved
	 * then new messages can be sent.
	 */
	public static final short REASON_CODE_MAX_INFLIGHT    			= 32202;

	/**
	 * The Client has attempted to publish a message whilst in the 'resting' / offline
	 * state with Disconnected Publishing enabled, however the buffer is full and
	 * deleteOldestMessages is disabled, therefore no more messages can be published
	 * until the client reconnects, or the application deletes buffered message
	 * manually.
	 */
	public static final short REASON_CODE_DISCONNECTED_BUFFER_FULL	= 32203;

	private int reasonCode;
	private Throwable cause;

	/**
	 * Set new Reason
	 *
	 * @param reasonCode the reason code for the exception.
	 * @param exception add Exception
	 * @return return new Exception
	 */
	public static MqttException withReason(short reasonCode, Object... exception) {
		MqttException mqttException = new MqttException();
		if(reasonCode != MqttException.REASON_CODE_DEFAULT) {
			mqttException.reasonCode = reasonCode;
		}else {
			mqttException.reasonCode = REASON_CODE_CLIENT_EXCEPTION;
		}
		if(exception != null && exception.length>0 && exception[0] instanceof Throwable) {
			mqttException.cause = (Throwable) exception[0];
		}
		return mqttException;
	}

	/**
	 * Returns the reason code for this exception.
	 * @return the code representing the reason for this exception.
	 */
	public int getReasonCode() {
		return reasonCode;
	}

	/**
	 * Returns the underlying cause of this exception, if available.
	 * @return the Throwable that was the root cause of this exception,
	 * which may be <code>null</code>.
	 */
	public Throwable getCause() {
		return cause;
	}

	/**
	 * Returns a <code>String</code> representation of this exception.
	 * @return a <code>String</code> representation of this exception.
	 */
	public String toString() {
		String result = getMessage() + " (" + reasonCode + ")";
		if (cause != null) {
			result = result + " - " + cause.toString();
		}
		return result;
	}
}
