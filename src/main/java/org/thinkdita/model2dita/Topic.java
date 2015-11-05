package org.thinkdita.model2dita;

import javax.swing.text.BadLocationException;

import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class Topic implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8612698296051094780L;
	private int level;
	private String title;
	private String type;
	private String filename;
	private String relativeFilePath;
	private String subfolderName;

	public Topic(AuthorDocumentController authorDocumentController, AuthorNode topic) {
		try {
			setLevel(new Integer(authorDocumentController.findNodesByXPath("level", topic, true, true,
					true, false)[0].getTextContent()));
			setTitle(authorDocumentController.findNodesByXPath("title", topic, true, true, true, false)[0]
					.getTextContent());
			setType(authorDocumentController.findNodesByXPath("type", topic, true, true, true, false)[0]
					.getTextContent());
			setFilename((getType().substring(0, 1) + "_" + getTitle().trim().toLowerCase() + ".xml")
					.replace(" ", "-"));
			setSubfolderName(getTitle().trim().toLowerCase().replace(" ", "-"));
		} catch (BadLocationException e) {
			e.printStackTrace();
		} catch (AuthorOperationException e) {
			e.printStackTrace();
		}

	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getRelativeFilePath() {
		return relativeFilePath;
	}

	public void setRelativeFilePath(String filePath) {
		this.relativeFilePath = filePath;
	}

	public String getSubfolderName() {
		return subfolderName;
	}

	public void setSubfolderName(String subfolderName) {
		this.subfolderName = subfolderName;
	}

	public String toString() {
		return "{" + "level = " + getLevel() + ", title = " + getTitle() + ", type = " + getType()
				+ ", filename = " + getFilename() + ", subfolderName = " + getSubfolderName()
				+ ", relativeFilePath = " + getRelativeFilePath() + "}";
	}
}
