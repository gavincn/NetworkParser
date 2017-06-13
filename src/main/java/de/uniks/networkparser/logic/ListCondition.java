package de.uniks.networkparser.logic;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import de.uniks.networkparser.buffer.CharacterBuffer;
import de.uniks.networkparser.interfaces.LocalisationInterface;
import de.uniks.networkparser.interfaces.ObjectCondition;
import de.uniks.networkparser.interfaces.ParserCondition;
import de.uniks.networkparser.interfaces.SendableEntityCreator;
import de.uniks.networkparser.list.ConditionSet;
import de.uniks.networkparser.list.SimpleSet;

public abstract class ListCondition implements ParserCondition, SendableEntityCreator {
	public static final String CHILD = "childs";
	protected Object list;
	protected boolean chain = true;

	@Override
	public boolean update(Object evt) {
		if(evt instanceof PropertyChangeEvent) {
			return updatePCE((PropertyChangeEvent) evt);
		} 
		Set<ObjectCondition> list = getList();
		boolean result=true;
		for(ObjectCondition item : list) {
			if(item.update(evt) == false) {
				result = false;
			}
		}
		return result;
	}

	public boolean updatePCE(PropertyChangeEvent evt) {
		if(list instanceof PropertyChangeListener) { 
			((PropertyChangeListener)list).propertyChange(evt);
			return true;
		} else if(list instanceof ObjectCondition) {
			return ((ObjectCondition)list).update(evt);
		}
		SimpleSet<?> collection = (SimpleSet<?>) this.list;
		
		for(Iterator<?> i = collection.iterator();i.hasNext();) {
			Object listener = i.next();
			if(listener instanceof ObjectCondition) {
				if(((ObjectCondition)listener).update(evt) == false) {
					if(chain) {
						return false;
					}
				}
			} else if(listener instanceof PropertyChangeListener) {
				((PropertyChangeListener)listener).propertyChange(evt);
			}
		}
		return true;
	}

	public ListCondition with(ObjectCondition... values) {
		add((Object[])values);
		return this;
	}

	public ListCondition with(PropertyChangeListener... values) {
		add((Object[])values);
		return this;
	}
	
	public boolean add(Object... values) {
		if(values == null || values.length < 1) {
			return false;
		}
		if(values.length == 1 && this.list == null) {
			// Dont do Chain in Chain
			if(values[0] instanceof ChainCondition == false) {
				if(values[0] instanceof PropertyChangeListener || values[0] instanceof ObjectCondition) {
					this.list = values[0];
				}
				return true;
			}
		}
		SimpleSet<?> list;
		if(this.list instanceof SimpleSet<?>) {
			list = (SimpleSet<?>) this.list;
		} else {
			if(values[0] instanceof PropertyChangeListener) {
				list = new SimpleSet<PropertyChangeListener>(); 
			} else {
				list = new ConditionSet();
			}
			list.with(this.list);
			this.list = list;
		}
		if(list instanceof ConditionSet) {
			for(Object condition : values) {
				if(condition instanceof ObjectCondition) {
					if(list.add((ObjectCondition)condition) == false) {
						return false;
					}
				}
			}
			return true;
		}
		return list.add(values);
	}
	
	public ConditionSet getList() {
		if(this.list instanceof ConditionSet) { 
			return (ConditionSet)this.list;
		}
		ConditionSet  result = new ConditionSet();
		result.with(this.list);
		return result;
	}
	
	public ObjectCondition first() {
		if(this.list instanceof ObjectCondition) {
			return (ObjectCondition) this.list;
		} else if(this.list instanceof SimpleSet<?>) { 
			Object first = ((SimpleSet<?>) this.list).first();
			if(first instanceof ObjectCondition) {
				return (ObjectCondition) first;
			}
		}
		return null;
	}
	
	public int size() {
		if(this.list==null) {
			return 0;
		}else if(this.list instanceof Collection<?>) {
			return ((Collection<?>)this.list).size();
		}
		return 1;
	}
	
	@Override
	public String toString() {
		Set<ObjectCondition> templates = getList();
		if(templates.size()>0) {
			CharacterBuffer buffer=new CharacterBuffer();
			for(ObjectCondition item : templates) {
				buffer.with(item.toString());
			}
			return buffer.toString();
		}
		return super.toString();
	}

	@Override
	public String[] getProperties() {
		return new String[] {CHILD};
	}

	@Override
	public Object getValue(Object entity, String attribute) {
		if(entity instanceof ChainCondition == false) {
			return false;
		}
		ChainCondition cc = (ChainCondition) entity;
		if (CHILD.equalsIgnoreCase(attribute)) {
			return cc.getList();
		}
		return null;
	}

	@Override
	public boolean setValue(Object entity, String attribute, Object value, String type) {
		if(entity instanceof ChainCondition == false) {
			return false;
		}
		ChainCondition cc = (ChainCondition) entity;
		if (CHILD.equalsIgnoreCase(attribute)) {
			cc.add(value);
			return true;
		}
		return false;
	}

	@Override
	public Object getValue(LocalisationInterface variables) {
		return getList().getAllValue(variables);
	}
}