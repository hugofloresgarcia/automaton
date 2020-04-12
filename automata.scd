
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
////////////////////////////////////////////////////

(
var init_x, init_y, width, height, bounds;
bounds = Window.availableBounds;
init_x = bounds.width / 2;
init_y = bounds.height / 2;

~cells_x = 36;
~cells_y = 24;
~cell_size = 20;
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
	resizable: true,
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
)

~view_array.flat[0].cell_grid.showCells

n = " [0, 2, 4, 7, 9, 11, 14 ]".replace("[", "").replace("]", "").split($,).collect({
	arg char;
	char.asInteger;
});

n + 2

