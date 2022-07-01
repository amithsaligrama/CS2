import java.util.*;
import java.util.List;
import tester.*;
import javalib.impworld.*;
import java.awt.*;
import javalib.worldimages.*;

class ChessWorld extends World {
	static final int WIDTH = Board.FULL_SIZE;
	static final int HEIGHT = Board.FULL_SIZE+Piece.SIZE;
	static final double TICK_SPEED = 1/Double.MAX_VALUE;

	public static double enforcePositive(double n) {
		if (n > 0) return n;

		throw new IllegalArgumentException("is non-positive");
	}

	public static double enforceNonnegative(double n) {
		if (n >= 0) return n;

		throw new IllegalArgumentException("is negative");
	}
	
	Player white;
	Player black;
	
	Board board;
	
	double timeWhite;
	double timeBlack;
	double increment;
	
	double initTime;
	
	boolean playing;
	
	boolean flipped;
	
	List<State> states = new ArrayList<>();

	ChessWorld(double initTime, double increment, Player white, Player black, Variant v) {
		if (white.color.equals(Side.BLACK) || black.color.equals(Side.WHITE)) {
			throw new IllegalArgumentException("the first player must be white, the second must be black");
		}
		
		white.activePlayer = true;
		black.activePlayer = false;
		
		this.white = white;
		this.black = black;
		
		this.initTime = enforcePositive(initTime);
		
		this.timeWhite = initTime; // already enforced positive
		this.timeBlack = initTime; // already enforced positive
		
		this.increment = enforceNonnegative(increment);
		
		this.board = new Board(v);
	}

	ChessWorld(double initTime, double increment, Player white, Player black) {
		this(initTime, increment, white, black, Variant.STANDARD);
	}
	
	ChessWorld(double initTime, double increment, Player white, Player black, String fen) {
		this(initTime, increment, white, black);
		
		String[] utils = fen.split(" ");
		String[] ranks = utils[0].split("/");
		
		this.board = new Board(utils, ranks);
		
		if (utils[1].equals("w")) {
			white.activePlayer = true;
			black.activePlayer = false;
		}
		else {
			white.activePlayer = false;
			black.activePlayer = true;
		}
		
	}
	
	public String fen() {
		String ans = board.fen() + " ";

		ans += turn().toString().substring(0, 1).toLowerCase() + " ";
		ans += board.addCastlingFen() + " ";
		ans += "- ";
		//board.getEnpassantable().ifPresentOrElse(p -> ans += Action.stringify(p) + " ", () -> ans.concat("- "));
		ans += repetitionCount() + " ";
		ans += states.size();

		return ans;
	}

	public void launchGame() {
		playing = true;
		
		timeWhite = initTime;
		timeBlack = initTime;
		
		states = new ArrayList<>();
		states.add(new State(board, initTime, initTime));
		
		flipped = !(!white.isAI || black.isAI);
		
		bigBang(WIDTH, HEIGHT, TICK_SPEED);
	}
	
	
	public WorldScene makeScene() {		
		WorldScene ans = getEmptyScene();
		
		addTime(ans);
		
		board.display(ans, flipped);
		
		addEndOfGameMsgs(ans);
		
		return ans;
	}
	
	public void addTime(WorldScene ans) {
		if (flipped) {
			ans.placeImageXY(timeDisplay(timeWhite), WIDTH/2, Piece.SIZE/4);
			ans.placeImageXY(timeDisplay(timeBlack), WIDTH/2, HEIGHT-Piece.SIZE/4);
		}
		else {
			ans.placeImageXY(timeDisplay(timeWhite), WIDTH/2, HEIGHT-Piece.SIZE/4);
			ans.placeImageXY(timeDisplay(timeBlack), WIDTH/2, Piece.SIZE/4);
		}
	}

	public void addEndOfGameMsgs(WorldScene ans) {
		if (board.checkmated(turn())) {
			ans.placeImageXY(new TextImage(turn().toString() + " is Checkmated!", Piece.SIZE/2, Color.GREEN), WIDTH/2, HEIGHT/2 + Piece.SIZE/2);
			if (playing) {
				if (turn().equals(Side.WHITE)) {
					System.out.println(white.sessionScore + " - " + ++black.sessionScore);
				}
				else {
					System.out.println(++white.sessionScore + " - " + black.sessionScore);
				}
			}
			playing = false;
		}
		
		if (board.stalemated(turn())) {
			ans.placeImageXY(new TextImage("Stalemate!", Piece.SIZE/2, Color.GREEN), WIDTH/2, HEIGHT/2 + Piece.SIZE/2);
			if (playing) {
				white.sessionScore += 0.5;
				black.sessionScore += 0.5;
				
				System.out.println(white.sessionScore + " - " + black.sessionScore);
			}
			playing = false;
		}
		
		if (timeWhite <= 0) {
			timeWhite = 0;
			ans.placeImageXY(new TextImage("White ran out of time", Piece.SIZE/2, Color.GREEN), WIDTH/2, HEIGHT/2 + Piece.SIZE/2);
			if (playing) {
				System.out.println(white.sessionScore + " - " + ++black.sessionScore);
			}
			playing = false;
		}
		if (timeBlack <= 0) {
			timeBlack = 0;
			ans.placeImageXY(new TextImage("Black ran out of time", Piece.SIZE/2, Color.GREEN), WIDTH/2, HEIGHT/2 + Piece.SIZE/2);
			if (playing) {
				System.out.println(++white.sessionScore + " - " + black.sessionScore);
			}
			playing = false;
		}
		
		if (board.insufficientMaterial()) {
			ans.placeImageXY(new TextImage("Insufficient Material", Piece.SIZE/2, Color.GREEN), WIDTH/2, HEIGHT/2 + Piece.SIZE/2);
			if (playing) {
				white.sessionScore += 0.5;
				black.sessionScore += 0.5;
				
				System.out.println(white.sessionScore + " - " + black.sessionScore);
			}
			playing = false;
		}
		
		if (board.fiftyMoveDraw()) {
			ans.placeImageXY(new TextImage("Fifty Move Draw", Piece.SIZE/2, Color.GREEN), WIDTH/2, HEIGHT/2 + Piece.SIZE/2);
			if (playing) {
				white.sessionScore += 0.5;
				black.sessionScore += 0.5;
				
				System.out.println(white.sessionScore + " - " + black.sessionScore);
			}
			playing = false;
		}
		
		if (repetitionCount() >= 3) {
			ans.placeImageXY(new TextImage("Three Repetition Draw", Piece.SIZE/2, Color.GREEN), WIDTH/2, HEIGHT/2 + Piece.SIZE/2);
			if (playing) {
				white.sessionScore += 0.5;
				black.sessionScore += 0.5;
			
				System.out.println(white.sessionScore + " - " + black.sessionScore);
			}
			playing = false;
		}
	}

	public WorldImage timeDisplay(double time) {
		int minutes = ((int) time)/60;
		double seconds = time % 60;
		
		String secs = Integer.toString((int) seconds);
		if (seconds < 10) {
			secs = "0" + secs;
		}
		secs = secs.substring(0, 2);
		
		return new TextImage(minutes + ":" + secs, Piece.SIZE/2, Color.BLACK);
	}

	public void onTick() {
		if (playing) {
			if (turn().equals(Side.WHITE)) {
				timeWhite -= TICK_SPEED;
			}
			else {
				timeBlack -= TICK_SPEED;
			}
			
			if (activePlayer().isAI) {
				states.add(new State(board, timeWhite, timeBlack));
				board = activePlayer().apply(board);
				switchActivePlayer();
				incrementTime();
				selected = new Posn(-1, -1);
			}
		}
	}

	Posn selected = new Posn(-1, -1);
	

	public void incrementTime() {
		if (turn().equals(Side.WHITE)) {
			timeWhite += increment;
		}
		else {
			timeBlack += increment;
		}
	}

	public void onMouseEvent(Posn p) {
		if (playing && !activePlayer().isAI) {
			int r = (int) ((1.0*p.y)/Piece.SIZE - 0.5);
			int c = p.x/Piece.SIZE;
	
			if (flipped) {
				r = 7 - r;
				c = 7 - c;
			}
	
			Posn pos = new Posn(c, r);
	
			if (Board.inBounds(pos)) {			
				if (!board.selected(pos) && board.isPiece(pos)) {
					//noinspection OptionalGetWithoutIsPresent
					if (board.get(pos).get().isColor(turn())) {
						board.select(pos);
						selected = pos;
					}
					else {
						board.deselect();
					}
				}
				else if (selected.equals(pos)) {
					board.deselect();
				}
				else if (board.isPiece(selected) && board.selected(pos)) {
					board.deselect();
					//noinspection OptionalGetWithoutIsPresent
					if (board.get(selected).get().isColor(turn())) {
						makeMove(pos);
						selected = new Posn(-1, -1);
					}
				}
			}
		}
	}
	
	public void makeMove(Posn pos) {
		states.add(new State(board, timeWhite, timeBlack));
		board = board.makeMove(selected, pos);
		incrementTime();
		switchActivePlayer();
	}

	public int repetitionCount() {
		int ans = 0;
		State currentState = states.get(states.size()-1);
		
		for (int i = 0; i < states.size()-1; i++) {
			if (states.get(i).samePosition(currentState)) {
				ans++;
			}
		}
				
		return ans;
	}

	public void onMouseReleased(Posn p, String me) {
		if (me.equals("LeftButton")) {
			onMouseEvent(p);
		}
	}
	public void onMousePressed(Posn p, String me) {
		if (me.equals("LeftButton")) {
			onMouseEvent(p);
		}
	}
	public void onMouseClicked(Posn p, String me) {
		if (me.equals("LeftButton")) {
			onMouseEvent(p);
		}
	}

	public Player activePlayer() {
		if (white.activePlayer) {
			return white;
		}
		else {
			return black;
		}
	}
	
	public Side turn() {
		return activePlayer().color;
	}
	
	public void switchActivePlayer() {
		white.swapActivity();
		black.swapActivity();
	}

	public void onKeyEvent(String ke) {
		if (playing) {
			if (ke.equals("escape")) {
				board.deselect();
				selected = new Posn(-1, -1);
			}
			else if (ke.equals("left") && !(white.isAI && black.isAI)) {
				takeBackMove();
				board.deselect();
				selected = new Posn(-1, -1);
			}
		}
		
		if (ke.equals("r")) {
			endOfWorld("reset");
			launchGame();
		}
		
		if (ke.equals("f")) {
			flip();
		}
		
		if (ke.equals("n")) {
			System.out.println(fen());
		}
	}

	public void takeBackMove() {
		if (states.size() <= 1) {
			return;
		}
		
		State s = states.get(states.size()-1);
		states.remove(states.size()-1);
		
		board = s.board;
		timeWhite = s.timeWhite;
		timeBlack = s.timeBlack;
		
		switchActivePlayer();
	}

	public void flip() {
		flipped = !flipped;
	}
}

class State {
	Board board;
	double timeWhite;
	double timeBlack;
	
	State(Board board, double timeWhite, double timeBlack) {
		this.board = board;
		this.timeBlack = Math.max(timeWhite, 0);
		this.timeWhite = Math.max(timeBlack, 0);
	}
	
	public boolean samePosition(State other) {
		return board.samePosition(other.board);
	}
}

enum Variant {
	STANDARD, CHESS960
}

class Chess {
	ChessWorld c = new ChessWorld(15, 0, new Human(Side.WHITE), new SmartAI(Side.BLACK), Variant.STANDARD);

	void testChess(Tester t) {
		c.launchGame();
	}
}