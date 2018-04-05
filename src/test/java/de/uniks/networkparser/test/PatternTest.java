package de.uniks.networkparser.test;

import org.junit.Test;

import de.uniks.networkparser.graph.Pattern;
import de.uniks.networkparser.test.model.ferryman.Bank;
import de.uniks.networkparser.test.model.ferryman.Boat;
import de.uniks.networkparser.test.model.ferryman.River;
import de.uniks.networkparser.test.model.ludo.Field;
import de.uniks.networkparser.test.model.ludo.Label;
import de.uniks.networkparser.test.model.ludo.Ludo;
import de.uniks.networkparser.test.model.ludo.Pawn;
import de.uniks.networkparser.test.model.ludo.util.LudoCreator;
import de.uniks.networkparser.test.model.util.RiverCreator;

public class PatternTest {
	@Test
	public void testFerryMan() {
		// StartSituation
		River river = new River();
		Boat boat = river.createBoat();
		Bank left = river.createBanks().withName("left").withBoat(boat);

		left.createCargos().withName("cabbage");
		left.createCargos().withName("goat");
		left.createCargos().withName("wolf");

		river.createBanks().withName("right");

//		List<Object> allMatches = new Pattern(river).has(River.PROPERTY_BANKS).has(Bank.PROPERTY_CARGOS).allMatches();
		Pattern pattern = new Pattern(RiverCreator.createIdMap("42"), river).has(River.PROPERTY_BANKS).has(Bank.PROPERTY_CARGOS);
//		pattern.withMatch(candidate)
		for (Object matchPattern : pattern) {
			System.out.println(pattern);
		}

//		System.out.println(allMatches);
//		for (Object found : allMatches) {
//			Cargo cargo = (Cargo) found;
//			System.out.println(cargo.getName());
//		}
	}

	@Test
	public void testPattern() {
		Ludo ludo = new Ludo();
		Field field = ludo.createFields().withName("1");
		field.createPawns().withColor("1-1");
		field.createPawns().withColor("1-2");
		field.createPawns().withColor("1-3");

		field = ludo.createFields().withName("2");
		field.createPawns().withColor("2-1");
		field.createPawns().withColor("2-2");
		field.createPawns().withColor("2-3");
		field.createPawns().withColor("2-4");

		field = ludo.createFields().withName("3");
		field.createPawns().withColor("3-1");
		field.createPawns().withColor("3-2");
		field.createPawns().withColor("3-3");
		field.createPawns().withColor("3-4");

		
		Pattern ludoPO = new Pattern(LudoCreator.createIdMap("42"), ludo);
		Pattern fieldPO = ludoPO.has(Ludo.PROPERTY_FIELDS);
		Pattern pawnPO = fieldPO.has(Field.PROPERTY_PAWNS);
		while(ludoPO.hasNext()) {
			System.out.println("LUDO: " + fieldPO.getMatch(Field.class).getName() + " -- " + pawnPO.getMatch(Pawn.class).getColor());
			ludoPO.find();
		}
//		for(Object result : pawnPO) {
//			System.out.println("LUDO: "+result); 
//		}
//		PawnPO pawnPO = fieldPO.createPawnsPO();
//		ludoPO.getCurrentMatch();
		
//		ludoPO.hasNext();
//
//		while (ludoPO.isHasMatch()) {
//			
//		}
		System.out.println("FINISH");
	}

	@Test
	public void testPatternCombi() {
		Ludo ludo = new Ludo();
		Field field = ludo.createFields().withName("1");
		Label labelA = field.createLabel().withName("A");
		Label labelB = field.createLabel().withName("B");
		Label labelC = field.createLabel().withName("C");
		field.createPawns().withColor("1-1");
		field.createPawns().withColor("1-2");
		field.createPawns().withColor("1-3");

		field = ludo.createFields().withName("2");
		field.createPawns().withColor("2-1");
		field.createPawns().withColor("2-2");
		field.createPawns().withColor("2-3");
		field.createPawns().withColor("2-4");
		field.withLabel(labelA);
		field.withLabel(labelB);
		field.withLabel(labelC);

		field = ludo.createFields().withName("3");
		field.withLabel(labelA);
		field.withLabel(labelB);
		field.withLabel(labelC);

//		LudoPO ludoPO = new LudoPO(ludo);
//		FieldPO fieldPO = ludoPO.createFieldsPO();
//		LabelPO labelPO = fieldPO.createLabelPO();
//		PawnPO pawnPO = fieldPO.createPawnsPO();
//
//		while (ludoPO.isHasMatch()) {
//			System.out.println("LUDO: " + fieldPO.getCurrentMatch().getName() + " -- "
//					+ labelPO.getCurrentMatch().getName() + " -- " + pawnPO.getCurrentMatch().getColor());
//			ludoPO.nextMatch();
//		}
	}

	@Test
	public void testPattern2() {
		Ludo ludo = new Ludo();
		Field field = ludo.createFields().withName("1");
		Label labelA = field.createLabel().withName("A");
		Label labelB = field.createLabel().withName("B");
		Label labelC = field.createLabel().withName("C");

		field = ludo.createFields().withName("2");
		field.withLabel(labelA);
		field.withLabel(labelB);
		field.withLabel(labelC);

		field = ludo.createFields().withName("3");
		field.withLabel(labelA);
		field.withLabel(labelB);
		field.withLabel(labelC);

		Pattern ludoPO = new Pattern(LudoCreator.createIdMap("42"), ludo);
		Pattern fieldPO = ludoPO.has(Ludo.PROPERTY_FIELDS);
		Pattern labelPO = fieldPO.has(Field.PROPERTY_LABEL);
		while (ludoPO.hasNext()) {
			System.out.println("LUDO: " + fieldPO.getMatch(Field.class).getName() + " -- " + labelPO.getMatch(Label.class).getName());
			ludoPO.next();
		}
	}

}
