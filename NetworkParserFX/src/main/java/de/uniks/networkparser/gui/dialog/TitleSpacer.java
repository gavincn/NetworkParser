package de.uniks.networkparser.gui.dialog;

/*
 NetworkParser
 Copyright (c) 2011 - 2014, Stefan Lindel
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class TitleSpacer extends Region implements DialogElement{
	private DialogBox owner;

	public TitleSpacer() {
        HBox.setHgrow(this, Priority.ALWAYS);
	}

	@Override
	public DialogElement withOwner(DialogBox value) {
		this.owner = value;
		return this;
	}

	public DialogBox getOwner() {
		return owner;
	}

}