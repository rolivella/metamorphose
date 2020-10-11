import oscP5.*;
import netP5.*;

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

  void dibujaCaja(){
    rectMode(CORNERS);
    stroke(0,255,0);
    strokeWeight(ratio);
    noFill();
    rect(cC[0]*ratio,cC[1]*ratio,cC[2]*ratio,cC[3]*ratio);
  }
  void dibujaCentro(){
    noStroke();
    fill(255,0,0);
    ellipse((cC[0]+(cC[2]-cC[0])/2)*ratio,(cC[1]+(cC[3]-cC[1])/2)*ratio,3*ratio,3*ratio);
  }
  void dibujaCeldas(){
    fill(255);
    stroke(255);
    for(int i=0; i<16; i++){
      for(int j=0; j<12; j++){
        //dibujamos así porque estamos en rectMode(CORNERS):
        if(cells[i+j*16]==1){
          rect(i*(ratio*10),j*(ratio*10),(i+1)*(ratio*10)-1,(j+1)*(ratio*10)+1);
        }
      }
    }
  }

  void dibujaSilueta(){
    //println("xxx-xxxxxx-xxxxxx");
    stroke(#76E9FF);
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
  void oscEvent(OscMessage theOscMessage) {
    
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



