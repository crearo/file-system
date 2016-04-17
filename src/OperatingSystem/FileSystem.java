package OperatingSystem;

import java.util.ArrayList;

public class FileSystem {

	int minimumFileSize = 4;

	private FAT fat;
	private MainMemory mainMemory;

	public ArrayList<File> root;

	public FileSystem(FAT fat, MainMemory mainMemory) {
		this.fat = fat;
		this.mainMemory = mainMemory;
		root = new ArrayList<File>();
	}

	public File createFile(String fileName) {
		return createFile(fileName, minimumFileSize);
	}

	public File createFile(String fileName, int size) {

		/* If there isn't enough space, return false, else allocate */
		if (!(fat.getTotalFreeSpace() >= size))
			return null;

		/* If name exists, don't create file */
		for (File each : root)
			if (each.getName().equals(fileName))
				return null;

		/*
		 * Traverse to all but last. For the last, store lastAddress and set it
		 * to -1.
		 */
		File newFile = new File(fileName, size, -1);

		int startingAddress = -1;
		int lastAddress = -1;
		for (int i = 0; i <= size - 1; i++) {
			int currentAddress = fat.getFreeBlock();
			int nextAddress = fat.getNextFreeBlock();
			fat.setElement(currentAddress, nextAddress);

			if (i == 0) {
				startingAddress = currentAddress;
			}
			if (i == (size - 2)) {
				lastAddress = nextAddress;
			}
		}
		fat.setElement(lastAddress, -1);
		newFile.setStartingAddress(startingAddress);
		write(newFile, "\0"); /* Write EOF word to newly created file */
		root.add(newFile);

		return newFile;
	}

	public void deleteFile(File file) {
		int currentAddress = file.getStartingAddress();
		int nextAddress = file.getStartingAddress();
		for (int i = 0; i < file.getSize(); i++) {
			nextAddress = fat.getElement(nextAddress);
			fat.setFree(currentAddress);
			currentAddress = nextAddress;
		}
		root.remove(file);
	}

	public void listFiles() {
		for (File file : root) {
			System.out.println(file.getName());
		}
	}

	public boolean renameFile(File file, String fileName) {

		if (getFileFromName(fileName) != null)
			return false;

		file.setName(fileName);
		return true;
	}

	public File getFileFromName(String name) {
		for (File file : root) {
			if (file.getName().equals(name))
				return file;
		}
		return null;
	}

	public boolean append(File file, String toWrite) {
		return write(file, read(file).indexOf("\0"), toWrite);
	}

	public boolean write(File file, String toWrite) {
		return write(file, 0, toWrite);
	}

	public boolean write(File file, int offset, String toWrite) {
		toWrite += "\0";
		// System.out.println("Requires " + (1 + ((offset + toWrite.length()) /
		// mainMemory.SIZE_OF_BLOCK)) + " blocks");
		/* If file requires more memory, allocate it */
		if (((offset + toWrite.length()) / mainMemory.SIZE_OF_BLOCK) + 1 > file.getSize()) {
			int blocksRequired = 1 + ((offset + toWrite.length()) / mainMemory.SIZE_OF_BLOCK) - file.getSize();
			if (!allocateMemory(file, blocksRequired))
				return false;
		}
		for (int i = 0; i < toWrite.length();) {
			int block = (offset + i) / mainMemory.SIZE_OF_BLOCK;
			int position = (offset + i) % mainMemory.SIZE_OF_BLOCK;
			int lengthToWriteTo = i + mainMemory.SIZE_OF_BLOCK <= toWrite.length() ? i + mainMemory.SIZE_OF_BLOCK : toWrite
					.length();
			mainMemory.writeToMemory(file.getStartingAddress() + block, position, toWrite.substring(i, lengthToWriteTo));
			i += (mainMemory.SIZE_OF_BLOCK - position);
		}
		return true;
	}

	public String read(File file) {
		String toRead = "";
		int next = file.getStartingAddress();
		while (next != -1) {
			toRead += mainMemory.readFromMemory(next);
			next = fat.getElement(next);
		}
		return toRead;
	}

	public boolean allocateMemory(File file, int blocksRequired) {
		if (fat.getTotalFreeSpace() >= blocksRequired) {
			System.out.println("Allocating " + blocksRequired + " blocks to file " + file.getName());
			int lastIndex = -1;
			int next = file.getStartingAddress();
			while (next != -1) {
				lastIndex = next;
				next = fat.getElement(next);
			}
			for (int i = 0; i < blocksRequired; i++) {
				int firstFree = fat.getFreeBlock();
				fat.setElement(lastIndex, firstFree);
				fat.setElement(firstFree, -1);
				lastIndex = firstFree;
			}

			file.setSize(file.getSize() + blocksRequired);
			return true;
		} else {
			System.out.println("Unable to Allocate More Memory");
			return false;
		}

	}
}