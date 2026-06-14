package solver;

import java.util.Arrays;

/**
 * Immutable search node for move-based vanilla A*.
 *
 * Vanilla version: identity is the EXACT player square plus the box layout.
 * There is no player-position normalization, so the same box layout with the
 * player on a different square counts as a different state.
 */
public class State {
  public final int[] boxes;   // sorted positions
  public final int player;    // exact player square (part of identity)
  public final int g;         // player moves so far
  public final int h;         // heuristic estimate

  public final State parent;
  public final char move;     // move that produced this state ('\0' for root)

  private final int hash;

  public State(int[] boxes, int player, int g, int h, State parent, char move) {
    this.boxes = boxes;
    this.player = player;
    this.g = g;
    this.h = h;
    this.parent = parent;
    this.move = move;
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
