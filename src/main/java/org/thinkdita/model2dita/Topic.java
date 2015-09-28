package org.thinkdita.model2dita;

import javax.swing.text.BadLocationException;

import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class Topic {
	private String level;
	private String title;
	private String type;
	private String filename;

	public Topic(AuthorDocumentController authorDocumentController, AuthorNode topic) {
		try {
			setLevel(authorDocumentController.findNodesByXPath("//level", topic, true, true, true,
					false)[0].getTextContent());
			setTitle(authorDocumentController.findNodesByXPath("//title", topic, true, true, true,
					false)[0].getTextContent());	
			setType(authorDocumentController.findNodesByXPath("//type", topic, true, true, true,
					false)[0].getTextContent());	
			setFilename((getType().substring(0, 1) + "_" + getTitle().trim().toLowerCase() + ".xml").replace(" ", "-"));
		} catch (BadLocationException e) {
			e.printStackTrace();
		} catch (AuthorOperationException e) {
			e.printStackTrace();
		}

	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
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
	
	public String toString() {
		return "{" + "level = " + getLevel() + ", " + "title = " + getTitle() + ", " + "type = " + getType() + ", " + "filename = " + getFilename() + "}";
	}	
}
