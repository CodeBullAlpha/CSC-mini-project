import java.io.File;

class Bin {
        int row, col;
        WasteType type;
        File imageFile;

        public Bin(int r, int c, WasteType t, File imageFile) {
            row = r;
            col = c;
            type = t;
            this.imageFile = imageFile;
        }
    }