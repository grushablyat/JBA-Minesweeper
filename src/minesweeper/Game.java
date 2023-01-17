package minesweeper;

import java.util.*;

/**
 *Class of the Minesweeper game.
 */
public class Game {

    /**
     * Values for the field.
     */
    private interface Sign {
        char getSign();
    }

    private enum DefaultSign implements Sign {
        DEFAULT_SIGN('.');

        final char sign;

        DefaultSign(char sign) {
            this.sign = sign;
        }

        @Override
        public char getSign() {
            return sign;
        }
    }

    private enum NumberSign implements Sign {
        ONE('1'),
        TWO('2'),
        THREE('3'),
        FOUR('4'),
        FIVE('5'),
        SIX('6'),
        SEVEN('7'),
        EIGHT('8');

        final char sign;

        NumberSign(char sign) {
            this.sign = sign;
        }

        @Override
        public char getSign() {
            return sign;
        }
    }

    private enum ExploredSign implements Sign {
        SAFE_EXPLORED('/'),
        MINE_EXPLORED('X');

        final char sign;

        ExploredSign(char sign) {
            this.sign = sign;
        }

        @Override
        public char getSign() {
            return sign;
        }
    }

    private enum UnexploredSign implements Sign {
//        SAFE_UNEXPLORED('.'),
        MINE_UNEXPLORED('.'),
        SAFE_MARKED('*'),
        MINE_MARKED('*');

        final char sign;

        UnexploredSign(char sign) {
            this.sign = sign;
        }

        @Override
        public char getSign() {
            return sign;
        }
    }

    private enum Action {
        FREE("free"),
        MINE("mine");

        final String action;

        Action(String action) {
            this.action = action;
        }
    }

    /**
     * Map for converting int to NumberSign (number).
     */
    private final static Map<Integer, NumberSign> numberDictionary;

    /**
     * Map for converting String to Action
     */
    private final static Map<String, Action> actionDictionary;

    // Initializing previous maps
    static {
        numberDictionary = new HashMap<>();
        numberDictionary.put(1, NumberSign.ONE);
        numberDictionary.put(2, NumberSign.TWO);
        numberDictionary.put(3, NumberSign.THREE);
        numberDictionary.put(4, NumberSign.FOUR);
        numberDictionary.put(5, NumberSign.FIVE);
        numberDictionary.put(6, NumberSign.SIX);
        numberDictionary.put(7, NumberSign.SEVEN);
        numberDictionary.put(8, NumberSign.EIGHT);
        actionDictionary = new HashMap<>();
        actionDictionary.put("free", Action.FREE);
        actionDictionary.put("mine", Action.MINE);
    }

    /**
     * Height and width of the field.
     */
    private final int size;

    /**
     * A number of mines on the field.
     */
    private final int mines;
    private int markedMines = 0;
    private int markedSafeSquares = 0;

    private int exploredSafeSquares = 0;

    /**
     * Multidimensional array for the field.
     */
    private final Sign[][] field;

    /**
     * Default ctor.
     */
    public Game() {
        System.out.print("How many mines do you want on the field? > ");
        mines = new Scanner(System.in).nextInt();
        size = 9;
        field = new Sign[size][size];

        for (int i = 0; i < size; i++) {
            Arrays.fill(field[i], 0, size, DefaultSign.DEFAULT_SIGN);
        }
    }

    /**
     * The process of a game.
     */
    public void play() {
        System.out.println(this);

        // Make sure first shot is safe
        System.out.print("Set/unset mines marks or claim a cell as free: > ");
        Scanner in = new Scanner(System.in);
        int y = in.nextInt() - 1;
        int x = in.nextInt() - 1;
        Action action = actionDictionary.get(in.next());

        setSafeDistance(x, y);
        setMines();
        clearStartingPosition();
        shot(x, y, action);

        // The rest game
        while (!(exploredSafeSquares == Math.pow(size, 2) - mines || markedMines == mines && markedSafeSquares == 0)) {
            System.out.println("\n" + this);
            System.out.print("Set/unset mines marks or claim a cell as free: > ");
            y = in.nextInt() - 1;
            x = in.nextInt() - 1;
            action = actionDictionary.get(in.next());

            if (!shot(x, y, action)) {
                gameOver();
                return;
            }
        }
        System.out.println(this);
        System.out.println("Congratulations! You found all the mines!");
    }

    /**
     * Setting the first shot square and around eight SAFE_UNEXPLORED to prevent first shot square to be a Mine or a Number.
     * @param x Y coordinate ( i )
     * @param y X coordinate ( j )
     */
    private void setSafeDistance(int x, int y) {
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if (i >= 0 && i < size && j >= 0 && j < size) {
                    field[i][j] = ExploredSign.SAFE_EXPLORED;
                }
            }
        }
    }

    /**
     * Setting mines of the field (cannot set a mine on first shot square or around).
     */
    private void setMines() {
        Random rand = new Random();
        int x, y;
        for (int i = 0; i < mines; i++) {
            x = rand.nextInt(size);
            y = rand.nextInt(size);
            if (!UnexploredSign.MINE_UNEXPLORED.equals(field[x][y]) && !ExploredSign.SAFE_EXPLORED.equals(field[x][y])) {
                field[x][y] = UnexploredSign.MINE_UNEXPLORED;
            } else {
                i--;
            }
        }
    }

    /**
     * Clearing starting position - unsetting explored squares.
     */
    private void clearStartingPosition() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (ExploredSign.SAFE_EXPLORED.equals(field[i][j])) {
                    field[i][j] = DefaultSign.DEFAULT_SIGN;
                }
            }
        }
    }

    /**
     * Shooting.
     * If mine - Game Over.
     * Summing mines around and setting a number or creating a "lake".
     * @param x Y coordinate ( i )
     * @param y X coordinate ( j )
     * @param action Action "free" of "mine"
     * @return true if the shot is fine, false - if GG
     */
    private boolean shot(int x, int y, Action action) {
        switch (action) {
            case MINE -> {
                if (DefaultSign.DEFAULT_SIGN.equals(field[x][y])) {
                    field[x][y] = UnexploredSign.SAFE_MARKED;
                    markedSafeSquares++;
                } else if (UnexploredSign.SAFE_MARKED.equals(field[x][y])) {
                    field[x][y] = DefaultSign.DEFAULT_SIGN;
                    markedSafeSquares--;
                } else if (UnexploredSign.MINE_UNEXPLORED.equals(field[x][y])) {
                    field[x][y] = UnexploredSign.MINE_MARKED;
                    markedMines++;
                } else if (UnexploredSign.MINE_MARKED.equals(field[x][y])) {
                    field[x][y] = UnexploredSign.MINE_UNEXPLORED;
                    markedMines--;
                } else if (ExploredSign.SAFE_EXPLORED.equals(field[x][y])) {
                    System.out.println("This is a safe square!");
                } else if (NumberSign.ONE.equals(field[x][y])
                        || NumberSign.TWO.equals(field[x][y])
                        || NumberSign.THREE.equals(field[x][y])
                        || NumberSign.FOUR.equals(field[x][y])
                        || NumberSign.FIVE.equals(field[x][y])
                        || NumberSign.SIX.equals(field[x][y])
                        || NumberSign.SEVEN.equals(field[x][y])
                        || NumberSign.EIGHT.equals(field[x][y])) {
                    System.out.println("There is a number here!");
                } else {
                    System.out.println("ERROR: Non-existing sign, shot-mine");
                }
            }
            case FREE -> {
                if (UnexploredSign.MINE_MARKED.equals(field[x][y])
                 || UnexploredSign.SAFE_MARKED.equals(field[x][y])) {
                    System.out.println("This is marked square");
                } else if (UnexploredSign.MINE_UNEXPLORED.equals(field[x][y])) {
                    return false; // GG
                } else if (ExploredSign.SAFE_EXPLORED.equals(field[x][y])) {
                    System.out.println("This is a safe square!");
                } else if (NumberSign.ONE.equals(field[x][y])
                        || NumberSign.TWO.equals(field[x][y])
                        || NumberSign.THREE.equals(field[x][y])
                        || NumberSign.FOUR.equals(field[x][y])
                        || NumberSign.FIVE.equals(field[x][y])
                        || NumberSign.SIX.equals(field[x][y])
                        || NumberSign.SEVEN.equals(field[x][y])
                        || NumberSign.EIGHT.equals(field[x][y])) {
                    System.out.println("There is a number here!");
                } else if (DefaultSign.DEFAULT_SIGN.equals(field[x][y])) {
                    setAround(x, y);
                } else {
                    System.out.println("ERROR: Non-existing sign, shot-free");
                }
            }
        }
        return true;
    }

    /**
     * Creating a "lake", recursive.
     * @param x Y coordinate ( i )
     * @param y X coordinate ( j )
     */
    private void setAround(int x, int y) {
        if (!DefaultSign.DEFAULT_SIGN.equals(field[x][y]) && !UnexploredSign.SAFE_MARKED.equals(field[x][y])) {
            return;
        }
        exploredSafeSquares++;
        int sum = 0;
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if (i >= 0 && i < size && j >= 0 &&j < size) {
                    if (UnexploredSign.MINE_UNEXPLORED.equals(field[i][j]) // field[x][y] == DefaultSign.DEFAULT_SIGN
                            || UnexploredSign.MINE_MARKED.equals(field[i][j])) {
                        sum++;
                    }
                }
            }
        }
        if (sum > 0 && sum < 9) {
            field[x][y] = numberDictionary.get(sum);
        } else if (sum == 0) {
            field[x][y] = ExploredSign.SAFE_EXPLORED;

            for (int i = x - 1; i <= x + 1; i++) {
                for (int j = y - 1; j <= y + 1; j++) {
                    if (i >= 0 && i < size && j >= 0 && j < size) {
                        setAround(i, j);
                    }
                }
            }
        } else {
            System.out.println("ERROR: Sum, setAround");
        }
    }

    /**
     * The end of the game.
     */
    private void gameOver() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (UnexploredSign.MINE_UNEXPLORED.equals(field[i][j]) || UnexploredSign.MINE_MARKED.equals(field[i][j])) {
                    field[i][j] = ExploredSign.MINE_EXPLORED;
                }
            }
        }
        System.out.println(this);
        System.out.println("You stepped on a mine and failed!");
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(" |123456789|\n-|---------|\n");
        for (int i = 0; i < size; i++) {
            result.append(i + 1).append("|");
            for (int j = 0; j < size; j++) {
                result.append(field[i][j].getSign());
            }
            result.append("|\n");
        }
        return result.append("-|---------|").toString();
    }
}
