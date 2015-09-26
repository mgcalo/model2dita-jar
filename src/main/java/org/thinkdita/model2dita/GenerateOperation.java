package org.thinkdita.model2dita;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.BadLocationException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ro.sync.annotations.api.API;
import ro.sync.annotations.api.APIType;
import ro.sync.annotations.api.SourceType;
import ro.sync.ecss.dom.wrappers.AuthorElementDomWrapper;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.XPathVersion;
import ro.sync.ecss.extensions.api.access.AuthorEditorAccess;
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

		File projectsDir = new File(oxygenInstallDir + File.separator + "projects" + File.separator
				+ projectName);
		try {
			FileUtils.forceMkdir(projectsDir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.debug("projectsDir: " + projectsDir);

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
		AuthorNode rootNode = authorDocumentController.findNodesByXPath("/*", true,
				true, true)[0];
		logger.debug("rootNode name: " + rootNode.getName());

		AuthorNode[] topicAuthorNodes = authorDocumentController.findNodesByXPath("topic", rootNode, true,
				true, true, false);
		logger.debug("topicAuthorNodes number: " + topicAuthorNodes.length);
		List<Topic> topicObjects = new ArrayList<Topic>();

		for (int i = 0, il = topicAuthorNodes.length; i < il; i++) {
			Topic topicObject = new Topic(authorDocumentController, topicAuthorNodes[i]);
			topicObjects.add(i, topicObject);
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
				FileUtils.forceMkdir(new File(projectsDir + File.separator + "source"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Create image subfolders?
		if (createImagefolder.equals("1")) {

		} else {
			try {
				FileUtils.forceMkdir(new File(projectsDir + File.separator + "source" + File.separator
						+ "aa_img"));

			} catch (IOException e) {
				e.printStackTrace();
			}
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
}
