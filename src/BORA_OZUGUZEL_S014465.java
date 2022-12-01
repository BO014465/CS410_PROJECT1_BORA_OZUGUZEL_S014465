//package cs410;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class BORA_OZUGUZEL_S014465 {

	public static final String FILE_NAME = "NFA1.txt";
	public static String filePath;

	public static void main(String[] args) {
		try {
			FiniteAutomata nfa = new FiniteAutomata(readFile(FILE_NAME));
			// System.out.println(nfa);
			FiniteAutomata dfa = nfa.createDfaFromNfa();
			System.out.println(dfa);
		} catch (NullPointerException e) {
			System.out.println("Scanner is null.");
		}
	}

	public static Scanner readFile(String fileName) {
		try {
			File file = new File(fileName);
			if (file.exists()) {
				filePath = file.getAbsolutePath();
			} else {
				findFile(FILE_NAME, new File("."));
			}
			return new Scanner(new File(filePath));
		} catch (Exception e) {
			System.out.println("Exception.");
		}
		return null;
	}

	public static void findFile(String fileName, File dir) {
		File[] list = dir.listFiles();
		if (list != null) {
			for (File f : list) {
				if (f.isDirectory()) {
					findFile(fileName, f);
				} else if (fileName.equals(f.getName())) {
					filePath = f.getAbsolutePath();
				}
			}
		}
	}
}

class FiniteAutomata {
	ArrayList<String> alphabet;
	ArrayList<String> states;
	String startState;
	ArrayList<String> finalStates;
	ArrayList<Transition> transitions;

	public FiniteAutomata(Scanner sc) {
		this.alphabet = new ArrayList<String>();
		this.states = new ArrayList<String>();
		this.startState = "";
		this.finalStates = new ArrayList<String>();
		this.transitions = new ArrayList<Transition>();

		int partitionState = 0;
		while (sc.hasNextLine()) {
			String currentLine = sc.nextLine();
			if (currentLine.isBlank() || currentLine.isEmpty()) {
				continue;
			}
			if (currentLine.equals("ALPHABET")) {
				partitionState = 1;
				continue;
			} else if (currentLine.equals("STATES")) {
				partitionState = 2;
				continue;
			} else if (currentLine.equals("START")) {
				partitionState = 3;
				continue;
			} else if (currentLine.equals("FINAL")) {
				partitionState = 4;
				continue;
			} else if (currentLine.equals("TRANSITIONS")) {
				partitionState = 5;
				continue;
			} else if (currentLine.equals("END")) {
				partitionState = 6;
				break;
			} else {

				if (partitionState == 1) {
					this.alphabet.add(currentLine);
				} else if (partitionState == 2) {
					this.states.add(currentLine);
				} else if (partitionState == 3) {
					this.startState = currentLine;
				} else if (partitionState == 4) {
					this.finalStates.add(currentLine);
				} else if (partitionState == 5) {
					StringTokenizer st = new StringTokenizer(currentLine, " ");
					if (st.hasMoreTokens()) {
						String transitionFromState = st.nextToken();
						String symbol = st.nextToken();
						String transitionToState = st.nextToken();
						this.transitions.add(new Transition(transitionFromState, symbol, transitionToState));
					}
				}

			}
		}
	}

	public FiniteAutomata(ArrayList<String> alphabet, ArrayList<String> states, String startState,
			ArrayList<String> finalStates, ArrayList<Transition> transitions) {
		this.alphabet = alphabet;
		this.states = states;
		this.startState = startState;
		this.finalStates = finalStates;
		this.transitions = transitions;
	}

	public FiniteAutomata createDfaFromNfa() {
		List<TransitionWithSets> transitionWithSetsList = new ArrayList<TransitionWithSets>();
		for (String symbol : this.alphabet) {
			HashSet<String> transitionFromStateSet = new HashSet<String>();
			transitionFromStateSet.add(this.startState);
			transitionWithSetsList.add(calculateAccessibleStates(transitionFromStateSet, symbol));
		}
		while (true) {
			List<HashSet<String>> newStateSets = transitionWithSetsList.stream()
					.filter(t1 -> !transitionWithSetsList.stream()
							.anyMatch(t2 -> t1.transtionToStateSets.equals(t2.transitionFromStateSets)))
					.map(t1 -> t1.transtionToStateSets).distinct().collect(Collectors.toList());
			for (HashSet<String> toStateSet : newStateSets) {
				for (String symbol : this.alphabet) {
					transitionWithSetsList.add(calculateAccessibleStates(toStateSet, symbol));
				}
			}
			if (newStateSets.size() == 0) {
				break;
			}
		}

		// for (TransitionWithSets ts : transitionWithSetsList)
		// System.out.println(ts);

		@SuppressWarnings("unchecked")
		ArrayList<String> tempAlphabet = (ArrayList<String>) this.alphabet.clone();

		ArrayList<String> tempStateNames = new ArrayList<String>();
		tempStateNames.addAll(transitionWithSetsList.stream().map(ts -> ts.createDfaStateNameWithTFSS()).distinct()
				.collect(Collectors.toList()));

		String tempStartState = "q{" + this.startState + "}";

		ArrayList<String> tempFinalStates = new ArrayList<String>();
		tempFinalStates.addAll(transitionWithSetsList.stream()
				.filter(ts -> this.finalStates.stream().anyMatch(f -> ts.transitionFromStateSets.contains(f)))
				.map(ts -> ts.createDfaStateNameWithTFSS()).distinct().collect(Collectors.toList()));

		ArrayList<Transition> tempTransitions = new ArrayList<Transition>();
		tempTransitions.addAll(transitionWithSetsList.stream().map(ts -> {
			Transition t = new Transition(ts.createDfaStateNameWithTFSS(), ts.symbol, ts.createDfaStateNameWithTTSS());
			return t;
		}).collect(Collectors.toList()));

		return new FiniteAutomata(tempAlphabet, tempStateNames, tempStartState, tempFinalStates, tempTransitions);
	}

	public TransitionWithSets calculateAccessibleStates(HashSet<String> transitionFromStateSets, String symbol) {
		TransitionWithSets transitionSet = new TransitionWithSets(symbol);
		for (String s : transitionFromStateSets) {
			transitionSet.transitionFromStateSets.add(s);
		}
		List<String> accessibleStates = new ArrayList<String>();
		for (String stateName : transitionFromStateSets) {
			accessibleStates.addAll(this.transitions.stream()
					.filter(t -> t.transitionFromState.equals(stateName) && t.symbol.equals(symbol))
					.map(t -> t.transitionToState).collect(Collectors.toList()));
		}
		for (String s : accessibleStates) {
			transitionSet.transtionToStateSets.add(s);
		}
		return transitionSet;
	}

	@Override
	public String toString() {
		String result = "ALPHABET\n";
		for (String symbol : this.alphabet) {
			result += symbol + "\n";
		}
		result += "STATES\n";
		for (String stateName : this.states) {
			result += stateName + "\n";
		}
		result += "START\n" + this.startState + "\nFINAL\n";
		for (String finalState : this.finalStates) {
			result += finalState + "\n";
		}
		result += "TRANSITIONS\n";
		for (Transition transition : this.transitions) {
			result += transition + "\n";
		}
		return result + "END";
	}

}

class Transition {
	String transitionFromState;
	String symbol;
	String transitionToState;

	public Transition(String transitionFromState, String symbol, String transitionToState) {
		this.transitionFromState = transitionFromState;
		this.symbol = symbol;
		this.transitionToState = transitionToState;
	}

	@Override
	public String toString() {
		return this.transitionFromState + " " + this.symbol + " " + this.transitionToState;
	}
}

class TransitionWithSets {
	HashSet<String> transitionFromStateSets;
	String symbol;
	HashSet<String> transtionToStateSets;

	public TransitionWithSets(String symbol) {
		this.transitionFromStateSets = new HashSet<String>();
		this.symbol = symbol;
		this.transtionToStateSets = new HashSet<String>();
	}

	public String createDfaStateNameWithTFSS() {
		String result = "q{";
		boolean firstIter = true;
		for (String s : this.transitionFromStateSets) {
			if (firstIter) {
				result += s;
				firstIter = false;
			} else
				result += ", " + s;
		}
		result += "}";
		return result;
	}

	public String createDfaStateNameWithTTSS() {
		String result = "q{";
		boolean firstIter = true;
		for (String s : this.transtionToStateSets) {
			if (firstIter) {
				result += s;
				firstIter = false;
			} else
				result += ", " + s;
		}
		result += "}";
		return result;
	}

	@Override
	public String toString() {
		String result = "{";
		boolean firstIter = true;
		for (String s : this.transitionFromStateSets) {
			if (firstIter) {
				result += s;
				firstIter = false;
			} else
				result += ", " + s;
		}
		result += "} -> " + symbol + " -> {";
		boolean firstIter2 = true;
		for (String s : this.transtionToStateSets) {
			if (firstIter2) {
				result += s;
				firstIter2 = false;
			} else
				result += ", " + s;
		}
		return result + "}";
	}
}
