package com.boomi.proserv.swift;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

/**
 * Basic Test Class to be executed from Eclipse as it is using the user directory
 * @author anthony.rabiaza@gmail.com
 *
 */
class TestCase {

	@Test
	void test() {

		try {
			System.out.println("--Start--");
			String folder = System.getProperty("user.dir") + "/";

			String filename = folder + "MT103.txt";
			String swift1 = SwiftToXML.readFile(filename);

			System.out.println("Swift message:");
			System.out.println(swift1);

			InputStream is = new FileInputStream(filename);
			InputStream os = SwiftToXML.parseSwift(is);

			String xml = SwiftToXML.inputStreamToString(os);
			System.out.println("XML message:");
			System.out.println(xml);

			InputStream os2 = SwiftToXML.parseXML(SwiftToXML.stringToInputStream(xml));
			String swift2 = SwiftToXML.inputStreamToString(os2);

			System.out.println("Swift message (back):");
			System.out.println(swift2);
			
			System.out.println("--Done--");

			assertEquals(swift1.replaceAll("[\r\n]",""),swift2.replaceAll("[\r\n]",""));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	void test2() {

		try {
			System.out.println("--Start--");
			String folder =  System.getProperty("user.dir") + "/";

			String filename = folder + "MT103_REMOVE.xml";
			String xml1 = SwiftToXML.readFile(filename);

			System.out.println("XML message:");
			System.out.println(xml1);
			
			String swift1 = SwiftToXML.parseXML(xml1);
			System.out.println("Swift message:");
			System.out.println(swift1);
			
			System.out.println("--Done--");

			assert(!swift1.contains("REMOVE"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	void test3() {

		try {
			System.out.println("--Start--");
			String folder =  System.getProperty("user.dir") + "/";

			String filename = folder + "MT564.txt";
			String swift1 = SwiftToXML.readFile(filename);

			System.out.println("Swift message:");
			System.out.println(swift1);

			InputStream is = new FileInputStream(filename);
			InputStream os = SwiftToXML.parseSwift(is);

			String xml = SwiftToXML.inputStreamToString(os);
			System.out.println("XML message:");
			System.out.println(xml);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
