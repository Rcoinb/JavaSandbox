package com.greatdevs.tools;

import java.util.Random;

import org.lwjgl.util.vector.Vector3f;

public class Maths {
	public static Random random = new Random();
	
	
    public static float square(float number) {
        return number * number;
    }
    
    public static float disBetween2Vecs(Vector3f point1, Vector3f point2){
    	return (float) (Math.sqrt(square(point2.x - point1.x) + square(point2.y - point1.y) + square(point2.z - point1.z)));
    }
	
	public static int toEven(int i){
		if (i % 2 == 0) return i;
		else return i + 1;
	}
	
	public static javax.vecmath.Vector3f toVecmath(Vector3f vec){
		return new javax.vecmath.Vector3f(vec.x, vec.y, vec.z);
	}
	
	public static Vector3f subVec(Vector3f v1, Vector3f v2){
		Vector3f temp = new Vector3f();
		Vector3f.sub(v1, v2, temp);
		return temp;
	}
	
	public static Vector3f addVec(Vector3f v1, Vector3f v2){
		Vector3f temp = new Vector3f();
		Vector3f.add(v1, v2, temp);
		return temp;
	}
	
	public static Vector3f multVec(Vector3f v, float f){
		Vector3f temp = new Vector3f(v.x, v.y, v.z);
		temp.x = temp.x * f;
		temp.y = temp.y * f;
		temp.z = temp.z * f;
		return temp;
	}
	
	public static Vector3f multVec(Vector3f v1, Vector3f v2){
		Vector3f temp = new Vector3f(v1.x, v1.y, v1.z);
		temp.x = temp.x * v2.x;
		temp.y = temp.y * v2.y;
		temp.z = temp.z * v2.z;
		return temp;
	}
	
	public static Vector3f normalizeVec(Vector3f v){
		Vector3f temp = new Vector3f();
		v.normalise(temp);
		return temp;
	}
	
	public static boolean VectorsEquel(Vector3f v1, Vector3f v2){
		if (v1.x == v2.x && v1.y == v2.y && v1.z == v2.z) return true;
		return false;
	}
	
	public static Vector3f VectorsArithmeticMean(Vector3f v1, Vector3f v2){
		Vector3f temp = new Vector3f(0, 0, 0);
		temp.x = (v1.x + v2.x) / 2;
		temp.y = (v1.y + v2.y) / 2;
		temp.z = (v1.z + v2.z) / 2;
		return temp;
	}
	
    public static Vector3f getNormals(Vector3f p1, Vector3f p2, Vector3f p3) {
        Vector3f normal = new Vector3f();
        Vector3f calU = Maths.subVec(p2, p1);
        Vector3f calV = Maths.subVec(p3, p1);
        normal.x = calU.y*calV.z - calU.z*calV.y;
        normal.y = calU.z*calV.x - calU.x*calV.z;
        normal.z = calU.x*calV.y - calU.y*calV.x;
        normal.normalise(normal);
        return normal;
    }
}
