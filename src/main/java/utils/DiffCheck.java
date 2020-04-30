package utils;

import java.io.File;
import java.nio.file.Paths;

public class DiffCheck {

	public static void main(String[] args) {
		File[]files = new File("graphsReduced/gml").listFiles();
		long before = 0;
		long after = 0;
		int changedFiles = 0;
		int totalFiles = 0;
		for (File category: files) {
			for (File language : category.listFiles()) {
				if(language.getName().endsWith("bf")){
					long afterSize = (Paths.get("graphsReducedCleaned/gml",category.getName(),language.getName()).toFile().length());
					long beforeSize = (language.length());
					before += beforeSize;
					after += afterSize;
					if(beforeSize < afterSize)
						System.out.println(language);
					if(beforeSize != afterSize){
						changedFiles++;
					}
					totalFiles++;
				}
			}
		}
		System.out.println(before);
		System.out.println(after);
		System.out.println(changedFiles);
		System.out.println(totalFiles);
		System.out.println(((double)before-after)/before);
	}
}
//0.01011974805152936
