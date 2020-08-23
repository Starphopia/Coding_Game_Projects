import java.util.*;
import java.io.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {
    // Constants for the directions.
    public static final String LEFT = "LEFT";
    public static final String RIGHT = "RIGHT";
    public static final String UP = "UP";
    public static final String DOWN = "DOWN";

    private final int R, C;
    private final char[][] map;
    private final boolean[][] visited;
    // Directions to return to previous cells.
    private final Deque<String> stack = new ArrayDeque<>(); 
    private int[] startRoom;
    private int[] controlRoom;

    public static void main(final String args[]) {
        final Player player = new Player();
    }

    public Player() {
        final Scanner in = new Scanner(System.in);
        R = in.nextInt(); // number of rows.
        C = in.nextInt(); // number of columns.
        final int A = in.nextInt(); // number of rounds between the time the alarm countdown is activated and the time the alarm goes off.
        map = new char[R][C]; 
        visited = new boolean[R][C];

        // game loop
        while (true) {
            final int KR = in.nextInt(); // row where Kirk is located.
            final int KC = in.nextInt(); // column where Kirk is located.
            if (startRoom == null) {
                startRoom = new int[] {KR, KC}; // Keeps track of start
            }
            for (int i = 0; i < R; i++) {
                final String nextRow = in.next(); // C of the characters in '#.TC?' (i.e. one line of the ASCII maze).
                for (int j = 0; j < C; j++) {
                    map[i][j] = nextRow.charAt(j);
                }
                System.err.println(nextRow);
            }            

            // If we haven't explored the entirety of the maze, do so, else we find the shortest path.
            if (!stack.isEmpty() || controlRoom == null) {
                mapOut(KR, KC); 
            } else {
                // Moving to the control room from the start position. 
                for (final String direction : aStarPath()) {
                    move(direction);
                }
                // To return to the start before detonation.
                while (!stack.isEmpty()) {
                    move("back");
                }
            }
        }
    }

    /**
        Maps out the grid.
        @param KR   row that Kirk is currently in.
        @param KC   column that Kirk is currently in.
        @return direction to be taken next.
    **/
    private void mapOut(final int KR, final int KC) {
        // Finds the next unexplored '.' and if c is found, records its position.
        if (KC < C - 1) {
            if (map[KR][KC + 1] == 'C') {
                controlRoom = new int[] {KR, KC + 1};
            } else if (map[KR][KC + 1] == '.' && !visited[KR][KC + 1]) {
                visited[KR][KC + 1] = true;
                move(RIGHT);
                return;
            } 
        } 
        if (KC > 0) {
            if (map[KR][KC - 1] == 'C') {
                controlRoom = new int[] {KR, KC - 1};
            } else if (map[KR][KC - 1] == '.' && !visited[KR][KC - 1]) {
                visited[KR][KC - 1] = true;
                move(LEFT);
                return;
            }
        } 
        if (KR < R - 1) {
            if (map[KR + 1][KC] == 'C'){
                controlRoom = new int[] {KR + 1, KC};
            } else if (map[KR + 1][KC] == '.' && !visited[KR + 1][KC]) {
                visited[KR + 1][KC] = true;
                move(DOWN);
                return;
            }
        } 
        if (KR > 0) {
            if (map[KR - 1][KC] == 'C') {
                controlRoom = new int[] {KR - 1, KC};
            } else if (map[KR - 1][KC] == '.' && !visited[KR - 1][KC]) {
                visited[KR - 1][KC] = true;
                move(UP);
                return;
            }
        } 

        move("back");   
    }

    /**
        Moves Kirk in a certain direction.
        @param direction    that Kirk will be moving in. 
    **/
    private void move(final String direction) {
        // Records the direciton needed to return to the last position.
        switch (direction) {
            case LEFT:
                stack.push(RIGHT); // Direction to move back.
                break;
            case RIGHT:
                stack.push(LEFT); 
                break;
            case UP:
                stack.push(DOWN);
                break;
            case DOWN:
                stack.push(UP);
                break;
            default:
                break;
        }

        // Issues the command to move.
        System.out.println(direction.equals("back") ? stack.pop() : direction);
    }

    /** 
        Finds the shortest path using the A* algorithm from the start room to the  
        control room.
        @return list of directions to to the control room.
    **/
    private List<String> aStarPath() {
        // Stores the references to the nodes.
        final Node[][] nodes = new Node[R][C];
        // Nodes to be evaluated.
        final PriorityQueue<Node> open = new PriorityQueue<>(new NodeComparator());
        // Evaluated nodes
        final PriorityQueue<Node> closed = new PriorityQueue<>(new NodeComparator());

        // Temporary placeholder to a node.
        Node node = new Node(startRoom[0], startRoom[1], null, controlRoom, "");
        // Add the reference of the start node to the array.
        nodes[startRoom[0]][startRoom[1]] = node;
        open.add(node); // Adds the start node.

        while (true) {
            // Removes the node with the lowest cost.
            node = open.poll();
            closed.add(node);

            // If the current node is the control room, return the list of directions.
            if (node.row == controlRoom[0] && node.col == controlRoom[1]) {
                return node.getPath();
            }

            // Processes each neighbour (each with the direction needed to traverse to it).
            for (final Map.Entry<String, int[]> adjacent : findNeighbours(node).entrySet()) {
                final int adjRow = adjacent.getValue()[0];
                final int adjCol = adjacent.getValue()[1];

                // If the neighbour doesn't have a node created for it yet and add it to open.
                if (nodes[adjRow][adjCol] == null) {
                    nodes[adjRow][adjCol] = new Node(adjRow, adjCol, node, controlRoom, adjacent.getKey());
                    open.add(nodes[adjRow][adjCol]);
                } else if (!closed.contains(nodes[adjRow][adjCol])) {
                    // If path to the node is shorter update this.
                    nodes[adjRow][adjCol].updateValues(node, adjacent.getKey());
                }
            }
        }
    }

    /**
     * Returns the list of neighbours within the grid that aren't walls.
     * 
     * @param node current node that we are finding neighbours to.
     **/
    private HashMap<String, int[]> findNeighbours(final Node node) {
        return findNeighbours(node.getRow(), node.getCol());
    }

    /**
     * Returns the list of neighbours within the grid that aren't walls.
     * 
     * @param row row of current cell that we are finding traversable adjacent cells
     *            to.
     * @param col column of current cell.
     * @return dictionary with keys as being the directions, and the integer arrays
     *         as being the coordinates of the neighbour nodes.
     **/
    private HashMap<String, int[]> findNeighbours(final int row, final int col) {
        final HashMap<String, int[]> neighbours = new HashMap<>();
        // Checks the node below.
        if ((row + 1 < R) && map[row + 1][col] != '#') {
            neighbours.put(DOWN, new int[] { row + 1, col });
        }
        // Checks the node above.
        if ((row - 1 >= 0) && map[row - 1][col] != '#') {
            neighbours.put(UP, new int[] { row - 1, col });
        }
        // Checks the node on the right.
        if ((col + 1 < C) && map[row][col + 1] != '#') {
            neighbours.put(RIGHT, new int[] { row, col + 1 });
        }
        // Checks the node on the left.
        if ((col - 1 >= 0) && map[row][col - 1] != '#') {
            neighbours.put(LEFT, new int[] { row, col - 1 });
        }

        return neighbours;
    }

    /**
     * Class for storing information about the nodes.
     **/
    private class Node {
        // Coordinates of current node.
        private final int row;
        private final int col;
        // Distance from the end node.
        private final double hCost;
        // Distance from the start node.
        private double gCost;
        
        // The previous node.
        private Node previous;
        // Direction from previous node to current node.
        private String previousToCurrent;

        /**
         * Constructor computes the cost of the nodes.
         * 
         * @param row               which row the node is in.
         * @param column            which column the node is in.
         * @param previous          previous node.
         * @param goalCoord         the coordinates the end node.
         * @param previousToCurrent direction to take us from the previous node to the
         *                          current node.
         **/
        public Node(final int row, final int column, final Node previous, final int[] goalCoord,
                final String previousToCurrent) {
            this.row = row;
            this.col = column;
            this.previous = previous;
            this.previousToCurrent = previousToCurrent;

            // Computes distance of path from start (each square has side length 10).
            gCost = previous == null ? 0 : previous.getGCost() + 10;
            // Computes approximate distance from the end node.
            hCost = Math.sqrt((row - goalCoord[0]) ^ 2 + (col - goalCoord[0]) ^ 2);
        }

        /**
         * Generates a path from the goal node with the current node as the goal node.
         * 
         * @return a list of directions from the beginning to the end node of a path.
         **/
        public List<String> getPath() {
            Node current = this;
            final ArrayList<String> pathDirections = new ArrayList<>();
            while (!current.getPreviousToCurrentDirection().equals("")) {
                pathDirections.add(0, current.getPreviousToCurrentDirection());
                current = current.getPrevious();
            }

            return pathDirections;
        }

        public double getFCost() {
            return gCost + hCost;
        }

        public double getGCost() {
            return gCost;
        }

        public double getHCost() {
            return hCost;
        }

        public int getCol() {
            return col;
        }

        public int getRow() {
            return row;
        }

        public Node getPrevious() {
            return previous;
        }

        public String getPreviousToCurrentDirection() {
            return previousToCurrent;
        }

        /**
         * Used to update the g value, previous node and the direction.
         * 
         * @param previous  the new previous node on the path leading to the current
         *                  node.
         * @param direction the direction from the previous to the current node.
         */
        public void updateValues(final Node previous, final String direction) {
            this.previous = previous;
            this.previousToCurrent = direction;
            gCost = previous.getGCost() + 10;
        }
    }

    /**
     * Used to compare nodes.
     */
    private class NodeComparator implements Comparator<Node> {
        /**
            Compares two nodes with each other based on their f cost.
            @param n1   first node
            @param n2   second node
        **/
        @Override
        public int compare(final Node n1, final Node n2) {
            // Compares them based on the f-cost.
            double value = n1.getFCost() - n2.getFCost();
            // If identical f-costs, compares them based on g-cost. 
            value = value == 0 ? value : n1.getGCost() - n2.getGCost();

            return (int)value;
        }
    }
}