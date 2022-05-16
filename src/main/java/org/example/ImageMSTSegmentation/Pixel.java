package org.example.ImageMSTSegmentation;


import java.awt.Color;

public class Pixel {
	private int Intensity, Treeidx;
	
	Pixel(int Intensity){
		this.Intensity = Intensity;
		this.Treeidx = 0;
	}
	
	public void setTreeidx(int newTreeidx){
		this.Treeidx = newTreeidx;
	}
	public void setIntensity(int newIntensity){
		this.Intensity = newIntensity;
	}
	
	public int getTreeidx(){
		return this.Treeidx;
	}
	public int getIntensity(){
		return this.Intensity;
	}
	
	public String toStringd(){
		return "("+Intensity+", "+Treeidx+")";
	}
	
	public int getRGBValue(){
		return new Color(Intensity,Intensity,Intensity).getRGB();
	}
	
	public Color toColor(){
		return new Color(Intensity,Intensity,Intensity);
	}
	
	public String toString(){
		return Intensity+"";
	}
}
