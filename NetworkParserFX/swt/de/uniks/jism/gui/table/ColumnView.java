package de.uniks.jism.gui.table;

/*
 Json Id Serialisierung Map
 Copyright (c) 2011 - 2013, Stefan Lindel
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 1. Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 3. All advertising materials mentioning features or use of this software
 must display the following acknowledgement:
 This product includes software developed by Stefan Lindel.
 4. Neither the name of contributors may be used to endorse or promote products
 derived from this software without specific prior written permission.

 THE SOFTWARE 'AS IS' IS PROVIDED BY STEFAN LINDEL ''AS IS'' AND ANY
 EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL STEFAN LINDEL BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

import org.eclipse.swt.widgets.Composite;
import de.uniks.jism.gui.GUIPosition;

/**
 * DummyClazz for Window Designer
 * @author Stefan
 *
 */
public class ColumnView extends Composite{
	private Column column=new Column();
	private TableComponent owner;

	public ColumnView(TableComponent owner, int style){
		super((Composite) owner.getTableComponent(), style);
		this.setLayoutData(GUIPosition.SOUTH);
		this.owner = owner;
	}
	
	public Column getColumn() {
		return column;
	}

	public String getAttributeName() {
		return column.getAttrName();
	}

	public void setAttributeName(String value) {
		column.withAttrName(value);
	}

	public String getLabel() {
		return column.getLabel();
	}

	public void setLabel(String label) {
		column.withLabel(label);
		finish();
		
	}
	public void finish(){
		owner.addColumn(this.column);
//		this.dispose();
	}
	public void setBounds (int x, int y, int width, int height) {
	}
}