package solver;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public class AStarSearch {
  private final Board board;
  private final Heuristic heuristic;
  private final int size;

  public AStarSearch(Board board) {
    this.board = board;
    this.heuristic = new Heuristic(board);
    this.size = board.width * board.height;
  }

  // Returns the move string, or null if no solution found before the deadline. 
  public String solve(long deadlineNanos) {
    int startPlayer = normalize(board.initialPlayer, board.initialBoxes);
    State start = new State(board.initialBoxes, startPlayer, 0,
        heuristic.estimate(board.initialBoxes), null, -1, -1);

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
      expand(current, open, bestG);
    }
    return null;
  }

  private void expand(State current, PriorityQueue<State> open,
      HashMap<State, Integer> bestG) {
    boolean[] box = occupancy(current.boxes);
    boolean[] reach = reachable(current.player, box);

    for (int i = 0; i < current.boxes.length; i++) {
      int p = current.boxes[i];
      for (int dir = 0; dir < 4; dir++) {
        int dest = board.step(p, dir);
        // destination must be free for the box to move into it
        if (dest == -1 || board.wall[dest] || box[dest]) {
          continue;
        }
        // player must be able to stand on the opposite side and reach it
        int from = board.step(p, dir ^ 1);
        if (from == -1 || board.wall[from] || box[from] || !reach[from]) {
          continue;
        }

        // DeadSquare Logic Addition (Early Pruning)
        if (board.deadSquare[dest]) {
          continue;
        }

        int[] newBoxes = current.boxes.clone();
        newBoxes[i] = dest;
        Arrays.sort(newBoxes);

        // Freeze Deadlock Detection (More Pruning)
        if (isFrozen(dest, newBoxes)) {
          continue;
        }

        // More Freeze Deadlock Detection (Even More Pruning)
        int d, neighborPosition;
        boolean deadlocked = false;
        for (d = 0; d < 4; d++) {
          neighborPosition = board.step(dest, d);
          if (neighborPosition != -1 && Arrays.binarySearch(newBoxes, neighborPosition) >= 0) {
            if (isFrozen(neighborPosition, newBoxes)) {
              deadlocked = true;
              break;
            }
          }
        }
        if (deadlocked) {
          continue;
        }

        int newPlayer = normalize(p, newBoxes); // after the push the player stands on p
        int g = current.g + 1;
        State next = new State(newBoxes, newPlayer, g, heuristic.estimate(newBoxes),
            current, from, dir);
        Integer known = bestG.get(next);
        if (known == null || g < known) {
          bestG.put(next, g);
          open.add(next);
        }
      }
    }
  }

  // Player reachability / normalization 
  private boolean[] occupancy(int[] boxes) {
    boolean[] box = new boolean[size];
    for (int b : boxes) {
      box[b] = true;
    }
    return box;
  }

  // Squares the player can walk to from start without moving a box. 
  private boolean[] reachable(int start, boolean[] box) {
    boolean[] seen = new boolean[size];
    ArrayDeque<Integer> queue = new ArrayDeque<>();
    seen[start] = true;
    queue.add(start);
    while (!queue.isEmpty()) {
      int cur = queue.poll();
      for (int d = 0; d < 4; d++) {
        int nx = board.step(cur, d);
        if (nx != -1 && !seen[nx] && !board.wall[nx] && !box[nx]) {
          seen[nx] = true;
          queue.add(nx);
        }
      }
    }
    return seen;
  }

  // Canonical player square = smallest index reachable in the given layout. 
  private int normalize(int player, int[] boxes) {
    boolean[] reach = reachable(player, occupancy(boxes));
    for (int i = 0; i < size; i++) {
      if (reach[i]) {
        return i;
      }
    }
    return player; // a floor square always reaches at least itself
  }

  // Freeze-deadlock detection  A box is frozen if it is off-goal and cannot ever move along either axis.
  private boolean isFrozen(int boxPosition, int[] boxes) {
    if (board.goal[boxPosition]) {
      return false;
    }
    return blockedOnAxis(boxPosition, true, boxes, new HashSet<>())
        && blockedOnAxis(boxPosition, false, boxes, new HashSet<>());
  }

  private boolean blockedOnAxis(int pos, boolean horizontal, int[] boxes,
      Set<Integer> asWall) {
    int a = board.step(pos, horizontal ? 2 : 0);
    int b = board.step(pos, horizontal ? 3 : 1);

    // Rule 1: wall / off-grid / recursion-wall on either side.
    boolean wallA = (a == -1 || board.wall[a] || asWall.contains(a));
    boolean wallB = (b == -1 || board.wall[b] || asWall.contains(b));
    if (wallA || wallB) {
      return true;
    }

    // Rule 2: both sides are simple-deadlock squares (a, b are valid here).
    if (board.deadSquare[a] && board.deadSquare[b]) {
      return true;
    }

    // Rule 3: a neighbor box that is itself frozen on the perpendicular axis.
    boolean boxA = Arrays.binarySearch(boxes, a) >= 0;
    boolean boxB = Arrays.binarySearch(boxes, b) >= 0;
    if (boxA || boxB) {
      asWall.add(pos); // current box acts as a wall while we recurse
      boolean blocked =
          (boxA && blockedOnAxis(a, !horizontal, boxes, asWall))
          || (boxB && blockedOnAxis(b, !horizontal, boxes, asWall));
      asWall.remove(pos);
      if (blocked) {
        return true;
      }
    }
    return false;
  }

  private boolean isGoal(int[] boxes) {
    for (int box : boxes) {
      if (!board.goal[box]) {
        return false;
      }
    }
    return true;
  }

  // Move-string reconstruction 

  /*
    Walks the parent chain and, for each push, replays the player's shortest
    walk up to the pushing square, then the push.
  */
  private String reconstruct(State goalState) {
    List<State> path = new ArrayList<>();
    for (State s = goalState; s.parent != null; s = s.parent) {
      path.add(s);
    }
    Collections.reverse(path);

    StringBuilder moves = new StringBuilder();
    int player = board.initialPlayer; // the player's real square, threaded forward
    for (State s : path) {
      boolean[] box = occupancy(s.parent.boxes);
      moves.append(walkPath(player, s.pushFrom, box));
      moves.append(Board.DIR_CHAR[s.pushDir]);
      player = board.step(s.pushFrom, s.pushDir); // push leaves the player on the vacated square
    }
    return moves.toString();
  }

  // Shortest sequence of u/d/l/r moving the player from start to target.
  private String walkPath(int start, int target, boolean[] box) {
    if (start == target) {
      return "";
    }
    int[] prev = new int[size];
    int[] viaDir = new int[size];
    Arrays.fill(prev, -1);
    boolean[] seen = new boolean[size];
    ArrayDeque<Integer> queue = new ArrayDeque<>();
    seen[start] = true;
    queue.add(start);
    while (!queue.isEmpty()) {
      int cur = queue.poll();
      if (cur == target) {
        break;
      }
      for (int d = 0; d < 4; d++) {
        int nx = board.step(cur, d);
        if (nx != -1 && !seen[nx] && !board.wall[nx] && !box[nx]) {
          seen[nx] = true;
          prev[nx] = cur;
          viaDir[nx] = d;
          queue.add(nx);
        }
      }
    }
    StringBuilder rev = new StringBuilder();
    for (int at = target; at != start; at = prev[at]) {
      rev.append(Board.DIR_CHAR[viaDir[at]]);
    }
    return rev.reverse().toString();
  }
}
