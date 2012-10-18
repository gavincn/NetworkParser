package de.uniks.jism.bytes;
/*
Copyright (c) 2012, Stefan Lindel
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

THIS SOFTWARE 'Json Id Serialisierung Map' IS PROVIDED BY STEFAN LINDEL ''AS IS'' AND ANY
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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import de.uniks.jism.IdMap;
import de.uniks.jism.event.BasicMessage;
import de.uniks.jism.event.UnknownMessage;
import de.uniks.jism.event.creater.BasicMessageCreator;
import de.uniks.jism.interfaces.ByteEntityCreator;
import de.uniks.jism.interfaces.ByteItem;
import de.uniks.jism.interfaces.SendableEntityCreator;

/**
 * The Class ByteIdMap.
 */
public class ByteIdMap extends IdMap{
	/** The SPLITTER. */
	public static char SPLITTER = ' ';

	/** The Constant NULL-VALUE. */
	public static final byte DATATYPE_NULL = 0x22;
	
	/** The Constant FIXED VALUE. */
	public static final byte DATATYPE_FIXED = 0x23;

	/** The Constant CLASS-VALUE. */
	public static final byte DATATYPE_CLAZZ = 0x24;
	
	/**
	 * SIMPLE TYPES 
	 * The Constant DATATYPE_BYTE.
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
	public static final byte DATATYPE_DATE = 0x2A;
		
	/** The Constant DATATYPE_MAP. */
	public static final byte DATATYPE_ASSOC = 0x2B;

	/** The Constant DATATYPE_BYTE. */
	public static final byte DATATYPE_BYTE = 0x30;

	/** The Constant DATATYPE_BYTEARRAY. */
	public static final byte DATATYPE_UNSIGNEDBYTE = 0x31;

		
	/** The Constant DATATYPE_BYTEARRAY. */
	public static final byte DATATYPE_BYTEARRAY = 0x32;

	/** The Constant DATATYPE_BYTEARRAY. */
	public static final byte DATATYPE_BYTEARRAYSHORT = 0x33;

	/** The Constant DATATYPE_BYTEARRAY. */
	public static final byte DATATYPE_BYTEARRAYMID = 0x34;

	/** The Constant DATATYPE_BYTEARRAY. */
	public static final byte DATATYPE_BYTEARRAYBIG = 0x35;

	/** The Constant DATATYPE_BYTEARRAY. */
	public static final byte DATATYPE_BYTEARRAYLAST = 0x36;

	
	/** The Constant DATATYPE_BYTEARRAY. */
	public static final byte DATATYPE_CHAR = 0x40;
	
	/** The Constant DATATYPE_STRING. */
	public static final byte DATATYPE_STRING = 0x42;

	/** The Constant DATATYPE_STRING. */
	public static final byte DATATYPE_STRINGSHORT = 0x43;
	
	/** The Constant DATATYPE_MIDSTRING. */
	public static final byte DATATYPE_STRINGMID = 0x44;

	/** The Constant DATATYPE_STRING. */
	public static final byte DATATYPE_STRINGBIG = 0x45;

	/** The Constant DATATYPE_STRING. */
	public static final byte DATATYPE_STRINGLAST = 0x46;
	
	/** The Constant DATATYPE_LIST. */
	public static final byte DATATYPE_LIST = 0x52;

	/** The Constant DATATYPE_SHORTLIST. */
	public static final byte DATATYPE_LISTSHORT = 0x53;
	
	/** The Constant DATATYPE_MIDLIST. */
	public static final byte DATATYPE_LISTMID = 0x54;

	/** The Constant DATATYPE_BIGLIST. */
	public static final byte DATATYPE_LISTBIG = 0x55;

	/** The Constant DATATYPE_LASTLIST. */
	public static final byte DATATYPE_LISTLAST = 0x56;
	
	/** The Constant DATATYPE_MAP. */
	public static final byte DATATYPE_MAP = 0x62;

	/** The Constant DATATYPE_MAP. */
	public static final byte DATATYPE_MAPSHORT = 0x63;

	/** The Constant DATATYPE_MAP. */
	public static final byte DATATYPE_MAPMID = 0x64;

	/** The Constant DATATYPE_MAP. */
	public static final byte DATATYPE_MAPBIG = 0x65;

	/** The Constant DATATYPE_MAP. */
	public static final byte DATATYPE_MAPLAST = 0x66;

	/** The Constant CHECKTYPE. */
	public static final byte DATATYPE_CHECK = 0x72;

	/** The Constant DATATYPE_CHECKSHORT. */
	public static final byte DATATYPE_CHECKSHORT = 0x73;

	/** The Constant DATATYPE_MAP. */
	public static final byte DATATYPE_CHECKMID = 0x74;

	/** The Constant DATATYPE_MAP. */
	public static final byte DATATYPE_CHECKBIG = 0x75;

	/** The decoder map. */
	protected HashMap<Byte, ByteEntityCreator> decoderMap;

	/**
	 * Instantiates a new byte id map.
	 */
	public ByteIdMap() {

	}

	/* (non-Javadoc)
	 * @see de.uni.kassel.peermessage.IdMap#addCreator(de.uni.kassel.peermessage.interfaces.SendableEntityCreator)
	 */
	@Override
	public boolean addCreator(SendableEntityCreator createrClass) {
		boolean result=super.addCreator(createrClass);
		if (this.decoderMap == null) {
			this.decoderMap = new HashMap<Byte, ByteEntityCreator>();
		}
		if(createrClass instanceof ByteEntityCreator){
			ByteEntityCreator byteCreator=(ByteEntityCreator) createrClass;
			if (this.decoderMap.containsKey(new Byte(byteCreator.getEventTyp()))) {
				return false;
			}
			this.decoderMap.put(new Byte(byteCreator.getEventTyp()), byteCreator);
		}else{
			return false;
		}
		return result;
	}
	
	/**
	 * Encode.
	 *
	 * @param entity the entity
	 * @return the byte entity message
	 */
	public ByteItem encode(Object entity) {
		ByteFilter filter=new ByteFilter();
		return encode(entity, filter);
	}
	public ByteItem encode(Object entity, ByteFilter filter) {
		SendableEntityCreator creator;
		if(this.parent!=null){
			creator = this.parent.getCreatorClass(entity);
		}else{
			creator=getCreatorClass(entity);
		}
		if (creator == null) {
			return null;
		}
		
		ByteList msg=new ByteList();
		
		if (creator instanceof BasicMessageCreator) {
			BasicMessage basicEvent = (BasicMessage) entity;
			String value = basicEvent.getValue();
			ByteEntity byteEntity = new ByteEntity();
			byteEntity.setValue(DATATYPE_FIXED, value.getBytes());
			msg.add(byteEntity);
			return msg;
		}
		
		if(creator instanceof ByteEntityCreator){
			msg.setTyp(((ByteEntityCreator) creator).getEventTyp());
		}else{
			Object reference = creator.getSendableInstance(true);
			ByteEntity byteEntity = new ByteEntity();
			byteEntity.setValue(DATATYPE_CLAZZ, reference.getClass().getName().getBytes());
			msg.add(byteEntity);
		}
		
		String[] properties = creator.getProperties();
		if (properties != null) {
			for (String property : properties) {
				ByteEntity msgEntity = new ByteEntity();
				Object value=creator.getValue(entity, property);
				msgEntity.setValues(value);
				msg.add(msgEntity);
				
//				msg.addChild(creator, entity, property, filter);				
			}
		}
		return msg;
	}

	/**
	 * Decode http.
	 *
	 * @param bytes the bytes
	 * @return the object
	 * @throws RuntimeException the runtime exception
	 */
	public Object decodeHTTP(String bytes) throws RuntimeException {
		int len = bytes.length();
		ByteBuffer buffer = ByteBuffer.allocate(len);
		for (int i = 0; i < len; i++) {
			int value = bytes.charAt(i);
			if (value == SPLITTER) {
				value = bytes.charAt(++i);
				buffer.put((byte) (value - SPLITTER - 1));
			} else {
				buffer.put((byte) value);
			}
		}
		int limit = buffer.position();
		buffer.position(0);
		buffer.limit(limit);
		return decode(buffer);
	}

	/**
	 * Gets the creator decoder class.
	 *
	 * @param typ the typ
	 * @return the creator decoder class
	 */
	public ByteEntityCreator getCreatorDecoderClass(byte typ) {
		return this.decoderMap.get(new Byte(typ));
	}
	
	/**
	 * To hex string.
	 *
	 * @param bytes the bytes
	 * @return the string
	 */
	public static String toHexString(byte[] bytes) {
		if (bytes == null) {
			return null;
		}

		String ret = "";
		for (int i = 0; i < bytes.length; i++) {

			char b = (char) (bytes[i] & 0xFF);
			if (b < 0x10) {
				ret = ret + "0";
			}
			ret = ret + (Integer.toHexString(b)).toUpperCase();
		}
		return ret;

	}

	/**
	 * To byte string.
	 *
	 * @param hexString the hex string
	 * @return the byte[]
	 */
	public static byte[] toByteString(String hexString) {
		String hexVal = "0123456789ABCDEF";
		byte[] out = new byte[hexString.length() / 2];

		int n = hexString.length();

		for (int i = 0; i < n; i += 2) {
			// make a bit representation in an int of the hex value
			int hn = hexVal.indexOf(hexString.charAt(i));
			int ln = hexVal.indexOf(hexString.charAt(i + 1));

			// now just shift the high order nibble and add them together
			out[i / 2] = (byte) ((hn << 4) | ln);
		}

		return out;
	}
	
	/**
	 * Decode.
	 *
	 * @param value the value
	 * @return the object
	 * @throws RuntimeException the runtime exception
	 */
	public Object decode(Object value) throws RuntimeException {
		if (value instanceof ByteBuffer) {
			return decode((ByteBuffer) value);
		} else if (value instanceof byte[]) {
			return decode(ByteBuffer.wrap((byte[]) value));
		}
		return null;
	}

	/**
	 * Decode.
	 *
	 * @param in the in
	 * @return the object
	 * @throws RuntimeException the runtime exception
	 */
	public Object decode(ByteBuffer in) throws RuntimeException {
		if (in.remaining() < 1)
			throw new RuntimeException("DecodeExpeption - Remaining:" + in.remaining());

		Object entity = null;
		byte typ = in.get();
		SendableEntityCreator eventCreater=null;
		if(typ==ByteIdMap.DATATYPE_CLAZZ){
			String clazz = (String) getDecodeObject(ByteIdMap.DATATYPE_CLAZZ, in);
			eventCreater = getCreatorClasses(clazz);
		}else{
			eventCreater = getCreatorDecoderClass(typ);
		}
		if (eventCreater == null) {
			UnknownMessage e = new UnknownMessage();
			e.set(UnknownMessage.PROPERTY_VALUE, in.array());
			entity = e;
		} else {
			entity = eventCreater.getSendableInstance(false);
			String[] properties = eventCreater.getProperties();
			if (properties != null) {
				for (String property : properties) {
					if (in.remaining() < 1) {
						break;
					}
					byte typValue = in.get();
					if (typValue == ByteIdMap.DATATYPE_CHECK) {
						int len = in.getInt();
						if (len > 0) {
							if (in.remaining() != len)
								throw new RuntimeException("DecodeExpeption - Remaining:" + in.remaining() + "!="+len);
						}
						typValue = in.get();
					}
					Object value = getDecodeObject(typValue, in);
					if (value != null) {
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
	 * @param typValue the typ value
	 * @param in the in
	 * @return the decode object
	 */
	public Object getDecodeObject(byte typValue, ByteBuffer in) {
		if (in.remaining() < 1) {
			return null;
		} else if (typValue == ByteIdMap.DATATYPE_BYTE) {
			return new Byte(in.get());
		} else if (typValue == ByteIdMap.DATATYPE_CHAR) {
			return new Character(in.getChar());
		} else if (typValue == ByteIdMap.DATATYPE_SHORT) {
			return new Short(in.getShort());
		} else if (typValue == ByteIdMap.DATATYPE_INTEGER) {
			return new Integer(in.getInt());
		} else if (typValue == ByteIdMap.DATATYPE_LONG) {
			return new Long(in.getLong());
		} else if (typValue == ByteIdMap.DATATYPE_FLOAT) {
			return new Float(in.getFloat());
		} else if (typValue == ByteIdMap.DATATYPE_DOUBLE) {
			return new Double(in.getDouble());
		} else if (typValue == ByteIdMap.DATATYPE_DATE) {
			return new Date(new Long(in.getInt()).longValue());
		}else if (typValue == ByteIdMap.DATATYPE_CLAZZ) {
			int len=in.get()-ByteIdMap.SPLITTER;
			byte[] values = new byte[len];
			in.get(values);
			return new String(values);
		} else {
			byte group=getTyp(typValue, ByteIdMap.DATATYPE_STRING);
			if(group==ByteIdMap.DATATYPE_STRING||
					group==ByteIdMap.DATATYPE_BYTEARRAY||
					group==ByteIdMap.DATATYPE_LIST||
					group==ByteIdMap.DATATYPE_MAP){
				byte subgroup=getTyp(ByteIdMap.DATATYPE_STRING, typValue);
				byte[] values;
				int len=0;
				if(subgroup == ByteIdMap.DATATYPE_STRINGSHORT) {
					len = in.get()-ByteIdMap.SPLITTER;
				}else if(subgroup == ByteIdMap.DATATYPE_STRING) {
					len = in.get();
				} else if (subgroup == ByteIdMap.DATATYPE_STRINGMID) {
					len = in.getShort();
				} else if (subgroup == ByteIdMap.DATATYPE_STRINGBIG) {
					len = in.getInt();
				} else if (subgroup == ByteIdMap.DATATYPE_STRINGLAST) {
					len = in.remaining();
				}
				values = new byte[len];
				in.get(values);
				if(group==ByteIdMap.DATATYPE_STRING){
					return new String(values);
				} else if (group == ByteIdMap.DATATYPE_BYTEARRAY) {
					return values;
				} else if (group == ByteIdMap.DATATYPE_LIST) {
					ByteBuffer child=ByteBuffer.wrap(values);
					ArrayList<Object> list = new ArrayList<Object>();
					while (child.remaining() > 0) {
						byte subType = child.get();
						Object entity = getDecodeObject(subType, child);
						if (entity != null) {
							list.add(entity);
						}
					}
					return list;
				} else if (typValue == ByteIdMap.DATATYPE_MAP) {
					ByteBuffer child=ByteBuffer.wrap(values);
					HashMap<Object, Object> map = new HashMap<Object, Object>();
					while (child.remaining() > 0) {
						byte subType = child.get();
						Object key = getDecodeObject(subType, child);
						if (key != null) {
							subType = child.get();
							Object value = getDecodeObject(subType, child);
							map.put(key, value);
						}
					}
					return map;
				}
			}
		}
		return null;
	}
	
	/**
	 * Gets the typ.
	 *
	 * @param group the group
	 * @param subgroup the subgroup
	 * @return the typ
	 */
	private static byte getTyp(byte group, byte subgroup){
		byte returnValue=(byte) ((group/16)*16);
		return (byte) (returnValue+(subgroup%16));
	}
}