package solver;

public class SokoBot {

  public String solveSokobanPuzzle(int width, int height, char[][] mapData, char[][] itemsData) {
    Board board = new Board(width, height, mapData, itemsData);
    // Single plain A* pass; 14s budget so check mode's 15s limit is respected.
    String solution = new AStarSearch(board).solve(System.nanoTime() + 14_000_000_000L);
    return solution != null ? solution : "";
  }

}
