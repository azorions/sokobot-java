package solver;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class SokoBot {

  private int columnCount;    // number of columns (width)
  private int rowCount;       // number of rows (height)
  private char[][] staticMap; // static layer: '#', '.', ' '

  private int playerRow;
  private int playerColumn;
  private Set<Integer> boxPositions;

  private static final char[] MOVE_LABELS = { 'u', 'd', 'l', 'r' };

  public String solveSokobanPuzzle(int width, int height, char[][] mapData, char[][] itemsData) {
    /*
     * YOU NEED TO REWRITE THE IMPLEMENTATION OF THIS METHOD TO MAKE THE BOT SMARTER
     */
    /*
     * Default stupid behavior: Think (sleep) for 3 seconds, and then return a
     * sequence
     * that just moves left and right repeatedly.
     */
    try {
      Thread.sleep(3000);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return "lrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlr";
  }

  // search algo to find the solution for the problem
  private void search(){
  }

  // Returns true when every box is sitting on a goal tile (puzzle solved).
  private boolean isGoal() {
    return true;
  }

  // Flattens a (row, column) cell into one unique int so it fits in a Set<Integer>.
  // I plan to just use 1 unique integer for box positions around the map
  private int encode(int row, int column) {
    return row * columnCount + column;
  }

  // Checks whether a cell is a wall or lies outside the board.
  private boolean isWall(int row, int column) {
    return row < 0 || row >= rowCount || column < 0 || column >= columnCount
           || staticMap[row][column] == '#';
  }

  // True if a box pushed here is wedged in a corner (two perpendicular walls) and can never move.
  private boolean isDeadlockCorner(int row, int column) {
    if (staticMap[row][column] == '.') {
      return false; //box on a goal is fine even in a corner
    }
    boolean wallAbove = isWall(row - 1, column);
    boolean wallBelow = isWall(row + 1, column);
    boolean wallLeft = isWall(row, column - 1);
    boolean wallRight = isWall(row, column + 1);
    return (wallAbove && wallLeft) || (wallAbove && wallRight)
            || (wallBelow && wallLeft) || (wallBelow && wallRight);
  }

  // Stores the player position and box positions for the current state.
  private void State(int playerRow, int playerColumn, Set<Integer> boxPositions) {
    this.playerRow = playerRow;
    this.playerColumn = playerColumn;
    this.boxPositions = boxPositions;
  }
}
