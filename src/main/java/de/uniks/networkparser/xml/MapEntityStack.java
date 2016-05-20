package de.uniks.networkparser.xml;

/*
 NetworkParser
 Copyright (c) 2011 - 2015, Stefan Lindel
 All rights reserved.

 Licensed under the EUPL, Version 1.1 or (as soon they
 will be approved by the European Commission) subsequent
 versions of the EUPL (the "Licence");
 You may not use this work except in compliance with the Licence.
 You may obtain a copy of the Licence at:

 http://ec.europa.eu/idabc/eupl5

 Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" basis,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the Licence for the specific language governing
 permissions and limitations under the Licence.
*/
import de.uniks.networkparser.IdMap;
import de.uniks.networkparser.interfaces.SendableEntityCreator;
import de.uniks.networkparser.list.SimpleKeyValueList;
import de.uniks.networkparser.list.SimpleList;
import de.uniks.networkparser.list.SimpleSet;

public class MapEntityStack {
	/** The Stack. */
	private SimpleKeyValueList<Object, SendableEntityCreator> stack = new SimpleKeyValueList<Object, SendableEntityCreator>();

	private SimpleList<String> tags = new SimpleList<String>();

	private SimpleKeyValueList<String, SimpleSet<String>> childProperties= new SimpleKeyValueList<String, SimpleSet<String>>();

	/** Variable of AllowQuote. */
//	private boolean isAllowQuote;

	/**
	 * Remove The Last Element
	 */
	public void popStack() {
		this.stack.removePos(this.stack.size() - 1);
		this.tags.remove(this.tags.size() - 1);
	}

	/** @return The StackSize */
	public int getStackSize() {
		return this.stack.size();
	}

	/**
	 * Get the current Element
	 *
	 * @return The Stack Element - offset
	 */
	public Object getCurrentItem() {
		return this.stack.last();
	}

	/**
	 * Get the previous Element
	 * @return The Stack Element - offset
	 */
	public Object getPrevItem() {
		int pos = this.stack.size() - 2;
		if(pos < 0) {
			return null;
		}
		return this.stack.get(pos);
	}

	/**
	 * Add a new Reference Object to Stack.
	 * @param tag	The new Tag
	 * @param item 	new Reference Object
	 * @param creator The Creator for the Item
	 * @return XMLTokener Instance
	 */
	public MapEntityStack withStack(String tag, Object item, SendableEntityCreator creator) {
		stack.add(item, creator);
		tags.add(tag);
		String[] properties = creator.getProperties();
		for(String property : properties) {
			int lastPos = property.lastIndexOf(IdMap.ENTITYSPLITTER);
			if(lastPos >= 0) {
				String prop;
				if(lastPos == property.length() - 1) {
					// Value of XML Entity like uni.
					prop = ".";
				} else {
					prop = property.substring(lastPos + 1);
				}
				int pos = childProperties.indexOf(prop);
				if(pos>=0) {
					childProperties.getValueByIndex(pos).add(property);
				} else {
					SimpleSet<String> child = new SimpleSet<String>();
					child.add(property);
					childProperties.put(prop, child);
				}
			}
		}
		return this;
	}

	/**
	 * Get the Current Creator for the MapEntity
	 *
	 * @return The Stack Element - offset
	 */
	public SendableEntityCreator getCurrentCreator() {
		return this.stack.getValueByIndex(this.stack.size() - 1);
	}

	public void setValue(String key, String value) {
		SimpleSet<String> set = childProperties.get(key);
		if(set != null) {
			for(String ChildKey : set) {
				int pos = getEntityPos(ChildKey);
				if(pos >= 0 ) {
					Object entity = stack.getKeyByIndex(pos);
					SendableEntityCreator creator = stack.getValueByIndex(pos);
					creator.setValue(entity, ChildKey, value, IdMap.NEW);
				}
			}
		}
	}

	private int getEntityPos(String entity) {
		int start=entity.lastIndexOf(IdMap.ENTITYSPLITTER);
		int pos = this.tags.size() - 1;
		for(int end=start-1;end>=0;end --) {
			if(entity.charAt(end) ==IdMap.ENTITYSPLITTER) {
				String item = entity.substring(end+1, start);
				String tag = tags.get(pos);
				if(tag == null || tag.equals(item) == false) {
					return -1;
				}
				start = end;
				pos--;
			}
		}
		return pos;
	}

	public String getCurrentTag() {
		if(this.tags.size() >0 ){
			return this.tags.get(this.tags.size() - 1);
		}
		return null;
	}
}
