package com.greatdevs.tools;

import org.lwjgl.util.vector.Vector3f;

public class Vec3 {
	
	public int x, y, z;
	
	public Vec3(int x, int y, int z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vec3(Vector3f vector) {
		this.x = (int) vector.x;
		this.y = (int) vector.y;
		this.z = (int) vector.z;
	}

	public Vec3 add(Vec3 vec){
		return this.add(vec.x, vec.y, vec.z);
	}
	
	public Vec3 add(int xx, int yy, int zz){
		this.x = x + xx;
		this.y = y + yy;
		this.z = z + zz;
		return this;
	}
	
	public Vec3 add(int a){
		return this.add(a, a, a);
	}
	
	public Vec3 mult(Vec3 vec){
		return this.mult(vec.x, vec.y, vec.z);
	}
	
	public Vec3 mult(int xx, int yy, int zz){
		this.x = x * xx;
		this.y = y * yy;
		this.z = z * zz;
		return this;
	}
	
	public Vec3 mult(int a){
		return this.mult(a, a, a);
	}
	
	public Vec3 div(int a){
		this.x = x / a;
		this.y = y / a;
		this.z = z / a;
		return this;
	}
	
	public Vec3 clone(){
		Vec3 vec = new Vec3(x, y, z);
		return vec;
	}
	
	public String toString(){
		return new String("X " + x + ", Y " + y + ", Z " + z);
	}
}
