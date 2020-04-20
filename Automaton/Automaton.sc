CellGrid {
	var <>cells_x, <>cells_y, <>cell_size,
	<>grid,
	<>default_color,
	<>randomness,
	<>available_rules,
	<>available_rule_sets,
	<>current_rule_set,

	<>synthdef,
	<>key,
	<>scale,
	<>pans,
	<>atks,
	<>rels,
	<>cutoffs,
	<>master_volume_bus,
	<>master_synth,

	<>buffer,
	<>rates,
	<>pans,
	<>grainSizes,

	<>centroids;

	*new{|cells_x = 10, cells_y = 10, cell_size = 10|
		^super.newCopyArgs(cells_x, cells_y, cell_size).create;
	}

	create{
		this.default_color = Color(51, 51, 51);

		this.grid = Array.fill2D(this.cells_x, this.cells_y, {
			arg row, column;
			var cell, organelle;
			cell = Cell(
				[row, column],
				this.cell_size,
				Color(
					red: (row / this.cells_y).linlin(0, 1, 0.5, 1),
					green: (row * column / this.cells_y / this.cells_x).linlin(0, 1, 0.5, 1),
					blue: (column / this.cells_x).linlin(0, 1, 0.5, 1))
				).init;
			cell.die;
		});
		this.randomness = 0;

		this.available_rules = [
			"life",
			"replicator",
			"seeds",
			"34 life",
			"diamoeba",
			"2x2",
			"day_and_night",
			"anneal",
		];

		this.available_rule_sets = [
			[[3],[2,3]],
			[[1, 3, 5, 7], [1, 3, 5, 7]],
			[[2],[]],
			[[3, 4],[3, 4]],
			[[3, 5, 6, 7, 8],[5, 6, 7, 8]],
			[[3, 6], [1, 2, 5]],
			[[3, 6, 7, 8],[3, 4, 6, 7, 8]],
			[[4, 6, 7, 8], [3, 5, 6, 7, 8]],
		];

		this.current_rule_set =  this.available_rule_sets[0];
	}

	createControlBusses{|master_vol|
		this.master_volume_bus = master_vol;
	}

	setCentroids{|n_clusters|
		//should receive an array of argument dictionaries
		var dim = [this.cells_x, this.cells_y].sort[0];
		var center_cell = this.grid[(this.cells_x/2).floor][(this.cells_y/2).floor];
		var rightmost_cell = this.grid[dim-1][(dim/2).floor];

		var angle = 2pi / n_clusters;
		var r_edge = rightmost_cell.point - center_cell.point;
		this.centroids = List();

		n_clusters.do({
			arg count;
			var centroid;
			centroid = (r_edge.rotate(angle * count));
			centroid = centroid + center_cell.point;
			this.centroids.add(Centroid(
				centroid,
				nil
			)
			.color_(Color.rand(0.5, 0.8))
			.durptr_(0.01););
			centroid.x.floor.post; " ".post ; centroid.y.floor.postln;
			this.grid[centroid.x.floor][centroid.y.floor].is_centroid = true;
		});
	}

	setBuffers{|buf_list|
		if(buf_list.isKindOf(Collection).not, {
			buf_list = [buf_list];
		});

		this.centroids.do({
			arg centroid, count;
			var buffer = buf_list.wrapAt(count);
			centroid.bufferptr = Ref(buf_list.wrapAt(count));
			centroid.durptr =  `(this.cells_x/10 * buffer.numFrames * buffer.numChannels
				/ buffer.sampleRate).reciprocal;
		});
		this.cell_grid.do({
			arg row, columns;
			columns.do({
				arg cell, column;
				cell.dict[\dur] = column * cell.centroid.durptr.dereference;
			})
		});
	}

	setBufferAt{|index, buf|
		this.centroids[index].bufferptr = Ref(buf);
	}



	/*// //debug
	w = Window.new.front;
	w.alwaysOnTop_(true);
	a = AutomatonView(w, 0, 0, 12, 24, 10);
	a.drawFunc_(a.cell_grid.draw);


	a.cell_grid.setCentroids(3);

	a.cell_grid.grid.flat.do({
	arg cell;
	if(cell.is_centroid, {cell.live});
	})

	(
		var center_cell = a.cell_grid.grid[(a.cell_grid.cells_x/2).ceil][(a.cell_grid.cells_y/2).ceil];
		center_cell.live
	)
*/
	findClosestCentroid{|cell|
		var results = List.new();
		this.centroids.do({
			arg centroid, count;
			results.add(cell.point.dist(centroid.point));
		});
		^this.centroids[results.minIndex];
	}


	setBufferOrganelles{|synthdef, rates, pans, grainSizes|
		this.synthdef = synthdef;
		this.rates = rates;
		this.pans = pans;
		this.grainSizes = grainSizes;
		if(BufferOrganelle.note_cubby.isNil,{
			BufferOrganelle.note_cubby = 0!(128);
		});
		this.grid.collect({
			arg rows, column;
			rows.collect({
				arg cell, row;
				var rate, pan, dur, startPos, args, grainSize, centroid;

				// this.grainSizes.postln;
				// grainSizes.postln;

				centroid = this.findClosestCentroid(cell);
				cell.live_color = centroid.color;
				cell.dead_color = centroid.color.blend(Color.new255(51, 51,51), 0.8);

				rate = row.linexp(0, this.cells_y, rates[0], rates[1]);
				pan = column.linlin(0, this.cells_x, pans[0], pans[1]);
				grainSize = row.linlin(0, this.cells_x, grainSizes[0], grainSizes[1]);
				startPos = column * 0.01;

				// startPos.postln;
				args = Dictionary.newFrom(List[
					\rate, rate,
					\pan, pan,
					\startPos, startPos,
					\grainSize, grainSize,
					\gate, 1,
					\amp, this.master_volume_bus.asMap]);

				cell.organelle = BufferOrganelle(
					synthdef: synthdef,
					centroid: centroid,
					arg_dict: args);

				cell.organelle.midinote = column;
			});
		});

	}


	setCellOrganelles{|synthdef, key, scale , pans , atks, rels, cutoffs|
		var num_octaves = (this.cells_x / scale.size).ceil;
		var base_scale;

		this.synthdef = synthdef;
		this.key      = key;
		this.scale    = scale.asArray;
		this.pans     = pans;
		this.atks     = atks;
		this.rels     = rels;
		this.cutoffs  = cutoffs;

		"synthdef: ".post; this.synthdef.postln;
		"key: ".post; this.key.postln;
		"scale: ".post; this.scale.postln;
		"pans: ".post; this.pans.postln;
		"atks: ".post; this.atks.postln;
		"rels: ".post; this.rels.postln;
		"cutoffs: ".post; this.cutoffs.postln;


		base_scale = this.scale;
		scale = [];
		num_octaves.do({
			arg count;
			var new_scale;
			new_scale = base_scale + (12*count);
			scale = scale ++ new_scale;
		});
		scale = scale.flat - scale.mean.ceil + key;

		if(Organelle.note_cubby.isNil,{
			Organelle.note_cubby = 0!(128);
		});

		// scale.postln;
		this.grid.collect({
			arg columns, row;
			columns.collect({
				arg cell, column;
				// scale.wrapAt(row).postln;
				cell.organelle = Organelle(
					synthdef: synthdef,
					midinote: scale.wrapAt(row).clip(24, 96).asInt,
					cutoff: column.linexp(0, this.cells_x, cutoffs[0], cutoffs[1]),
					pan: column.linlin(0, this.cells_x, pans[0], pans[1]),
					atk: cell.color.red.linlin(cell.colorBounds.at(0), cell.colorBounds.at(1), atks[0], atks[1]),
					rel: cell.color.blue.linlin(cell.colorBounds.at(0), cell.colorBounds.at(1), rels[0], rels[1]),
					gate: 1
				);
				cell.organelle.amp_bus = this.master_volume_bus;
			});
		});
	}

	killAllOrganelles{
			this.grid.flat.do({
				arg cell;
				cell.organelle.kill;
				"killed".postln;
			});
	}
	stopAllOrganelles{
		Routine({
			this.grid.flat.do({
				arg cell;
				cell.organelle.stop;
			});
		}).play;
	}

	setMasterVol{|val|
		this.master_volume_bus.set(val);
	}

	showCells{
		this.grid.do({
			arg columns, row;
			columns.do({
				arg cell, idx;
				cell.post;
			});
		});
	}


	setRandomColor{
		// postf("inside \n");
		this.grid.flatten.do({
			arg cell;
			cell.color = Color.rand(0.0, 1.0);
		});
	}

	clear {
		this.grid.collect({
			arg column, row;
			column.collect({
				arg cell, idx;
				cell.die;
			});
		});
	}

	spawnLine {
		var a = this.cells_x.rand,
		b = this.cells_y.rand;
		("inserting center at " + a.asString + ", " + b.asString).postln;
		this.grid[a][b].live;
		this.grid[a-1][b].live;
		this.grid[a+1][b].live;
	}

	spawnRandom { |num|
		var a = this.cells_x.rand,
		b = this.cells_y.rand,
		col = this.grid.at(a),
		cell = col.at(b);
		cell.live;
		num.do({
			this.getNeighbors(a, b).choose.pop.live;
		});

	}


	spawnRandomAtPos { |num, x, y|
		var a = x , b = y,
		col = this.grid.at(a),
		cell = col.at(b);
		cell.live;
		num.do({
			arg count;
			this.getNeighbors(a, b).choose.pop.live;
		});
	}
	/////////////////////////////////////////////
	//////////////     DANIEL     /////////////
	/////////////////////////////////////////////

	//use cmd-D for help
	//this is how you define a function, note the curly brackets
	spawnGlider {
		//use x, y coordinates and access using this.grid[x][y]
		//this.grid[x][y] returns a cell (a pixel block)
		//to make the pixel ligth up, simply use this.grid[x][y].live
		var cell; //declare your new variables AT THE TOP
		this.cells_x; //this is the max x coordinate
		this.cells_y; //this is the max y coordinate
	}

	getState {
		^this.grid.collect({
			arg column, row;
			column.collect({
				arg cell;
				cell.isDead;
			});
		});
	}

	getNeighbors { |row, column|
		var cell, neighbors, idxC, idxR;
		var num_rows, num_cols;
		cell = this.grid[row][column];
		idxR = row - 1;
		idxC = column - 1;

		num_rows = 3;
		num_cols = 3;

		if(idxR < 0, {idxR = 0; num_rows = 2});
		if(idxR >= (this.cells_x-2), {idxR = this.cells_x - 2; num_rows = 2});
		if(idxC < 0, {idxC = 0; num_cols = 2});
		if(idxC >= (this.cells_y-2), {idxC = this.cells_y - 2; num_cols = 2});

		neighbors = List.fill2D(num_rows, num_cols, {
			arg r, c;
			var cell;
			// c.postln;
			cell = this.grid[idxR + r][idxC + c];

		});
		// neighbors.postln;
		^neighbors;
	}


	updateAll {
		this.grid.do({
			arg column, row;
			// "row change ; ".post;
			column.do({
				arg neighbors, c_idx, num_alive, this_cell;
				this_cell = this.grid[row][c_idx];
				if(this_cell.willDie, {
					this_cell.die
				}, {
					this_cell.live
				});
			});
		});
	}

	draw{
		^{
			this.grid.collect({
				arg column, row;
				column.collect({
					arg cell, idx;
					cell.draw();
				});
			});
		}
	}

	drawGameOfLife{|spawnRandomly = true|
		^{
			this.grid.scramble.do({
				arg column, row;
				// "row change ; ".post;
				column.scramble.do({
					arg cell, c_idx;
					var neighbors, num_alive, this_cell, rule;
					// "column change ; ".postln;
					neighbors = this.getNeighbors(row, c_idx);
					this_cell = this.grid[row][c_idx];

					num_alive = 0;
					/*
					neighbors.postln;
					this_cell.postln;*/
					this_cell.draw;
					neighbors.do({
						arg col, row;
						col.do({
							arg cell, count;
							if((this_cell.grid_pos == cell.grid_pos), {},{
								if(cell.isDead.not, {num_alive = num_alive + 1}, {});
							});
						});
					});
					// num_alive.postln;

					rule = this.current_rule_set;
					this_cell.willDie = true;
					if((this_cell.isDead.not  && (rule[1].find([num_alive]).isNil.not)),
						{this_cell.willDie = false;}); // survival
					if((this_cell.isDead      && (rule[0].find([num_alive]).isNil.not)),
						{this_cell.willDie = false;});


					if (spawnRandomly, {
						if ((-10.0.rrand(this.randomness)).asBoolean,{
							if (-1.0.rrand(1.0).asBoolean, {this_cell.live}, {this_cell.die});
						});
					});


				});
			});
			this.updateAll();
		}
	}

	drawRandom{
		^{
			this.grid.collect({
				arg column, row;
				column.collect({
					arg cell, idx;
					cell.color = Color.rand();
					cell.draw();
				});
			});
		}
	}
}

Centroid {
	var
	<>point,
	<>bufferptr,
	<>durptr,
	<>color;

	*new{|point, buffer|
		^super.newCopyArgs(point, buffer);
	}
}



Cluster {
	var
	<>centroid,
	<>cells_x,
	<>cells_y,
	<>color;

	*new{|centroid, cells_x, cells_y|
		^super.new.init(centroid, cells_x, cells_y);
	}

	init{|centroid, cells_x, cells_y|
		this.centroid = centroid;
		this.cells_x = cells_x;
		this.cells_y = cells_y;
	}



}


Arg2D {
	//map an arg on two dimensional space
	var
	<>bounds,
	<>arg_symbol,
	<>warp,
	<>direction;


	*new{|arg_symbol, bounds, warp|
		^super.new.init(arg_symbol, bounds, warp)

	}

	init{|arg_symbol, bounds, warp|
		this.arg_symbol = arg_symbol;
		this.bounds = bounds;
		this.warp = warp;
	}

	map{|x, y|
		if(direction == \x,{
			if(warp == \exp, {
				^x.linexp(0, 1, bounds.left, bounds.width);
			});
			if(warp == \lin, {
				^x.linlin(0, 1, bounds.left, bounds.width);
			});
		});
		if(direction == \y,{
			if(warp == \exp, {
				^x.linexp(0, 1, bounds.left, bounds.width);
			});
			if(warp == \lin, {
				^x.linlin(0, 1, bounds.left, bounds.width);
			});
		});
		^nil;
	}
}

