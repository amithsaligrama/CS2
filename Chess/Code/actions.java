import java.util.*;
import java.util.function.*;
import javalib.worldimages.*;

abstract class Action implements Function<Board, Board> {
	public static String stringify(Posn end) {
		if (!Board.inBounds(end)) {
			throw new IllegalArgumentException("posn out of bounds");
		}
		return (char)(end.x + 97) + Integer.toString(8-end.y);
	}
	
	Posn start;
	Posn end;
	Piece toMove;

	Action(Posn start, Posn end, Piece toMove) {
		if (Board.inBounds(start) && Board.inBounds(end)) {
			this.start = start;
			this.end = end;
		}
		else {
			throw new RuntimeException();
		}
		
		this.toMove = toMove;
	}

	public abstract Board apply(Board b);
	
	abstract String toString(Board b);
 
	public void print(Board b) {
		System.out.println(toString(b));
	}
}

class Move extends Action {
	Move(Posn start, Posn end, Piece p) {
		super(start, end, p);
	}
	
	public Board apply(Board b) {		
		Board next = new Board(b.pieces, b.fiftyMoveCounter, start, end);
		
		next.movePieceTo(toMove.copy(), start, end);
		//noinspection OptionalGetWithoutIsPresent
		next.get(end).get().setMoved();
		if (Math.abs(start.y - end.y) == 2 && toMove.isPawn()) {
			next.unpassant(end);
		}
		else {
			next.unpassant();
		}
		
		if (toMove.isPawn()) {
			next.fiftyMoveCounter = 0;
		}
		else {
			next.fiftyMoveCounter++;
		}
		
		return next;
	}
	
	public String toString(Board b) {
		String ans = toMove.toString() + stringify(end);
		if (b.checkmated(toMove.color.opponent())) {
			ans += "#";
		} else if (b.inCheck(toMove.color.opponent())) {
			ans += "+";
		}
		return ans;
	}
}

class Capture extends Action {
	Capture(Posn start, Posn end, Piece toMove) {
		super(start, end, toMove);
	}
	
	@SuppressWarnings("OptionalGetWithoutIsPresent")
	public Board apply(Board b) {
		Board next = new Board(b.pieces, 0, start, end);
		
		next.movePieceTo(toMove.copy(), start, end);	
		next.unpassant();
		//noinspection OptionalGetWithoutIsPresent
		next.get(end).get().setMoved();
		
		return next;
	}
	
	public String toString(Board b) {
		String ans = toMove.toString(start) + "x" + stringify(end);
		if (b.checkmated(toMove.color.opponent())) {
			ans += "#";
		}
		else if (b.inCheck(toMove.color.opponent())) {
			ans += "+";
		}
		return ans;
	}
}

class Enpassant extends Action {
	Enpassant(Posn start, Posn end, Pawn toMove) {
		super(start, end, toMove);
	}

	public Board apply(Board b) {
		Board next = new Board(b.pieces, 0, start, end);
		
		next.movePieceTo(toMove.copy(), start, end);
		//noinspection OptionalGetWithoutIsPresent
		next.get(end).get().setMoved();
		next.removePieceAt(new Posn(end.x, start.y));
		next.unpassant();
		
		return next;
	}
	
	public String toString(Board b) {
		String ans = toMove.toString(start) + "x" + stringify(end);
		if (b.checkmated(toMove.color.opponent())) {
			ans += "#";
		}
		else if (b.inCheck(toMove.color.opponent())) {
			ans += "+";
		}
		return ans;
	}

}

class Promotion extends Action {
	public static void add(Set<Action> ans, Posn st, Posn en, Pawn pawn) {
		ans.add(new Promotion(st, en, pawn, new Queen(pawn.color)));
		ans.add(new Promotion(st, en, pawn, new Knight(pawn.color)));
		ans.add(new Promotion(st, en, pawn, new Rook(pawn.color)));
		ans.add(new Promotion(st, en, pawn, new Bishop(pawn.color)));
	}
	
	Piece promoteTo;
	
	Promotion(Posn start, Posn end, Pawn toMove, Piece promoteTo) {
		super(start, end, toMove);
		
		if (promoteTo.isKnight() || promoteTo.isBishop() || promoteTo.isQueen() || promoteTo.isRook()) {
			this.promoteTo = promoteTo;
		}
		else {
			throw new IllegalArgumentException("can only promote to knight, bishop, queen, or rook");
		}
	}
	
	public Board apply(Board b) {
		Board next = new Board(b.pieces, 0, start, end);
		
		next.removePieceAt(start);
		next.placePieceAt(promoteTo, end);
		next.unpassant();
		return next;
	}
	
	public String toString(Board b) {
		String ans = toMove.toString() + stringify(end) + "=" + promoteTo.toString();

		if (b.checkmated(toMove.color.opponent())) {
			ans += "#";
		}
		else if (b.inCheck(toMove.color.opponent())) {
			ans += "+";
		}
		return ans;
	}

}

class Castle extends Action {
	Castle(Posn start, Posn end, King toMove) {
		super(start, end, toMove);
	}

	public Board apply(Board b) {
		Board next = new Board(b.pieces, b.fiftyMoveCounter + 1, start, end);
		next.movePieceTo(toMove.copy(), start, end);
		next.get(end).get().setMoved();

		if (isKingside()) {
			Posn p = new Posn(7, start.y);
			if (b.get(p).get().isRook()) {
				next.movePieceTo(b.get(p).get(), p, new Posn(5, start.y));
			}
		}
		else {
			Posn p = new Posn(0, start.y);
			if (b.get(p).get().isRook()) {
				next.movePieceTo(b.get(p).get(), p, new Posn(3, start.y));
			}
		}
		
		next.unpassant();
		
		return next;
	}
	
	public boolean isKingside() {
		return end.x > start.x;
	}
	
	public String toString(Board b) {
		String ans;
		if (isKingside()) {
			ans = "O-O";
		}
		else {
			ans = "O-O-O";
		}
		
		if (b.checkmated(toMove.color.opponent())) {
			ans += "#";
		}
		else if (b.inCheck(toMove.color.opponent())) {
			ans += "+";
		}
		
		return ans;
	}

}