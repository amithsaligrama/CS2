import java.util.*;
import java.util.stream.Collectors;
import javalib.worldimages.*;

enum Side {
	BLACK, WHITE;
	
	public void print() {
		System.out.println(this);
	}

	public String toString() {
		if (equals(WHITE)) {
			return "White";
		}
		else {
			return "Black";
		}
	}
	
	public Side opponent() {
		if (equals(WHITE)) {
			return BLACK;
		}
		else {
			return WHITE;
		}
	}
}

abstract class Piece {
	static final int SIZE = 70;
	
	final Side color;
	boolean moved;
	WorldImage pieceImage;
	
	Piece(Side color) {
		this.color = color;
	}

	public boolean colorSame(Piece p) {
		return isColor(p.color);
	}

	public boolean isColor(Side s) {
		return s.equals(color);
	}
	
	public Set<Action> generateActions(Board b, Posn pos) {
		Set<Action> ans = generateEnpassants(b, pos);
		ans.addAll(generateCastles(b, pos));
		ans.addAll(generatePromotions(b, pos));
		ans.addAll(generateCaptures(b, pos));
		ans.addAll(generateMoves(b, pos));
		
		return ans.stream()
				.filter(a -> !a.apply(b).inCheck(color))
				.collect(Collectors.toSet());
		
	}
	
	public boolean attacks(Board b, Posn target, Posn start) { // override for pawn
		Set<Action> acts = generateMoves(b, start);
		acts.addAll(generateCaptures(b, start));
		
		return acts.stream().anyMatch(a -> a.end.equals(target));
	}
	
	abstract Set<Action> generateMoves(Board b, Posn pos);
	abstract Set<Action> generateCaptures(Board b, Posn pos);
	
	public Set<Action> generateEnpassants(Board b, Posn pos) { return new HashSet<>(); }
	
	public Set<Action> generatePromotions(Board b, Posn pos) {
		return new HashSet<>();
	}
	
	public Set<Action> generateCastles(Board b, Posn pos) {
		return new HashSet<>();
	}
	
	public Board makeMove(Board b, Posn start, Posn end) {
		Set<Action> acts = generateActions(b, start);

		Action act = acts.stream().filter(a -> end.equals(a.end)).findFirst().get();
		act.print(b);

		return act.apply(b);
	}
	
	public abstract String toString();
	
	public String toString(Posn p) {
		return toString();
	}

	public void setMoved() {
		moved = true;
	}

	public void putInCheck() {}

	public void unCheck() {}

	public boolean isKnight() {
		return false;
	}

	public boolean isRook() {
		return false;
	}

	public boolean isPawn() {
		return false;
	}

	public boolean isQueen() {
		return false;
	}

	public boolean isBishop() {
		return false;
	}

	abstract Piece copy();

	public boolean isKing() {
		return false;
	}

	public void unpassant() {}

	abstract int numericValue();

	public boolean isEnpassantable() {
		return false;
	}
}