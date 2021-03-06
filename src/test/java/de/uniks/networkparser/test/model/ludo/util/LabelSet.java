/*
   Copyright (c) 2018 Stefan
   
   Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
   and associated documentation files (the "Software"), to deal in the Software without restriction, 
   including without limitation the rights to use, copy, modify, merge, publish, distribute, 
   sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is 
   furnished to do so, subject to the following conditions: 
   
   The above copyright notice and this permission notice shall be included in all copies or 
   substantial portions of the Software. 
   
   The Software shall be used for Good, not Evil. 
   
   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING 
   BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
   DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
 */

package de.uniks.networkparser.test.model.ludo.util;

import java.util.Collection;
import java.util.Collections;

import de.uniks.networkparser.list.ObjectSet;
import de.uniks.networkparser.list.SimpleSet;
import de.uniks.networkparser.test.model.ludo.Field;
import de.uniks.networkparser.test.model.ludo.Label;

public class LabelSet extends SimpleSet<Label> {
	public Class<?> getTypClass() {
		return Label.class;
	}

	public LabelSet() {
		// empty
	}

	public LabelSet(Label... objects) {
		for (Label obj : objects) {
			this.add(obj);
		}
	}

	public LabelSet(Collection<Label> objects) {
		this.addAll(objects);
	}

	public static final LabelSet EMPTY_SET = new LabelSet().withFlag(LabelSet.READONLY);

	public String getEntryType() {
		return "org.sdmlib.test.examples.ludo.model.Label";
	}

	@Override
	public LabelSet getNewList(boolean keyValue) {
		return new LabelSet();
	}

	@SuppressWarnings("unchecked")
	public LabelSet with(Object value) {
		if (value == null) {
			return this;
		} else if (value instanceof java.util.Collection) {
			this.addAll((Collection<Label>) value);
		} else if (value != null) {
			this.add((Label) value);
		}

		return this;
	}

	public LabelSet without(Label value) {
		this.remove(value);
		return this;
	}

	/**
	 * Loop through the current set of Label objects and collect a list of the name
	 * attribute values.
	 * 
	 * @return List of String objects reachable via name attribute
	 */
	public ObjectSet getName() {
		ObjectSet result = new ObjectSet();

		for (Label obj : this) {
			result.add(obj.getName());
		}

		return result;
	}

	/**
	 * Loop through the current set of Label objects and collect those Label objects
	 * where the name attribute matches the parameter value.
	 * 
	 * @param value Search value
	 * 
	 * @return Subset of Label objects that match the parameter
	 */
	public LabelSet createNameCondition(String value) {
		LabelSet result = new LabelSet();

		for (Label obj : this) {
			if (value.equals(obj.getName())) {
				result.add(obj);
			}
		}

		return result;
	}

	/**
	 * Loop through the current set of Label objects and collect those Label objects
	 * where the name attribute is between lower and upper.
	 * 
	 * @param lower Lower bound
	 * @param upper Upper bound
	 * 
	 * @return Subset of Label objects that match the parameter
	 */
	public LabelSet createNameCondition(String lower, String upper) {
		LabelSet result = new LabelSet();

		for (Label obj : this) {
			if (lower.compareTo(obj.getName()) <= 0 && obj.getName().compareTo(upper) <= 0) {
				result.add(obj);
			}
		}

		return result;
	}

	/**
	 * Loop through the current set of Label objects and assign value to the name
	 * attribute of each of it.
	 * 
	 * @param value New attribute value
	 * 
	 * @return Current set of Label objects now with new attribute values.
	 */
	public LabelSet withName(String value) {
		for (Label obj : this) {
			obj.setName(value);
		}

		return this;
	}

	/**
	 * Loop through the current set of Label objects and collect a set of the Field
	 * objects reached via field.
	 * 
	 * @return Set of Field objects reachable via field
	 */
	public SimpleSet<Field> getField() {
		SimpleSet<Field> result = new SimpleSet<Field>();

		for (Label obj : this) {
			result.with(obj.getField());
		}

		return result;
	}

	/**
	 * Loop through the current set of Label objects and collect all contained
	 * objects with reference field pointing to the object passed as parameter.
	 * 
	 * @param value The object required as field neighbor of the collected results.
	 * 
	 * @return Set of Field objects referring to value via field
	 */
	public LabelSet filterField(Object value) {
		ObjectSet neighbors = new ObjectSet();

		if (value instanceof Collection) {
			neighbors.addAll((Collection<?>) value);
		} else {
			neighbors.add(value);
		}

		LabelSet answer = new LabelSet();

		for (Label obj : this) {
			if (!Collections.disjoint(neighbors, obj.getField())) {
				answer.add(obj);
			}
		}

		return answer;
	}

	/**
	 * Loop through current set of ModelType objects and attach the Label object
	 * passed as parameter to the Field attribute of each of it.
	 * 
	 * @return The original set of ModelType objects now with the new neighbor
	 *         attached to their Field attributes.
	 */
	public LabelSet withField(Field value) {
		for (Label obj : this) {
			obj.withField(value);
		}

		return this;
	}

	/**
	 * Loop through current set of ModelType objects and remove the Label object
	 * passed as parameter from the Field attribute of each of it.
	 * 
	 * @return The original set of ModelType objects now without the old neighbor.
	 */
	public LabelSet withoutField(Field value) {
		for (Label obj : this) {
			obj.withoutField(value);
		}

		return this;
	}

}
