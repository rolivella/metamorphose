import processing.core.*; 
import processing.xml.*; 

import lsystem.*; 
import lsystem.turtle.*; 
import lsystem.collection.*; 
import oscP5.*; 
import netP5.*; 
import oscP5.*; 
import netP5.*; 

import oscP5.*; 
import netP5.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class Metamorphose_PD extends PApplet {








OscP5 oscP5;
NetAddress myRemoteLocation;

/*
* Roger Olivella
* Juny 2011
* Juliol 2011 - PD
*/

int llindarTolerancia=1;
boolean modeKey=false;
int numReadings = 10;

//ratio respecto al 160 * 120 al que queremos dibujar 
int ratio = 2;
//Objeto mouTe
mouTe mt = new mouTe(ratio, 7001, 7002, 7003, 7004);

//Par\u00e0metres caixa:
float x1;
float y1;
float x2;
float y2;
float boxHeight;
float boxWidth;
float boxArea10;
float boxArea180;

//Par\u00e0metres cursor: 
float xCursor;

ArrayList points;
float DELTA; 
float PHI;
float startLength;
Grammar grammar; 
String axiom;
String rule;
String production;
float drawLength;
float theta;
float xpos;
float ypos;
TurtleStack ts;

//Average: 

int index=0;
int indexAverageCeldasActivas=0;

float[] readingsXCursor = new float[numReadings];
float[] readingsBoxHeight = new float[numReadings];
float[] readingsBoxWidth = new float[numReadings];
float[] readingsTotalCeldasActivas = new float[numReadings];

float totalXCursor = 0;
float totalBoxHeight = 0;
float totalBoxWidth = 0;
float totalCeldasActivas=0;

float averageXCursor = 0; 
float averageBoxHeight = 0; 
float averageBoxWidth = 0; 
float averageBoxArea=0;
float averageCeldasActivas=0;

String[] rules = new String[5];
int randomRule;

int generations;

float[] celdasActivas = new float[3];
int indexCeldasActivas=0;
boolean flagMakePhoto=false;
//int llindarToleranciaMovimentCaixa=100;



//PImage imgStart;
//PImage imgCrop;
//PImage imgHerbari;
//PImage imgHerbariDisplayUser;

boolean flagTop=false;
boolean flagInitLeft=false;
boolean flagInitRight=false;
int yTop;
int xLeft;
int xRight;
String now;

int lastMillis=0;
boolean flagHerbariumPageCreated=false;

int nowSeconds;

float umbral;
boolean bang;
float prevMillis;
int valueKey = 0;

public void setup() {
  size(1024, 768);
  PFont font;
  font = loadFont("ArialMT-20.vlw"); 
  textFont(font);
  
  //Reinicia los arrays de la running average:
  for (int thisReading = 0; thisReading < numReadings; thisReading++){
    readingsXCursor[thisReading] = 0;
    readingsBoxHeight[thisReading] = 0;
    readingsBoxWidth[thisReading] = 0;
  }
  
  //
  randomRule=(int)random(0,5);
  rules[0] = "F[+F]F[-F]F";
  rules[1] = "F[+F]F[-F][F]";
  rules[2] = "FF-[-F+F+F]+[+F-F-F]";
  rules[3] = "F[+FF][-FF]F[+FF][-FF]F";
  rules[4] = "F[+F[+F][-F]F][-F[+F][-F]F]F[+F][-F]F";
  
  /* start oscP5, listening for incoming messages at port 12000 */
  oscP5 = new OscP5(this,12000);
  
  /* myRemoteLocation is a NetAddress. a NetAddress takes 2 parameters,
   * an ip address and a port number. myRemoteLocation is used as parameter in
   * oscP5.send() when sending osc packets to another computer, device, 
   * application. usage see below. for testing purposes the listening port
   * and the port of the remote location address are the same, hence you will
   * send messages back to this sketch.
   */
  myRemoteLocation = new NetAddress("127.0.0.1",12001);

}

public void draw() {
   
      if(flagMakePhoto){
        
        if(!flagHerbariumPageCreated){
          createHerbariumPage();//generaci\u00f3 p\u00e0gina herbari
          lastMillis=millis();
          flagHerbariumPageCreated=true;
        }
        
        //Mostra el resultat de la p\u00e0gina herbari a l'usuari: 
        PImage imgHerbariDisplayUser=loadImage("./herbari/herbari-"+now+".jpg");
        imgHerbariDisplayUser.resize(300,0);
        //posar aqui una imatge blanca de fons???  
        image(imgHerbariDisplayUser,20,height-imgHerbariDisplayUser.height-20);
        
        //Delay a mano: 
        if(millis() > (lastMillis+2000)){
          flagHerbariumPageCreated=false;
          flagMakePhoto=false;
          lastMillis=0;
          celdasActivas[0]=0;
          celdasActivas[1]=0;
          celdasActivas[2]=0;
          indexCeldasActivas=0;
          randomRule=(int)random(0,5);
        }
        
        //noLoop();
 
              
      } else if(!flagMakePhoto){
        
        background(255);  
        
        drawGrid();
        
        //Mapping Mouse: 
        /*
        PHI=map(mouseX,0,width,180,0);//<-----------------posici\u00f3 X cursor
        //DELTA=PI/(map(mouseX,0,width,3,35)); //<-----------------amplada caixa
        DELTA=PI/15;
        startLength = map(mouseY,0,height,65,5);//<-----------------altura caixa
        */
        
        //Amb Moute: 
        x1=mt.cC[0];//x1: upper left X
        y1=mt.cC[1];//y1: upper left Y
        x2=mt.cC[2];//x2: lower right X
        y2=mt.cC[3];//y2: lower right Y
        //Posici\u00f3 X cursor: 
        xCursor=(x1+(x2-x1)/2);
        //Altura caixa: 
        boxHeight=y2-y1;//
        //Amplada caixa
        boxWidth=x2-x1;
        
        //Running average (per evitar vibraci\u00f3 caixa):
        totalXCursor= totalXCursor - readingsXCursor[index];    
        totalBoxHeight= totalBoxHeight - readingsBoxHeight[index];     
        totalBoxWidth= totalBoxWidth - readingsBoxWidth[index];
        readingsXCursor[index] = xCursor; 
        readingsBoxHeight[index] = boxHeight;
        readingsBoxWidth[index] = boxWidth;
        totalXCursor= totalXCursor + readingsXCursor[index];       
        totalBoxHeight= totalBoxHeight + readingsBoxHeight[index];
        totalBoxWidth= totalBoxWidth + readingsBoxWidth[index];
        index = index + 1;                    
      
        if (index >= numReadings){              
          index = 0;                           
        }
        
        averageXCursor = totalXCursor / numReadings;
        averageBoxHeight = totalBoxHeight / numReadings;
        averageBoxWidth = totalBoxWidth / numReadings;
              
        ////////////////////////////////////////////////////////////////////////////////
        //Mapping Moute: ///////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////
        randomRule=0;//ULLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL
        if(randomRule==0){
          //generations=6;
          generations=4;
          if(averageXCursor==-1){//Posici\u00f3 per defecte
            PHI=90;
            DELTA=60;
            startLength=30;
          //} else if (averageBoxWidth>=90){//Per evitar artefactes amb l'angle DELTA
          //  averageBoxWidth=80;
          } else {
            PHI=map(averageXCursor, 15, 160, 120, 60);//<-----------------posici\u00f3 X cursor
            DELTA=map(averageBoxWidth, 50, 120, 12, 90); //<----------amplada caixa
            startLength = map(averageBoxHeight, 45, 90, 5, 25);//<-------altura caixa
          }
        } else if(randomRule==1){
          generations=4;
          if(averageXCursor==-1){//Posici\u00f3 per defecte
            PHI=90;
            DELTA=60;
            startLength=80;
          //} else if (averageBoxWidth>=80){//Per evitar artefactes amb l'angle DELTA
          //  averageBoxWidth=80;
          } else {
            PHI=map(averageXCursor, 15, 160, 120, 60);//<-----------------posici\u00f3 X cursor
            DELTA=map(averageBoxWidth, 50, 120, 12, 90); //<----------amplada caixa
            startLength = map(averageBoxHeight, 45, 90, 5, 65);//<-------altura caixa
          }
        } else if(randomRule==2){
          generations=4;
          if(averageXCursor==-1){//Posici\u00f3 per defecte
            PHI=90;
            DELTA=30;
            startLength=50;
          //} else if (averageBoxWidth>=80){//Per evitar artefactes amb l'angle DELTA
          //  averageBoxWidth=80;
          } else {
            PHI=map(averageXCursor, 15, 160, 120, 60);//<-----------------posici\u00f3 X cursor
            DELTA=map(averageBoxWidth, 50, 120, 8, 90); //<----------amplada caixa
            startLength = map(averageBoxHeight, 45, 90, 5, 35);//<-------altura caixa
          }
        } else if(randomRule==3){
          generations=3;
          if(averageXCursor==-1){//Posici\u00f3 per defecte
            PHI=90;
            DELTA=40;
            startLength=50;
          //} else if (averageBoxWidth>=80){//Per evitar artefactes amb l'angle DELTA
          //  averageBoxWidth=80;
          } else {
            PHI=map(averageXCursor, 15, 160, 120, 60);//<-----------------posici\u00f3 X cursor
            DELTA=map(averageBoxWidth, 50, 120, 10, 90); //<----------amplada caixa
            startLength = map(averageBoxHeight, 45, 90, 5, 35);//<-------altura caixa
          }
        } else if(randomRule==4){
          generations=3;
          if(averageXCursor==-1){//Posici\u00f3 per defecte
            PHI=90;
            DELTA=40;
            startLength=60;
          //} else if (averageBoxWidth>=80){//Per evitar artefactes amb l'angle DELTA
          //  averageBoxWidth=80;
          } else {
            PHI=map(averageXCursor, 15, 160, 120, 60);//<-----------------posici\u00f3 X cursor
            DELTA=map(averageBoxWidth, 50, 120, 6, 90); //<----------amplada caixa
            startLength = map(averageBoxHeight, 45, 90, 5, 50);//<-------altura caixa
          }
        }
        ////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////
      
   
        //Comunicaci\u00f3 amb PD: 
    
        OscMessage ftype = new OscMessage("/ftype");//Quin tipus de fractal estem
        ftype.add(randomRule); /* add an int to the osc message */
        oscP5.send(ftype, myRemoteLocation); /* send the message */
        
        OscMessage oscxcursor = new OscMessage("/xcursor");//Quin tipus de fractal estem
        oscxcursor.add(averageXCursor); /* add an int to the osc message */
        oscP5.send(oscxcursor, myRemoteLocation); /* send the message */
        
        OscMessage oscboxwidth = new OscMessage("/boxwidth");//Quin tipus de fractal estem
        oscboxwidth.add(averageBoxWidth); /* add an int to the osc message */
        oscP5.send(oscboxwidth, myRemoteLocation); /* send the message */
        
        OscMessage oscboxheight = new OscMessage("/boxheight");//Quin tipus de fractal estem
        oscboxheight.add(averageBoxHeight); /* add an int to the osc message */
        oscP5.send(oscboxheight, myRemoteLocation); /* send the message */
        
        
        //Recuadre superior esquerra de text:
        fill(210);
        text("Axioma: F", 15, 90);
        text("Regla: "+rules[randomRule], 15, 120);
        //text("Al\u00e7ada: "+round(averageBoxHeight), 15, 90);
        //text("Amplada: "+round(averageBoxWidth), 15, 120);
        text("Longitud: "+round(startLength), 15, 150);
        text("PHI: "+round(PHI), 15, 180);
        text("DELTA: "+round(DELTA), 15, 210);//
        noFill();
      
        createLSystem();
        points = new ArrayList();
        ts = new TurtleStack(this);
        //stroke(0);
        //strokeWeight(2);
        //noFill();
        //smooth();
        translateRules();
      
        //Dibuixa la planta
        float[] point;
        for (int i = 0; i < points.size(); i++) {
          point = (float[])points.get(i);
          stroke(0);
          line(point[0], point[1], point[2], point[3]);
        }
             
        
        if(!modeKey){
          //Snapshot del fractal quan l'usuari es queda quiet:     
          if(millis()%1000==0){//cada segon...
          //if(timer(1000)){
            /*
            totalCeldasActivas= totalCeldasActivas - readingsTotalCeldasActivas[indexAverageCeldasActivas];    
            readingsTotalCeldasActivas[indexAverageCeldasActivas] = mt.totalActivas; 
            totalCeldasActivas= totalCeldasActivas + readingsTotalCeldasActivas[indexAverageCeldasActivas];       
            indexAverageCeldasActivas = indexAverageCeldasActivas + 1;                    
            if (indexAverageCeldasActivas >= numReadings){              
              indexAverageCeldasActivas = 0;                           
            }        
            averageCeldasActivas = totalCeldasActivas / numReadings;
            celdasActivas[indexCeldasActivas]=averageCeldasActivas;
            */
            celdasActivas[indexCeldasActivas]=mt.totalActivas;
            
            if(indexCeldasActivas==2){//fa la foto al cap de tres segons si el num. de cel\u00b7les \u00e9s el mateix...
              float restaCeldasActivasA=abs(celdasActivas[0]-celdasActivas[1]);
              float restaCeldasActivasB=abs(celdasActivas[1]-celdasActivas[2]); 
              if(restaCeldasActivasA <= llindarTolerancia && restaCeldasActivasB <= llindarTolerancia && averageXCursor!=-1){
                flagMakePhoto=true;
              } else {
                indexCeldasActivas=0;
                celdasActivas[0]=0;
                celdasActivas[1]=0;
                celdasActivas[2]=0;
              }
            } else {
              indexCeldasActivas=indexCeldasActivas+1;
            }
          }        
        }
                
       
      }//if flag photo

}//end draw







public void createLSystem() {

  //int generations = 4;
  // set no of recursions
  String axiom = "F";  
  grammar = new StochasticGrammar(this, axiom);  // initialize library

  grammar.addRule('F', rules[randomRule],1); // 1.24a
  //grammar.addRule('F', "F[+F]F[-F]F",1); // 1.24a
  //grammar.addRule('F', "F[+F]F[-F][F]", 0.2); // 1.24b
  //grammar.addRule('F', "FF-[-F+F+F]+[+F-F-F]", 0.1); // 1.24c
  //grammar.addRule('F', "F[+F]F", 0.45);
  //grammar.addRule('F', "F[-F]F", 0.45);

  production = grammar.createGrammar(generations);
  drawLength = startLength * pow(0.7f, generations);
}

public void translateRules() {
  float x_temp, y_temp;
  Turtle turtle = new Turtle(width/2, height, radians(PHI));
  //Turtle turtle = new Turtle(0.4 * height, 0.98 * width, QUARTER_PI);
  CharacterIterator it = grammar.getIterator(production);
  for (char ch = it.first(); ch != CharacterIterator.DONE; ch = it.next()) {
    switch (ch) {
    case 'F':
      x_temp = turtle.getX();
      y_temp = turtle.getY();
      turtle.setX(x_temp + drawLength * cos(turtle.getTheta()));
      turtle.setY(y_temp - drawLength * sin(turtle.getTheta()));
      float[] temp = {
        x_temp, y_temp, turtle.getX(), turtle.getY()
      };
      points.add(temp);
      break;
    case '+':
      //println("theta: "+turtle.getTheta());
      turtle.setTheta(turtle.getTheta() + radians(DELTA));
      break;
    case '-':
      turtle.setTheta(turtle.getTheta() - radians(DELTA));
      break;
    case '[':
      ts.push(turtle.clone()); // shallow copy is OK here really if you are worried use copy constructor
      // ts.push(new Turtle(turtle)); // Uncomment this (and comment previous line to avoid using clone) 
      break;
    case ']':
      turtle = ts.pop();
      break;
    default:
      System.err.println("character " + ch + " not in grammar");
    }
  }
}//end translaterules

public void createHerbariumPage(){
  
    now=str(year())+str(month())+str(day())+str(hour())+str(minute())+str(second());
   
    saveFrame("displaywindow.png");
    PImage imgStart = loadImage("displaywindow.png");
    PImage imgHerbari = loadImage("oldpaper2.jpg");//la 2, molt comprimida

    imgStart.loadPixels(); 
    //C\u00e0lcul \u00e0rea de NOM\u00c9S la planta:
    for (int y = 0; y < imgStart.height; y++) {
      for (int x = 0; x < imgStart.width; x++) {
        int loc = x + y*imgStart.width;     
        float r = red(imgStart.pixels[loc]);
        float g = green(imgStart.pixels[loc]);
        float b = blue(imgStart.pixels[loc]);
  
        if (r==0 && g==0 && b==0) {
          //Extreu el pixel negre de m\u00e9s amunt: 
          if (flagTop==false) {
            yTop=y;
            flagTop=true;
          }
          //Extreu el pixel negre de m\u00e9s a la esquerra:
          if (!flagInitLeft) {
            xLeft=x;  
            flagInitLeft=true;
          } 
          else if (flagInitLeft) {
            if (x<xLeft) {
              xLeft=x;
            }
          }
          //Extreu el pixel negre de m\u00e9s a la dreta:
          if (!flagInitRight) {
            xRight=x;  
            flagInitRight=true;
          } 
          else if (flagInitRight) {
            if (x>xRight) {
              xRight=x;
            }
          }
        }//if rgb
      }//for
    }//for
  
    //Faig el crop de NOM\u00c9S la planta: 
    PImage imgCrop=imgStart.get(xLeft, yTop, xRight-xLeft, imgStart.height-yTop);
    //imgCrop.save("crop-"+now+".jpg");   
 
    //Convertim el fractal en "planta": 
    imgCrop.loadPixels(); 
    for (int y = 0; y < imgCrop.height; y++) {
      for (int x = 0; x < imgCrop.width; x++) {
        
        int loc = x + y*imgCrop.width;
  
        // The functions red(), green(), and blue() pull out the 3 color components from a pixel.
        float r = red(imgCrop.pixels[loc]);
        float g = green(imgCrop.pixels[loc]);
        float b = blue(imgCrop.pixels[loc]);
  
        if (r==0 && g==0 && b==0) {
          imgCrop.pixels[loc]=color(0, 255, 0);//verd
          //imgCrop.pixels[loc+1]=color(0, 150, 0);
          //imgCrop.pixels[loc+2]=color(0, 150, 0);
          //imgCrop.pixels[loc-1]=color(0, 150, 0);
          //imgCrop.pixels[loc-2]=color(0, 150, 0);
        } 
        else {
          imgCrop.pixels[loc]=color(255, 255, 255);//blanc
        }
      }
    }//end for
    
    //El superposem al paper de l'herbari: 
    imgHerbari.blend(imgCrop,0,0,imgCrop.width,imgCrop.height,(imgHerbari.width/2)-(imgCrop.width/2), (imgHerbari.height/2)-(imgCrop.height/2), imgCrop.width, imgCrop.height, MULTIPLY);

    //Guardem la p\u00e0gina de l'herbari
    imgHerbari.save("./herbari/herbari-"+now+".jpg");
    
    //Inicialitzem vars.: 
    flagInitLeft=false;
    flagInitRight=false;
    flagTop=false;
    
}

public void keyPressed() {
  flagMakePhoto=true; 
}

public void drawGrid() {
    stroke(240);
    line(0,0,width,0);
    line(0,50,width,50);
    line(0,100,width,100);
    line(0,150,width,150);
    line(0,200,width,200);
    line(0,250,width,250);
    line(0,300,width,300);
    line(0,350,width,350);
    line(0,400,width,400);
    line(0,450,width,450);
    line(0,500,width,500);
    line(0,550,width,550);
    line(0,600,width,600);
    line(0,650,width,650);
    line(0,700,width,700);
    line(0,750,width,750);
    
    line(0,0,0,height);
    line(50,0,50,height);
    line(100,0,100,height);
    line(150,0,150,height);
    line(200,0,200,height);
    line(250,0,250,height);
    line(300,0,300,height);
    line(350,0,350,height);
    line(400,0,400,height);
    line(450,0,450,height);
    line(500,0,500,height);
    line(550,0,550,height);
    line(600,0,600,height);
    line(650,0,650,height);
    line(700,0,700,height);
    line(750,0,750,height);
    line(800,0,800,height);
    line(850,0,850,height);
    line(900,0,900,height);
    line(950,0,950,height);
    line(1000,0,1000,height);
}

public boolean timer (float _t) {
  
  umbral = _t;

  if(millis() - prevMillis >= umbral)
  {
    bang = true;
    //prevMillis = millis();
    prevMillis += umbral;
  } 
  else { 
    bang = false;
  }

  return bang;
}





class mouTe{

  //creamos array para la caja contenedora
  int[] cC = new int[4];
  //una variable para el total de celdas activas
  int totalActivas;
  //y un array para todas las celdas
  int[] cells = new int[192];
  //array para silueta
  int[] silh = new int[240];
  //ratio respecto al 160 * 120 al que queremos dibujar 
  int ratio;

  //OSCss
  OscP5 oscP5Box,oscP5Cells, oscP5TotalCells, oscP5Silh;

  mouTe(int _r, int _box, int _sil, int _cells, int _total){
    ratio = _r;
    oscP5Box = new OscP5(this,_box);
    oscP5Silh = new OscP5(this,_sil);
    oscP5Cells = new OscP5(this,_cells);
    oscP5TotalCells = new OscP5(this,_total);
  }

  public void dibujaCaja(){
    rectMode(CORNERS);
    stroke(0,255,0);
    strokeWeight(ratio);
    noFill();
    rect(cC[0]*ratio,cC[1]*ratio,cC[2]*ratio,cC[3]*ratio);
  }
  public void dibujaCentro(){
    noStroke();
    fill(255,0,0);
    ellipse((cC[0]+(cC[2]-cC[0])/2)*ratio,(cC[1]+(cC[3]-cC[1])/2)*ratio,3*ratio,3*ratio);
  }
  public void dibujaCeldas(){
    fill(255);
    stroke(255);
    for(int i=0; i<16; i++){
      for(int j=0; j<12; j++){
        //dibujamos as\u00ed porque estamos en rectMode(CORNERS):
        if(cells[i+j*16]==1){
          rect(i*(ratio*10),j*(ratio*10),(i+1)*(ratio*10)-1,(j+1)*(ratio*10)+1);
        }
      }
    }
  }

  public void dibujaSilueta(){
    //println("xxx-xxxxxx-xxxxxx");
    stroke(0xff76E9FF);
    for(int i=0; i<240; i+=2){
     // print(silh[i]);
     strokeWeight(ratio);
      if(silh[i]!=-1){
        line(silh[i]*ratio,(i/2)*ratio,silh[i+1]*ratio,(i/2)*ratio);
        //line(silh[i],i/2,silh[i+1],i/2);
      }
    }
       // println("---XXX------XXXX_------");
  }

  ////////////////////////////////////////////////
  //////// OSC

  //  void gotOSC(OscMessage theOscMessage) {
  public void oscEvent(OscMessage theOscMessage) {
    
    /* print the address pattern and the typetag of the received OscMessage */
    //print(" addrpattern: "+theOscMessage.addrPattern());
    //println(" typetag: "+theOscMessage.typetag());
    if(theOscMessage.checkAddrPattern("/box")) {
      //guardamos los valores de la caja contenedora en el array
      for (int i = 0; i < 4; i++){
        cC[i] = theOscMessage.get(i).intValue();
        // if(i<2)println(theOscMessage.get(i).intValue());
      }
    } 
    else if (theOscMessage.checkAddrPattern("/act")) {
      totalActivas = theOscMessage.get(0).intValue();
    } 
    else if (theOscMessage.checkAddrPattern("/st")) {

      for (int i = 0; i < 192; i++){
        cells[i] = theOscMessage.get(i).intValue();
      }
    }
    else if (theOscMessage.checkAddrPattern("/silh")) {
      for (int i = 0; i < 240; i++){
  //     print(theOscMessage.get(i).intValue());
      
        silh[i] = theOscMessage.get(i).intValue();
//        println(silh[i]);
      }
//       println("______");
    }
  }
}



  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#ECE9D8", "Metamorphose_PD" });
  }
}
