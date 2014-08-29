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
Object_create = Object.create || function (o) {var F = function() {};F.prototype = o; return new F();};
Drawer = function(){};
Drawer.prototype.isShowRaster = function(){return false;}
Drawer.prototype.showInfoBox = function(){return false;}
Drawer.prototype.clearBoard = function(){};
Drawer.prototype.onLoadImage = function(){};
Drawer.prototype.onFinishImage = function(){};
Drawer.prototype.createText = function(text){return document.createTextNode(text);}
Drawer.prototype.createSubGraph = function(node, element){
	var options = new Options();
	options.rootElement = element;
	options.display = graph.options.display;
	options.bar = false;
	options.parent = graph;
	node.g = new Graph(node.graph, options);
	node.g.layout();
};
Drawer.prototype.setSize = function(item, x, y){item.width = x;item.height = y;};
Drawer.prototype.createObject= function(ns, tag){
	if(document.createElementNS){
		return document.createElementNS(ns, tag);
	}
	var e = document.createElement(tag);
	e.setAttribute('xmlns', ns);
	return e;
};

HTMLDrawer = function() {};
HTMLDrawer.prototype = Object_create(Drawer.prototype);
HTMLDrawer.prototype.showInfoBox = function(){return true;}
HTMLDrawer.prototype.isShowRaster = function(){return true;}
HTMLDrawer.prototype.setPos = function(item, x, y){item.style.left = x;item.style.top = y;};
HTMLDrawer.prototype.setSize = function(item, x, y){item.style.width = x;item.style.height = y;};
HTMLDrawer.prototype.createContainer = function(graph){
	this.graph = graph;	
	var board = document.createElement("div");
	board.className="Board";
	board.graph = graph;
	board.rasterElements=[];
	return board;
};

HTMLDrawer.prototype.createImage = function(model){
	var img = new Image();
	img.src = model.src;
	img.node = model;
	var add=true;
	if(model.width){
		img.width = model.width;
		add=false;
	}
	if(model.height){
		img.height = model.height;
		add=false;
	}
	if(add){
		this.graph.loader.appendImg(img);
	}
	return img;
};

HTMLDrawer.prototype.createCell = function(parent, tag){
	var tr = document.createElement('tr');
	var cell = document.createElement(tag);
	cell.style.fontSize=this.graph.options.fontsize;
	tr.appendChild(cell);
	parent.appendChild(tr);
	return cell;
};
HTMLDrawer.prototype.getHTMLNode = function(node, calculate){
	var htmlElement = document.createElement("div");
	var symbolLib = new SymbolLibary();
	if(node.typ=="patternObject") {
		htmlElement.className="patternElement";
	} else if(symbolLib.isSymbol(node)) {
		return symbolLib.draw(null, node, calculate);
	} else if(this.graph.typ=="object") {
		htmlElement.className="objectElement";
	} else {
		htmlElement.className="classElement";
	}
	this.setPos(htmlElement, node.x, node.y);
	htmlElement.style.zIndex=5000;
	this.graph.addNodeLister(htmlElement);

	if(node.typ=="subgraph"){
		this.createSubGraph(node, htmlElement);
		htmlElement.style.width = node.g.board.style.width;
		htmlElement.style.height = node.g.board.style.height;
		return htmlElement;
	}
	if(node.content_src){
		if(!node.content_img){
			node.content_img = {};
			node.content_img.src = node.content_src;
			node.content_img.width = node.content_width;
			node.content_img.height = node.content_height;
		}
		htmlElement.appendChild(this.createImage(node.content_img));
		return htmlElement;
	}
	if(node.content_html){
		htmlElement.innerHTML = node.content_html;return htmlElement;
	}
	if(node.content_plain){
		htmlElement.appendChild(this.createText(node.content_plain));return htmlElement;
	}
	var table = document.createElement('table');
	htmlElement.appendChild(table);
	var cell;
	table.border="0";
	if(node.head_src){
		cell = this.createCell(table, "td");
		if(!node.head_img){
			node.head_img = {};
			node.head_img.src = node.head_src;
			node.head_img.width = node.head_width;
			node.head_img.height = node.head_height;
		}
		cell.appendChild(this.createImage(node.head_img));
	}
	if(node.headinfo){
		cell = this.createCell(table, "td");
		cell.className="head";
		cell.innerHTML = node.headinfo;
	}
	if(this.graph.typ=="object"){
		info = "<u>"+ node.id.charAt(0).toLowerCase() + node.id.slice(1) + "</u>";
	}else{
		info = node.id;
	}
	if(node.href){
		info = "<a href=\""+node.href+"\">" + info + "</a>";
	}
	cell = this.createCell(table, "th");
	cell.innerHTML = info;

	if(node.attributes){
		var first=true;
		for(var a = 0; a < node.attributes.length; a++){
			cell = this.createCell(table, "td");
			cell.innerHTML = node.attributes[a];
			if(!first){
				cell.className = 'attributes';				
			}else{
				cell.className = 'attributes first';
				first=false;
			}
		}
	}
	if(node.methods){
		var first=true;
		for(var m=0;m < node.methods.length;m++){
			var method = node.methods[m];
			cell = this.createCell(table, "td");
			cell.innerHTML = node.methods[m];
			if(!first){
				cell.className = 'methods';
			}else{
				cell.className = 'methods first';
				first=false;
			}
		}
	}
	htmlElement.appendChild(table);
	htmlElement.node = node;
	node.htmlElement = htmlElement;
	return htmlElement;
};

HTMLDrawer.prototype.createInfo = function(x, y, text, calculate){
	var info = document.createElement("div");
	info.className="EdgeInfo";
	info.style.fontSize = this.graph.options.fontsize;
	this.setPos(info, x, y);
	info.innerHTML = text;
	return info;
};

HTMLDrawer.prototype.createLine = function(x1, y1, x2, y2, style){
	if (x2 < x1 ){
		var temp = x1;
		x1 = x2;
		x2 = temp;
		temp = y1;
		y1 = y2;
		y2 = temp;
	}
	// Formula for the distance between two points
	// http://www.mathopenref.com/coorddist.html
	var length = Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));

	var line = document.createElement("div");
	line.className="lineElement";
	line.style.width = length + "px";
	line.style.position = "absolute";
	line.style.zIndex = 42;
	line.style.borderBottomStyle= style;

	var angle = Math.atan((y1-y2)/(x1-x2));
	if(x1==x2){
		angle = Math.atan((y1-y2)/(x1-x2))*-1;
	}
	line.style.top = y1 + 0.5*length*Math.sin(angle) + "px";
	line.style.left = x1 - 0.5*length*(1 - Math.cos(angle)) + "px";
	line.style.transform="rotate("+angle+"rad)";
	line.style.MozTransform = line.style.WebkitTransform = line.style.OTransform= "rotate(" + angle + "rad)";
	return line;
};
HTMLDrawer.prototype.onLoadImage = function(event){
	var img = event.target;
	img.node.width = img.width;
	img.node.height = img.height;
	this.graph.loader.onLoad(img);
};
HTMLDrawer.prototype.onFinishImage = function(event){
	this.graph.layouting();
}
// ######################################################           SVG           ####################################################################################


SVGDrawer = function() {};
SVGDrawer.prototype = Object_create(Drawer.prototype);
SVGDrawer.prototype.createContainer = function(graph){
	this.graph = graph;
	var board = this.createObject("http://www.w3.org/2000/svg", "svg");
	board.rasterElements=[];
	board.graph = graph;
	if(!isIE ()){
		board.setAttribute('xmlns', "http://www.w3.org/2000/svg");
	}
	return board;
};
SVGDrawer.prototype.setSize = function(item, x, y){item.style.width = x;item.style.height = y;};
SVGDrawer.prototype.getWidth = function(label, calculate){
	var text = this.createObject("http://www.w3.org/2000/svg", "text");
	text.appendChild(document.createTextNode(label));
	text.style.fontSize=this.graph.options.fontsize * "px";
	this.graph.board.appendChild(text);
	var width =text.getBoundingClientRect().width;
	this.graph.board.removeChild(text);
	return width;
}
SVGDrawer.prototype.getHTMLNode = function(node, calculate){
	var group = this.createObject("http://www.w3.org/2000/svg", "g");
	group.setAttribute('transform', "translate("+node.x+" "+node.y+")");
	var width=0;
	var height=30;
	if(node.typ!="node"){
		return new SymbolLibary().draw(this, node, calculate);
	}
	if(this.graph.typ=="object"){
		width = Math.max(width, this.getWidth(node.id.charAt(0).toLowerCase() + node.id.slice(1)));
	}else{
		width = Math.max(width, this.getWidth(node.id));
	}
	if(node.attributes){
		height = height + node.attributes.length*20;
		for(var a=0; a<node.attributes.length;a++){
			var attribute = node.attributes[a];
			width = Math.max(width, this.getWidth(attribute));
		}
	} 
	height += 10;
	width += 20;
	var textwidth=width-10;

	if(node.content_src){
		group.appendChild(this.createElement({id:"image", height: node.height, width: node.width, content_src: node.content_src}));
		return group;
	}
	if(node.content_svg){
		group.innerHTML = node.content_svg;return group;
	}
	if(node.content_plain){
		var text = this.createElement({id:"text", "text-anchor":"left", x:"10"});
		text.setAttribute("style", "font-size:"+this.graph.options.fontsize+"px;");
		var textNode = document.createTextNode(node.content_plain)
		text.appendChild(textNode);
		group.appendChild(text);
		return group;
	}
	group.appendChild(this.createElement({id:"rect", "width":width, "height":height}));
	var text = this.createElement({id:"text", "text-anchor":"middle", x:width/2, y:20, width:textwidth});
	if(this.graph.typ=="object"){
		text.setAttribute("style", "text-decoration: underline;font-size:"+this.graph.options.fontsize+"px;");
		text.appendChild(document.createTextNode(node.id.charAt(0).toLowerCase() + node.id.slice(1)));
	}else{
		text.setAttribute("style", "font-size: "+this.graph.options.fontsize+"px;");
		text.appendChild(document.createTextNode(node.id));
	}
	group.appendChild(text);
	group.appendChild( this.createElement({id:"line", x1:0, y1:30, x2: width, y2: 30, style:"stroke:#000;"}) );
	if(node.attributes){
		var y = 50;
		for(var a=0;a<node.attributes.length;a++){
			var attribute = node.attributes[a];
			var text = this.createElement({id:"text", "text-anchor":"left", "width": textwidth, x:10, "y":y, "style": "font-size:"+this.graph.options.fontsize+"px;"});
			text.appendChild(document.createTextNode(attribute));
			group.appendChild(text);
			y += 20;
		}
	}
	return group;
};

SVGDrawer.prototype.createElement = function(node){
	var element = this.createObject("http://www.w3.org/2000/svg", node.id);
	//if(node.id=="rect" || node.id=="ellipse" || node.id=="circle" || node.id=="line"){
	element.setAttribute("style", "fill:none;stroke:#000;stroke-width:1px;");
	//}
	if(node.id=="path"){
		element.setAttribute('fill', "rgb(255, 255, 255)");
		//element.setAttribute('style', "stroke:#000;");
	}
	
	for (var key in node) {
		if(key=='id')continue;
		if(key=='content_src'&& node.id=="image") continue;
		element.setAttribute(key, node[key]);
	}
	if(node.id=="image"){
		element.setAttribute('xmlns:xlink', "http://www.w3.org/1999/xlink");
		element.setAttributeNS("http://www.w3.org/1999/xlink", 'href',node["content_src"]);
	}
	return element;
};

SVGDrawer.prototype.createInfo = function(x, y, text, calculate){
	return null;
};

SVGDrawer.prototype.createLine = function(x1, y1, x2, y2, style){
	var line = this.createElement({id:"line", 'x1': x1, 'y1': y1, 'x2': x2, 'y2': y2});
	if(style=="DOTTED"){
		line.setAttribute('style', "stroke:#000;stroke-miterlimit:4;stroke-dasharray:1, 1;");
	}else{
		line.setAttribute('style', "stroke:#000;");
	}
	return line;
};
SVGDrawer.prototype.createGroup = function(node, group){
	var entity = document.createElementNS("http://www.w3.org/2000/svg", "g");
	if(group.scale){
		entity.setAttribute('transform', "translate("+group.x+" "+group.y+") scale("+group.scale+")");
	}else{
		entity.setAttribute('transform', "translate("+group.x+" "+group.y+")");
	}
	entity.setAttribute("height", group.height);
	entity.setAttribute("width", group.width);
	for (var i = 0; i < group.items.length; ++i){
		entity.appendChild( this.createElement( group.items[i] ) );
	}
	return entity;
};


CanvasDrawer = function() {};
CanvasDrawer.prototype = Object_create(Drawer.prototype);
CanvasDrawer.prototype.clearBoard = function(){
	if( !this.graph ){
		return;
	}
	var canvas = this.graph.board;
	var context = canvas.getContext('2d');
	context.clearRect(0, 0, canvas.width, canvas.height);
};
CanvasDrawer.prototype.createContainer = function(graph){
	this.graph = graph;	
	var board = document.createElement("canvas");
	board.rasterElements=[];
	board.graph = graph;
	return board;
};
CanvasDrawer.prototype.getWidth = function(text){
	var context = this.graph.board.getContext('2d');
	context.font = this.graph.options.fontsize+"px Arial";
	var metrics = context.measureText(text);
	return metrics.width;

};
CanvasDrawer.prototype.getHTMLNode = function(node, calculate){
	var canvas = this.graph.board;

	// Calculate Height
	var width=0;
	var height=20;
	if(this.graph.typ=="object"){
		width = Math.max(width, this.getWidth(node.id.charAt(0).toLowerCase() + node.id.slice(1)));
	}else{
		width = Math.max(width, this.getWidth(node.id));
	}
	if(node.attributes){
		height = height + node.attributes.length*20;
		for(var a=0;a<node.attributes.length;a++){
			var attribute = node.attributes[a];
			width = Math.max(width, this.getWidth(attribute));
		}
	}
	height += 10;
	width += 20;
	if(calculate){
		if(!node.startWidth){
			node.width = width;
		}
		if(!node.startHeight){
			node.height=height;
		}
		return null;
	}

	var textwidth=node.width-10;
	var context = canvas.getContext('2d');
	if(node.content_src){
		var img = document.createElement("img");
		img.src =  node.content_src;
		img.node = node;
		this.graph.loader.appendImg(img);
		return null;
	}
	if(node.content_plain){
		context.font = this.graph.options.fontsize+"px Arial";
		context.fillText(node.content_plain, node.x, node.y);
		return null;
	}

	context.beginPath();
	context.rect(node.x, node.y, node.width, node.height);
	context.lineWidth = 1;
	context.strokeStyle = 'black';
	context.stroke();

	this.createLine(node.x, node.y+20, node.x + node.width, node.y+20);

	var context = canvas.getContext('2d');
	context.font = this.graph.options.fontsize+"px Arial";
	var text="";
	if(this.graph.typ=="object"){
		text = node.id.charAt(0).toLowerCase() + node.id.slice(1);
		var start = node.x + (node.width - this.getWidth(text))/2;
		this.createLine(start, node.y+16, start + this.getWidth(text), node.y+16);
	}else{
		text = node.id;
	}
	context.fillText(text, node.x + (node.width - this.getWidth(text))/2, node.y + 15);

	if(node.attributes){
		var y = node.y+40;
		for(var a=0;a<node.attributes.length;a++){
			var attribute = node.attributes[a];
			var context = canvas.getContext('2d');
			context.font = this.graph.options.fontsize+"px Arial";
			context.fillText(attribute, node.x + 10, y);
			y += 20;
		}
	}
	return null;
};
CanvasDrawer.prototype.createInfo = function(x, y, text, calculate){
	return null;
};
CanvasDrawer.prototype.createLine = function(x1, y1, x2, y2, style){
	var canvas = this.graph.board;
	var context = canvas.getContext('2d');
	if(style=="DOTTED"){
		context.setLineDash([1,2]);
	}
	context.moveTo(x1, y1);
	context.lineTo(x2, y2);
	context.stroke();
	return null;
};

CanvasDrawer.prototype.onLoadImage = function(event){
	var img = event.target;
	
	var canvas = this.graph.board;
	var context = canvas.getContext('2d');
	context.drawImage(img, img.node.x, img.node.y);
	this.graph.loader.remove(img);
	this.graph.root.removeChild(img);
	this.graph.loader.resetDrawer();
};
CanvasDrawer.prototype.onFinishImage = function(event){
	try{
		var img = document.createElement("img");
		img.src =  this.graph.board.toDataURL();
		this.graph.clearBoard();
		this.graph.board = img;
		this.graph.root.appendChild(img);
	} catch (e) {
		this.graph.clearBoard();
		this.graph.drawer = this.oldDrawer;
		this.graph.drawGraph(0,0);
		alert("Browser nicht unterstuetzt");
	}
};
// Example Items
// {id:"path", d:""}
// {id:"rect", width:46, height:34}
// {id:"ellipse", width:23, height:4}
// {id:"line", x1:650, y1:-286, x2:650, y2:-252}
// {id:"circle", r:5, x:12, y:0}
// {id:"image", height: 30, width: 50, content_src: hallo}
// {id:"text", "text-anchor":"left", x:"10"}

SymbolLibary = function(){};
SymbolLibary.prototype.upFirstChar = function(txt)
{
    return txt.charAt(0).toUpperCase() + txt.slice(1).toLowerCase();
};

SymbolLibary.prototype.isSymbol = function(node){
	var fn  = this["draw" + this.upFirstChar(node.typ)];
	return typeof fn === "function";
};
SymbolLibary.prototype.draw = function(drawer, node, calculate){
	var fn  = this["draw" + this.upFirstChar(node.typ)];
	if(typeof fn === "function"){
		var group = fn.apply(this, [node, calculate]);
		if( !drawer){
			drawer = new SVGDrawer();
			var board = drawer.createContainer(null);
			var element = drawer.createGroup(node, group);
			board.appendChild(element);
			return board;
		}
		return drawer.createGroup(node, group);
	}
};
SymbolLibary.prototype.drawSmily = function(node, calculte){
	return {
		x:0,
		y:0,
		width:100,
		height:100,
		items:[
			{id:"path", d:"m 49.5002 25.0001a 24.5001 24.5000 0 1 1-49.0001 0 24.5001 24.5000 0 1 1 49.0001 0z"},
			{id:"path", d:"m 8.6239 30.9175c 15.9633 20 32.1560 0.3211 32.1560 0.3211"},
			{id:"path", d:"m 19.6330 19.6789a 1.7431 2.5229 0 1 1-3.4862 0 1.7431 2.5229 0 1 1 3.4862 0z"},
			{id:"path", d:"m 33.4862 19.6789a 1.7431 2.5229 0 1 1-3.4862 0 1.7431 2.5229 0 1 1 3.4862 0z"},
			{id:"path", d:"m 6.0550 31.0091c 3.3945 0.9175 4.0367-2.2017 4.0367-2.2017"},
			{id:"path", d:"m 43.5780 31.3761c-3.3945 0.9175-4.0367-2.2017-4.0367-2.2017"}
		]};
};
SymbolLibary.prototype.drawDatabase = function(node, calculte){
	return {
		x:0,
		y:0,
		width:100,
		height:100,
		items:[
			{id:"path", d:"M 650-252a 16 4 0 0 0 45 0"},
			{id:"rect", width:46, height:34},
			{id:"ellipse", width:23, height:4},
			{id:"line", x1:650, y1:-286, x2:650, y2:-252},
			{id:"line", x1:696, y1:-286, x2:696, y2:-252},
			{id:"rect", width:46,height:42}
		]};
};
SymbolLibary.prototype.drawLetter = function(node, calculte){
	return {
		x:0,
		y:0,
		width:100,
		height:35,
		items:[
			{id:"path", d:"m 1 1 98 0 0 48-98 0z"},
			{id:"path", d:"m 1.2684 1.4855 48.7259 23.3589 48.6202-23.676"}
		]};
};

SymbolLibary.prototype.drawMobilphone = function(node, calculte){
	return {
		x:0,
		y:0,
		width:25,
		height:50,
		items:[
			{id:"path", d:"m 4.1937 0.5 15.6127 0c 2.0463 0 3.6937 1.6474 3.6937 3.6936l 0 41.6127c 0 2.0463-1.6474 3.6937-3.6937 3.6937l-15.6127 0c-2.0463 0-3.6937-1.6474-3.6937-3.6937l 0-41.6127c 0-2.0462 1.6474-3.6936 3.6937-3.6936z"},
			{id:"path", d:"m 12.5 2.7338a 0.5 0.5 0 1 1-1 0 0.5 0.5 0 1 1 1 0z"},
			{id:"path", d:"m 14 45.6882a 2 2.0000 0 1 1-4 0 2 2.0000 0 1 1 4 0z"},
			{id:"path", d:"m 8.3516 5.0581 7.2969 0"},
			{id:"path", d:"m 1.6352 7.5455 20.7297 0 0 34.0796-20.7297 0z"},
		]};
};
SymbolLibary.prototype.drawWall = function(node, calculte){
	return {
		x:0,
		y:0,
		width:25,
		height:50,
		items:[
			{id:"path", d:"m 26.5000 45.9384-5.0389 3.5616-20.9610-9.0435 0.0000-36.3952 5.0389-3.5613 20.9611 9.0437z"},
			{id:"path", d:"m 2.7070 11.4274 18.3409 7.9133m-14.4589-12.5655 0 6.3473m 8.1631 21.7364 0 6.3472m-8.6393-9.9876 0 6.3472m 4.0923-10.6702 0 6.3473m 4.7743-10.2152 0 6.3473m-8.8666-10.2152 0 6.3472m 4.7743-10.2151 0 6.3472m-7.9572 14.4578 18.3409 7.9132m-18.3409-13.9132 18.3409 7.9132m-18.3409-13.9133 18.3409 7.9133m-18.3409-13.9133 18.3409 7.9132m-0.0000-13.0532-0.0001 34.0433m-18.2251-41.8406 18.2998 7.9024m 0 0.1115 4.9978-3.5723"}
		]};
};
SymbolLibary.prototype.drawActor = function(node, calculte){
	return {
		x:10,
		y:10,
		width:25,
		height:50,
		items:[
			{id:"line", x1:12,y1:5,x2:12,y2:25},
			{id:"circle", r:5, cx:12, cy:0},
			{id:"line", x1:0, y1:13, x2:25, y2:13},
			{id:"line", x1:12, y1:25, x2:5, y2:34},
			{id:"line", x1:12, y1:25, x2:20, y2:34}
		]};
};
SymbolLibary.prototype.drawLamp = function(node, calculte){
	return {
		x:10,
		y:10,
		width:25,
		height:50,
		items:[
			{id:"path", d:"m 22.4676 10.5797c-6.5690 0-11.8905 5.1708-11.8905 11.5437 0 2.3507 0.7376 4.538 1.9817 6.3616 2.0562 3.9241 4.3637 5.6306 4.4198 10.4001l 11.1459 0c 0.1160-4.9336 2.5455-6.7664 4.4319-10.4001 1.3930-1.5069 1.7799-4.4684 1.8016-6.3616 0-6.3729-5.3215-11.5437-11.8905-11.5437z"},
			{id:"path", d:"m 18.4085 40.0784 8.0294 0c 0.5820 0 1.0505 0.4685 1.0505 1.0504 0 0.582-0.4685 1.0505-1.0505 1.0505l-8.0294 0c-0.5820 0-1.0505-0.4685-1.0505-1.0505 0-0.5819 0.4685-1.0504 1.0505-1.0504z"},
			{id:"path", d:"m 18.4085 42.7311 8.0294 0c 0.5820 0 1.0505 0.4685 1.0505 1.0504 0 0.582-0.4685 1.0505-1.0505 1.0505l-8.0294 0c-0.5820 0-1.0505-0.4685-1.0505-1.0505 0-0.5819 0.4685-1.0504 1.0505-1.0504z"},
			{id:"path", d:"m 18.4411 45.2823 8.0294 0c 0.5820 0 1.0505 0.4685 1.0505 1.0505 0 0.582-0.4685 1.0505-1.0505 1.0505l-8.0294 0c-0.5820 0-1.0505-0.4685-1.0505-1.0505 0-0.582 0.4685-1.0505 1.0505-1.0505z"},
			{id:"path", d:"m 19.4727 48.0741c 0.3690 0.8074 1.0610 1.3087 1.8885 1.7116 0.6333 0.3084 1.4623 0.262 2.1164 0 0.7971-0.3192 1.4109-0.7966 1.8559-1.7762z"},
			{id:"path", d:"m 5.9483 37.4973 4.1544-4.0548c 0.3042-0.2969 0.7902-0.2931 1.0897 0.0084 0.2995 0.3016 0.2958 0.7833-0.0084 1.0802l-4.1544 4.0548c-0.3042 0.2969-0.7902 0.2931-1.0897-0.0085-0.2995-0.3016-0.2958-0.7833 0.0084-1.0802z"},
			{id:"path", d:"m 39.0558 37.5618-4.1544-4.0548c-0.3042-0.2969-0.7902-0.2931-1.0897 0.0085-0.2995 0.3016-0.2958 0.7833 0.0084 1.0802l 4.1544 4.0548c 0.3042 0.2969 0.7902 0.2931 1.0897-0.0085 0.2995-0.3016 0.2958-0.7833-0.0084-1.0802z"},
			{id:"path", d:"m 37.886 22.9798 5.8406-0.0467c 0.4233-0.0034 0.7616-0.3469 0.7584-0.7703-0.0032-0.4233-0.3465-0.7614-0.7698-0.7580l-5.8406 0.0467c-0.4233 0.0034-0.7616 0.3469-0.7584 0.7702 0.0032 0.4234 0.3465 0.7615 0.7698 0.7581z"},
			{id:"path", d:"m 1.2884 22.9797 5.8406-0.0467c 0.4233-0.0034 0.7616-0.3469 0.7584-0.7702-0.0032-0.4233-0.3465-0.7614-0.7698-0.7580l-5.8406 0.0467c-0.4233 0.0034-0.7616 0.3469-0.7584 0.7702 0.0032 0.4233 0.3465 0.7614 0.7698 0.7580z"},
			{id:"path", d:"m 34.7476 11.2245 4.0877-4.1204c 0.2994-0.3018 0.2956-0.7839-0.0084-1.0810-0.3040-0.2971-0.7898-0.2933-1.0892 0.0084l-4.0877 4.1204c-0.2994 0.3018-0.2956 0.7839 0.0084 1.0810 0.3040 0.2971 0.7898 0.2933 1.0892-0.0084z"},
			{id:"path", d:"m 11.2494 9.9815-4.1544-4.0548c-0.3042-0.2969-0.7902-0.2931-1.0897 0.0084-0.2995 0.3016-0.2958 0.7833 0.0084 1.0802l 4.1544 4.0548c 0.3042 0.2969 0.7902 0.2931 1.0897-0.0084 0.2995-0.3016 0.2958-0.7833-0.0084-1.0802z"},
			{id:"path", d:"m 21.6435 1.2928 0.0469 5.7682c 0.0035 0.4268 0.3498 0.7678 0.7766 0.7647 0.4268-0.0032 0.7676-0.3493 0.7641-0.7761l-0.0469-5.7682c-0.0035-0.4268-0.3498-0.7678-0.7766-0.7647-0.4268 0.0032-0.7676 0.3493-0.7641 0.7761z"},
			{id:"path", d:"m 26.1069 24.375c-0.4677 0.033-0.9728 0.1942-1.3332 0.3931-1.1368 0.6273-2.0556 2.9226-2.27 3.5024-0.2599-0.6887-1.1412-2.8637-2.2340-3.4666-0.7208-0.3978-1.9633-0.6605-2.4502 0-0.5916 0.8024 0.1647 2.1844 0.9008 2.8591 0.9822 0.9003 3.9275 0.8935 3.9275 0.8935 0 0 0.0005-0.034 0-0.036 0.5398-0.011 2.8424-0.097 3.7113-0.8935 0.7361-0.6746 1.4924-2.0566 0.9008-2.8591-0.2434-0.3302-0.6853-0.4259-1.1530-0.3931z"},
			{id:"path", d:"m 22.4693 28.5688 0 10.6875"}
		]};
};