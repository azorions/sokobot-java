package solver;

import java.util.Arrays;
import java.util.HashMap;
import java.util.PriorityQueue;

/**
 * Vanilla (textbook) A* over PLAYER MOVES. Every node is one player step
 * (u/d/l/r); pushing a box is just a move whose destination holds a box.
 *
 * Compared with the optimized solver there is:
 *   - no push-based abstraction (every walking step is a node)
 *   - no player-position normalization
 *   - no dead-square or freeze-deadlock pruning
 *   - no weighting (plain f = g + h)
 */
public class AStarSearch {
  private final Board board;
  private final Heuristic heuristic;

  public AStarSearch(Board board) {
    this.board = board;
    this.heuristic = new Heuristic(board);
  }

  /** Returns the move string, or null if no solution found before the deadline. */
  public String solve(long deadlineNanos) {
    State start = new State(board.initialBoxes, board.initialPlayer,
        0, heuristic.estimate(board.initialBoxes), null, '\0');

    PriorityQueue<State> open = new PriorityQueue<>(
        (a, b) -> a.f() != b.f() ? Integer.compare(a.f(), b.f())
                                 : Integer.compare(b.g, a.g));
    HashMap<State, Integer> bestG = new HashMap<>();
    open.add(start);
    bestG.put(start, 0);

    int expanded = 0;
    while (!open.isEmpty()) {
      if ((++expanded & 1023) == 0 && System.nanoTime() > deadlineNanos) {
        return null; // out of time
      }
      State current = open.poll();
      if (current.g > bestG.getOrDefault(current, Integer.MAX_VALUE)) {
        continue; // stale queue entry
      }
      if (isGoal(current.boxes)) {
        return reconstruct(current);
      }

      for (int dir = 0; dir < 4; dir++) {
        State next = tryMove(current, dir);
        if (next == null) {
          continue;
        }
        Integer known = bestG.get(next);
        if (known == null || next.g < known) {
          bestG.put(next, next.g);
          open.add(next);
        }
      }
    }
    return null;
  }

// Checks if a box would be frozen in place
private boolean isFrozen(int boxPosition, int[] currentBoxes){
  if (board.goal[boxPosition]){
    return false;
  }

  int up = board.step(boxPosition, 0), 
      down = board.step(boxPosition, 1), 
      left = board.step(boxPosition, 2), 
      right = board.step(boxPosition, 3);

  boolean isBlockedLeft = (left == -1 || board.wall[left] || Arrays.binarySearch(currentBoxes, left) >= 0),
          isBlockedRight = (right == -1 || board.wall[right] || Arrays.binarySearch(currentBoxes, right) >= 0),
          isBlockedHorizontal = isBlockedLeft && isBlockedRight;      

  boolean isBlockedUp = (up == -1 || board.wall[up] || Arrays.binarySearch(currentBoxes, up) >= 0),
          isBlockedDown = (down == -1 || board.wall[down] || Arrays.binarySearch(currentBoxes, down) >= 0),
          isBlockedVertical = isBlockedUp && isBlockedDown;

  return isBlockedHorizontal && isBlockedVertical;
}

  /** One player step in dir: walk into a free square, or push a box if one
   *  is there and the square beyond it is free. Returns null if illegal. */
  private State tryMove(State current, int dir) {
    int dest = board.step(current.player, dir);
    if (dest == -1 || board.wall[dest]) {
      return null;
    }
    int boxIdx = Arrays.binarySearch(current.boxes, dest);
    int[] newBoxes = current.boxes;
    if (boxIdx >= 0) {
      int behind = board.step(dest, dir);
      if (behind == -1 || board.wall[behind]
          || Arrays.binarySearch(current.boxes, behind) >= 0) {
        return null; // push blocked
      }

      // DeadSquare Logic Addition (Early Pruning)
      if (board.deadSquare[behind]){
        return null;
      }

      newBoxes = current.boxes.clone();
      newBoxes[boxIdx] = behind;
      Arrays.sort(newBoxes);

      // Freeze Deadlock Detection (More Pruning)
      if (isFrozen(behind, newBoxes)){
        return null;
      }
      
      // More Freeze Deadlock Detection (Even More Pruning)
      int d, neighborPosition;
      for (d = 0; d < 4; d++){
        neighborPosition = board.step(behind, d);
        if (neighborPosition != -1 && Arrays.binarySearch(newBoxes, neighborPosition) >= 0){
          if (isFrozen(neighborPosition, newBoxes)){
            return null; 
          } 
        }
      }
    }
    int h = heuristic.estimate(newBoxes);
    return new State(newBoxes, dest, current.g + 1, h, current,
        Board.DIR_CHAR[dir]);
  }

  private boolean isGoal(int[] boxes) {
    for (int box : boxes) {
      if (!board.goal[box]) {
        return false;
      }
    }
    return true;
  }

  /** Walks the parent chain back to the root, collecting one char per move. */
  private String reconstruct(State goalState) {
    StringBuilder moves = new StringBuilder();
    for (State s = goalState; s.parent != null; s = s.parent) {
      moves.append(s.move);
    }
    return moves.reverse().toString();
  }
}
