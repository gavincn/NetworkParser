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

import de.uniks.networkparser.interfaces.ObjectCondition;
import de.uniks.networkparser.interfaces.SendableEntityCreator;

public class CompareTo implements ObjectCondition, SendableEntityCreator {
	public static final String VALUE = "value";
	public static final String COMPARE = "compare";
	public static final int GREATER = 1;
	public static final int LOWER = -1;
	private Comparable<Object> value;
	private int compare;

	public Comparable<?> getValue() {
		return value;
	}

	public CompareTo withValue(Comparable<Object> value) {
		this.value = value;
		return this;
	}

	public int getCompare() {
		return compare;
	}

	public CompareTo withCompare(int compare) {
		this.compare = compare;
		return this;
	}

	@Override
	public boolean update(Object value) {
		PropertyChangeEvent evt = (PropertyChangeEvent) value;
		Object entityValue = evt.getNewValue();
		if (entityValue != null) {
			if (entityValue instanceof Comparable<?>) {
				Comparable<?> comparatorValue = (Comparable<?>) entityValue;
				if (compare < 0) {
					return this.value.compareTo(comparatorValue) <= compare;
				} else {
					return this.value.compareTo(comparatorValue) >= compare;
				}
			}
		}
		return false;
	}

	@Override
	public String[] getProperties() {
		return new String[] {COMPARE, VALUE };
	}

	@Override
	public Object getSendableInstance(boolean prototyp) {
		return new CompareTo();
	}

	@Override
	public Object getValue(Object entity, String attribute) {
		if (COMPARE.equalsIgnoreCase(attribute)) {
			return ((CompareTo) entity).getCompare();
		}
		if (VALUE.equalsIgnoreCase(attribute)) {
			return ((CompareTo) entity).getValue();
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean setValue(Object entity, String attribute, Object value,
			String type) {
		if (COMPARE.equalsIgnoreCase(attribute)) {
			((CompareTo) entity).withCompare(Integer.parseInt("" + value));
			return true;
		}
		if (VALUE.equalsIgnoreCase(attribute)) {
			if (value instanceof Comparable<?>) {
				((CompareTo) entity).withValue((Comparable<Object>) value);
			}
			return true;
		}
		return false;
	}
}
