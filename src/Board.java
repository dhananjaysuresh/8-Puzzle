import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

/**
 * Class: Board
 * @author Dhananjay Suresh
 *
 * Stores a NxN board for use with Solver class
 * and calculates relevant information
 */
public class Board {
	
	private final int[][] board;
	private final ArrayList<int[]> goal;
	private final HashMap<Integer, Pair> goalMap; 
	private int N;
	
	/**
	 * @param board
	 * @param goal
	 * Constructor to create a board from 2D int array
	 * Creates goal board and HashMap to access pairs
	 */
	public Board(int[][] board, int[][]goal) {
		super();
		N = board.length;
		this.board = new int[N][N];
		this.goalMap = new HashMap<>();
		this.goal = new ArrayList<int[]>();
		for(int i = 0; i < N; i++) {
			this.goal.add(new int[N]);
			for (int j = 0; j < N; j++) {
				this.board[i][j] = board[i][j];
				this.goal.get(i)[j] = goal[i][j];
				//Store x,y pair with the value as key
				this.goalMap.put(goal[i][j], new Pair(i,j));
			}
		}
	}
	
	/**
	 * @param board
	 * @param goal
	 * @param goalMap
	 * Constructor to create a board from 2D int array
	 * Using existing goal board and HashMap to save memory
	 */
	public Board(int[][] board, ArrayList<int[]> goal, HashMap<Integer, Pair> goalMap) {
		super();
		N = board.length;
		this.board = new int[N][N];
		for(int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				this.board[i][j] = board[i][j];
			}
		}
		this.goal = goal;
		this.goalMap = goalMap;
	}
	
	/**
	 * @return N
	 */
	public int size() {
		return board.length;
	}

	/**
	 * @return Misplace heuristic value
	 */
	public int misplaceCount() {
		int misplaceCount = 0;
		for(int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				//Checks if spaces between board and goal are equal
				if (board[i][j] != goal.get(i)[j])
					misplaceCount++;
			}
		}
		return misplaceCount;
	}
	
	/**
	 * @return Manhattan heuristic value
	 */
	public int manhattan() {
		int distance = 0;
		int currentTile;
		int goalTile;
		Pair goalPair;
		for(int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				//Get current tile
				currentTile = board[i][j];
				//Get expected tile
				goalTile = goal.get(i)[j];
				//If tile is not blank and are not equal
				if(currentTile != 0 && currentTile != goalTile) {
					//Get x,y coordinates for goal tile
					goalPair = goalMap.get(currentTile);
					//Calculate distance between the two tiles
					distance += Math.abs(i - goalPair.x) +
							Math.abs(j - goalPair.y);
				}
			}
		}
		return distance;
	}
	
	/**
	 * @return Stack of neighbors
	 */
	public Iterable<Board> neighbors() {
		Stack<Board> neighbors = new Stack<Board>();
		//zero coordinates
		int zeroX = 0, zeroY = 0;
		
		//Find zero
		outerLoop:
		for(int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if(board[i][j] == 0) {
					zeroX = i;
					zeroY = j;
					break outerLoop;
				}
			}
		}
		//Add neighbor to stack if:
		//If can move left
		if(zeroX > 0) {
			int[][] tempBoard = copyBoard();
			int temp = tempBoard[zeroX-1][zeroY];
			tempBoard[zeroX-1][zeroY] = 0;
			tempBoard[zeroX][zeroY] = temp;
			Board newBoard = new Board(tempBoard, goal, goalMap);
			neighbors.add(newBoard);
		}
		//If can move right
		if(zeroX < N-1) {
			int[][] tempBoard = copyBoard();
			int temp = tempBoard[zeroX+1][zeroY];
			tempBoard[zeroX+1][zeroY] = 0;
			tempBoard[zeroX][zeroY] = temp;
			Board newBoard = new Board(tempBoard, goal, goalMap);
			neighbors.add(newBoard);
		}
		//If can move up
		if(zeroY > 0) {
			int[][] tempBoard = copyBoard();
			int temp = tempBoard[zeroX][zeroY-1];
			tempBoard[zeroX][zeroY-1] = 0;
			tempBoard[zeroX][zeroY] = temp;
			Board newBoard = new Board(tempBoard, goal, goalMap);
			neighbors.add(newBoard);
		}
		//If can move down
		if(zeroY < N-1) {
			int[][] tempBoard = copyBoard();
			int temp = tempBoard[zeroX][zeroY+1];
			tempBoard[zeroX][zeroY+1] = 0;
			tempBoard[zeroX][zeroY] = temp;
			Board newBoard = new Board(tempBoard, goal, goalMap);
			neighbors.add(newBoard);
		}
		
		return neighbors;
	}
	
	/**
	 * @return copy of 2D int array of this board
	 */
	private int[][] copyBoard() {
		int[][] tempBoard = new int[N][];
		
		for(int i = 0; i < N; i++)
		{
		  int[] row = board[i];
		  int   rowLength = row.length;
		  tempBoard[i] = new int[rowLength];
		  System.arraycopy(row, 0, tempBoard[i], 0, rowLength);
		}
		
		return tempBoard;
	}

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     * Check if two boards are equal
     */
    public boolean equals(Object x) {
        if (x == this)
            return true;
        if (x == null)
            return false;
        if (x.getClass() != this.getClass())
            return false;

        Board that = (Board) x;
        return this.N == that.N && Arrays.deepEquals(this.board, that.board);
    }
	
    
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 * Output board to string
	 */
	public String toString() {
        StringBuilder s = new StringBuilder();

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                s.append(String.format("%2d ", board[i][j]));
            }
            s.append("\n");
        }
        return s.toString();
    }
	
	
	/**
	 * Class: Pair
	 * @author Dhananjay Suresh
	 * Class to hold x and y coordinate values
	 */
	private class Pair {
		public int x, y;

		Pair(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
	
}
