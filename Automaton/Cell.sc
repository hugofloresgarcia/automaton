Cell {
	var <>grid_pos, <>size, <>color,
	<>abs_pos,
	<>rect,
	<>willDie,
	<>colorBounds,
	<>organelle;

	*new { |grid_pos, size, color|
		^super.newCopyArgs(grid_pos, size, color);
	}

	post{
		"a cell with".postln;
		"    position: ".post; this.grid_pos.postln;
		"    midinote: ".post; this.organelle.midinote.postln;
	}


	init{
		this.abs_pos = this.grid_pos * this.size;
		this.rect = Rect(this.abs_pos[0], this.abs_pos[1], this.size, this.size);
		this.willDie = true;
		this.colorBounds = [0.6, 0.8]
	}


	getColorMagnitude{
		var mag;
		^mag = (this.color.red * 0.3) + (this.color.green * 0.59) + (this.color.blue * 0.11)
	}


	isDead {
		if (this.getColorMagnitude(this.color) < 0.5, {
			^true
		}, {
			^false
		});
	}

	die {
		this.color = Color.new255(51, 51, 51);
		this.willDie = true;

	}

	live {
		this.color = Color.rand(this.colorBounds[0], this.colorBounds[1]);
		this.willDie = false;

	}

	draw {
		Routine({
			if(this.isDead.not,{if(this.organelle.isNil.not, {this.organelle.play;});});
			if(this.isDead,    {if(this.organelle.isNil.not, {this.organelle.stop;})});
		}).play(TempoClock.default);




		Pen.fillColor = this.color;
		Pen.fillRect(this.rect);

		Pen.color = Color.new255(51, 51, 51);
		Pen.addRect(this.rect);

		if(this.isDead, {
/*			this.organelle.synth.get(\gate, {
				arg v;
				if(v == 1, {
					"shit".postln;
				});
			});*/
		});

/*		Pen.stringInRect(
			{var ou; if(this.organelle.synth == nil,
				{ou = "";},
				{ou = this.organelle.synth.nodeID.asString}); ou}.value,
			this.rect, Font("Helvetica", 9), Color.white);*/
		Pen.stroke;
	}
}


Organelle {
	classvar <>note_cubby,
	<>default_max_note_frequency = 2,
	<>max_note_frequency = 2,
	<>total_synth_count = 0,
	<>absolute_max_synth_count = 100;
	var
	<>synthdef = nil,
	<>midinote = 60,
	<>cutoff = 1000,
	<>atk = 0.1,
	<>pan = 0,
	<>rel = 1,
	<>gate = 1,
	<>amp_bus,
	<>synth = nil;

	*new{ |synthdef, midinote, cutoff, atk, rel, gate, pan, amp_bus|
		^super.newCopyArgs(synthdef, midinote, cutoff, atk, rel, gate, amp_bus);
	}

	play {

		if(total_synth_count > absolute_max_synth_count, {
			max_note_frequency = max_note_frequency - 1;
		}, {
			max_note_frequency = max_note_frequency + 1;
		});
		max_note_frequency = max_note_frequency.clip(1, 3);

/*		note_cubby.postln;
		note_cubby.size.postln;
		this.midinote.postln;*/

		if(note_cubby.at(this.midinote) < max_note_frequency, {

			//
			// "note ".post; this.midinote.postln;
			// "note freq ".post; note_cubby.at(this.midinote).postln;
			// "total_synth ".post; total_synth_count.postln;
			// "max ".post; max_note_frequency.postln;

			if (this.synth.isNil, {
				this.synth = Synth(this.synthdef.asSymbol, [
					\midi_note, this.midinote,
					\cutoff, this.cutoff,
					\atk, this.atk,
					\rel, this.rel,
					\pan, this.pan,
					\gate, 1,
					\amp, this.amp_bus.asMap
				]);
				NodeWatcher.register(this.synth, true);

				note_cubby[this.midinote] = note_cubby[this.midinote] + 1;
				total_synth_count = total_synth_count + 1;
			},{
				if(this.synth.isPlaying, {
				},{
					this.synth = Synth(this.synthdef.asSymbol, [
						\midi_note, this.midinote,
						\cutoff, this.cutoff,
						\atk, this.atk,
						\rel, this.rel,
						\pan, this.pan,
						\gate, 1,
						\amp, this.amp_bus.asMap
					]);
					NodeWatcher.register(this.synth, true);

					note_cubby[this.midinote] = note_cubby[this.midinote] + 1;
					total_synth_count = total_synth_count + 1;
				});
				// this.synth.nodeID.post; " was triggered".postln;
				/*				this.synth.get(\gate, {|v|
				// v.postln;
				if(v == 0, {
				this.synth.set(\gate, 1);
				note_cubby[this.midinote] = note_cubby[this.midinote] + 1;
				total_synth_count = total_synth_count + 1;
				});
				});*/
				/*note_cubby[this.midinote] = note_cubby[this.midinote] + 1;
				total_synth_count = total_synth_count + 1;*/
			});
		});
	}

	stop {
		if (this.synth.isNil.not,{
			if(this.synth.isPlaying,{
				this.synth.set(\gate, 0);
				NodeWatcher.unregister(this.synth);
				this.synth.free;
				note_cubby[this.midinote] = note_cubby[this.midinote] - 1;
				total_synth_count = total_synth_count - 1;
			}, {
			});

				/*if(v == 1, {
					// "killing".postln;
					this.synth.set(\gate, 0);
					note_cubby[this.midinote] = note_cubby[this.midinote] - 1;
					total_synth_count = total_synth_count - 1;
				});*/
		});

	}

/*		if(this.synth.isNil.not, {
			if(this.synth.isPlaying, {
				// "synth playing".postln;
				NodeWatcher.unregister(this.synth);
				this.synth.set(\gate, 0);
				this.synth.release(-1);
				this.synth.free;
			}, {

			});
			this.synth = nil;
			note_cubby[this.midinote] = note_cubby[this.midinote] - 1;
			total_synth_count = total_synth_count - 1;

		});*/
		// total_synth_count = total_synth_count - 1;

}

BufferOrganelle : Organelle {
	*new{ |synthdef, midinote, cutoff, atk, rel, gate, pan|
		^super.newCopyArgs(synthdef, midinote, cutoff, atk, rel, gate);
	}
}
