package de.uniks.networkparser;

import de.uniks.networkparser.buffer.Tokener;
import de.uniks.networkparser.interfaces.BaseItem;
import de.uniks.networkparser.interfaces.Entity;
import de.uniks.networkparser.interfaces.Grammar;
import de.uniks.networkparser.interfaces.SendableEntityCreator;
import de.uniks.networkparser.json.JsonObject;
import de.uniks.networkparser.json.JsonTokener;
import de.uniks.networkparser.list.SimpleList;

public class SimpleGrammar implements Grammar {
	private SimpleList<String> basicProperties =new SimpleList<String>().with(IdMap.ID, IdMap.CLASS, IdMap.SESSION, IdMap.TIMESTAMP);
	
	@Override
	public BaseItem getProperties(Entity item, IdMap map, Filter filter, boolean isId, String type) {
		if (isId) {
			if (item.has(JsonTokener.PROPS)) {
				return ((JsonObject)item).getJsonObject(JsonTokener.PROPS);
			}
		} else {
			JsonObject props = new JsonObject();
			for (int i = 0; i < item.size(); i++) {
				if (!IdMap.CLASS.equalsIgnoreCase(item.getKeyByIndex(i))) {
					props.put(item.getKeyByIndex(i), item.getValue(i));
				}

			}
			return props;
		}
		return null;
	}

	@Override
	public String getId(Object obj, IdMap map) {
		return null;
	}

	@Override
	public Entity writeBasicValue(Entity entity, String className, String id, IdMap map) {
		if(basicProperties.contains(IdMap.SESSION)) {
			String session = map.getSession();
			if(session != null) {
				entity.put(IdMap.SESSION, session);
			}
		}
		if(basicProperties.contains(IdMap.CLASS)) {
			entity.setType(className);
		}

		if(id != null) {
			if(basicProperties.contains(IdMap.ID)) {
				entity.put(IdMap.ID, id);
			}
			if(basicProperties.contains(IdMap.TIMESTAMP)) {
				if(map.getTimeStamp() == 0) {
					String ts = id.substring(1);
					if(EntityUtil.isNumeric(ts)) {
							entity.put(IdMap.TIMESTAMP, ts);
					}
				}
			}
		}
		return entity;
	}

	@Override
	public SendableEntityCreator getCreator(String type, Object item, IdMap map, boolean searchForSuperCreator, String className) {
		if(Grammar.WRITE.equals(type)) {
			SendableEntityCreator creator = map.getCreator(className, true);

			if(creator != null) {
				return creator;
			}
			if (item instanceof SendableEntityCreator) {
				return (SendableEntityCreator) item;
			}
			return getSuperCreator(map, searchForSuperCreator, item); 
		}
		if(className == null && item instanceof Entity) {
			Object name = ((Entity)item).getValue(IdMap.CLASS);
			if(name == null) {
				return null;
			}
			className = (String) name;
		}
		SendableEntityCreator creator = map.getCreator((String) className, true);
		if(creator != null) {
			return creator;
		}
		Class<?> clazzName = getClassForName((String) className);
		return getSuperCreator(map, searchForSuperCreator, clazzName);
	}

	public SendableEntityCreator getSuperCreator(IdMap map, boolean searchForSuperCreator, Object modelItem) {
		return null;
	}

	protected Class<?> getClassForName(String name) {
		return null;
	}

	@Override
	public boolean hasValue(Entity item, String property) {
		return item.has(property);
	}

	@Override
	public Object getNewEntity(SendableEntityCreator creator, String className, boolean prototype) {
		return creator.getSendableInstance(prototype);
	}

	@Override
	public String getValue(Entity item, String property) {
		return item.getString(property);
	}

	@Override
	public BaseItem encode(Object entity, MapEntity map, Tokener tokener) {
		return tokener.encode(entity, map);
	}
	
	public SimpleGrammar withBasicFeature(String... values) {
		if(values == null) {
			return this;
		}
		for(String item : values) {
			this.basicProperties.add(item);
		}
		return this;
	}

	public SimpleGrammar withoutBasicFeature(String... values) {
		if(values == null) {
			return this;
		}
		for(String item : values) {
			this.basicProperties.without(item);
		}
		return this;
	}

	@Override
	public boolean writeValue(BaseItem parent, String property, Object value, MapEntity map, Tokener tokener) {
		return false;
	}
}
