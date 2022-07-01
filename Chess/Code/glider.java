import java.util.*;
import javalib.worldimages.*;

abstract class Glider extends Piece {
	abstract Posn[][] initialize();
	
	Posn[][] deltas;
	
	Glider(Side color) {
		super(color);
		deltas = initialize();
	}
	
	public Set<Action> generateMoves(Board b, Posn pos) {
		if (!Board.inBounds(pos)) {
			throw new IllegalArgumentException("out of bounds");
		}
		
		int row = pos.y;
		int col = pos.x;

		Set<Action> ans = new HashSet<>();
		
		for (Posn[] q : deltas) {
			for (Posn p : q) {
				int r = p.y+row;
				int c = p.x+col;
				
				if (!Board.inBounds(r, c) || b.isPiece(new Posn(c, r))) {
					break;
				}
				
				ans.add(new Move(pos, new Posn(c, r), this));
			}
		}
		
		return ans;
	}
	
	public Set<Action> generateCaptures(Board b, Posn pos) {
		if (!Board.inBounds(pos)) {
			throw new IllegalArgumentException("out of bounds");
		}
		
		int row = pos.y;
		int col = pos.x;

		Set<Action> ans = new HashSet<>();
		
		for (Posn[] q : deltas) {
			for (Posn p : q) {
				int r = p.y+row;
				int c = p.x+col;

				//noinspection OptionalGetWithoutIsPresent
				if (!Board.inBounds(r, c) ||
						(b.isPiece(new Posn(c, r)) && b.get(new Posn(c, r)).get().colorSame(this))) {
					break;
				}
				
				if (b.isPiece(new Posn(c, r))) {
					ans.add(new Capture(pos, new Posn(c, r), this));
					break;
				}
			}
		}
		
		return ans;
	}
}


class Rook extends Glider {
	static final WorldImage WHITE = new FromFileImage("whiteRook.png");
	static final WorldImage BLACK = new FromFileImage("blackRook.png");
	
	public Posn[][] initialize() {
		Posn[][] ans = new Posn[4][7];
		
		for (int i = 0; i < 7; i++) {
			ans[0][i] = new Posn(i+1, 0);
			ans[1][i] = new Posn(0, i+1);
			ans[2][i] = new Posn(-i-1, 0);
			ans[3][i] = new Posn(0, -i-1);
		}
		
		return ans;
	}
	
	boolean moved;

	Rook(Side color) {
		super(color);

		if (isColor(Side.WHITE)) {
			pieceImage = new ScaleImageXY(WHITE, (1.0*SIZE)/WHITE.getWidth(), (1.0*SIZE)/WHITE.getHeight());
		}
		else {
			pieceImage = new ScaleImageXY(BLACK, (1.0*SIZE)/BLACK.getWidth(), (1.0*SIZE)/BLACK.getHeight());
		}
	}
	
	public boolean canCastleTo() {
		return !moved;
	}
	
	public boolean isRook() {
		return true;
	}
	
	public void setMoved() {
		moved = true;
	}
	
	public Rook copy() {
		Rook ans = new Rook(color);
		ans.moved = moved;
		return ans;
	}
	
	public String toString() {
		return "R";
	}

	public int numericValue() {
		if (color.equals(Side.WHITE)) {
			return 5;
		}
		else {
			return -5;
		}
	}
}

class Bishop extends Glider {
	static WorldImage WHITE = new FromFileImage("whiteBishop.png");
	static WorldImage BLACK = new FromFileImage("blackBishop.png");
	
	public Posn[][] initialize() {
		Posn[][] ans = new Posn[4][7];
		
		for (int i = 0; i < 7; i++) {
			ans[0][i] = new Posn(i+1, i+1);
			ans[1][i] = new Posn(i+1, -i-1);
			ans[2][i] = new Posn(-i-1, -i-1);
			ans[3][i] = new Posn(-i-1, i+1);
		}
		
		return ans;
	}
	
	Bishop(Side color) {
		super(color);

		if (isColor(Side.WHITE)) {
			WorldImage wi = new FromFileImage("whiteBishop.png");
			pieceImage = new ScaleImageXY(wi, (1.0*SIZE)/wi.getWidth(), (1.0*SIZE)/wi.getHeight());
		}
		else {
			WorldImage wi = new FromFileImage("blackBishop.png");
			pieceImage = new ScaleImageXY(wi, (1.0*SIZE)/wi.getWidth(), (1.0*SIZE)/wi.getHeight());
		}
	}
	
	public boolean isBishop() {
		return true;
	}
	
	public Piece copy() {
		Bishop ans = new Bishop(color);
		ans.moved = moved;
		return ans;
	}
	
	public String toString() {
		return "B";
	}
	
	public int numericValue() {
		if (color.equals(Side.WHITE)) {
			return 3;
		}
		else {
			return -3;
		}
	}
}

class Queen extends Glider {
	static final WorldImage WHITE = new FromFileImage("whiteQueen.png");
	static final WorldImage BLACK = new FromFileImage("blackQueen.png");
	
	public Posn[][] initialize() {
		Posn[][] ans = new Posn[8][7];
		
		for (int i = 0; i < 7; i++) {
			ans[0][i] = new Posn(i+1, 0);
			ans[1][i] = new Posn(0, i+1);
			ans[2][i] = new Posn(-i-1, 0);
			ans[3][i] = new Posn(0, -i-1);
			
			ans[4][i] = new Posn(i+1, i+1);
			ans[5][i] = new Posn(i+1, -i-1);
			ans[6][i] = new Posn(-i-1, -i-1);
			ans[7][i] = new Posn(-i-1, i+1);
		}
		
		return ans;
	}
	
	Queen(Side color) {
		super(color);

		if (isColor(Side.WHITE)) {
			pieceImage = new ScaleImageXY(WHITE, (1.0*SIZE)/WHITE.getWidth(), (1.0*SIZE)/WHITE.getHeight());
		}
		else {
			pieceImage = new ScaleImageXY(BLACK, (1.0*SIZE)/BLACK.getWidth(), (1.0*SIZE)/BLACK.getHeight());
		}
	}
	
	public boolean isQueen() {
		return true;
	}
	
	public Piece copy() {
		Queen ans = new Queen(color);
		ans.moved = moved;
		return ans;
	}
	
	public String toString() {
		return "Q";
	}
	
	public int numericValue() {
		if (color.equals(Side.WHITE)) {
			return 9;
		}
		else {
			return -9;
		}
	}
}


