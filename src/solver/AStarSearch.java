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
      newBoxes = current.boxes.clone();
      newBoxes[boxIdx] = behind;
      Arrays.sort(newBoxes);
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
