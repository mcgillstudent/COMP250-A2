import assignment2.Caterpillar;
import assignment2.GameColors;
import assignment2.Position;
import assignment2.food.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Random;

public class JerrysPlayground extends Frame {
    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 60;
    private static final int TOP_MARGIN = 80;

    private static final int CANVAS_SIZE = 800;
    private static final int WORLD_SIZE = 15;
    private static final int GRID_SIZE = CANVAS_SIZE / WORLD_SIZE;

    private static Field nextField;
    private static Field colorField;
    private static Field positionField;

    private static final Font detailsFont = new Font("Arial", Font.PLAIN, 30);

    static {
        try {
            nextField = Caterpillar.Segment.class.getDeclaredField("next");
            colorField = Caterpillar.Segment.class.getDeclaredField("color");
            positionField = Caterpillar.Segment.class.getDeclaredField("position");

            nextField.setAccessible(true);
            colorField.setAccessible(true);
            positionField.setAccessible(true);
        } catch (NoSuchFieldException shouldntHappen) {
            shouldntHappen.printStackTrace();
        }
    }

    private static Caterpillar.Segment next(Caterpillar.Segment current) {
        try {
            return (Caterpillar.Segment) nextField.get(current);
        } catch (Exception whatever) {
            return null;
        }
    }
    private static void setNext(Caterpillar.Segment current, Caterpillar.Segment next) {
        try {
            nextField.set(current, next);
        } catch (Exception ignored) { }
    }

    private static Color color(Caterpillar.Segment segment) {
        try {
            return (Color) colorField.get(segment);
        } catch (Exception whatever) {
            return null;
        }
    }

    private static Position position(Caterpillar.Segment segment) {
        try {
            return (Position) positionField.get(segment);
        } catch (Exception whatever) {
            return null;
        }
    }

    WorldCanvas canvas;

    JerrysPlayground() {
        setSize(1200, 1000);
        setTitle("Jerry's Playground");

        initialiseResetButton();
        initialiseMovementButtons();
        initialiseFeedButtons();

        canvas = new WorldCanvas(this);
        add(canvas);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        resetCaterpillar();

        setLayout(null);
        setVisible(true);
        setResizable(false);
    }

    Caterpillar gus;
    private void resetCaterpillar() {
        gus = new Caterpillar(new Position(10, 10), GameColors.RED, 10);

        // Manually links the segments for the initialisation, since there's no unified way of accessing linked list
        // methods.
        Caterpillar.Segment orange = gus.new Segment(new Position(10, 11), GameColors.ORANGE);
        Caterpillar.Segment yellow = gus.new Segment(new Position(10, 12), GameColors.YELLOW);

        setNext(gus.head, orange);
        setNext(orange, yellow);
        gus.tail = yellow;
        gus.length = 3;

        // Update the canvas.
        updateCanvas();
    }

    Button up = new Button("Up");
    Button down = new Button("Down");
    Button left = new Button("Left");
    Button right = new Button("Right");
    private void initialiseMovementButtons() {
        up.addActionListener(e -> moveCaterpillar(0, -1));
        up.setBounds(getButtonBox(1));

        down.addActionListener(e -> moveCaterpillar(0, 1));
        down.setBounds(getButtonBox(2));

        left.addActionListener(e -> moveCaterpillar(-1, 0));
        left.setBounds(getButtonBox(3));

        right.addActionListener(e -> moveCaterpillar(1, 0));
        right.setBounds(getButtonBox(4));

        addButtons(up, down, left, right);
    }

    Button reset = new Button("Reset");
    private void initialiseResetButton() {
        reset.addActionListener(e -> resetCaterpillar());
        reset.setBounds(getButtonBox(0));

        addButton(reset);
    }

    Button fruit = new Button("Fruit");
    Random fruitRNG = new Random(1);
    Button iceCream = new Button("Ice Cream");
    Button lollipop = new Button("Lollipop");
    Button pickle = new Button("Pickle");
    Button cheese = new Button("Cheese");
    Button cake = new Button("Cake (5)");
    private void initialiseFeedButtons() {
        fruit.addActionListener(e -> feedCaterpillar(new Fruit(
                GameColors.SEGMENT_COLORS[fruitRNG.nextInt(GameColors.SEGMENT_COLORS.length)]
        )));
        fruit.setBounds(getButtonBox(5));

        iceCream.addActionListener(e -> feedCaterpillar(new IceCream()));
        iceCream.setBounds(getButtonBox(6));

        lollipop.addActionListener(e -> feedCaterpillar(new Lollipop()));
        lollipop.setBounds(getButtonBox(7));

        pickle.addActionListener(e -> feedCaterpillar(new Pickle()));
        pickle.setBounds(getButtonBox(8));

        cheese.addActionListener(e -> feedCaterpillar(new SwissCheese()));
        cheese.setBounds(getButtonBox(9));

        cake.addActionListener(e -> feedCaterpillar(new Cake(5)));
        cake.setBounds(getButtonBox(10));

        addButtons(fruit, iceCream, lollipop, pickle, cheese, cake);
    }

    private Rectangle getButtonBox(int index) {
        return new Rectangle(0, TOP_MARGIN + BUTTON_HEIGHT * index, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    private void addButton(Button button) {
        button.setFont(new Font("Arial", Font.PLAIN, 40));
        add(button);
    }
    private void addButtons(Button... buttons) {
        for (Button button: buttons) {
            addButton(button);
        }
    }

    private void moveCaterpillar(int deltaX, int deltaY) {
        Position headPosition = gus.getHeadPosition();

        if (headPosition != null) {
            gus.move(new Position(headPosition.getX() + deltaX, headPosition.getY() + deltaY));
        }
        updateButtons();
        updateCanvas();
    }

    private void feedCaterpillar(FoodItem food) {
        food.accept(gus);
        updateButtons();
        updateCanvas();
    }

    private void updateButtons() {
        Position headPosition = gus.getHeadPosition();

        up.setEnabled(!(headPosition == null || headPosition.getY() == 0));
        down.setEnabled(!(headPosition == null || headPosition.getY() == WORLD_SIZE));
        left.setEnabled(!(headPosition == null || headPosition.getX() == 0));
        right.setEnabled(!(headPosition == null || headPosition.getX() == WORLD_SIZE));
    }

    private void updateCanvas() {
        canvas.repaint();
    }

    public static void main(String[] args) {
        new JerrysPlayground();
    }

    private static class WorldCanvas extends Canvas {
        private JerrysPlayground testingKit;

        public WorldCanvas(JerrysPlayground kit) {
            this.testingKit = kit;

            setBackground(Color.LIGHT_GRAY.brighter());
            setBounds(200, 0, 1000, 1000);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);

            paintGrid(g);
            paintCaterpillar(g, testingKit.gus);
        }

        private void paintGrid(Graphics g) {
            g.setColor(Color.LIGHT_GRAY);

            for (int i = 0; i < WORLD_SIZE; i++) {
                // Horizontal line.
                g.drawLine(0, i * GRID_SIZE, CANVAS_SIZE, i * GRID_SIZE);
                // Vertical line.
                g.drawLine(i * GRID_SIZE, 0, i * GRID_SIZE, CANVAS_SIZE);
            }
        }

        private void paintSegments(Graphics g, Caterpillar gus) {
            // First paint the segments.
            for (SegmentIterator it = new SegmentIterator(gus); it.hasNext(); ) {
                Caterpillar.Segment segment = it.next();
                Color color = color(segment);
                Position position = position(segment);

                g.setColor(color);
                g.fillRect(position.getX() * GRID_SIZE, position.getY() * GRID_SIZE, GRID_SIZE, GRID_SIZE);
            }
        }

        private void highlightHead(Graphics g, Caterpillar gus) {
            // Highlight the head.
            if (gus.head != null) {
                Color color = color(gus.head);
                Position position = position(gus.head);

                g.setColor(color.brighter());
                g.drawRect(position.getX() * GRID_SIZE, position.getY() * GRID_SIZE, GRID_SIZE, GRID_SIZE);
            }
        }

        // Outlines the positions that have been previously occupied.
        private void paintTrail(Graphics g, Caterpillar gus) {
            int numberOfPositions = gus.positionsPreviouslyOccupied.size();
            int i = 0;
            for (Position position: gus.positionsPreviouslyOccupied) {
                g.setColor(new Color(0, 0, 0, 0.3f + 0.5f * (i / (float)numberOfPositions)));
                g.drawRect(position.getX() * GRID_SIZE, position.getY() * GRID_SIZE, GRID_SIZE, GRID_SIZE);
                i++;
            }
        }

        private void paintDetails(Graphics g, Caterpillar gus) {
            FontMetrics metrics = g.getFontMetrics();

            String details = String.format("stage=%s digest=%d goal=%d", gus.stage, gus.turnsNeededToDigest, gus.goal);
            int x = CANVAS_SIZE / 2 - metrics.stringWidth(details) / 2;

            g.setColor(Color.BLACK);
            g.drawString(details, x, TOP_MARGIN * 2);
        }

        private void paintCaterpillar(Graphics g, Caterpillar gus) {
            BufferedImage buffer = new BufferedImage(CANVAS_SIZE, CANVAS_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D bufferedGraphics = buffer.createGraphics();

            bufferedGraphics.setFont(detailsFont);
            paintSegments(bufferedGraphics, gus);
            highlightHead(bufferedGraphics, gus);
            paintTrail(bufferedGraphics, gus);
            paintDetails(bufferedGraphics, gus);

            g.drawImage(buffer, 0, 0, null);
        }

        private static class SegmentIterator implements Iterator<Caterpillar.Segment> {
            private Caterpillar.Segment current;
            // Also keep track of the size in case weird loops happen.
            private int itemsRemaining;

            public SegmentIterator(Caterpillar caterpillar) {
                this.current = caterpillar.head;
                this.itemsRemaining = caterpillar.length;
            }

            @Override
            public boolean hasNext() {
                return current != null && itemsRemaining > 0;
            }

            @Override
            public Caterpillar.Segment next() {
                Caterpillar.Segment toReturn = current;
                current = JerrysPlayground.next(current);
                itemsRemaining--;
                return toReturn;
            }
        }
    }
}