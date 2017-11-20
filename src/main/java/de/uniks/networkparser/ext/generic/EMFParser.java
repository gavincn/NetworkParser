package de.uniks.networkparser.ext.generic;

import java.util.List;

import de.uniks.networkparser.EntityUtil;
import de.uniks.networkparser.ext.ClassModel;
import de.uniks.networkparser.graph.Cardinality;
import de.uniks.networkparser.graph.Clazz;
import de.uniks.networkparser.graph.DataType;
import de.uniks.networkparser.graph.GraphUtil;
import de.uniks.networkparser.list.SimpleKeyValueList;
import de.uniks.networkparser.list.SimpleList;
import de.uniks.networkparser.list.SimpleSet;

public class EMFParser {
	protected Object value;
	
	public EMFParser(Object value) {
		this.value = value;
	}

	public static final void addAttributes(EMFParser eclass, Clazz sdmClass) {
		List<Object> callList = getEAttributes(eclass);
		for(Object item : callList) {
			if (item != null) {
				String name = getName(item);
				EMFParser eClassifier = new EMFParser(ReflectionLoader.call("getEType", item));
				sdmClass.withAttribute(name, DataType.create(EntityUtil.shortClassName(getInstanceClassName(eClassifier))));
			}
		}
	}

	public static final ClassModel getClassModelFromEPackage(Object epackage, String packageName, boolean withImpl) {
		// get class model from epackage
		ClassModel model = new ClassModel(packageName);
		if (epackage == null) {
			return model;
		}
		if (ReflectionLoader.EPACKAGE.isAssignableFrom(epackage.getClass()) == false) {
			return model;
		}

		SimpleKeyValueList<EMFParser, Clazz> classMap = new SimpleKeyValueList<EMFParser, Clazz>();
		List<EMFParser> eClasses = getEClasses(epackage);
		for (EMFParser eclass : eClasses) {
			// add an interface and a class to the SDMModel
			String fullClassName = getName(eclass);
			Clazz sdmClass = model.createClazz(fullClassName).enableInterface();

			if (withImpl) {
				sdmClass.enableInterface();

				String implClassName = GraphUtil.getPackage(fullClassName) + ".impl." + getName(eclass) + "Impl";
				model.createClazz(implClassName).withSuperClazz(sdmClass);
			}

			classMap.put(eclass, sdmClass);

			// add attributes
			addAttributes(eclass, sdmClass);
		}

		SimpleSet<Object> doneERefs = new SimpleSet<Object>();
		for (EMFParser eclass : eClasses) {
			if (getESuperTypes(eclass).isEmpty() == false) {
				EMFParser eSuperClass = getESuperTypes(eclass).get(0);
				Clazz sdmSuperClass = classMap.get(eSuperClass);
				Clazz sdmClass = classMap.get(eclass);
				sdmClass.withSuperClazz(sdmSuperClass);
			}

			List<Object> eReferences = getEReferences(eclass);
			for (Object eref : eReferences) {
				if (doneERefs.contains(eref) == false) {
					Object oppositeERef = getEOpposite(eref);
					if (oppositeERef != null) {
						// create assoc
						EMFParser srcEClass = getEType(oppositeERef);
						EMFParser tgtEClass = getEType(eref);

						Clazz srcSDMClass = classMap.get(srcEClass);
						Clazz tgtSDMClass = classMap.get(tgtEClass);

						Cardinality srcCard = (getUpperBound(oppositeERef) == 1 ? Cardinality.ONE : Cardinality.MANY);
						Cardinality tgtCard = (getUpperBound(eref) == 1 ? Cardinality.ONE : Cardinality.MANY);

						srcSDMClass.withBidirectional(tgtSDMClass, getName(eref), tgtCard, getName(oppositeERef), srcCard);

						doneERefs.add(eref);
						doneERefs.add(oppositeERef);
					} else {
						// uni directional assoc
						EMFParser srcEClass = eclass;
						EMFParser tgtEClass = getEType(eref);

						Clazz srcSDMClass = classMap.get(srcEClass);
						Clazz tgtSDMClass = classMap.get(tgtEClass);

						Cardinality tgtCard = (getUpperBound(eref) == 1 ? Cardinality.ONE : Cardinality.MANY);

						srcSDMClass.withUniDirectional(tgtSDMClass, getName(eref), tgtCard);

						doneERefs.add(eref);
					}
				}
			}
		}
		return model;
	}
	
	//REFACTORING
	public static final List<Object> getEAttributes(Object eref) {
		if(eref instanceof EMFParser) {
			return getEAttributes(((EMFParser)eref).getValue());
		}
		List<Object> callList = ReflectionLoader.callList("getEAttributes", eref);
		return callList;
	}

	
	public static final List<Object> getEReferences(Object eref) {
		if(eref instanceof EMFParser) {
			return getEReferences(((EMFParser)eref).getValue());
		}
		List<Object> callList = ReflectionLoader.callList("getEReferences", eref);
		return callList;
	}

	public static final SimpleList<EMFParser> getESuperTypes(Object eref) {
		if(eref instanceof EMFParser) {
			return getESuperTypes(((EMFParser)eref).getValue());
		}
		SimpleList<EMFParser> list=new SimpleList<EMFParser>();
		List<Object> callList = ReflectionLoader.callList("getESuperTypes", eref);
		for(Object item : callList) {
			if(item != null) {
				list.add(new EMFParser(item));
			}
		}
		return list;
	}

	public static final List<EMFParser> getEClasses(Object eref) {
		if(eref instanceof EMFParser) {
			return getEClasses(((EMFParser)eref).getValue());
		}
		SimpleList<EMFParser> items = new SimpleList<EMFParser>();
		List<Object> callList = ReflectionLoader.callList("getEClassifiers", eref);
		for(Object item : callList) {
			if(item != null && ReflectionLoader.ECLASS.isAssignableFrom(item.getClass())) {
				items.add(new EMFParser(item));
			}
		}
		return items;
	}
	
	public static final String getInstanceClassName(Object eref) {
		if(eref instanceof EMFParser) {
			return getInstanceClassName(((EMFParser)eref).getValue());
		}
		return ""+ReflectionLoader.call("getInstanceClassName", eref);
	}
	
	public static final EMFParser getEType(Object eref) {
		if(eref instanceof EMFParser) {
			return getEType(((EMFParser)eref).getValue());
		}
		return new EMFParser(ReflectionLoader.call("getEType", eref));
	}

	public static final int getUpperBound(Object eref) {
		if(eref instanceof EMFParser) {
			return getUpperBound(((EMFParser)eref).getValue());
		}
		return (int)ReflectionLoader.call("getUpperBound", eref);
	}

	public static final String getName(Object eref) {
		if(eref instanceof EMFParser) {
			return getName(((EMFParser)eref).getValue());
		}
		return "" + ReflectionLoader.call("getName", eref);
	}
	public static final Object getEOpposite(Object eref) {
		if(eref instanceof EMFParser) {
			return getEOpposite(((EMFParser)eref).getValue());
		}
		return ReflectionLoader.call("getEOpposite", eref);
	}

	public boolean equals(Object obj) {
		if(super.equals(obj)) {
			return true;
		}
		if(obj instanceof EMFParser == false) {
			return false;
		}
		EMFParser other = (EMFParser) obj;
		return this.value.equals(other.getValue());
	}

	public Object getValue() {
		return this.value;
	}
}
