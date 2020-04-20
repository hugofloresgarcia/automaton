Cell {
	var <>grid_pos, <>size, <>color,
	<>live_color,
	<>dead_color,
	<>abs_pos,
	<>rect,
	<>willDie,
	<>colorBounds,
	<>organelle,
	<>is_centroid;

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
		this.live_color = this.color;
		this.dead_color = Color.new255(51, 51, 51);
		this.willDie = true;
		this.colorBounds = [0.6, 0.8];
		this.is_centroid = false;
	}

	point{
		^(this.grid_pos[0]@this.grid_pos[1]);
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
		this.color = this.dead_color;
		this.willDie = true;

	}

	live {
		this.color = this.live_color.vary(0.02);
		this.willDie = false;

	}

	draw {
		//I'm not sure if playing a tempoclock actually works
		Routine({
			if(this.isDead.not,{if(this.organelle.isNil.not, {this.organelle.play;});});
			if(this.isDead,    {if(this.organelle.isNil.not, {this.organelle.stop;})});
		}).play(TempoClock.default);

		Pen.fillColor = this.color;
		Pen.fillRect(this.rect);

		Pen.color = Color.new255(51, 51, 51);
		Pen.addRect(this.rect);


/*		if(this.organelle.isKindOf(BufferOrganelle),{
		if(this.organelle.isNil.not,{
			Pen.stringInRect(this.organelle.dict[\startPos], this.rect, Font("Helvetica", 9), Color.black);
		});
		});*/

		//debug: write ON if the synth is making a sound
/*		if(this.organelle.isNil.not, {
			if(this.organelle.synth.isPlaying, {
				Pen.stringInRect("ON", this.rect, Font("Helvetica", 9), Color.black);
			});
		});*/

/*		//debug: write REL if the synth is releasing
		if(this.organelle.isNil.not, {
			if(this.organelle.currently_releasing, {
				Pen.stringInRect("REL", this.rect, Font("Helvetica", 9), Color.black);
				Pen.fillColor = this.color.blend(Color.grey);
				Pen.fillRect(this.rect);
			});
		});*/

		Pen.stroke;
	}
}


Organelle {
	classvar <>note_cubby,
	<>default_max_note_frequency = 2,
	<>max_note_frequency = 2,
	<>total_synth_count = 0,
	<>absolute_max_synth_count = 50;
	var
	<>synthdef = nil,
	<>midinote = 60,
	<>currently_releasing,
	<>cutoff = 1000,
	<>atk = 0.1,
	<>pan = 0,
	<>rel = 1,
	<>gate = 1,
	<>amp_bus,
	<>synth = nil;

	*new{ |synthdef, midinote, cutoff, atk, rel, gate, pan, amp_bus|
		^super.new.init(synthdef, midinote, cutoff, atk, rel, gate, amp_bus);
	}

	init{|synthdef, midinote, cutoff, atk, rel, gate, pan, amp_bus|
		this.synthdef = synthdef;
		this.midinote = midinote;
		this.cutoff = cutoff;
		this.atk = atk;
		this.rel = rel;
		this.gate = gate;
		this.pan = pan;
		this.amp_bus = amp_bus;
		this.currently_releasing = false;

	}

	play {

		//dynamically change the maximun number of synths allowed in each midinote cubby between 1 and 3
		//1 would alllow for more pitches
		//3 would allow for different
		//more pitches gets priority
		if(total_synth_count > absolute_max_synth_count, {
			max_note_frequency = max_note_frequency - 1;
		}, {
			max_note_frequency = max_note_frequency + 1;
		});
		max_note_frequency = max_note_frequency.clip(1, 3);

		// if there are less than three
		if((note_cubby.at(this.midinote) < max_note_frequency) &&
			(total_synth_count < absolute_max_synth_count), {


			if (this.synth.isNil, { // if synth is nil, create a new instance
				this.synth = Synth(this.synthdef.asSymbol, [
					\midi_note, this.midinote,
					\cutoff, this.cutoff,
					\atk, this.atk,
					\rel, this.rel,
					\pan, this.pan,
					\gate, 1,
					\amp, this.amp_bus.asMap
				]);
					NodeWatcher.register(this.synth, true); // register to check if .isPlaying
				// "node ".post; this.synth.nodeID.post; " created".postln;
				this.currently_releasing = false;
				note_cubby[this.midinote] = note_cubby[this.midinote] + 1; // counting to set a max limit
				total_synth_count = total_synth_count + 1; // counting to set  a max limit
			});
		});
	}


	stop {
		if (this.synth.isNil.not,{      // only release if synth exists
			// this.currently_releasing.postln;
			if(this.currently_releasing.not, {
				// if(this.synth.isPlaying,{   // only release if synth is playing
				this.synth.set(\gate, 0); // set gate to 0 to release properly
				// "node ".post; this.synth.nodeID.post; " released".postln;

				NodeWatcher.unregister(this.synth); //unregister node from nodeWatcher
				this.currently_releasing = true;
				this.rel.wait; //we're doing this inside a routine, so waiting is allowed

				this.synth.release(-1); //releases the synth NOW
				this.synth = nil; // I NEED TO GET RID OF THIS

				// "synth released".postln;
				this.currently_releasing = false;

				note_cubby[this.midinote] = note_cubby[this.midinote] - 1; //one fewer node
				total_synth_count = total_synth_count - 1; // one fewer node
				"total synths: ".post; total_synth_count.postln;

			});
		});

	}

	kill {
		// THIS CAN BE CALLED FROM THE MAIN THREAD
		// SO IT MEANS I CAN STOP SHIT ABRUPTLY
		// USE IF UR DELETING THE REFERENCES TO THE SYNTHS IN THE NEAR FUTURE
		if (this.synth.isNil.not,{      // only release if synth exists
			// this.currently_releasing.postln;
			if(this.currently_releasing.not, {
				// if(this.synth.isPlaying,{   // only release if synth is playing
				this.synth.set(\gate, 0); // set gate to 0 to release properly
				// "node ".post; this.synth.nodeID.post; " released".postln;

				NodeWatcher.unregister(this.synth); //unregister node from nodeWatcher
				this.currently_releasing = true;

				this.synth.release(-1); //releases the synth NOW
				this.synth = nil; // I NEED TO GET RID OF THIS

				// "synth released".postln;
				this.currently_releasing = false;

				note_cubby[this.midinote] = note_cubby[this.midinote] - 1; //one fewer node
				total_synth_count = total_synth_count - 1; // one fewer node
				"total synths: ".post; total_synth_count.postln;

			});
		});
	}

}

//A BUFFER CLASS I WILL USE LATER

BufferOrganelle : Organelle{
	classvar <>note_cubby,
	<>default_max_note_frequency = 2,
	<>max_note_frequency = 2,
	<>total_synth_count = 0,
	<>absolute_max_synth_count = 500;
	var
	<>synthdef,
	<>centroid,
	<>synth,
	<>arg_dict,
	<>rel,
	<>dict;

	*new{ |synthdef,centroid, arg_dict|
		arg_dict.postln;
		^super.new.binit(synthdef,centroid, arg_dict);
	}

	binit{|synthdef, centroid, arg_dict|
		// arg_dict.postln;
		this.synthdef = synthdef;
		this.centroid = centroid;
		this.arg_dict = arg_dict;
		this.currently_releasing = false;
		this.rel = `0.1;
	}

	play {
		//dynamically change the maximun number of synths allowed in each midinote cubby between 1 and 3
		//1 would alllow for more pitches
		//3 would allow for different
		//more pitches gets priority
		if(total_synth_count > absolute_max_synth_count, {
			max_note_frequency = max_note_frequency - 1;
		}, {
			max_note_frequency = max_note_frequency + 1;
		});
		max_note_frequency = max_note_frequency.clip(1, 2);

		// if there are less than three
		if(
			(total_synth_count < absolute_max_synth_count), {
				if (this.synth.isNil, { // if synth is nil, create a new instance

					this.arg_dict.add(\buffer -> this.centroid.bufferptr.dereference);
					this.arg_dict.add(\dur -> this.centroid.durptr.dereference);


					this.synth = Synth(this.synthdef.asSymbol, this.arg_dict.asPairs(Array));
					NodeWatcher.register(this.synth, true); // register to check if .isPlaying
					// "node ".post; this.synth.nodeID.post; " created".postln;
					this.currently_releasing = false;
					note_cubby[this.midinote] = note_cubby[this.midinote] + 1; // counting to set a max limit
					total_synth_count = total_synth_count + 1; // counting to set  a max limit
				});
		});
	}

	stop {
		if (this.synth.isNil.not,{      // only release if synth exists
			// this.currently_releasing.postln;
			if(this.currently_releasing.not, {
				// if(this.synth.isPlaying,{   // only release if synth is playing
				this.synth.set(\gate, 0); // set gate to 0 to release properly
				// "node ".post; this.synth.nodeID.post; " released".postln;

				NodeWatcher.unregister(this.synth); //unregister node from nodeWatcher
				this.currently_releasing = true;
				this.rel.dereference.wait; //we're doing this inside a routine, so waiting is allowed

				this.synth.release(-1); //releases the synth NOW
				this.synth = nil; // I NEED TO GET RID OF THIS

				// "synth released".postln;
				this.currently_releasing = false;

				note_cubby[this.midinote] = note_cubby[this.midinote] - 1; //one fewer node
				total_synth_count = total_synth_count - 1; // one fewer node
				"total synths: ".post; total_synth_count.postln;

			});
		});

	}

	kill {
		// THIS CAN BE CALLED FROM THE MAIN THREAD
		// SO IT MEANS I CAN STOP SHIT ABRUPTLY
		// USE IF UR DELETING THE REFERENCES TO THE SYNTHS IN THE NEAR FUTURE
		if (this.synth.isNil.not,{      // only release if synth exists
			// this.currently_releasing.postln;
			if(this.currently_releasing.not, {
				// if(this.synth.isPlaying,{   // only release if synth is playing
				this.synth.set(\gate, 0); // set gate to 0 to release properly
				// "node ".post; this.synth.nodeID.post; " released".postln;

				NodeWatcher.unregister(this.synth); //unregister node from nodeWatcher
				this.currently_releasing = true;

				this.synth.release(-1); //releases the synth NOW
				this.synth = nil; // I NEED TO GET RID OF THIS

				// "synth released".postln;
				this.currently_releasing = false;

				note_cubby[this.midinote] = note_cubby[this.midinote] - 1; //one fewer node
				total_synth_count = total_synth_count - 1; // one fewer node
				"total synths: ".post; total_synth_count.postln;

			});
		});
	}
}
