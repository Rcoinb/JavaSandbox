package com.greatdevs.util;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class Vertex {
	@Override
	public String toString() {
		return "Vertex [pos=" + pos + ", color=" + color + ", normal=" + normal + ", texCoord=" + texCoord + "]";
	}

	public static final int SIZE = 12;

	private Vector3f pos;
	private Vector4f color;
	private Vector3f normal;
	private Vector2f texCoord = new Vector2f(-1, -1);

	public Vertex(Vector3f pos) {
		this(pos, new Vector4f(0, 0, 0, 1));
	}

	public Vertex(Vector3f pos, Vector4f color) {
		this(pos, color, new Vector3f(0, 0, 0));
	}

	public Vertex(Vector3f pos, Vector4f color, Vector3f normal) {
		this.pos = pos;
		this.color = color;
		this.normal = normal;
	}

	public Vector3f getPos() {
		return pos;
	}

	public void setPos(Vector3f pos) {
		this.pos = pos;
	}

	public Vector4f getColor() {
		return color;
	}

	public void setColor(Vector4f color) {
		this.color = color;
	}

	public Vector3f getNormal() {
		return normal;
	}

	public void setNormal(Vector3f normal) {
		this.normal = normal;
	}
	
	public Vector2f getTexCoord() {
		return texCoord;
	}

	public void setTexCoord(Vector2f texCoord) {
		this.texCoord = texCoord;
	}
}
