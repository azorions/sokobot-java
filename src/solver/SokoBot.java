package solver;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.awt.Point;
public class SokoBot {

  private int columnCount;    // number of columns (width)
  private int rowCount;       // number of rows (height)
  private char[][] staticMap; // static layer: '#' = wall, '.' = button, ' ' = floor, 
                              // '@' = player, '$' = box, '*' = box on button

  private int playerRow;
  private int playerColumn;
  //got rid of Set<Integer> boxPositions in favor of Set<Point> boxPositionsSet

  //This is all for storing map data into text file for later retrieval

  private Point playerPosition;
  
  private Set<Point> boxPositionsSet; //set of all box positions in x,y; Maybe this should be a HashSet? Or assume that boxPositionSet(1) = box 1, boxPositionSet(2) = box 2, etc.?
  //Size of boxPositionSet == size of goalPositionSet?

  private Set<Point> goalPositionSet; //set of all button positions in x,y
  private Set<Point> wallPositionSet; //set of all wall positions in x,y
  private Set<String> visitedStatesSet;

  private static final char[] MOVE_LABELS = { 'u', 'd', 'l', 'r' };// up, down, left, right

  
  // search algo to find the solution for the problem


  private void search(int width, int height, char[][] mapData, char[][] itemsData){ /*!!!!!*/
    //while (!isGoal()){
    StringBuilder move = new StringBuilder();
    /**This function searches for a solution to sokoban levels using State-based AI*/

    //Get tile type positions and append to Point sets
    for(int i = 0; i < height; i++){
      for(int j = 0; j < width; j++){
        char tile = mapData[i][j];
        if(tile == '@')
          playerPosition = new Point(j,i); //j = x, i = y
        else if(tile == '$')
          boxPositionsSet.add(new Point(j,i));
        else if(tile == '#')
          wallPositionSet.add(new Point(j,i));
        else if(tile == '.')
          goalPositionSet.add(new Point(j,i));
      }
    }

    //does player have a space to move to?
    if(isWall(playerPosition.x - 1, playerPosition.y) == false)
      move.append('l');
    else if(isWall(playerPosition.x + 1, playerPosition.y) == false)
      move.append('r');
    if(isWall(playerPosition.x, playerPosition.y - 1) == false)
      move.append('u');
    else if(isWall(playerPosition.x, playerPosition.y + 1) == false)
      move.append('d');
    /***How should I loop this? ^^^ */


    //is player position near a box?
    if(boxPositionsSet.contains(new Point(playerPosition.x + 1, playerPosition.y)))  
      {
        
        //does player position with box have 3 spaces left and right?
    }
    else if(boxPositionsSet.contains(new Point(playerPosition.x - 1, playerPosition.y))){
        //does player position with box have 3 spaces left and right?
    }
    else if(boxPositionsSet.contains(new Point(playerPosition.x, playerPosition.y + 1)))  
      {
        //does player position with box have 3 spaces top and down?
    }
    else if(boxPositionsSet.contains(new Point(playerPosition.x, playerPosition.y - 1))){
        //does player position with box have 3 spaces top and down?
    }
    /**I made it like this because box can only go in one direction in front of where player is headed^^^*/

    
    //is player position with a box near a goal?
    //is player position with a box near a wall?
    
    //}
  }

  // Returns true when every box is sitting on a goal tile (puzzle solved).


  private boolean isGoal() { /*!!!!!*/
    return true;
  }

  // Flattens a (row, column) cell into one unique int so it fits in a Set<Integer>.
  // I plan to just use 1 unique integer for box positions around the map


  private int encode(int row, int column) { /*!!!!!*/
    return row * columnCount + column;
  }

  // Checks whether a cell is a wall or lies outside the board.


  private boolean isWall(int row, int column) { /*!!!!!*/
    return row < 0 || row >= rowCount || column < 0 || column >= columnCount
           || staticMap[row][column] == '#';
  }

  // True if a box pushed here is wedged in a corner (two perpendicular walls) and can never move.


  private boolean isDeadlockCorner(int row, int column) { /*!!!!!*/
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


  public String solveSokobanPuzzle(int width, int height, char[][] mapData, char[][] itemsData) {
    /*
     * YOU NEED TO REWRITE THE IMPLEMENTATION OF THIS METHOD TO MAKE THE BOT SMARTER
     */
    /*
     * Default stupid behavior: Think (sleep) for 3 seconds, and then return a
     * sequence
     * that just moves left and right repeatedly.
     */
    

    String finalsolution = "lrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlr";

    try {
      Thread.sleep(3000);
      search(width, height, mapData, itemsData);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return finalsolution;
  }


}
