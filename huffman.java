import java.util.*;
import java.util.List;

import tester.Tester;

class ExamplesHuffman {
	Huffman huffman = new Huffman(new ArrayList<String>(Arrays.asList("a", "b", "c", "d", "e", "f")),
								   new ArrayList<Integer>(Arrays.asList(12,  45,  5,  13,   9,  16)));

	
	void testConstructor(Tester t) {
		ArrayList<Boolean> a = new ArrayList<Boolean>(Arrays.asList(true, false, false));
		ArrayList<Boolean> b = new ArrayList<Boolean>(Arrays.asList(false));
		ArrayList<Boolean> c = new ArrayList<Boolean>(Arrays.asList(true, true, false, false));
		ArrayList<Boolean> d = new ArrayList<Boolean>(Arrays.asList(true, false, true));
		ArrayList<Boolean> e = new ArrayList<Boolean>(Arrays.asList(true, true, false, true));
		ArrayList<Boolean> f = new ArrayList<Boolean>(Arrays.asList(true, true, true));		
		
		t.checkExpect(huffman.forest, new ArrayList<Tree>(Arrays.asList(new Node(new Leaf("b", 45), new Node(new Node(new Leaf("a", 12), new Leaf("d", 13)), new Node(new Node(new Leaf("c", 5), new Leaf("e", 9)), new Leaf("f", 16)))))));
		t.checkExpect(huffman.codes, new ArrayList<ArrayList<Boolean>>(Arrays.asList(a, b, c, d, e, f)));
	}
	
	void testEncodeDecode(Tester t) {
		t.checkExpect(huffman.encode("eba"), new ArrayList<Boolean>(Arrays.asList(true, true, false, true, false, true, false, false)));
		t.checkExpect(huffman.decode(new ArrayList<Boolean>(Arrays.asList(true, true, false, true, false, true, false, false))), "eba");
		
		t.checkExpect(huffman.encode("df"), new ArrayList<Boolean>(Arrays.asList(true, false, true, true, true, true)));
		t.checkExpect(huffman.decode(new ArrayList<Boolean>(Arrays.asList(true, false, true, true, true, true))), "df");
		
		t.checkExpect(huffman.encode("facade"), new ArrayList<Boolean>(Arrays.asList(true, true, true, true, false, false, true, true, false, false, true, false, false, true, false, true, true, true, false, true)));
		t.checkExpect(huffman.decode(new ArrayList<Boolean>(Arrays.asList(true, true, true, true, false, false, true, true, false, false, true, false, false, true, false, true, true, true, false, true))), "facade");

		t.checkException(new IllegalArgumentException("Tried to encode g but that is not part of the language."), huffman.all, "encode", "g");
		t.checkExpect(huffman.decode(new ArrayList<Boolean>(Arrays.asList(true, false, true, true))), "d?");
		
	
	}
	
	
}

class Huffman {
	ArrayList<String> letters;
	ArrayList<Integer> freqs;
	
	ArrayList<Tree> forest;
	Tree all;
	ArrayList<ArrayList<Boolean>> codes;
	
	Huffman(ArrayList<String> letters, ArrayList<Integer> freqs) {
		if (freqs.size() != letters.size()) throw new IllegalArgumentException("Frequencies size (" + freqs.size() + ")  not equal to Alphabet size (" + letters.size() + ").");
		if (freqs.size() < 2) throw new IllegalArgumentException("Frequencies size less than 2");
		if (letters.size() < 2) throw new IllegalArgumentException("Alphabet size less than 2");
		
		this.letters = letters;
		this.freqs = freqs;
		
		this.codes = new ArrayList<ArrayList<Boolean>>();
		this.forest = new ArrayList<Tree>();
		
		constructLeaves();
		
		constructForest();
		this.all = forest.get(0);
		
		encode();
	}
	
	public void constructLeaves() {
		for (int i = 0; i < freqs.size(); i++) forest.add(new Leaf(letters.get(i), freqs.get(i)));
		
		for (int i = 1; i < forest.size(); i++) {
			Tree key = forest.get(i);
			int j = i - 1;
			
			while (j >= 0 && forest.get(j).val > key.val) {
				Tree t = forest.get(j);
				forest.set(j + 1, t);
				j--;
			}
			
			forest.set(j + 1, key);
		}
	}
	
	public void sortForest() {
		Tree toBeSorted = forest.get(forest.size() - 1);
		
		for (int i = 0; i < forest.size(); i++) {
			if (forest.get(i).val < toBeSorted.val) {
				forest.remove(toBeSorted);
				forest.add(i, toBeSorted);
			}
		}
	}
	
	public void constructForest() {
		while (forest.size() > 1) {			
			Tree leastFreq = forest.get(0);
			Tree secondLeastFreq = forest.get(1);
			
			Tree combo = new Node(leastFreq, secondLeastFreq);
			forest.add(combo);
			
			forest.remove(1);
			forest.remove(0);
			
			sortForest();
		}
	}
	
	public void encode() {		
		for (int i = 0; i < letters.size(); i++) {
			ArrayList<Boolean> code = all.encode(letters.get(i));
			Utils.REVERSE(code);
			codes.add(code);
		}
	}
	
	public ArrayList<Boolean> encode(String text) {
		ArrayList<Boolean> ans = new ArrayList<Boolean>();
		
		for (int i = 0; i < text.length(); i++) {
			String letter = Character.toString(text.charAt(i));
			int index = letters.indexOf(letter);
			
			ArrayList<Boolean> letterCode = codes.get(index);
			ans.addAll(letterCode);
		}
		
		return ans;
	}
	
	public String decode(ArrayList<Boolean> code) {
		return all.decode(code);
	}
}

abstract class Tree {
	int val;
	
	Tree(int val) {
		this.val = val;
	}
	
	public String decode(ArrayList<Boolean> code) {
		return this.decode(code, 0, this);
	}
	
	abstract String decode(ArrayList<Boolean> code, int index, Tree all);

	abstract ArrayList<Boolean> encode(String letter);
	abstract boolean contains(String letter);
}

class Leaf extends Tree {
	String letter;
	
	Leaf(String letter, int freq) {
		super(freq);
		this.letter = letter;
	}

	public boolean contains(String letter) {
		return this.letter.equals(letter);
	}

	public ArrayList<Boolean> encode(String letter) {
		return new ArrayList<Boolean>();
	}

	public String decode(ArrayList<Boolean> code, int index, Tree all) {
		if (index >= code.size()) return letter;
		
		return letter + all.decode(code, index, all);
	}

}

class Node extends Tree {
	Tree left;
	Tree right;
	
	Node(Tree left, Tree right) {
		super(left.val + right.val);
		this.left = left;
		this.right = right;
	}

	public boolean contains(String letter) {
		return left.contains(letter) || right.contains(letter);
	}

	public ArrayList<Boolean> encode(String letter) {
		if (!this.contains(letter)) throw new IllegalArgumentException("Tried to encode " + letter + " but that is not part of the language.");
		
		if (left.contains(letter)) {
			ArrayList<Boolean> ans = left.encode(letter);
			ans.add(false);
			return ans;
		}
		
		ArrayList<Boolean> ans = right.encode(letter);
		ans.add(true);
		return ans;
	}

	public String decode(ArrayList<Boolean> code, int index, Tree all) {
		if (index >= code.size()) return "?";
		
		
		if (code.get(index)) return right.decode(code, index+1, all);
		
		return left.decode(code, index+1, all);
	}

	
}

class Utils {
	public static void REVERSE(ArrayList<Boolean> ab) {
		
		for (int i = 0; i < ab.size()/2; i++) {
			int j = ab.size() - i - 1;
			SWAP(ab, i, j);
		}
	}
	
	public static void SWAP(ArrayList<Boolean> ab, int i, int j) {		
		boolean b = ab.get(i);
		ab.set(i, ab.get(j));
		ab.set(j, b);
	}
}