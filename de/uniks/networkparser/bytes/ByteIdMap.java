package de.uniks.networkparser.bytes;

/*
 NetworkParser
 Copyright (c) 2011 - 2013, Stefan Lindel
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 1. Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 3. All advertising materials mentioning features or use of this software
 must display the following acknowledgement:
 This product includes software developed by Stefan Lindel.
 4. Neither the name of contributors may be used to endorse or promote products
 derived from this software without specific prior written permission.

 THE SOFTWARE 'AS IS' IS PROVIDED BY STEFAN LINDEL ''AS IS'' AND ANY
 EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL STEFAN LINDEL BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import de.uniks.networkparser.AbstractIdMap;
import de.uniks.networkparser.Filter;
import de.uniks.networkparser.IdMap;
import de.uniks.networkparser.bytes.converter.ByteConverter;
import de.uniks.networkparser.bytes.converter.ByteConverterHTTP;
import de.uniks.networkparser.event.BasicMessage;
import de.uniks.networkparser.event.MapEntry;
import de.uniks.networkparser.event.UnknownMessage;
import de.uniks.networkparser.event.creator.BasicMessageCreator;
import de.uniks.networkparser.exceptions.TextParsingException;
import de.uniks.networkparser.interfaces.ByteCreator;
import de.uniks.networkparser.interfaces.ByteItem;
import de.uniks.networkparser.interfaces.BaseEntity;
import de.uniks.networkparser.interfaces.SendableEntityCreator;
/**
 * The Class ByteIdMap.
 */

public class ByteIdMap extends IdMap {
	/** The SPLITTER. */
	public static char SPLITTER = ' ';

	/** The Constant FIXED VALUE. */
	public static final byte DATATYPE_FIXED = 0x00;

	/** The Constant NULL-VALUE. */
	public static final byte DATATYPE_NULL = 0x22;

	/** The Constant DATATYPE_MAP. */
	public static final byte DATATYPE_CLAZZID = 0x23;

	/** The Constant CLASS-VALUE. */
	public static final byte DATATYPE_CLAZZNAME = 0x24;

	/**
	 * SIMPLE TYPES The Constant DATATYPE_BYTE.
	 */
	/** The Constant DATATYPE_INTEGER. */
	public static final byte DATATYPE_SHORT = 0x25;

	/** The Constant DATATYPE_INTEGER. */
	public static final byte DATATYPE_INTEGER = 0x26;

	/** The Constant DATATYPE_INTEGER. */
	public static final byte DATATYPE_LONG = 0x27;

	/** The Constant DATATYPE_FLOAT. */
	public static final byte DATATYPE_FLOAT = 0x28;

	/** The Constant DATATYPE_DOUBLE. */
	public static final byte DATATYPE_DOUBLE = 0x29;

	/** The Constant DATATYPE_BYTEARRAY. */
	public static final byte DATATYPE_DATE = 0x30;

	/** The Constant DATATYPE_BYTE. */
	public static final byte DATATYPE_BYTE = 0x31;

	/** The Constant DATATYPE_BYTEARRAY. */
	public static final byte DATATYPE_UNSIGNEDBYTE = 0x32;
	
	/** The Constant DATATYPE_BYTEARRAY. */
	public static final byte DATATYPE_BYTEARRAY = 0x3A;

	/** The Constant DATATYPE_CHAR. */
	public static final byte DATATYPE_CHAR = 0x40;

	/** The Constant DATATYPE_STRING. */
	public static final byte DATATYPE_STRING = 0x4A;

	/** The Constant DATATYPE_LIST. */
	public static final byte DATATYPE_LIST = 0x5A;

	/** The Constant DATATYPE_MAP. */
	public static final byte DATATYPE_MAP = 0x6A;

	/** The Constant CHECKTYPE. */
	public static final byte DATATYPE_CHECK = 0x7A;

	/** The Constant LEN_LITTLE. */
	public static final byte LEN_LITTLE = 0x01;

	/** The Constant LEN_SHORT. */
	public static final byte LEN_SHORT = 0x02;

	/** The Constant LEN_MID. */
	public static final byte LEN_MID = 0x03;

	/** The Constant LEN_BIG. */
	public static final byte LEN_BIG = 0x04;

	/** The Constant DATATYPE_LAST. */
	public static final byte LEN_LAST = 0x05;

	
	/** The decoder map. */
	protected HashMap<Byte, ByteCreator> decoderMap;

	private ByteFilter filter=new ByteFilter();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni.kassel.peermessage.IdMap#addCreator(de.uni.kassel.peermessage.
	 * interfaces.SendableEntityCreator)
	 */
	public boolean addCreator(SendableEntityCreator createrClass) {
		if (createrClass instanceof ByteCreator) {
			if(this.decoderMap != null){
				if (this.decoderMap.containsKey(new Byte(((ByteCreator) createrClass).getEventTyp()))) {
					return false;
				}
			}
		}
		super.withCreator(createrClass);
		return true;
	}

	@Override
	public AbstractIdMap withCreator(String className,
			SendableEntityCreator createrClass) {
		super.withCreator(className, createrClass);

		if (createrClass instanceof ByteCreator) {
			ByteCreator byteCreator = (ByteCreator) createrClass;
			if (this.decoderMap == null) {
				this.decoderMap = new HashMap<Byte, ByteCreator>();
			}

			this.decoderMap.put(new Byte(byteCreator.getEventTyp()),
					byteCreator);
		}
		return this;
	}

	/**
	 * Encode.
	 * 
	 * @param entity
	 *            the entity
	 * @return the byte entity message
	 */
	@Override
	public ByteItem encode(Object entity) {
		return encode(entity, (ByteFilter)filter.clone());
	}

	public ByteItem encode(Object entity, Filter filter) {
		SendableEntityCreator creator;
		if (this.parent != null) {
			creator = this.parent.getCreatorClass(entity);
		} else {
			creator = getCreatorClass(entity);
		}
		if (creator == null) {
			return null;
		}

		ByteList msg = new ByteList();
		if (creator instanceof BasicMessageCreator) {
			BasicMessage basicEvent = (BasicMessage) entity;
			String value = basicEvent.getValue();
			msg.add(new ByteEntity().withValue(DATATYPE_FIXED, value.getBytes()));
			return msg;
		}

		if (creator instanceof ByteCreator) {
			long id=((ByteCreator) creator).getEventTyp();
			msg.add(new ByteEntity().withValue(ByteIdMap.DATATYPE_CLAZZID, new byte[]{(byte) id}));
		} else {
			Object reference = creator.getSendableInstance(true);
			ByteEntity byteEntity = new ByteEntity();
			byteEntity.withValue(DATATYPE_CLAZZNAME, reference.getClass().getName()
					.getBytes());
			msg.add(byteEntity);
		}
		String[] properties = creator.getProperties();
		if (properties != null) {
			Object referenceObj = creator.getSendableInstance(true);
			for (String property : properties) {
				Object value = creator.getValue(entity, property);
				if (creator.getValue(referenceObj, property) == value) {
					value = null;
				}
				ByteItem child = encodeValue(value, filter);
				if (child != null) {
					msg.add(child);
				}
			}
			
			// Kill Empty Fields
			Object[] array = msg.toArray();
			for(int i=array.length-1;i>0;i--){
				if(array[i] instanceof ByteEntity){
					ByteEntity byteEntity = (ByteEntity)array[i];
					if(byteEntity.getTyp()==ByteIdMap.DATATYPE_NULL){
						msg.remove(i);
					}else{
						break;
					}
				}else if(array[i] instanceof ByteList){
					if(((ByteList)array[i]).size()<1){
						msg.remove(i);
					}else{
						break;
					}
				}
			}
		}
		return msg;
	}

	public ByteItem encodeValue(Object value, Filter filter) {
		ByteEntity msgEntity = new ByteEntity();
		if (msgEntity.setValues(value)) {
			return msgEntity;
		} else {
			// Map, List, Assocs
			if (value instanceof List<?>) {
				ByteList byteList = new ByteList();
				List<?> list = (List<?>) value;
				byteList.setTyp(ByteIdMap.DATATYPE_LIST);
				for (Object childValue : list) {
					ByteItem child = encodeValue(childValue, filter);
					if (child != null) {
						byteList.add(child);
					}
				}
				return byteList;
			} else if (value instanceof Map<?, ?>) {
				ByteList byteList = new ByteList();
				byteList.setTyp(ByteIdMap.DATATYPE_MAP);
				Map<?, ?> map = (Map<?, ?>) value;
				ByteItem child;

				for (Iterator<?> i = map.entrySet().iterator(); i.hasNext();) {
					java.util.Map.Entry<?, ?> entity = (Entry<?, ?>) i.next();
					ByteList item = new ByteList();
					item.setTyp(ByteIdMap.DATATYPE_CHECK);

					child = encodeValue(entity.getKey(), filter);
					if (child != null) {
						item.add(child);
					}
					child = encodeValue(entity.getValue(), filter);
					if (child != null) {
						item.add(child);
					}
					byteList.add(item);
				}
				return byteList;
			} else if (value != null) {
				ByteItem child = encode(value, filter);
				if (child != null) {
					ByteList byteList = new ByteList();
//					byteList.setTyp(ByteIdMap.DATATYPE_CLAZZ);
					byteList.add(child);
					return byteList;
				}
				return child;
			}
		}
		return null;
	}

	/**
	 * Gets the creator decoder class.
	 * 
	 * @param typ
	 *            the typ
	 * @return the creator decoder class
	 */
	public ByteCreator getCreatorDecoderClass(byte typ) {
		return this.decoderMap.get(new Byte(typ));
	}

	/**
	 * Decode.
	 * 
	 * @param value
	 *            the value
	 * @return the object
	 */
	public Object decode(String value) {
		return decode(value, new ByteConverterHTTP());
	}

	/**
	 * Decode.
	 * 
	 * @param value
	 *            the value
	 * @param ByteConverter
	 *            the Converter for bytes to String
	 * @return the object
	 */
	public Object decode(String value, ByteConverter converter) {
		byte[] decodeBytes = converter.decode(value);
		return decode(decodeBytes);
	}

	/**
	 * Decode.
	 * 
	 * @param value
	 *            the value
	 * @return the object
	 */
	public Object decode(Object value) {
		if (value instanceof BufferedBytes) {
			return decode((BufferedBytes) value);
		} else if (value instanceof byte[]) {
			return decode(new BytesBuffer().withValue((byte[]) value));
		}
		return null;
	}

	@Override
	public Object decode(BaseEntity value) {
		if(value instanceof ByteEntity){
			return decode(((ByteEntity)value).getValue());
		}
		return null;
	}
	
	/**
	 * Decode.
	 * 
	 * @param buffer
	 *            the in
	 * @return the object
	 */
	public Object decode(BufferedBytes buffer) throws TextParsingException{
		if (buffer.remaining() < 1)
			throw new TextParsingException("DecodeExpeption - Remaining:"
					+ buffer.remaining(), buffer.remaining());
		
		return decodeValue(buffer, buffer.length());
	}

	/**
	 * Decode.
	 * 
	 * @param typ
	 *            of message
	 * @param buffer
	 *            the in
	 * @return the object
	 */
	public Object decodeClazz(BufferedBytes buffer, SendableEntityCreator eventCreater) {
		if (eventCreater == null) {
			UnknownMessage e = new UnknownMessage();
			e.set(UnknownMessage.PROPERTY_VALUE, buffer.array());
			return e;
		}
		Object entity = eventCreater.getSendableInstance(false);
		String[] properties = eventCreater.getProperties();
		if (properties != null) {
			for (String property : properties) {
				if (buffer.remaining() < 1) {
					break;
				}
				Object value = decodeValue(buffer, buffer.length() - buffer.position());
				if(value != null){
					if(value instanceof List<?>){
						List<?> list=(List<?>) value;
						for(Iterator<?> i=list.iterator();i.hasNext();){
							Object item = i.next();
							eventCreater.setValue(entity, property, item, IdMap.NEW);
						}
					}else{
						eventCreater.setValue(entity, property, value, IdMap.NEW);
					}
				}
			}
		}
		return entity;
	}
	
	/**
	 * Gets the decode object.
	 * 
	 * @param typ
	 *            the typ value
	 * @param buffer
	 *            the in
	 * @return the decode object
	 */
	public Object decodeValue(BufferedBytes buffer, int end) {
		if (buffer.remaining() < 1 ){
			return null;
		}
		byte typ=buffer.getByte();
		if(typ == ByteIdMap.DATATYPE_NULL) {
			return null;
		}
		if (typ == ByteIdMap.DATATYPE_BYTE) {
			return new Byte(buffer.getByte());
		}
		if (typ == ByteIdMap.DATATYPE_CHAR) {
			return new Character(buffer.getChar());
		}
		if (typ == ByteIdMap.DATATYPE_SHORT) {
			return new Short(buffer.getShort());
		}
		if (typ == ByteIdMap.DATATYPE_INTEGER) {
			return new Integer(buffer.getInt());
		}
		if (typ == ByteIdMap.DATATYPE_LONG) {
			return new Long(buffer.getLong());
		}
		if (typ == ByteIdMap.DATATYPE_FLOAT) {
			return new Float(buffer.getFloat());
		}
		if (typ == ByteIdMap.DATATYPE_DOUBLE) {
			return new Double(buffer.getDouble());
		}
		if (typ == ByteIdMap.DATATYPE_DATE) {
			return new Date(new Long(buffer.getInt()).longValue());
		}
		if (typ == ByteIdMap.DATATYPE_CLAZZNAME) {
			int len = buffer.getByte() - ByteIdMap.SPLITTER;
			SendableEntityCreator eventCreater = getCreatorClasses(new String(buffer.getValue(len)));
			return decodeClazz(buffer, eventCreater);
		}
		if (typ == ByteIdMap.DATATYPE_CLAZZID) {
			typ = buffer.getByte();
			SendableEntityCreator eventCreater = getCreatorDecoderClass(typ);
			return decodeClazz(buffer, eventCreater);
		}
		if(ByteUtil.isGroup(typ)){
			byte subgroup = ByteUtil.getSubGroup(typ);
			int len = 0;
			if (subgroup == ByteIdMap.LEN_LITTLE) {
				len = buffer.getByte() - ByteIdMap.SPLITTER;
			} else if (subgroup == ByteIdMap.LEN_SHORT) {
				len = buffer.getByte();
			} else if (subgroup == ByteIdMap.LEN_MID) {
				len = buffer.getShort();
			} else if (subgroup == ByteIdMap.LEN_BIG) {
				len = buffer.getInt();
			} else if (subgroup == ByteIdMap.LEN_LAST) {
				len = end-1;
			}
			byte group = ByteUtil.getGroup(typ);
			if (group == ByteIdMap.DATATYPE_STRING) {
				return new String(buffer.getValue(len));
			} else if (group == ByteIdMap.DATATYPE_BYTEARRAY) {
				return buffer.getValue(len);
			} else if (group == ByteIdMap.DATATYPE_LIST) {
				int start = buffer.position();
				ArrayList<Object> values=new ArrayList<Object>();
				while(start+len-buffer.position()>0){
					Object value = decodeValue(buffer, start+len-buffer.position());
					if(value!=null){
						values.add(value);
					}
				}
				return values;
			} else if (group == ByteIdMap.DATATYPE_MAP) {
				int start = buffer.position();
				ArrayList<Object> values=new ArrayList<Object>();
				while(start+len-buffer.position()>0){
					Object subValues = decodeValue(buffer, start+len-buffer.position());
					if(subValues!=null && subValues instanceof List<?>){
						List<?> list=(List<?>) subValues;
						if(list.size()==2){
							values.add(new MapEntry(list.get(0), list.get(1)));
						}
					}else{
						break;
					}
				}
				return values;
			} else if (group == ByteIdMap.DATATYPE_CHECK) {
				int start = buffer.position();
				if(buffer.length()<start+len){
					return null;
				}
				ArrayList<Object> values=new ArrayList<Object>();
				while(start+len-buffer.position()>0){
					values.add( decodeValue(buffer, start+len-buffer.position()) );
				}
				return values;
			}
		}
		return null;
	}
}
