import com.jogamp.newt.event.InputEvent;

import java.awt.*;
import java.awt.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.*;

class FlockingTest {

    @org.junit.jupiter.api.Test

    void testKeyPressed() {
        Flocking.Flock flock = new Flocking.Flock();
        Flocking flocking = new Flocking();
        // get count before method was executed
        int boidsCount = flock.boids.size();


        int oldCount = flocking.counter;
        System.out.println(oldCount);


        try {
            Robot robot = new Robot();

            // Simulate a mouse click
            //robot.mousePress(InputEvent.BUTTON1_MASK);
            //robot.mouseRelease(InputEvent.BUTTON1_MASK);

            // Simulate a key press
            robot.keyPress(KeyEvent.VK_A);
            robot.keyRelease(KeyEvent.VK_A);



            assertEquals(oldCount+1,flocking.counter);

        } catch (AWTException e) {
            System.out.println(e);
        }


        flocking.keyPressed();
        // one boid was created;
        assertEquals(1,boidsCount+1);

        // todo: geht das iwie anders?

    }

    @org.junit.jupiter.api.Test
    void testMousePressed() {

/*        try{

            Robot robot = new Robot();
            // mouse move
            //robot.mouseMove(x,y);// x,y are cordinates
            // Simulate a mouse click
            Robot bot = new Robot();
            bot.mousePress(InputEvent.BUTTON1_MASK);
            bot.mouseRelease(InputEvent.BUTTON1_MASK);

            // Simulate a key press
           // robot.keyPress(KeyEvent.VK_A);
           // robot.keyRelease(KeyEvent.VK_A);

        } catch (AWTException e) {
            e.printStackTrace();
        }*/
    }
}