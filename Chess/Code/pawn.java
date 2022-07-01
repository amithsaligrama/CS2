import java.util.*;
import javalib.worldimages.*;

class Pawn extends Piece {
	static final WorldImage WHITE = new FromFileImage("whitePawn.png");
	static final WorldImage BLACK = new FromFileImage("blackPawn.png");
	
	boolean enpassantable = true;
	
	Pawn(Side color) {
		super(color);

		if (isColor(Side.WHITE)) {
			pieceImage = new ScaleImageXY(WHITE, (1.0*SIZE)/WHITE.getWidth(), (1.0*SIZE)/WHITE.getHeight());
		}
		else {
			pieceImage = new ScaleImageXY(BLACK, (1.0*SIZE)/BLACK.getWidth(), (1.0*SIZE)/BLACK.getHeight());
		}
	}

	public Set<Action> generateMoves(Board b, Posn pos) {
		if (!Board.inBounds(pos)) {
			throw new IllegalArgumentException("out of bounds");
		}
		
		int row = pos.y;
		int col = pos.x;

		Set<Action> ans = new HashSet<>();
		
		if ((row == 6 && color.equals(Side.BLACK)) || row == 1 && color.equals(Side.WHITE)) {
			return ans;
		}
		
		if (isColor(Side.WHITE)) {
			if (row-1 >= 0 && !b.isPiece(new Posn(col, row-1))) {
				ans.add(new Move(pos, new Posn(col, row-1), this));

				if (!moved && row-2 >= 0 && !b.isPiece(new Posn(col, row-2))) {
					ans.add(new Move(pos, new Posn(col, row-2), this));
				}
			}
		}
		else {
			if (row+1 < 8 && !b.isPiece(new Posn(col, row+1))) {
				ans.add(new Move(pos, new Posn(col, row+1), this));

				if (!moved && row+2 < 8 && !b.isPiece(new Posn(col, row+2))) {
					ans.add(new Move(pos, new Posn(col, row+2), this));
				}
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
		
		if ((row == 6 && color.equals(Side.BLACK)) || row == 1 && color.equals(Side.WHITE)) {
			return ans;
		}
		
		if (isColor(Side.WHITE)) {
			//noinspection OptionalGetWithoutIsPresent
			if (Board.inBounds(row-1, col-1)
					&& b.isPiece(new Posn(col-1, row-1)) && !b.get(new Posn(col-1, row-1)).get().isKing()
					&& !b.get(new Posn(col-1, row-1)).get().colorSame(this)) {
				ans.add(new Capture(pos, new Posn(col-1, row-1), this));
			}
			//noinspection OptionalGetWithoutIsPresent
			if (Board.inBounds(row-1, col+1)
					&& b.isPiece(new Posn(col+1, row-1)) && !b.get(new Posn(col+1, row-1)).get().isKing()
					&& !b.get(new Posn(col+1, row-1)).get().colorSame(this)) {
				ans.add(new Capture(pos, new Posn(col+1, row-1), this));
			}
		}
		else {
			//noinspection OptionalGetWithoutIsPresent
			if (Board.inBounds(row+1, col-1)
					&& b.isPiece(new Posn(col-1, row+1)) && !b.get(new Posn(col-1, row+1)).get().isKing()
					&& !b.get(new Posn(col-1, row+1)).get().colorSame(this)) {
				ans.add(new Move(pos, new Posn(col-1, row+1), this));
			}
			//noinspection OptionalGetWithoutIsPresent
			if (Board.inBounds(row+1, col+1)
					&& b.isPiece(new Posn(col+1, row+1)) && !b.get(new Posn(col+1, row+1)).get().isKing()
					&& !b.get(new Posn(col+1, row+1)).get().colorSame(this)) {
				ans.add(new Move(pos, new Posn(col+1, row+1), this));
			}
		}
		
		return ans;
	}
	public Set<Action> generateEnpassants(Board b, Posn pos) {
		if (!Board.inBounds(pos)) {
			throw new RuntimeException("out of bounds bozo");
		}
		
		int row = pos.y;
		int col = pos.x;

		Set<Action> ans = new HashSet<>();
		
		if (row > 4 || row < 3) {
			return ans;
		}

		//noinspection OptionalGetWithoutIsPresent
		if (col-1 >= 0
				&& b.isPiece(new Posn(col-1, row))
				&& b.get(new Posn(col-1, row)).get().isEnpassantable()) {
			
			if (isColor(Side.WHITE) && row == 3) {
				ans.add(new Enpassant(pos, new Posn(col-1, row-1), this));
			}
			else if (isColor(Side.BLACK) && row == 4) {
				ans.add(new Enpassant(pos, new Posn(col-1, row+1), this));
			}
		}
		//noinspection OptionalGetWithoutIsPresent
		if (col+1 < 0
				&& b.isPiece(new Posn(col+1, row))
				&& b.get(new Posn(col+1, row)).get().isEnpassantable()) {
			
			if (isColor(Side.WHITE) && row == 3) {
				ans.add(new Enpassant(pos, new Posn(col+1, row-1), this));
			}
			else if (isColor(Side.BLACK) && row == 4) {
				ans.add(new Enpassant(pos, new Posn(col+1, row+1), this));
			}
		}
		
		return ans;
	}
	
	public Set<Action> generatePromotions(Board b, Posn pos) {
		if (!Board.inBounds(pos)) {
			throw new RuntimeException("out of bounds bozo");
		}
		
		int row = pos.y;
		int col = pos.x;

		Set<Action> ans = new HashSet<>();
		
		if (row == 6 && isColor(Side.BLACK)) {
			//noinspection OptionalGetWithoutIsPresent
			if (col-1 >= 0 && b.isPiece(new Posn(col-1, 7)) && !b.get(new Posn(col-1, 7)).get().colorSame(this)) {
				Promotion.add(ans, pos, new Posn(col-1, 7), this);
			}
			//noinspection OptionalGetWithoutIsPresent
			if (col+1 < 8 && b.isPiece(new Posn(col+1, 7)) && !b.get(new Posn(col+1, 7)).get().colorSame(this)) {
				Promotion.add(ans, pos, new Posn(col+1, 7), this);
			}
			
			if (!b.isPiece(new Posn(col, 7))) {
				Promotion.add(ans, pos, new Posn(col, 7), this);
			}
		}
		if (row == 1 && isColor(Side.WHITE)) {
			//noinspection OptionalGetWithoutIsPresent
			if (col-1 >= 0 && b.isPiece(new Posn(col-1, 0)) && !b.get(new Posn(col-1, 0)).get().colorSame(this)) {
				Promotion.add(ans, pos, new Posn(col-1, 0), this);
			}
			//noinspection OptionalGetWithoutIsPresent
			if (col+1 < 8 && b.isPiece(new Posn(col+1, 0)) && !b.get(new Posn(col+1, 0)).get().colorSame(this)) {
				Promotion.add(ans, pos, new Posn(col+1, 0), this);
			}
			
			if (!b.isPiece(new Posn(col, 0))) {
				Promotion.add(ans, pos, new Posn(col, 0), this);
			}
		}	
		
		return ans;
	}
	
	public boolean attacks(Board b, Posn target, Posn start) {
		return (Math.abs(target.x - start.x) == 1) &&
				(color.equals(Side.WHITE) && start.y - target.y == 1) ||
				(color.equals(Side.BLACK) && start.y - target.y == -1);
	}
	
	public boolean isEnpassantable() {
		return enpassantable;
	}
	
	public boolean isPawn() {
		return true;
	}
	
	public Piece copy() {
		Pawn ans = new Pawn(color);
		
		ans.moved = moved;
		ans.enpassantable = enpassantable;
		
		return ans;
	}
	
	public void unpassant() {
		enpassantable = false;
	}
	
	public String toString() {
		return "";
	}
	
	public String toString(Posn p) {
		return Action.stringify(p).substring(0, 1);
	}

	public int numericValue() {
		if (color.equals(Side.WHITE)) {
			return 1;
		}
		else {
			return -1;
		}
	}
}