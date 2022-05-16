package org.example.ImageMSTSegmentation;


import org.jgrapht.graph.DefaultWeightedEdge;

public class PixelEdge extends DefaultWeightedEdge implements Comparable<PixelEdge> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int compareTo(PixelEdge comparestu) {
		int compareage=(int) ((PixelEdge)comparestu).getWeight();
		return compareage-(int)this.getWeight();
		}

	/*@Override
	public int compareTo(Object o) {
		int compareage=(int) ((PixelEdge)o).getWeight();
		return compareage-(int)this.getWeight();
	}*/
	
	public Object getSource(){
		return super.getSource();
	}
	
	public Object getTarget(){
		return super.getTarget();
	}
	
	public double getWeight() {
		return super.getWeight();
	}
	
	public String toString(){
		return "(" + this.getSource() + " : " + this.getTarget() + ")" + " W= " + this.getWeight();
	}
	
}
