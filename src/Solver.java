import java.awt.Dimension;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Project: 8 Puzzle Solver
 * Class: Solver
 * @author DJ
 * CSCI 363
 * 
 * Solves 3x3 board puzzles
 */
public class Solver {

	private ArrayList<BoardNode> closed;
	private PriorityQueue<BoardNode> open;
	private int nodesExpanded, numberofOptimalSolutions;
	private long startTime;
	private long endTime;
	private long executionTime;
	private long optimalTime;

	/**
	 * Constructor that sets node counter
	 * and execution time to 0;
	 */
	public Solver() {
		super();
		nodesExpanded = 0;
		executionTime = 0;
	}
	
	/**
	 * @return number of nodes expanded
	 */
	public int numberOfNodesExpanded() {
		return nodesExpanded;
	}
	
	/**
	 * @return number of optimal solutions
	 */
	public int numberofOptimalSolutions() {
		return numberofOptimalSolutions;
	}
	
	/**
	 * @return execution time in nanoseconds
	 */
	public long executionTime() {
		return executionTime;
	}
	
	/**
	 * @return optimal time in nanoseconds
	 */
	public long optimalTime() {
		return optimalTime;
	}

	/**
	 * @return String of solution boards
	 */
	public String solution() {
		StringBuilder ss = new StringBuilder();
		for(BoardNode bn : closed) {
			ss.append(bn.board.toString());
			ss.append("\n");
		}
		return ss.toString();
	}


	/**
	 * @param startBoard- initial board
	 * @param useMisplace- toggle for heuristic
	 * @return boolean if board was solved
	 * 
	 * Solves board using A*
	 */
	public boolean solveUsingAStar(Board startBoard, boolean useMisplace) {
		//Initialize time counter, closed and open list
		startTime = System.nanoTime();
		executionTime = 0;
		closed = new ArrayList<BoardNode>();
		open = new PriorityQueue<BoardNode>();
		boolean solved = false;
		
		//Create start node
		BoardNode node = new BoardNode(startBoard, 0, null, useMisplace);
		nodesExpanded = 0;
		numberofOptimalSolutions = 0;
		open.add(node);
		
		//Loop while solution is not found
		while(!solved) {
			//Check if open list is empty
			if(open.isEmpty())
				return false;
			//Pop node from open list with lowest fValue
			node = open.poll();
			nodesExpanded++;

			//Check if node is goal state
			if(node.hValue == 0) {
				closed.add(node);
				System.out.println("Board Final \n" + node.board);
				numberofOptimalSolutions++;
				endTime = System.nanoTime();
				optimalTime = endTime - startTime;
				executionTime = optimalTime;
				return true;
			}
			//Add node to closed list
			closed.add(node);
			//Get neighbors
			Iterable<Board> neighbors = node.board.neighbors();
			for(Board n : neighbors) {
				BoardNode newNode = new BoardNode(n, node.moves+1, node, useMisplace);
				//If neighbor already is checked process it or
				//add to open list
				if (processNeighbor(newNode))
					continue;
				else {
					open.add(newNode);
				}
			}
		}
		endTime = System.nanoTime();
		executionTime = endTime - startTime;
		return false;
	}

	/**
	 * @param startBoard- initial board
	 * @return boolean if board was solved
	 * 
	 * Solves board using Depth-First-Brand and Bound
	 */
	public boolean solveUsingDFBB(Board startBoard) {
		//Initialize time counter, closed and open list
		startTime = System.nanoTime();
		executionTime = 0;
		closed = new ArrayList<BoardNode>();
		open = new PriorityQueue<BoardNode>();
		boolean solved = false;
		//Optimal solution values
		int L = Integer.MAX_VALUE;
		BoardNode optimalNode = null;

		//Create start node
		BoardNode node = new BoardNode(startBoard, 0, null, false);
		open.add(node);
		nodesExpanded = 0;
		numberofOptimalSolutions = 0;

		//Loop while there are unvisited nodes
		while(!open.isEmpty()) {
			//Pop node with lowest fValue
			node = open.poll();	
			
			//If node is goal and is better than best solution found
			if(node.hValue == 0  && node.fValue < L) {
				solved = true;
				System.out.println("L Value: " + L + "\nBoard Optimal Found: " + node.board);
				numberofOptimalSolutions++;
				//Set as new optimal solution
				optimalNode = node;
				L = node.fValue;
				//Record time
				endTime = System.nanoTime();
				optimalTime = endTime - startTime;
			}
			else {
				//Add to visited list
				closed.add(node);
				nodesExpanded++;
				//Get neighbors
				Iterable<Board> neighbors = node.board.neighbors();
				neighborLoop:
					for(Board n : neighbors) {
						BoardNode neighbor = new BoardNode(n, node.moves+1, node, false);
						//If neighbor is already visited continue
						for(BoardNode bn : closed) {
							if(bn.board.equals(neighbor.board))
								continue neighborLoop; 
						}
						//If neighbor is less than optimal solution add to open list
						if(neighbor.fValue < L)
							open.add(neighbor);
					}
			}
		}
		//Solved
		if(solved) {
			closed.clear();
			closed.add(optimalNode);
			System.out.println("L Value: " + L + "\nBoard Final Found: \n" + optimalNode.board);
			while(optimalNode.previous != null) {
				closed.add(optimalNode.previous);
				optimalNode = optimalNode.previous;
			}
			endTime = System.nanoTime();
			executionTime = endTime - startTime;
			return true;
		}
		else {
			endTime = System.nanoTime();
			executionTime = endTime - startTime;
			return false;
		}
	}
	
	/**
	 * @param startBoard- initial board
	 * @return boolean if board was solved
	 * 
	 * Solves board using IDA*
	 */
	public boolean solveUsingIDAStar(Board startBoard) {
		//Initialize time counter, closed and open list
		startTime = System.nanoTime();
		executionTime = 0;
		closed = new ArrayList<BoardNode>();
		nodesExpanded = 0;
		numberofOptimalSolutions = 0;
		boolean solved = false;
		
		//Create start node and initialize level
		BoardNode node = new BoardNode(startBoard, 0, null, false);
		int level = node.hValue;
		
		//Loop increasing level until solved
		while(!solved) {
			//Call helper function
			solved = IDAStarHelper(node, level);
			if(solved) {
				endTime = System.nanoTime();
				executionTime = endTime - startTime;
				return true;
			}
			else
				//Increase level
				level++;
		}
		return false;
	}
	
	/**
	 * @param startBoard- initial board
	 * @param level- current level
	 * @return boolean if board was solved
	 * 
	 * IDA* helper function
	 */
	private boolean IDAStarHelper(BoardNode bn, int level) {
		boolean solved = false;
		nodesExpanded++;
		//Check if board is solution
		if(bn.hValue == 0) {
			System.out.println("Board Final Found: \n" + bn.board);
			numberofOptimalSolutions++;
			closed.clear();
			closed.add(bn);
			while(bn.previous != null) {
				closed.add(bn.previous);
				bn = bn.previous;
			}
			return true;
		}
		
		//If neighbor's fValue is greater than current level return false
		if(bn.fValue > level)
			return false;

		//Get neighbors
		Iterable<Board> neighbors = bn.board.neighbors();
		for(Board n : neighbors) {
			BoardNode neighborNode = new BoardNode(n, bn.moves+1, bn, false);
			//Recursive call to neighbor
			solved = IDAStarHelper(neighborNode, level);
			if(solved)
				return true;
		}
		return false;
	}

	/**
	 * @param neighborNode- neighbor
	 * @return boolean if neighbor existed in either list
	 * 
	 * Checks if neighbor is in either list and handles
	 * accordingly
	 */
	private boolean processNeighbor(BoardNode neighborNode) {
		//Check closed list
		for(int i = 0; i < closed.size(); i++) {
			BoardNode closedNode = closed.get(i);
			if(closedNode.board.equals(neighborNode.board)) {
				//If neighbor is better than node in closed list
				if(closedNode.moves > neighborNode.moves) {
					//Add neighbor to open list
					closed.remove(i);
					closedNode.moves = neighborNode.moves;
					closedNode.calculatefValue();
					closedNode.previous = neighborNode.previous;
					open.add(closedNode);
					return true; 
				}
				else
					return true;
			}
		}
		//Check open list
		for(BoardNode openNode : open) {
			if(openNode.board.equals(neighborNode.board)) {
				//If neighbor is better than node in closed list
				if(openNode.moves > neighborNode.moves) {
					//Update neighbor in open list with better neighbor
					BoardNode updatedBN = openNode;
					open.remove(openNode);
					updatedBN.moves = neighborNode.moves;
					updatedBN.calculatefValue();
					updatedBN.previous = neighborNode.previous;
					open.add(updatedBN);
					return true; 
				}
				else
					return true;
			}
		}
		return false;
	}


	/**
	 * Class:BoardNode
	 * @author Dhananjay Suresh
	 * 
	 *	Node to be used with Solver class
	 */
	private class BoardNode implements Comparable<BoardNode> {
		private Board board;
		private int moves;
		private BoardNode previous;
		private int hValue = -1;
		private int fValue = -1;
		boolean useMisplace;

		/**
		 * @param board- current board
		 * @param moves- number of moves
		 * @param previous- previous board
		 * @param useMisplace- toggle to use misplace heuristic function
		 * 
		 * Constructor to create node
		 */
		BoardNode(Board board, int moves, BoardNode previous, boolean useMisplace) {
			this.board = board;
			this.previous = previous;
			this.moves = moves;
			this.useMisplace = useMisplace;
			calculatefValue();
		}
		//Calculate f(n)
		private int calculatefValue() {
			if (hValue == -1) {
				//Calculate manhattan distance or misplace count
				if(useMisplace)
					hValue = board.misplaceCount();
				else
					hValue = board.manhattan();
				//f(n) = g(n) + h(n)
				fValue = moves + hValue;
			}
			return fValue;
		}

		/**
		 * @return number of moves to current board
		 */
		public int gValue() {
			return moves;
		}
		/**
		 * @return heuristic function value
		 */
		public int hValue() {
			return hValue;
		}
		/**
		 * @return f(n) value
		 */
		public int fValue() {
			return fValue;
		}

		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 * 
		 * Overridden compareTo function to compare hValues
		 */
		@Override
		public int compareTo(BoardNode o) {
			// TODO Auto-generated method stub
			if (this.hValue < o.hValue) {
				return -1;
			}
			if (this.hValue > o.hValue) {
				return +1;
			}
			return 0;
		}
	}

	public static void main(String[] args) {
		//various boards in 2d int arrays
		int[][] goal = {
				{1,2,3},
				{8,0,4},
				{7,6,5}
		};

		int[][] easy = {
				{1,3,4},
				{8,6,2},
				{7,0,5}
		};
		int[][] medium = {
				{2,8,1},
				{0,4,3},
				{7,6,5}
		};
		int[][] hard = {
				{2,8,1},
				{4,6,3},
				{0,7,5}
		};
		int[][] worst = {
				{5,6,7},
				{4,0,8},
				{3,2,1}
		};
		//Create new board
		Board b1 = new Board(worst, goal);
		//Create new solver
		Solver s = new Solver();
		
		String[] options = { "A* with Manhattan Heuristic", "A* with Misplace Heuristic", "IDA* with Manhattan Heuristic", "DFBnB with Manhattan Heuristic" };
		int response = JOptionPane.showOptionDialog(null, "Choose one of the following search algorithms", "8 Puzzle Solver",
		        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
		        null, options, options[0]);
		boolean b;
		//Solve using picked search algorithm
		System.out.println("Solving using: " + options[response]);
		switch(response) {
			case 0:
				b = s.solveUsingAStar(b1, false);
				break;
			case 1:
				b = s.solveUsingAStar(b1, true);
				break;
			case 2:
				b = s.solveUsingIDAStar(b1);
				break;
			case 3:
				b = s.solveUsingDFBB(b1);
				break;
				
			default:
				System.exit(0);
				
		}
		//Display relevant information in JOptionPane
        StringBuilder ss = new StringBuilder();
        ss.append(String.format("Nodes Expanded %2d ", s.numberOfNodesExpanded()));
        ss.append("\n");
        ss.append(String.format("Number of Optimal Solutions %2d ", s.numberofOptimalSolutions()));
        ss.append("\n");
        ss.append(String.format("Total execution Time %2d ", s.executionTime()));
        ss.append("\n");
        ss.append(String.format("Optimal Time %2d ", s.optimalTime()));
        ss.append("\n");
        ss.append(s.solution());
		
        JTextArea textArea = new JTextArea(ss.toString());
        JScrollPane scrollPane = new JScrollPane(textArea);  
        textArea.setLineWrap(true);  
        textArea.setWrapStyleWord(true); 
        textArea.setEditable(false);
        scrollPane.setPreferredSize( new Dimension( 500, 500 ) );
        JOptionPane.showMessageDialog(null, scrollPane,String.format("8-Puzzle result using %s", options[response]) ,  
                                               JOptionPane.OK_OPTION);
	}

}
