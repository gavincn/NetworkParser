package de.uniks.networkparser;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import de.uniks.networkparser.ext.LogItem;
import de.uniks.networkparser.interfaces.ObjectCondition;

/*
NetworkParser
The MIT License
Copyright (c) 2010-2016 Stefan Lindel https://github.com/fujaba/NetworkParser/

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
/**
 * A simple logging interface abstracting logging APIs. In order to be
 * instantiated successfully by Apache Common Logging, classes that implement
 * this interface must have a constructor that takes a single String parameter
 * representing the "name" of this Log.
 * <p>
 * The six logging levels used by <code>Log</code> are (in order):
 * <ol>
 * <li>trace (the least serious)</li>
 * <li>debug</li>
 * <li>info</li>
 * <li>warn</li>
 * <li>error</li>
 * <li>fatal (the most serious)</li>
 * </ol>
 * The mapping of these log levels to the concepts used by the underlying
 * logging system is implementation dependent. The implementation should ensure,
 * though, that this ordering behaves as expected.
 * <p>
 * Performance is often a logging concern. By examining the appropriate
 * property, a component can avoid expensive operations (producing information
 * to be logged).
 * <p>
 * For example, <code>
 *	if (log.isDebugEnabled()) {
 *		... do something expensive ...
 *		log.debug(theResult);
 *	}
 * </code>
 * <p>
 * Configuration of the underlying logging system will generally be done
 * external to the Logging APIs, through whatever mechanism is supported by that
 * system.
 *
 * @version $Id: Log.java 1432663 2013-01-13 17:24:18Z tn $
 * @author Stefan Lindel
 */
public class NetworkParserLog extends Handler {
	public static final String ERROR_TYP_PARSING = "PARSING";
	public static final String ERROR_TYP_CONCURRENTMODIFICATION = "CONCURRENTMODIFICATION";
	public static final String ERROR_TYP_NOCREATOR = "NOCREATORFOUND";
	public static final String ERROR_TYP_DUPPLICATE = "DUPPLICATE";
	public static final byte LOGLEVEL_TRACE = 1;
	public static final byte LOGLEVEL_DEBUG = 2;
	public static final byte LOGLEVEL_INFO = 4;
	public static final byte LOGLEVEL_WARNING = 8;
	public static final byte LOGLEVEL_ERROR = 16;
	public static final byte LOGLEVEL_FATAL = 32;
	public static final byte LOGLEVEL_ALL = 63;
	public static final String TRACE = "TRACE";
	public static final String DEBUG = "DEBUG";
	public static final String INFO = "INFO";
	public static final String WARNING = "WARNING";
	public static final String ERROR = "ERROR";
	public static final String FATAL = "FATAL";
	public static final String LOG = "LOG";

	private byte flag = LOGLEVEL_ERROR + LOGLEVEL_INFO; // ERROR + INFO
	private ObjectCondition condition;

	/**
	 * Log a message with debug log level.
	 *
	 * @param owner		The Element with call the Methods
	 * @param method	The Caller-Method
	 * @param message	log this message
	 * @return if method must Cancel
	 */
	public boolean debug(Object owner, String method, String message) {
		if(condition!= null) {
			return condition.update(new SimpleEvent(owner, method, null, message).withType(DEBUG));
		}
		return false;
	}

	public boolean print(Object owner, LogItem item) {
		if(condition!= null) {
			return condition.update(new SimpleEvent(owner, null, null, item).withType(LOG));
		}
		return false;
	}

	/**
	 * Log a message with info log level.
	 *
	 * @param owner		The Element with call the Methods
	 * @param method	The Caller-Method
	 * @param message	log this message
	 * @param params	advanced Information
	 * @return boolean if method must Cancel
	 */
	public boolean info(Object owner, String method, String message, Object... params) {
		if((flag & LOGLEVEL_INFO) != 0) {
			if(condition!= null) {
				Object values=params;
				if(params != null && params.length == 1) {
					values = params[0];
				}
				return condition.update(new SimpleEvent(owner, method, null, message).withModelValue(values).withType(INFO));
			}
		}
		return false;
	}

	public NetworkParserLog withFlag(byte flag) {
		this.flag = (byte) (this.flag | flag);
		return this;
	}

	public NetworkParserLog withoutFlag(byte flag) {
		this.flag = (byte) (this.flag | flag);
		this.flag -= flag;
		return this;
	}

	/**
	 * Log a message with warn log level.
	 *
	 * @param owner		The Element with call the Methods
	 * @param method	The Caller-Method
	 * @param message	log this message
	 * @param params	advanced Information
	 * @return boolean if method must Cancel
	 */
	public boolean warn(Object owner, String method, String message, Object... params) {
		if((flag & LOGLEVEL_WARNING) != 0) {
			if(condition!= null) {
				return condition.update(new SimpleEvent(owner, method, null, message).withModelValue(params).withType(WARNING));
			}
		}
		return false;
	}

	/**
	 * Log a message with error log level.
	 *
	 * @param owner		The Element with call the Methods
	 * @param method	The Caller-Method
	 * @param message		Typ of Log Value
	 * @param params	advanced Information
	 * @return boolean if method must Cancel
	 */
	public boolean error(Object owner, String method, String message,
			Object... params) {
		if((flag & LOGLEVEL_ERROR) != 0) {
			if(condition!= null) {
				return condition.update(new SimpleEvent(owner, method, null, message).withModelValue(params).withType(ERROR));
			}
		}
		return false;
	}

	public boolean log(Object owner, String method, String msg, int level, Object... params) {
		if(level == LOGLEVEL_ERROR) {
			return this.error(owner, method, msg, params);
		}
		if(level == LOGLEVEL_WARNING) {
			return this.warn(owner, method, msg, params);
		}
		return this.info(owner, method, msg, params);
	}

	public NetworkParserLog withListener(ObjectCondition condition) {
		this.condition = condition;
		return this;
	}

	@Override
	public void publish(LogRecord record) {
		 // <li>SEVERE (highest value)
		 // <li>WARNING
		// <li>INFO
		// <li>CONFIG
		// <li>FINE
		// <li>FINER
		// <li>FINEST  (lowest value)
		if(global != null && global != this) {
			global.publish(record);;
			return;
		}
		String level = record.getLevel().toString();
		if("SEVERE".equals(level)) {
			this.error(record.getSourceClassName(), record.getSourceMethodName(), record.getMessage());
		}
		if("WARNING".equals(level)) {
			this.warn(record.getSourceClassName(), record.getSourceMethodName(), record.getMessage());
		}
		this.info(record.getSourceClassName(), record.getSourceMethodName(), record.getMessage());
	}
	
	public void trace(Object owner, String method, String message, Object... params) {
		
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() throws SecurityException {
	}

	public static NetworkParserLog createLogger(byte flag, boolean removeConsoleHandler, ObjectCondition... conditions) {
		if(global != null) {
			return global;
		}
		// suppress the logging output to the console
		Logger rootLogger = Logger.getLogger("");
		NetworkParserLog logger = new NetworkParserLog().withFlag(flag);
		if(conditions != null && conditions.length>0) {
			logger.withListener(conditions[0]);
		}
		if(removeConsoleHandler) {
			Handler[] handlers = rootLogger.getHandlers();
			if(handlers != null && handlers.length>0 && handlers[0] instanceof ConsoleHandler)
			if (handlers[0] instanceof ConsoleHandler) {
				rootLogger.removeHandler(handlers[0]);
			}
		}
		rootLogger.addHandler(new NetworkParserLog());
		global = logger;
		return logger;
	}
	
	static NetworkParserLog global;

	public boolean isLevel(byte logLevel) {
		return (flag & logLevel) != 0;
	}
}
