s.waitForBoot({
	////////////////////////////////////////////////////
	////////////////////////////////////////////////////
	////////////////     SOUNDS     ///////////////////
	////////////////////////////////////////////////////
	////////////////////////////////////////////////////

	(
		SynthDef(
			\particle, {
				arg midi_note = 60, amp = 0.3, cutoff = 3000, gain = 2, detune = 0, atk=0.1, rel = 1, num_voices = 10;
				var sig, env, freq;

				freq = (midi_note + detune).midicps;

				atk =  Rand(atk-atk/10, atk+atk/10);

				env = EnvGen.ar(
					envelope: Env.perc(atk, rel),
					doneAction: 2);

				sig = LFSaw.ar(
					freq: freq + (LFNoise0.kr(freq, freq/20)),
					mul: amp + LFNoise0.kr(Rand(1, 5)).range(-0.2, 0.2)).distort;

				sig = MoogFF.ar(
					in: sig,
					freq: cutoff,
					gain: gain);

				sig = Splay.ar(sig);

				sig = sig * env * amp;

				Out.ar(0, sig);
		}).add;
	);

	////////////////////////////////////////////////////
	////////////////////////////////////////////////////
	////////////////////////////////////////////////////
	////////////////////MAIN WINDOW///////////////////
	////////////////////////////////////////////////////
	////////////////////////////////////////////////////
	(
		~mainMenu = {
			var bounds = Window.availableBounds;
			var cells_x = 24, cells_y = 18, cell_size = 20,
			xtext, ytext, stext;
			w = Window.new(
				name: "automata",
				bounds: Rect(
					left: bounds.width/2,
					top: bounds.height/2,
					width: 320,
					height: 120),
				resizable: false)
			.front
			.alwaysOnTop_(true)
			.background_(Color.rand(0.6, 0.8));


			StaticText(
				parent: w,
				bounds:  Rect(10, 0, 300, 30))
			.string_("the automaton factory!")
			.align_(\center);

			xtext = EZText(
				parent: w,
				bounds: Rect(0, 40, 100, 30),
				label: "cells in x")
			.value_(cells_x)
			.action_({|v| cells_x = v.value.asInteger.clip(1, 30)});

			ytext = EZText(
				parent: w,
				bounds:  Rect(100, 40, 100, 30),
				label: "cells in y")
			.value_(cells_y)
			.action_({|v| cells_y = v.value.asInteger.clip(1, 30)  ; cells_y.postln;});

			stext = EZText(
				parent: w,
				bounds:  Rect(200, 40, 100, 30),
				label: "cell size")
			.value_(cell_size)
			.action_({|v| cell_size = v.value.asInteger.clip(1, bounds.height)});

			Button(
				parent: w,
				bounds:  Rect(10, 80, 300, 30))
			.states_([["go!"]])
			.action_({~makeAutomatonWindow.value(
				xtext.textField.value.asInteger.clip(5, 30),
				ytext.textField.value.asInteger.clip(5, 30),
				stext.textField.value.asInteger.clip(10, bounds.height))});

		}
	);

	////////////////////////////////////////////////////
	////////////////////////////////////////////////////
	////////////////////////////////////////////////////
	////////////////////////////////////////////////////
	(
		~makeAutomatonWindow = {
			arg cells_x, cells_y, cell_size;
			var init_x, init_y, width, height, bounds;
			bounds = Window.availableBounds;
			init_x = bounds.width.rand;
			init_y = bounds.height.rand;

			~cells_x = cells_x;
			~cells_y = cells_y;
			~cell_size = cell_size;
			~v_rows = 1;
			~v_cols = 1;
			~pad = 10;
			~view_dim_x = ~cells_x * ~cell_size + (~pad);
			~view_dim_y = ~cells_y * ~cell_size + (~pad);

			~control_bar_y = 12 * ~cell_size;

			~window_x = ~v_cols * (~view_dim_x) + ~pad;
			~window_y = ~v_rows * (~view_dim_y) + ~control_bar_y+~pad+~pad;

			~control_bar_x = ~window_x - (2*~pad); //~cells_x * ~cell_size * ~v_cols + (~pad *2);
			////////////////////////////////////////////////////
			////////////////////////////////////////////////////
			////////////////      WINDOW     ///////////////////
			////////////////////////////////////////////////////
			////////////////////////////////////////////////////
			~window = Window.new(
				name: "automaton",
				bounds: Rect(init_x, init_y, ~window_x, ~window_y),
				resizable: false,
				border: true,
				server: s,
				scroll: false);

			~window.front;
			~window.alwaysOnTop = true;
			// ~window.onClose = { "window closed! ".postln;};

			////////////////////////////////////////////////////
			////////////////////////////////////////////////////
			////////////////      CELLS     ///////////////////
			////////////////////////////////////////////////////
			////////////////////////////////////////////////////

			~view_array = Array.fill2D(~v_rows, ~v_cols, {
				arg row, column;
				var view;
				view = AutomatonView(
					parent: ~window,
					left: (column * ~view_dim_x) + ~pad,
					top: (row * ~view_dim_y) + ~pad,
					cells_x: ~cells_x,
					cells_y: ~cells_y,
					cell_size: ~cell_size)
				.spawnRandomOnClick;
				// .clickForControl;

				15.do({view.cell_grid.spawnRandom});
				view.drawFunc = view.cell_grid.drawGameOfLife(spawnRandomly: true);
				view.cell_grid.setCellOrganelles(
					synthdef: \particle,
					key: 48,
					scale: [0, 2, 4, 7, 9, 11, 14],
					pans: [-1.0, 1.0],
					atks: [0.01, 0.1],
					rels:    [0.5, 0.1],
					cutoffs: [500, 1500]);
				view;
			});

			////////////////////////////////////////////////////
			////////////////////////////////////////////////////
			////////////////    CONTROL BAR     ///////////////////
			////////////////////////////////////////////////////
			////////////////////////////////////////////////////

			~control_bar = View(
				parent: ~window,
				bounds: Rect(
					left: ~pad,
					top: (~view_dim_y) * ~v_rows + ~pad ,
					width: ~control_bar_x,
					height: ~control_bar_y))
			.background_(Color.rand())
			.visible_(true);


			////////////////////////////////////////////////////
			///////////////   INDIVIDUAL CONTROL ///////////////
			////////////////////////////////////////////////////

			// ~view_array.flatten[0].grid.flatten[100].organelle.inspect;

			~control_control_bar = AutomatonControlView(
				parent: ~control_bar,
				bounds: Rect(
					left: ~control_bar.bounds.width / 2,
					top: 0,
					width: ~control_bar.bounds.width / 2,
					height: ~control_bar.bounds.height),
				client: ~view_array.flatten.at(0)
			).createAll;


			~sound_control_bar = AutomatonSoundView(
				parent: ~control_bar,
				bounds: Rect(
					left: 0,
					top: 0,
					width: ~control_bar.bounds.width / 2,
					height: ~control_bar.bounds.height),
				client: ~view_array.flatten.at(0)
			).textfields[0].getBounds;
		}
	);
	////////////////////////////////////////////////////
	////////////////////////////////////////////////////
	////////////////////////////////////////////////////
	////////////////////////////////////////////////////
	~mainMenu.value
}
)

