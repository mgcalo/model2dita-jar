package org.thinkdita.model2dita;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class GenerateTopicrefTreeTest {

	@Test
	public void test1() {
		FileInputStream fis;
		List<Topic> topicObjects = null;

		try {
			fis = new FileInputStream(new File(getClass().getResource("topicObjects.ser").toURI()));
			ObjectInputStream ois = new ObjectInputStream(fis);

			topicObjects = (List<Topic>) ois.readObject();
			ois.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		String topicrefTree = GenerateOperation.parseTopicObjects(topicObjects, "topicref");

		try {
			FileUtils.writeStringToFile(new File("topicObjects.xml"), topicrefTree);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	@Test
	public void test2() {
		FileInputStream fis;
		List<Topic> topicObjects = null;

		try {
			fis = new FileInputStream(new File(getClass().getResource("topicObjects.ser").toURI()));
			ObjectInputStream ois = new ObjectInputStream(fis);

			topicObjects = (List<Topic>) ois.readObject();
			ois.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		Map<String, List<Topic>> topicObjectsByParentFolderPath = topicObjects.stream()
				.collect(Collectors.groupingBy(Topic::getRelativeParentFolderPath));

		System.out.println(topicObjects.size());
		System.out.println(topicObjectsByParentFolderPath.size());
		for (String relativeParentFolderPath : topicObjectsByParentFolderPath.keySet()) {
			List<Topic> topicList = topicObjectsByParentFolderPath.get(relativeParentFolderPath);
			String topicrefTree = GenerateOperation.parseTopicObjects(topicList, "topicref");
			topicrefTree = topicrefTree.substring(topicrefTree.indexOf(">") + 1, topicrefTree.lastIndexOf("<"));
			System.out.println(topicrefTree);
		}
	}
}
