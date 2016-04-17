package OperatingSystem;

import java.util.Random;

public class Main {

	public static void main(String arg[]) throws InterruptedException {

		int size = 20;

		java.io.File mainDirectory = new java.io.File("FILE_SYSTEM");
		mainDirectory.mkdirs();

		MainMemory mainMemory = new MainMemory(size);
		FAT fat = new FAT(size);
		FileSystem fileSystem = new FileSystem(fat, mainMemory);

		System.out.println("This is our FAT visualizer. " + "\nIt draws the FAT table at every iteration. "
				+ "\nA new file is created/deleted with some probablity."
				+ "\nA line is written if a file is created/deleted during that run." + "\n\n\n");

		for (int i = 0; i < 1000; i++) {
			if (new Random().nextInt(10) > 4) {
				File file = fileSystem.createFile(generateRandomString(), 2 + new Random().nextInt(4));
				if (file != null)
					System.out.println("Created File, " + file.getName() + " " + file.getSize());
			}
			if (new Random().nextInt(10) > 7) {
				if (fileSystem.root.size() > 0) {
					File toDelete = fileSystem.root.get(new Random().nextInt(fileSystem.root.size()));
					if (toDelete != null) {
						fileSystem.deleteFile(toDelete);
						System.out.println("Deleted File " + toDelete.getName() + " " + toDelete.getSize());
					}
				}
			}
			fat.drawFAT(fileSystem.root);
			// fat.drawFAT();
			// Thread.sleep(50);

		}
		System.out.println("\n\n------------------------------------------------"
				+ "\n\nPlease Go To The Top And Read Description of this Visualizer"
				+ "\n\n-----------------------------------------------");
	}

	private static String generateRandomString() {
		String word = "";
		for (int i = 0; i < 1; i++) {
			word += (char) ('A' + new Random().nextInt(26));
		}
		return word;
	}

}