package assignment2;

import java.awt.Color;
import java.util.Random;
import java.util.Stack;

import assignment2.food.*;
// eat fruit when pos most recently occupied is currently occ. by a segment?
public class Caterpillar {
	// All the fields have been declared public for testing purposes
	public Segment head;
	public Segment tail;
	public int length;
	public EvolutionStage stage;

	public Stack<Position> positionsPreviouslyOccupied;
	public int goal;
	public int turnsNeededToDigest;


	public static Random randNumGenerator = new Random(1);


	// Creates an assignment2.Caterpillar with one Segment. It is up to students to decide how to implement this.
	public Caterpillar(Position p, Color c, int goal) {
		this.head = new Segment(p, c);
		this.tail = this.head;
		this.length = 1;
		this.stage = EvolutionStage.FEEDING_STAGE;
		this.positionsPreviouslyOccupied = new Stack<Position>();
		this.turnsNeededToDigest = 0;
		this.goal = goal;
	}

	
	public EvolutionStage getEvolutionStage() {
		return this.stage;
	}

	public Position getHeadPosition() {
		return this.head.position;
	}

	public int getLength() {
		return this.length;
	}


	// returns the color of the segment in position p. Returns null if such segment does not exist
	public Color getSegmentColor(Position p) {
		Segment tmp = this.head;
		while (tmp != null) {
			if (tmp.position.equals(p)) {
				return tmp.color;
			}
			tmp = tmp.next;
		}
		return null;
	}


	// shift all Segments to the previous assignment2.Position while maintaining the old color
	public void move(Position p) {
		if ((Position.getDistance(this.getHeadPosition(), p)) != 1)
			throw new IllegalArgumentException("This position is not within reach.");

		Segment tmp = this.head;
		while (tmp != null) {
			if (tmp.position.equals(p)) {
				if (tmp == this.tail) {
					allowMove(p);
					return;
				} else {
					this.stage = EvolutionStage.ENTANGLED;
					return;
				}
			}
			if (tmp == this.tail) {
				allowMove(p);
				break;
			}
			tmp = tmp.next;
		}

		if (this.stage == EvolutionStage.GROWING_STAGE && this.turnsNeededToDigest == 0) {
			this.stage = EvolutionStage.FEEDING_STAGE;
		}

		if (this.turnsNeededToDigest > 0) {
			this.growRandom();
			this.turnsNeededToDigest--;
			if (this.length == this.goal) {
				this.stage = EvolutionStage.BUTTERFLY;
			}
		}
	}

	// private method facilitating move (no duplicated code)
	private void allowMove(Position p) {
		this.positionsPreviouslyOccupied.push(this.tail.position);
		Position[] positions = new Position[this.length];
		Segment tmp = this.head;
		for (int i = 0; i < this.length; i++) {
			positions[i] = tmp.position;
			tmp = tmp.next;
		}
		if (this.head != this.tail) {
			Segment tmp2 = this.head.next;
			for (int j = 0; j < this.length - 1; j++) {
				tmp2.position = positions[j];
				tmp2 = tmp2.next;
			}
		}
		this.head.position = p;
	}


	// a segment of the fruit's color is added at the end
	public void eat(Fruit f) {
		Segment newSegment = new Segment(this.positionsPreviouslyOccupied.pop(), f.getColor());
		this.tail.next = newSegment;
		this.tail = this.tail.next;
		this.length++;
		if (this.length == this.goal)
			this.stage = EvolutionStage.BUTTERFLY;
	}

	// the caterpillar moves one step backwards because of sourness
	public void eat(Pickle p) {
		Segment tmp = this.head;
		while (tmp != this.tail) {
			tmp.position = tmp.next.position;
			tmp = tmp.next;
		}
		this.tail.position = positionsPreviouslyOccupied.pop();
	}


	// all the caterpillar's colors shuffles around
	public void eat(Lollipop lolly) {
		Color[] colors = new Color[this.length];
		int i = 0;
		Segment tmp = this.head;
		while (tmp != null) {
			colors[i] = tmp.color;
			tmp = tmp.next;
			i++;
		}
		for (int j = this.length - 1; j >= 1; j--) {
			int r = randNumGenerator.nextInt(j+1);
			Color temp = colors[r];
			colors[r] = colors[j];
			colors[j] = temp;
		}
		int n = 0;
		Segment tmp2 = this.head;
		while (tmp2 != null) {
			tmp2.color = colors[n];
			tmp2 = tmp2.next;
			n++;
		}
	}

	// brain freeze!!
	// It reverses and its (new) head turns blue
	public void eat(IceCream gelato) {
		Segment pointer = this.head;
		Segment prev = null;
		Segment current = null;
		this.tail = this.head;

		while (pointer != null) {
			current = pointer;
			pointer = pointer.next;
			current.next = prev;
			prev = current;
			this.head = current;
		}
		this.head.color = GameColors.BLUE;
		this.positionsPreviouslyOccupied.clear();
	}

	// the caterpillar embodies a slide of Swiss cheese losing half of its segments.
	public void eat(SwissCheese cheese) {
		int newLength = (this.length)/2 + (this.length)%2;
		Color[] colors = new Color[newLength];
		int i = 0;
		Segment tmp = this.head;
		while (tmp != null) {
			colors[i] = tmp.color;
			if (tmp.next == null)
				break;
			tmp = tmp.next.next;
			i++;
		}
		Position[] positions = new Position[this.length];
		Segment tmp2 = this.head;
		for (int j = 0; j < this.length; j++) {
			positions[j] = tmp2.position;
			tmp2 = tmp2.next;
		}
		for (int x = this.length - 1; x >= newLength; x--)
			this.positionsPreviouslyOccupied.push(positions[x]);
		Segment tmp3 = this.head;
		for (int n = 0; n < newLength; n++) {
			tmp3.position = positions[n];
			tmp3.color = colors[n];
			if (n == newLength - 1) {
				this.tail = tmp3;
				this.tail.next = null;
				break;
			}
			tmp3 = tmp3.next;
		}
		this.length = newLength;
	}


	public void eat(Cake cake) {
		this.stage = EvolutionStage.GROWING_STAGE;
		Segment tmp = this.head;
		int growthCounter = 0;
		int maxGrowth = cake.getEnergyProvided();
		while (tmp != null && growthCounter < maxGrowth) {
			Position p = this.positionsPreviouslyOccupied.peek();
			if (tmp.position.equals(p))
				break;
			if (tmp == this.tail) {
				this.growRandom();
				growthCounter++;
				tmp = this.head;
				if (this.length == this.goal) {
					this.stage = EvolutionStage.BUTTERFLY;
					return;
				}
				if (this.positionsPreviouslyOccupied.isEmpty()) {
					break;
				}
			} else {
				tmp = tmp.next;
			}
		}
		if (growthCounter < maxGrowth) {
			this.turnsNeededToDigest = maxGrowth - growthCounter;
		} else {
			this.stage = EvolutionStage.FEEDING_STAGE;
		}
	}

	// Helper method for eat(Cake cake) and move(Position p).
	private void growRandom() {
		Color randomColor = GameColors.SEGMENT_COLORS[randNumGenerator.nextInt(5)];
		Segment newSegment = new Segment(this.positionsPreviouslyOccupied.pop(), randomColor);
		this.tail.next = newSegment;
		this.tail = this.tail.next;
		this.length++;
	}

	// This nested class was declared public for testing purposes
	public class Segment {
		private Position position;
		private Color color;
		private Segment next;

		public Segment(Position p, Color c) {
			this.position = p;
			this.color = c;
		}

	}


	public String toString() {
		Segment s = this.head;
		String gus = "";
		while (s!=null) {
			String coloredPosition = GameColors.colorToANSIColor(s.color) + 
					s.position.toString() + GameColors.colorToANSIColor(Color.WHITE);
			gus = coloredPosition + " " + gus;
			s = s.next;
		}
		return gus;
	}


}