package solver;

public class SokoBot {

  public String solveSokobanPuzzle(int width, int height, char[][] mapData, char[][] itemsData) {
    Board board = new Board(width, height, mapData, itemsData);
    String solution = new AStarSearch(board).solve(System.nanoTime() + 15_000_000_000L);
    return solution != null ? solution : "";
  }

}
