package scripts;

import org.tribot.api.Clicking;
import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api.util.ABCUtil;
import org.tribot.api2007.*;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Painting;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

@ScriptManifest(authors = { "BizarreAlex" }, category = "Ranged", name = "Better Cannon Clicker (BCC)", version = 1.0, description = "A better cannon clicker", gameMode = 1)
public class BCC extends Script implements Painting {

    private final Image PAINT_BG = getImage("https://i.gyazo.com/5271540431bacca56966a466a8e4919a.png");

    private boolean started = false;
    private RSObject cannon = null;
    private long time = 0;
    private ABCUtil util = new ABCUtil();
    private String status = "N/A";
    private int cannonballs;
    private long startTime = System.currentTimeMillis();
    private int startXP = Skills.getXP(Skills.SKILLS.RANGED);
    private int startLevel = Skills.getCurrentLevel(Skills.SKILLS.RANGED);
    private long xpPerHour;

    private boolean placeCannon() {
        System.out.println("Checking for cannon base..");
        RSItem[] data = Inventory.find("Cannon base");

        if (data.length == 0) {
            System.out.println("Cannon base not found in inventory!");
            return false;
        }

        int x = Player.getPosition().getX();
        int y = Player.getPosition().getY();

        cannonballs = Inventory.getCount("Cannonball");

        if (Clicking.click(data)) {
            Timing.waitCondition(new Condition() {
                @Override
                public boolean active() {
                    return Inventory.find("Cannon furnace").length == 0;
                }
            }, General.random(12000, 20000));

            Walking.clickTileMS(new RSTile(x, y), "Walk here");

            return Timing.waitCondition(new Condition() {
                @Override
                public boolean active() {
                    return Player.getPosition().getX() == x && Player.getPosition().getY() == y;
                }
            }, General.random(2000, 5000));
        }

        return false;
    }

    private boolean repairCannon() {
        if (cannon != null) {
            RSObject[] objects = Objects.findNearest(15, 5);
            if (objects.length > 0) {
                System.out.println("Found a broken cannon..");
                RSObject object = objects[0];
                if (object.getPosition().getX() == cannon.getPosition().getX() && object.getPosition().getY() == cannon.getPosition().getY()) {
                    System.out.println("Shit, it looks like ours.. Let's try to repair it.");
                    status = "Repairing cannon";
                    return DynamicClicking.clickRSObject(object, "Repair");
                }
            }
        }
        return false;
    }

    private boolean loadCannon() {
        if (cannon == null) {
            status = "Searching";
            RSObject[] objects = Objects.findNearest(15, 6);

            if (objects.length == 0) {
                System.out.println("Cannon object not found!");
                return false;
            }

            cannon = objects[0];
        }

        status = "Loading cannon";
        System.out.println("Loading cannon..");
        return DynamicClicking.clickRSObject(cannon, "Fire");
    }

    private Image getImage(String url) {
        try {
            return ImageIO.read(new URL(url));
        } catch(IOException e) {
            return null;
        }
    }

    @Override
    public void run() {
        while (true) {
            int gainedXP = Skills.getXP(Skills.SKILLS.RANGED) - startXP;
            long timeRan = System.currentTimeMillis() - startTime;

            sleep(100);

            xpPerHour =  (long)(gainedXP * 3600000d / timeRan);

            if (!(started)) {
                started = true;

                General.useAntiBanCompliance(true);

                status = "Starting";
                System.out.println("Starting base functions...");

                status = "Placing cannon";
                if (!(placeCannon())) {
                    System.out.println("Failed to place cannon, script stopping.");
                    break;
                }

                sleep(General.random(500, 1500));

                status = "Loading cannon";
                if (!(loadCannon())) {
                    System.out.println("Failed to load cannon and establish cached object, script stopping.");
                    break;
                }

                status = "Chilling";
                continue;
            }

            if (time >= General.random(500, 1000)) {
                if (!(loadCannon())) {
                    System.out.println("Failed to load cannon.. Let's see if it's broken.");
                    if (!(repairCannon())) {
                        System.out.println("lol something fucking broke");
                        break;
                    }
                }
                time = 0;
            } else {
                status = "Chilling";
                if (time >= 0 && time <= 50) {
                    util.performRotateCamera();
                } else {
                    util.performRandomMouseMovement();
                }
            }

            time++;
        }
    }

    @Override
    public void onPaint(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics;
        graphics2D.drawImage(PAINT_BG, 0, 237, null);

        graphics2D.setColor(Color.YELLOW);
        graphics2D.drawString("Status: " + status + "..", 140, 290);
        graphics2D.drawString("XP/H: " + xpPerHour + "..", 140, 305);
        graphics2D.drawString("Cannonballs Used: " + (!started ? 0 : cannonballs - Inventory.getCount("Cannonball")) + "..", 140, 320);

    }
}
