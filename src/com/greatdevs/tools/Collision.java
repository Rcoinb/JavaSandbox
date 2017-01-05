package com.greatdevs.tools;

import org.lwjgl.util.vector.Vector3f;

import com.greatdevs.shapes.Polygon;
import com.greatdevs.shapes.QuadPolygon;
import com.greatdevs.util.ObjectHandler;

public class Collision {
	public static boolean pointInCube(Vector3f position, Vector3f dimensions, Vector3f point){
		if (point.x < (position.x + Math.abs(dimensions.x)) && point.x > (position.x - Math.abs(dimensions.x))
				&& point.y < (position.y + Math.abs(dimensions.y)) && point.y > (position.y - Math.abs(dimensions.y))
				&& point.z < (position.z + Math.abs(dimensions.z)) && point.z > (position.z - Math.abs(dimensions.z)))
			return true;
		return false;
	}
	
	public static boolean cubeInCube(Vector3f position1, Vector3f dimensions1, Vector3f position2, Vector3f dimensions2){
		if ((position2.x - Math.abs(dimensions2.x)) < (position1.x + Math.abs(dimensions1.x)) && (position2.x + Math.abs(dimensions2.x)) > (position1.x - Math.abs(dimensions1.x))
				&& (position2.y - Math.abs(dimensions2.y)) < (position1.y + Math.abs(dimensions1.y)) && (position2.y + Math.abs(dimensions2.y)) > (position1.y - Math.abs(dimensions1.y))
				&& (position2.z - Math.abs(dimensions2.z)) < (position1.z + Math.abs(dimensions1.z)) && (position2.z + Math.abs(dimensions2.z)) > (position1.z - Math.abs(dimensions1.z)))
			return true;
		return false;
	}
	
	public static boolean cubeInCubeWithExtend(Vec3 pos1, Vec3 exd1, Vec3 pos2, Vec3 exd2){
		if ((pos1.x + exd1.x) > pos2.x && pos1.x < (pos2.x + exd2.x)) {
			if ((pos1.y + exd1.y) > pos2.y && pos1.y < (pos2.y + exd2.y)) {
				if ((pos1.z + exd1.z) > pos2.z && pos1.z < (pos2.z + exd2.z)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static Vector3f Hit = new Vector3f();
	
	public static boolean checkLineBox(Vector3f pos, Vector3f dim, Vector3f L1, Vector3f L2) {
		Vector3f B1 = new Vector3f(pos.x - dim.x, pos.y - dim.y, pos.z - dim.z);
		Vector3f B2 = new Vector3f(pos.x + dim.x, pos.y + dim.y, pos.z + dim.z);
		if (L2.x < B1.x && L1.x < B1.x)
			return false;
		if (L2.x > B2.x && L1.x > B2.x)
			return false;
		if (L2.y < B1.y && L1.y < B1.y)
			return false;
		if (L2.y > B2.y && L1.y > B2.y)
			return false;
		if (L2.z < B1.z && L1.z < B1.z)
			return false;
		if (L2.z > B2.z && L1.z > B2.z)
			return false;
		if (L1.x > B1.x && L1.x < B2.x && L1.y > B1.y && L1.y < B2.y && L1.z > B1.z && L1.z < B2.z) {
			Hit = L1;
			return true;
		}
		if ((getIntersection(L1.x - B1.x, L2.x - B1.x, L1, L2, Hit) && inBox(Hit, B1, B2, 1))
				|| (getIntersection(L1.y - B1.y, L2.y - B1.y, L1, L2, Hit) && inBox(Hit, B1, B2, 2))
				|| (getIntersection(L1.z - B1.z, L2.z - B1.z, L1, L2, Hit) && inBox(Hit, B1, B2, 3))
				|| (getIntersection(L1.x - B2.x, L2.x - B2.x, L1, L2, Hit) && inBox(Hit, B1, B2, 1))
				|| (getIntersection(L1.y - B2.y, L2.y - B2.y, L1, L2, Hit) && inBox(Hit, B1, B2, 2))
				|| (getIntersection(L1.z - B2.z, L2.z - B2.z, L1, L2, Hit) && inBox(Hit, B1, B2, 3)))
			return true;
		
		return false;
	}

	private static boolean getIntersection(float fDst1, float fDst2, Vector3f P1, Vector3f P2, Vector3f Hit) {
		Vector3f temp = new Vector3f();
		if ((fDst1 * fDst2) >= 0.0f)
			return false;
		if (fDst1 == fDst2)
			return false;
		Vector3f.sub(P2, P1, temp);
		temp.x = temp.x * (-fDst1 / (fDst2 - fDst1));
		temp.y = temp.y * (-fDst1 / (fDst2 - fDst1));
		temp.z = temp.z * (-fDst1 / (fDst2 - fDst1));
		Vector3f.add(P1, temp, Hit);
		return true;
	}

	private static boolean inBox(Vector3f Hit, Vector3f B1, Vector3f B2, int Axis) {
		if (Axis == 1 && Hit.z > B1.z && Hit.z < B2.z && Hit.y > B1.y && Hit.y < B2.y)
			return true;
		if (Axis == 2 && Hit.z > B1.z && Hit.z < B2.z && Hit.x > B1.x && Hit.x < B2.x)
			return true;
		if (Axis == 3 && Hit.x > B1.x && Hit.x < B2.x && Hit.y > B1.y && Hit.y < B2.y)
			return true;
		return false;
	}
	
	public static Vector3f getLineQuadPolygonIntersection(QuadPolygon p, Vector3f R1, Vector3f R2){
		Polygon pp[] = new Polygon[2];
		for (int i = 0; i < 2; i ++){
			pp[i] = new Polygon(p.p[i].v[0].getPos(), p.p[i].v[1].getPos(), p.p[i].v[2].getPos());
			pp[i].objectHandler = new ObjectHandler(pp[i]);
			pp[i].objectHandler.setPosition(p.objectHandler.getModelMatrix().getPosition());
			pp[i].objectHandler.setScale(p.objectHandler.getModelMatrix().getScale());
		}
		Vector3f p1I = getLinePolygonIntersection(pp[0], R1, R2);
		Vector3f p2I = getLinePolygonIntersection(pp[1], R1, R2);
		if (p1I != null) return p1I;
		if (p2I != null) return p2I;
		return null;
	}
	
	public static Vector3f getLinePolygonIntersection(Polygon p, Vector3f R1, Vector3f R2){
		return getLinePolygonIntersection(Maths.addVec(Maths.multVec(p.v[0].getPos(), p.objectHandler.getModelMatrix().getScale()), p.objectHandler.getModelMatrix().getPosition()), Maths.addVec(Maths.multVec(p.v[1].getPos(), p.objectHandler.getModelMatrix().getScale()), p.objectHandler.getModelMatrix().getPosition()), Maths.addVec(Maths.multVec(p.v[2].getPos(), p.objectHandler.getModelMatrix().getScale()), p.objectHandler.getModelMatrix().getPosition()), R1, R2);
	}
	
	public static Vector3f getLinePolygonIntersection(Vector3f P1, Vector3f P2, Vector3f P3, Vector3f R1, Vector3f R2) {  
	    Vector3f Normal = new Vector3f(0, 0, 0);
	    Vector3f IntersectPos = new Vector3f(0, 0, 0);
	    Vector3f PIP = new Vector3f(0, 0, 0);
	    Vector3f.cross(Maths.subVec(P2, P1), Maths.subVec(P3, P1), Normal);
	    Normal.normalise(Normal);

	    float Dist1 = Vector3f.dot(Maths.subVec(R1, P1), Normal);
	    float Dist2 = Vector3f.dot(Maths.subVec(R2, P1), Normal);

	    if ((Dist1 * Dist2) >= 0.0f) return null; 

	    if (Dist1 == Dist2) return null; 
	    IntersectPos = Maths.addVec(R1, Maths.multVec(Maths.subVec(R2,R1), (-Dist1/(Dist2-Dist1))));
	    
	    Vector3f vTest = new Vector3f();
	    vTest = Vector3f.cross(Normal, Maths.subVec(P2,P1), vTest);
	    if (Vector3f.dot(vTest, Maths.subVec(IntersectPos,P1)) < 0.0f) return null; 

	    vTest = Vector3f.cross(Normal, Maths.subVec(P3,P2), vTest);
	    if (Vector3f.dot(vTest, Maths.subVec(IntersectPos,P2)) < 0.0f) return null; 

	    vTest = Vector3f.cross(Normal, Maths.subVec(P1,P3), vTest);
	    if (Vector3f.dot(vTest, Maths.subVec(IntersectPos,P1)) < 0.0f) return null; 
	    
	    if (Float.isNaN(IntersectPos.x)) return null;
	    if (Float.isNaN(IntersectPos.y)) return null;
	    if (Float.isNaN(IntersectPos.z)) return null;
	    
	    PIP = IntersectPos;
	    
	    return PIP;
	}
	
	public static boolean checkCubePolygonIntersection(Vector3f cPos, Vector3f cDim, Vector3f p1, Vector3f p2, Vector3f p3){
		if (pointInCube(cPos, cDim, p1)) return true;
		if (pointInCube(cPos, cDim, p2)) return true;
		if (pointInCube(cPos, cDim, p3)) return true;
		if (checkLineBox(cPos, cDim, p1, p2)) return true;
		if (checkLineBox(cPos, cDim, p2, p3)) return true;
		if (checkLineBox(cPos, cDim, p3, p1)) return true;
		
		if (getLinePolygonIntersection(p1, p2, p3, new Vector3f(cPos.x - cDim.x, cPos.y - cDim.y, cPos.z - cDim.z), new Vector3f(cPos.x + cDim.x, cPos.y - cDim.y, cPos.z - cDim.z)) != null) return true;
		if (getLinePolygonIntersection(p1, p2, p3, new Vector3f(cPos.x - cDim.x, cPos.y - cDim.y, cPos.z - cDim.z), new Vector3f(cPos.x - cDim.x, cPos.y - cDim.y, cPos.z + cDim.z)) != null) return true;
		if (getLinePolygonIntersection(p1, p2, p3, new Vector3f(cPos.x + cDim.x, cPos.y - cDim.y, cPos.z + cDim.z), new Vector3f(cPos.x - cDim.x, cPos.y - cDim.y, cPos.z + cDim.z)) != null) return true;
		if (getLinePolygonIntersection(p1, p2, p3, new Vector3f(cPos.x + cDim.x, cPos.y - cDim.y, cPos.z + cDim.z), new Vector3f(cPos.x + cDim.x, cPos.y - cDim.y, cPos.z - cDim.z)) != null) return true;
		
		if (getLinePolygonIntersection(p1, p2, p3, new Vector3f(cPos.x - cDim.x, cPos.y + cDim.y, cPos.z - cDim.z), new Vector3f(cPos.x + cDim.x, cPos.y + cDim.y, cPos.z - cDim.z)) != null) return true;
		if (getLinePolygonIntersection(p1, p2, p3, new Vector3f(cPos.x - cDim.x, cPos.y + cDim.y, cPos.z - cDim.z), new Vector3f(cPos.x - cDim.x, cPos.y + cDim.y, cPos.z + cDim.z)) != null) return true;
		if (getLinePolygonIntersection(p1, p2, p3, new Vector3f(cPos.x + cDim.x, cPos.y + cDim.y, cPos.z + cDim.z), new Vector3f(cPos.x - cDim.x, cPos.y + cDim.y, cPos.z + cDim.z)) != null) return true;
		if (getLinePolygonIntersection(p1, p2, p3, new Vector3f(cPos.x + cDim.x, cPos.y + cDim.y, cPos.z + cDim.z), new Vector3f(cPos.x + cDim.x, cPos.y + cDim.y, cPos.z - cDim.z)) != null) return true;
		
		if (getLinePolygonIntersection(p1, p2, p3, new Vector3f(cPos.x - cDim.x, cPos.y - cDim.y, cPos.z - cDim.z), new Vector3f(cPos.x - cDim.x, cPos.y + cDim.y, cPos.z - cDim.z)) != null) return true;
		if (getLinePolygonIntersection(p1, p2, p3, new Vector3f(cPos.x - cDim.x, cPos.y - cDim.y, cPos.z + cDim.z), new Vector3f(cPos.x - cDim.x, cPos.y + cDim.y, cPos.z + cDim.z)) != null) return true;
		if (getLinePolygonIntersection(p1, p2, p3, new Vector3f(cPos.x + cDim.x, cPos.y - cDim.y, cPos.z + cDim.z), new Vector3f(cPos.x + cDim.x, cPos.y + cDim.y, cPos.z + cDim.z)) != null) return true;
		if (getLinePolygonIntersection(p1, p2, p3, new Vector3f(cPos.x + cDim.x, cPos.y - cDim.y, cPos.z - cDim.z), new Vector3f(cPos.x + cDim.x, cPos.y + cDim.y, cPos.z - cDim.z)) != null) return true;
		
		return false;
	}

	public static boolean checkCubePolygonIntersection(Vector3f cPos, Vector3f cDim, Polygon p, ObjectHandler oh) {
		return checkCubePolygonIntersection(cPos, cDim, Maths.addVec(Maths.multVec(p.v[0].getPos(), oh.getModelMatrix().getScale()), oh.getModelMatrix().getPosition()), Maths.addVec(Maths.multVec(p.v[1].getPos(), oh.getModelMatrix().getScale()), oh.getModelMatrix().getPosition()), Maths.addVec(Maths.multVec(p.v[2].getPos(), oh.getModelMatrix().getScale()), oh.getModelMatrix().getPosition()));
	}
}
