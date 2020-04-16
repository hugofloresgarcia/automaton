/// A WAY TO PEEK INTO THE CELLS AND THEIR ORGANELLES
// grid[x][y[ for their position
~view_array.flat[0].cell_grid.killAllOrganelles
~view_array.flat[0].cell_grid.grid[0][0].organelle.synth.isPlaying
~view_array.flat[0].cell_grid.grid[0][0].organelle.synth.isRunning
~view_array.flat[0].cell_grid.grid[0][1].organelle.synth.get(\gate, {arg v; v.postln;})
~view_array.flat[0].cell_grid.grid[0][1].isDead

(
s.waitForBoot({
	////////////////////////////////////////////////////
	////////////////////////////////////////////////////
	////////////////     SOUNDS     ///////////////////
	////////////////////////////////////////////////////
	////////////////////////////////////////////////////

	(
		(
			SynthDef(
				\test, {
					arg midi_note = 60, amp = 0.3, gate =1;
					var sig;
					sig = SinOsc.ar(
						freq: midi_note.midicps,
						mul: EnvGen.kr(
							envelope: Env.adsr,
							gate: gate,
							doneAction: 2
						);
					);
					Out.ar(0, sig * amp);
			}).add;
		);
		(
			SynthDef(
				\sawtooth, {
					arg midi_note = 60, amp = 0.3,
					cutoff = 3000, gain = 2, gate = 1, detune = 0, atk=3, rel=1, num_voices = 10,
					master_bus=0;
					var sig, env, freq;

					freq = (midi_note + detune).midicps;

					atk =  Rand(atk-atk/10, atk+atk/10);
					env = EnvGen.ar(
						envelope: Env.adsr(attackTime:atk, releaseTime: rel),
						gate: gate,
						doneAction: 2);

					sig = Saw.ar(
						freq: (XLine.kr(
							start: freq/8,
							end: freq,
							dur: atk,
						) + (LFNoise0.kr(freq, freq/20))),
						mul: amp +
						LFNoise0.kr(Rand(1, 5)).range(-0.2, 0.2));

					sig = MoogFF.ar(
						in: sig,
						freq: cutoff,
						gain: gain);

					sig = Splay.ar(sig);

					sig = sig * env * amp * 0.3;

					Out.ar(master_bus, sig);
			}).add;
		);




		SynthDef(
			\lofitri, {
				arg midi_note = 60, amp = 0.3, cutoff = 3000, gain = 2, detune = 0,
				atk=0.1, rel = 1, num_voices = 10, master_bus=0, gate = 1;
				var sig, env, freq;

				freq = (midi_note + detune).midicps;

				atk =  Rand(atk-atk/10, atk+atk/10);

				env = EnvGen.kr(
					envelope: Env.adsr(attackTime:atk, releaseTime: rel),
					gate: gate,
					doneAction: 2);

				sig = LFTri.ar(
					freq: freq + (LFNoise0.kr(freq, freq/20)),
					mul: env + LFNoise0.kr(Rand(1, 5)).range(-0.2, 0.2)).distort;

				sig = MoogFF.ar(
					in: sig,
					freq: cutoff,
					gain: gain);

				sig = Splay.ar(sig);

				sig = sig * amp * 0.3;

				Out.ar(master_bus, sig);
		}).add;


		SynthDef(
			\lofisaw, {
				arg midi_note = 60, amp = 0.3, cutoff = 3000, gain = 2,
				detune = 0, atk=0.1, rel = 1, num_voices = 10, master_bus=0, gate = 1;
				var sig, env, freq;

				freq = (midi_note + detune).midicps;

				atk =  Rand(atk-atk/10, atk+atk/10);

				env = EnvGen.kr(
					envelope: Env.adsr(attackTime:atk, releaseTime: rel),
					gate: gate,
					doneAction: 2);

				sig = LFSaw.ar(
					freq: freq + (LFNoise0.kr(freq, freq/20)),
					mul: env + LFNoise0.kr(Rand(1, 5)).range(-0.2, 0.2)).distort;

				sig = MoogFF.ar(
					in: sig,
					freq: cutoff,
					gain: gain);

				sig = Splay.ar(sig);

				sig = sig * amp * 0.3;

				Out.ar(master_bus, sig);
		}).add;

		SynthDef(
			\lofisq, {
				arg midi_note = 60, amp = 0.3, cutoff = 3000, gain = 2,
				detune = 0, atk=0.1, rel = 1, num_voices = 10, master_bus=0, gate = 1;
				var sig, env, freq;

				freq = (midi_note + detune).midicps;

				atk =  Rand(atk-atk/10, atk+atk/10);

				env = EnvGen.kr(
					envelope: Env.adsr(attackTime:atk, releaseTime: rel),
					gate: gate,
					doneAction: 2);

				sig = LFPulse.ar(
					freq: freq + (LFNoise0.kr(freq, freq/20)),
					mul: env + LFNoise0.kr(Rand(1, 5)).range(-0.2, 0.2)).distort;

				sig = MoogFF.ar(
					in: sig,
					freq: cutoff,
					gain: gain);

				sig = Splay.ar(sig);

				sig = sig * amp * 0.3;

				Out.ar(master_bus, sig);
		}).add;


		SynthDef(
			\lofisin, {
				arg midi_note = 60, amp = 0.3, cutoff = 3000, gain = 2,
				detune = 0, atk=0.1, rel = 1, num_voices = 10, master_bus=0, gate = 1;
				var sig, env, freq;

				freq = (midi_note + detune).midicps;

				atk =  Rand(atk-atk/10, atk+atk/10);

				env = EnvGen.kr(
					envelope: Env.adsr(attackTime:atk, releaseTime: rel),
					gate: gate,
					doneAction: 2);

				sig = SinOsc.ar(
					freq: freq + (LFNoise0.kr(freq, freq/20)),
					mul: env + LFNoise0.kr(Rand(1, 5)).range(-0.2, 0.2)).distort;

				sig = MoogFF.ar(
					in: sig,
					freq: cutoff,
					gain: gain);

				sig = Splay.ar(sig);

				sig = sig * amp * 0.3;

				Out.ar(master_bus, sig);
		}).add;

	);

	////////////////////////////////////////////////////
	////////////////////////////////////////////////////
	////////////////////////////////////////////////////
	////////////////////MAIN WINDOW///////////////////
	////////////////////////////////////////////////////
	////////////////////////////////////////////////////
	(
		~liveAutomatons = List.new();
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
					height: 160),
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
				bounds: Rect(0, 40, 150, 30),
				label: "cells in x")
			.value_(cells_x)
			.action_({|v| cells_x = v.value.asInteger.clip(1, 30)});

			ytext = EZText(
				parent: w,
				bounds:  Rect(150, 40, 150, 30),
				label: "cells in y")
			.value_(cells_y)
			.action_({|v| cells_y = v.value.asInteger.clip(1, 30)  ; cells_y.postln;});
/*
			stext = EZText(
				parent: w,
				bounds:  Rect(200, 40, 100, 30),
				label: "cell size")
			.value_(cell_size)
			.action_({|v| cell_size = v.value.asInteger.clip(1, bounds.height)});*/

			Button(
				parent: w,
				bounds:  Rect(10, 80, 300, 30))
			.states_([["go!"]])
			.action_({~liveAutomatons.add(
				~makeAutomatonWindow.value(
				xtext.textField.value.asInteger.clip(3, 30),
				ytext.textField.value.asInteger.clip(3, 30),
			/*				stext.textField.value.asInteger.clip(10, bounds.height)*/))
			});

			EZText(
				parent: w,
				bounds: Rect(10, 120, 150, 30),
				label: "master key")
			.value_(48)
			.action_({
				arg v;
				~liveAutomatons.do({
					arg sound_ctrl;
					sound_ctrl.eztexts.at(1).valueAction_(v.textField.value.asInt);
					sound_ctrl.update_button.doAction;
				});
			});
			Organelle.note_cubby_(nil);
		}
	);

	////////////////////////////////////////////////////
	////////////////////////////////////////////////////
	////////////////////////////////////////////////////
	////////////////////////////////////////////////////
	(
		~makeAutomatonWindow = {
			arg
			cells_x = 24,
			cells_y = 12,
			synthdef = \sawtooth,
			key = 48,
			scale = [0, 2, 4, 7, 9];



			var init_x, init_y, width, height, bounds,
			cell_size,
			v_rows, v_cols, pad,
			view_dim_x, view_dim_y,
			control_bar_y, control_bar_x,
			window_x, window_y;

			bounds = Window.availableBounds;
			init_x = bounds.width.rand;
			init_y = bounds.height.rand;


			cells_x = cells_x;
			cells_y = cells_y;
			cell_size =  30 * 20 / cells_x;
			v_rows = 1;
			v_cols = 1;
			pad = 10;
			view_dim_x = cells_x * cell_size + (pad);
			view_dim_y = cells_y * cell_size + (pad);

			control_bar_y = 8 * 20;

			~window_x = v_cols * (view_dim_x) + pad;
			~window_y = v_rows * (view_dim_y) + control_bar_y+pad+pad;

			control_bar_x = 1* (view_dim_x) + pad - (2*pad); //cells_x * cell_size * v_cols + (pad *2);
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

			~view_array = Array.fill2D(v_rows, v_cols, {
				arg row, column;
				var view;
				view = AutomatonView(
					parent: ~window,
					left: (column * view_dim_x) + pad,
					top: (row * view_dim_y) + pad,
					cells_x: cells_x,
					cells_y: cells_y,
					cell_size: cell_size)
				.spawnRandomOnClick
				.frameRate_(6)
				.createControlBusses(Bus.control(s, 1))
				.onClose_({
					arg v;
					v.cell_grid.killAllOrganelles();
				});
				// .clickForControl;

				15.do({view.cell_grid.spawnRandom});
				view.cell_grid.setCellOrganelles(
					synthdef: synthdef,
					key: key,
					scale: scale,
					pans: [-1.0, 1.0],
					atks: [0.01, 0.1],
					rels:    [0.2, 0.5],
					cutoffs: [500, 5000]);
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
					left: pad,
					top: (view_dim_y) * v_rows + pad ,
					width: control_bar_x,
					height: control_bar_y))
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
					width: ~control_bar.bounds.width *9/ 16,
					height: ~control_bar.bounds.height),
				client: ~view_array.flatten.at(0)
			).createAll;

			~sound_control_bar = AutomatonSoundView(
				parent: ~control_bar,
				bounds: Rect(
					left: 0,
					top: 0,
					width: ~control_bar.bounds.width *7/ 16,
					height: ~control_bar.bounds.height),
				client: ~view_array.flatten.at(0)
			);

			~view_array.flat.do({
				arg view;
				view.drawFunc = view.cell_grid.drawGameOfLife(spawnRandomly: true);
			});
			~sound_control_bar;
		};

	);

	////////////////////////////////////////////////////
	////////////////////////////////////////////////////
	////////////////////////////////////////////////////
	////////////////////////////////////////////////////
	~mainMenu.value
}
);
)


/*a = Synth(\sawtooth);
NodeWatcher.new(s)
NodeWatcher.register(a, true)
a.set(\gate, 0);
a.isRunning
a.isPlaying

3.post;
~view_array.flat[0].cell_grid.master_volume_bus.getSynchronous
~view_array.flat[0].cell_grid.grid[10][6].organelle.amp_bus
~view_array.flat[0].cell_grid.grid[10][6].organelle.synth.set(\gate, 0)
~view_array.flat[0].cell_grid.grid[10][6].isDead

~view_array.flat[0].cell_grid.killAllOrganelles
~view_array.flat[0].cell_grid.grid[0][0].organelle.synth.isPlaying
~view_array.flat[0].cell_grid.grid[0][0].organelle.synth.isRunning
~view_array.flat[0].cell_grid.grid[0][1].organelle.synth.get(\gate, {arg v; v.postln;})
~view_array.flat[0].cell_grid.grid[0][1].isDead


Organelle.note_cubby = 0!(128)
Organelle.note_cubby
a = Cell([0, 0], 10, Color.red)
a.organelle = Organelle(\sawtooth, 60, 3000, 0.1, 1, 1, 0)

a.organelle.play;
a.organelle.stop

a = Synth(\sawtooth);
a.release(-1)
a.free
Organelle.
a.set(\gate, 1);
a.set(\gate, 0)
a.release*/