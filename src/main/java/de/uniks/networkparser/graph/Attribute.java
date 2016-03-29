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

public class Attribute extends Value {
	public static final StringFilter<Attribute> NAME = new StringFilter<Attribute>(GraphMember.PROPERTY_NAME);
	public static final String PROPERTY_CLAZZ = "clazz";
	public static final String PROPERTY_VALUE = "value";
	public static final String PROPERTY_VISIBILITY = "visibility";

	Attribute() {
	}

	public Attribute(String name, DataType datatyp) {
		this.with(name);
		this.with(datatyp);
	}

	@Override
	public Attribute with(Class<?> value) {
		super.with(value);
		return this;
	}

	@Override
	public Attribute withValue(String value) {
		super.withValue(value);
		return this;
	}

	@Override
	public Modifier getModifier() {
		Modifier modifier = super.getModifier();
		if(modifier == null) {
			modifier = new Modifier(Modifier.PRIVATE.getName());
			super.withChildren(modifier);
		}
		return modifier;
	}

	public Attribute with(Modifier... modifier) {
		super.withModifier(modifier);
		return this;
	}

	public Attribute without(Modifier... modifier) {
		getModifier().without(modifier);
		return this;
	}

	public Attribute without(Annotation... annotation) {
		super.without(annotation);
		return this;
	}

	public Clazz getClazz() {
		return (Clazz) parentNode;
	}

	// Redirect
	@Override
	public Attribute with(String value) {
		super.with(value);
		return this;
	}

	@Override
	public Attribute with(DataType value) {
		super.with(value);
		return this;
	}

	@Override
	public Attribute with(Clazz value) {
		super.with(value);
		return this;
	}

	public Attribute with(String name, DataType typ) {
		this.with(typ);
		this.with(name);
		return this;
	}

	public Attribute with(String name, Clazz typ) {
		this.with(typ);
		this.with(name);
		return this;
	}


	public String getValue(String typ, boolean shortName) {
		if (typ.equals(GraphTokener.OBJECT)) {
			if(DataType.STRING == this.type && !this.value.startsWith("\"")){
				return "\""+ this.value + "\"";
			}
			return this.value;
		}
		return getType(shortName);
	}

	public Annotation getAnnotation() {
		return super.getAnnotation();
	}

	public Attribute with(Annotation value) {
		super.withAnnotaion(value);
		return this;
	}
}