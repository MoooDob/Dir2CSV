package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.io.FilenameUtils;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

public class Dir2CSV extends Application {

	// Files with this extension will be shown, null or empty array => all files 
	final String[] fileExtensionFilter = {}; // {"java", "cpp", "h"} // null /*=> all*/

	// files with this extension will shown using their dimension (max line length x lines),
	// other files will be shown using an equal sized rounded rectangle
	// null or empty array => show all files with dimensions
	final String[] dimensionDisplayExtensionFilter = {}; // {"java"}

	// files with this file name will be explicitly shown using their dimension 
	// (max line length x lines)
	final String[] dimensionDisplayFilenameFilter = {}; // {"readme.md"}

	// **********************


	@Override
	public void start(Stage stage) {

		// ask for directory

		DirectoryChooser dc = new DirectoryChooser();
		File selectedDirectory = dc.showDialog(stage);

		if (selectedDirectory == null) {
			System.out.println("No directory selected. Terminated.");
		} else {

			FileWriter csvWriter;
			try {
				csvWriter = new FileWriter(selectedDirectory.getName() + ".csv");
				csvWriter.append(String.format("\"parentDir\",\"name\",\"fullname\",\"path\",\"type\",\"width\",\"height\"\n"));
				convertDirectory(csvWriter, selectedDirectory);
				csvWriter.flush();
				csvWriter.close();
				System.out.println(selectedDirectory.getName() + ".csv succesful written.");
			} catch (IOException e) {
				System.out.println("Problem while writing to .csv file");
			} 

		}
	}

	private void convertDirectory(FileWriter csvWriter, File directory) throws IOException {
		
		String currentFullDir = directory.getAbsolutePath();

		int numSubDirs = 0;
		int fileAndFolderCntr = 1;
		
		// Read child files and folders, filter them 
		String[] subFilesAndDirectories = directory.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (fileExtensionFilter == null || fileExtensionFilter.length == 0 ) {
					// No Filter defined
					return true;
				} else {
					// check if file extension is in the list of allowed extensions
					return Arrays.stream(fileExtensionFilter).anyMatch(FilenameUtils.getExtension(name.toLowerCase())::equals);
				}
			};
		});			

		// loop over all files and folders
		for (String fileOrDirectoryName : subFilesAndDirectories) {
			
			File fileOrDirectory = new File(directory, fileOrDirectoryName);

			if (fileOrDirectory.isDirectory()) {
				csvWriter.append(String.format(Locale.US, "%s:::%d,%s,%s,%s,%s,%d,%d\n", currentFullDir, fileAndFolderCntr, fileOrDirectory.getName(), fileOrDirectory.getAbsoluteFile(), currentFullDir, "folder", 0, 0));
				convertDirectory(csvWriter, fileOrDirectory);
				numSubDirs++;
			} else {
				// analyse file to get height and width
				int lineCtr = 0;
				int maxLineLength = 0;

				try {
					Scanner scanner = new Scanner(fileOrDirectory);
					while (scanner.hasNextLine()) {
						String line = scanner.nextLine();
						maxLineLength = Math.max(maxLineLength, line.length());
						lineCtr++;
					}
					scanner.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}

				csvWriter.append(String.format(Locale.US, "%s:::%d,%s,%s,%s,%s,%d,%d\n", currentFullDir, fileAndFolderCntr,  fileOrDirectory.getName(), fileOrDirectory.getAbsoluteFile(), currentFullDir, "file", maxLineLength, lineCtr));
			}
			
			fileAndFolderCntr++;

		}
	}

	public static void main(String args[]) {
		launch(args);
	}
}

