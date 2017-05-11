package assignment3;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker.State;
import javafx.stage.Stage;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.event.EventHandler;
import javafx.util.Duration;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebHistory.Entry;

/** This class represents an expanded web browser made using JavaFX.
 * 
 * @author Eric Maryan
 *
 */
public class MyGUI extends Application implements Serializable{
	protected WebEngine engine;
	
	public static void main(String[] args) {		launch(args);	}
	
	BorderPane root = new BorderPane();
	Scene scene = new Scene(root, 800, 600);
	HBox hLayout = new HBox();
	VBox vLayout = new VBox();
	MenuBar mb = new MenuBar();
	Menu fileMenu = new Menu("File");
	MenuItem quit = new MenuItem("Quit");
	Menu bookmarks = new Menu("Bookmarks");
	Menu help = new Menu("Help");
	Menu settings = new Menu("Settings");
	MenuItem homepage = new MenuItem("Homepage");
	MenuItem downloads = new MenuItem("Downloads");
	MenuItem javaScript = new MenuItem("javaScript");
	MenuItem jClass = new MenuItem("Get help for Java class");
	CheckMenuItem sHistory = new CheckMenuItem("Show History");
	MenuItem about = new MenuItem("About");
	Button back = new Button ("Back");
	Button bookmark = new Button ("Add Bookmark");
	Button forward = new Button ("Forward");
	TextField tField = new TextField();
	ArrayList<String> savedBookmarks = new ArrayList<>();
	Tooltip b = new Tooltip("Go back one page.");
	Tooltip f = new Tooltip("Go forward one page.");
	Tooltip ab = new Tooltip("Add this website to your bookmarks.");
	/** A String that stores the homepage. The default value is google.ca.*/
	String hp = "http://www.google.ca";
	/** A String that stores the download directory.*/
	String drct = "./";

	/** This method returns the download directory. 
	 * 
	 * @return A string that stores the download directory.
	 */
	public String getDownloadDirectory() {
		return drct;
	}
	
	/** This method contains the JavaFX elements for the browser
	 * 
	 * @param primaryStage The primary stage for this application
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		WebView browserView = new WebView();
		engine = browserView.getEngine();
		primaryStage.setTitle("Assignment 3");
		primaryStage.setScene(scene);
		HBox.setHgrow(tField, Priority.ALWAYS);
		hLayout.getChildren().addAll(back, tField, bookmark, forward);	
		fileMenu.getItems().add(quit);	
		help.getItems().addAll(jClass, sHistory, about);
		settings.getItems().addAll(homepage, downloads, javaScript);
		mb.getMenus().addAll(fileMenu, bookmarks, help, settings);
		ObservableList<WebHistory.Entry> wh = engine.getHistory().getEntries();
		ListView<Entry> lv = new ListView<>(wh);
		lv.setMaxSize(0,0);
		
		BrowserHandler bh = new BrowserHandler();
		back.setOnMouseClicked(bh);
		bookmark.setOnMouseClicked(bh);
		forward.setOnMouseClicked(bh);
		tField.setOnMouseClicked(bh);

		Tooltip.install(back, b);
		Tooltip.install(forward, f);
		Tooltip.install(bookmark, ab);
		
		vLayout.getChildren().addAll(mb, hLayout);
		root.setTop(vLayout);
		root.setCenter(browserView);
		root.setRight(lv);
		engine.load(hp);
		
		tField.setOnKeyPressed(event ->{
			/** A String that stores the URL the user enters*/
			String search;
			if(event.getCode() == KeyCode.ENTER){
				search = tField.getText();
				engine.load(search);
			}
		});
		
		quit.setOnAction(event -> {
			saveSettings(primaryStage);
			Platform.exit(); 
		});
	
		jClass.setOnAction(event -> {
			TextInputDialog dialog = new TextInputDialog("Type here");
			/** A String that stores the search URL for the java class the user would like to research*/
			String jSearch;
			dialog.setTitle("Find help for java class");
			dialog.setHeaderText("Search for Java Class Documentation");
			dialog.setContentText("Which Java class do you want to research?");
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent()){
				jSearch = "https://www.google.ca/search?q=java+" + 	result.get() + "&sourceid=chrome&ie=UTF-8";
				engine.load(jSearch);
			}
		});
		
		sHistory.setOnAction(event -> {
			if (sHistory.isSelected() == true){	
				RotateTransition rt = new RotateTransition(Duration.millis(1000), lv);
				ScaleTransition st = new ScaleTransition(Duration.millis(1000), lv);
				ParallelTransition pt = new ParallelTransition(rt,st);  
				lv.setOpacity(1);
	    		lv.setTranslateX(0);
				rt.setFromAngle(0);		
				rt.setToAngle(360);
				st.setFromX(0);
				st.setFromY(0);
				st.setByX(1.0); 		
				st.setByY(1.0);		
				lv.setMaxSize(7000, 1000);
				pt.play();
			}
			else {
				FadeTransition ft = new FadeTransition(Duration.millis(500), lv);
				TranslateTransition tt = new TranslateTransition(Duration.millis(500), lv);
				SequentialTransition seqt = new SequentialTransition(tt, ft);
				tt.setByX(-552);			
				ft.setFromValue(1);
				ft.setToValue(0);	
				seqt.setOnFinished(evt -> lv.setMaxWidth(0));
				seqt.play();
			}
		});
		
		about.setOnAction(event -> {
			Alert alert = new Alert(  AlertType.INFORMATION  ); 
			alert.setTitle("Information Dialog");
			alert.setHeaderText("About");	
			alert.setContentText("Eric Maryan's browser, v1.0. March 9, 2016");
			alert.show();
		});
		
		homepage.setOnAction(event -> {
			TextInputDialog dialog = new TextInputDialog("Type here");
			dialog.setTitle("Homepage");
			dialog.setHeaderText("Type in a URL to make it your homepage.");
			dialog.setContentText("URL: ");
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent()){
				hp = result.get();
			}
		});
		
		javaScript.setOnAction(event -> {
			TextInputDialog dialog = new TextInputDialog("Type here");
			dialog.setTitle("javaScript");
			dialog.setHeaderText("Type in a javaScript command.");
			dialog.setContentText("Command: ");
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent()){
				/** A String that stores the javaScript command entered by the user.*/
				String javaScriptCommand = result.get();
				engine.executeScript(javaScriptCommand);
			}
		});
		
		engine.setOnAlert(evt -> {
			Alert alert = new Alert(  AlertType.INFORMATION  ); 
			alert.setTitle("javaScript");
			alert.setHeaderText("Data");	
			alert.setContentText(evt.getData());
			alert.show();
		});
		
		
		downloads.setOnAction(event -> {
			TextInputDialog dialog = new TextInputDialog("Type here");
			dialog.setTitle("Downloads");
			dialog.setHeaderText("Type in a directory for downloading files.");
			dialog.setContentText("Directory: ");
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent()){
				/** A String that stores the download directory entered by the user.*/
				String newpath = result.get();
				if(Files.isWritable(Paths.get(newpath)) && Files.exists(Paths.get(newpath)))
				{
					drct = newpath;
				}
				else if (!Files.exists(Paths.get(newpath))){
					try {
						Files.createDirectory(Paths.get(newpath));
						if (Files.isWritable(Paths.get(newpath))){
							drct = newpath;
						}
						else {
							Alert alert = new Alert(  AlertType.INFORMATION  ); 
							alert.setTitle("No permissions");
							alert.setHeaderText("Warning");	
							alert.setContentText(newpath + " does not have write permissions.");
							alert.show();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else{
					Alert alert = new Alert(  AlertType.INFORMATION  ); 
					alert.setTitle("No permissions");
					alert.setHeaderText("Warning");	
					alert.setContentText(newpath + " does not have write permissions.");
					alert.show();
				}
			}
		});
		
		quit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
		about.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN));
		jClass.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN));
		sHistory.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));

		browserView.setOnKeyPressed(event ->{
			if(event.isControlDown() == true && event.getCode() == KeyCode.LEFT){
				goBack();
			}
			if(event.isControlDown() == true && event.getCode() == KeyCode.RIGHT){
				goForward();
			}
		});
		
		primaryStage.setOnCloseRequest(event -> {
			saveSettings(primaryStage);
		});
		
		lv.getSelectionModel().selectedItemProperty().addListener(evt ->{
			final WebHistory history=engine.getHistory();
			ObservableList<WebHistory.Entry> entryList=history.getEntries();
			/** An int that stores the current index for the user's web history*/
			int currentIndex=history.getCurrentIndex();
			/** An int that stores the index of the URL clicked on by the user*/
			int clickIndex =  lv.getSelectionModel().getSelectedIndex();
			engine.getHistory().go( clickIndex -  currentIndex);
			tField.setText(lv.getItems().get(clickIndex).getUrl());
		});

		//This is a 3-parameter Lambda function for listening for changes
		// of state for the web page loader.				VVV  VVV         VVV
		engine.getLoadWorker().stateProperty().addListener(( ov, oldState,  newState)->
		{
			// This if statement gets run if the new page load succeeded.
			if (newState == State.SUCCEEDED) {
				tField.setText(engine.getLocation());
				
				if (savedBookmarks.contains(engine.getLocation())){
					bookmark.setDisable(true);
				}
				else {
					bookmark.setDisable(false);
				}
				
				final WebHistory history=engine.getHistory();
				    ObservableList<WebHistory.Entry> entryList=history.getEntries();
				    /** An int that stores the current index for the user's web history*/
				    int currentIndex=history.getCurrentIndex();
				   
				    if (currentIndex == 0){
				    	back.setDisable(true);
				    }
				    else{
				    	back.setDisable(false);
				    }
				    if (entryList.size() == currentIndex+1){
				    	forward.setDisable(true);
				    }
				    else{
				    	forward.setDisable(false);
				    }
			}
			else if (newState == State.SCHEDULED){
				back.setDisable(true);
				forward.setDisable(true);
				bookmark.setDisable(true);
			}
		});
		
		// monitor the location url, and if newLoc ends with one of the download file endings, create a new DownloadTask.
				engine.locationProperty().addListener(new ChangeListener<String>() {
					@Override public void changed(ObservableValue<? extends String> observableValue, String oldLoc, String newLocation) {
						if(newLocation.endsWith("exe") || newLocation.endsWith("pdf") || newLocation.endsWith("zip") || newLocation.endsWith("doc")
								|| newLocation.endsWith("docx") || newLocation.endsWith("xls") || newLocation.endsWith("xlsx") || newLocation.endsWith("iso")
								|| newLocation.endsWith("img") || newLocation.endsWith("dmg") || newLocation.endsWith("tar") || newLocation.endsWith("gz") || newLocation.endsWith("jar"))
						{
							DownloadBar newDownload = new DownloadBar(newLocation, drct);		  
						}
					}
				});	
		readSettings(primaryStage);
		engine.load(hp);
		primaryStage.show();
	}

	/** This method allows the user to go back 1 page in their history
	 * 
	 */
	public void goBack()
	{    
		final WebHistory history=engine.getHistory();
		ObservableList<WebHistory.Entry> entryList=history.getEntries();
		/** An int that stores the current index for the user's web history*/
		int currentIndex=history.getCurrentIndex();

		if(currentIndex > 0)
		{			// 		  VVV  This is a no-parameter Lambda function run();
			Platform.runLater( () -> { 
	    			history.go(-1); 
	    			/** A String that stores the next address in the user's history*/
	    			final String nextAddress = history.getEntries().get(currentIndex - 1).getUrl();
			} );
	    }
	 }
	
	//Tell the engine to go forward 1 page in the history
	/** This method allows the user to go forward 1 page in their history
	 *  
	 */
	public void goForward()
	{    
	    final WebHistory history=engine.getHistory();
	    ObservableList<WebHistory.Entry> entryList=history.getEntries();
		/** An int that stores the current index for the user's web history*/
	    int currentIndex=history.getCurrentIndex();
	    
	    if(currentIndex + 1 < entryList.size())
	    {	    					//This is a no-parameter Lambda function run();
	    		Platform.runLater( () -> { 
	    			history.go(1); 
	    			/** A String that stores the next address in the user's history*/
	    			final String nextAddress = history.getEntries().get(currentIndex + 1).getUrl();
	    		});
	    }    
	}
	
	/** This inner class is used to handle click events for the browser bar.
	 * 
	 */
	private class BrowserHandler implements EventHandler<MouseEvent>{
		
		public void handle(MouseEvent event) {
			
			if (event.getSource() == back){
				goBack();
			}
			
			else if (event.getSource() == tField){
				if (event.getClickCount() == 1){
					tField.selectAll();
				}
				if (event.getClickCount() == 2){
					tField.setText("");
				}
			}
			
			else if (event.getSource() == bookmark){
				if (!savedBookmarks.contains(engine.getLocation())){
					savedBookmarks.add(engine.getLocation());
					MenuItem bm = new MenuItem(engine.getLocation());
					bookmarks.getItems().add(bm);	
					
					bm.setOnAction(event2 ->{
						engine.load(bm.getText());
					});
				}
					bookmark.setDisable(true);
				}
			
			else if (event.getSource() == forward){
					goForward();
			}
		}	
	}
	
	/** This method saves the bookmarks and settings of the browser to separate files on their computer.
	 *  
	 * @param primaryStage The primary stage for this application
	 */
	public void saveSettings(Stage primaryStage){
		BufferedWriter writer = null;
		try (ObjectOutputStream bookmarkFile = new ObjectOutputStream(new FileOutputStream("./bookmarks.bin")))
		{
			bookmarkFile.writeObject(savedBookmarks);  
		} catch (Exception e) {
			e.printStackTrace();
		}
		try{
			writer = Files.newBufferedWriter(Paths.get("./browserSettings.txt"));
			writer.write("screenX="+primaryStage.getX());
			writer.newLine();
			writer.write("screenY="+primaryStage.getY());
			writer.newLine();
			writer.write("height="+primaryStage.getHeight());
			writer.newLine();
			writer.write("width="+primaryStage.getWidth());
			writer.newLine();
			writer.write("downloadDirectory="+drct);
			writer.newLine();
			writer.write("homepage="+hp);
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/** This method reads the bookmarks and settings of the browser from separate files located on their computer.
	 *  
	 * @param primaryStage The primary stage for this application
	 */
	public void readSettings(Stage primaryStage){
		ObjectInputStream bookmarkRead = null;
		BufferedReader reader = null;
		/** A String that stores the line being read in the settings file*/
		String r;
		
		try { 
			bookmarkRead = new ObjectInputStream(Files.newInputStream(Paths.get("./bookmarks.bin")));
			ArrayList<String> newBookmarks = (ArrayList<String>)bookmarkRead.readObject();
			for(String s: newBookmarks){
				savedBookmarks.add(s);
				MenuItem bm = new MenuItem(s);
				bm.setOnAction(evt ->engine.load(bm.getText()));
				bookmarks.getItems().add(bm);
			}
			bookmarkRead.close();
		} catch (Exception e){
			e.printStackTrace();
		}
		
		try { 
			reader = Files.newBufferedReader(Paths.get("./browserSettings.txt"));
			do{
				r= reader.readLine();
				if (r !=null){
					/** An array that stores the line being read by splitting it in two after the '=' symbol*/
					String parts[] = r.split("=");
					if(parts[0].contains("screenX"))
						primaryStage.setX(Double.parseDouble( parts[1] ));
					else if(parts[0].contains("screenY"))
						primaryStage.setY(Double.parseDouble( parts[1] ));
					else if(parts[0].contains("height"))
						primaryStage.setHeight(Double.parseDouble( parts[1] ));
					else if(parts[0].contains("width"))
						primaryStage.setWidth(Double.parseDouble( parts[1] ));
					else if(parts[0].contains("downloadDirectory"))
						drct = parts[1];
					else if(parts[0].contains("homepage"))
						hp = parts[1];
				}
				
			}while (r != null);
			reader.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
