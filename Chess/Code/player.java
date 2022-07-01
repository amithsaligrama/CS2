import java.util.*;
import java.util.function.*;

abstract class Player implements Function<Board, Board> {
	final Side color;
	boolean activePlayer;
	boolean isAI;
	double sessionScore = 0;
	
	Player(Side color) {
		this.color = color;
	}

	public void swapActivity() {
		activePlayer = !activePlayer;
	}
	
	public abstract Board apply(Board b);
}

class Human extends Player {
	boolean isAI = false;
	
	Human(Side color) {
		super(color);
	}

	public Board apply(Board b) {
		throw new UnsupportedOperationException("reject human, become machine");
	}
}

abstract class AI extends Player {
	AI(Side color) {
		super(color);
		isAI = true;
	}
}

class RanDumbAI extends AI {
	RanDumbAI(Side color) {
		super(color);
	}
	
	public Board apply(Board b) {
		return b.makeRandomMove(color);
	}
}

class SmartAI extends AI {
	static final int DEPTH = 4;
	
	Map<Board, Integer> alreadyComputedValues = new HashMap<>();
	
	public int alphaBeta(Board b) {
		int ans = alphaBeta(b, DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, color.opponent());
		System.out.println(ans);
		return ans;
	}
	
	public int alphaBeta(Board b, int depth, int alpha, int beta, Side s) {
		if (alreadyComputedValues.containsKey(b)) {
			return alreadyComputedValues.get(b);
		}
		
		if (depth <= 0 || b.endOfGame()) {
			int ans = b.heuristicValue();
			alreadyComputedValues.put(b, b.heuristicValue());
			return ans;
		}

		Set<Action> acts = b.allActions(s);

		int ans;
		if (s.equals(Side.WHITE)) {
			ans = Integer.MIN_VALUE;
			
			for (Action a : acts) {
				ans = Math.max(ans, alphaBeta(a.apply(b), depth-1, alpha, beta, s.opponent()));

				if (ans >= beta) {
					break;
				}
				alpha = Math.max(alpha, ans);
			}

		}
		else {
			ans = Integer.MAX_VALUE;
			
			for (Action a : acts) {
				ans = Math.min(ans, alphaBeta(a.apply(b), depth-1, alpha, beta, s.opponent()));
				
				if (ans <= alpha) {
					break;
				}
				beta = Math.min(beta, ans);
			}

		}
		alreadyComputedValues.put(b, ans);
		return ans;
	}
	
	SmartAI(Side color) {
		super(color);
	}

	public Board apply(Board b) {
		List<Action> acts = new ArrayList<>(b.allActions(color));
		
		Action bestMove = acts.get(0);
		bestMove.print(b);
		int bestVal = alphaBeta(bestMove.apply(b));
		
		if (color.equals(Side.WHITE)) {
			for (int i = 1; i < acts.size(); i++) {
				Action a = acts.get(i);
				a.print(b);
				
				int val = alphaBeta(a.apply(b));
				if (bestVal < val) {
					bestMove = a;
					bestVal = val;
				}
			}
		}
		else {

			for (int i = 1; i < acts.size(); i++) {
				Action a = acts.get(i);
				a.print(b);
				
				int val = alphaBeta(a.apply(b));
				if (bestVal > val) {
					bestMove = a;
					bestVal = val;
				}
			}
		}
			
		System.out.print("the best move is: ");
		bestMove.print(b);
		return bestMove.apply(b);
	}
}