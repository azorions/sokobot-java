package solver;

import java.util.Arrays;

/**
 * Static level data parsed once from the GUI's two-layer grids.
 * Positions are encoded as a single int: row * width + col.
 *
 * Vanilla version: no precomputed push distances, no dead-square detection.
 * The board only knows about walls, goals, and the starting positions.
 */
public class Board {
  public static final int[] DIR_ROW = { -1, 1, 0, 0 };
  public static final int[] DIR_COL = { 0, 0, -1, 1 };
  public static final char[] DIR_CHAR = { 'u', 'd', 'l', 'r' };

  public final int width;
  public final int height;
  public final boolean[] wall;
  public final boolean[] goal;

  public final int initialPlayer;
  public final int[] initialBoxes; // sorted

  public Board(int width, int height, char[][] mapData, char[][] itemsData) {
    this.width = width;
    this.height = height;
    int size = width * height;
    wall = new boolean[size];
    goal = new boolean[size];

    int player = -1;
    int boxCount = 0;
    for (int r = 0; r < height; r++) {
      for (int c = 0; c < width; c++) {
        int pos = r * width + c;
        if (mapData[r][c] == '#') {
          wall[pos] = true;
        } else if (mapData[r][c] == '.') {
          goal[pos] = true;
        }
        if (itemsData[r][c] == '$') {
          boxCount++;
        } else if (itemsData[r][c] == '@') {
          player = pos;
        }
      }
    }

    initialBoxes = new int[boxCount];
    int idx = 0;
    for (int r = 0; r < height; r++) {
      for (int c = 0; c < width; c++) {
        if (itemsData[r][c] == '$') {
          initialBoxes[idx++] = r * width + c;
        }
      }
    }
    Arrays.sort(initialBoxes);
    initialPlayer = player;
  }

  /** Neighbor of pos in direction dir, or -1 if it leaves the grid. */
  public int step(int pos, int dir) {
    int r = pos / width + DIR_ROW[dir];
    int c = pos % width + DIR_COL[dir];
    if (r < 0 || r >= height || c < 0 || c >= width) {
      return -1;
    }
    return r * width + c;
  }
}
