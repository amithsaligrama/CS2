import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.Map.*;
import java.util.stream.*;
import javalib.impworld.*;
import javalib.worldimages.*;

class Board {
	static final int SIZE = Piece.SIZE;
	static final int FULL_SIZE = 8 * SIZE;
	
	static final Color LIGHT = new Color(240, 217, 181);
	static final Color DARK = new Color(181, 136, 99);
	static final WorldImage LIGHT_SQUARE = new RectangleImage(SIZE, SIZE, "solid", LIGHT);
	static final WorldImage DARK_SQUARE = new RectangleImage(SIZE, SIZE, "solid", DARK);
	
	static final Color LIGHT_PREVIOUS_COLOR = new Color(171, 162, 58);
	static final Color DARK_PREVIOUS_COLOR = new Color(206, 210, 107);
	static final WorldImage LIGHT_PREVIOUS_SQUARE = new RectangleImage(SIZE, SIZE, "solid", LIGHT_PREVIOUS_COLOR);
	static final WorldImage DARK_PREVIOUS_SQUARE = new RectangleImage(SIZE, SIZE, "solid", DARK_PREVIOUS_COLOR);
	
	static final WorldImage SELECTED_PIECE_BG_DARK = new RectangleImage(SIZE, SIZE, "solid", new Color(100, 110, 64));
	static final WorldImage SELECTED_PIECE_BG_LIGHT = new RectangleImage(SIZE, SIZE, "solid", new Color(129, 150, 105));
	
	static final WorldImage POSSIBLE_MOVES_BG_DARK = new OverlayImage(new CircleImage(SIZE/6, "solid", new Color(100, 110, 64)), DARK_SQUARE);
	static final WorldImage POSSIBLE_MOVES_BG_LIGHT = new OverlayImage(new CircleImage(SIZE/6, "solid", new Color(129, 150, 105)), LIGHT_SQUARE);
	
	static final WorldImage POSSIBLE_CAPTURES_BG_DARK = new OverlayImage(new CircleImage(SIZE/2, "solid", DARK), SELECTED_PIECE_BG_LIGHT);
	static final WorldImage POSSIBLE_CAPTURES_BG_LIGHT = new OverlayImage(new CircleImage(SIZE/2, "solid", LIGHT), SELECTED_PIECE_BG_LIGHT);

	static final WorldImage CHECK_BG = new CircleImage(SIZE/2, "solid", Color.RED);
	
	public static boolean inBounds(int r, int c) {
		return r < 8 && r >= 0 && c < 8 && c >= 0;
	}
	
	public static boolean inBounds(Posn p) {
		return inBounds(p.y, p.x);
	}

	public static Piece stringToPiece(String str, Side color) {
		switch (str) {
			case "Bishop":
				return new Bishop(color);
			case "Knight":
				return new Knight(color);
			case "King":
				return new King(color);
			case "Queen":
				return new Queen(color);
			case "Rook":
				return new Rook(color);
			case "Pawn":
				return new Pawn(color);
			default:
				throw new IllegalArgumentException(str + " not a piece");
		}
	}

	public static Posn stringToPosn(String str) {
		char c = str.charAt(0);
		int i = Integer.parseInt(str.charAt(1)+"");
		
		int j = -1;
		switch (c) {
			case 'a':
				j = 0;
			case 'b':
				j = 1;
			case 'c':
				j = 2;
			case 'd':
				j = 3;
			case 'e':
				j = 4;
			case 'f':
				j = 5;
			case 'g':
				j = 6;
			case 'h':
				j = 7;
		}
		
		return new Posn(8-i, j);
	}
	
	public void copyMap(Map<Posn, Piece> pieceMap) {
		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				Posn p = new Posn(c, r);
				if (pieceMap.containsKey(p)) {
					pieces.put(p, pieceMap.get(p).copy());
				}
			}
		}
	}

	Posn start = new Posn(-1, -1);
	Posn end = new Posn(-1, -1);
	
	int fiftyMoveCounter = 0;
	
	final Map<Posn, Piece> pieces = new HashMap<>();

	Set<Posn> selected = new HashSet<>();
	Posn current = new Posn(-1, -1);
	
	Board(Map<Posn, Piece> pieces, int fiftyMoveCounter, Posn start, Posn end) {
		copyMap(pieces);
		this.fiftyMoveCounter = fiftyMoveCounter;
		
		this.start = start;
		this.end = end;
		
		setChecks();
	}
	
	public void initialize(Variant v) {
		if (v.equals(Variant.STANDARD)) {
			initializeStandard();
		}
		else if (v.equals(Variant.CHESS960)) {
			initialize960();
		}
	}

	public void initialize960() {
		for (int i = 0; i < 8; i++) {
			pieces.put(new Posn(i, 1), new Pawn(Side.BLACK));
			pieces.put(new Posn(i, 6), new Pawn(Side.WHITE));
		}

		List<String> piecesToPlace = new ArrayList<>(
				Arrays.asList("Bishop", "Bishop", "Knight", "Knight", "King", "Queen", "Rook", "Rook"));

		Random r = new Random();
		int firstBishopCol = -1;
		for (int i = 0; i < 8; i++) {
			int index = r.nextInt(piecesToPlace.size());
			String s = piecesToPlace.get(index);
			while (s.equals("Bishop") && firstBishopCol >= 0 && firstBishopCol % 2 == i % 2) {
				index = r.nextInt(piecesToPlace.size());
				s = piecesToPlace.get(index);
			}
			if (s.equals("Bishop")) {
				firstBishopCol = i;
			}
			piecesToPlace.remove(index);

			Piece pWhite = stringToPiece(s, Side.WHITE);
			Piece pBlack = stringToPiece(s, Side.BLACK);

			pieces.put(new Posn(i, 7), pWhite);
			pieces.put(new Posn(i, 0), pBlack);
		}
	}

	public void initializeStandard() {
		for (int i = 0; i < 8; i++) {
			pieces.put(new Posn(i, 1), new Pawn(Side.BLACK));
			pieces.put(new Posn(i, 6), new Pawn(Side.WHITE));
		}

		pieces.put(new Posn(0, 0), new Rook(Side.BLACK));
		pieces.put(new Posn(7, 0), new Rook(Side.BLACK));
		pieces.put(new Posn(0, 7), new Rook(Side.WHITE));
		pieces.put(new Posn(7, 7), new Rook(Side.WHITE));

		pieces.put(new Posn(1, 0), new Knight(Side.BLACK));
		pieces.put(new Posn(6, 0), new Knight(Side.BLACK));
		pieces.put(new Posn(1, 7), new Knight(Side.WHITE));
		pieces.put(new Posn(6, 7), new Knight(Side.WHITE));

		pieces.put(new Posn(2, 0), new Bishop(Side.BLACK));
		pieces.put(new Posn(5, 0), new Bishop(Side.BLACK));
		pieces.put(new Posn(2, 7), new Bishop(Side.WHITE));
		pieces.put(new Posn(5, 7), new Bishop(Side.WHITE));

		pieces.put(new Posn(3, 0), new Queen(Side.BLACK));
		pieces.put(new Posn(4, 0), new King(Side.BLACK));
		pieces.put(new Posn(3, 7), new Queen(Side.WHITE));
		pieces.put(new Posn(4, 7), new King(Side.WHITE));
	}

	Board(Variant v) {
		initialize(v);
	}
	
	Board(String[] utils, String[] ranks) {
		for (int rank = 0; rank < 8; rank++) {
			int file = 0;
			for (int i = 0; i < ranks[rank].length(); i++) {
				char c = ranks[rank].charAt(i);
				if (Character.isDigit(c)) {
					file += Integer.parseInt(c+"");
				}
				else {
					Posn p = new Posn(file, rank);
					if (c == 'r') {
						pieces.put(p, new Rook(Side.BLACK));
					}
					else if (c == 'p') {
						pieces.put(p, new Pawn(Side.BLACK));
					}
					else if (c == 'q') {
						pieces.put(p, new Queen(Side.BLACK));
					}
					else if (c == 'n') {
						pieces.put(p, new Knight(Side.BLACK));
					}
					else if (c == 'k') {
						pieces.put(p, new King(Side.BLACK));
					}
					else if (c == 'b') {
						pieces.put(p, new Bishop(Side.BLACK));
					}
					else if (c == 'R') {
						pieces.put(p, new Rook(Side.WHITE));
					}
					else if (c == 'P') {
						pieces.put(p, new Pawn(Side.WHITE));
					}
					else if (c == 'Q') {
						pieces.put(p, new Queen(Side.WHITE));
					}
					else if (c == 'N') {
						pieces.put(p, new Knight(Side.WHITE));
					}
					else if (c == 'K') {
						pieces.put(p, new King(Side.WHITE));
					}
					else if (c == 'B') {
						pieces.put(p, new Bishop(Side.WHITE));
					}
					file++;
				}
			}
		}
		
		String castlingAbility = utils[2];
		
		if (!castlingAbility.contains("k")) {
			get(new Posn(7, 0)).ifPresent(Piece::setMoved);
		}
		if (!castlingAbility.contains("q")) {
			get(new Posn(0, 0)).ifPresent(Piece::setMoved);
		}
		if (!castlingAbility.contains("K")) {
			get(new Posn(7, 7)).ifPresent(Piece::setMoved);
		}
		if (!castlingAbility.contains("Q")) {
			get(new Posn(0, 7)).ifPresent(Piece::setMoved);
		}
		
		if (!utils[3].contains("-")) {
			Posn p = stringToPosn(utils[3]);
			unpassant(p);
		}
		
		if (!utils[4].contains("-")) {
			fiftyMoveCounter = Integer.parseInt(utils[4]);
		}
		
		setChecks();
	}
	
	public String fen() {
		StringBuilder ans = new StringBuilder();
		for (int r = 0; r < 8; r++) {
			int nonPieces = 0;
			for (int c = 0; c < 8; c++) {
				Posn p = new Posn(c, r);
				if (isPiece(p)) {
					if (nonPieces > 0) {
						ans.append(nonPieces);
						nonPieces = 0;
					}
					Piece pi = get(p).get();
					if (pi.isColor(Side.WHITE)) {
						if (pi.isRook()) {
							ans.append("R");
						}
						else if (pi.isBishop()) {
							ans.append("B");
						}
						else if (pi.isKing()) {
							ans.append("K");
						}
						else if (pi.isKnight()) {
							ans.append("N");
						}
						else if (pi.isPawn()) {
							ans.append("P");
						}
						else if (pi.isQueen()) {
							ans.append("Q");
						}
					}
					else {
						if (pi.isRook()) {
							ans.append("r");
						}
						else if (pi.isBishop()) {
							ans.append("b");
						}
						else if (pi.isKing()) {
							ans.append("k");
						}
						else if (pi.isKnight()) {
							ans.append("n");
						}
						else if (pi.isPawn()) {
							ans.append("p");
						}
						else if (pi.isQueen()) {
							ans.append("q");
						}
					}
				}
				else {
					nonPieces++;
				}
			}
			if (nonPieces > 0) {
				ans.append(nonPieces);
			}
			if (r < 7) {
				ans.append("/");
			}
		}
		return ans.toString();
	}

	public void display(WorldScene ans, boolean flipped) {
		boolean startColor = true;
		for (int r = 0; r < 8; r++) {
			boolean color = startColor;
			for (int c = 0; c < 8; c++) {
				Posn p = new Posn(c, r);
				
				if (flipped) {
					c = 7-c;
					r = 7-r;
				}

				if (color) {
					if (isPiece(p)) {
						Piece pi = get(p).get();
						if (p.equals(current)) {
							ans.placeImageXY(pi.pieceImage.overlayImages(SELECTED_PIECE_BG_LIGHT),
									(int) ((c + 0.5) * (Piece.SIZE)),
									(r + 1) * (Piece.SIZE));
						}
						else if (selected(p)) {
							ans.placeImageXY(pi.pieceImage.overlayImages(POSSIBLE_CAPTURES_BG_LIGHT),
									(int) ((c + 0.5) * (Piece.SIZE)),
									(r + 1) * (Piece.SIZE));
						}
						else if (inCheck(pi.color) && pi.isKing()) {
							ans.placeImageXY(pi.pieceImage.overlayImages(CHECK_BG.overlayImages(LIGHT_SQUARE)),
									(int) ((c + 0.5) * (Piece.SIZE)),
									(r + 1) * (Piece.SIZE));
						}
						else if (p.equals(start) || p.equals(end)) {
							ans.placeImageXY(pi.pieceImage.overlayImages(LIGHT_PREVIOUS_SQUARE),
									(int) ((c + 0.5) * (Piece.SIZE)),
									(r + 1) * (Piece.SIZE));
						}
						else {
							ans.placeImageXY(pi.pieceImage.overlayImages(LIGHT_SQUARE),
									(int) ((c + 0.5) * (Piece.SIZE)),
									(r + 1) * (Piece.SIZE));
						}
					}
					else if (selected(p)) {
						ans.placeImageXY(POSSIBLE_MOVES_BG_LIGHT,
								(int) ((c + 0.5) * (Piece.SIZE)),
								(r + 1) * (Piece.SIZE));
					}
					else if (p.equals(start) || p.equals(end)) {
						ans.placeImageXY(LIGHT_PREVIOUS_SQUARE,
								(int) ((c + 0.5) * (Piece.SIZE)),
								(r + 1) * (Piece.SIZE));
					}
					else {
						ans.placeImageXY(LIGHT_SQUARE,
								(int) ((c + 0.5) * (Piece.SIZE)),
								(r + 1) * (Piece.SIZE));
					}
				}
				else {
					if (isPiece(p)) {
						Piece pi = get(p).get();
						if (p.equals(current)) {
							ans.placeImageXY(pi.pieceImage.overlayImages(SELECTED_PIECE_BG_DARK),
									(int) ((c + 0.5) * (Piece.SIZE)),
									(r + 1) * (Piece.SIZE));
						}
						else if (selected(p)) {
							ans.placeImageXY(pi.pieceImage.overlayImages(POSSIBLE_CAPTURES_BG_DARK),
									(int) ((c + 0.5) * (Piece.SIZE)),
									(r + 1) * (Piece.SIZE));
						}
						else if (inCheck(pi.color) && pi.isKing()) {
							ans.placeImageXY(pi.pieceImage.overlayImages(CHECK_BG.overlayImages(DARK_SQUARE)),
									(int) ((c + 0.5) * (Piece.SIZE)),
									(r + 1) * (Piece.SIZE));
						}
						else if (p.equals(start) || p.equals(end)) {
							ans.placeImageXY(pi.pieceImage.overlayImages(DARK_PREVIOUS_SQUARE),
									(int) ((c + 0.5) * (Piece.SIZE)),
									(r + 1) * (Piece.SIZE));
						}
						else {
							ans.placeImageXY(pi.pieceImage.overlayImages(DARK_SQUARE),
									(int) ((c + 0.5) * (Piece.SIZE)),
									(r + 1) * (Piece.SIZE));
						}
					}
					else if (selected(p)) {
						ans.placeImageXY(POSSIBLE_MOVES_BG_DARK,
								(int) ((c + 0.5) * (Piece.SIZE)),
								(r + 1) * (Piece.SIZE));
					}
					else if (p.equals(start) || p.equals(end)) {
						ans.placeImageXY(DARK_PREVIOUS_SQUARE,
								(int) ((c + 0.5) * (Piece.SIZE)),
								(r + 1) * (Piece.SIZE));
					}
					else {
						ans.placeImageXY(DARK_SQUARE,
								(int) ((c + 0.5) * (Piece.SIZE)),
								(r + 1) * (Piece.SIZE));
					}
				}
				
				color = !color;
				if (flipped) {
					c = 7-c;
					r = 7-r;
				}
			}
			startColor = !startColor;
		}
	}
	
	public void setChecks() {
		if (inCheck(Side.WHITE)) {
			//noinspection OptionalGetWithoutIsPresent
			get(getKing(Side.WHITE)).get().putInCheck();
		}
		else {
			//noinspection OptionalGetWithoutIsPresent
			get(getKing(Side.WHITE)).get().unCheck();
		}
		
		if (inCheck(Side.BLACK)) {
			//noinspection OptionalGetWithoutIsPresent
			get(getKing(Side.BLACK)).get().putInCheck();
		}
		else {
			//noinspection OptionalGetWithoutIsPresent
			get(getKing(Side.BLACK)).get().unCheck();
		}
	}
	
	public boolean fiftyMoveDraw() {
		return fiftyMoveCounter >= 100; // move for black and white
	}

	public boolean checkmated(Side s) {
		Set<Action> acts = allActions(s);
		return inCheck(s) && acts.size() == 0;
	}
	
	public boolean stalemated(Side s) {
		Set<Action> acts = allActions(s);
		return !inCheck(s) && acts.size() == 0;
	}

	public Set<Action> allActions(Side s) {

		return pieces.entrySet().stream()
				.filter(e -> e.getValue().isColor(s))
				.map(e -> e.getValue().generateActions(this, e.getKey()))
				.flatMap(Set::stream)
				.collect(Collectors.toSet())/*.stream().sorted((a1, a2) -> (int) (a1.priority(this) - a2.priority(this))).collect(Collectors.toList())*/;
	}
	
	public boolean insufficientMaterial() {		
		int whiteBishopFreq = 0;
		int whiteKnightFreq = 0;
		
		int blackBishopFreq = 0;
		int blackKnightFreq = 0;
		
		Set<Entry<Posn, Piece>> allPieces = pieces.entrySet();
		
		for (Entry<Posn, Piece> e : allPieces) {
			Piece p = e.getValue();
			if (p.isRook() || p.isPawn() || p.isQueen()) {
				return false;
			}
			
			if (p.isBishop() && p.isColor(Side.WHITE)) {
				whiteBishopFreq++;
				if (whiteBishopFreq >= 2) {
					return false;
				}
			}
			if (p.isBishop() && p.isColor(Side.BLACK)) {
				blackBishopFreq++;
				if (blackBishopFreq >= 2) {
					return false;
				}
			}
			
			if (p.isKnight() && p.isColor(Side.WHITE)) {
				whiteKnightFreq++;
				if (whiteKnightFreq >= 3) {
					return false;
				}
			}
			if (p.isKnight() && p.isColor(Side.BLACK)) {
				blackKnightFreq++;
				if (blackKnightFreq >= 3) {
					return false;
				}
			}
		}
		return true;
	}

//	public void setMoved() {
//		Stream<Entry<Posn, Piece>> pawnsMoved = pieces.entrySet().stream()
//				.filter(e -> e.getValue().isPawn())
//				.filter(e -> ((e.getValue().isColor(Side.WHITE) && e.getKey().y != 6) 
//								|| (e.getValue().isColor(Side.BLACK) && e.getKey().y != 1)))
//				.peek(e -> e.setValue(e.getValue().copy()))
//				.peek(e -> e.getValue().setMoved());
//		
//		Stream<Entry<Posn, Piece>> rooksMoved = pieces.entrySet().stream()
//				.filter(e -> e.getValue().isRook())
//				.filter(e -> ((e.getValue().isColor(Side.WHITE) 
//										&& !(e.getKey().y == 7 && e.getKey().x == 7)
//										&& !(e.getKey().y == 7 && e.getKey().x == 0)) 
//								|| ((e.getValue().isColor(Side.BLACK) 
//										&& !(e.getKey().y == 0 && e.getKey().x == 7)
//										&& !(e.getKey().y == 0 && e.getKey().x == 0)))))
//				.peek(e -> e.setValue(e.getValue().copy()))
//				.peek(e -> e.getValue().setMoved());
//		
//		Stream<Entry<Posn, Piece>> kingsMoved = pieces.entrySet().stream()
//				.filter(e -> e.getValue().isKing())
//				.filter(e -> ((e.getValue().isColor(Side.WHITE) && !(e.getKey().y == 7 && e.getKey().x == 4)) 
//						  || ((e.getValue().isColor(Side.BLACK) && !(e.getKey().y == 0 && e.getKey().x == 4)))))
//				.peek(e -> e.setValue(e.getValue().copy()))
//				.peek(e -> e.getValue().setMoved());
//	}

	public void select(Posn p) {
		deselect();
		if (isPiece(p)) {
			current = p;
			//noinspection OptionalGetWithoutIsPresent
			selected = get(p).get()
					.generateActions(this, p)
					.stream()
					.map(a -> a.end)
					.collect(Collectors.toSet());
		}
	}
	
	public void deselect() {
		current = new Posn(-1, -1);
		selected.clear();
	}

	public void movePieceTo(Piece p, Posn start, Posn end) {		
		removePieceAt(end);
		placePieceAt(p, end);
		removePieceAt(start);
	}

	public void removePieceAt(Posn p) {
		if (inBounds(p)) {
			pieces.remove(p);
		}
	}

//	public void print() {
//		for (Cell[] row : grid) {
//			for (Cell c : row) {
//				if (c.isPiece()) {
//					if (c.isPawn()) System.out.print(((Piece) c).color.toString().charAt(0) + "P");
//					else System.out.print(((Piece) c).color.toString().charAt(0) + c.toString());
//				}
//				else {
//					System.out.print("  ");
//				}
//				
//				System.out.print("|");
//			}
//			System.out.println("\n-----------------------------");
//		}
//	}

	public void placePieceAt(Piece p, Posn pn) {
		if (inBounds(pn)) {
			pieces.put(pn, p.copy());
		}
	}
	
	public Optional<Piece> get(Posn p) {
		return Optional.ofNullable(pieces.get(p));
	}
	public boolean isPiece(Posn p) {
		return get(p).isPresent(); // pieces.containsKey(p);
	}
	
	public boolean selected(Posn p) {
		return selected.contains(p);
	}

	public Board makeMove(Posn start, Posn end) {
		this.start = start;
		this.end = end;
		//noinspection OptionalGetWithoutIsPresent
		return get(start).get().makeMove(this, start, end);
	}

	public boolean inCheck(Side s) {
		return attacked(getKing(s), s);
	}
	
	public boolean attacked(Posn p, Side s) {
		return pieces.entrySet()
				.stream()
				.filter(e -> e.getValue().isColor(s.opponent()))
				.anyMatch(e -> e.getValue().attacks(this, p, e.getKey()));
	}
	
	public Posn getKing(Side s) {
		//noinspection OptionalGetWithoutIsPresent
		return pieces.entrySet().stream()
				.filter(e -> e.getValue().isColor(s) && e.getValue().isKing())
				.findFirst()
				.get()
				.getKey();
	}
	
	public boolean samePosition(Board other) {
		return fen().equals(other.fen());
	}
	
	public boolean equals(Object other) {
		return samePosition((Board) other);
	}

	public void unpassant(Posn p) {
		unpassant();
		//noinspection OptionalGetWithoutIsPresent
		if (isPiece(p) && get(p).get().isPawn()) {
			//noinspection OptionalGetWithoutIsPresent
			((Pawn) get(p).get()).enpassantable = true;
		}
		
//		Stream<Entry<Posn, Piece>> unpassantedPawns = pieces.entrySet()
//				.stream()
//				.filter(e -> e.getValue().isPawn() && e.getKey().y >= 2 && e.getKey().y <= 5 && !e.getKey().equals(p))
//				.peek(e -> e.setValue(e.getValue().copy()))
//				.peek(e -> e.getValue().unpassant());
	}
	public void unpassant() {
		pieces.forEach((k, v) -> v.unpassant());
		
//		Stream<Entry<Posn, Piece>> unpassantedPawns = pieces.entrySet()
//				.stream()
//				.filter(e -> e.getValue().isPawn() && e.getKey().y >= 2 && e.getKey().y <= 5)
//				.peek(e -> e.setValue(e.getValue().copy()))
//				.peek(e -> e.getValue().unpassant());
	}

	public Board makeRandomMove(Side color) {
		List<Action> acts = new ArrayList<>(allActions(color));
		
		Random r = new Random();
		int index = r.nextInt(acts.size());
		Action a = acts.get(index);
		
		Board ans = a.apply(this);
		a.print(ans);
		return ans;
	}

	public boolean endOfGame() {
		return fiftyMoveDraw() || insufficientMaterial() || 
				stalemated(Side.WHITE) || stalemated(Side.BLACK) ||
				checkmated(Side.WHITE) || checkmated(Side.BLACK);
	}

	public boolean canCastleTo(Posn p) {
		return isPiece(p) && !get(p).get().moved;
	}
	public boolean canCastleThrough(Posn p, Side color) {
		return !isPiece(p) && !attacked(p, color);
	}

	public int heuristicValue() {
		if (checkmated(Side.WHITE)) {
			return Integer.MIN_VALUE;
		}
		if (checkmated(Side.BLACK)) {
			return Integer.MAX_VALUE;
		}
		if (fiftyMoveDraw() || insufficientMaterial() || stalemated(Side.WHITE) || stalemated(Side.BLACK)) {
			return 0;
		}
		 
		return pieces.values()
				.stream()
				.map(Piece::numericValue)
				.mapToInt(Integer::intValue).sum();
	}

	public String addCastlingFen() {
		String ans = "";
		Piece whiteKing = get(getKing(Side.WHITE)).get();
		if (!whiteKing.moved) {
			Posn p = new Posn(0, 7);
			//noinspection OptionalGetWithoutIsPresent
			if (isPiece(p) && !get(p).get().moved) {
				ans += "Q";
			}
			p = new Posn(7, 7);
			//noinspection OptionalGetWithoutIsPresent
			if (isPiece(p) && !get(p).get().moved) {
				ans += "K";
			}
		}
		Piece blackKing = get(getKing(Side.BLACK)).get();
		if (!blackKing.moved) {
			Posn p = new Posn(0, 0);
			//noinspection OptionalGetWithoutIsPresent
			if (isPiece(p) && !get(p).get().moved) {
				ans += "q";
			}
			p = new Posn(0, 7);
			//noinspection OptionalGetWithoutIsPresent
			if (isPiece(p) && !get(p).get().moved) {
				ans += "k";
			}
		}
		
		if (ans.equals("")) {
			return "-";
		}
		
		return ans;
	}

	public Optional<Posn> getEnpassantable() {
		return pieces.entrySet().stream()
				.filter(e -> e.getValue().isEnpassantable())
				.map(Entry::getKey)
				.findFirst();
	}
}