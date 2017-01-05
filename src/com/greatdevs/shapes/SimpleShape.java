package com.greatdevs.shapes;

import com.greatdevs.util.ObjectHandler;
import com.greatdevs.util.Vertex;

public abstract class SimpleShape {	
	public ObjectHandler objectHandler;
	
	public abstract int[] getIndices();

	public abstract Vertex[] getVertices();
}
