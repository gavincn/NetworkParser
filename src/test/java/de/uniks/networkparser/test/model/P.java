package de.uniks.networkparser.test.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import de.uniks.networkparser.interfaces.SendableEntity;

public class P implements SendableEntity {
	private String PROPERTY_MSG = "msg";

   public void setPROPERTY_MSG(String value) {
	  this.PROPERTY_MSG = value;
   }

   public String getPROPERTY_MSG() {
	  return this.PROPERTY_MSG;
   }

	public static final String PROPERTY_ID="number";
	public static final String PROPERTY_CHILD="child";
	private int number;
	private P child;

	public int getNumber() {
		return number;
	}

	public void setId(int id) {
		int oldValue=this.number;
		   this.number = id;
		   if(id!=oldValue){
			   firePropertyChange(SortedMsg.PROPERTY_ID, oldValue, id);
		   }
	   }

	public P getChild() {
		return child;
	}

	public void setChild(P child) {
		P oldValue=this.child;
		   this.child = child;
		   if(child!=oldValue){
			   firePropertyChange(SortedMsg.PROPERTY_CHILD, oldValue, child);
		   }
	   }

	public boolean set(String attribute, Object value) {
		if (attribute.equalsIgnoreCase(PROPERTY_ID)) {
			   setId((Integer) value);
			   return true;
		   } else if (attribute.equalsIgnoreCase(PROPERTY_CHILD)) {
			   setChild((P) value);
			   return true;
		   }
		   return false;
	   }

	public Object get(String attrName) {
		String attribute;
		   int pos = attrName.indexOf(".");
		   if (pos > 0) {
			   attribute = attrName.substring(0, pos);
		   } else {
			   attribute = attrName;
		   }
		   if (attribute.equalsIgnoreCase(PROPERTY_ID)) {
			   return getNumber();
		   } else if (attribute.equalsIgnoreCase(PROPERTY_CHILD)) {
			   return getChild();
		   }
		   return null;
	   }

	protected PropertyChangeSupport listeners = null;
	
	public boolean firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		if (listeners != null) {
			listeners.firePropertyChange(propertyName, oldValue, newValue);
			return true;
		}
		return false;
	}

	public boolean addPropertyChangeListener(PropertyChangeListener listener) {
		if (listeners == null) {
			listeners = new PropertyChangeSupport(this);
		}
		listeners.addPropertyChangeListener(listener);
		return true;
	}

	public boolean addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		if (listeners == null) {
			listeners = new PropertyChangeSupport(this);
		}
		listeners.addPropertyChangeListener(propertyName, listener);
		return true;
	}

	public boolean removePropertyChangeListener(PropertyChangeListener listener) {
		if (listeners != null) {
			listeners.removePropertyChangeListener(listener);
		}
		return true;
	}

	public boolean removePropertyChangeListener(String property,
			PropertyChangeListener listener) {
		if (listeners != null) {
			listeners.removePropertyChangeListener(property, listener);
		}
		return true;
	}
}
