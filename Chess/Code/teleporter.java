import java.util.*;
import java.awt.Color;

import javalib.worldimages.*;

abstract class Teleporter extends Piece {
	Set<Posn> deltas;
	
	Teleporter(Side color) {
		super(color);
	}
	
	public Set<Action> generateMoves(Board b, Posn pos) {
		if (!Board.inBounds(pos)) {
			throw new IllegalArgumentException("out of bounds");
		}

		int row = pos.y;
		int col = pos.x;

		Set<Action> ans = new HashSet<>();

		for (Posn p : deltas) {
			int r = p.y+row;
			int c = p.x+col;

			if (!Board.inBounds(r, c)) {
				continue;
			}

			if (!b.isPiece(new Posn(c, r))) {
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

		for (Posn p : deltas) {
			int r = p.y+row;
			int c = p.x+col;

			Posn q = new Posn(c, r);

			if (!Board.inBounds(q) || !b.isPiece(q) || b.get(q).get().isColor(color)) {
				continue;
			}

			ans.add(new Capture(pos, new Posn(c, r), this));
		}

		return ans;
	}
}


class Knight extends Teleporter {
	static final WorldImage WHITE = new FromFileImage("whiteKnight.png");
	static final WorldImage BLACK = new FromFileImage("blackKnight.png");
	
	Knight(Side color) {
		super(color);
		
		if (isColor(Side.WHITE)) {
			pieceImage = new ScaleImageXY(WHITE, (1.0*SIZE)/WHITE.getWidth(), (1.0*SIZE)/WHITE.getHeight());
		}
		else {
			pieceImage = new ScaleImageXY(BLACK, (1.0*SIZE)/BLACK.getWidth(), (1.0*SIZE)/BLACK.getHeight());
		}
		
		deltas = new HashSet<>(Arrays.asList(new Posn(2, 1), new Posn(-2, 1), new Posn(2, -1), new Posn(-2, -1),
				new Posn(1, 2), new Posn(1, -2), new Posn(-1, 2), new Posn(-1, -2)));
	}
	
	public boolean isKnight() {
		return true;
	}
	
	public Piece copy() {
		Knight ans = new Knight(color);
		ans.moved = moved;		
		return ans;
	}
	
	public String toString() {
		return "N";
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

class King extends Teleporter {
	static final WorldImage WHITE = new FromFileImage("whiteKing.png");
	static final WorldImage BLACK = new FromFileImage("blackKing.png");
	
	boolean check;
	WorldImage CHECK_BG = new CircleImage(SIZE/2, "solid", Color.RED);
	
	King(Side color) {
		super(color);

		if (isColor(Side.WHITE)) {
			pieceImage = new ScaleImageXY(WHITE, (1.0*SIZE)/WHITE.getWidth(), (1.0*SIZE)/WHITE.getHeight());
		}
		else {
			pieceImage = new ScaleImageXY(BLACK, (1.0*SIZE)/BLACK.getWidth(), (1.0*SIZE)/BLACK.getHeight());
		}
		
		deltas = new HashSet<>(Arrays.asList(new Posn(1,1), new Posn(-1,1), new Posn(1,-1), new Posn(-1,-1),
				new Posn(1,0), new Posn(0,-1), new Posn(-1,0), new Posn(0,1)));
	}

	public Set<Action> generateCastles(Board b, Posn pos) {
		Set<Action> ans = new HashSet<>();

		if (!moved && !b.inCheck(color)) {
			if (isColor(Side.WHITE)) {
				if (b.canCastleTo(new Posn(7, 7))
						&& b.canCastleThrough(new Posn(6, 7), color)
						&& b.canCastleThrough(new Posn(5, 7), color)) {
					ans.add(new Castle(pos, new Posn(6, 7), this));
				}

				if (b.canCastleTo(new Posn(0, 7))
						&& b.canCastleThrough(new Posn(3, 7), color)
						&& b.canCastleThrough(new Posn(2, 7), color)
						&& b.canCastleThrough(new Posn(1, 7), color)) {
					ans.add(new Castle(pos, new Posn(2, 7), this));
				}

			}
			else {
				if (b.canCastleTo(new Posn(7, 0))
						&& b.canCastleThrough(new Posn(6, 0), color)
						&& b.canCastleThrough(new Posn(5, 0), color)) {
					ans.add(new Castle(pos, new Posn(6, 0), this));
				}
				if (b.canCastleTo(new Posn(0, 0))
						&& b.canCastleThrough(new Posn(3, 0), color)
						&& b.canCastleThrough(new Posn(2, 0), color)
						&& b.canCastleThrough(new Posn(1, 0), color)) {
					ans.add(new Castle(pos, new Posn(2, 0), this));
				}
			}
		}

		return ans;
	}
	
	public boolean isKing() {
		return true;
	}

	public void putInCheck() {
		check = true;
	}
	
	public void unCheck() {
		check = false;
	}
	
	public Piece copy() {
		King ans = new King(color);
		ans.check = check;
		ans.moved = moved;
		return ans;
	}
	
	public String toString() {
		return "K";
	}

	public int numericValue() {
		if (color.equals(Side.WHITE)) {
			return 1000;
		}
		else {
			return -1000;
		}
	}
}