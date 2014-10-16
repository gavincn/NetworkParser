package de.uniks.networkparser.graph;

/*
NetworkParser
Copyright (c) 2011 - 2013, Stefan Lindel
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
import java.util.ArrayList;

import de.uniks.networkparser.json.JsonArray;
import de.uniks.networkparser.json.JsonObject;

public class GraphOptions {
	// Options
	public enum TYP{HTML, CANVAS, SVG, PDF};
	public enum RANK{LR, TB};
	private TYP display;
	private Boolean raster;
	private String canvasid;
	private Integer fontSize;
	private String fontFamily;
	private String rank;
	private Integer nodeSep;
	private Boolean infobox;
	private Boolean cardinalityInfo;
	private Boolean propertyInfo;
	private ArrayList<TYP> buttons;

	public JsonObject getJson(){
		JsonObject result = new JsonObject();
		
		result.withValue("display", display);
		result.withValue("raster", raster);
		result.withValue("canvasid", canvasid);
		result.withValue("fontSize", fontSize);
		result.withValue("fontFamily", fontFamily);
		result.withValue("rank", rank);
		result.withValue("nodeSep", nodeSep);
		result.withValue("infobox", infobox);
		result.withValue("cardinalityInfo", cardinalityInfo);
		result.withValue("propertyInfo", propertyInfo);
		if(buttons != null){
			result.withValue("buttons", new JsonArray().withList(buttons));
		}
		return result;
	}

	public Boolean getRaster() {
		return raster;
	}

	public GraphOptions withRaster(Boolean value) {
		this.raster = value;
		return this;
	}

	public String getCanvasid() {
		return canvasid;
	}

	public GraphOptions withCanvasid(String value) {
		this.canvasid = value;
		return this;
	}

	public Integer getFontSize() {
		return fontSize;
	}

	public GraphOptions withFontSize(Integer value) {
		this.fontSize = value;
		return this;
	}

	public String getFontFamily() {
		return fontFamily;
	}

	public GraphOptions withFontFamily(String value) {
		this.fontFamily = value;
		return this;
	}

	public String getRank() {
		return rank;
	}

	public GraphOptions withRank(String value) {
		this.rank = value;
		return this;
	}

	public Integer getNodeSep() {
		return nodeSep;
	}

	public GraphOptions withNodeSep(Integer value) {
		this.nodeSep = value;
		return this;
	}

	public Boolean getInfobox() {
		return infobox;
	}

	public GraphOptions withInfobox(Boolean value) {
		this.infobox = value;
		return this;
	}

	public Boolean getCardinalityInfo() {
		return cardinalityInfo;
	}

	public GraphOptions withCardinalityInfo(Boolean value) {
		this.cardinalityInfo = value;
		return this;
	}

	public Boolean getPropertyInfo() {
		return propertyInfo;
	}

	public GraphOptions withPropertyInfo(Boolean value) {
		this.propertyInfo = value;
		return this;
	}

	public ArrayList<TYP> getButtons() {
		return buttons;
	}

	public GraphOptions withButton(TYP... values) {
		if(values == null) {
			return this;
		}
		if(this.buttons == null) {
			this.buttons = new ArrayList<GraphOptions.TYP>();
		}
		for(TYP item : values) {
			this.buttons.add(item);
		}
		return this;
	}

	public TYP getDisplay() {
		return display;
	}

	public GraphOptions withDisplay(TYP display) {
		this.display = display;
		return this;
	}
}