package solver;

import java.util.Arrays;

public class State {
  public final int[] boxes;   // sorted positions
  public final int player;    // normalized (canonical) reachable player square
  public final int g;         // pushes so far
  public final int h;         // heuristic estimate (lower bound on pushes left)

  public final State parent;
  public final int pushFrom;  // square the player stood on before the push (-1 for root)
  public final int pushDir;   // direction of the push (-1 for root)

  private final int hash;

  public State(int[] boxes, int player, int g, int h, State parent,
      int pushFrom, int pushDir) {
    this.boxes = boxes;
    this.player = player;
    this.g = g;
    this.h = h;
    this.parent = parent;
    this.pushFrom = pushFrom;
    this.pushDir = pushDir;
    this.hash = Arrays.hashCode(boxes) * 31 + player;
  }

  public int f() {
    return g + h;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof State)) {
      return false;
    }
    State o = (State) other;
    return player == o.player && Arrays.equals(boxes, o.boxes);
  }

  @Override
  public int hashCode() {
    return hash;
  }
}
