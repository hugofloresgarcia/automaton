AutomatonControlView : View {
	var
	<>parent,
	<>bounds,
	<>client,

	<>pad,
	<>slider_height,
	<>slider_width,
	<>slider_actions,
	<>slider_labels,

	<>button_actions,
	<>button_height,
	<>button_width,
	<>button_labels,

	<>list_width,
	<>list_height,

	<>list_left,
	<>slider_left,
	<>button_left,


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

		this.slider_width = 2 * grid_x;
		this.slider_height = 1 * grid_y;

		this.button_width = 1 * grid_x;
		this.button_height = 1 * grid_y;

		this.list_width = 2 * grid_x;
		this.list_height = 9 * grid_y;


		this.list_left = this.pad;
		this.button_left = this.list_left + this.list_width + this.pad;
		this.slider_left = this.button_left + this.button_width + this.pad;

	}

	createAll{
		this.createWidgetActions();
		this.createSliders();
		this.createButtons();
		this.createRuleSets();
	}

	createWidgetActions{
		this.slider_labels = ["randomness"];

		this.slider_actions = [{
			arg view;
			client.cell_grid.randomness = view.value.linlin(0.0, 1.0, 0.0, 1.0);
		}];

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
	}

	createSliders{
		this.slider_actions.do({
			arg action, count;
			var slider;

			slider = Slider.new(
				parent: this,
				bounds: Rect(
					left: this.slider_left,
					top: this.pad + (this.slider_height * count),
					width: this.slider_width,
					height: this.slider_height));

			slider.action_(action);
			this.sliders.add(slider);
		});
	}

	createButtons{
		this.buttons = List.new();

		this.button_actions.do({
			arg action, count;
			var bttn;

			bttn = Button.new(
				parent: this,
				bounds: Rect(
					left: this.button_left,
					top: this.pad + (this.button_height  * count),
					width: this.button_width,
					height: this.button_height))
			.action_(action)
			// .visible_(false)
			.states_([[this.button_labels[count]]]);

			this.buttons.add(bttn);
		});
	}

	createRuleSets {
		var view, items, functions;
		items = this.client.cell_grid.available_rules;
		functions  = this.client.cell_grid.available_rule_sets;

		view = ListView(this, Rect(
			left: this.list_left,
			top: this.pad,
			width: this.list_width,
			height: this.list_height))
		.items_(items)
		.action_({
			arg v;
			this.client.cell_grid.current_rule_set = client.cell_grid.available_rule_sets[v.value];
		});

		this.list_views.add(view);
	}
}

AutomatonSoundView : AutomatonControlView{
	 var parent, bounds, client,
	<>textfields,
	<>params,
	<>param_s,
	<>single_entries,
	<>single_entrie_s,
	<>eztexts,
	<>update_button;


	*new{|parent, bounds, client|
		^super.new(parent, bounds, client).createWidgetActions;
	}

	createWidgetActions{
		// this.list_views.add(RangeTextField(
		var grid_x, grid_y, update_button, top_labels;
		this.textfields = List.new();
		this.eztexts = List.new();

		this.params = [ "pans", "atks", "rels", "cutoffs"];
		this.param_s = [this.client.cell_grid.pans, this.client.cell_grid.atks,
			           this.client.cell_grid.rels, this.client.cell_grid.cutoffs];

		this.single_entries = ["synthdef", "key", "scale"];
		this.single_entrie_s = [
			this.client.cell_grid.synthdef,
			this.client.cell_grid.key,
			this.client.cell_grid.scale];

		grid_x = this.bounds.width - 10;
		grid_y = this.bounds.height / (this.single_entries.size + this.params.size+1);

		this.single_entries.do({
			arg str, count;
			this.eztexts.add(EZText.new(
				parent: this,
				bounds: Rect(
					left: 0,
					top: grid_y * count,
					width:grid_x,
					height: grid_y),
				label: str,
				labelWidth: grid_x/4,
				textWidth: grid_x/4*3,
				labelHeight: grid_y)
			.value_(this.single_entrie_s[count]);
			);
		});



		this.params.do({
			arg param, count;
			this.textfields.add(
				RangeTextField(
					parent: this,
					bounds: Rect(
						left:  (0),
						top: (this.single_entries.size+count) * grid_y,
						width: grid_x,
						height: grid_y
					),
					labeltext: param,
					min: this.param_s.at(count)[0],
					max: this.param_s.at(count)[1]
				)
			);
		});

		this.update_button = Button(this, Rect(
			left: 10,
			top:  grid_y * ( this.single_entries.size + this.params.size),
			width:  grid_x-20,
			height: grid_y))
		.states_([["update all"]])
		.action_({
			arg b;
			var synthdef, key, scale, pans, atks, rels, cutoffs;

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
		});

	}

	createLists{
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