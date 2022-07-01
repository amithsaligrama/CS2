import java.util.*;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

class Minesweeper extends World {
	static double TICK_SPEED = 0.01;
	 
	int rows;
	int cols;	
	int mines;
	Tile[][] board;
	
	boolean lose;
	boolean playing;
	boolean firstClick;
	
	int flags;
	double time;
		
	Minesweeper(int rows, int cols, int mines) {
		this.rows = Utils.enforcePositive(rows);
		this.cols = Utils.enforcePositive(cols);
		this.mines = Utils.enforcePositive(mines);

		this.flags = this.mines;
		
	}
	

	public void initializeBoard() {
		board = new Tile[rows][cols];
		Random rand = new Random();

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				board[r][c] = new Tile(Utils.mineColor(rand.nextInt(9)));
			}
		}
	}
	
	public void minesAdj() {
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				Utils.minesAdj(board, r, c);
			}
		}
	}
	
	public int broken() {
		int ans = 0;
		for (Tile[] row : board) {
			for (Tile c : row) {
				if (c.shown) {
					ans++;
				}
			}
		}
		return ans;
	}

	// r,c is first click
	public void placeMines(int r, int c) {
		List<Posn> mineCoords = new ArrayList<Posn>();
		
		Random row = new Random();
		Random col = new Random();
		
		while (mineCoords.size() < mines) {
			Posn p = new Posn(col.nextInt(cols), row.nextInt(rows));
			if (p.y == r && p.x == c) continue;
			if (!mineCoords.contains(p))  {
				mineCoords.add(p);
				board[p.y][p.x].placeMine();
			}
		}
	}
	
	void launchGame() {
		initializeBoard();
		lose = false;
		playing = true;
		time = 0;
		firstClick = true;
		flags = mines;
		this.bigBang(this.width(), this.height(), TICK_SPEED);
	}
	
	public void onKeyEvent(String key) {
		if (key.equals("r") || key.equals("R")) {
			this.endOfWorld("reset");
			launchGame();
		}
	}
	
	public void showAllMines() {
		for (Tile[] row : board) {
			for (Tile c : row) {
				if (c.mine) {
					c.setFlag(false);
					c.setShown(true);
				}
			}
		}
	}

	public WorldScene makeScene() {
		WorldScene ans = this.getEmptyScene();
		
		ans.placeImageXY(new TextImage("Time: " + (Math.floor(time * 100.0) / 100.0 + 0.00), Tile.TEXT_SIZE, Color.magenta), width()/3, Tile.SIZE/2);
		
		ans.placeImageXY(new TextImage("Flags: " + flags, Tile.TEXT_SIZE, Color.red), 2*width()/3, Tile.SIZE/2);
		
		if (win()) {
			ans.placeImageXY(new TextImage("WIN!!", Tile.TEXT_SIZE, Color.green), width()/2, (int) ((this.rows + 1.5) * Tile.SIZE));
			playing = false;
		}
		else if (lose) {
			ans.placeImageXY(new TextImage("LOSE!!", Tile.TEXT_SIZE, Color.red), width()/2, (int) ((this.rows + 1.5) * Tile.SIZE));
			showAllMines();
			playing = false;
		}
		else {
			ans.placeImageXY(new TextImage("Broken: " + broken(), Tile.TEXT_SIZE, Tile.UNSHOWN_COLOR1), width()/2, (int) ((this.rows + 1.5) * Tile.SIZE));
		}
		
		boolean startColor = true;
		for (int r = 0; r < rows; r++) {
			boolean color = startColor;
			for (int c = 0; c < cols; c++) {
				if (board[r][c].mine && board[r][c].shown) {
					lose = true;
				}
				
				ans.placeImageXY(board[r][c].display(board, r, c, color),
						(int) ((c + 0.5) * (Tile.SIZE)),
						(int) ((r + 1.5) * (Tile.SIZE)));
				color = !color;
			}
			startColor = !startColor;
		}
		return ans;
	}

	public boolean win() {		
		for (Tile[] row : board) {
			for (Tile c : row) {
				if (!c.shown && !c.mine) {
					return false;
				}
			}
		}
		return true;
	}
	
	public void onTick() {
		if (playing) time += 3.5 * TICK_SPEED;
	}

	public void onMouseClicked(Posn pos, String buttonName) {
		int r = pos.y / Tile.SIZE - 1;
		int c = pos.x / Tile.SIZE;
		
		if (!Utils.inBounds(r, c, rows, cols)) return;
		
		if (playing && buttonName.equals("RightButton")) {
			int flagDelta = board[r][c].changeFlag();
			flags += flagDelta;
		}
		
		if (playing && buttonName.equals("LeftButton")) {
			if (firstClick) {
				placeMines(r, c);
				minesAdj();
				firstClick = false;
			}
			board[r][c].breakCell(board, r, c);
			if (board[r][c].mine) { 
				lose = true; 
			}
		}
		if (playing && buttonName.equals("MiddleButton")) {
			board[r][c].breakNearbyCells(board, r, c);
		}
	}
	
	public void onMousePressed(Posn pos, String buttonName) {
		int r = pos.y / Tile.SIZE - 1;
		int c = pos.x / Tile.SIZE;
		
		if (!Utils.inBounds(r, c, rows, cols)) return;
		
		if (playing && buttonName.equals("MiddleButton") && board[r][c].shown && board[r][c].adj > 0) {
			Utils.selectNeighbors(board, r, c);
		}
	}
	
	public void onMouseReleased(Posn pos, String buttonName) {
		int r = pos.y / Tile.SIZE - 1;
		int c = pos.x / Tile.SIZE;
		
		if (!Utils.inBounds(r, c, rows, cols)) return;
		
		if (playing && buttonName.equals("MiddleButton")) {
			Utils.deselectAll(board, r, c);
		}
	}
	
	public void onMouseMoved(Posn pos) {
		int r = pos.y / Tile.SIZE - 1;
		int c = pos.x / Tile.SIZE;
		
		if (!Utils.inBounds(r, c, rows, cols)) return;
		
		board[r][c].select();
		Utils.deselectAll(board, r, c);
	}
	
	public void onMouseExited(Posn pos) {
		playing = false;
	}
	
	public void onMouseEntered(Posn pos) {
		playing = true;
	}
	
	public int width() {
		return this.cols * Tile.SIZE;
	}
	
	public int height() {
		return (this.rows + 2) * Tile.SIZE;
	}
}

class Tile {	
	static int SIZE = 50;
	static double TEXT_SIZE = (1.0 * SIZE)/(2.0);
	
	static Color UNSHOWN_COLOR1 = new Color(176, 212, 84);
	static Color UNSHOWN_COLOR2 = new Color(162, 209, 73);
	
	static Color SHOWN_COLOR1 = new Color(215, 184, 153);
	static Color SHOWN_COLOR2 = new Color(229, 194, 159);
	
	static WorldImage UNSHOWN_TILE1 = new RectangleImage(SIZE, SIZE, "solid", UNSHOWN_COLOR1);
	static WorldImage SELECTED_UNSHOWN_TILE1 = new RectangleImage(SIZE, SIZE, "solid", UNSHOWN_COLOR1.brighter());
	static WorldImage UNSHOWN_TILE2 = new RectangleImage(SIZE, SIZE, "solid", UNSHOWN_COLOR2);
	static WorldImage SELECTED_UNSHOWN_TILE2 = new RectangleImage(SIZE, SIZE, "solid", UNSHOWN_COLOR2.brighter());
	
	static WorldImage SHOWN_TILE1 = new RectangleImage(SIZE, SIZE, "solid", SHOWN_COLOR1);
	static WorldImage SELECTED_SHOWN_TILE1 = new RectangleImage(SIZE, SIZE, "solid", SHOWN_COLOR1.brighter());
	static WorldImage SHOWN_TILE2 = new RectangleImage(SIZE, SIZE, "solid", SHOWN_COLOR2);
	static WorldImage SELECTED_SHOWN_TILE2 = new RectangleImage(SIZE, SIZE, "solid", SHOWN_COLOR1.brighter());
	
	public WorldImage MINE() {
		return new CircleImage(SIZE/4, "solid", c.darker())
				.overlayImages(new RectangleImage(SIZE, SIZE, "solid", c));
	}

	static WorldImage google_flag = new FromFileImage("flag_icon.png");
	static WorldImage FLAG = new ScaleImageXY(google_flag, (1.0*SIZE)/google_flag.getWidth(), (1.0*SIZE)/google_flag.getHeight());
	
	boolean shown;
	boolean mine;
	boolean flag;
	boolean selected;
	int adj;
	Color c;
	
	Tile(Color c) {
		shown = false;
		mine = false;
		flag = false;
		selected = false;
		adj = -1;
		
		this.c = c;
	}

	public void placeMine() {
		mine = true;
	}
	
	public void select() {
		selected = true;
	}
	
	public void deselect() {
		selected = false;
	}
	
	public int changeFlag() {
		if (!shown) {
			flag = !flag;
			return flag ? -1 : 1;
		}
		return 0;
	}
	
	public void breakCell(Tile[][] board, int row, int col) {
		if (!flag) {
			shown = true;
			breakNearbyCells(board, row, col);
		}
	}
	
	public void breakNearbyCells(Tile[][] board, int row, int col) {
		Utils.displayIsolatedNeighbors(board, row, col);
		if (shown && adj > 0 && Utils.flaggedNeighbors(board, row, col, adj)) {
			Utils.showAllNeighbors(board, row, col);
		}
	}
	
	public WorldImage display(Tile[][] board, int row, int col, boolean tile1) {
		if (selected) {
			if (flag && tile1) return FLAG.overlayImages(SELECTED_UNSHOWN_TILE1);
			if (flag && !tile1) return FLAG.overlayImages(SELECTED_UNSHOWN_TILE2);
			
			// unflagged after this
			
			if (!shown && tile1) return SELECTED_UNSHOWN_TILE1;
			if (!shown && !tile1) return SELECTED_UNSHOWN_TILE2;
			
			// shown after this
			
			if (mine) return MINE();
			
			// not mines after this
			
			if (adj == 0) {
				Utils.showNeighbors(board, row, col);
				if (tile1) return SELECTED_SHOWN_TILE1;
				if (!tile1) return SELECTED_SHOWN_TILE2;
			}
			
			// have adjacent mines after this
			
			if (tile1) {
				return new TextImage("" + adj, TEXT_SIZE, FontStyle.BOLD, Utils.adjColor(adj))
						.overlayImages(SELECTED_SHOWN_TILE1);
			}
			
			// !color after this
			
			return new TextImage("" + adj, TEXT_SIZE, FontStyle.BOLD, Utils.adjColor(adj))
						.overlayImages(SELECTED_SHOWN_TILE2);
		}
		
		// not highlighted after this
		
		if (flag && tile1) return FLAG.overlayImages(UNSHOWN_TILE1);
		if (flag && !tile1) return FLAG.overlayImages(UNSHOWN_TILE2);
		
		// unflagged after this
		
		if (!shown && tile1) return UNSHOWN_TILE1;
		if (!shown && !tile1) return UNSHOWN_TILE2;
		
		// shown after this
		
		if (mine) return MINE();
		
		// not mines after this
		
		if (adj == 0) {
			Utils.showNeighbors(board, row, col);
			if (tile1) return SHOWN_TILE1;
			if (!tile1) return SHOWN_TILE2;
		}
		
		// have adjacent mines after this
		
		if (tile1) {
			return new TextImage("" + adj, TEXT_SIZE, FontStyle.BOLD, Utils.adjColor(adj))
					.overlayImages(SHOWN_TILE1);
		}
		
		// !color after this
		
		return new TextImage("" + adj, TEXT_SIZE, FontStyle.BOLD, Utils.adjColor(adj))
					.overlayImages(SHOWN_TILE2);
		
	}
	
	public void setFlag(boolean b) {
		flag = b;
	}


	public void setShown(boolean b) {
		shown = b;
	}

	public void incrementAdj() {
		adj++;
	}
}

class Utils {
	static int[] dr = new int[] {0, 1, -1, 0, 1, -1, 1, -1};
	static int[] dc = new int[] {1, 0, 0, -1, 1, -1, -1, 1};
	
	public static int enforcePositive(int n) {
		if (n > 0) return n;
		
		throw new IllegalArgumentException("is non-positive");
	}

	public static void displayIsolatedNeighbors(Tile[][] board, int row, int col) {
		for (int i = 0; i < 8; i++) {
			int r = dr[i] + row;
			int c = dc[i] + col;
			
			if (inBounds(r, c, board.length, board[row].length) && board[r][c].adj == 0 && !board[r][c].mine) {
				board[r][c].setShown(true);
			}
		}
	}

	public static Color mineColor(int n) {		
		if (n == 0) return Color.orange;
		if (n == 1) return Color.red;
		if (n == 2) return Color.yellow;
		if (n == 3) return Color.blue;
		if (n == 4) return Color.green;
		if (n == 5) return Color.magenta;
		if (n == 6) return Color.cyan;
		if (n == 7) return new Color(255, 105, 180);
		if (n == 8) return Color.yellow;
		
		throw new IllegalArgumentException("random is from [0,8]");
	}

	public static void deselectAll(Tile[][] board, int row, int col) {
		for (Tile[] r : board) {
			for (Tile c : r) {
				c.deselect();
			}
		}
		board[row][col].select();
	}

	public static void selectNeighbors(Tile[][] board, int row, int col) {
		for (int i = 0; i < 8; i++) {
			int r = dr[i] + row;
			int c = dc[i] + col;
			
			if (inBounds(r, c, board.length, board[row].length) && !board[r][c].shown && !board[r][c].flag) {
				board[r][c].select();
			}
		}
	}
	
	public static void deselectNeighbors(Tile[][] board, int row, int col) {
		for (int i = 0; i < 8; i++) {
			int r = dr[i] + row;
			int c = dc[i] + col;
			
			if (inBounds(r, c, board.length, board[row].length) && !board[r][c].shown) {
				board[r][c].deselect();
			}
		}
	}

	public static boolean flaggedNeighbors(Tile[][] board, int row, int col, int adj) {
		int flags = 0;
		for (int i = 0; i < 8; i++) {
			int r = dr[i] + row;
			int c = dc[i] + col;
			
			if (inBounds(r, c, board.length, board[row].length) && board[r][c].flag) {
				flags++;
			}
		}
		return flags == adj;
	}

	public static boolean inBounds(int x, int y, int n, int m) {
		return x >= 0 && x < n && y >= 0 && y < m;
	}

	public static void showNeighbors(Tile[][] board, int row, int col) {
		for (int i = 0; i < 8; i++) {
			int r = dr[i] + row;
			int c = dc[i] + col;

			if (inBounds(r, c, board.length, board[row].length) && !board[r][c].mine && !board[r][c].flag) {
				board[r][c].setShown(true);
			}
		}
	}
	
	public static void showAllNeighbors(Tile[][] board, int row, int col) {
		for (int i = 0; i < 8; i++) {
			int r = dr[i] + row;
			int c = dc[i] + col;
			
			if (inBounds(r, c, board.length, board[row].length) && !board[r][c].flag) {
				board[r][c].setShown(true);
			}
		}
	}

	public static void minesAdj(Tile[][] board, int row, int col) {
		board[row][col].adj = 0;
		for (int i = 0; i < 8; i++) {
			int r = dr[i] + row;
			int c = dc[i] + col;
			
			if (inBounds(r, c, board.length, board[row].length) && board[r][c].mine) {
				board[row][col].incrementAdj();
			}
		}
	}
	
	public static Color adjColor(int adj) {
		if (adj == 1) return new Color(64, 132, 199);
		if (adj == 2) return new Color(100, 149, 77);
		if (adj == 3) return new Color(213, 69, 37);
		if (adj == 4) return new Color(89, 41, 113);
		if (adj == 5) return new Color(250, 146, 17);
		if (adj == 6) return new Color(0, 128, 128);
		if (adj == 7) return new Color(81, 78, 74);
		if (adj == 8) return Color.lightGray;
		
		throw new IllegalArgumentException("adjacent must be in range [1,8], but found " + adj + ".");
	}
}

class ExamplesMinesweeper {
	Minesweeper easiest = new Minesweeper(10, 10, 1);
	Minesweeper ez = new Minesweeper(8, 10, 10);
	Minesweeper med = new Minesweeper(14, 18, 40);
	Minesweeper hard = new Minesweeper(20, 24, 100);
	
	void testBigBang(Tester t) {
		ez.launchGame();
	}
}