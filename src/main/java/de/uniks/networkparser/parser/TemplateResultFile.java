package de.uniks.networkparser.parser;

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
import de.uniks.networkparser.buffer.CharacterBuffer;
import de.uniks.networkparser.ext.ClassModel;
import de.uniks.networkparser.graph.Clazz;
import de.uniks.networkparser.graph.GraphEntity;
import de.uniks.networkparser.graph.GraphMember;
import de.uniks.networkparser.graph.GraphSimpleSet;
import de.uniks.networkparser.graph.GraphUtil;
import de.uniks.networkparser.graph.ModifyEntry;
import de.uniks.networkparser.graph.SourceCode;
import de.uniks.networkparser.interfaces.LocalisationInterface;
import de.uniks.networkparser.interfaces.SendableEntityCreator;
import de.uniks.networkparser.list.SimpleSet;
import de.uniks.networkparser.list.SortedSet;
import de.uniks.networkparser.parser.generator.BasicGenerator;

public class TemplateResultFile extends SortedSet<TemplateResultFragment> implements SendableEntityCreator, LocalisationInterface {
	public static final String PROPERTY_PARENT="parent";
	public static final String PROPERTY_CHILD="child";
	public static final String PROPERTY_MEMBER="member";

	public static final String PROPERTY_NAME="name";
	public static final String PROPERTY_HEADERS="headers";
	private String name;
	private String postfix;
	private String extension;
	private String path;
	private SendableEntityCreator parent;
	private GraphEntity member;
	private boolean metaModell;

	TemplateResultFile() {
		super(true);
	}

	public TemplateResultFile(GraphEntity clazz, boolean comparator) {
		super(comparator);
		this.withName(clazz);
		this.withMember(clazz);
	}

	public TemplateResultFile(GraphEntity clazz, String name, boolean comparator) {
		super(comparator);
		this.withName(name);
		this.withMember(clazz);
	}

	public String getName() {
		return name;
	}

	public String getFileName() {
		CharacterBuffer buffer=new CharacterBuffer();
		if(path != null) {
			buffer.with(this.path);
			buffer.with('/');
		}
		buffer.with(this.name);
		buffer.with(this.postfix);
		buffer.with('.');
		buffer.with(this.extension);
		return buffer.toString();
	}

	public TemplateResultFile withName(String name) {
		this.name = name;
		return this;
	}

	public TemplateResultFile withName(GraphEntity clazz) {
		this.name = clazz.getName().replace(".", "/");
		return this;
	}

	public TemplateResultFile withPostfix(String value) {
		this.postfix = value;
		return this;
	}

	public TemplateResultFile withExtension(String value) {
		this.extension = value;
		return this;
	}
	public TemplateResultFile withPath(String value) {
		this.path = value;
		return this;
	}

	public boolean addChild(SendableEntityCreator child) {
		//FIXME FOR NON COMPARATOR
		if(isComparator() == false && child instanceof TemplateResultFragment) {
			TemplateResultFragment fragment = (TemplateResultFragment) child;
			if(fragment.getKey() == Template.TEMPLATE) {
				super.add(0, fragment);
				return true;
			}
		}
		if(super.add(child) == false) {
			return false;
		}
		child.setValue(child, PROPERTY_PARENT, this, SendableEntityCreator.NEW);
		return true;
	}

	@Override
	public String getText(CharSequence label, Object model, Object gui) {
		return null;
	}

	@Override
	public String put(String label, Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean setParent(SendableEntityCreator value) {
		if(value != this.parent) {
			this.parent = value;
			return true;
		}
		return false;
	}

	public SendableEntityCreator getParent() {
		return this.parent;
	}

	@Override
	public Object getSendableInstance(boolean prototyp) {
		return new TemplateResultFile();
	}

	@Override
	public String[] getProperties() {
		return new String[] {PROPERTY_NAME, PROPERTY_PARENT};
	}

	@Override
	public Object getValue(Object entity, String attribute) {
		if(entity instanceof TemplateResultFile == false) {
			return null;
		}
		TemplateResultFile element = (TemplateResultFile) entity;
		int pos = attribute.indexOf('.');
		String attrName;
		if(pos>0) {
			attrName = attribute.substring(0, pos);
		}else {
			attrName = attribute;
		}
		if(PROPERTY_PARENT.equalsIgnoreCase(attrName)) {
			if(pos>0) {
				return element.getParent().getValue(element, attribute.substring(pos+1));
			}
			return element.getParent();
		}
		if(PROPERTY_MEMBER.equalsIgnoreCase(attrName)) {
			GraphEntity member = element.getMember();
			if(pos > 0) {
				return member.getValue(attribute.substring(pos+1));
			}
			return member;
		}
		if(PROPERTY_HEADERS.equalsIgnoreCase(attrName)) {
			SimpleSet<String> headers=new SimpleSet<String>();
			for(TemplateResultFragment child : this) {
				headers.addAll(child.getHeaders());
			}
			return headers;
		}

		return null;
	}

	@Override
	public boolean setValue(Object entity, String attribute, Object value, String type) {
		if(PROPERTY_PARENT.equalsIgnoreCase(attribute)) {
			return this.setParent((SendableEntityCreator) value);
		}
		if(PROPERTY_CHILD.equalsIgnoreCase(attribute)) {
			return this.addChild((SendableEntityCreator) value);
		}
		return false;
	}

	public SourceCode getCode() {
		if(this.member instanceof Clazz) {
			Clazz clazz = (Clazz) this.member;
			GraphMember code = clazz.getChildByName(SourceCode.NAME, SourceCode.class);
			if(code != null && code instanceof SourceCode) {
				return (SourceCode) code;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		SourceCode code = getCode();
		if(this.size() < 1 ) {
			if(code != null) {
				return code.toString();
			}
		}
		// ADD CODE
		// Check for Existing
		if(code != null && isMetaModell()) {
			StringBuilder sb=new StringBuilder();
			sb.append(code.getContent().toString());
//			buffer = code.getContent();
			TemplateResultFragment importDecl = null;
			// REMVOE OLD SOURCE
			GraphSimpleSet children = GraphUtil.getChildren(this.member);

			for(GraphMember member : children) {
				if(member instanceof ModifyEntry == false) {
					continue;
				}
				ModifyEntry modifierChild = (ModifyEntry) member;
				if(ModifyEntry.TYPE_DELETE.equalsIgnoreCase(modifierChild.getType())) {
					GraphMember entry = modifierChild.getEntry();
					if(entry == null) {
						continue;
					}
					SymTabEntry symbolEntry = code.getSymbolEntry(entry.getClass().getSimpleName(), entry.getName());
					if(symbolEntry != null) {
						sb.replace(symbolEntry.getStartPos(), symbolEntry.getEndPos(), "");
					}
				}
			}
			for(TemplateResultFragment fragment : this) {
				if(fragment.getKey() == Template.DECLARATION) {
					continue;
				}
				if(fragment.getKey() == Template.IMPORT) {
					// EVALUATION IMPORT
//					TextItems
					fragment.update();
					importDecl = fragment;
					continue;
				}
				if(fragment.getName() != null) {
					if(SymTabEntry.TYPE_METHOD.equalsIgnoreCase(fragment.getName())) {
						SymTabEntry symbolEntry = code.getSymbolEntry(fragment.getName(), fragment.getMember().getName());
						if(symbolEntry == null) {
							int pos = code.getEndOfBody();
							sb.replace(pos, pos, fragment.getValue().toString());
						}
					}
				} else if(fragment.getKey() == Template.VALUE){
					SymTabEntry symbolEntry = code.getSymbolEntry("ATTRIBUTE", fragment.getMember().getName());
					if(symbolEntry == null) {
						int pos = code.getEndOfBody();
						sb.replace(pos, pos, fragment.getValue().toString());
					}
//					sb.append(fragment.getValue().toString());
				}
			}

			if(importDecl != null) {
				int start = code.getStartOfImports();
				int end = code.getEndOfImports();
//FIXME STEFAN				buffer.replace(start, end, importDecl.getValue().toString());
			}
			return sb.toString();
		}
		CharacterBuffer buffer = new CharacterBuffer();
		for(TemplateResultFragment fragment : this) {
			if(fragment.getKey() == Template.DECLARATION) {
				continue;
			}
			if(fragment.getKey() == Template.IMPORT) {
				// EVALUATION IMPORT
//				TextItems
				fragment.update();
			}
			buffer.with(fragment.getValue());
		}
		return buffer.toString();
	}

	public GraphEntity getMember() {
		return member;
	}

	public TemplateResultFile withMember(GraphEntity member) {
		this.member = member;
		return this;
	}

	public TemplateResultFile withMetaModel(boolean value) {
		this.metaModell = value;
		return this;
	}

	public boolean isMetaModell() {
		return metaModell;
	}

	public static TemplateResultFile createJava(Clazz clazz) {
		TemplateResultFile templateResult = new TemplateResultFile(clazz, true);
		templateResult.withExtension(BasicGenerator.TYPE_JAVA);
		templateResult.withPath((String) clazz.getClassModel().getValue(ClassModel.PROPERTY_PATH));
		return templateResult;
	}
}
