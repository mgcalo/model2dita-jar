package org.thinkdita.model2dita;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import org.junit.Test;
import org.thinkdita.model2dita.Topic;

public class GenerateTopicrefTreeTest {

	@Test
	public void test1() {
		FileInputStream fis;
		List<Topic> topicObjects = null;
		
		try {
			fis = new FileInputStream("topicObjects.ser");
			// Read object using ObjectInputStream
			ObjectInputStream ois = new ObjectInputStream(fis);

			// Read an object
			topicObjects = (List<Topic>) ois.readObject();			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(topicObjects);

	}
}
