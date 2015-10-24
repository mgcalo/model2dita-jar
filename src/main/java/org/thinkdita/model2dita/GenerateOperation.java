package org.thinkdita.model2dita;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.text.BadLocationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ro.sync.annotations.api.API;
import ro.sync.annotations.api.APIType;
import ro.sync.annotations.api.SourceType;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.access.AuthorEditorAccess;
import ro.sync.ecss.extensions.api.access.AuthorWorkspaceAccess;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.util.URLUtil;

@API(type = APIType.INTERNAL, src = SourceType.PUBLIC)
public class GenerateOperation implements AuthorOperation {

	/**
	 * Logger for logging.
	 */
	private static final Logger logger = Logger.getLogger(GenerateOperation.class.getName());

	/**
	 * The arguments of the operation.
	 */
	private ArgumentDescriptor[] arguments = null;

	/**
	 * Constructor.
	 */
	public GenerateOperation() {
		arguments = new ArgumentDescriptor[0];
	}

	/**
	 * @see ro.sync.ecss.extensions.api.AuthorOperation#doOperation(AuthorAccess,
	 *      ArgumentsMap)
	 */
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap args) throws AuthorOperationException {
		AuthorEditorAccess authorEditorAccess = authorAccess.getEditorAccess();
		AuthorDocumentController authorDocumentController = authorAccess.getDocumentController();
		AuthorWorkspaceAccess authorWorkspaceAccess = authorAccess.getWorkspaceAccess();

		// get the target dir for creating the DITA project
		File projectDir = null;
		projectDir = authorWorkspaceAccess.chooseDirectory();
		logger.debug("projectDir: " + projectDir);

		if (projectDir != null) {
		} else {
			authorWorkspaceAccess
					.showErrorMessage("You have to choose an existing folder, otherwise this operation will stop.");
			return;
		}

		AuthorNode currentNode = null;

		try {
			currentNode = authorDocumentController.getNodeAtOffset(authorEditorAccess.getSelectionStart());
		} catch (BadLocationException e) {

			e.printStackTrace();
		}

		String projectName = "";
		try {
			projectName = authorDocumentController.findNodesByXPath("//projectname", true, true, true)[0]
					.getTextContent();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		logger.debug("projectName: " + projectName);

		String language = "en-US";
		try {
			language = authorDocumentController.findNodesByXPath("//language", true, true, true)[0]
					.getTextContent();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		logger.debug("language: " + language);

		String oxygenInstallDir = URLUtil.uncorrect(authorAccess.getUtilAccess().expandEditorVariables(
				"${oxygenInstallDir}", null));
		logger.debug("oxygenInstallDir: " + oxygenInstallDir);

		String frameworkDir = URLUtil.uncorrect(authorAccess.getUtilAccess().expandEditorVariables(
				"${frameworkDir}", null));
		logger.debug("frameworkDir: " + frameworkDir);

		File templatesDir = new File(frameworkDir + File.separator + "templates" + File.separator
				+ language);
		logger.debug("templatesDir: " + templatesDir);

		String createSubfolder = "0";
		try {
			createSubfolder = authorDocumentController.findNodesByXPath("//subfolders", true, true, true)[0]
					.getTextContent();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		logger.debug("createSubfolder: " + createSubfolder);

		String createImagefolder = "0";
		try {
			createImagefolder = authorDocumentController
					.findNodesByXPath("//img-folders", true, true, true)[0].getTextContent();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		logger.debug("createImagefolder: " + createImagefolder);

		// generate the 'topic' objects
		AuthorNode rootNode = authorDocumentController.findNodesByXPath("/*", true, true, true)[0];
		logger.debug("rootNode name: " + rootNode.getName());

		AuthorNode[] topicAuthorNodes = authorDocumentController.findNodesByXPath("//topic", null, true,
				true, true, false);
		int topicAuthorNodesNumber = topicAuthorNodes.length;
		logger.debug("topicAuthorNodes number: " + topicAuthorNodesNumber);
		List<Topic> topicObjects = new ArrayList<Topic>();

		for (int i = 0, il = topicAuthorNodesNumber; i < il; i++) {
			Topic topicObject = new Topic(authorDocumentController, topicAuthorNodes[i]);
			topicObjects.add(topicObject);
			try {
				logger.debug("title: "
						+ authorDocumentController.findNodesByXPath("//title", topicAuthorNodes[i], true,
								true, true, true)[0].getTextContent());
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.debug("topicObject #" + (i + 1) + ": " + topicObject);
		}
		logger.debug("topicObjects: " + topicObjects);

		// Create keymaps?

		// Create subfolders?
		if (createSubfolder.equals("1")) {

		} else {
			try {
				FileUtils.forceMkdir(new File(projectDir + File.separator + "source"));
				for (int i = 0, il = topicObjects.size(); i < il; i++) {
					Topic topicObject = topicObjects.get(i);
					topicObject.setFilePath("source/" + topicObject.getFilename());
					logger.debug("topicObject #" + (i + 1) + ": " + topicObject);
				}
				logger.debug("topicObjects: " + topicObjects);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Create image subfolders?
		if (createImagefolder.equals("1")) {

		} else {
			try {
				File sourceFolder = new File(projectDir + File.separator + "source");
				FileUtils.forceMkdir(new File(sourceFolder + File.separator + "aa_img"));
				for (int i = 0, il = topicAuthorNodesNumber; i < il; i++) {
					Topic topic = topicObjects.get(i);
					createTopicFile(sourceFolder, topic, templatesDir);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Create the root ditamap
		String topicrefTree = parseTopicObjects(topicObjects);
		logger.debug("topicrefTree: " + topicrefTree);

		createDitamapFile(projectDir, projectName, topicrefTree, templatesDir);

		// FileOutputStream f_out;
		// try {
		// f_out = new FileOutputStream("topicObjects.ser");
		// ObjectOutputStream obj_out = new ObjectOutputStream(f_out);
		// obj_out.writeObject(topicObjects);
		// } catch (FileNotFoundException e1) {
		// e1.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

	}

	/**
	 * @see ro.sync.ecss.extensions.api.AuthorOperation#getArguments()
	 */
	@Override
	public ArgumentDescriptor[] getArguments() {
		return arguments;
	}

	/**
	 * @see ro.sync.ecss.extensions.api.AuthorOperation#getDescription()
	 */
	public String getDescription() {
		return "Execute a void operation.";
	}

	private void createTopicFile(File path, Topic topic, File templatesDir) {
		String fileTitle = topic.getTitle();
		logger.debug("fileTitle: " + fileTitle);

		String fileName = topic.getFilename();
		logger.debug("fileName: " + fileName);

		String fileType = topic.getType();
		logger.debug("fileType: " + fileType);

		String fileContent = null;
		try {
			fileContent = new Scanner(new FileInputStream(new File(templatesDir + File.separator + fileType
					+ ".xml")), "UTF-8").useDelimiter("\\A").next();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		logger.debug("fileContent: " + fileContent);

		fileContent = fileContent.replace("${title}", fileTitle);
		fileContent = fileContent.replace("${id}", fileName.replace(".xml", ""));
		logger.debug("processed fileContent: " + fileContent);

		try {
			FileUtils.writeStringToFile(new File(path + File.separator + fileName), fileContent, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createDitamapFile(File path, String fileTitle, String topicrefTree, File templatesDir) {
		String fileContent = null;
		try {
			fileContent = new Scanner(new FileInputStream(new File(templatesDir + File.separator
					+ "root.ditamap")), "UTF-8").useDelimiter("\\A").next();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		logger.debug("fileContent: " + fileContent);

		fileContent = fileContent.replace("${title}", fileTitle);
		fileContent = fileContent.replace("${topicrefs}", topicrefTree);
		logger.debug("processed fileContent: " + fileContent);

		try {
			FileUtils.writeStringToFile(new File(path + File.separator
					+ (fileTitle.trim().toLowerCase() + ".ditamap").replace(" ", "-")), fileContent,
					"UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String parseTopicObjects(List<Topic> topicObjects) {
		String topicrefTree = "";

		XMLOutputFactory actionsOutputFactory = XMLOutputFactory.newInstance();

		ByteArrayOutputStream parserOutput = new ByteArrayOutputStream();

		XMLStreamWriter streamWriter;
		try {
			streamWriter = actionsOutputFactory.createXMLStreamWriter(parserOutput);

			int topicObjectsNumber = topicObjects.size();

			for (int i = 0, il = topicObjectsNumber; i < il; i++) {
				Topic currentTopicObject = topicObjects.get(i);

				int currentTopicObjectLevel = currentTopicObject.getLevel();
				int nextTopicObjectLevel = (i == topicObjectsNumber - 1) ? 1 : topicObjects.get(i + 1)
						.getLevel();

				if (currentTopicObjectLevel == nextTopicObjectLevel
						|| currentTopicObjectLevel > nextTopicObjectLevel) {
					streamWriter.writeEmptyElement("topicref");
				} else {
					streamWriter.writeStartElement("topicref");
				}

				// TODO: delete start
				streamWriter.writeAttribute("level", Integer.toString(currentTopicObjectLevel));
				// TODO: delete end
				streamWriter.writeAttribute("href", currentTopicObject.getFilePath());
				streamWriter.writeAttribute("navtitle", currentTopicObject.getTitle());
				streamWriter.writeAttribute("format", "dita");
				streamWriter.writeAttribute("type", currentTopicObject.getType());

				if (currentTopicObjectLevel > nextTopicObjectLevel) {
					for (int j = 0, jl = currentTopicObjectLevel - nextTopicObjectLevel; j < jl; j++) {
						streamWriter.writeEndElement();
					}
				}
			}

		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

		try {
			topicrefTree = parserOutput.toString(StandardCharsets.UTF_8.displayName());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return topicrefTree;
	}
}
