AutomatonView : UserView {
	var <>parent,
	<>left,
	<>top,
	<>cells_x,
	<>cells_y,
	<>cell_size,
	<>cell_grid,
	<>mousePressed;

	*new { |parent, left, top, cells_x, cells_y, cell_size|
		^super.new(parent, Rect(left, top, cells_x * cell_size, cells_y * cell_size))
		.background_(Color.black)
		.animate_(true)
		.frameRate_(8)
		.clearOnRefresh_(true).init(parent, left, top, cells_x, cells_y, cell_size);
	}

	init {|parent, left, top, cells_x, cells_y, cell_size|
		this.parent = parent;
		this.bounds = Rect(left, top, cells_x * cell_size, cells_y * cell_size);
		this.cells_x = cells_x;
		this.cells_y = cells_y;
		this.cell_size = cell_size;
		this.cell_grid = CellGrid(this.cells_x, this.cells_y, this.cell_size);
		this.cell_grid.postln;

	}

	drawRandom {
		this.drawFunc = this.cell_grid.drawRandom;
	}

	createControlBusses{|master_vol|
		this.cell_grid.master_volume_bus = master_vol;
	}


	gameOfLife {
		^{
			this.cell_grid.spawnRandom;
			this.cell_grid.gameOfLife;
			this.cell_grid.draw;
		};
	}

	spawnRandomOnClick{ |spawn_num=10|
		this.mouseDownAction = {
			this.mousePressed = true;
		};
		this.mouseMoveAction = {
			arg v, x, y;
			if (this.mousePressed,{
				if ((x > 0) &&
					(y > 0) &&
					(x < this.bounds.width)  &&
					(y < this.bounds.height), {

						x = (x / this.cell_size).floor;
						y = (y / this.cell_size).floor;
						this.cell_grid.spawnRandomAtPos(spawn_num, x, y);
				});
			});
		}
	}

}

