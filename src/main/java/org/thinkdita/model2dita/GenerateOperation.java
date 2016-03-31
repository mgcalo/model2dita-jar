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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.stream.Collectors;

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
	@SuppressWarnings("resource")
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap args) throws AuthorOperationException {
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

		String projectName = "";
		try {
			projectName = authorDocumentController.findNodesByXPath("//projectname", true, true, true)[0]
					.getTextContent();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		String projectFileName = projectName.trim().toLowerCase().replace(" ", "-");
		logger.debug("projectName: " + projectName);
		logger.debug("processedProjectName: " + projectFileName);

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

		File sourceFolder = new File(projectDir + File.separator + "source");
		logger.debug("sourceFolder: " + sourceFolder);

		String createSubfolders = "0";
		try {
			createSubfolders = authorDocumentController.findNodesByXPath("//subfolders", true, true, true)[0]
					.getTextContent();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		logger.debug("createSubfolder: " + createSubfolders);

		String createImageSubfolders = "0";
		try {
			createImageSubfolders = authorDocumentController.findNodesByXPath("//img-folders", true, true,
					true)[0].getTextContent();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		logger.debug("createImageSubfolders: " + createImageSubfolders);

		String createKeymaps = "0";
		try {
			createKeymaps = authorDocumentController.findNodesByXPath("//keymaps", true, true, true)[0]
					.getTextContent();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		logger.debug("createKeymaps: " + createKeymaps);

		String createSubmaps = "0";
		try {
			createSubmaps = authorDocumentController.findNodesByXPath("//submaps", true, true, true)[0]
					.getTextContent();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		logger.debug("createSubmaps: " + createSubmaps);

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
			topicObject.setRelativeFilePath("source/" + topicObject.getFilename());
			topicObjects.add(topicObject);
			try {
				logger.debug("title: "
						+ authorDocumentController.findNodesByXPath("//title", topicAuthorNodes[i], true,
								true, true, true)[0].getTextContent());
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			logger.debug("topicObject #" + (i + 1) + ": " + topicObject);
		}
		logger.debug("topicObjects: " + topicObjects);

		// create keymaps
		if (createKeymaps.equals("1")) {
			try {
				FileUtils.copyFile(new File(templatesDir + File.separator + "prod-keys.ditamap"), new File(
						projectDir + File.separator + "prod-keys.ditamap"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// create subfolders
		if (createSubfolders.equals("1")) {
			File currentSubfolder = sourceFolder;
			String currentRelativeSubfolder = "source/";

			for (int i = 0, il = topicAuthorNodesNumber; i < il; i++) {
				Topic topicObject = topicObjects.get(i);
				if (topicObject.getLevel() == 1) {
					File subfolder = new File(sourceFolder + File.separator
							+ topicObject.getSubfolderName());
					try {
						FileUtils.forceMkdir(subfolder);
					} catch (IOException e) {
						e.printStackTrace();
					}
					currentSubfolder = subfolder;
					currentRelativeSubfolder = "source/" + topicObject.getSubfolderName();
					logger.debug("subfolder: " + subfolder);
					logger.debug("currentRelativeSubfolder: " + currentRelativeSubfolder);
				}

				topicObject.setRelativeFilePath(currentRelativeSubfolder + "/" + topicObject.getFilename());
				topicObject.setRelativeParentFolderPath(currentRelativeSubfolder);
				createTopicFile(currentSubfolder, topicObject, templatesDir);
			}
		} else {
			for (int i = 0, il = topicAuthorNodesNumber; i < il; i++) {
				Topic topic = topicObjects.get(i);
				createTopicFile(sourceFolder, topic, templatesDir);
			}
		}

		// create image subfolders
		if (createImageSubfolders.equals("1") && createSubfolders.equals("1")) {
			for (int i = 0, il = topicAuthorNodesNumber; i < il; i++) {
				Topic topicObject = topicObjects.get(i);
				if (topicObject.getLevel() == 1) {
					File imgSubfolder = new File(sourceFolder + File.separator
							+ topicObject.getSubfolderName() + File.separator + "aa_img");
					try {
						FileUtils.forceMkdir(imgSubfolder);
					} catch (IOException e) {
						e.printStackTrace();
					}
					logger.debug("imgSubfolder: " + imgSubfolder);
				}
			}
		} else {
			try {
				FileUtils.forceMkdir(new File(sourceFolder + File.separator + "aa_img"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// create the root ditamap
		String topicrefTree = parseTopicObjects(topicObjects, "topicref");
		logger.debug("topicrefTree: " + topicrefTree);

		File rootDitamapFile = createDitamapFile(projectDir, projectName, projectFileName, topicrefTree,
				templatesDir);

		// Create the project file
		createProjectFile(projectDir, projectName, projectFileName, templatesDir);

		if (createKeymaps.equals("1") && createSubfolders.equals("1") && createImageSubfolders.equals("1")) {
			try {
				FileUtils.copyFile(new File(templatesDir + File.separator + "img-keys.ditamap"), new File(
						projectDir + File.separator + "img-keys.ditamap"));

				Map<String, String> filters = new HashMap<String, String>();
				String keysSectionText = new Scanner(new FileInputStream(new File(templatesDir
						+ File.separator + "keys-section.txt")), StandardCharsets.UTF_8.displayName())
						.useDelimiter("\\A").next();
				filters.put("</title>", "</title>" + keysSectionText);
				filterFile(rootDitamapFile, filters);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// cases d,e and g
		if ((createKeymaps.equals("1") && createSubfolders.equals("1") && createImageSubfolders.equals("0"))
				|| (createKeymaps.equals("1") && createSubfolders.equals("0") && (createImageSubfolders
						.equals("0") || createImageSubfolders.equals("1")))) {
			try {
				FileUtils.copyFile(new File(templatesDir + File.separator + "img-keys.ditamap"), new File(
						sourceFolder + File.separator + "aa_img" + File.separator + "img-keys.ditamap"));

				Map<String, String> filters = new HashMap<String, String>();
				String keysSectionText = new Scanner(new FileInputStream(new File(templatesDir
						+ File.separator + "keys-section.txt")), StandardCharsets.UTF_8.displayName())
						.useDelimiter("\\A").next();
				filters.put("</title>", "</title>" + keysSectionText);
				filterFile(rootDitamapFile, filters);

				filters = new HashMap<String, String>();
				filters.put("img-keys.ditamap", "source/aa_img/img-keys.ditamap");
				filterFile(rootDitamapFile, filters);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (createSubfolders.equals("1")
				&& (createImageSubfolders.equals("0") || createImageSubfolders.equals("1"))
				&& createSubmaps.equals("1")) {
			Map<String, List<Topic>> topicObjectsByParentFolderPath = topicObjects.stream().collect(
					Collectors.groupingBy(Topic::getRelativeParentFolderPath));

			for (String relativeParentFolderPath : topicObjectsByParentFolderPath.keySet()) {
				List<Topic> topicSublist = topicObjectsByParentFolderPath.get(relativeParentFolderPath);
				int topicSublistSize = topicSublist.size();
				logger.debug("topicSublistSize = " + topicSublistSize);

				String topicrefSubTree = "";
				if (topicSublistSize > 1) {
					topicrefSubTree = GenerateOperation.parseTopicObjects(topicSublist, "topicref");
					topicrefSubTree = topicrefSubTree.substring(topicrefSubTree.indexOf(">") + 1,
							topicrefSubTree.lastIndexOf("<"));
				}
				topicrefSubTree = topicrefSubTree.replace(relativeParentFolderPath + "/", "");
				logger.debug("topicrefSubTree = " + topicrefSubTree);

				createDitamapFile(new File(projectDir + File.separator + relativeParentFolderPath),
						projectName, "s_" + topicSublist.get(0).getSubfolderName(), topicrefSubTree,
						templatesDir);
			}
		}

		FileOutputStream f_out;
		try {
			f_out = new FileOutputStream("topicObjects.ser");
			ObjectOutputStream obj_out = new ObjectOutputStream(f_out);
			obj_out.writeObject(topicObjects);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

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
					+ ".xml")), StandardCharsets.UTF_8.displayName()).useDelimiter("\\A").next();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		logger.debug("fileContent: " + fileContent);

		fileContent = fileContent.replace("${title}", fileTitle);
		fileContent = fileContent.replace("${id}", fileName.replace(".xml", ""));
		logger.debug("processed fileContent: " + fileContent);

		try {
			FileUtils.writeStringToFile(new File(path + File.separator + fileName), fileContent,
					StandardCharsets.UTF_8.displayName());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private File createDitamapFile(File parentFolderPath, String projectName, String fileName,
			String topicrefTree, File templatesDir) {
		String fileContent = null;
		try {
			fileContent = new Scanner(new FileInputStream(new File(templatesDir + File.separator
					+ "root.ditamap")), StandardCharsets.UTF_8.displayName()).useDelimiter("\\A").next();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		logger.debug("root.ditamap file content: " + fileContent);

		fileContent = fileContent.replace("${title}", projectName);
		fileContent = fileContent.replace("${topicrefs}", topicrefTree);
		logger.debug("processed root.ditamap file content: " + fileContent);

		File rootDitamapFile = new File(parentFolderPath + File.separator + fileName + ".ditamap");

		try {
			FileUtils.writeStringToFile(rootDitamapFile, fileContent, StandardCharsets.UTF_8.displayName());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return rootDitamapFile;
	}

	private void createProjectFile(File projectDir, String projectName, String projectFileName,
			File templatesDir) {
		String fileContent = null;
		try {
			fileContent = new Scanner(new FileInputStream(new File(templatesDir + File.separator
					+ "projectname.xpr")), StandardCharsets.UTF_8.displayName()).useDelimiter("\\A").next();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		logger.debug("projectname.xpr file content: " + fileContent);

		fileContent = fileContent.replace("${projectName}", projectName);
		fileContent = fileContent.replace("${projectFileName}", projectFileName);
		logger.debug("processed projectname.xpr file content: " + fileContent);

		try {
			FileUtils.writeStringToFile(new File(projectDir + File.separator + projectFileName + ".xpr"),
					fileContent, StandardCharsets.UTF_8.displayName());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String parseTopicObjects(List<Topic> topicObjects, String elementName) {
		logger.debug("started parseTopicObjects()");

		String topicrefTree = "";

		XMLOutputFactory actionsOutputFactory = XMLOutputFactory.newInstance();

		ByteArrayOutputStream parserOutput = new ByteArrayOutputStream();

		XMLStreamWriter streamWriter = null;
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
					streamWriter.writeEmptyElement(elementName);
				} else {
					streamWriter.writeStartElement(elementName);
				}

				streamWriter.writeAttribute("href", currentTopicObject.getRelativeFilePath());
				streamWriter.writeAttribute("navtitle", currentTopicObject.getTitle());
				streamWriter.writeAttribute("format", "dita");
				streamWriter.writeAttribute("type", currentTopicObject.getType());

				if (currentTopicObjectLevel > nextTopicObjectLevel) {
					for (int j = 0, jl = currentTopicObjectLevel - nextTopicObjectLevel; j < jl; j++) {
						streamWriter.writeEndElement();
					}
				}

				logger.debug("streamWriter = " + currentTopicObject.toString());
			}

			streamWriter.writeEndDocument();
			streamWriter.flush();

		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

		try {

			logger.debug("parserOutput = " + parserOutput.toString());
			topicrefTree = parserOutput.toString(StandardCharsets.UTF_8.displayName());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		logger.debug("ended parseTopicObjects()");
		return topicrefTree;
	}

	private void filterFile(File file, Map<String, String> filters) {
		String fileContent = "";
		try {
			fileContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8.displayName());
			logger.debug("rootDitamapContent: " + fileContent);

			for (Entry<String, String> filter : filters.entrySet()) {
				String filterKey = filter.getKey();
				String filterValue = filter.getValue();
				fileContent = fileContent.replace(filterKey, filterValue);
				logger.debug("filterKey: " + filterKey);
				logger.debug("filterValue: " + filterValue);
				logger.debug("rootDitamapContent: " + fileContent);
			}

			FileUtils.writeStringToFile(file, fileContent, StandardCharsets.UTF_8.displayName());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
