/*  Edward Zhou
    2020-01-18

    Culminating Game: Shadow Warrior
        -   See README for game details
 */

//  Setup

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Arc2D;
import java.util.ArrayList;

public class Main extends JPanel implements Runnable, KeyListener {
    static JFrame frame;
    final int WINDOW_WIDTH = 1280, WINDOW_HEIGHT = 720;
    final int FPS = 120;
    final int floorHeight = 620;
    final double xAccel = 0.2;
    final double xDecel = 0.2;
    final double GRAV = 0.2;
    final double jumpAccel = 9;
    final int LEFT = -1;
    final int RIGHT = 1;
    Thread thread;
    boolean showMenu = true;
    boolean showHitboxes = false;
    int tick = 0;
    Image background = Toolkit.getDefaultToolkit().getImage("background.png");
    Image floor = Toolkit.getDefaultToolkit().getImage("ground.png");
    //  Dimensions
    int playerHeight = 75, playerWidth = playerHeight * 50 / 200;
    //  Pixels above, below and to the side of playeracter model in image
    int spaceAbove = playerHeight * 266 / 200;
    int spaceBelow = playerHeight * 185 / 200;
    int spaceSide = playerWidth * 9;
    //  Image dimensions
    int imageHeight = playerHeight + spaceAbove + spaceBelow;
    int imageWidth = playerWidth + 2 * spaceSide;
    //  Position variables
    int xPos = 0, yPos = floorHeight - playerHeight - spaceAbove;
    //  Horizontal variables
    double xVel = 0;
    double walkVel = 2;
    double sprintVel = 6;
    double xVelMax = walkVel;
    boolean isSprinting = false;
    //  Vertical variables
    double yVel = 0;
    double yAccel = 2;              //  Down press acceleration
    double termVel = 8;
    double yVelMax = termVel;
    boolean canDoubleJump;
    //  Movement
    boolean jump, drop, left, right;
    boolean jumpHeld;               //  jump pulses true when pressed, jumpHeld is true while held
    boolean leftHeld, rightHeld;
    int direction = RIGHT;
    boolean isAirborne = false;
    boolean isHeldO = false;        //  True if 'O' is held
    boolean isHeldP = false;        //  True if 'P' is held
    //  Dashing
    double xVelDash = 25;
    double xDecelDash = xVelDash * 0.016;
    boolean isDashing = false;
    //  Special
    boolean isSpecial = false;
    //  Slam
    double yVelSlam = 16;
    boolean isSlamming = false;
    //  Attacks
    int attackFrame = 0;
    double xVelAttack = 8;                     //  Side attack travel speed
    double xDecelAttack = xVelAttack * 0.016;
    boolean isAttackingS = false;               //  Side attack
    boolean isAttackingU = false;               //  Up attack
    boolean isAttackingD = false;               //  Down attack
    boolean inAction;           //  Whether or not playeracter is performing an attack or dash
    int hitTick = 0;
    boolean isInvuln;
    //  Health
    int hitpointsPlayer = 10;
    //  Enemy
    double xVelMaxEnemy = 1;
    ArrayList<Integer> xPosEnemy = new ArrayList<>();
    ArrayList<Integer> yPosEnemy = new ArrayList<>();
    ArrayList<Double> xVelEnemy = new ArrayList<>();
    ArrayList<Double> yVelEnemy = new ArrayList<>();
    ArrayList<Integer> directionEnemy = new ArrayList<>();
    ArrayList<Integer> hitpointsEnemy = new ArrayList<>();
    ArrayList<Boolean> alreadyHit = new ArrayList<>();
    ArrayList<Boolean> isKnockedBack = new ArrayList<>();
    ArrayList<Rectangle> healthBars = new ArrayList<>();

    //  Icons
    Image heart = Toolkit.getDefaultToolkit().getImage("heart.png");
    //  Standing
    Image lStand = Toolkit.getDefaultToolkit().getImage("lStand.png");
    Image rStand = Toolkit.getDefaultToolkit().getImage("rStand.png");
    Image[] Stand = {lStand, rStand};
    //  Jumping
    Image lJump = Toolkit.getDefaultToolkit().getImage("lJump.png");
    Image rJump = Toolkit.getDefaultToolkit().getImage("rJump.png");
    Image[] Jump = {lJump, rJump};
    //  Sprinting animations
    Image[] lSprint = new Image[6];
    Image[] rSprint = new Image[6];
    Image[][] Sprint = {lSprint, rSprint};
    //  Special
    Image lShockwave = Toolkit.getDefaultToolkit().getImage("lShockwave.png");
    Image rShockwave = Toolkit.getDefaultToolkit().getImage("rShockwave.png");
    //  Slam attack
    Image lSlam = Toolkit.getDefaultToolkit().getImage("lSlam.png");
    Image rSlam = Toolkit.getDefaultToolkit().getImage("rSlam.png");
    Image slamShockwave = Toolkit.getDefaultToolkit().getImage("slamShockwave.png");
    //  Attack animations
    Image[] lAttackA = new Image[4];
    Image[] lAttackB = new Image[4];
    Image[] lAttackC = new Image[4];
    Image[] lAttackD = new Image[4];
    Image[] rAttackA = new Image[4];
    Image[] rAttackB = new Image[4];
    Image[] rAttackC = new Image[4];
    Image[] rAttackD = new Image[4];
    Image[][] AttackA = {lAttackA, rAttackA};
    Image[][] AttackB = {lAttackB, rAttackB};
    Image[][] AttackC = {lAttackC, rAttackC};
    Image[][] AttackD = {lAttackD, rAttackD};
    //  Enemy running animations
    Image[] lEnemy = new Image[6];
    Image[] rEnemy = new Image[6];
    Image[][] Enemy = {lEnemy, rEnemy};

    //  Hitboxes
    Rectangle hbStanding = new Rectangle((WINDOW_WIDTH - playerWidth) / 2,
            yPos + spaceAbove, playerWidth, playerHeight);
    Rectangle hbPlayer = hbStanding;
    Shape hbAttack = new Rectangle(50, 50, 100, 10);
    ArrayList<Rectangle> hbEnemies = new ArrayList<>();

    //  Offscreen buffer
    Image offScreenImage;
    Graphics offScreenBuffer;

    //  Constructor
    public Main() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        thread = new Thread(this);
        thread.start();

        loadImages();
    }

    //  Main method
    public static void main(String[] args) {
        frame = new JFrame("Shadow Warrior Ninja Strike Team: The Final Mission - One Against Ten Thousand");
        Main myPanel = new Main();
        frame.add(myPanel);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
    }

    //  Main game loop
    public void run() {
        //  Game loop
        while (true) {
            tick++;
            update();
            spawnEnemies();
            movePlayer();
            moveEnemies();
            detectCollision();
            keepInBound();
            this.repaint();
            try {
                Thread.sleep(1000 / FPS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //  Updates and recalculates essential variables once every tick
    //  Return type: No return, updates global variables (void) (void)
    //  Parameters: No parameters, uses global variables
    public void update() {
        //  Determines if playeracter is performing action
        inAction = isDashing || isSpecial || isSlamming || isAttackingS || isAttackingU || isAttackingD;

        //  Resets variables upon landing
        if (yPos == floorHeight - playerHeight - spaceAbove) {
            isAirborne = false;
            canDoubleJump = true;
        }

        //  Determines if playeracter is sprinting
        if (isSprinting) {
            xVelMax = sprintVel;
        } else {
            xVelMax = walkVel;
        }

        //  Determines if dash is finished
        if (xVel <= xVelMax && xVel >= -xVelMax)
            isDashing = false;

        if (!inAction) {
            clearAttackHitbox();                            //  Clears attack hitbox when not in use
            for (int i = 0; i < alreadyHit.size(); i++)     //  Allows enemies to be hit again
                alreadyHit.set(i, false);
        }

        //  Updates enemy health bars
        for (int i = 0; i < healthBars.size(); i++) {
            healthBars.get(i).width = hitpointsEnemy.get(i) * 5;
        }
    }

    //  Saves animation sprites into arrays for easy future access
    //  Return type: No return, arrays are global (void)
    //  Parameters: No parameters, arrays are global
    public void loadImages() {
        //  Attack Animations
        for (int i = 0; i < 4; i++) {
            lAttackA[i] = Toolkit.getDefaultToolkit().getImage("lAttackA" + (i + 1) + ".png");
            lAttackB[i] = Toolkit.getDefaultToolkit().getImage("lAttackB" + (i + 1) + ".png");
            lAttackC[i] = Toolkit.getDefaultToolkit().getImage("lAttackC" + (i + 1) + ".png");
            lAttackD[i] = Toolkit.getDefaultToolkit().getImage("lAttackD" + (i + 1) + ".png");
            rAttackA[i] = Toolkit.getDefaultToolkit().getImage("rAttackA" + (i + 1) + ".png");
            rAttackB[i] = Toolkit.getDefaultToolkit().getImage("rAttackB" + (i + 1) + ".png");
            rAttackC[i] = Toolkit.getDefaultToolkit().getImage("rAttackC" + (i + 1) + ".png");
            rAttackD[i] = Toolkit.getDefaultToolkit().getImage("rAttackD" + (i + 1) + ".png");
        }
        //  Sprinting and Enemy Animations
        for (int i = 0; i < 6; i++) {
            lSprint[i] = Toolkit.getDefaultToolkit().getImage("lSprint" + (i + 1) + ".png");
            rSprint[i] = Toolkit.getDefaultToolkit().getImage("rSprint" + (i + 1) + ".png");
            lEnemy[i] = Toolkit.getDefaultToolkit().getImage("lEnemy" + (i + 1) + ".png");
            rEnemy[i] = Toolkit.getDefaultToolkit().getImage("rEnemy" + (i + 1) + ".png");
        }
    }

    //  Given a direction (LEFT, RIGHT), method returns an index number (0, 1) respectively
    //  Return type: Index (int)
    //  Parameters: Direction variable (int)
    public int directionToIndex(int i) {
        if (i == LEFT)
            return 0;
        else
            return 1;
    }

    //  Detects when a keyboard key is pressed
    //  Return type: No return, updates global variables (void)
    //  Parameters: KeyEvent variable (KeyEvent)
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        //  Movement
        if (key == KeyEvent.VK_A) {
            if (!inAction) {
                left = true;
                leftHeld = true;
                right = false;
                rightHeld = false;
                isSprinting = false;
                direction = LEFT;
            }
        } else if (key == KeyEvent.VK_D) {
            if (!inAction) {
                right = true;
                rightHeld = true;
                left = false;
                leftHeld = false;
                isSprinting = false;
                direction = RIGHT;
            }
        } else if (key == KeyEvent.VK_W) {
            if (!jumpHeld && !inAction) {
                jumpHeld = true;
                jump = true;
            }
        } else if (key == KeyEvent.VK_S) {
            drop = true;
        }

        //  Sprinting
        if (key == KeyEvent.VK_SHIFT && !isAirborne && !inAction && xVel != 0) {
            isSprinting = !isSprinting;
        }

        //  Special
        if (key == KeyEvent.VK_O && jumpHeld) {                                 //  Special attack
            isSpecial = true;
        } else if (key == KeyEvent.VK_O && drop && isAirborne) {                //  Slam attack
            isSlamming = true;
            if (yVel <= yVelMax)
                yVelMax = yVelSlam;
        } else if (key == KeyEvent.VK_O && (left || right) && !isHeldO) {       //  Dash attack
            isDashing = true;
            if (xVel <= xVelMax && xVel >= -xVelMax)
                xVel = xVelDash * direction;
        }

        if (key == KeyEvent.VK_O)
            isHeldO = true;

        //  Directional Attack
        if (key == KeyEvent.VK_P && jumpHeld) {                         //  Up attack
            isAttackingU = true;
        } else if (key == KeyEvent.VK_P && drop) {                      //  Down attack
            isAttackingD = true;
        } else if (key == KeyEvent.VK_P && (left || right)) {           //  Side attack
            isAttackingS = true;
            if (xVel <= xVelMax && xVel >= -xVelMax)
                xVel = xVelAttack * direction;
        }

        //  Toggle hitboxes
        if (key == KeyEvent.VK_H)
            showHitboxes = !showHitboxes;
    }

    public void keyTyped(KeyEvent e) {
    }

    //  Detects when a keyboard key is released
    //  Return type: No return, updates global variables (void)
    //  Parameters: KeyEvent variable (KeyEvent)
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_A) {
            left = false;
            leftHeld = false;
            isSprinting = false;
        } else if (key == KeyEvent.VK_D) {
            right = false;
            rightHeld = false;
            isSprinting = false;
        } else if (key == KeyEvent.VK_W) {
            jump = false;
            jumpHeld = false;
        } else if (key == KeyEvent.VK_S) {
            drop = false;
        } else if (key == KeyEvent.VK_O) {
            isHeldO = false;
        }
    }

    //  Updates player and player hitbox position based on movement commands and virtual physics
    //  Return type: No return, updates global variables (void)
    //  Parameters: No parameters, all variables global
    public void movePlayer() {
        //  Horizontal movement
        if (left && xVel > -xVelMax) {
            xVel -= xAccel;
        } else if (right && xVel < xVelMax) {
            xVel += xAccel;
        }

        if (xVel != 0) {
            manageSpeed();
        }

        //  Vertical movement
        if (canDoubleJump && isAirborne && jump) {        //  Allows double jump
            yVel = jumpAccel;
            canDoubleJump = false;
            jump = false;
        } else if (!isAirborne && jump) {
            isAirborne = true;
            yVel = jumpAccel;
            jump = false;
        } else if (isAirborne) {
            if (drop)
                yVel -= yAccel;
            yVel -= GRAV;
        }

        //  Shifts background
        if (xPos < -WINDOW_WIDTH * 2 || xPos > WINDOW_WIDTH * 2)
            xPos = 0;

        //  Limit falling speed
        if (yVel < -yVelMax)        //  Down
            yVel = -yVelMax;

        //  Position updated
        xPos -= xVel;
        for (int i = 0; i < xPosEnemy.size(); i++) {
            xPosEnemy.set(i, xPosEnemy.get(i) - (int) xVel);
            hbEnemies.get(i).x = hbEnemies.get(i).x - (int) xVel;
            healthBars.get(i).x = hbEnemies.get(i).x - (int) xVel;
        }
        yPos -= yVel;
        hbPlayer.y -= yVel;
    }

    //  Updates enemy sprite, health bar, and hitbox positions and removes runaway enemies
    //  Return type: No return, updates global variables (void)
    //  Parameters: No parameters, all variables global
    public void moveEnemies() {
        for (int i = 0; i < xPosEnemy.size(); i++) {
            //  Horizontal
            xVelEnemy.set(i, xVelEnemy.get(i) + directionEnemy.get(i));
            manageSpeedEnemy();

            //  Vertical
            if (yPosEnemy.get(i) < floorHeight - playerHeight - spaceAbove) {
                yVelEnemy.set(i, yVelEnemy.get(i) - GRAV);
            }

            //  Updates positions
            xPosEnemy.set(i, (int) (xPosEnemy.get(i) + xVelEnemy.get(i)));
            hbEnemies.get(i).x += xVelEnemy.get(i);
            healthBars.get(i).x += xVelEnemy.get(i);

            yPosEnemy.set(i, (int) (yPosEnemy.get(i) - yVelEnemy.get(i)));
            hbEnemies.get(i).y -= yVelEnemy.get(i);
            healthBars.get(i).y -= yVelEnemy.get(i);

            //  Removes entities that travel too far off screen
            if (xPosEnemy.get(i) > WINDOW_WIDTH + 1000 && directionEnemy.get(i) == RIGHT ||
                    xPosEnemy.get(i) < -imageWidth - 1000 && directionEnemy.get(i) == LEFT)
                removeEntity(i);
        }
    }

    //  Spawns enemies at a random and increasing rate, assigning variables like position and hitpoints to each
    //  A given index represents the same enemy entity across all ArrayLists
    //  Return type: No return, updates global variables (void)
    //  Parameters: No parameters, all variables global
    public void spawnEnemies() {
        double rand;
        int randSpacing = (int) (Math.random() * (200 - 100 + 1)) + 100;
        if (tick % randSpacing == 0) {              //  Spawns enemies with 50 to 150 ticks between
            rand = Math.random();
            if (rand < 0.5) {                       //  Left spawn
                xPosEnemy.add(-imageWidth);
                directionEnemy.add(RIGHT);
                hbEnemies.add(new Rectangle(-imageWidth / 2, floorHeight - playerHeight,
                        playerHeight * 125 / 200, playerHeight));
                healthBars.add(new Rectangle(-imageWidth / 2 + 35, floorHeight - playerHeight - 15,
                        50, 5));
            } else {                                //  Right spawn
                xPosEnemy.add(WINDOW_WIDTH);
                directionEnemy.add(LEFT);
                hbEnemies.add(new Rectangle(WINDOW_WIDTH + imageWidth / 2 - playerHeight * 125 / 200,
                        floorHeight - playerHeight, playerHeight * 125 / 200, playerHeight));
                healthBars.add(new Rectangle(WINDOW_WIDTH + imageWidth / 2 - playerHeight * 90 / 200,
                        floorHeight - playerHeight - 15, 50, 5));
            }
            yPosEnemy.add(floorHeight - playerHeight - spaceAbove);
            xVelEnemy.add(0.0);
            yVelEnemy.add(0.0);
            hitpointsEnemy.add(10);
            alreadyHit.add(false);
            isKnockedBack.add(false);
        }
    }

    //  Checks for the collision of two hitboxes and registers damage dealt
    //  Return type: No return, updates global variables (void)
    //  Parameters: No parameters, all variables global
    public void detectCollision() {
        if (tick >= hitTick + 60) {         //  60 frame invulnerability period after getting hit
            isInvuln = false;
        }
        for (int i = 0; i < hbEnemies.size(); i++) {
            //  Hitting player
            if (hbPlayer.intersects(hbEnemies.get(i))) {
                if (!isInvuln && !isDashing && !isSlamming) {
                    hitpointsPlayer--;
                    isInvuln = true;
                    hitTick = tick;
                }
            }

            //  Player death
            if (hitpointsPlayer <= 0) {
                System.out.println("Character died");
                hitpointsPlayer = 10;
            }

            //  Hitting enemies
            if (hbAttack.intersects(hbEnemies.get(i))) {
                isKnockedBack.set(i, true);
                if (isDashing && !alreadyHit.get(i)) {
                    hitpointsEnemy.set(i, hitpointsEnemy.get(i) - 2);
                } else if (isSpecial && !alreadyHit.get(i)) {
                    hitpointsEnemy.set(i, hitpointsEnemy.get(i) - 9);
                } else if (isSpecial && !alreadyHit.get(i)) {
                    hitpointsEnemy.set(i, hitpointsEnemy.get(i) - 4);
                } else if (isAttackingS && !alreadyHit.get(i)) {
                    hitpointsEnemy.set(i, hitpointsEnemy.get(i) - 2);
                } else if (isAttackingU && !alreadyHit.get(i)) {
                    hitpointsEnemy.set(i, hitpointsEnemy.get(i) - 3);
                } else if (isAttackingD && !alreadyHit.get(i)) {
                    hitpointsEnemy.set(i, hitpointsEnemy.get(i) - 3);
                }

                //  Knockback
                if (xPosEnemy.get(i) + spaceSide - WINDOW_WIDTH / 2 > 0) {
                    if (directionEnemy.get(i) == LEFT)
                        xVelEnemy.set(i, 20.0);
                    else
                        xVelEnemy.set(i, 5.0);
                } else {
                    if (directionEnemy.get(i) == LEFT)
                        xVelEnemy.set(i, -5.0);
                    else
                        xVelEnemy.set(i, -20.0);
                }
                yVelEnemy.set(i, 10.0);

                //  When enemy is killed
                if (hitpointsEnemy.get(i) <= 0) {
                    removeEntity(i);
                } else {
                    alreadyHit.set(i, true);
                }
            }
        }
    }

    //  Decelerates the player when necessary and limits velocity otherwise
    //  Return type: No return, updates global variables (void)
    //  Parameters: No parameters, all variables global
    public void manageSpeed() {
        if (xVel > xVelMax) {
            if (isDashing)
                xVel -= xDecelDash;
            else if (isAttackingS)
                xVel -= xDecelAttack;
            else
                xVel = xVelMax;
        } else if (xVel < -xVelMax) {
            if (isDashing)
                xVel += xDecelDash;
            else if (isAttackingS)
                xVel += xDecelAttack;
            else
                xVel = -xVelMax;
        } else if (!left && !right && xVel > 0) {
            xVel -= xDecel;
            if (xVel < 0) xVel = 0;
        } else if (!left && !right && xVel < 0) {
            xVel += xDecel;
            if (xVel > 0) xVel = 0;
        }
    }

    //  Decelerates enemies when necessary and limits velocity otherwise
    //  Return type: No return, updates global variables (void)
    //  Parameters: No parameters, all variables global
    public void manageSpeedEnemy() {
        for (int i = 0; i < xVelEnemy.size(); i++) {
            if (xVelEnemy.get(i) > xVelMaxEnemy)
                if (isKnockedBack.get(i)) {
                    xVelEnemy.set(i, xVelEnemy.get(i) - 0.2);
                    if (xVelEnemy.get(i) <= xVelMaxEnemy)
                        isKnockedBack.set(i, false);
                } else
                    xVelEnemy.set(i, xVelMaxEnemy);
            else if (xVelEnemy.get(i) < -xVelMaxEnemy)
                if (isKnockedBack.get(i)) {
                    xVelEnemy.set(i, xVelEnemy.get(i) + 0.2);
                    if (xVelEnemy.get(i) >= -xVelMaxEnemy)
                        isKnockedBack.set(i, false);
                } else
                    xVelEnemy.set(i, -xVelMaxEnemy);
        }
    }


    //  Removes all elements of index i from all enemy characteristic ArrayLists
    //  All successive indices are shifted automatically by ArrayList
    //  Return type: No return, updates global variables (void)
    //  Parameters: Index to be removed (int)
    public void removeEntity(int i) {
        xPosEnemy.remove(i);
        yPosEnemy.remove(i);
        xVelEnemy.remove(i);
        yVelEnemy.remove(i);
        directionEnemy.remove(i);
        hbEnemies.remove(i);
        hitpointsEnemy.remove(i);
        alreadyHit.remove(i);
        healthBars.remove(i);
        isKnockedBack.remove(i);
    }

    //  Prevents players, enemies, hitboxes, and health bars from falling through the floor
    //  Return type: No return, draws from method
    //  Parameters: No parameters, all variables global
    public void keepInBound() {
        //  Player
        if (yPos > floorHeight - playerHeight - spaceAbove) {
            yPos = floorHeight - playerHeight - spaceAbove;
            hbPlayer.y = floorHeight - playerHeight;
            yVel = 0;
        }
        //  Enemy
        for (int i = 0; i < yPosEnemy.size(); i++)
            if (yPosEnemy.get(i) > floorHeight - playerHeight - spaceAbove) {
                yPosEnemy.set(i, floorHeight - playerHeight - spaceAbove);
                hbEnemies.get(i).y = floorHeight - playerHeight;
                healthBars.get(i).y = floorHeight - playerHeight - 15;
                yVelEnemy.set(i, 0.0);
            }
    }

    //  Draws the dash attack animation frames and hitbox
    //  Return type: No return, draws from method
    //  Parameters: No parameters, all variables global
    public void dash() {
        //  Hitbox
        if (attackFrame >= 2) {
            if (direction == LEFT)
                hbAttack = new Rectangle((WINDOW_WIDTH - imageWidth) / 2, yPos + playerHeight * 345 / 200,
                        170, playerHeight * 35 / 200);
            else
                hbAttack = new Rectangle(WINDOW_WIDTH / 2, yPos + playerHeight * 345 / 200,
                        170, playerHeight * 35 / 200);
        }

        if (0 <= attackFrame && attackFrame <= 1) {
            drawPlayer(AttackC[directionToIndex(direction)][0]);
        } else if (2 <= attackFrame && attackFrame <= 4) {
            drawPlayer(AttackC[directionToIndex(direction)][1]);
        } else if (5 <= attackFrame && attackFrame <= 6) {
            drawPlayer(AttackC[directionToIndex(direction)][2]);
        } else {
            drawPlayer(AttackC[directionToIndex(direction)][3]);
        }
        attackFrame++;
    }

    //  Draws the special animation and hitbox
    //  Return type: No return, draws from method
    //  Parameters: No parameters, all variables global
    public void special() {
        left = false;
        right = false;
        //  Player animation
        if (0 <= attackFrame && attackFrame <= 1) {
            drawPlayer(AttackB[directionToIndex(direction)][0]);
            drawPlayer(AttackB[directionToIndex(-direction)][0]);
        } else if (2 <= attackFrame && attackFrame <= 4) {
            drawPlayer(AttackB[directionToIndex(direction)][1]);
            drawPlayer(AttackB[directionToIndex(-direction)][1]);
        } else if (5 <= attackFrame && attackFrame <= 6) {
            drawPlayer(AttackB[directionToIndex(direction)][2]);
            drawPlayer(AttackB[directionToIndex(-direction)][2]);
        } else {
            drawPlayer(AttackB[directionToIndex(direction)][3]);
            drawPlayer(AttackB[directionToIndex(-direction)][3]);
        }
        if (!isAirborne) {
            //  Hitbox
            hbAttack = new Rectangle(WINDOW_WIDTH / 2 - attackFrame * 7, floorHeight - playerHeight * 2,
                    attackFrame * 14, playerHeight * 2);
            //  Shockwave animation
            offScreenBuffer.drawImage(lShockwave, WINDOW_WIDTH / 2 - imageWidth * 3 - attackFrame * 7 + 720,
                    floorHeight - (playerHeight + spaceAbove) * 3, imageWidth * 3, imageHeight * 3, this);
            offScreenBuffer.drawImage(rShockwave, WINDOW_WIDTH / 2 + attackFrame * 7 - 720,
                    floorHeight - (playerHeight + spaceAbove) * 3, imageWidth * 3, imageHeight * 3, this);
        }
        attackFrame++;
        if (attackFrame >= 100)
            isSpecial = false;
    }

    //  Draws the slam animation and hitbox
    //  Return type: No return, draws from method
    //  Parameters: No parameters, all variables global
    public void slam() {
        if (isAirborne) {
            //  Hitbox
            hbAttack = new Rectangle(WINDOW_WIDTH / 2 - playerWidth, yPos + spaceAbove, playerWidth * 2, playerHeight * 2);
            if (direction == LEFT) {
                drawPlayer(lSlam);
            } else {
                drawPlayer(rSlam);
            }
        }
        //  Landing shockwave
        if (!isAirborne) {
            offScreenBuffer.drawImage(slamShockwave, (WINDOW_WIDTH - imageWidth) / 2,
                    floorHeight - playerHeight - spaceAbove, imageWidth, imageHeight, this);
            hbAttack = new Rectangle(WINDOW_WIDTH / 2 - 175, floorHeight - playerHeight, 350, playerHeight);
            attackFrame++;
            if (attackFrame >= 6)
                isSlamming = false;
        }
    }

    //  Draws the side attack animation frames and hitbox
    //  Return type: No return, draws from method
    //  Parameters: No parameters, all variables global
    public void attackS() {
        isSprinting = false;
        if (!isAirborne) {
            left = false;
            right = false;
        }

        //  Hitbox
        if (attackFrame >= 2) {
            if (direction == LEFT)
                hbAttack = new Arc2D.Double((WINDOW_WIDTH - playerHeight * 3) / 2, yPos + playerHeight * 50 / 200,
                        playerHeight * 3, playerHeight * 3, 120, 120, Arc2D.PIE);
            else
                hbAttack = new Arc2D.Double((WINDOW_WIDTH - playerHeight * 3) / 2, yPos + playerHeight * 50 / 200,
                        playerHeight * 3, playerHeight * 3, 300, 120, Arc2D.PIE);
        }

        if (0 <= attackFrame && attackFrame <= 1) {
            drawPlayer(AttackA[directionToIndex(direction)][0]);
        } else if (2 <= attackFrame && attackFrame <= 4) {
            drawPlayer(AttackA[directionToIndex(direction)][1]);
        } else if (5 <= attackFrame && attackFrame <= 6) {
            drawPlayer(AttackA[directionToIndex(direction)][2]);
        } else {
            clearAttackHitbox();
            drawPlayer(AttackA[directionToIndex(direction)][3]);
        }
        attackFrame++;
        if (attackFrame >= 20) {
            isAttackingS = false;
            attackFrame = 0;
        }
        if (!isAttackingS) {         //  Continues movement when attack ends
            if (leftHeld)
                left = true;
            else if (rightHeld)
                right = true;
        }
    }

    //  Draws the up attack animation frames and hitbox
    //  Return type: No return, draws from method
    //  Parameters: No parameters, all variables global
    public void attackU() {
        isSprinting = false;

        if (0 <= attackFrame && attackFrame <= 1) {
            drawPlayer(AttackD[directionToIndex(direction)][0]);
        } else if (2 <= attackFrame && attackFrame <= 4) {
            if (direction == LEFT)
                hbAttack = new Arc2D.Double((WINDOW_WIDTH - playerHeight * 3) / 2, yPos + playerHeight * 50 / 200,
                        playerHeight * 3, playerHeight * 3, 30, 150, Arc2D.PIE);
            else
                hbAttack = new Arc2D.Double((WINDOW_WIDTH - playerHeight * 3) / 2, yPos + playerHeight * 50 / 200,
                        playerHeight * 3, playerHeight * 3, 0, 150, Arc2D.PIE);
            drawPlayer(AttackD[directionToIndex(direction)][1]);
        } else if (5 <= attackFrame && attackFrame <= 6) {
            drawPlayer(AttackD[directionToIndex(direction)][2]);
        } else {
            clearAttackHitbox();
            drawPlayer(AttackD[directionToIndex(direction)][3]);
        }
        attackFrame++;
        if (attackFrame >= 20) {
            isAttackingU = false;
            attackFrame = 0;
        }
    }

    //  Draws the down attack animation frames and hitbox
    //  Return type: No return, draws from method
    //  Parameters: No parameters, all variables global
    public void attackD() {
        isSprinting = false;
        if (!isAirborne) {
            left = false;
            right = false;
        }

        //  Hitbox
        if (attackFrame >= 2 && attackFrame <= 6) {
            if (direction == LEFT)
                hbAttack = new Arc2D.Double((WINDOW_WIDTH - playerHeight * 750 / 200) / 2, yPos + spaceAbove + playerHeight - playerHeight * 375 / 200,
                        playerHeight * 750 / 200, playerHeight * 750 / 200, 90, 90, Arc2D.PIE);
            else
                hbAttack = new Arc2D.Double((WINDOW_WIDTH - playerHeight * 750 / 200) / 2, yPos + spaceAbove + playerHeight - playerHeight * 375 / 200,
                        playerHeight * 750 / 200, playerHeight * 750 / 200, 0, 90, Arc2D.PIE);
        }

        if (0 <= attackFrame && attackFrame <= 1) {
            drawPlayer(AttackB[directionToIndex(direction)][0]);
        } else if (2 <= attackFrame && attackFrame <= 4) {
            drawPlayer(AttackB[directionToIndex(direction)][1]);
        } else if (5 <= attackFrame && attackFrame <= 6) {
            drawPlayer(AttackB[directionToIndex(direction)][2]);
        } else {
            clearAttackHitbox();
            drawPlayer(AttackB[directionToIndex(direction)][3]);
        }
        attackFrame++;
        if (attackFrame >= 20 && !isAirborne) {
            isAttackingD = false;
            attackFrame = 0;
        }
        if (!isAttackingD) {         //  Continues movement when attack ends
            if (leftHeld)
                left = true;
            else if (rightHeld)
                right = true;
        }
    }

    //  "Clears" attack hitboxes by drawing them off-screen
    //  Return type: No return, all variables global
    //  Parameters: No parameters, all variables global
    public void clearAttackHitbox() {
        hbAttack = new Rectangle(-500, -500, 0, 0);
    }

    //  Draws the given player sprite at location updated by movePlayer()
    //  Return type: No return, draws from method
    //  Parameters: Image to be drawn (Image)
    public void drawPlayer(Image image) {
        offScreenBuffer.drawImage(image, (WINDOW_WIDTH - imageWidth) / 2,
                yPos, imageWidth, imageHeight, this);
    }

    //  Draws enemy sprites at location updated by moveEnemies()
    //  Return type: No return, draws from method
    //  Parameters: No parameters, all variables global
    public void drawEnemies() {
        for (int i = 0; i < xPosEnemy.size(); i++)
            offScreenBuffer.drawImage(Enemy[directionToIndex(directionEnemy.get(i))][tick / 10 % lEnemy.length], xPosEnemy.get(i),
                    yPosEnemy.get(i), imageWidth, imageHeight, this);
    }

    public void menu() {

    }

    //  Paint component method
    //  Return type: No return, draws from method
    //  Parameters: Graphics variable (Graphics)
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        // Set up the offscreen buffer the first time paint() is called
        if (offScreenBuffer == null) {
            offScreenImage = createImage(WINDOW_WIDTH, WINDOW_HEIGHT);
            offScreenBuffer = offScreenImage.getGraphics();
        }

        //  Background
        offScreenBuffer.drawImage(background, xPos, 0, WINDOW_WIDTH * 2, floorHeight, this);
        offScreenBuffer.drawImage(background, xPos + WINDOW_WIDTH * 2, 0, WINDOW_WIDTH * 2, floorHeight, this);
        offScreenBuffer.drawImage(background, xPos - WINDOW_WIDTH * 2, 0, WINDOW_WIDTH * 2, floorHeight, this);
        //  Floor
        offScreenBuffer.drawImage(floor, xPos, floorHeight - 75, WINDOW_WIDTH * 2, WINDOW_HEIGHT - floorHeight + 75, this);
        offScreenBuffer.drawImage(floor, xPos + WINDOW_WIDTH * 2, floorHeight - 75, WINDOW_WIDTH * 2, WINDOW_HEIGHT - floorHeight + 75, this);
        offScreenBuffer.drawImage(floor, xPos - WINDOW_WIDTH * 2, floorHeight - 75, WINDOW_WIDTH * 2, WINDOW_HEIGHT - floorHeight + 75, this);

        //  Player
        if (!inAction) {
            attackFrame = 0;
            if (isAirborne) {
                hbPlayer = hbStanding;
                drawPlayer(Jump[directionToIndex(direction)]);
            } else if (isSprinting) {
                if (direction == LEFT)
                    hbPlayer = new Rectangle(WINDOW_WIDTH / 2 - playerHeight * 125 / 200,
                            yPos + spaceAbove, playerHeight * 125 / 200, playerHeight);
                else if (direction == RIGHT)
                    hbPlayer = new Rectangle(WINDOW_WIDTH / 2,
                            yPos + spaceAbove, playerHeight * 125 / 200, playerHeight);
                drawPlayer(Sprint[directionToIndex(direction)][tick / 10 % lSprint.length]);
            } else {
                hbPlayer = hbStanding;
                drawPlayer(Stand[directionToIndex(direction)]);
            }
        }

        //  Enemies
        drawEnemies();

        //  Dash
        if (isDashing)
            dash();

        //  Special
        if (isSpecial)
            special();

        //  Slam
        if (isSlamming)
            slam();

        //  Side Attack
        if (isAttackingS && !isDashing)
            attackS();

        //  Up Attack
        if (isAttackingU && !isDashing)
            attackU();

        //  Down Attack
        if (isAttackingD && !isDashing)
            attackD();

        //  Moves offScreenImage onto screen
        g.drawImage(offScreenImage, 0, 0, this);

        //  Player health bar
        g.drawImage(heart, 25, 20, 75, 75, this);
        g2.setColor(Color.WHITE);
        g2.fillRect(110, 50, 500, 25);
        g2.setColor(Color.RED);
        g2.fillRect(110, 50, hitpointsPlayer * 50, 25);
        Stroke stroke = g2.getStroke();
        g2.setStroke(new BasicStroke(5));
        g2.setColor(Color.BLACK);
        g2.drawRect(110, 50, 500, 25);
        g2.setStroke(stroke);

        //  Enemy health bars
        g2.setColor(Color.ORANGE);
        for (int i = 0; i < healthBars.size(); i++)
            g2.fill(healthBars.get(i));

        //  Draw hitbox
        if (showHitboxes) {
            g2.setColor(Color.GREEN);
            g2.fill(hbPlayer);
            g2.setColor(Color.CYAN);
            g2.fill(hbAttack);
            g2.setColor(Color.MAGENTA);
            for (int i = 0; i < hbEnemies.size(); i++)
                g2.fill(hbEnemies.get(i));
        }
    }
}