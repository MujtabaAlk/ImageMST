package org.example.ImageMSTSegmentation;

/*
 *
 * Title			:	A Unified method for segmentation and edge detection using Graph Theory.
 * Author			: 	Mujtaba al-Khalifa, 	Ahmed al-Elg.
 * ID   			: 	201577250		  , 	201507470.
 * Date				: 	April 24, 2018
 * Description		: 	User enters an image with P pixels and a number R, gets a gray segmented image with R regions.
 * Input			:	An image and a number of segmentation R.
 * Output			:	An image segmented into R regions.
 *
 *
 */
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.util.UnionFind;
import org.jgrapht.graph.SimpleWeightedGraph;







public class Main {

    public static void main(String[] args) {
        new Window();

    }

}

class Window extends JFrame{
    /**
     *
     */
    private static final long serialVersionUID = -1257709083717493234L;
    JButton MST = new JButton("MST");
    JButton RMST = new JButton("RMST");
    JCheckBox n8Button = new JCheckBox("8-neighbouhood ?");
    JCheckBox noiseButton = new JCheckBox("Noise ?");
    JSlider regns = new JSlider(JSlider.HORIZONTAL,0, 100, 15);

    JButton save = new JButton("Save");
    JButton about = new JButton("About");

    JLabel rLbl = new JLabel("Number of regions (R)%: ");


    static JLabel picLabel = new JLabel();
    static BufferedImage img = null;
    static File file = null;
    JFrame j = null;
    public String FlPath, FlDirectory, FlPathOut;

    public Window() {

        JFrame j = new JFrame("RMST/MST Application");
        j.setLocationRelativeTo(null);
        j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        j.setSize(960,540);
        j.setLayout(new BorderLayout());
        j.setLocationRelativeTo(null);
        regns.setMajorTickSpacing(10);
        regns.setMinorTickSpacing(1);
        regns.setPaintTicks(true);
        regns.setPaintLabels(true);
        regns.setPreferredSize(new Dimension(500, 50));
        JPanel btnSec = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel slidSec = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel both = new JPanel(new GridLayout(0,1));



        slidSec.add(rLbl);
        slidSec.add(regns);
        btnSec.add(MST);
        btnSec.add(RMST);
        btnSec.add(save);
        slidSec.add(n8Button);
        slidSec.add(noiseButton);

        btnSec.add(about);

        both.add(btnSec);
        both.add(slidSec);


        j.setResizable(false);
        j.add(both, BorderLayout.NORTH);
        j.add(picLabel, BorderLayout.CENTER);


        about.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                String pt1 = "<html><body width='300'>";
                pt1+=
                        "<h1>MST/RMST App.</h1>" +
                                "<p>A Unified method for segmentation and edge detection using Graph Theory</p><br>" +
                                "Under supervision of Dr.Wasfi G. al-Khatib. <br><br>" +
                                "<p>&copy 2018 Mujtaba al-Khalifa and Ahmed al-Elg" +
                                "</p>";

                JOptionPane.showMessageDialog(null, pt1, "About", JOptionPane.INFORMATION_MESSAGE);
            }

        });
        save.addActionListener(new ActionListener() {


            @Override
            public void actionPerformed(ActionEvent arg0) {

                try {
                    file =new File(FlPathOut);
                    ImageIO.write(img, "png", file);
                    JOptionPane.showMessageDialog(null,"Image saved to " + FlPathOut);


                }catch(Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        });

        MST.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                try {
                    BufferedImage img = ConvertGray(openImage(), noiseButton.isSelected());//convert to gray and output

                    MSTAlg(img);
                }catch(Exception e) {

                }

            }
        });

        RMST.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {

                    RMSTAlg(openImage());
                }catch(Exception e) {

                }



            }

        });

        j.setVisible(true);
    }



    public void MSTAlg(BufferedImage theImage) {
        System.out.println();
        long startTime = System.nanoTime();
        int width = theImage.getWidth();
        int height = theImage.getHeight();
        int dim = width * height;
        double  r = dim * (regns.getValue()/100.0);
        int R = (int) r;

        SimpleWeightedGraph<Pixel, PixelEdge> ImgGph = new SimpleWeightedGraph<Pixel, PixelEdge>(PixelEdge.class);//Pixel Graph

        Pixel[][] Pxs = new Pixel[img.getWidth()][img.getHeight()];// Pixel grid

        for(int i = 0; i<img.getHeight(); i++){// import to Pixel grid
            for(int j = 0; j<img.getWidth(); j++){
                int gray = new Color(img.getRGB(j, i)).getRed();

                if(noiseButton.isSelected()) {
                    int noiseAmt = (int)(255*(Math.random()-0.5));
                    gray += noiseAmt;
                    if(gray>255)
                        gray=200;
                    else if(gray<0)
                        gray=0;
                }

                Pxs[j][i] = new Pixel(gray);
            }
        }
        System.out.println("done! Importing to Grid");

        for(int i = 0; i<Pxs.length; i++){// add vertices
            for(int j = 0; j<Pxs[i].length; j++){
                ImgGph.addVertex(Pxs[i][j]);
            }
        }
        System.out.println("done! Importing Vertices to Graph");

        ArrayList<PixelEdge> EdgeList = new ArrayList<PixelEdge>();

        for(int i = 0; i<Pxs.length; i++){// create and add Edges
            for(int j = 0; j<Pxs[i].length; j++){
                if(j>0){
                    EdgeList.add(ImgGph.addEdge(Pxs[i][j], Pxs[i][j-1]));
                    double w = Math.abs((Pxs[i][j].getIntensity()-Pxs[i][j-1].getIntensity()));
                    ImgGph.setEdgeWeight(ImgGph.getEdge(Pxs[i][j], Pxs[i][j-1]), w);
                }
                if(i>0){
                    EdgeList.add(ImgGph.addEdge(Pxs[i][j], Pxs[i-1][j]));
                    double w = Math.abs((Pxs[i][j].getIntensity()-Pxs[i-1][j].getIntensity()));
                    ImgGph.setEdgeWeight(ImgGph.getEdge(Pxs[i][j], Pxs[i-1][j]), w);
                }

                if(n8Button.isSelected()) {
                    if((i>0)&&(j>0)){
                        EdgeList.add(ImgGph.addEdge(Pxs[i][j], Pxs[i-1][j-1]));
                        double w = Math.abs((Pxs[i][j].getIntensity()-Pxs[i-1][j-1].getIntensity()));
                        ImgGph.setEdgeWeight(ImgGph.getEdge(Pxs[i][j], Pxs[i-1][j-1]), w);
                    }
                    if((i>0)&&(j<(Pxs[i].length-1))){
                        EdgeList.add(ImgGph.addEdge(Pxs[i][j], Pxs[i-1][j+1]));
                        double w = Math.abs((Pxs[i][j].getIntensity()-Pxs[i-1][j+1].getIntensity()));
                        ImgGph.setEdgeWeight(ImgGph.getEdge(Pxs[i][j], Pxs[i-1][j+1]), w);
                    }
                }

            }
        }
        //System.out.println("done! Creating & importing Edges to Graph & List");

        System.out.println("1. Map the image onto a primal weighted graph. [OK]");



        // Kruskal's MST
        ArrayList<PixelEdge> MSTEdges = kruskalsMST(ImgGph, EdgeList);


        //System.out.println("done! Creating Minimum Spanning Tree");
        System.out.println("2. Find an MST of the graph. [OK]");
        Collections.sort(MSTEdges);//sort edges descending order

        //System.out.println("done! Creating MST Edge List & Sorting");


        for(int i = 0; i<EdgeList.size();){//make main graph to MTS graph
            if(MSTEdges.contains(EdgeList.get(i))){
                i++;
            }
            else{
                ImgGph.removeEdge(EdgeList.get(i));
                EdgeList.remove(i);
            }
        }
        //System.out.println("done! Converting Main Graph to MTS Graph");

        for(int i = 0; i<(R-1); i++){// cut rg-1 Edges to create rg regions
            ImgGph.removeEdge(EdgeList.get(0));
            EdgeList.remove(0);
        }
        //System.out.println("done! Cutting "+(R-1)+" Edges");
        System.out.println("3. Cut the MST at the "+ R +" (R-1) most costly edges. [OK]");


        ConnectivityInspector<Pixel, PixelEdge> CI = new ConnectivityInspector<Pixel, PixelEdge>(ImgGph) ;
        List<Set<Pixel>> lst = CI.connectedSets();
        //System.out.println("done! Finding Connected SubTrees");

        //System.out.println("number of connected SubTrees: "+lst.size());

        int[] Avgs = new int[R];

        for(int i = 0; i<lst.size(); i++){// calculate averages
            Iterator<Pixel> Vxit = lst.get(i).iterator();

            while(Vxit.hasNext()){
                Avgs[i] += Vxit.next().getIntensity();
            }
            Avgs[i] = (int)(Avgs[i]/lst.get(i).size());
        }
        //System.out.println("done! Calculating Averages");
        for(int i = 0; i<lst.size(); i++){// set average intensities
            Iterator<Pixel> Vxit = lst.get(i).iterator();
            while(Vxit.hasNext()){
                Pixel PXL = Vxit.next();
                PXL.setIntensity(Avgs[i]);
            }
        }
        //	System.out.println("done! Setting Averages to Vertex intensities");
        System.out.println("4. Assign the average tree vertex weight to each vertex in each tree in the forest. [OK]");

        for(int i = 0; i<EdgeList.size(); i++){
            int newWeight = Math.abs(((Pixel)EdgeList.get(i).getSource()).getIntensity()-((Pixel)EdgeList.get(i).getTarget()).getIntensity());
            ImgGph.setEdgeWeight(EdgeList.get(i), newWeight);
        }
        //	System.out.println("done! Recalculating Edge Weights");

        //Create Segmented Image
        BufferedImage Segimg = new BufferedImage(Pxs.length,Pxs[0].length, BufferedImage.TYPE_INT_ARGB);
        for(int i = 0; i<Pxs.length; i++){
            for(int j = 0; j<Pxs[i].length; j++){
                Segimg.setRGB(i, j, Pxs[i][j].getRGBValue());
            }
        }
        System.out.println("5. Map the partition onto a segmentation image [OK]");
        //	System.out.println("done! Creating Segmented Image");
        long endTime   = System.nanoTime();
        long totalTime = endTime - startTime;


        System.out.println("\nTotal time: " + totalTime + "ns");

        Image ima =  Segimg.getScaledInstance(picLabel.getWidth(), picLabel.getHeight(), java.awt.Image.SCALE_SMOOTH);
        picLabel.setIcon(new ImageIcon(ima));
        img = Segimg;
        String nighbor = n8Button.isSelected() ? "_8_neighbhd_" : "_4_neighbhd_";
        String noise = noiseButton.isSelected() ? "_wNoise_" : "_woNoise_";

        FlPathOut = FlPath.substring(0, FlPath.indexOf("."))+"_" +R + "("+regns.getValue()+"%) Seg_MSTOut_"+nighbor+noise+System.currentTimeMillis()+".png";

        System.out.println("[Finished MST]");

    }

    public void RMSTAlg(BufferedImage theImage) {
        long startTime = System.nanoTime();

        int width = theImage.getWidth();
        int height = theImage.getHeight();
        int dim = width * height;
        double  rV = dim * (regns.getValue()/100.0);
        int R = (int) rV;

        SimpleWeightedGraph<Pixel, PixelEdge> gg = new SimpleWeightedGraph<>(PixelEdge.class);
        ArrayList<PixelEdge> edges = new ArrayList<PixelEdge>();
        ArrayList<PixelEdge> allEdges = new ArrayList<PixelEdge>();

        Pixel[][] pixels = new Pixel[height][width];



        for(int y=0; y<height; y++) {
            for(int x=0; x<width; x++) {
                int p = img.getRGB(x, y);
                Color clr = new Color(p);
                int r = clr.getRed();
                int g = clr.getBlue();
                int b = clr.getGreen();
                int gray =  (int)(0.2125 * r + 0.7154 * g + 0.0721 * b);
                if(noiseButton.isSelected())
                {
                    int noiseAmt = (int)(255*(Math.random()-0.5));
                    gray += noiseAmt;
                    if(gray>255)
                        gray=200;
                    else if(gray<0)
                        gray=0;
                }
                pixels[y][x] = new Pixel(gray);

                //1. Map the image onto a primal weighted graph.
                gg.addVertex(pixels[y][x]);

                if (x>0){
                    PixelEdge leftEdge = gg.addEdge(pixels[y][x], pixels[y][x-1]);
                    edges.add(leftEdge);
                    allEdges.add(leftEdge);

                    double w = Math.abs(pixels[y][x-1].getIntensity()- pixels[y][x].getIntensity());
                    gg.setEdgeWeight(leftEdge, w);

                }

                if (y>0){
                    PixelEdge upwardEdge = gg.addEdge(pixels[y-1][x], pixels[y][x]);
                    edges.add(upwardEdge);
                    allEdges.add(upwardEdge);
                    double w = Math.abs(pixels[y-1][x].getIntensity()- pixels[y][x].getIntensity());
                    gg.setEdgeWeight(upwardEdge, w);
                }

                if(y>=1 && n8Button.isSelected()) {
                    if(x>=0 && x!=width-1) {
                        PixelEdge leftDiagonalEdge = gg.addEdge(pixels[y-1][x+1], pixels[y][x]);
                        edges.add(leftDiagonalEdge);
                        allEdges.add(leftDiagonalEdge);
                        double w = Math.abs(pixels[y-1][x+1].getIntensity()- pixels[y][x].getIntensity());
                        gg.setEdgeWeight(leftDiagonalEdge, w);

                    }
                    if(x!=0 && x<=width-1) {
                        PixelEdge rightDiagonalEdge = gg.addEdge(pixels[y-1][x-1], pixels[y][x]);
                        edges.add(rightDiagonalEdge);
                        allEdges.add(rightDiagonalEdge);
                        double w = Math.abs(pixels[y-1][x-1].getIntensity()- pixels[y][x].getIntensity());
                        gg.setEdgeWeight(rightDiagonalEdge, w);

                    }
                }


            }
        }
        // ----------------------------------------
        Collections.sort(allEdges);


        System.out.println("1. Map the image onto a primal weighted graph. [OK]");



        //2. For I = P-2 down to R-1 do:
        int I = (height*width) -2;
        System.out.println("2. For I = P-2 down to R-1 do:");
        for(; I>=(R-1); I--) {
            // 2.1. Find an MST of the graph.
            ArrayList<PixelEdge> MSTEdges = kruskalsMST(gg, edges); // get MST using kruskal's alg.

            Collections.sort(MSTEdges);//sort edges descending order


            gg.removeAllEdges(edges);
            edges.clear();
            for(int j=0; j<MSTEdges.size();j++)
            {
                Pixel p1 = (Pixel) MSTEdges.get(j).getSource();
                Pixel p2 = (Pixel) MSTEdges.get(j).getTarget();
                PixelEdge theEdge =  gg.addEdge(p1, p2);
                double w = Math.abs(p1.getIntensity() - p2.getIntensity());
                gg.setEdgeWeight(theEdge, w);
                edges.add(theEdge);
            }

            Collections.sort(edges);


            //2.2. Cut the MST at the I most costly edges.

            for(int j=0; j<I; j++)
            {
                gg.removeEdge(edges.get(0));
                edges.remove(0);

            }

            //2.3 Assign the average tree vertex weight to each vertex in each tree in the forest

            ConnectivityInspector<Pixel, PixelEdge> CI = new ConnectivityInspector<Pixel, PixelEdge>(gg) ;
            List<Set<Pixel>> lst = CI.connectedSets();

            for(int j=0; j<lst.size(); j++)
            {
                int avg = 0;
                Iterator<Pixel> itr = lst.get(j).iterator();

                while(itr.hasNext()) //get sum of all vertices intensity
                    avg += itr.next().getIntensity();

                avg =  avg / lst.get(j).size();

                itr = lst.get(j).iterator(); // set vertices intensity to avg.
                while(itr.hasNext())
                    itr.next().setIntensity(avg);
            }

            //	2.4. Re-evaluate the graph edge weights
            gg.removeAllEdges(edges);
            edges.clear();

            for(int j=0; j<allEdges.size(); j++) {
                PixelEdge ege = allEdges.get(j);
                Pixel p1 = (Pixel) ege.getSource();
                Pixel p2 = (Pixel) ege.getTarget();

                double w = Math.abs(p1.getIntensity() - p2.getIntensity());
                PixelEdge theEdge =  gg.addEdge(p1, p2);
                gg.setEdgeWeight(theEdge, w);
                edges.add(theEdge);

            }


        }
        System.out.println("[Finished RMST]");

        for(int y=0; y<height; y++) {
            for(int x=0; x<width; x++) {
                int rgb = pixels[y][x].getRGBValue();
                img.setRGB(x, y,rgb);
            }
        }

        long endTime   = System.nanoTime();
        long totalTime = endTime - startTime;
        System.out.println("Total time: " + totalTime + "ns");


        Image ima =  img.getScaledInstance(picLabel.getWidth(), picLabel.getHeight(), java.awt.Image.SCALE_SMOOTH);
        picLabel.setIcon(new ImageIcon(ima));
        String nighbor = n8Button.isSelected() ? "_8_neighbhd_" : "_4_neighbhd_";
        String noise = noiseButton.isSelected() ? "_wNoise_" : "_woNoise_";

        FlPathOut = FlPath.substring(0, FlPath.indexOf("."))+"_" +R + "("+regns.getValue()+"%) Seg_RMSTOut_"+nighbor+noise+System.currentTimeMillis()+".png";



    } //RMSTAlg ends

    public static ArrayList<PixelEdge> kruskalsMST(SimpleWeightedGraph<Pixel, PixelEdge> theGraph, ArrayList<PixelEdge> theEdgeList){
        // Kruskal's MST
        ArrayList<PixelEdge> MSTEdges = new ArrayList<PixelEdge>();// edges of MST in ArrayList
        //SimpleWeightedGraph<Pixel, PixelEdge> KMST = new SimpleWeightedGraph<Pixel, PixelEdge>(PixelEdge.class);// MST graph
        //org.jgrapht.Graphs.addAllVertices(KMST, ImgGph.vertexSet()); // adding all vertices in the original graph to the new graph

        Collections.sort(theEdgeList);
        Queue<PixelEdge> q = new LinkedList<>(); //enqueue edges of G in a queue in increasing order of cost.
        for(int j=theEdgeList.size()-1; j>=0; j--)
            q.add(theEdgeList.get(j));

        UnionFind<Pixel> disjointSet = new UnionFind<>(theGraph.vertexSet()); //used to check for cycles in an undirected graph

        while(!q.isEmpty()) //while(queue is not empty)
        {
            PixelEdge e = q.poll(); //dequeue an edge e;
            Pixel source = (Pixel) e.getSource();
            Pixel target = (Pixel) e.getTarget();
            if(! (disjointSet.find(source).equals(disjointSet.find(target))) ) //if(e does not create a cycle with edges in T)
            {
                //org.jgrapht.Graphs.addEdgeWithVertices(KMST, ImgGph, e); //add e to T;
                disjointSet.union(source, target);
                MSTEdges.add(e);
            }

        }
        return MSTEdges;
    }


    public static BufferedImage ConvertGray(BufferedImage img, boolean addNoise) throws IOException{
        Color colors [][] = new Color[img.getWidth()][img.getHeight()];
        for(int i = 0; i<img.getHeight(); i++){
            for(int j = 0; j<img.getWidth(); j++){
                colors[j][i] = new Color(img.getRGB(j, i));
            }
        }

        BufferedImage gimg = new BufferedImage(colors.length,colors[0].length, BufferedImage.TYPE_INT_ARGB);
        System.out.println("Noise: " + addNoise);
        for(int i = 0; i<gimg.getHeight(); i++){
            for(int j = 0; j<gimg.getWidth(); j++){

                int r = colors[j][i].getRed();
                int g = colors[j][i].getGreen();
                int b = colors[j][i].getBlue();


                int s = (int)((r*0.2125)+(g*0.7154)+(b*0.0721));

//			    		if(addNoise)
//			    		{
//
//			    			int noiseAmt = (int)(255*(Math.random()-0.5));
//				    		s += noiseAmt;
//				    		if(s>255)
//				    			s=200;
//				    		else if(s<0)
//				    			s=0;
//			    		}


                gimg.setRGB(j, i, new Color(s,s,s).getRGB());
            }
        }

        return gimg;

    }
    public BufferedImage openImage() throws Exception {
        JFileChooser c = new JFileChooser();
        File workingDirectory = new File(System.getProperty("user.dir"));
        c.setCurrentDirectory(workingDirectory);
        FileNameExtensionFilter imageFilter = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());
        c.setAcceptAllFileFilterUsed(false);
        c.setFileFilter(imageFilter);
        int rVal = c.showOpenDialog(Window.this);
        if (rVal == JFileChooser.APPROVE_OPTION) {
            System.out.println(c.getSelectedFile().getPath());
            System.out.println(c.getCurrentDirectory().toString());
            FlPath = c.getSelectedFile().getPath();
            FlDirectory = c.getCurrentDirectory().toString();

            file = new File (c.getSelectedFile().getPath());
            img = ImageIO.read(file);
            return new BufferedImage(img.getWidth(),img.getHeight(), BufferedImage.TYPE_INT_ARGB);



        }
        throw new Exception("Error");

    }

}



