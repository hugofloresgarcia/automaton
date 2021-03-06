AutomatonControlView : View {
	var
	<>parent,
	<>bounds,
	<>client,
	<>layout,

	<>pad,
	<>slider_height,
	<>slider_width,
	<>slider_actions,
	<>slider_labels,
	<>default_slider_values,

	<>button_actions,
	<>button_height,
	<>button_width,
	<>button_labels,

	<>list_width,
	<>list_height,

	<>text_labels,
	<>text_actions,

	<>texts,
	<>sliders,
	<>buttons,
	<>list_views;

	*new { |parent, bounds, client|
		^super.new(parent, bounds)
		.background_(Color.rand())
		.visible_(true)
		.init(parent, bounds, client);
	}

	init {|parent, bounds, client|
		var grid_y, grid_x;
		this.parent = parent;
		this.bounds = bounds;
		this.client = client;

		grid_y = bounds.height/10;
		grid_x = bounds.width/6;

		this.pad = 10;

		this.slider_width = 0.5 * grid_x;
		this.slider_height = 9 * grid_y;

		this.button_width = 1 * grid_x;
		this.button_height = 1 * grid_y;

		this.list_width = 2 * grid_x;
		this.list_height = 9 * grid_y;


/*		this.list_left = this.pad;
		this.button_left = this.list_left + this.list_width + this.pad;
		this.slider_left = this.button_left + this.button_width + this.pad;*/

	}

	createAll{
		var layout;
		this.sliders = List.new();
		this.buttons = List.new();
		this.list_views = List.new();

		layout  = FlowLayout(this.bounds, 5@5, 5@5);

		this.decorator_(layout);

		this.createActions();
		this.createRuleSets();
		this.createSliders();
		// this.createButtons();
		this.createTexts();

	}

	createActions{
		this.slider_labels = ["rand", "master"];

		this.slider_actions = [{
			arg view;
			client.cell_grid.randomness = view.value.linlin(0.0, 1.0, 0.0, 1.0);
		}, {
			arg view;
			client.cell_grid.setMasterVol(view.value);
		}];

		this.default_slider_values = [0.0, 0.5];

		///////////////////////////////////
		//////////////DANIEL//////////////
		///////////////////////////////////
		//this is where ur gonna put ur buttons
		//simply add a string for the button label
		//and then add a new function that gets executed when the mouse gets pressed
		//to access the functions you in Automaton.sc (where you will write the code to spawn new
		//creatures, refer to this.client.cell_grid
		//for example, if I want to call spawnGlider(), simply
		//call this.client.cell_grid.spawnGlider();

		this.button_labels = ["chaos"];

		this.button_actions = [{
			this.client.cell_grid.setRandomColor();
		}];

		this.text_labels = ["bpm"];

		this.text_actions = [{
			arg v;
			var periodt;
			periodt = (v.value/60).clip(1, 120);
			this.client.frameRate_(periodt);
		}];

	}

	createSliders{
		this.slider_actions.do({
			arg action, count;
			var slider;

			slider = EZSlider.new(
				parent: this,
				bounds: this.slider_width@this.slider_height,
				label: this.slider_labels.at(count),
				layout: 'vert'
			)
			.action_(action)
			.valueAction_(this.default_slider_values[count]);

			this.sliders.add(slider);
		});
	}

	createTexts{
		this.text_actions.do({
			arg action, count;
			var txt;
			txt = EZText.new(
				parent: this,
				bounds: (this.list_width*1.25)@(this.button_height*2),
				label: this.text_labels.at(count),
				labelWidth: 40,
				action: action
			)
			.valueAction_(420);
			this.texts.add(txt);
		});
	}

	createButtons{
		this.buttons = List.new();

		this.button_actions.do({
			arg action, count;
			var bttn;

			bttn = Button.new(
				parent: this,
				bounds: this.button_width@this.button_height)
			.action_(action)
			.states_([[this.button_labels[count]]]);

			this.buttons.add(bttn);
		});
	}

	createRuleSets {
		var view, items, functions;
		items = this.client.cell_grid.available_rules;
		functions  = this.client.cell_grid.available_rule_sets;

		view = ListView(
			parent: this,
			bounds: this.list_width@this.list_height)
		.items_(items)
		.action_({
			arg v;
			this.client.cell_grid.current_rule_set = client.cell_grid.available_rule_sets[v.value];
		});

		this.list_views.add(view);
	}
}
//////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
AutomatonSoundView : VLayoutView{
	 var parent, bounds, <>client,
	<>textfields,
	<>eztexts,
	<>update_button,

	<>entry_labels,
	<>entry_values,
	<>range_labels,
	<>range_values;


	*new{|parent, bounds, client|
		^super.new(parent, bounds).init(client).createWidgetActions;
	}

	init{|client|
		this.client = client;
	}

	createWidgetActions{
		// this.list_views.add(RangeTextField(
		var grid_x, grid_y, update_button, top_labels;
		this.textfields = List.new();
		this.eztexts = List.new();

		this.range_labels = [ "pans", "atks", "rels", "cutoffs"];
		this.range_values = [this.client.cell_grid.pans, this.client.cell_grid.atks,
			           this.client.cell_grid.rels, this.client.cell_grid.cutoffs];

		this.entry_labels = ["synthdef", "key", "scale"];
		this.entry_values = [
			this.client.cell_grid.synthdef,
			this.client.cell_grid.key,
			this.client.cell_grid.scale];

		grid_x = this.bounds.width - 10;
		grid_y = this.bounds.height / (this.entry_labels.size + this.range_labels.size+1);


		this.entry_labels.do({
			arg str, count;
			this.eztexts.add(EZText.new(
				parent: this,
				bounds: grid_x @ grid_y,
				label: str,
				labelWidth: grid_x/4,
				textWidth: grid_x/4 * 3,
				labelHeight: grid_y,
				initVal: this.entry_values[count]
			));

		});

		this.range_labels.do({
			arg label, count;
			this.textfields.add(
				RangeTextField(
					parent: this,
					bounds: Rect(
						left:  (0),
						top: (this.entry_labels.size+count) * grid_y,
						width: grid_x,
						height: grid_y
					),
					labeltext: label,
					min: this.range_values.at(count)[0],
					max: this.range_values.at(count)[1]
				)
			);
		});

		this.update_button = Button(this, Rect(
			left: 10,
			top:  grid_y * ( this.entry_labels.size + this.range_labels.size),
			width:  grid_x-20,
			height: grid_y))
		.states_([["update all"]])
		.action_({
			arg b;
			var synthdef, key, scale, pans, atks, rels, cutoffs;

			client.cell_grid.killAllOrganelles();

			synthdef = b.parent.eztexts[0].textField.value.replace("'", "").asSymbol;
			key      = b.parent.eztexts[1].textField.value.asInt;
			//fancy string to array conversion
			scale    = b.parent.eztexts[2].textField.value.replace("[", "")
			.replace("]", "").split($,).collect({
				arg char;
				char.asInteger;
			});
			//
			pans     = b.parent.textfields[0].getBounds.asArray;
			atks     = b.parent.textfields[1].getBounds.asArray;
			rels     = b.parent.textfields[2].getBounds.asArray;
			cutoffs  = b.parent.textfields[3].getBounds.asArray;

			client.cell_grid.setCellOrganelles(
				synthdef: synthdef,
				key: key,
				scale: scale,
				pans: pans,
				atks: atks,
				rels: rels,
				cutoffs: cutoffs);
			// client.cell_grid.killAllOrganelles();
		});

	}
}

AutomatonBufferView : VLayoutView {
	var parent, bounds,
	<>client,
	<>textfields,

	<>eztexts,
	<>update_button,
	<>buffer,

	<>entry_labels,
	<>entry_values,
	<>range_labels,
	<>range_values,

	<>load_buffer,
	<>synthdef,
	<>playback_menu,
	<>buffer,
	<>ranges,
	<>entries;

	*new{|parent, bounds, client|
		^super.new(parent, bounds).init(client).createWidgetActions;
	}

	init{|client|
		this.client = client;
		this.synthdef = \linbuf;
		// this.layout_(FlowLayout(this.bounds, 2@2, 2@2));

	}

	createWidgetActions{
		// this.list_views.add(RangeTextField(
		var grid_x, grid_y, update_button, top_labels;
		this.textfields = Dictionary.new();
		this.eztexts = Dictionary.new();


		////////////////////////////////////////
		this.ranges = Dictionary.with(*[
			\rate-> [0.125, 8],
			// "dur", this.client.cell_grid.dur,
			\pan-> [-1, 1],
			\grainSize -> [0.01, 100]
		]);

		this.ranges.postln;
		this.entries = Dictionary.with(*[
			\synthdef -> \linbuf
		]);

		////////////////////////////////////////

		grid_x = this.bounds.width - 10;
		grid_y = this.bounds.height / (this.entries.keys.size + this.ranges.keys.size+2);

		////////////////////////////////////////
		this.load_buffer = HLayoutView(this, grid_x@grid_y);
		this.client.cell_grid.centroids.do({
			arg centroid, count;
			Button(this.load_buffer)
			.states_([["b" ++ count.asString]])
			.action_({
				buffer = Buffer.loadDialog(Server.default, action: {
					arg b;
					this.client.cell_grid.setBufferAt(count, b);
				});
			});
		});

/*		this.load_buffer = Button(
			parent: this,
			bounds: grid_x @ grid_y)
		.states_([["load buffer"]])
		.action_({
			buffer = Buffer.loadDialog(Server.default, action: {
				arg b;
				this.client.cell_grid.setBuffer(b);
			});
		});*/

/*		this.playback_menu = Button(
			parent: this,
			bounds: grid_x @ grid_y)
		.states_([["playback"]])
		.action_({
			Menu(
				MenuAction("linear", {this.synthdef = \linbuf}),
				MenuAction("sine", {this.synthdef = \sinbuf}),
				MenuAction("pulse", {this.synthdef = \pulsebuf}),
			);
		});*/

		this.entries.keysDo({
			arg key;
			key.postln;
			this.eztexts.add(key -> EZText.new(
				parent: this,
				bounds: grid_x @ grid_y,
				label: key,
				labelWidth: grid_x/4,
				textWidth: grid_x/4 * 3,
				labelHeight: grid_y,
				initVal: this.entries[key]
			));
		});

		this.ranges.keysDo({
			arg key;
			key.postln;
			this.textfields.add(key ->
				RangeTextField(
					parent: this,
					bounds: Rect(
						left:  0,
						top: 0,
						width: grid_x,
						height: grid_y
					),
					labeltext: key,
					min: this.ranges[key][0],
					max: this.ranges[key][1]
				)
/*				AutomatonRanger(
					parent: this,
					bounds: Rect.fromPoints(0@0, grid_x @ grid_y),
					label: key,
					initAction: true,
					controlSpec: ControlSpec(this.ranges[key][0], this.ranges[key][1]),
					layout: \horz);*/
			);
		});

		////////////////////////////////////////

		this.update_button = Button(this, grid_x@grid_y)
		.states_([["update all"]])
		.action_({
			arg b;
			var buffer, rate, pan, dur, grainSize;

			client.cell_grid.killAllOrganelles();

			buffer = this.buffer;
			rate = this.textfields[\rate].getBounds.asArray;
			pan = this.textfields[\pan].getBounds.asArray;
			grainSize = this.textfields[\grainSize].getBounds.asArray;

			this.synthdef = this.eztexts[\synthdef].textField.value.replace("'", "").asSymbol;

			// grainSize.postln;
			// dur = this.textfields["dur"].getBounds.asArray;

			client.cell_grid.setBufferOrganelles(this.synthdef, rate, pan, grainSize);
		});

	}


}

AutomatonRanger : EZRanger {

	getBounds {
		^[this.lo.asFloat, this.hi.asFloat];
	}
}


RangeTextField : View {
	var <>parent, <>bounds, <>labeltext, <>min, <>max,
	<>main_view,
	<>label,
	<>minfield,
	<>to_label,
	<>maxfield;

	*new{|parent, bounds, labeltext, min, max|
		^super.new(parent, bounds).rangeinit(parent, bounds, labeltext, min, max);
	}
	rangeinit{|parent, bounds, labeltext, min, max|
		var valueactn, grid_x;
		this.parent = parent;
		this.bounds = bounds;
		this.labeltext = labeltext;
		this.min = min;
		this.max = max;

		grid_x = bounds.width/7;
		// labeltext.postln;

		valueactn = {
			arg v;
			v.value =(v.value).clip(this.min, this.max);
		};
		this.label = StaticText(this, Rect(
			left: 0 * grid_x+10,
			top: 0,
			width: 2*grid_x,
			height: this.bounds.height))
		.string_(labeltext);

		this.minfield = TextField(this, Rect(
			left: 2*grid_x,
			top: 0,
			width: 2*grid_x,
			height: this.bounds.height))
		.valueAction_(valueactn)
		.value_(this.min);

		this.to_label = StaticText(this, Rect(
			left: 4*grid_x,
			top: 0,
			width: grid_x,
			height: this.bounds.height))
		.string_("to");

		this.maxfield = TextField(this, Rect(
			left: 5 * grid_x,
			top: 0,
			width: 2*grid_x,
			height: this.bounds.height))
		.valueAction_(valueactn)
		.value_(this.max);
	}

	getBounds{
		^[this.minfield.value.asFloat, this.maxfield.value.asFloat];
	}
}
