package de.uniks.networkparser.graph;

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
import java.util.Collection;
import de.uniks.networkparser.Filter;
import de.uniks.networkparser.IdMap;
import de.uniks.networkparser.interfaces.BaseItem;
import de.uniks.networkparser.interfaces.SendableEntityCreator;
/**
 * The Class YUMLIdParser.
 */

public class GraphIdMap extends IdMap {
	/** The Constant for CLASS Diagramms. */
	public static final String CLASS = "classdiagram";

	/** The Constant for OBJECT Diagramms. */
	public static final String OBJECT = "objectdiagram";

	private GraphIdMapFilter filter = new GraphIdMapFilter()
			.withShowCardinality(true).withTyp(CLASS);

	/**
	 * Parses the object.
	 *
	 * @param object
	 *            the object
	 * @return the string
	 */
	public String parseObject(Object object) {
		return parse(object,
				filter.clone(new GraphIdMapFilter()).withTyp(OBJECT));
	}
	
	/**
	 * Parses the object.
	 *
	 * @param object
	 *            the object
	 * @return the string
	 */
	public GraphList parsingObject(Object object) {
		return parsing(object,
				filter.clone(new GraphIdMapFilter()).withTyp(OBJECT));
	}

	/**
	 * Parses the class.
	 *
	 * @param object
	 *            the object
	 * @return the string
	 */
	public String parseClass(Object object) {
		return parse(object, filter.clone(new GraphIdMapFilter())
				.withTyp(CLASS));
	}

	public String parse(Object object, GraphIdMapFilter filter) {
		return parsing(object, filter).toString();
	}
	
	public GraphList parsing(Object object, GraphIdMapFilter filter) {
		GraphList list = this.createList().withTyp(filter.getTyp());
		GraphClazz main = parse(object, filter, list, 0);
		if(list instanceof GraphListDiff) {
			((GraphListDiff)list).withMain(main);	
		}
		
		return list;
	}

	/**
	 * Parses the.
	 *
	 * @param object
	 *            the object to Serialisation
	 * @param typ
	 *            Is it a OBJECT OR A CLASS diagram
	 * @param filter
	 *            Filter for Serialisation
	 * @param showCardinality
	 *            the show cardinality
	 * @return the Object as String
	 */
	private GraphClazz parse(Object object, GraphIdMapFilter filter,
			GraphList list, int deep) {
		if (object == null) {
			return null;
		}

		String mainKey = getId(object);
		GraphMember element = list.getByObject(mainKey, true);
		if (element != null && element instanceof GraphClazz) {
			return (GraphClazz)element;
		}

		SendableEntityCreator prototyp = getCreatorClass(object);
		String className = object.getClass().getName();
		className = className.substring(className.lastIndexOf('.') + 1);

		GraphClazz newElement = this.createClazz();
		newElement.withId(mainKey);
		newElement.with(className);
		list.with(newElement);
		if (prototyp != null) {
			for (String property : prototyp.getProperties()) {
				Object value = prototyp.getValue(object, property);
				if (value == null) {
					continue;
				}
				if (value instanceof Collection<?>) {
					for (Object containee : ((Collection<?>) value)) {
						parsePropertyValue(object, filter, list, deep, newElement,
								property, containee, GraphCardinality.MANY);
					}
				} else {
					parsePropertyValue(object, filter, list, deep, newElement,
							property, value, GraphCardinality.ONE);
				}
			}
		}
		return newElement;
	}

	private void parsePropertyValue(Object entity, GraphIdMapFilter filter,
			GraphList list, int deep, GraphClazz element, String property,
			Object item, GraphCardinality cardinality) {
		if (item == null) {
			return;
		}
		if (!filter.isPropertyRegard(entity, property, item, deep + 1)) {
			return;
		}
		if (!filter.isConvertable(entity, property, item, deep + 1)) {
			return;
		}
		SendableEntityCreator valueCreater = getCreatorClass(item);
		if (valueCreater != null) {
			GraphClazz subId = parse(item, filter, list, deep + 1);
			list.add(this.createEdge().with(element).with(this.createEdge(subId, cardinality, property)));
		} else {
			GraphAttribute attribute = element.createAttribute(property, GraphDataType.ref(item.getClass()));
			attribute.withValue("" + item);
		}
		return;
	}

	@Override
	public BaseItem encode(Object value) {
		GraphList list = new GraphList();
		parse(value, this.filter.clone(new GraphIdMapFilter()), list, 0);
		return list;
	}

	@Override
	public BaseItem encode(Object value, Filter filter) {
		GraphList list = new GraphList();
		if (filter instanceof GraphIdMapFilter) {
			GraphIdMapFilter yumlFilter = (GraphIdMapFilter) filter;
			list.withTyp(yumlFilter.getTyp());
			parse(value, yumlFilter, list, 0);
		}
		return list;
	}

	/**
	 * Gets the class name.
	 *
	 * @param object
	 *            the object
	 * @return the class name
	 */
	public String getClassName(Object object) {
		if (object instanceof String) {
			object = getObject((String) object);
		}
		String className = object.getClass().getName();
		return className.substring(className.lastIndexOf('.') + 1);
	}

	public GraphList createList() {
		return new GraphList();
	}

	GraphEdge createEdge() {
		return new GraphEdge();
	}
	
	GraphEdge createEdge(GraphClazz node, GraphCardinality cardinality, String property) {
		return new GraphEdge().with(node, cardinality, property);
	}
	
	public GraphClazz createClazz() {
		return new GraphClazz();
	}
	public GraphAttribute createAttribute() {
		return new GraphAttribute();
	}
}
