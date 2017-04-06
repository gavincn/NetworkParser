package de.uniks.networkparser.logic;

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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Iterator;

import de.uniks.networkparser.interfaces.ObjectCondition;
import de.uniks.networkparser.list.SimpleList;

public class ChainCondition implements ObjectCondition{
	private boolean chain=true;
	private Object list;

	public ChainCondition enableHook() {
		this.chain = false;
		return this;
	}

	@Override
	public boolean update(Object evt) {
		if(evt instanceof PropertyChangeEvent) {
			return updatePCE((PropertyChangeEvent) evt);
		} 
		SimpleList<ObjectCondition> list = getTemplates();
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
		SimpleList<?> collection = (SimpleList<?>) this.list;
		
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

	public ChainCondition with(ObjectCondition... values) {
		add((Object[])values);
		return this;
	}

	public ChainCondition with(PropertyChangeListener... values) {
		add((Object[])values);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public boolean add(Object... values) {
		if(values == null) {
			return false;
		}
		if(values.length == 1 && this.list == null) {
			if(values[0] instanceof PropertyChangeListener || values[0] instanceof ObjectCondition) {
				this.list = values[0];
			}
			return true;
		}
		SimpleList<?> list;
		if(this.list instanceof SimpleList<?>) {
			list = (SimpleList<Object>) this.list;
		} else {
			if(values[0] instanceof PropertyChangeListener) {
				list = new SimpleList<PropertyChangeListener>(); 
			} else {
				list = new SimpleList<ObjectCondition>();
			}
			list.with(this.list);
			this.list = list;
		}
		return list.add(values);
	}
	
	@SuppressWarnings("unchecked")
	public SimpleList<ObjectCondition> getTemplates() {
		SimpleList<ObjectCondition> result = new SimpleList<ObjectCondition>();
		if(this.list instanceof SimpleList<?>) { 
			return ((SimpleList<ObjectCondition>)this.list);
		} 
		result.with(this.list);
		return result;
	}
	
	public int size() {
		if(this.list==null) {
			return 0;
		}else if(this.list instanceof Collection<?>) {
			return ((Collection<?>)this.list).size();
		}
		return 1;
	}
}