package de.uniks.networkparser.ext.io;

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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import de.uniks.networkparser.IdMap;
import de.uniks.networkparser.buffer.Buffer;
import de.uniks.networkparser.buffer.ByteBuffer;
import de.uniks.networkparser.buffer.CharacterBuffer;
import de.uniks.networkparser.interfaces.BaseItem;
import de.uniks.networkparser.json.JsonArray;
import de.uniks.networkparser.json.JsonObject;
import de.uniks.networkparser.xml.XMLEntity;

public class FileBuffer extends Buffer {
	public static final int BUFFER=4096;
	private BufferedReader reader;
	private File file;
	private CharacterBuffer lookAHead = new CharacterBuffer();
	private int length;
	private char currentChar;
	public static byte NONE=0;
	public static byte APPEND=1;
	public static byte OVERRIDE=2;

	public FileBuffer withFile(String fileName) {
		if(fileName != null) {
			withFile(new File(fileName));
		}
		return this;
	}

	public FileBuffer withFile(File file, int cache) {
		this.file = file;
		this.length = (int) this.file.length();
		try {
			FileInputStream fis = new FileInputStream(this.file);
			InputStreamReader isr = new InputStreamReader(fis, Charset.forName(BaseItem.ENCODING));
			this.reader = new BufferedReader(isr, cache);
		}catch (Exception e) {
		}
		this.position = 0;
		return this;
	}

	public FileBuffer withFile(File file) {
		return withFile(file, 1024*1024);
	}

	@Override
	public int length() {
		return length;
	}

	public boolean exists() {
		if(this.file == null) {
			return false;
		}
		return this.file.exists();
	}

	@Override
	public char getChar() {
		char value = 0;
		if(lookAHead.length() > 0) {
			value = lookAHead.charAt(0);
			if(lookAHead.length() == 1) {
				lookAHead.clear();
			}else {
				lookAHead.trimStart(1);
			}
			this.position++;
			return value;
		}
		try {
			int charInt = this.reader.read();
			if(charInt<0) {
				charInt =0;
			}
			value = (char)charInt;
			this.currentChar = value;
			position++;
		} catch (IOException e) {
		}
		return value;
	}

	@Override
	public String toString() {
		char[] values = new char[remaining()];
		int len = lookAHead.length();
		if(len>0) {
			for(int i = 0;i<len;i++) {
				values[i] = lookAHead.charAt(i);
			}
		}
		try {
			if(values.length==0) {
				return "";
			}
			int max = values.length - len;
			int read = this.reader.read(values, len, max);
			if(read<max) {
				this.length = (max -read);
			}
			this.lookAHead.clear();
			this.lookAHead.with(values, 0, len+read);
//			this.position = this.length();
		} catch (IOException e) {
		}
		return new String(values);
	}

	@Override
	public FileBuffer withLookAHead(CharSequence lookahead) {
		this.lookAHead.set(lookahead);
		this.currentChar = lookahead.charAt(0);
		this.lookAHead.trimStart(1);
		this.position -= this.lookAHead.length();
		return this;
	}

	@Override
	public FileBuffer withLookAHead(char current) {
		this.lookAHead.set(this.currentChar);
		this.currentChar = current;
		this.position--;
		return this;
	}

	@Override
	public char nextClean(boolean currentValid) {
		char current = super.nextClean(currentValid);
		this.currentChar = current;
		return current;
	}

	@Override
	public char getCurrentChar() {
		if(currentChar != 0) {
			return currentChar;
		}
		char value = getChar();
		return value;
	}

	public byte getByte() {
		return (byte)getChar();
	}

	public void close() {
		try {
			if(this.reader != null) {
				this.reader.close();
			}
		} catch (IOException e) {
		}
	}

	public static final boolean writeFile(String fileName, CharSequence data, byte flag) {
		if(data != null) {
			return writeFile(fileName, data.toString().getBytes(), flag);
		}
		return false;
	}

	public static final boolean writeFile(String fileName, byte[] data, byte flag) {
		if(fileName == null || fileName.length()<1) {
			return false;
		}
		FileBuffer buffer = new FileBuffer();
		buffer.withFile(fileName);
		if(buffer.exists()) {
			if(flag==NONE) {
				return false;
			}
		} else {
			if(buffer.createFile() == false) {
				return false;
			}
		}
		return buffer.write(flag, data);
	}
	public static final boolean writeFile(String fileName, CharSequence data) {
		return writeFile(fileName, data, OVERRIDE);
	}
	
	public static final boolean writeFile(String fileName, byte[] data) {
		return writeFile(fileName, data, OVERRIDE);
	}

	public static final CharacterBuffer readResource(String file) {
		InputStream is = IdMap.class.getResourceAsStream(file);
		CharacterBuffer sb = new CharacterBuffer();
		if (is != null) {
			final byte[] buffer = new byte[BUFFER];
			try {
				int read;
				do {
					read = is.read(buffer);
					if(read>0) {
						sb.with(new String(buffer, 0, read, BaseItem.ENCODING));
					}
				} while (read>=0);
			} catch (IOException e) {
			} finally {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
		return sb;
	}

	public static final CharacterBuffer readFile(String file) {
		return readFile(new File(file));
	}
	public static final CharacterBuffer readFile(File file) {
		CharacterBuffer sb = new CharacterBuffer();
		if(file.exists()){
			final byte[] buffer = new byte[BUFFER];
			int read;
			FileInputStream is = null;
			try {
				is = new FileInputStream(file);
				do {
					read = is.read(buffer, 0, buffer.length);
					if (read>0) {
						sb.with(new String(buffer, 0, read, BaseItem.ENCODING));
					}
				} while (read>=0);
			} catch (IOException e) {
			} finally {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
		return sb;
	}
	
	public static final ByteBuffer readBinaryFile(String file) {
		File content=new File(file);
		ByteBuffer sb = new ByteBuffer();
		if(content.exists()){
			final byte[] buffer = new byte[BUFFER];
			int read;
			FileInputStream is = null;
			try {
				is = new FileInputStream(content);
				do {
					read = is.read(buffer, 0, buffer.length);
					if (read>0) {
						sb.addBytes(buffer,  read, false);
					}
				} while (read>=0);
			} catch (IOException e) {
			} finally {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
		return sb;
	}


	public static BaseItem readBaseFile(String configFile){
		return readBaseFile(configFile, null);
	}

	public static BaseItem readBaseFile(String configFile, BaseItem container){
		// load it
		CharacterBuffer buffer = FileBuffer.readFile(configFile);
		if (buffer != null&&buffer.length()>0)
		{
			if(buffer.charAt(0)=='{'){
				JsonObject result;
				if(container instanceof JsonObject ) {
					result = (JsonObject) container;
				}else {
					result = new JsonObject();
				}
				result.withValue(buffer);
				if(buffer.isEnd()) {
					return result;
				}
				//buffer not at end
				JsonArray array = new JsonArray();
				array.add(result);
				while(buffer.isEndCharacter() == false) {
					result = new JsonObject();
					result.withValue(buffer);
					array.add(result);
				}
				return array;
			}
			if(buffer.charAt(0)=='['){
				JsonArray result;
				if(container instanceof JsonArray ) {
					result = (JsonArray) container;
				}else {
					result = new JsonArray();
				}
				return result.withValue(buffer);
			}
			if(buffer.charAt(0)=='<'){
				XMLEntity result;
				if(container instanceof XMLEntity ) {
					result = (XMLEntity) container;
				}else {
					result = new XMLEntity();
				}
				return result.withValue(buffer);
			}
		}
		return container;
	}

	public static final boolean deleteFile(String fileName) {
		File file;
		file = new File(fileName);

		if (file.exists()) {
			return file.delete();
		}
		return true;
	}

	public boolean createFile() {
		return FileBuffer.createFile(this.file);
	}

	public static boolean createFile(File file) {
		if(file == null) {
			return false;
		}
		File parentFile = file.getParentFile();
		if(parentFile == null || parentFile.exists()) {
			return true;
		}
		if(parentFile.mkdirs() == false) {
			return false;
		}
		try {
			return file.createNewFile();
		} catch (IOException e) {
		}
		return false;
	}

	public boolean write(byte flag, CharSequence data) {
		if(data != null) {
			return write(flag, data.toString().getBytes());
		}
		return false;
	}
	public boolean write(byte flag, byte... data) {
		if(this.file == null) {
			return false;
		}
		try {
			boolean append = false;
			if(flag==APPEND) {
				append = true;
			}
			FileOutputStream os = new FileOutputStream(this.file, append);
			os.write(data);
			os.flush();
			os.close();
			return true;
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return false;
	}

	public boolean println(CharSequence string) {
		this.write(APPEND, string);
		return newline();
	}

	public boolean newline() {
		return this.write(APPEND, BaseItem.CRLF);
	}
}
