package de.uniks.networkparser.test.generator;

import de.uniks.networkparser.ext.ClassModel;
import de.uniks.networkparser.graph.Cardinality;
import de.uniks.networkparser.graph.Clazz;
import de.uniks.networkparser.graph.DataType;

public class testGen {

//	@Test
	public void testModel() {
		ClassModel model = new ClassModel("org.networkparser.simple.model");
		Clazz person = model.createClazz("Person");
		Clazz room = model.createClazz("Room");
		person.createAttribute("name", DataType.STRING);
		person.createAttribute("age", DataType.INT);

		room.withMethod("init", DataType.VOID);

		person.withBidirectional(room, "room", Cardinality.ONE, "persons", Cardinality.MANY);

		model.generate("gen");
	}
}
