package de.uniks.networkparser.graph;

import de.uniks.networkparser.list.SimpleSet;

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

public abstract class GraphMember {
	protected String name;
	protected Object children;
	protected GraphMember parentNode;
	
	String getFullId() {
		return name;
	}
	// PACKAGE VISIBILITY
	GraphSimpleSet getChildren() {
		if(this.children instanceof GraphSimpleSet) {
			return (GraphSimpleSet)this.children;
		}
		GraphSimpleSet collection = new GraphSimpleSet();
		if(this.children == null) {
			return collection;
		}
		if(this.children instanceof GraphMember) {
			collection.withAll(this.children);
		}
		return collection;
	}
	
	SimpleSet<GraphEntity> getNodes() {
		SimpleSet<GraphEntity> collection = new SimpleSet<GraphEntity>();
		if(this.children == null) {
			return collection;
		}
		if(this.children instanceof GraphEntity) {
			collection.add((GraphEntity)this.children);
			return collection;
		}
		if(this.children instanceof GraphSimpleSet) {
			GraphSimpleSet list = (GraphSimpleSet) this.children;
			for(GraphMember item : list) {
				if(item instanceof GraphEntity) {
					collection.add((GraphEntity)item);	
				}
			}
		}
		return collection;
	}
	
	/** Set the name of Element
	 * @param name The Name of Element
	 * @return The Instance	
	 */
	public GraphMember with(String name) {
		setName(name);
		return this;
	}

	boolean setName(String value) {
		if(value != this.name) {
			this.name = value;
			return true;
		}
		return false;
	}


	boolean setParent(GraphMember value) {
		if (this.parentNode != value) {
			GraphMember oldValue = this.parentNode;
			if (this.parentNode != null) {
				this.parentNode = null;
				oldValue.without(this);
			}
			this.parentNode = value;
			if (value != null) {
				value.withChildren(true, this);
			}
			return true;
		}
		return false;
	}
	
	protected GraphMember withChildren(boolean back, GraphMember... values) {
		// Do Nothing
		if (values == null || (values.length == 1 && (this.children == values[0]))) {
			return this;
		}
		if(this.children == null) {
			if(values.length==1){
				this.children = values[0];
				if(back) {
					((GraphMember)values[0]).setParent(this);
				}
				return this;
			}
		}
		GraphSimpleSet list;
		if( this.children instanceof GraphSimpleSet) {
			list = (GraphSimpleSet) this.children;
		}else {
			list = new GraphSimpleSet();
			list.with((GraphMember) this.children);
			this.children = list;
		}
		for (GraphMember value : values) {
			if(value != null ) {
				if(list.add(value)) {
					if(back) {
						value.setParent(this);
					}
				}
			}
		}
		return this;
	}

	protected GraphMember without(GraphMember... values) {
		if (values == null || this.children == null) {
			return this;
		}
		if(this.children instanceof GraphMember) {
			for (GraphMember value : values) {
				if(this.children == value) {
					this.children = null;
					value.setParent(null);
				}
			}
			return this;
		}
		GraphSimpleSet collection = (GraphSimpleSet) this.children;
		for (GraphMember value : values) {
			if(value != null) {
				collection.remove(value);
				value.setParent(null);
			}
		}
		return this;
	}
	
	GraphDiff getDiff() {
		if(this.children == null) {
			return null;
		}
		for(GraphMember item : getChildren()) {
			if(item instanceof GraphDiff) {
				return (GraphDiff) item;
			}
		}
		return null;
	}
	
	public String getName() {
		return this.name;
	}
	
	protected GraphMember withAnnotaion(Annotation value) {
		// Remove Old GraphAnnotation
		if(this.children != null) {
			if(this.children instanceof GraphMember) {
				if(this.children instanceof Annotation) {
					((Annotation)this.children).setParent(null);
					this.children = null;
				}
			}
			if(this.children instanceof GraphSimpleSet) {
				GraphSimpleSet collection = (GraphSimpleSet) this.children;
				for(int i=collection.size();i>=0;i--) {
					if(collection.get(i) instanceof Annotation) {
						GraphMember oldValue = collection.remove(i);
						oldValue.setParent(null);
					}
				}
			}
		}
		withChildren(true, value);
		return this;
	}
	
	protected Annotation getAnnotation() {
		if(this.children == null) {
			return null;
		}
		if (this.children instanceof Annotation) {
			return (Annotation)this.children;
		} else if(this.children instanceof GraphSimpleSet) {
			GraphSimpleSet collection = (GraphSimpleSet) this.children;
			for(GraphMember item : collection) {
				if(item instanceof Annotation) {
					return (Annotation) item;
				}
			}
		}
		return null;
	}
	
	public Modifier getModifiers() {
		if(this.children == null) {
			return null;
		}
		if (this.children instanceof Modifier) {
			return (Modifier)this.children;
		} else if(this.children instanceof GraphSimpleSet) {
			GraphSimpleSet collection = (GraphSimpleSet) this.children;
			for(GraphMember item : collection) {
				if(item instanceof Modifier) {
					return (Modifier) item;
				}
			}
		}
		return null;
	}
	GraphMember withModifier(Modifier... values) {
		if(values == null) {
			return this;
		}
		Modifier rootModifier = getModifiers();
		for (Modifier item : values) {
			if (item.has(Modifier.PUBLIC) || item.has(Modifier.PACKAGE) || item.has(Modifier.PROTECTED)
					|| item.has(Modifier.PRIVATE)) {
				rootModifier.with(item.getName());
				continue;
			}
			rootModifier.withChildren(true, item);
		}
		return this;
	}
}
