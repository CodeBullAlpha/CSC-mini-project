import java.io.File;

class Waste {
        int row, col;
        WasteType type;
        boolean identified = false;
        File imageFile;

        public Waste(int row, int col, WasteType type, File imageFile) {
            this.row = row;
            this.col = col;
            this.type = type;
            this.imageFile = imageFile;
        }
    }