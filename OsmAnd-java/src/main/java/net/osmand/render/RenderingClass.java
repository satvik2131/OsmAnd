package net.osmand.render;

import java.util.ArrayList;
import java.util.List;

public class RenderingClass {

	private final String name;
	private final String title;
	private final List<RenderingClass> children = new ArrayList<>();
	private final boolean enable;

	private String path;

	public RenderingClass(String name, String title, boolean enable) {
		this.name = name;
		this.title = title;
		this.enable = enable;
	}

	public String getName() {
		return name;
	}

	public String getColorName() {
		return name.replace(".", "") + "Color";
	}

	public String getTitle() {
		return title;
	}

	public boolean isEnable() {
		return enable;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public List<RenderingClass> getChildren() {
		return children;
	}

	public void addChild(RenderingClass child) {
		children.add(child);
	}

	public RenderingClass findByPath(String path) {
		if (this.path.equals(path)) {
			return this;
		}
		for (RenderingClass child : children) {
			RenderingClass result = child.findByPath(path);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
}