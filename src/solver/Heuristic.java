package solver;

/**
 * Vanilla heuristic: for each box, the Manhattan distance to its nearest
 * goal, summed over all boxes. Admissible but weak — it ignores walls, the
 * player, other boxes, and the fact that two boxes cannot share one goal.
 */
public class Heuristic {
  private final Board board;
  private final int[] goals;

  public Heuristic(Board board) {
    this.board = board;
    int size = board.width * board.height;
    int goalCount = 0;
    for (int pos = 0; pos < size; pos++) {
      if (board.goal[pos]) {
        goalCount++;
      }
    }
    goals = new int[goalCount];
    int idx = 0;
    for (int pos = 0; pos < size; pos++) {
      if (board.goal[pos]) {
        goals[idx++] = pos;
      }
    }
  }

  /** Sum over boxes of Manhattan distance to the nearest goal. */
  public int estimate(int[] boxes) {
    int total = 0;
    for (int box : boxes) {
      int br = box / board.width;
      int bc = box % board.width;
      int best = Integer.MAX_VALUE;
      for (int goal : goals) {
        int gr = goal / board.width;
        int gc = goal % board.width;
        int d = Math.abs(br - gr) + Math.abs(bc - gc);
        if (d < best) {
          best = d;
        }
      }
      total += best;
    }
    return total;
  }
}
