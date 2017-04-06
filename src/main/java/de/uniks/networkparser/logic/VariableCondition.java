package de.uniks.networkparser.logic;

import java.util.Map;

import de.uniks.networkparser.buffer.CharacterBuffer;
import de.uniks.networkparser.interfaces.ObjectCondition;
import de.uniks.networkparser.interfaces.ParserCondition;
import de.uniks.networkparser.list.SimpleKeyValueList;

public class VariableCondition implements ParserCondition{
	private CharSequence value;
    private boolean expression;

	@Override
	public boolean update(Object value) {
		if(value instanceof ObjectCondition) {
			return ((ObjectCondition)value).update(this);
		}
		if(value instanceof Map<?,?>) {
			Map<?, ?> collection = (Map<?, ?>) value;
			Object object = collection.get(this.value.toString());
			return  object != null && !object.equals("");
		}
		if(this.value == null) {
			return value == null;
		}
		return this.value.equals(value);
	}

	public VariableCondition withValue(CharSequence value) {
		this.value = value;
		return this;
	}
	public CharSequence getValue(SimpleKeyValueList<String, String> variables) {
		if(variables != null && this.value != null) {
			return variables.get(this.value);
		}
		return null;
	}

    public VariableCondition withExpression(boolean value) {
        this.expression = value;
        return this;
    }
    
    public static VariableCondition create(CharSequence sequence, boolean expression) {
        return new VariableCondition().withValue(sequence).withExpression(expression);
	}
	
	@Override
	public ParserCondition create(CharacterBuffer buffer) {
		return create(buffer);
	}

    @Override
    public boolean isExpression() {
        return expression;
    }
	@Override
	public String getKey() {
		return null;
	}

}