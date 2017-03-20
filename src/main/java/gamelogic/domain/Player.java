package main.java.gamelogic.domain;

import main.java.constants.CellState;
import main.java.event.arguments.PlayerMovedEventArgs;

/**
 *
 * @author Tom Galvin
 * @author Lyubomir Pashev
 * @author Simeon Kostadinov
*/
public abstract class Player extends Entity {
	private String deathReason;
	private String name;
	private double angle;
	private int dotsEaten;
	private SkillSet skillSet;
    private int shield;

	public Player(final String name) {
		this.name = name;
		dotsEaten = 0;
		this.shield = 0;

		// Create SkillSet for each Player when added
        PacShield shield = new PacShield();
        shield.setCD(40);
        shield.setOwner(this);
        PacLaser laser = new PacLaser();
        laser.setCD(20);
        laser.setOwner(this);
        SkillSet skillSet = new SkillSet();
        skillSet.setQ(laser); // set E button to activate laser
        skillSet.setW(shield); // set W button to activate shield
        this.setSkillSet(skillSet);
	}

	/**
	 * Fetch the player's name
	 *
	 * @return the name as a string
	 */
	public String getName() {
		return name;
	}

	/**
	 * Fetch the player's angle
	 *
	 * @return the angle as a double decimal number
	 */
	public double getAngle() {
		return angle;
	}

	public int getDotsEaten() {
		return dotsEaten;
	}

    /**
     * Fetch the entity's shield
     *
     * @return a boolean
     */
    public int getShield() {
        return this.shield;
    }

    /**
     * Reduce the entity's shield
     *
     */
    public void reduceShield() {
        this.shield = shield - 1;
    }

    /**
     * Update the entity's shield
     *
     * @param shield
     *            the new shield value
     */
    public void setShield(final int shield) {
        this.shield = shield;
    }

    public void incrementCoolDown(){
        int coolDown = skillSet.getQ().getCD();
        if(coolDown < 20){
            skillSet.getQ().setCD(coolDown + 1);
        }
        coolDown = skillSet.getW().getCD();
        if(coolDown < 40 && shield == 0){
            skillSet.getW().setCD(coolDown + 1);
        }
    }
	/**
	 * Fetch the player's skillset
	 *
	 * @return player skillset
	 */
	public SkillSet getSkillSet() {
		return skillSet;
	}


	/**
	 * Set the player's skillset
	 *
	 * @return player skillset
	 */
	public void setSkillSet(SkillSet skillSet) {
		this.skillSet = skillSet;
	}

	/**
	 * Update the player's angle
	 *
	 * @param angle
	 *            the new angle
	 */
	public void setAngle(final double angle) {
		this.angle = angle;
		getOnMovedEvent()
				.fire(new PlayerMovedEventArgs(getPosition().getRow(), getPosition().getColumn(), angle, this));
	}

	@Override
	public boolean setPosition(final Position position) {
		eatDot();
		final boolean returnValue = super.setPosition(position);
		return returnValue;
	}

	protected void eatDot() {
		if (getWorld() != null/* && !getWorld().isRemote() */) {
			final Cell currentCell = getWorld().getMap().getCell(getPosition());

			if (currentCell.getState() == CellState.FOOD) {
				currentCell.setState(CellState.EMPTY);
				dotsEaten++;
			}
		}
	}

	public String getDeathReason() {
		return deathReason;
	}

	public void setDeathReason(String deathReason) {
		this.deathReason = deathReason;
	}
}
