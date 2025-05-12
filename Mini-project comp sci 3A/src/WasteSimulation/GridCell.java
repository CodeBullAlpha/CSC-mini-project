package WasteSimulation;
class GridCell implements Comparable<GridCell> {
	int row, col;
	char type;

	public GridCell(int row, int col, char type) {
		this.row = row;
		this.col = col;
		this.type = type;
	}

	@Override
	public int compareTo(GridCell other) {
		if (this.row != other.row)
			return Integer.compare(this.row, other.row);
		if (this.col != other.col)
			return Integer.compare(this.col, other.col);
		return Character.compare(this.type, other.type);
	}

	@Override

	public boolean equals(Object obj) {
		if (!(obj instanceof GridCell))
			return false;
		GridCell other = (GridCell) obj;

		return this.row == other.row && this.col == other.col && this.type == other.type;
	}

	@Override
	public int hashCode() {
		return 31 * (31 * row + col) + type;
	}

	@Override
	public String toString() {
		return String.format("Cell[%d,%d,%c]", row, col, type);
	}
}