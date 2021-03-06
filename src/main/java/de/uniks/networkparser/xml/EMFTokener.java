package de.uniks.networkparser.xml;

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
import java.util.ArrayList;
import java.util.Collection;

import de.uniks.networkparser.EntityUtil;
import de.uniks.networkparser.IdMap;
import de.uniks.networkparser.MapEntity;
import de.uniks.networkparser.Tokener;
import de.uniks.networkparser.buffer.CharacterBuffer;
import de.uniks.networkparser.graph.Association;
import de.uniks.networkparser.graph.AssociationTypes;
import de.uniks.networkparser.graph.Attribute;
import de.uniks.networkparser.graph.Cardinality;
import de.uniks.networkparser.graph.Clazz;
import de.uniks.networkparser.graph.DataType;
import de.uniks.networkparser.graph.GraphList;
import de.uniks.networkparser.graph.GraphModel;
import de.uniks.networkparser.graph.GraphUtil;
import de.uniks.networkparser.graph.Literal;
import de.uniks.networkparser.graph.Method;
import de.uniks.networkparser.graph.Modifier;
import de.uniks.networkparser.graph.Parameter;
import de.uniks.networkparser.graph.Value;
import de.uniks.networkparser.graph.util.AssociationSet;
import de.uniks.networkparser.graph.util.AttributeSet;
import de.uniks.networkparser.graph.util.ClazzSet;
import de.uniks.networkparser.graph.util.MethodSet;
import de.uniks.networkparser.graph.util.ParameterSet;
import de.uniks.networkparser.interfaces.BaseItem;
import de.uniks.networkparser.interfaces.Entity;
import de.uniks.networkparser.interfaces.EntityList;
import de.uniks.networkparser.interfaces.SendableEntityCreator;
import de.uniks.networkparser.interfaces.SendableEntityCreatorIndexId;
import de.uniks.networkparser.list.SimpleKeyValueList;
import de.uniks.networkparser.list.SimpleList;
import de.uniks.networkparser.list.SimpleSet;

public class EMFTokener extends Tokener{
	public static final String ECORE = "ecore";
	public static final String EPACKAGE = "ecore:EPackage";
	public static final String EAttribute = "eAttributes";
	public static final String ECLASS = "eClassifiers";
	public static final String EANNOTATIONS = "eAnnotations";
	public static final String EREFERENCE = "eReferences";
	public static final String ETYPE = "eType";
	public static final String EDATATYPE ="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//";
	public static final String TYPE_ECLASS = "ecore:EClass";
	public static final String TYPE_EAttribute = "ecore:EAttribute";
	public static final String TYPE_EReferences = "ecore:EReference";
	public static final String TYPE_ESUPERTYPE = "eSuperTypes";
	public static final String TYPE_EEnum = "ecore:EEnum";
	public static final String EOpposite = "eOpposite";
	public static final String ATTRIBUTE_URL = "http://www.eclipse.org/emf/2002/Ecore#//";
	public static final String UPPERBOUND = "upperBound";
	public static final String XMI_TYPE = "xmi:type";
	public static final String XSI_TYPE = "xsi:type";
	public static final String XMI_ID = "xmi:id";
	public static final String NAME = "name";
	public static final String VALUE ="value";

	/**
	 * Skip the Current Entity to &gt;.
	 */
	protected void skipEntity() {
		skipTo('>', false);
		// Skip >
		nextClean(false);
	}

	public String skipHeader() {
		boolean skip=false;
		CharacterBuffer tag;
		do {
			tag = this.getString(2);
			if(tag == null) {
				break;
			}
			if(tag.equals("<?")) {
				skipEntity();
				skip = true;
			} else if(tag.equals("<!")) {
				skipEntity();
				skip = true;
			} else {
				skip = false;
			}
		}while(skip);
		if(tag != null) {
			String item = tag.toString();
			this.buffer.withLookAHead(item);
			return item;
		}
		return "";
	}

	public XMLEntity encode(Object entity, MapEntity map) {
		if(entity == null || map == null) {
			return null;
		}
		if(entity instanceof GraphList) {
			return  encodeClassModel((GraphList)entity, map);
		}
		XMLEntity result = new XMLEntity();

		String typetag = entity.getClass().getName().replaceAll("\\.", ":");
		result.setType(typetag);

		encodeChildren(entity, result, map);

		return result;
	}

	public XMLEntity encodeClassModel(GraphList entity, MapEntity map) {
		XMLContainer container = new XMLContainer();
		container.withStandardPrefix();
		if(entity == null || map == null) {
			return container;
		}

		XMLEntity root = container.createChild(EPACKAGE);
		root.withKeyValue("xmi:version", "2.0");
		root.withKeyValue("xmlns:xmi", "http://www.omg.org/XMI");
		root.withKeyValue("xmlns:ecore", "http://www.eclipse.org/emf/2002/Ecore");
		String id, name = "model";
		if(entity.getName()!= null) {
			id = EntityUtil.shortClassName(entity.getName());
			name = entity.getName();
		} else {
			id = name;
		}
		root.withKeyValue(NAME, id);
		root.withKeyValue("nsURI", "http:///"+name.replace(".", "/")+".ecore");
		root.withKeyValue("nsPrefix", name);

		for(Clazz child : entity.getClazzes()) {
			XMLEntity ecoreClass = root.createChild(ECLASS);
			ecoreClass.withKeyValue(XSI_TYPE, TYPE_ECLASS);
			ecoreClass.withKeyValue(NAME, child.getName());
			for(Attribute attribute : child.getAttributes()) {
				DataType type = attribute.getType();
				if(EntityUtil.isPrimitiveType(type.getName(false))) {
					XMLEntity ecoreAttribute = ecoreClass.createChild(EAttribute);
					ecoreAttribute.withKeyValue(NAME, attribute.getName());
					ecoreAttribute.withKeyValue(ETYPE, EDATATYPE + "E" + EntityUtil.upFirstChar(type.getName(true)));
				}
			}

			for(Association assoc : child.getAssociations()) {
				XMLEntity ecoreAssociation = ecoreClass.createChild(EREFERENCE);
				ecoreAssociation.withKeyValue(NAME, assoc.getOther().getName());
				ecoreAssociation.withKeyValue(ETYPE, "#//"+assoc.getOtherClazz().getName());
				ecoreAssociation.withKeyValue(EOpposite, "#//"+assoc.getOtherClazz().getName()+"/"+assoc.getName());
				if(Cardinality.MANY.equals(assoc.getCardinality())) {
					ecoreAssociation.withKeyValue(UPPERBOUND, "-1");
				} else {
					ecoreAssociation.withKeyValue(UPPERBOUND, "1");
				}
			}
		}
		return container;
	}

	private void encodeChildren(Object entity, XMLEntity parent, MapEntity map) {
		SendableEntityCreator creatorClass = getCreatorClass(entity);
		if(creatorClass == null) {
			return;
		}

		for (String propertyName : creatorClass.getProperties()) {
			Object propertyValue = creatorClass.getValue(entity, propertyName);

			if (EntityUtil.isPrimitiveType(EntityUtil.shortClassName(propertyValue.getClass().getName()))) {
				parent.put(propertyName, propertyValue);
			} else if (propertyValue instanceof Collection<?>) {
				for (Object childValue : (Collection<?>) propertyValue) {
					XMLEntity child = new XMLEntity();

					parent.withChild(child);

					child.setType(propertyName);

					String typetag = childValue.getClass().getName().replaceAll("\\.", ":");

					child.put(XSI_TYPE, typetag);

					encodeChildren(childValue, child, map);
				}
			} else {
				XMLEntity child = new XMLEntity();

				parent.withChild(child);

				child.setType(propertyName);

				String typetag = propertyValue.getClass().getName().replaceAll("\\.", ":");

				child.put(XSI_TYPE, typetag);

				encodeChildren(propertyValue, child, map);
			}
		}
	}

	/**
	 * Decode a Element from EMF
	 *
	 * @param map decoding runtime values
	 * @param root The Root Element of Returnvalue
	 * @return decoded Object
	 */
	public Object decode(MapEntity map, Object root) {
		skipHeader();
		XMLEntity xmlEntity = new XMLEntity();
		xmlEntity.withValue(this.buffer);
		if(EPACKAGE.equals(xmlEntity.getTag())) {
			if(root instanceof GraphList) {
				return decoding(xmlEntity, (GraphList)root);
			}
			return decoding(xmlEntity, null);
		}
		// build root entity
		String tag = xmlEntity.getTag();
		if(tag == null) {
			return null;
		}
		String[] splitTag = tag.split("\\:");
		if(splitTag.length<2) {
			return null;
		}
		if(ECORE.equalsIgnoreCase(splitTag[0]) || root instanceof GraphModel) {
			GraphModel model;
			if(root == null || root instanceof GraphModel == false) {
				model = new GraphList();
			} else {
				model = (GraphModel) root;
			}
			return decodingClassModel(xmlEntity, model);
		}
		Object rootObject = null;
		SendableEntityCreator rootFactory;
		if(root == null) {
			String className = splitTag[1];
			rootFactory = getCreator(className, false, null);
			if (rootFactory != null) {
				rootObject = rootFactory.getSendableInstance(false);
			} else {
				// just use an ArrayList
				rootObject = new ArrayList<Object>();
			}
		}else {
			rootObject = root;
			rootFactory = getCreatorClass(root);
		}

		parsing(xmlEntity, rootFactory, rootObject, null);

		for(int i =0;i< notKey.size();i++) {
			XMLEntity itemXmlEntity = notKey.get(i);
			SimpleKeyValueList<String, String> myRefs = notKey.getValueByIndex(i);
			String id = itemXmlEntity.getString(XMI_ID);
			Object item = getObject(id);

			SendableEntityCreator creator = getCreator(item.getClass().getName(), false, null);
			for(int r = 0; r <myRefs.size();r++) {
				String prop = myRefs.get(r);
				String value = myRefs.getValueByIndex(r);
				SimpleList<String> refs = getRef(value, itemXmlEntity, creator);
				for(String ref : refs) {
					Object object = getObject(ref);
					 if (object != null) {
						 creator.setValue(item, prop, object, "");
					 }
				}
			}
		}
		return rootObject;
	}

	private Object decodingClassModel(XMLEntity values, GraphModel model) {
		if(values == null || model == null) {
			return null;
		}
		SimpleKeyValueList<String, Clazz> items = new SimpleKeyValueList<String, Clazz>();
		for(int c=0;c<values.sizeChildren();c++) {
			BaseItem item = values.getChild(c);
			if(item instanceof XMLEntity == false) {
				continue;
			}
			XMLEntity child = (XMLEntity) item;
			String[] splitTag = child.getTag().split("\\:");
			String className = splitTag[1];
			Clazz clazz = items.get(className);
			if(clazz == null) {
				// Create New One
				clazz = model.createClazz(className);
				items.add(className, clazz);
			}
			for(int i = 0;i < child.size();i++) {
				String key = child.get(i);
				String value = (String) child.getValueByIndex(i);
				if(value == null) {
					value = "";
				}
				if(value.startsWith("/")) {
					// Association
					AssociationSet associations = clazz.getAssociations();
					Association found = null;
					for(Association assoc : associations) {
						if(key.equals(assoc.getName())) {
							found = assoc;
							break;
						}
					}
					if(found == null ) {
						found = new Association(clazz);
						found.with(key);
						SimpleList<String> refs = getRef(key, child, null);
						for (String ref : refs) {
							Association back = new Association(items.get(ref));
							found.with(back);
						}
					}
					if(value.indexOf("/", 1) > 0) {
						// To Many
						found.with(Cardinality.MANY);
					}
				}
			}
		}
		//TODO CREATING METHOD BODY
		return model;
	}

	private SimpleList<String> getRef(String value, XMLEntity xmlEntity, SendableEntityCreator rootFactory) {
		if(value == null) {
			return null;
		}
		SimpleList<String> result = new SimpleList<String>();
		if (value.startsWith("//@")) {
			for (String ref : value.split(" ")) {
				String myRef = "_" + ref.substring(3);
				if (myRef.indexOf('.') > 0) {
					myRef = myRef.replaceAll("\\.|/@", "");
				} else {
					myRef = "_" + myRef.subSequence(0, 1) + "0";
				}
				result.add(myRef);
			}
		} else if (value.startsWith("/")) {
			// maybe multiple separated by blanks
			String tagChar = xmlEntity.getTag().substring(0, 1);
			for (String ref : value.split(" ")) {
				ref = "_" + tagChar + ref.substring(1);
				if (getObject(ref) != null) {
					result.add(ref);
				}
			}
		} else if (value.indexOf('_') > 0) {
			// maybe multiple separated by blanks
			for (String ref : value.split(" ")) {
				if (getObject(ref) != null) {
				   result.add(ref);
				}
			}
		} else if (value.startsWith("$")) {
			for (String ref : value.split(" ")) {
				String myRef = "_" + ref.substring(1);
				if (rootFactory != null && getObject(myRef) != null) {
					result.add(myRef);
				}
			}
		}
		return result;
	}

	SimpleKeyValueList<String, Integer> runningNumbers = new SimpleKeyValueList<String, Integer>();
	SimpleKeyValueList<XMLEntity, SimpleKeyValueList<String, String>> notKey = new SimpleKeyValueList<XMLEntity, SimpleKeyValueList<String, String>>();

	@SuppressWarnings("unchecked")
	private void parsing(XMLEntity xmlEntity, SendableEntityCreator entityFactory, Object entityObject, String rootId) {
		if(xmlEntity == null || this.map == null) {
			return;
		}
		String id = (String) xmlEntity.getValue(XMI_ID);
		Collection<Object> rootCollection = null;
		if (id == null) {
			String tag = xmlEntity.getTag();
			if (rootId != null) {
				rootId += tag;
				Integer num = runningNumbers.get(rootId);
				if (num == null) {
					num = 0;
				} else {
					num++;
				}
				runningNumbers.put(rootId, num);
				rootId += num;
			} else {
				rootId = "$";
			}
			if (xmlEntity.has("href")) {
				// might point to another xml file already loaded
				// might point to another xml file already loaded
				String refString = xmlEntity.getString("href");
				String[] split = refString.split("#//");

				if (split.length == 2) {
					String objectId = split[1];
					objectId = objectId.replace('@', '_');
					objectId = objectId.replace(".", "");
					Object object = getObject(objectId);

					if (object != null) {
						// yes we know it
						if (entityObject instanceof Collection<?>) {
							rootCollection = (Collection<Object>) entityObject;
						}
						if (rootCollection != null) {
							rootCollection.add(object);
						} else {
							entityFactory.setValue(entityObject, tag, object, "");
						}
						return;
					} else {

					}
				}

				if (split.length == 2) {
					String objectId = split[1];
					objectId = objectId.replace('@', '$');
					objectId = objectId.replace(".", "");
					xmlEntity.put(XMI_ID, objectId);
				}
			}
			if(entityFactory instanceof SendableEntityCreatorIndexId) {
				// Get Creator
				String temp = xmlEntity.getString(IdMap.ID);
				if(temp != null) {
					rootId = temp;
				}
			}
		}

		if (rootId.startsWith("$")) {
			rootId = "_" + rootId.substring(1);
		}

		this.map.put(rootId, entityObject, true);
		if(xmlEntity.has(XMI_ID) == false) {
			xmlEntity.put(XMI_ID, rootId);
		}

		// set plain attributes
		if(entityFactory != null) {
			for (int i = 0; i < xmlEntity.size(); i++) {
				String key = xmlEntity.getKeyByIndex(i);
				String value = xmlEntity.getString(key);
				if (value == null) {
					continue;
				}
				value = value.trim();
				if ("".equals(value) || XMI_ID.equals(key)) {
					continue;
				}
				SimpleList<String> myRefs = getRef(value, xmlEntity, entityFactory);
				if (myRefs.size() == 0) {
				   entityFactory.setValue(entityObject, key, value, "");
				}
				for (String myRef : myRefs) {
				   Object object = getObject(myRef);
				   if (object != null) {
					   entityFactory.setValue(entityObject, key, object, "");
				   } else {
					   // Link not know
					   SimpleKeyValueList<String, String> list = notKey.get(xmlEntity);
					   if(list == null) {
						   list = new SimpleKeyValueList<String, String>();
						   notKey.put(xmlEntity, list);
					   }
					   list.put(key, value);
					   break;
				   }
				}
			}
		}
		String tag;
		int pos;

		for(int i=0;i<xmlEntity.sizeChildren();i++) {
			String typeName = null;
			XMLEntity kid = (XMLEntity) xmlEntity.getChild(i);
			tag = kid.getTag();
			// identify kid type
			if (entityObject instanceof Collection) {
				rootCollection = (Collection<Object>) entityObject;
				// take the type name from the tag
				pos = tag.indexOf(":");
				if(pos > 0) {
					typeName = tag.substring(pos+1);
				}else{
					typeName = tag;
				}
			}

			if (kid.has(XSI_TYPE)) {
				typeName = kid.getString(XSI_TYPE);
				typeName = typeName.replaceAll(":", ".");
			}
			if(typeName == null) {
				Object value = entityFactory.getValue(entityObject, tag);
				if(value != null) {
					if(value instanceof SimpleSet<?>) {
						SimpleSet<?> set = (SimpleSet<?>) value;
						typeName = set.getTypClass().getName();
					}else {
						typeName = value.getClass().getName();
					}
				} else {
					typeName = tag;
				}
			}

			if (typeName != null) {
				SendableEntityCreator kidFactory = getCreator(typeName, false, null);
				if (kidFactory == null && typeName.endsWith("s")) {
					kidFactory = getCreator(typeName.substring(0, typeName.length() - 1), false, null);
				}
				if(kidFactory == null) {
					continue;
				}
				Object kidObject = kidFactory.getSendableInstance(false);

				parsing(kid, kidFactory, kidObject, rootId);
				if (rootCollection != null) {
					rootCollection.add(kidObject);
				} else {
					entityFactory.setValue(entityObject, tag, kidObject, "");
				}
			}
		}
	}

	public GraphList decoding(String content) {
		return decoding(new XMLEntity().withValue(content), null);
	}
	public GraphList decoding(Tokener content) {
		return decoding(new XMLEntity().withValue(this), null);
	}

	private GraphList decoding(XMLEntity ecore, GraphList model) {
		if(model == null) {
			model = new GraphList();
		}
		if(ecore ==null) {
			return model;
		}
		SimpleList<Entity> superClazzes = new SimpleList<Entity>();

		// add classes
		SimpleKeyValueList<Entity, EntityList> parentList=new SimpleKeyValueList<Entity, EntityList>();
		for(int i=0;i<ecore.sizeChildren();i++) {
			BaseItem eClassifier = ecore.getChild(i);
			if(eClassifier instanceof XMLEntity == false) {
				continue;
			}
			XMLEntity xml = (XMLEntity) eClassifier;
			if (xml.has(XSI_TYPE)== false) {
				continue;
			}

			if (xml.getString(XSI_TYPE).equalsIgnoreCase(TYPE_ECLASS)) {
				Clazz clazz = new Clazz(xml.getString(EMFTokener.NAME));
				model.with(clazz);
				for(int c=0;c<xml.sizeChildren();c++) {
					BaseItem child = xml.getChild(c);
					if(child instanceof Entity == false) {
						continue;
					}
					Entity childItem = (Entity) child;
					String typ = childItem.getString(XSI_TYPE);
					if(typ.equals(TYPE_EAttribute)) {
						String etyp = EntityUtil.getId(childItem.getString(ETYPE));
						if (EntityUtil.isEMFType(etyp)) {
							etyp = etyp.substring(1);
						}
						if (EntityUtil.isPrimitiveType(etyp.toLowerCase())) {
							etyp = etyp.toLowerCase();
						}
						clazz.withAttribute(EntityUtil.toValidJavaId(childItem.getString(EMFTokener.NAME)), DataType.create(etyp));
					}else if(typ.equals(TYPE_EReferences)) {
						parentList.add(childItem, eClassifier);
					}
				}
				if(xml.has(TYPE_ESUPERTYPE)) {
					superClazzes.add(xml);
				}
			} else if (xml.getString(XSI_TYPE).equals(TYPE_EEnum)) {
				Clazz graphEnum = new Clazz(xml.getString(EMFTokener.NAME));
				GraphUtil.setClazzType(graphEnum, Clazz.TYPE_ENUMERATION);
				for(int c=0;c<xml.sizeChildren();c++) {
					BaseItem child = ecore.getChild(i);
					if(child instanceof Entity == false) {
						continue;
					}
					Entity childItem = (Entity) child;
					Literal literal = new Literal(childItem.getString(EMFTokener.NAME));
					for(int z=0;z<childItem.size();z++) {
						String key = childItem.getKeyByIndex(z);
						if(key.equals(EMFTokener.NAME)) {
							continue;
						}
						literal.withValue(childItem.getValue(key));
						GraphUtil.setLiteral(graphEnum, literal);
					}
				}
			}
		}
		 // inheritance
		for(Entity eClass : superClazzes) {
			String id = EntityUtil.getId(eClass.getString(TYPE_ESUPERTYPE));
			 Clazz kidClazz = model.getNode(eClass.getString(EMFTokener.NAME));
			 if(kidClazz != null) {
				 Clazz superClazz = model.getNode(id);
				 kidClazz.withSuperClazz(superClazz);
			 }
		}
		// assocs
		SimpleKeyValueList<String, Association> items = new SimpleKeyValueList<String, Association>();
		for(int i=0;i<parentList.size();i++) {
			Entity eref = parentList.get(i);
			String tgtClassName = eref.getString(ETYPE);
			if(tgtClassName.indexOf("#")>=0) {
				tgtClassName = tgtClassName.substring(tgtClassName.indexOf("#") + 3);
			}
			String tgtRoleName = eref.getString(EMFTokener.NAME);

			Association tgtAssoc = getOrCreate(items, model, tgtClassName, tgtRoleName);

			if (eref.has(UPPERBOUND)) {
				Object upperValue = eref.getValue(UPPERBOUND);
				if (upperValue instanceof Number) {
					if (((Number) upperValue).intValue() != 1) {
						tgtAssoc.with(Cardinality.MANY);
					}
				}
			}

			String srcRoleName = null;
			XMLEntity parent = (XMLEntity) parentList.getValueByIndex(i);
			String srcClassName = parent.getString(EMFTokener.NAME);
			if (!eref.has(EOpposite)) {
//				srcRoleName = tgtRoleName+"_back";
			}else{
				srcRoleName = EntityUtil.getId(eref.getString(EOpposite));
			}
			Association srcAssoc = getOrCreate(items, model, srcClassName, srcRoleName);
			// Create as Unidirection
			tgtAssoc.with(srcAssoc);
			srcAssoc.with(AssociationTypes.EDGE);

			GraphUtil.setAssociation(tgtAssoc.getClazz(), tgtAssoc);
			GraphUtil.setAssociation(srcAssoc.getClazz(), srcAssoc);

			GraphUtil.setAssociation(model, tgtAssoc);
		}
		return model;
	}

	private Association getOrCreate(SimpleKeyValueList<String, Association> items, GraphList model, String className, String roleName) {
		if(items == null) {
			return null;
		}
		if(className != null ) {
			int pos = className.indexOf("/");
			if(pos>0) {
				className = className.substring(pos+1);
			}
		}
		roleName = EntityUtil.toValidJavaId(roleName);
		String assocName = className+":"+roleName;
		Association edge = (Association) items.getValue(assocName);
		if(edge == null) {
			Clazz clazz = model.getNode(className);
			if(clazz == null) {
				// Create it
				clazz = model.createClazz(className);
			}
			if(clazz != null) {
				edge = new Association(clazz).with(Cardinality.ONE).with(roleName);
				GraphUtil.setAssociation(clazz, edge);
				if(roleName != null) {
					items.add(assocName, edge);
				}
			}
		}
		return edge;
	}

	/**
	 * Export to XMI File
	 * @param list the GraphList
	 * @return XMLEntity
	 */
	public XMLEntity toXMI(GraphList list) {
		XMLContainer container = new XMLContainer();
		container.withStandardPrefix();
		if(list == null) {
			return container;
		}

		XMLEntity root = container.createChild("uml:Model");
		root.withKeyValue("xmlns:xmi", "http://www.omg.org/XMI");
		root.withKeyValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		root.withKeyValue("xmlns:uml", "http://www.eclipse.org/uml2/5.0.0/UML");
		root.withKeyValue("xmi:version", "2.0");
		root.withKeyValue(XMI_ID, list.getName());
		root.withKeyValue(NAME, "model");

		this.encodeAnnotations(root.createChild(null));

		// Add all Clazzes
		ClazzSet clazzes = list.getClazzes();
		for(Clazz clazz : clazzes) {
			encodePackagedElementClass(root.createChild(null), clazz);
		}

		AssociationSet associations = list.getAssociations();
		for(Association assoc : associations) {
			encodeAssoc(root.createChild(null), assoc);
		}

		return container;
	}
	/**
	 * To UML FileFormat
	 * @param list The GraphModel
	 * @return The XMLEntity
	 */
	public XMLEntity toUML(GraphList list) {
		return toXMI(list);
	}

	public void encodePackagedElementClass(XMLEntity root, Clazz clazz) {
		if(clazz == null || root == null) {
			return;
		}
		root.setType("packagedElement");
		root.withKeyValue(XMI_ID, clazz.getId());
		root.withKeyValue(NAME, clazz.getName());
		root.withKeyValue("isAbstract", clazz.getModifier().has(Modifier.ABSTRACT));
		if(Clazz.TYPE_INTERFACE.equals(clazz.getType())) {
			root.withKeyValue(XMI_TYPE, "uml:Interface");
		} else {
			root.withKeyValue(XMI_TYPE, clazz.getName());
			ClazzSet interfaces = clazz.getInterfaces(false);
			if(interfaces.size()>0) {
				CharacterBuffer value = new CharacterBuffer();
				for(Clazz interfaceClazz : interfaces) {
					if(value.length() >0) {
						value.with(' ');
					}
					value.with(interfaceClazz.getId());
					XMLEntity interfaceChild = root.createChild("interfaceRealization");
					interfaceChild.withKeyValue(XMI_TYPE, "uml:InterfaceRealization");
					interfaceChild.withKeyValue(XMI_ID, interfaceClazz.getId());
					interfaceChild.withKeyValue("supplier", clazz.getId());
					interfaceChild.withKeyValue("client", interfaceClazz.getId());
					interfaceChild.withKeyValue("contract", interfaceClazz.getId());
				}
				root.withKeyValue("clientDependency", value.toString());
			}
			ClazzSet superClazzes = clazz.getSuperClazzes(false);
			for(Clazz superClazzesClazz : superClazzes) {
				XMLEntity superClassChild = root.createChild("generalization");
				superClassChild.withKeyValue(XMI_TYPE, "uml:Generalization");
				superClassChild.withKeyValue(XMI_ID, superClazzesClazz.getId());
				superClassChild.withKeyValue("general", clazz.getId());
			}
		}
		// attributes
		AttributeSet attributes = clazz.getAttributes();
		for(Attribute attribute : attributes) {
			encodeOwnedValue(root.createChild(null), attribute);
		}
		// methods
		MethodSet methods = clazz.getMethods();
		for(Method method : methods) {
			encodeOwnedOperation(root.createChild(null), method);
		}
		/* result += this.operations.reduce((acc, method) => acc + method.toString(), '');  */
	}

	public void encodeOwnedValue(XMLEntity root, Value value) {
		if(root == null || value == null) {
			return;
		}
		if(value instanceof Attribute) {
			root.setType("ownedAttribute");
		} else {
			root.setType("ownedParameter");
		}
		root.withKeyValue(XMI_TYPE, value.getType());
		root.withKeyValue(XMI_ID, value.getName());
		root.withKeyValue(NAME, value.getName());
		root.withKeyValue("visibility", value.getModifier());
		if(EntityUtil.isPrimitiveType(value.getType().getName(false))) {
			XMLEntity child = root.createChild("type");
			child.withKeyValue(XMI_TYPE, "uml:PrimitiveType");
			child.withKeyValue("href", "http://www.omg.org/spec/UML/20110701/PrimitiveTypes.xmi"+value.getType().getName(false));
		} else {
			root.withKeyValue("type", value.getType().getName(false));
		}
		XMLEntity child = root.createChild("lowerValue");
		child.withKeyValue(XMI_TYPE, "uml:LiteralInteger");
		child.withKeyValue(XMI_ID, "_lv"+value.getName());
		child.withKeyValue(VALUE, "1");

		child = root.createChild("upperValue");
		child.withKeyValue(XMI_TYPE, "uml:LiteralUnlimitedNatural");
		child.withKeyValue(XMI_ID, "_uv"+value.getName());
		child.withKeyValue(VALUE, "1");
	}

	public void encodeOwnedOperation(XMLEntity root, Method method) {
		if(root == null || method == null) {
			return;
		}
		root.setType("ownedOperation");
		root.withKeyValue(XMI_ID, method.getName());
		root.withKeyValue(NAME, method.getName());
		root.withKeyValue("visibility", method.getModifier());

		// parameters
		ParameterSet parameter = method.getParameters();
		for(Parameter param : parameter) {
			encodeOwnedValue(root.createChild(null), param);
		}
		// return type
		DataType returnType = method.getReturnType();
		if(method.getReturnType().equals(DataType.VOID) == false) {
			XMLEntity returnChild = root.createChild("ownedParameter");
			returnChild.withKeyValue(XMI_ID, method.getReturnType().toString());
			returnChild.withKeyValue("direction", "return");
			if(EntityUtil.isPrimitiveType(returnType.toString())) {
				XMLEntity child = root.createChild("type");
				child.withKeyValue(XMI_TYPE, "uml:PrimitiveType");
				child.withKeyValue("href", "http://www.omg.org/spec/UML/20110701/PrimitiveTypes.xmi"+returnType.toString());
			} else {
				returnChild.withKeyValue("type", returnType.toString());
			}
			XMLEntity child = root.createChild("lowerValue");
			child.withKeyValue(XMI_TYPE, "uml:LiteralInteger");
			child.withKeyValue(XMI_ID, "_lv"+returnType.toString());
			child.withKeyValue(VALUE, "1");

			child = root.createChild("upperValue");
			child.withKeyValue(XMI_TYPE, "uml:LiteralUnlimitedNatural");
			child.withKeyValue(XMI_ID, "_uv"+returnType.toString());
			child.withKeyValue(VALUE, "1");
		}
	}

	public void encodeAssoc(XMLEntity root, Association assoc) {
		if(root == null || assoc == null || assoc.getOther() == null) {
			return;
		}
		root.setType("packagedElement");
		root.withKeyValue(XMI_TYPE, assoc.getType());
		root.withKeyValue(XMI_ID, assoc.getName());
		root.withKeyValue(NAME, assoc.getName());
		root.withKeyValue("memberEnd", "_end-"+assoc.getName()+" _end-"+assoc.getOther().getName());

		// source
		encodeSubAssoc(root, assoc);

		//target
		encodeSubAssoc(root, assoc.getOther());
	}

	public void encodeSubAssoc(XMLEntity root, Association assoc) {
		if(root == null || assoc == null || assoc.getClazz() == null) {
			return;
		}
		XMLEntity child = root.createChild("ownedEnd");
		root.withKeyValue(XMI_TYPE, "uml:Property");
		root.withKeyValue(XMI_ID, assoc.getName());
		root.withKeyValue(NAME, assoc.getName());
		root.withKeyValue("type", assoc.getClazz().getId());
		root.withKeyValue("association", assoc.getName());

		XMLEntity childChild = child.createChild("lowerValue");
		childChild.withKeyValue(XMI_TYPE, "uml:LiteralInteger");
		childChild.withKeyValue(XMI_ID, assoc.getClazz().getId());
		childChild.withKeyValue(VALUE, "1");

		childChild = child.createChild("upperValue");
		childChild.withKeyValue(XMI_TYPE, "uml:LiteralUnlimitedNatural");
		childChild.withKeyValue(XMI_ID, assoc.getClazz().getId());
		if(assoc.getCardinality()==Cardinality.ONE) {
			childChild.withKeyValue(VALUE, "1");
		} else {
			childChild.withKeyValue(VALUE, "*");
		}
	}

	private void encodeAnnotations(XMLEntity child) {
		if(child == null) {
			return;
		}
		String modelid="_modelid";
		child.setType("eAnnotations");
		child.withKeyValue(XMI_ID, modelid);
		child.withKeyValue("source", "Objing");
		XMLEntity content = child.createChild("contents");
		content.withKeyValue(XMI_TYPE, "uml:Property");
		content.withKeyValue(XMI_ID, modelid);
		content.withKeyValue(NAME, "exporterVersion");
		XMLEntity value = content.createChild("defaultValue");
		value.withCloseTag();
		value.withKeyValue(XMI_TYPE, "uml:LiteralString");
		value.withKeyValue(XMI_ID, modelid);
		value.withKeyValue(VALUE, "3.0.0");
	}
}
