package de.uniks.networkparser.graph;

/*
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
import java.util.HashSet;
import java.util.LinkedHashSet;
import de.uniks.networkparser.graph.util.FeatureSet;
import de.uniks.networkparser.list.SimpleSet;

public enum Feature {
	PROPERTYCHANGESUPPORT, 
	PATTERNOBJECT, 
	SERIALIZATION, 
	SETCLASS, 
	REMOVEYOUMETHOD, 
	JUNIT, 
	STANDALONE, 
	EMFSTYLE, 
	CODESTYLE, 
	DIFFERENCE_BEHAVIOUR, 
	METADATA,
	DOCUMENTATION,
	SOURCECODE,
	GENCODE;

	public static final String CODESTYLE_STANDARD = "standard";
	public static final String CODESTYLE_DIVIDED = "divided";

	public static final HashSet<FeatureProperty> getNone() {
		return new HashSet<FeatureProperty>();
	}

	public static FeatureSet getAll() {
		FeatureSet result = new FeatureSet().with(PROPERTYCHANGESUPPORT, PATTERNOBJECT, SERIALIZATION, REMOVEYOUMETHOD, SETCLASS);
		result.add(CODESTYLE.create().withStringValue(CODESTYLE_STANDARD));
		return result;
	}

	public static FeatureSet getStandAlone() {
		FeatureSet result = new FeatureSet().with(PROPERTYCHANGESUPPORT, STANDALONE, REMOVEYOUMETHOD);
		result.add(SETCLASS.create().withClazzValue(LinkedHashSet.class));
		result.add(CODESTYLE.create().withStringValue(CODESTYLE_STANDARD));
		return result;
	}

	public final FeatureProperty create() {
		if(this==Feature.SETCLASS) {
			return new FeatureProperty(this).withClazzValue(SimpleSet.class);
		}
		return new FeatureProperty(this);
	}
}
