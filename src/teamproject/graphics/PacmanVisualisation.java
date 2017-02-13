package teamproject.graphics;

import java.util.Optional;

import javafx.scene.Node;
import javafx.scene.image.ImageView;

import teamproject.ai.AIPlayer;
import teamproject.constants.CellState;
import teamproject.constants.Images;
import teamproject.gamelogic.domain.Behaviour;

/**
 * Created by Boyan Bonev on 11/02/2017.
 */
public class PacmanVisualisation extends AIPlayer {

	private PositionVisualisation position;
	private GridVisualisation grid;
	private Node node;
	private MapVisualisation map;

	/**
	 * Initialize new visualisation for the PacMan player
	 * @param behaviour
	 * @param name
	 * @param grid
	 * @param map
	 */
	public PacmanVisualisation(final Behaviour behaviour, final String name, final GridVisualisation grid,
			final MapVisualisation map) {
		super(Optional.empty(), name, behaviour, map.getGrid());

		this.position = new PositionVisualisation(1, 1);
		this.grid = grid;
		this.map = map;
		Images.PacMan = new ImageView("pacman.png");
	}

	/**
	 * Move the PacMan up
	 * @return true/false depending on whether the move is legit or not
	 */
	public boolean moveUp() {

		if (grid.getCell(position.getRow() - 1, position.getColumn()).getState() == CellState.OBSTACLE) {
			return false;
		}

		Images.PacMan.setRotate(90 + 180);

		position = new PositionVisualisation(position.getRow() - 1, position.getColumn());
		if (grid.getCell(position.getRow(), position.getColumn()).getState() == CellState.FOOD) {
			grid.getCell(position.getRow(), position.getColumn()).setState(CellState.EMPTY);
		}

		map.redrawMap();

		return true;
	}

	/**
	 * Move the PacMan down
	 * @return true/false depending on whether the move is legit or not
	 */
	public boolean moveDown() {

		if (grid.getCell(position.getRow() + 1, position.getColumn()).getState() == CellState.OBSTACLE) {
			return false;
		}

		Images.PacMan.setRotate(90);

		position = new PositionVisualisation(position.getRow() + 1, position.getColumn());
		if (grid.getCell(position.getRow(), position.getColumn()).getState() == CellState.FOOD) {
			grid.getCell(position.getRow(), position.getColumn()).setState(CellState.EMPTY);
		}

		map.redrawMap();

		return true;
	}

	/**
	 * Move the PacMan left
	 * @return true/false depending on whether the move is legit or not
	 */
	public boolean moveLeft() {

		if (grid.getCell(position.getRow(), position.getColumn() - 1).getState() == CellState.OBSTACLE) {
			return false;
		}

		Images.PacMan.setRotate(-180);

		position = new PositionVisualisation(position.getRow(), position.getColumn() - 1);
		if (grid.getCell(position.getRow(), position.getColumn()).getState() == CellState.FOOD) {
			grid.getCell(position.getRow(), position.getColumn()).setState(CellState.EMPTY);
		}

		map.redrawMap();

		return true;
	}

	/**
	 * Move the PacMan right
	 * @return true/false depending on whether the move is legit or not
	 */
	public boolean moveRight() {

		if (grid.getCell(position.getRow(), position.getColumn() + 1).getState() == CellState.OBSTACLE) {
			return false;
		}

		Images.PacMan.setRotate(0);

		position = new PositionVisualisation(position.getRow(), position.getColumn() + 1);
		if (grid.getCell(position.getRow(), position.getColumn()).getState() == CellState.FOOD) {
			grid.getCell(position.getRow(), position.getColumn()).setState(CellState.EMPTY);
		}

		map.redrawMap();

		return true;
	}

	/**
	 * Get the node that represents the PacMan player
	 * @return Node
	 */
	public Node getNode() {
		double min = position.getHeight();
		if (position.getWidth() < position.getHeight()) {
			min = position.getWidth();
		}

		Images.PacMan.setFitWidth(min);
		Images.PacMan.setFitHeight(min);

		Images.PacMan.setX(position.getPixelX() + position.getWidth() / 2 - min / 2);
		Images.PacMan.setY(position.getPixelY() + position.getHeight() / 2 - min / 2);

		node = Images.PacMan;

		return node;
	}

	/**
	 * Get the position of the PacMan
	 * @return PositionVisualisation
	 */
	public PositionVisualisation getPosition() {
		return position;
	}
}
