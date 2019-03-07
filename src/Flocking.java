import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Flocking extends PApplet {

Flock flock;
boolean highlight = false;
PVector mouse_enemy = new PVector(0, 0);
int MOUSE_SEPARATION = 30;
int counter = 201;
PImage boid_image;

public void setup() {
  
  frameRate(30);
  
  boid_image = loadImage("bird.png");
  flock = new Flock();
  // Add an initial set of boids into the system
  for (int i = 0; i < 200; i++) {
    flock.addBoid(new Boid(i, width/2, height/2, boid_image));
  }
}

public void draw() {
  background(50);
  flock.run();
}

// tested method
public void keyPressed() {
  if ( key == 'a') {
   flock.addBoid(new Boid(counter, width/2, height/2, boid_image)); 
   counter++;
  }
}
// tested method
public void mousePressed() {
  if (mouseButton == LEFT) {
    if (mouse_enemy.x == 0 && mouse_enemy.y == 0) {
      mouse_enemy.x = mouseX;
      mouse_enemy.y = mouseY;
    } else {
      mouse_enemy = new PVector(0, 0);
    }
  } else if (mouseButton == RIGHT) {
    highlight = !highlight;
  }
}
class Boid {

  PVector position;
  PVector velocity;
  PVector acceleration;
  float r;
  float maxforce;    // Maximum steering force
  float maxspeed;    // Maximum speed
  int id;
  float desiredseparation = 25.0f;
  float neighbordist = 50;
  PImage boid_image;

  Boid(int id, float x, float y, PImage img) {
    acceleration = new PVector(0, 0);
    float angle = random(TWO_PI);
    velocity = new PVector(cos(angle), sin(angle));

    position = new PVector(x, y);

    this.id = id;
    this.boid_image = img;

    r = 16.0f;
    maxspeed = 2;
    maxforce = 0.03f;
  }

  public void run(ArrayList<Boid> boids) {
    flock(boids);
    update();
    borders();
    render();
  }

  public void applyForce(PVector force) {
    // We could add mass here if we want A = F / M
    acceleration.add(force);
  }

  // We accumulate a new acceleration each time based on three rules
  public void flock(ArrayList<Boid> boids) {
    PVector sep = separate(boids);   // Separation
    PVector ali = align(boids);      // Alignment
    PVector coh = cohesion(boids);   // Cohesion
    PVector mos = mouse();
    // Arbitrarily weight these forces
    sep.mult(1.0f);
    ali.mult(0.4f);
    coh.mult(0.2f);
    mos.mult(3.0f);
    // Add the force vectors to acceleration
    applyForce(sep);
    applyForce(ali);
    applyForce(coh);
    applyForce(mos);
  }

  // Method to update position
  public void update() {
    // Update velocity
    velocity.add(acceleration);
    // Limit speed
    velocity.limit(maxspeed);
    position.add(velocity);
    // Reset accelertion to 0 each cycle
    acceleration.mult(0);
  }

  public PVector mouse () {
    PVector steer = new PVector(0, 0);

      float d = PVector.dist(this.position, mouse_enemy);
      if ((d > 0) && (d < MOUSE_SEPARATION*2)) {
        PVector diff = PVector.sub(this.position, mouse_enemy);
        diff.normalize();
        diff.div(d);       
        steer.add(diff);
      }

    if (steer.mag() > 0) {
      steer.normalize();
      steer.mult(maxspeed);
      steer.sub(velocity);
      steer.limit(maxforce);
    }
    return steer;
  }

  public PVector seek(PVector target) {
    PVector desired = PVector.sub(target, position); 

    desired.normalize();
    desired.mult(maxspeed);

    PVector steer = PVector.sub(desired, velocity);
    steer.limit(maxforce); 
    return steer;
  }

  public void render() {
    float theta = velocity.heading(); //+ radians(90);

    fill(200, 100);
    stroke(255);
    pushMatrix();
    translate(position.x, position.y);
    rotate(theta);
    image(boid_image, -r/2, -r/2);
    popMatrix();

    noFill();
    if (highlight && id == 0) {
      stroke(0, 0, 0);
      ellipse(position.x, position.y, neighbordist*2, neighbordist*2); // neighbour-radius
      stroke(255, 0, 255);
      line(position.x, position.y, position.x+velocity.x*20, position.y+velocity.y*20);
      stroke(255, 0, 0);
      ellipse(position.x, position.y, desiredseparation*2, desiredseparation*2); // separation-radius
    }

    if (mouse_enemy.mag() > 0) {
      stroke(255, 153, 0);
      ellipse(mouse_enemy.x, mouse_enemy.y, MOUSE_SEPARATION*2, MOUSE_SEPARATION*2);
    }
  }

  // Wraparound
  public void borders() {
    if (position.x < -r) position.x = width+r;
    if (position.y < -r) position.y = height+r;
    if (position.x > width+r) position.x = -r;
    if (position.y > height+r) position.y = -r;
  }

  public PVector separate (ArrayList<Boid> boids) {
    PVector steer = new PVector(0, 0, 0);
    int count = 0;
    for (Boid other : boids) {
      float d = PVector.dist(position, other.position);
      if ((d > 0) && (d < desiredseparation)) {
        PVector diff = PVector.sub(position, other.position);
        diff.normalize();
        diff.div(d);       
        steer.add(diff);
        count++;           
      }
    }
    
    if (count > 0) {
      steer.div((float)count);
    }

    if (steer.mag() > 0) {
      steer.normalize();
      steer.mult(maxspeed);
      steer.sub(velocity);
      steer.limit(maxforce);
    }
    return steer;
  }

  public PVector align (ArrayList<Boid> boids) {
    PVector sum = new PVector(0, 0);
    int count = 0;
    for (Boid other : boids) {
      float d = PVector.dist(position, other.position);
      if ((d > 0) && (d < neighbordist)) {
        sum.add(other.velocity);
        count++;
      }
    }
    if (count > 0) {
      sum.div((float)count);
      sum.normalize();
      sum.mult(maxspeed);
      PVector steer = PVector.sub(sum, velocity);
      steer.limit(maxforce);
      return steer;
    } else {
      return new PVector(0, 0);
    }
  }

  public PVector cohesion (ArrayList<Boid> boids) {
    PVector sum = new PVector(0, 0);
    int count = 0;
    for (Boid other : boids) {
      float d = PVector.dist(position, other.position);
      if ((d > 0) && (d < neighbordist)) {
        sum.add(other.position);
        count++;
      }
    }
    if (count > 0) {
      sum.div(count);
      return seek(sum);
    } else {
      return new PVector(0, 0);
    }
  }
}
static class Flock {
  ArrayList<Boid> boids;

  Flock() {
    boids = new ArrayList<Boid>();
  }

  public void run() {
    for (Boid b : boids) {
      b.run(boids);
    }
  }

  public void addBoid(Boid b) {
    boids.add(b);
  }

}
  public void settings() {  size(1200, 600);  smooth(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--present", "--window-color=#666666", "--stop-color=#cccccc", "Flocking" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
