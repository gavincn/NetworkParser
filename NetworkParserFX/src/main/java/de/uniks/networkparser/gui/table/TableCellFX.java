package de.uniks.networkparser.gui.table;

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
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TablePosition;
import javafx.scene.text.Font;
import de.uniks.networkparser.gui.Style;
import de.uniks.networkparser.gui.controls.EditControl;
import de.uniks.networkparser.gui.controls.EditFieldMap;
import de.uniks.networkparser.gui.grid.StyleFX;
import de.uniks.networkparser.interfaces.GUIPosition;
import de.uniks.networkparser.interfaces.SendableEntityCreator;

public class TableCellFX extends TableCell<Object, TableCellValue> implements CellEditorElement{
	private EditFieldMap fieldMap;
	private Column field;
	private EditControl<? extends Node> control;
	private TableComponent tableComponent;

	public TableCellFX withEditFieldMap(EditFieldMap fieldMap) {
		this.fieldMap = fieldMap;
		return this;
	}

	@Override
	public TableCellFX withColumn(Column column) {
		this.field = column;
		if(this.field.getStyle() instanceof StyleFX){
			this.setStyle(this.field.getStyle().toString());
		}
		if(this.field.isListener()){
			this.setCursor(Cursor.HAND);
		}
		
		if(field.getStyle().getAlignment() != null) {
			GUIPosition alignment = GUIPosition.valueOf( field.getStyle().getAlignment() );
			if(alignment != null) {
				if(alignment==GUIPosition.CENTER) {
					this.setAlignment(Pos.CENTER);		
				}else if(alignment==GUIPosition.WEST) {
					this.setAlignment(Pos.CENTER_LEFT);
				}else if(alignment==GUIPosition.EAST) {
					this.setAlignment(Pos.CENTER_RIGHT);
				}
			}
		}
		//explorer.withLabel("Explorer").withStyle(new Style().withWidth(80).withForground("00A18F").withBackground("D4D4D4").withAlignment(GUIPosition.CENTER));
		//explorer.withResizable(false);
		//explorer.withListener(new ExplorerFilesButtonEditingSupport(this));

		
		return this;
	}

	@Override
	protected void updateItem(TableCellValue arg0, boolean empty) {
		super.updateItem(arg0, empty);
		if (arg0 != null) {
			setText("" + arg0);

			if (this.field.getStyle() != null) {
				Style myStyle = this.field.getStyle();
				if (myStyle.getFontFamily() != null
						&& myStyle.getFontSize() != null) {
					setFont(new Font(myStyle.getFontFamily(),
							Integer.valueOf(myStyle.getFontSize())));
				}
				String style = myStyle.toString();
				if(style.startsWith("-")){
					setStyle(style);
				}
			}
		}else if(empty){
			setText("");
		}
	}

	@Override
	public void startEdit() {
		TablePosition<Object, ?> editingCell = getTableView().getEditingCell();
		int row = editingCell.getRow();
		Object entity = tableComponent.getElement(row);
		SendableEntityCreator creator = tableComponent.getCreator(entity);
		
		if (!isEmpty() && this.field.getListener().canEdit(entity, creator)) {
			super.startEdit();
			Object value = getItem().getCreator().getValue(getItem().getItem(), this.field.getAttrName());
			FieldTyp typ = fieldMap.getControllForTyp(field, value);
			control = fieldMap.getControl(typ, field, value, this);
			if(control!=null){
				setText(null);
				setGraphic(control.getControl());
			}
		}else if(this.field.isListener()) {
			this.field.getListener().onEdit(entity, creator);
		}
	}
	
	@Override
	public void commitEdit(TableCellValue arg0) {
		super.commitEdit(arg0);
		apply();
	}
	
	@Override
	public void cancelEdit() {
		super.cancelEdit();
		cancel();
	}
	
	@Override
	public void cancel() {
		setText(""+getItem());
		setGraphic(null);
	}

	@Override
	public boolean nextFocus() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void apply() {
		Object value = control.getValue(false);
		getItem().getColumn().getListener().setValue(this, getItem().getItem(), getItem().getCreator(), value);
		setText(""+value);
		setGraphic(null);		
	}

	@Override
	public Object getValue(boolean convert) {
		return control.getValue(convert);
	}

	@Override
	public TableCellFX withValue(Object value) {
		control.withValue(value);
		return this;
	}
	
	public TableCellFX withTableComponent(TableComponent value) {
		this.tableComponent = value;
		return this;
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean setFocus(boolean value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onActive(boolean value) {
		// TODO Auto-generated method stub
		return false;
	}

	
	
	
//TODO REMOVE
	// @Override
		// public Color getBackground(Object element) {
		// return colors.getColor(column.getBackgroundColor());
		// }
		// @Override
		// public Color getForeground(Object element) {
		// return colors.getColor(column.getForgroundColor());
		// }
		// public Color getForgroundColorActiv() {
		// return colors.getColor(column.getForgroundColorActiv());
		// }
		//
		// public Color getBackgroundColorActiv() {
		// return colors.getColor(column.getBackgroundColorActiv());
		// }
		//
		// @Override
		// public String getToolTipText(Object element) {
		// String altAttribute = column.getAltAttribute();
		// if (altAttribute != null) {
		// if(altAttribute.startsWith("\"")){
		// return altAttribute.substring(1, altAttribute.length()-1);
		// }
		// SendableEntityCreator creatorClass =
		// owner.getMap().getCreatorClass(element);
		// if (creatorClass != null) {
		// String text = ""
		// + creatorClass.getValue(element, altAttribute);
		// if (text.equals("")) {
		// return null;
		// }
		// return text;
		// }
		// }
		// return getTextValue(element);
		// }

}
