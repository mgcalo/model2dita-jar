package org.thinkdita.model2dita;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class GenerateTopicrefTreeTest {

	@Test
	public void test1() {
		FileInputStream fis;
		List<Topic> topicObjects = null;

		try {
			fis = new FileInputStream(new File(getClass().getResource("topicObjects.ser").toURI()));
			ObjectInputStream ois = new ObjectInputStream(fis);

			topicObjects = (List<Topic>) ois.readObject();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		String topicrefTree = GenerateOperation.parseTopicObjects(topicObjects);

		try {
			FileUtils.writeStringToFile(new File("topicObjects.xml"), topicrefTree);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

}
