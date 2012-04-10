package io;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
 

  public class DrawingPane extends JFrame implements ActionListener{
    /**
	 * 
	 */
	private static final long serialVersionUID = 3191511323202890066L;
	int inc = 1;
    int count = 0;
    BufferedImage renderer[];
    Graphics rendererG[];
    int nextImage = 0;
   // BufferedImage bi;
   // Graphics myG;
   // Graphics renderG;
    //Timer t = new Timer(40, this);
    int width;
    int height;
    BufferStrategy bufferStrategy;
    /**
     * @wbp.parser.entryPoint
     */
    public DrawingPane(int width, int height){
    	this.width = width;
    	this.height = height;
    	
     // bi = new BufferedImage(width+1, height+1, BufferedImage.TYPE_INT_RGB);
      //myG = bi.createGraphics();
      //renderG = renderer.createGraphics();
      this.setPreferredSize(new Dimension(width+1, height+1));
      //myG.clearRect(0, 0, bi.getWidth(), bi.getHeight());
     // this.repaint();
     // t.start();
    }
    
    public void initializeGraphics()
    {
    	//setIgnoreRepaint(true);
        
    	createBufferStrategy(3);
    	bufferStrategy = getBufferStrategy();
    	renderer = new BufferedImage[3];
    	rendererG = new Graphics[3];
      renderer[0] = new BufferedImage(width+1,height+1, BufferedImage.TYPE_INT_RGB);
      renderer[1] = new BufferedImage(width+1,height+1, BufferedImage.TYPE_INT_RGB);
      renderer[2] = new BufferedImage(width+1,height+1, BufferedImage.TYPE_INT_RGB);
      rendererG[0] = renderer[0].createGraphics();
      rendererG[1] = renderer[1].createGraphics();
      rendererG[2] = renderer[2].createGraphics();
    }
    //public void clear()
 //   {
    	 
 //  }
  //  public WritableRaster getRasterizer()
  //  {
  //  	return renderer.getRaster();
  //  }
   // public void actionPerformed(ActionEvent e){
  //    MyPaintOffScreen(myG);
  //    this.repaint();
  //    t.restart();
  //  }
  //  public void paintComponent(Graphics g){
  //    super.paintComponent(g);
  //    g.drawImage(bi, 0, 0, this);
  //  }
   // public void MyPaintOffScreen(Graphics g){
    
    	
    	//   if(count<0){
    	//    count = 1;
    	//    inc *= -1;
    	// }else if(count > 9){
    	//    count = 8;
    	//    inc *= -1;
    	//  }
    	//  g.drawImage
    	//  g.clearRect(0, 0, 256, 256);
    	//  g.drawString(Integer.toString(count), 128, 128);
    	//  count+=inc;
   // 	g.drawImage(renderer,0,0,null);
    	
  //  }
    public BufferedImage getRasterizer()
    {
    	if (nextImage == 3)
    		nextImage = 0;
    	rendererG[nextImage].clearRect(0, 0, renderer[nextImage].getWidth(), renderer[nextImage].getHeight());
    	return renderer[nextImage++];
    }
    public void render(BufferedImage renderer)
    {
    	Graphics g = bufferStrategy.getDrawGraphics();
    	g.drawImage(renderer,0,0,null);
    	g.dispose();
    	bufferStrategy.show();
 
    }


	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
  }