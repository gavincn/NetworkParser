package de.uniks.networkparser.ext.petaf.proxy;

/*
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
import java.sql.Connection;
import de.uniks.networkparser.ext.generic.ReflectionLoader;
import de.uniks.networkparser.ext.petaf.Message;
import de.uniks.networkparser.ext.petaf.NodeProxy;
import de.uniks.networkparser.ext.petaf.messages.ChangeMessage;
import de.uniks.networkparser.ext.sql.SQLStatement;
import de.uniks.networkparser.ext.sql.SQLStatementList;
import de.uniks.networkparser.ext.sql.SQLTokener;

public class NodeProxySQL extends NodeProxy{
	public static final String PROPERTY_DATABASE = "database";
	public static final String PROPERTY_DRIVER = "driver";

	private String database;
	private String driver;
	private SQLTokener tokener;

	public NodeProxySQL() {
		this.property.addAll(PROPERTY_DATABASE, PROPERTY_DRIVER);
		this.propertyInfo.addAll(PROPERTY_DATABASE, PROPERTY_DRIVER);
		this.propertyUpdate.addAll(PROPERTY_DATABASE, PROPERTY_DRIVER);
	}

	@Override
	public Object getValue(Object element, String attrName) {
		if(element instanceof NodeProxySQL) {
			NodeProxySQL nodeProxy = (NodeProxySQL) element;
			if (PROPERTY_DATABASE.equals(attrName)) {
				return nodeProxy.getDataBase();
			}
			if (PROPERTY_DRIVER.equals(attrName)) {
				return nodeProxy.getDriver();
			}
		}
		return super.getValue(element, attrName);
	}

	public NodeProxySQL withConnection(Connection con) {
		initTokener(con);
		return this;
	}

	public String getDataBase() {
		return this.database;
	}

	public String getDriver() {
		return driver;
	}

	public NodeProxySQL withDriver(String value) {
		this.driver = value;
		return this;
	}

	private NodeProxySQL withDatabase(String value) {
		this.database = value;
		return this;
	}

	@Override
	public boolean setValue(Object element, String attrName, Object value, String type) {
		if(element instanceof NodeProxyMessages) {
			NodeProxySQL nodeProxy = (NodeProxySQL) element;
			if (PROPERTY_DATABASE.equals(attrName)) {
				nodeProxy.withDatabase((String) value);
				return true;
			}
			if (PROPERTY_DRIVER.equals(attrName)) {
				nodeProxy.withDriver((String) value);
				return true;
			}
		}
		return super.setValue(element, attrName, value, type);
	}

	@Override
	public int compareTo(NodeProxy o) {
		return 0;
	}

	@Override
	public String getKey() {
		return database;
	}

	@Override
	public boolean close() {
		if(this.tokener != null) {
			return this.tokener.close();
		}
		return true;
	}

	@Override
	protected boolean initProxy() {
		withType(NodeProxy.TYPE_INOUT);
		return true;
	}

	@Override
	public boolean isSendable() {
		return driver != null;
	}

	@Override
	public Object getSendableInstance(boolean prototyp) {
		return new NodeProxySQL();
	}

	public void initTokener(Connection con) {
		if(this.tokener == null) {
			String[] split = driver.split(":");
			if(split.length>2) {
				this.tokener = new SQLTokener(SQLStatement.connect(split[0], split[1], split[2]));
				this.database = split[2];
			}
		}
		if(con != null) {
			this.tokener.withConnection(con);
		} else {
			if(this.tokener.getConnection() == null) {
				Connection connection = ReflectionLoader.loadSQLDriver(driver, this.database);
				this.tokener.withConnection(connection);
			}
		}
	}

	@Override
	protected boolean sending(Message msg) {
		if (super.sending(msg)) {
			return true;
		}
		if(driver == null) {
			return false;
		}
		initTokener(null);
		SQLStatementList statements = null;
		if(msg instanceof ChangeMessage) {
			ChangeMessage cm = (ChangeMessage) msg;
			Object entity = cm.getEntity();
			String property = cm.getProperty();
			Object newValue = cm.getNewValue();
			String id = cm.getId();
			statements = tokener.update(entity, id, property, newValue);
		}
		if(statements != null) {
			tokener.executeStatements(statements);
		}
		return super.sending(msg);
	}
}
