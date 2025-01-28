import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// represents the minesweeper game
class Minesweeper extends World {
  ArrayList<ArrayList<Cell>> board;

  Minesweeper(int rows, int columns, int mines, Random rand, boolean reveal) {
    // allows a game with at least a single non-bomb tile
    if (mines > rows * columns - 1) {
      throw new IllegalArgumentException("too many mines!");
    }

    // initializes the gameboard
    this.board = new ArrayList<ArrayList<Cell>>(rows);

    // adds the number of cells in the array lists based on the given number of rows
    // and columns
    for (int i = 0; i < rows; i++) {
      ArrayList<Cell> inner = new ArrayList<Cell>();
      for (int j = 0; j < columns; j++) {
        if (reveal) {
          inner.add(new Cell(false, true));
        } else {
          inner.add(new Cell());
        }
      }
      this.board.add(inner);
    }

    // creates a new list of random distinct cartPt's within the bounds of the given
    // rows and columns. these cartPt's represent where the bombs are
    ArrayList<CartPt> bombs = new Utils().mineList(rows, columns, mines, rand);

    // mutates each cell's neighboring cells, mutates the mines, and mutates each
    // non-bomb-cells minecount

    new Utils().addNeighborsToBoard(this.board);
    new Utils().addMines(this.board, bombs);
    new Utils().addMineCountToBoard(this.board);

  }

  Minesweeper(ArrayList<ArrayList<Cell>> board) {
    this.board = board;
    new Utils().addNeighborsToBoard(this.board);
    new Utils().addMineCountToBoard(this.board);
  }

  Minesweeper(ArrayList<ArrayList<Cell>> board, boolean b) {
    this.board = board;
    if (b) {
      new Utils().addNeighborsToBoard(this.board);
    }

  }


  // determines what happens to the game on a mouseclick
  public void onMouseClicked(Posn pos, String buttonName) {
    if ((pos.x > 100 && pos.x < 100 + 20 * this.board.get(0).size()) 
        && (pos.y > 100 && pos.y < 100 + 20 * this.board.size())) {
      if (buttonName.equals("RightButton")) {
        new Utils().flagCell(pos, this.board);
      }
      else if (buttonName.equals("LeftButton")) {
        new Utils().clickCell(pos, this.board);
      }
    }
  }

  // draws the game onto a WorldScene
  public WorldScene makeScene() {
    return new Utils().drawBoard(this.board, getEmptyScene());
  }
}

// represents a utils class for minesweeper
class Utils {

  // done
  // returns a distinct list of cartPt's representing where the mines are on the
  // game
  ArrayList<CartPt> mineList(int rows, int columns, int mines, Random rand) {
    ArrayList<CartPt> temp = new ArrayList<CartPt>();
    while (temp.size() < mines) {
      CartPt newMine = new CartPt(rand.nextInt(rows), rand.nextInt(columns));
      if (!this.alreadyIn(temp, newMine)) {
        temp.add(newMine);
      }
    }
    return temp;
  }

  // done
  // determines if a cartpt is in the given minelist already
  boolean alreadyIn(ArrayList<CartPt> mineList, CartPt newMine) {
    boolean result = false;
    for (CartPt mine : mineList) {
      if (mine.sameCartPt(newMine)) {
        result = true;
      }
    }
    return result;
  }

  // done
  // mutates the cells to bombs on the given board for each CartPt in the given
  // mineList
  void addMines(ArrayList<ArrayList<Cell>> board, ArrayList<CartPt> mineList) {
    for (int i = 0; i < mineList.size(); i++) {
      mineList.get(i).addSingleMine(board);
    }
  }

  // done
  // simply returns a list of cartPt's of the "neighbors" of the given row and
  // column index's
  // *** determining which neighbors are possible on the given gameboard is done
  // in the
  // addCellNeighbors method ***
  ArrayList<CartPt> possibleNeighbors(int row, int column) {
    return new ArrayList<CartPt>(Arrays.asList(new CartPt(row, column + 1),
        new CartPt(row, column - 1), new CartPt(row + 1, column), new CartPt(row + 1, column + 1),
        new CartPt(row + 1, column - 1), new CartPt(row - 1, column),
        new CartPt(row - 1, column + 1), new CartPt(row - 1, column - 1)));
  }

  /// done
  // adds cell neighbors for a row on the board
  void addNeighborsToRow(ArrayList<ArrayList<Cell>> board, ArrayList<Cell> row, int rowNum) {
    for (int i = 0; i < row.size(); i++) {
      row.get(i).addCellNeighbors(rowNum, i, row.size(), board);
    }
  }

  // done
  // adds cell neighbors to the whole board
  void addNeighborsToBoard(ArrayList<ArrayList<Cell>> board) {
    for (int i = 0; i < board.size(); i++) {
      for (int j = 0; j < board.get(i).size(); j++) {
        board.get(i).get(j).addCellNeighbors(i, j, board.get(i).size(), board);
      }
      // this.addNeighborsToRow(board, board.get(i), i);
    }
  }

  // done
  // adds the minecount to each non-bomb-cell
  void addMineCountToBoard(ArrayList<ArrayList<Cell>> board) {
    for (int i = 0; i < board.size(); i++) {
      for (int j = 0; j < board.get(0).size(); j++) {
        board.get(i).get(j).mineCounter();
      }
    }
  }

  // draws the board of cells
  WorldScene drawBoard(ArrayList<ArrayList<Cell>> board, WorldScene world) {
    if (new Utils().mineVisible(board)) {
      world.placeImageXY(new TextImage("Game Over", 20, Color.BLACK), 
          100 + board.size() / 2 * 20, 100 + board.get(0).size() / 2 * 20);
    } else {
      for (int i = 0; i < board.size(); i++) {
        for (int j = 0; j < board.get(0).size(); j++) {
          world.placeImageXY(board.get(i).get(j).drawCell(), 
              100 + j * 20, 100 + i * 20);
        }
      }
    }
    return world;
  }

  // flags a cell on the board
  void flagCell(Posn pos, ArrayList<ArrayList<Cell>> board) {
    board.get((int) ((pos.y - 90) / 20)).get((int) ((pos.x - 90) / 20)).setToOppositeFlagged();
  }


  // clicks a cell with the given position
  void clickCell(Posn pos, ArrayList<ArrayList<Cell>> board) {
    board.get((int) ((pos.y - 90) / 20)).get((int) ((pos.x - 90) / 20)).leftClick();
  }

  // determines if a mine is visible on the given board
  boolean mineVisible(ArrayList<ArrayList<Cell>> board) {
    boolean result = false;
    for (int i = 0; i < board.size(); i++) {
      for (int j = 0; j < board.get(0).size(); j++) {
        if (board.get(i).get(j).isBomb && board.get(i).get(j).isRevealed) {
          result = true;
        }

      }
    }
    return result;
  }
}

// represents a cartesian point, or index's of our minesweeper game
class CartPt {
  int rowsDown;
  int columnsAcross;

  CartPt(int rowsDown, int columnsAcross) {
    this.rowsDown = rowsDown;
    this.columnsAcross = columnsAcross;
  }

  // determines if this CartPt is the same as the given CartPt
  public boolean sameCartPt(CartPt that) {
    return this.rowsDown == that.rowsDown && this.columnsAcross == that.columnsAcross;
  }

  // sets the cell with this CartPt's coordinates to a bomb
  public void addSingleMine(ArrayList<ArrayList<Cell>> board) {
    board.get(this.rowsDown).get(this.columnsAcross).isBomb = true;
  }

  // determines if this cartPt is valid on the game board given its rowSize and
  // colSize
  public boolean isValidCartPt(int rowSize, int colSize) {
    return this.rowsDown >= 0 && this.rowsDown < rowSize && this.columnsAcross >= 0
        && this.columnsAcross < colSize;
  }

  // retreieves the cell at this CartPt on the game board
  public Cell findCell(ArrayList<ArrayList<Cell>> board) {
    return board.get(this.rowsDown).get(this.columnsAcross);
  }
}

// represents a cell in the game minesweeper
class Cell {
  boolean isBomb;
  boolean isRevealed;
  boolean isFlagged;
  int numMines;
  ArrayList<Cell> neighbors;

  Cell() {
    this.isBomb = false;
    this.isRevealed = false;
    this.isFlagged = false;
    this.numMines = 0;
    this.neighbors = new ArrayList<Cell>();
  }

  // constructor to make revealed cells
  Cell(boolean isBomb, boolean isRevealed) {
    this.isBomb = isBomb;
    this.isRevealed = isRevealed;
    this.isFlagged = false;
    this.numMines = 0;
    this.neighbors = new ArrayList<Cell>();
  }

  Cell(boolean isBomb, boolean isRevealed, boolean isFlagged, int numMines) {
    this.isBomb = isBomb;
    this.isRevealed = isRevealed;
    this.isFlagged = false;
    this.numMines = numMines;
    this.neighbors = new ArrayList<Cell>();
  }

  // checks if this cell has zero mines
  public boolean checkZeroMines() {
    return this.numMines == 0;
  }

  // sets this cell's isBomb field to true
  void setToBomb() {
    this.isBomb = true;
  }

  // sets this cells isRevealed boolean to true
  void makeRevealed() {
    this.isRevealed = true;
  }

  // negates this cell's isFlagged field
  void setToOppositeFlagged() {
    this.isFlagged = !this.isFlagged;
  }


  // determines what is shown/not shown when a player left clicks on this cell
  void leftClick() {
    if (this.isBomb) {
      this.isBomb = true;
      this.isRevealed = true;
    }
    if (!this.isFlagged) { 
      if (!this.isRevealed) {
        this.isRevealed = true;
        if (this.numMines == 0) {
          this.floodFill();
        }   
      }
    }
  }


  // flood fills the board if this cell has 0 mines
  public void floodFill() {
    this.isRevealed = true;
    if (this.numMines == 0) {
      for (Cell neighbor : neighbors) {
        neighbor.leftClick();
      }
    }  
  }

  // addcellneighbors(0, 0, 2, board)
  // adds all of the possible cell neighbors to this cells neighbors list
  void addCellNeighbors(int i, int j, int rowSize, ArrayList<ArrayList<Cell>> board) {
    ArrayList<CartPt> possibleNeighbors = new Utils().possibleNeighbors(i, j);

    for (int k = 0; k < possibleNeighbors.size(); k++) {
      if (possibleNeighbors.get(k).isValidCartPt(board.size(), rowSize)) {
        this.neighbors.add(possibleNeighbors.get(k).findCell(board));
      }
    }
  }

  // determines if this cell is a bomb, returns 1 if it is a bomb, and 0 if not
  int bombAdd() {
    if (isBomb) {
      return 1;
    }
    else {
      return 0;
    }
  }

  // determines how many mines are surrounding this cell
  void mineCounter() {
    int temp = 0;
    for (int i = 0; i < this.neighbors.size(); i++) {
      temp = temp + neighbors.get(i).bombAdd();
    }
    this.numMines = temp;
  }

  // draws a single cell based on if its a bomb, revealed, flagged, and its cell
  // count
  WorldImage drawCell() {
    if (this.isRevealed) {

      if (!this.isBomb) {
        if (this.numMines > 0) {
          return new OverlayImage(new TextImage(Integer.toString(this.numMines), 13, Color.BLACK),
              new OverlayImage(new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK),
                  (new RectangleImage(20, 20, OutlineMode.SOLID, Color.GRAY))));
        }
        else {
          return new OverlayImage(new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK),
              (new RectangleImage(20, 20, OutlineMode.SOLID, Color.GRAY)));
        }
      }
      else {
        return new OverlayImage(new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK),
            (new RectangleImage(20, 20, OutlineMode.SOLID, Color.RED)));
      }
    }
    else if (this.isFlagged) {
      return new OverlayImage(new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK),
          (new RectangleImage(20, 20, OutlineMode.SOLID, Color.YELLOW)));
    }
    else {
      return new OverlayImage(new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK),
          (new RectangleImage(20, 20, OutlineMode.SOLID, Color.DARK_GRAY)));
    }
  }
}

// examples class for Minesweeper
class ExamplesMines {
  Minesweeper ClickedUnRevealedBoard2;
  Minesweeper unRevealedBoard2;
  Minesweeper board1;
  Minesweeper board2;
  Minesweeper boardWithMineAt00;
  Minesweeper boardWithNoNums;
  Minesweeper boardWithNums;
  Minesweeper boardWithNoNums2ndVersion;
  Minesweeper boardWithNums2ndVersion;
  Minesweeper board22;
  Minesweeper boardWithNoNeighbors;
  Minesweeper unrevealedGame;
  Minesweeper unrevealedGameV2;
  Minesweeper unrevealedGameV3;
  Minesweeper unrevealedGameV4;

  ArrayList<Cell> ClickedUnRevealedRow1;
  ArrayList<Cell> ClickedUnRevealedRow2;
  ArrayList<Cell> CLickedUnRevealedRow3;

  ArrayList<Cell> unRevealedRow1;
  ArrayList<Cell> unRevealedRow2;
  ArrayList<Cell> unRevealedRow3;

  ArrayList<Cell> row1;
  ArrayList<Cell> row2;
  ArrayList<Cell> row3;

  ArrayList<Cell> row1WithMineAt00;
  ArrayList<Cell> row200;
  ArrayList<Cell> row300;

  ArrayList<Cell> row1NoNums;
  ArrayList<Cell> row2NoNums;
  ArrayList<Cell> row3NoNums;

  ArrayList<Cell> row1Nums;
  ArrayList<Cell> row2Nums;
  ArrayList<Cell> row3Nums;

  ArrayList<Cell> row1NoNums2ndVersion;
  ArrayList<Cell> row2NoNums2ndVersion;
  ArrayList<Cell> row3NoNums2ndVersion;

  ArrayList<Cell> row1Nums2ndVersion;
  ArrayList<Cell> row2Nums2ndVersion;
  ArrayList<Cell> row3Nums2ndVersion;

  ArrayList<Cell> row11;
  ArrayList<Cell> row22;
  ArrayList<Cell> row33;

  ArrayList<Cell> noNeighborRow1;
  ArrayList<Cell> noNeighborRow2;
  ArrayList<Cell> noNeighborRow3;

  ArrayList<ArrayList<Cell>> ClickedUnRevealedBoard;
  ArrayList<ArrayList<Cell>> unRevealedBoard;
  ArrayList<ArrayList<Cell>> board;
  ArrayList<ArrayList<Cell>> board00;

  ArrayList<ArrayList<Cell>> boardNoNums;
  ArrayList<ArrayList<Cell>> boardNums;

  ArrayList<ArrayList<Cell>> boardNoNums2ndVersion;
  ArrayList<ArrayList<Cell>> boardNums2ndVersion;

  ArrayList<ArrayList<Cell>> boardd;

  ArrayList<ArrayList<Cell>> boardNoNeighbors;

  ArrayList<Cell> unrevealed1;
  ArrayList<Cell> unrevealed2;
  ArrayList<Cell> unrevealed3;
  ArrayList<ArrayList<Cell>> unrevealedBoard;

  ArrayList<Cell> unrevealed1V2;
  ArrayList<Cell> unrevealed2V2;
  ArrayList<Cell> unrevealed3V2;
  ArrayList<ArrayList<Cell>> unrevealedBoardV2;

  ArrayList<Cell> unrevealed1V3;
  ArrayList<Cell> unrevealed2V3;
  ArrayList<Cell> unrevealed3V3;
  ArrayList<ArrayList<Cell>> unrevealedBoardV3;

  ArrayList<Cell> unrevealed1V4;
  ArrayList<Cell> unrevealed2V4;
  ArrayList<Cell> unrevealed3V4;
  ArrayList<ArrayList<Cell>> unrevealedBoardV4;



  WorldImage blankCell;
  WorldImage mine;
  WorldImage cell1;
  WorldImage cell2;
  WorldImage cell3;

  Cell testCell;

  CartPt cp1 = new CartPt(0, 0);
  CartPt cp2 = new CartPt(15, 15);
  CartPt cp3 = new CartPt(5, 5);
  CartPt cp4 = new CartPt(1, 0);
  CartPt cp5 = new CartPt(0, 1);
  CartPt cp6 = new CartPt(5, 5);

  Minesweeper gameBoard = new Minesweeper(25, 25, 50, new Random(3), false);



  //this.boardWithNoNeighbors.bigBang(1000, 1000, 1);
  void initialConditions() {

    // board w two stacked mines at bottom of middle row and nothing revealed
    unrevealed1V4 = new ArrayList<Cell>(Arrays.asList(new Cell(true, false),
        new Cell(false, true), new Cell(false, true)));
    unrevealed2V4 = new ArrayList<Cell>(Arrays.asList(new Cell(true, false), 
        new Cell(false, true), new Cell(false, true)));
    unrevealed3V4 = new ArrayList<Cell>(Arrays.asList(new Cell(false, false), 
        new Cell(false, true), new Cell(false, true)));
    unrevealedBoardV4 = new ArrayList<ArrayList<Cell>>(Arrays.asList(this.unrevealed1V4, 
        this.unrevealed2V4, this.unrevealed3V4));
    unrevealedGameV4 = new Minesweeper(this.unrevealedBoardV4);

    // board w two stacked mines at bottom of middle row and nothing revealed
    unrevealed1V3 = new ArrayList<Cell>(Arrays.asList(new Cell(true, false), 
        new Cell(false, false), new Cell(false, false)));
    unrevealed2V3 = new ArrayList<Cell>(Arrays.asList(new Cell(true, false), 
        new Cell(false, false), new Cell(false, false)));
    unrevealed3V3 = new ArrayList<Cell>(Arrays.asList(new Cell(false, false), 
        new Cell(false, false), new Cell(false, false)));
    unrevealedBoardV3 = new ArrayList<ArrayList<Cell>>(Arrays.asList(this.unrevealed1V3, 
        this.unrevealed2V3, this.unrevealed3V3));
    unrevealedGameV3 = new Minesweeper(this.unrevealedBoardV3);

    // board w revealed bottom cell
    unrevealed1V2 = new ArrayList<Cell>(Arrays.asList(new Cell(false, false), 
        new Cell(false, false), new Cell(false, false)));
    unrevealed2V2 = new ArrayList<Cell>(Arrays.asList(new Cell(false, false), 
        new Cell(true, false), new Cell(true, false)));
    unrevealed3V2 = new ArrayList<Cell>(Arrays.asList(new Cell(false, false), 
        new Cell(true, false), new Cell(false, true)));
    unrevealedBoardV2 = new ArrayList<ArrayList<Cell>>(Arrays.asList(this.unrevealed1V2, 
        this.unrevealed2V2, this.unrevealed3V2));
    unrevealedGameV2 = new Minesweeper(this.unrevealedBoardV2);

    // board with no revealed cells and 3 mines surrounding bottom right corner
    unrevealed1 = new ArrayList<Cell>(Arrays.asList(new Cell(false, false), 
        new Cell(false, false), new Cell(false, false)));
    unrevealed2 = new ArrayList<Cell>(Arrays.asList(new Cell(false, false), 
        new Cell(true, false), new Cell(true, false)));
    unrevealed3 = new ArrayList<Cell>(Arrays.asList(new Cell(false, false), 
        new Cell(true, false), new Cell(false, false)));
    unrevealedBoard = new ArrayList<ArrayList<Cell>>(Arrays.asList(this.unrevealed1, 
        this.unrevealed2, this.unrevealed3));
    unrevealedGame = new Minesweeper(this.unrevealedBoard);


    // unrevealed 3x3 board with mine in top left
    // cell( isBomb, isRevealed, isFlagged, numMines
    unRevealedRow1  = new ArrayList<Cell>(Arrays.asList(new Cell(true, false, false, 0),
        new Cell(false, false, false, 1), new Cell(false, false, false, 0)));
    unRevealedRow2  = new ArrayList<Cell>(Arrays.asList(new Cell(false, false, false, 1),
        new Cell(false, false, false, 1), new Cell(false, false, false, 0)));
    unRevealedRow3 = new ArrayList<Cell>(Arrays.asList(new Cell(false, false, false, 0),
        new Cell(false, false, false, 0), new Cell(false, false, false, 0)));
    unRevealedBoard = new ArrayList<ArrayList<Cell>>(Arrays.asList(unRevealedRow1, 
        unRevealedRow2, unRevealedRow3));
    unRevealedBoard2 = new Minesweeper(this.unRevealedBoard);

    // result of clicking the bottom right corner cell in the board above
    ClickedUnRevealedRow1  = new ArrayList<Cell>(Arrays.asList(new Cell(true, 
        false, false, 0),
        new Cell(false, true, false, 1), new Cell(false, true, false, 0)));
    ClickedUnRevealedRow1  = new ArrayList<Cell>(Arrays.asList(new Cell(false, 
        true, false, 1),
        new Cell(false, true, false, 1), new Cell(false, true, false, 0)));
    ClickedUnRevealedRow1 = new ArrayList<Cell>(Arrays.asList(new Cell(false, 
        true, false, 0),
        new Cell(false, true, false, 0), new Cell(false, true, false, 0)));
    ClickedUnRevealedBoard = new ArrayList<ArrayList<Cell>>(Arrays.asList(unRevealedRow1, 
        unRevealedRow2, unRevealedRow3));
    ClickedUnRevealedBoard2 = new Minesweeper(this.unRevealedBoard);

    // Cell(boolean isBomb, boolean isRevealed, boolean isFlagged, boolean numMines)
    // {

    // 3x3 mineboard
    row1 = new ArrayList<Cell>(
        Arrays.asList(new Cell(false, true), new Cell(true, true), new Cell(false, true)));
    row2 = new ArrayList<Cell>(
        Arrays.asList(new Cell(false, true), new Cell(false, true), new Cell(false, true)));
    row3 = new ArrayList<Cell>(
        Arrays.asList(new Cell(false, true), new Cell(false, true), new Cell(true, true)));
    board = new ArrayList<ArrayList<Cell>>(Arrays.asList(row1, row2, row3));
    board2 = new Minesweeper(this.board);

    // identical one of above
    row11 = new ArrayList<Cell>(
        Arrays.asList(new Cell(false, true), new Cell(true, true), new Cell(false, true)));
    row22 = new ArrayList<Cell>(
        Arrays.asList(new Cell(false, true), new Cell(false, true), new Cell(false, true)));
    row33 = new ArrayList<Cell>(
        Arrays.asList(new Cell(false, true), new Cell(false, true), new Cell(true, true)));
    boardd = new ArrayList<ArrayList<Cell>>(Arrays.asList(row11, row22, row33));
    board22 = new Minesweeper(this.boardd);

    // first board with a new mine at top left
    row1WithMineAt00 = new ArrayList<Cell>(Arrays.asList(new Cell(true, true, false, 0),
        new Cell(true, true, false, 0), new Cell(false, true, false, 1)));
    row200 = new ArrayList<Cell>(Arrays.asList(new Cell(false, true, false, 1),
        new Cell(false, true, false, 2), new Cell(false, true, false, 2)));
    row300 = new ArrayList<Cell>(Arrays.asList(new Cell(false, true, false, 0),
        new Cell(false, true, false, 1), new Cell(true, true, false, 0)));
    board00 = new ArrayList<ArrayList<Cell>>(
        Arrays.asList(this.row1WithMineAt00, this.row200, this.row300));
    boardWithMineAt00 = new Minesweeper(this.board00);

    // first board without any numbers
    row1NoNums = new ArrayList<Cell>(Arrays.asList(new Cell(false, true, false, 0),
        new Cell(true, true, false, 0), new Cell(false, true, false, 0)));
    row2NoNums = new ArrayList<Cell>(Arrays.asList(new Cell(false, true, false, 0),
        new Cell(false, true, false, 0), new Cell(false, true, false, 0)));
    row3NoNums = new ArrayList<Cell>(Arrays.asList(new Cell(false, true, false, 0),
        new Cell(true, true, false, 0), new Cell(true, true, false, 0)));
    boardNoNums = new ArrayList<ArrayList<Cell>>(
        Arrays.asList(this.row1NoNums, this.row2NoNums, this.row3NoNums));
    boardWithNoNums = new Minesweeper(this.boardNoNums, true);

    row1Nums = new ArrayList<Cell>(Arrays.asList(new Cell(false, true, false, 1),
        new Cell(true, true, false, 0), new Cell(false, true, false, 1)));
    row2Nums = new ArrayList<Cell>(Arrays.asList(new Cell(false, true, false, 2),
        new Cell(false, true, false, 3), new Cell(false, true, false, 3)));
    row3Nums = new ArrayList<Cell>(Arrays.asList(new Cell(false, true, false, 1),
        new Cell(true, true, false, 1), new Cell(true, true, false, 1)));
    boardNums = new ArrayList<ArrayList<Cell>>(
        Arrays.asList(this.row1Nums, this.row2Nums, this.row3Nums));
    boardWithNums = new Minesweeper(this.boardNums, true);

    row1NoNums2ndVersion = new ArrayList<Cell>(
        Arrays.asList(new Cell(false, true), new Cell(false, true), new Cell(false, true)));
    row2NoNums2ndVersion = new ArrayList<Cell>(
        Arrays.asList(new Cell(false, true), new Cell(false, true), new Cell(false, true)));
    row3NoNums2ndVersion = new ArrayList<Cell>(
        Arrays.asList(new Cell(false, true), new Cell(false, true), new Cell(false, true)));
    this.boardNoNums2ndVersion = new ArrayList<ArrayList<Cell>>(Arrays
        .asList(this.row1NoNums2ndVersion, this.row2NoNums2ndVersion, this.row3NoNums2ndVersion));
    this.boardWithNoNums2ndVersion = new Minesweeper(this.boardNoNums2ndVersion, true);

    row1Nums2ndVersion = new ArrayList<Cell>(Arrays.asList(new Cell(false, true, false, 1),
        new Cell(true, true, false, 0), new Cell(false, true, false, 1)));
    row2Nums2ndVersion = new ArrayList<Cell>(Arrays.asList(new Cell(false, true, false, 2),
        new Cell(false, true, false, 2), new Cell(false, true, false, 2)));
    row3Nums2ndVersion = new ArrayList<Cell>(Arrays.asList(new Cell(false, true, false, 1),
        new Cell(true, true, false, 0), new Cell(false, true, false, 1)));
    this.boardNums2ndVersion = new ArrayList<ArrayList<Cell>>(
        Arrays.asList(this.row1Nums2ndVersion, this.row2Nums2ndVersion, this.row3Nums2ndVersion));
    this.boardWithNums2ndVersion = new Minesweeper(this.boardNums2ndVersion);

    noNeighborRow1 = new ArrayList<Cell>(
        Arrays.asList(new Cell(false, true), new Cell(true, true), new Cell(false, true)));
    noNeighborRow2 = new ArrayList<Cell>(
        Arrays.asList(new Cell(false, true), new Cell(false, true), new Cell(false, true)));
    noNeighborRow3 = new ArrayList<Cell>(
        Arrays.asList(new Cell(false, true), new Cell(false, true), new Cell(true, true)));
    boardNoNeighbors = new ArrayList<ArrayList<Cell>>(
        Arrays.asList(this.noNeighborRow1, this.noNeighborRow2, this.noNeighborRow3));
    boardWithNoNeighbors = new Minesweeper(this.boardNoNeighbors, false);

    board1 = new Minesweeper(3, 3, 2, new Random(3), true);

    testCell = new Cell();
  }

  // conditions for mineCount
  void mineCountConditions() {
    row1NoNums2ndVersion = new ArrayList<Cell>(Arrays.asList(new Cell(false, true, false, 0),
        new Cell(true, true, false, 0), new Cell(false, true, false, 0)));
    row2NoNums2ndVersion = new ArrayList<Cell>(Arrays.asList(new Cell(true, true, false, 0),
        new Cell(false, true, false, 0), new Cell(false, true, false, 0)));
    row3NoNums2ndVersion = new ArrayList<Cell>(Arrays.asList(new Cell(false, true, false, 0),
        new Cell(true, true, false, 0), new Cell(false, true, false, 0)));
    this.boardNoNums2ndVersion = new ArrayList<ArrayList<Cell>>(Arrays
        .asList(this.row1NoNums2ndVersion, this.row2NoNums2ndVersion, this.row3NoNums2ndVersion));
    this.boardWithNoNums2ndVersion = new Minesweeper(this.boardNoNums2ndVersion, true);

    row1Nums2ndVersion = new ArrayList<Cell>(Arrays.asList(new Cell(false, true, false, 2),
        new Cell(true, true, false, 0), new Cell(false, true, false, 1)));
    row2Nums2ndVersion = new ArrayList<Cell>(Arrays.asList(new Cell(true, true, false, 2),
        new Cell(false, true, false, 3), new Cell(false, true, false, 2)));
    row3Nums2ndVersion = new ArrayList<Cell>(Arrays.asList(new Cell(false, true, false, 2),
        new Cell(true, true, false, 0), new Cell(false, true, false, 1)));
    this.boardNums2ndVersion = new ArrayList<ArrayList<Cell>>(
        Arrays.asList(this.row1Nums2ndVersion, this.row2Nums2ndVersion, this.row3Nums2ndVersion));
    this.boardWithNums2ndVersion = new Minesweeper(this.boardNums2ndVersion);
  }

  // conditions for setToBomb
  void setToBombConditions() {
    row1WithMineAt00 = new ArrayList<Cell>(Arrays.asList(new Cell(true, true, false, 0),
        new Cell(true, true, false, 0), new Cell(false, true, false, 1)));
    row200 = new ArrayList<Cell>(Arrays.asList(new Cell(false, true, false, 1),
        new Cell(false, true, false, 2), new Cell(false, true, false, 2)));
    row300 = new ArrayList<Cell>(Arrays.asList(new Cell(false, true, false, 0),
        new Cell(false, true, false, 1), new Cell(true, true, false, 0)));
    board00 = new ArrayList<ArrayList<Cell>>(
        Arrays.asList(this.row1WithMineAt00, this.row200, this.row300));
    boardWithMineAt00 = new Minesweeper(this.board00);
  }

  // doesn't set neighbors
  void setAddNeighborsConditions() {
    board2 = new Minesweeper(this.board, false);
    board22 = new Minesweeper(this.boardd, false);
  }

  void drawConditions() {
    blankCell = new OverlayImage(new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK),
        (new RectangleImage(20, 20, OutlineMode.SOLID, Color.GRAY)));
    mine = new OverlayImage(new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK),
        (new RectangleImage(20, 20, OutlineMode.SOLID, Color.RED)));
    cell1 = new OverlayImage(new TextImage("1", 13, Color.BLACK),
        new OverlayImage(new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK),
            (new RectangleImage(20, 20, OutlineMode.SOLID, Color.GRAY))));
    cell2 = new OverlayImage(new TextImage("2", 13, Color.BLACK),
        new OverlayImage(new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK),
            (new RectangleImage(20, 20, OutlineMode.SOLID, Color.GRAY))));
    cell3 = new OverlayImage(new TextImage("3", 13, Color.BLACK),
        new OverlayImage(new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK),
            (new RectangleImage(20, 20, OutlineMode.SOLID, Color.GRAY))));
  }

  void testStuff(Tester t) {
    this.initialConditions();

    // checking making board with the two different constructors
    t.checkExpect(this.board1, this.board2);
    t.checkExpect(this.board1.board, this.board2.board);
    this.gameBoard.bigBang(1000, 1000, 0.001);


  }

  // testing mineList method
  void testMineList(Tester t) {
    this.initialConditions();

    // checking mineList
    t.checkExpect(new Utils().mineList(3, 3, 2, new Random(3)),
        new ArrayList<CartPt>(Arrays.asList(new CartPt(2, 2), new CartPt(0, 1))));
    t.checkExpect(new Utils().mineList(3, 3, 2, new Random(0)),
        new ArrayList<CartPt>(Arrays.asList(new CartPt(0, 1), new CartPt(1, 2))));
    // checking w no mines
    t.checkExpect(new Utils().mineList(3, 3, 0, new Random(3)),
        new ArrayList<CartPt>(Arrays.asList()));
  }

  // testing alreadyIn method
  void testAlreadyIn(Tester t) {
    this.initialConditions();

    t.checkExpect(new Utils().alreadyIn(
        new ArrayList<CartPt>(Arrays.asList(new CartPt(1, 1), new CartPt(4, 7))), new CartPt(1, 1)),
        true);
    t.checkExpect(new Utils().alreadyIn(
        new ArrayList<CartPt>(Arrays.asList(new CartPt(1, 1), new CartPt(4, 7))), new CartPt(1, 2)),
        false);
    t.checkExpect(new Utils().alreadyIn(
        new ArrayList<CartPt>(Arrays.asList(new CartPt(1, 1), new CartPt(4, 7))), new CartPt(4, 7)),
        true);
    t.checkExpect(new Utils().alreadyIn(
        new ArrayList<CartPt>(Arrays.asList(new CartPt(1, 1), new CartPt(4, 7), new CartPt(5, 3))),
        new CartPt(5, 3)), true);
    t.checkExpect(new Utils().alreadyIn(new ArrayList<CartPt>(), new CartPt(4, 7)), false);

  }

  // testing adding mines to board
  void testAddMines(Tester t) {
    this.initialConditions();

    // can use addMineCount to board beacause it has been tested
    // testing adding single mine
    new Utils().addMines(this.board2.board, new ArrayList<CartPt>(Arrays.asList(new CartPt(0, 0))));
    new Utils().addMineCountToBoard(this.board2.board);
    t.checkExpect(this.board2, this.boardWithMineAt00);

    // testing adding 2 mines
    new Utils().addMines(this.boardWithNoNums2ndVersion.board,
        new ArrayList<CartPt>(Arrays.asList(new CartPt(0, 1), new CartPt(2, 1))));
    new Utils().addMineCountToBoard(this.boardWithNoNums2ndVersion.board);
    t.checkExpect(this.boardWithNoNums2ndVersion, this.boardWithNums2ndVersion);

  }

  // testing adding mine count to board
  void testMineCountToBoard(Tester t) {
    this.initialConditions();
    this.mineCountConditions();

    new Utils().addMineCountToBoard(this.boardWithNoNums.board);
    // new Utils().addMineCountToBoard(this.boardWithNums.board);
    t.checkExpect(this.boardWithNoNums, this.boardWithNums);

    new Utils().addMineCountToBoard(this.boardWithNoNums2ndVersion.board);
    t.checkExpect(this.boardWithNoNums2ndVersion, this.boardWithNums2ndVersion);

  }

  // testing the possible neighbor method
  void testPossibleNeighbors(Tester t) {
    ArrayList<CartPt> possibleNeighbors12 = new ArrayList<CartPt>(
        Arrays.asList(new CartPt(1, 3), new CartPt(1, 1), new CartPt(2, 2), new CartPt(2, 3),
            new CartPt(2, 1), new CartPt(0, 2), new CartPt(0, 3), new CartPt(0, 1)));
    t.checkExpect(new Utils().possibleNeighbors(1, 2), possibleNeighbors12);

    ArrayList<CartPt> possibleNeighbors00 = new ArrayList<CartPt>(
        Arrays.asList(new CartPt(0, 1), new CartPt(0, -1), new CartPt(1, 0), new CartPt(1, 1),
            new CartPt(1, -1), new CartPt(-1, 0), new CartPt(-1, 1), new CartPt(-1, -1)));
    t.checkExpect(new Utils().possibleNeighbors(0, 0), possibleNeighbors00);
  }

  // void addCellNeighbors(int i, int j, int rowSize, ArrayList<ArrayList<Cell>>
  // board)

  void testAddNeighborsToRow(Tester t) {
    this.initialConditions();
    this.setAddNeighborsConditions();

    // setting top row
    new Utils().addNeighborsToRow(this.board2.board, this.board2.board.get(0), 0);
    board22.board.get(0).get(0).addCellNeighbors(0, 0, 3, this.board22.board);
    board22.board.get(0).get(1).addCellNeighbors(0, 1, 3, this.board22.board);
    board22.board.get(0).get(2).addCellNeighbors(0, 2, 3, this.board22.board);
    t.checkExpect(this.board2.board.get(0), this.board22.board.get(0));

    // setting middle row
    this.initialConditions();
    this.setAddNeighborsConditions();
    new Utils().addNeighborsToRow(this.board2.board, this.board2.board.get(1), 1);
    board22.board.get(1).get(0).addCellNeighbors(1, 0, 3, this.board22.board);
    board22.board.get(1).get(1).addCellNeighbors(1, 1, 3, this.board22.board);
    board22.board.get(1).get(2).addCellNeighbors(1, 2, 3, this.board22.board);
    t.checkExpect(this.board2.board.get(1), this.board22.board.get(1));

    // setting bottom row
    this.initialConditions();
    this.setAddNeighborsConditions();
    new Utils().addNeighborsToRow(this.board2.board, this.board2.board.get(2), 2);
    board22.board.get(2).get(0).addCellNeighbors(2, 0, 3, this.board22.board);
    board22.board.get(2).get(1).addCellNeighbors(2, 1, 3, this.board22.board);
    board22.board.get(2).get(2).addCellNeighbors(2, 2, 3, this.board22.board);
    t.checkExpect(this.board2.board.get(2), this.board22.board.get(2));

  }

  void testAddNeighborsToBoard(Tester t) {
    this.initialConditions();
    this.setAddNeighborsConditions();

    new Utils().addNeighborsToBoard(this.board2.board);
    new Utils().addNeighborsToRow(this.board22.board, this.board22.board.get(0), 0);
    new Utils().addNeighborsToRow(this.board22.board, this.board22.board.get(1), 1);
    new Utils().addNeighborsToRow(this.board22.board, this.board22.board.get(2), 2);
    t.checkExpect(this.board2.board, this.board22.board);
  }

  // have to use makeScene to test draw
  void testDrawBoard(Tester t) {
    //////////////////////////////////////////////////
    //// COMMENTED OUT THESE CHECKEXPECTS BECAUSE WE//
    //// TESTED THESE BEFORE ADDING THE END         //
    //// GAME FUNCTIONALITY                         //
    //////////////////////////////////////////////////
    this.initialConditions();
    this.drawConditions();
    WorldScene testBoard2 = new WorldScene(1000, 1000);
    // adding every cell to board2 :| :|
    testBoard2.placeImageXY(this.cell1, 100, 100);
    testBoard2.placeImageXY(this.mine, 120, 100);
    testBoard2.placeImageXY(this.cell1, 140, 100);
    testBoard2.placeImageXY(this.cell1, 100, 120);
    testBoard2.placeImageXY(this.cell2, 120, 120);
    testBoard2.placeImageXY(this.cell2, 140, 120);
    testBoard2.placeImageXY(this.blankCell, 100, 140);
    testBoard2.placeImageXY(this.cell1, 120, 140);
    testBoard2.placeImageXY(this.mine, 140, 140);
    //t.checkExpect(new Utils().drawBoard(this.board2.board, 
    //new WorldScene(1000, 1000), false), testBoard2);

    WorldScene testBoard00 = new WorldScene(1000, 1000);
    // adding every cell to board2 :| :|
    testBoard00.placeImageXY(this.mine, 100, 100);
    testBoard00.placeImageXY(this.mine, 120, 100);
    testBoard00.placeImageXY(this.cell1, 140, 100);
    testBoard00.placeImageXY(this.cell2, 100, 120);
    testBoard00.placeImageXY(this.cell3, 120, 120);
    testBoard00.placeImageXY(this.cell2, 140, 120);
    testBoard00.placeImageXY(this.blankCell, 100, 140);
    testBoard00.placeImageXY(this.cell1, 120, 140);
    testBoard00.placeImageXY(this.mine, 140, 140);

    //t.checkExpect(new Utils().drawBoard(this.boardWithMineAt00.board,
    //new WorldScene(1000, 1000)),
    //    testBoard00);
    //this.boardWithMineAt00.bigBang(1000, 1000, 1);
  }

  // testing the flagging method
  void testFlagCell(Tester t) {
    this.initialConditions();

    // checking the cell is not flagged
    t.checkExpect(this.board2.board.get(0).get(0).isFlagged, false);
    // flagging the cell
    new Utils().flagCell(new Posn(105, 105), this.board2.board);
    // checking the cell is flagged
    t.checkExpect(this.board2.board.get(0).get(0).isFlagged, true);
    // unflagging the cell
    new Utils().flagCell(new Posn(105, 105), this.board2.board);
    // checking the cell is unflagged
    t.checkExpect(this.board2.board.get(0).get(0).isFlagged, false);

  }

  void testSetToBomb(Tester t) {
    this.initialConditions();

    // * testing on a cell not in a board
    t.checkExpect(this.testCell.isBomb, false);
    this.testCell.setToBomb();
    t.checkExpect(this.testCell.isBomb, true);

    // * testing on cells in boards

    // checking the cell is not a bomb
    t.checkExpect(this.board2.board.get(0).get(0).isBomb, false);
    // setting the cell to a bomb
    this.board2.board.get(0).get(0).setToBomb();
    // checking that it is now a bomb
    t.checkExpect(this.board2.board.get(0).get(0).isBomb, true);

    // checking the cell is not a bomb
    t.checkExpect(this.board2.board.get(1).get(2).isBomb, false);
    // setting the cell to a bomb
    this.board2.board.get(1).get(2).setToBomb();
    // checking that it is now a bomb
    t.checkExpect(this.board2.board.get(1).get(2).isBomb, true);

  }

  void testSetToOppositeFlagged(Tester t) {
    this.initialConditions();

    // * testing on a cell not in a board
    t.checkExpect(this.testCell.isFlagged, false);
    this.testCell.setToOppositeFlagged();
    t.checkExpect(this.testCell.isFlagged, true);

    // checking the cell is not flagged
    t.checkExpect(this.board2.board.get(0).get(0).isFlagged, false);
    // setting the cell to flagged
    this.board2.board.get(0).get(0).setToOppositeFlagged();
    // checking that it is now flagged
    t.checkExpect(this.board2.board.get(0).get(0).isFlagged, true);
    // setting the cell to unflagged
    this.board2.board.get(0).get(0).setToOppositeFlagged();
    // checking is unflagged
    t.checkExpect(this.board2.board.get(0).get(0).isFlagged, false);

    // checking the cell is not flagged
    t.checkExpect(this.board2.board.get(1).get(2).isFlagged, false);
    // setting the cell to flagged
    this.board2.board.get(1).get(2).setToOppositeFlagged();
    // checking that it is now flagged
    t.checkExpect(this.board2.board.get(1).get(2).isFlagged, true);
    // setting the cell to unflagged
    this.board2.board.get(1).get(2).setToOppositeFlagged();
    // checking is unflagged
    t.checkExpect(this.board2.board.get(1).get(2).isFlagged, false);
  }

  // testing bombAdd
  void testBombAdd(Tester t) {
    this.initialConditions();

    // cell isn't bomb so it returns 0
    t.checkExpect(this.testCell.bombAdd(), 0);
    // setting cell to bomb
    this.testCell.isBomb = true;
    // cell is bomb so it returns 1
    t.checkExpect(this.testCell.bombAdd(), 1);

    // testing on a non bomb cell
    t.checkExpect(this.board2.board.get(0).get(0).bombAdd(), 0);
    // testing on a bomb cell
    t.checkExpect(this.board2.board.get(0).get(1).bombAdd(), 1);

  }

  // testing adding cell neighbors
  void testAddCellNeighbors(Tester t) {
    this.initialConditions();

    // making sure neighbor list is empty
    t.checkExpect(this.boardWithNoNeighbors.board.get(0).get(0).neighbors, new ArrayList<Cell>());
    this.boardWithNoNeighbors.board.get(0).get(0).addCellNeighbors(0, 0, 3,
        this.boardWithNoNeighbors.board);
    ArrayList<Cell> neighbors = new ArrayList<Cell>(
        Arrays.asList(this.boardWithNoNeighbors.board.get(0).get(1),
            this.boardWithNoNeighbors.board.get(1).get(1),
            this.boardWithNoNeighbors.board.get(1).get(0)));
    t.checkExpect(this.boardWithNoNeighbors.board.get(0).get(0).neighbors, neighbors);

    // testing on another cell
    this.boardWithNoNeighbors.board.get(1).get(2).addCellNeighbors(1, 2, 3,
        this.boardWithNoNeighbors.board);
    neighbors = new ArrayList<Cell>(Arrays.asList(this.boardWithNoNeighbors.board.get(0).get(2),
        this.boardWithNoNeighbors.board.get(0).get(1),
        this.boardWithNoNeighbors.board.get(1).get(1),
        this.boardWithNoNeighbors.board.get(2).get(1),
        this.boardWithNoNeighbors.board.get(2).get(2)));
    t.checkExpect(this.boardWithNoNeighbors.board.get(1).get(2).neighbors, neighbors);

  }

  // testing mineCounter method
  void testMineCounter(Tester t) {
    this.initialConditions();

    // we haven't had the cells add their mine counts so we expect 0
    t.checkExpect(this.boardWithNoNums.board.get(0).get(0).numMines, 0);
    // adding the mine count to top left cell
    this.boardWithNoNums.board.get(0).get(0).mineCounter();
    // now should show that 1 mine is near it
    t.checkExpect(this.boardWithNoNums.board.get(0).get(0).numMines, 1);

    // testing it on another cell
    t.checkExpect(this.boardWithNoNums.board.get(1).get(2).numMines, 0);
    // adding the mine count to top left cell
    this.boardWithNoNums.board.get(1).get(2).mineCounter();
    // now should show that 1 mine is near it
    t.checkExpect(this.boardWithNoNums.board.get(1).get(2).numMines, 3);

    // cell with no neighbors
    this.testCell.mineCounter();
    t.checkExpect(this.testCell.numMines, 0);

  }

  // testing makeScene
  void testMakeScene(Tester t) {
    this.initialConditions();
    this.drawConditions();

    WorldScene testBoard2 = new WorldScene(0, 0);

    // adding every cell to board2 :| :|
    testBoard2.placeImageXY(this.cell1, 100, 100);
    testBoard2.placeImageXY(this.mine, 120, 100);
    testBoard2.placeImageXY(this.cell1, 140, 100);
    testBoard2.placeImageXY(this.cell1, 100, 120);
    testBoard2.placeImageXY(this.cell2, 120, 120);
    testBoard2.placeImageXY(this.cell2, 140, 120);
    testBoard2.placeImageXY(this.blankCell, 100, 140);
    testBoard2.placeImageXY(this.cell1, 120, 140);
    testBoard2.placeImageXY(this.mine, 140, 140);
    t.checkExpect(this.board2.makeScene(), testBoard2);

    WorldScene testBoard00 = new WorldScene(0, 0);

    // adding every cell to board2 :| :|
    testBoard00.placeImageXY(this.mine, 100, 100);
    testBoard00.placeImageXY(this.mine, 120, 100);
    testBoard00.placeImageXY(this.cell1, 140, 100);
    testBoard00.placeImageXY(this.cell2, 100, 120);
    testBoard00.placeImageXY(this.cell3, 120, 120);
    testBoard00.placeImageXY(this.cell2, 140, 120);
    testBoard00.placeImageXY(this.blankCell, 100, 140);
    testBoard00.placeImageXY(this.cell1, 120, 140);
    testBoard00.placeImageXY(this.mine, 140, 140);

  }

  //tests sameCartPt
  void testSameCartPt(Tester t) {
    t.checkExpect(this.cp1.sameCartPt(this.cp1), true);
    t.checkExpect(this.cp1.sameCartPt(this.cp2), false);
    t.checkExpect(this.cp4.sameCartPt(this.cp5), false);
    t.checkExpect(this.cp3.sameCartPt(this.cp6), true);
    t.checkExpect(this.cp6.sameCartPt(this.cp3), true);
  }

  // tests is ValidCartPt
  void testIsValidCartPt(Tester t) {
    t.checkExpect(this.cp1.isValidCartPt(10, 10), true);
    t.checkExpect(this.cp1.isValidCartPt(4, 8), true);
    t.checkExpect(this.cp2.isValidCartPt(10, 10), false);
    t.checkExpect(this.cp3.isValidCartPt(4, 2), false);
    t.checkExpect(this.cp2.isValidCartPt(10, 20), false);
    t.checkExpect(this.cp6.isValidCartPt(5, 5), false);
  }

  // tests addSingleMine
  void testAddSingleMine(Tester t) {
    this.initialConditions();

    // checking on different cartpt
    t.checkExpect(this.board2.board.get(0).get(0).isBomb, false);
    new CartPt(0, 0).addSingleMine(this.board2.board);
    t.checkExpect(this.board2.board.get(0).get(0).isBomb, true);

    t.checkExpect(this.board2.board.get(1).get(2).isBomb, false);
    new CartPt(1, 2).addSingleMine(this.board2.board);
    t.checkExpect(this.board2.board.get(1).get(2).isBomb, true);

    t.checkExpect(this.board2.board.get(0).get(1).isBomb, true);
    new CartPt(1, 2).addSingleMine(this.board2.board);
    t.checkExpect(this.board2.board.get(0).get(1).isBomb, true);
  }

  // testing findCell
  void testFindCell(Tester t) {
    this.initialConditions();

    t.checkExpect(new CartPt(0, 0).findCell(this.board2.board), this.board2.board.get(0).get(0));
    t.checkExpect(new CartPt(1, 1).findCell(this.board2.board), this.board2.board.get(1).get(1));
    t.checkExpect(new CartPt(2, 2).findCell(this.board2.board), this.board2.board.get(2).get(2));
    t.checkExpect(new CartPt(2, 0).findCell(this.board2.board), this.board2.board.get(2).get(0));
  }


  void testLeftClick(Tester t) {
    // making a cell for testing that is bomb that is not revealed or flagged
    Cell c1 = new Cell(true, false, false,  0);

    //making another cell to put as neighbor
    Cell c2 = new Cell();
    c2.neighbors.add(c1);
    // checking for exception when clicking
    //t.checkException(new RuntimeException("clicked a bomb"), c1, "leftClick");
    // setting it to not be a bomb and confirming it's not revealed
    c1.isBomb = false;
    t.checkExpect(c1.isRevealed, false);
    // clicking should reveal it
    c1.leftClick();
    t.checkExpect(c1.isRevealed, true);
    // setting it to flagged
    c1.isRevealed = false;
    c1.isFlagged = true;
    // clicking it shouldn't change anything
    c1.leftClick();
    t.checkExpect(c1.isRevealed, false);
    t.checkExpect(c1.isFlagged, true);

    // checking to make sure that a clicked cell passes the flood cell 
    // method to its neighbor list
    // c2 has c1 as a neighbor and vice versa
    c1.isFlagged = false;
    c1.neighbors.add(c2);
    c2.leftClick();
    t.checkExpect(c1.isRevealed, true);

    // c2 is revealed so shouldn't spread through neighbor list
    c1.isRevealed = false;
    c2.leftClick();
    t.checkExpect(c1.isRevealed, false);

    // c2 is flagged so shouldn't spread
    c2.isRevealed = false;
    c2.isFlagged = true;
    c2.leftClick();
    t.checkExpect(c1.isRevealed, false);
  }

  // tests flood fill
  void testFloodFill(Tester t) {
    this.initialConditions();
    //this.unrevealedGameV4.bigBang(1000, 1000, 1);

    // testing when a cell fills
    this.unRevealedBoard2.board.get(2).get(2).leftClick();
    t.checkExpect(this.unRevealedBoard2.board, this.ClickedUnRevealedBoard2.board);

    // testing when a cell doesn't fill
    this.unrevealedGame.board.get(2).get(2).leftClick();
    t.checkExpect(this.unrevealedGame.board, this.unrevealedGameV2.board);

    // testing when it floods to one cell then stops
    this.unrevealedGameV3.board.get(2).get(2).leftClick();
    t.checkExpect(this.unrevealedGameV3.board, this.unrevealedGameV4.board);
  }

  // tests mine Visible
  void testMineVisible(Tester t ) {
    this.initialConditions();
    t.checkExpect(new Utils().mineVisible(this.ClickedUnRevealedBoard), false);
    t.checkExpect(new Utils().mineVisible(this.board), true);
    t.checkExpect(new Utils().mineVisible(this.unRevealedBoard), false);
    t.checkExpect(new Utils().mineVisible(this.board00), true);


  }

}