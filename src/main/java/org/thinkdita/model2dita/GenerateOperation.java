package org.thinkdita.model2dita;

import javax.swing.text.BadLocationException;

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

		Object[] folderNodeObjects = authorDocumentController.evaluateXPath("//topic[level/text() = 1]",
				currentNode, false, true, true, false, XPathVersion.XPATH_3_0);

		for (int i = 0, il = folderNodeObjects.length; i < il; i++) {
			Object targetNodeObject = folderNodeObjects[i];

			if (targetNodeObject instanceof AuthorElementDomWrapper) {
				AuthorNode targetNode = ((AuthorElementDomWrapper) targetNodeObject).getWrappedAuthorNode();

				logger.debug("targetNode: " + targetNode.getDisplayName() + ", " + targetNode.getName());
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
