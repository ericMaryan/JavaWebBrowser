package assignment3;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/** This class represents a download bar that manages downloads in the JavaFX browser.
 * 
 * @author Eric Maryan
 *
 */
public class DownloadBar extends HBox {

	private static Stage downloadWindow = null;
	private static VBox downloadTasks;
	private static TextArea messageArea;
	private static Text fileNameDisplay;
	private static ProgressBar pb;
	private static Button cancel;

	/** Calling this function will guarantee that the downloadTasks VBox is created and visible.
	 * @return A Stage that will show each downloadTask's progress.
	 */
	public Stage getDownloadWindow()
	{
		if(downloadWindow == null)
		{
			//Create a new borderPane for the download window
			BorderPane downloadRoot = new BorderPane();
			downloadTasks = new VBox();
			//downloadTasks will contain rows of DownloadTask objects, which are HBoxes
			downloadRoot.setCenter(		 downloadTasks		);
			
			//The bottom of the window will be the message box for download tasks
			downloadRoot.setBottom(		messageArea = new TextArea() 		);
			downloadWindow = new Stage();
			downloadWindow.setScene( new Scene(downloadRoot, 400, 600)  );
			
			//When closing the window, set the variable downloadWindow to null
			downloadWindow.setOnCloseRequest(		event -> downloadWindow = null		);
		}
		return downloadWindow;
	}
	
	/**The constructor for a DownloadBar
	 * 
	 * @param newLocation  The String URL of a file to download
	 * @param dir  A string that stores the download directory. 
	 */
	public DownloadBar(String newLocation, String dir)
	{
		File f = new File(newLocation);
		/** An integer that stores the index of the last '/' in a web address.*/
		int index = newLocation.lastIndexOf("/");
		/** A String that stores the name of the file to be downloaded.*/
		String fileName = newLocation.substring(index+1);
		/** A String that stores the final version of the file name.*/
		String finalFilename = dir+ "/" +fileName;
		/** An integer used to add a number after a duplicate file.*/
		int i = 0;
		
		//See if the filename at the end of newLocation exists on your hard drive.
		// If the file already exists, then add (1), (2), ... (n) until you find a new filename that doesn't exist.
		while (Files.exists(Paths.get(finalFilename))){
			i++;
			finalFilename = dir+ "/" + fileName+ "(" + i + ")";
		}	
		
		//Create the window if it doesn't exist. After this call, the VBox and TextArea should exist.
		getDownloadWindow();

		downloadWindow.show();
		
		//Add a Text label for the filename
		fileNameDisplay = new Text(fileName);
		//Add a ProgressBar to show the progress of the task
		pb = new ProgressBar();
		//Add a cancel button that asks the user for confirmation, and cancel the task if the user agrees
		cancel = new Button("Cancel");
		
		this.getChildren().addAll(fileNameDisplay, pb, cancel);
		downloadTasks.getChildren().add(this);
		 //Start the download
		DownloadTask aFileDownload = new DownloadTask(newLocation, finalFilename  ) ;
		 cancel.setOnAction(event -> {
				Alert alert = new Alert(  AlertType.CONFIRMATION  ); 
				alert.setTitle("Cancel Download");
				alert.setHeaderText("Would you like to cancel the download?");
				alert.setContentText("Press OK to cancel.");
			
				Optional<ButtonType> result = alert.showAndWait();
				if (result.get() == ButtonType.OK){
					aFileDownload.cancel();
				}
				else{}
			});
		pb.progressProperty().bind( aFileDownload.progressProperty());
		new Thread( aFileDownload ).start();
	}
	
	
	/**This class represents a task that will be run in a separate thread. It will run call(), 
	 *  and then call succeeded, cancelled, or failed depending on whether the task was cancelled
	 *  or failed. If it was not, then it will call succeeded() after call() finishes.
	 */
	private class DownloadTask extends Task<String>
	{
		/** A String that stores the URL of the file.*/
		String fileURL ="";
		/** A String that stores the final version of the file name.*/
		String finalFilename = "";
		FileOutputStream outputStream;
		/** An integer that stores the BUFFER_SIZE for downloads.*/
		private static final int BUFFER_SIZE = 4096;
		
		/**The constructor for a DownloadTask
		 * 
		 * @param URL  The String URL of a file to download
		 * @param name  A string that stores the file name
		 */
		public DownloadTask(String URL, String name){
			fileURL = URL;
			finalFilename = name;
		}
		
		// This should start the download. Look at the downloadFile() function at:
		//  http://www.codejava.net/java-se/networking/use-httpurlconnection-to-download-file-from-an-http-url
		//Take that function but change it so that it updates the progress bar as it iterates through the while loop.
		//Here is a tutorial on how to upgrade a progress bar:
		//	https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/progress.htm
		
		/**This method starts a download for the selected file
		 * 
		 * @return A string representing the end result of the method
		 */
		@Override
		protected String call() throws Exception {
		           
		        URL url = new URL(fileURL);
		        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
				/** An integer that stores the HTTP response code of the connection.*/
		        int responseCode = httpConn.getResponseCode();
		 
		        // always check HTTP response code first
		        if (responseCode == HttpURLConnection.HTTP_OK) {
		    		/** A String that stores the file name.*/
		        	String fileName = "";
		    		/** A String that stores the disposition of the file.*/
		            String disposition = httpConn.getHeaderField("Content-Disposition");
		    		/** A String that stores the content type of the file.*/
		            String contentType = httpConn.getContentType();
		    		/** An integer that stores the content length.*/
		            int contentLength = httpConn.getContentLength();
		 
		            if (disposition != null) {
		                // extracts file name from header field
		            	/** An integer that stores the index of 'filename='.*/
		                int index = disposition.indexOf("filename=");
		                if (index > 0) {
		                    fileName = disposition.substring(index + 10,
		                            disposition.length() - 1);
		                }
		            } else {
		                // extracts file name from URL
		                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
		                        fileURL.length());
		            }
		 
		            System.out.println("Content-Type = " + contentType);
		            System.out.println("Content-Disposition = " + disposition);
		            System.out.println("Content-Length = " + contentLength);
		            System.out.println("fileName = " + fileName);
		 
		            // opens input stream from the HTTP connection
		            InputStream inputStream = httpConn.getInputStream();
		             
		            // opens an output stream to save into file
		            outputStream = new FileOutputStream(finalFilename);
		 
		            /** An integer that stores the number of bytes read.*/
		            int bytesRead = -1;
		            /** An integer that stores the total bytes read.*/
		            int totalRead = 0;
		            /** An array of bytes representing the BUFFER_SIZE.*/
		            byte[] buffer = new byte[BUFFER_SIZE];
		            while (!this.isCancelled() && (bytesRead = inputStream.read(buffer)) != -1) {
		                outputStream.write(buffer, 0, bytesRead);
		                totalRead += bytesRead;
		                this.updateProgress(totalRead, contentLength);
		            }
		 
		            outputStream.close();
		            inputStream.close();
		 
		            System.out.println("File downloaded");
		        } else {
		            System.out.println("No file to download. Server replied HTTP code: " + responseCode);
		        }
		        httpConn.disconnect();
		      
			return "Finished";
		}
		
		//Write the code here to handle a successful completion of the call() function.
		/**This method is called when a download succeeds
		 * 
		 * @return nothing
		 */
		@Override
		protected void succeeded() {
			super.succeeded();	
			downloadTasks.getChildren().remove(DownloadBar.this);
			messageArea.setText(finalFilename + " was successfully downloaded.");
			try {
				Desktop.getDesktop().open(new File(finalFilename));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//Write the code here to handle the task being cancelled before call() finishes.
		/**This method is called when a download is cancelled
		 * 
		 * @return nothing
		 */
		@Override
		protected void cancelled() {
			super.cancelled();	
			try{
				outputStream.close();
				Files.delete(Paths.get("./" + finalFilename));
			} catch (Exception e){
				e.printStackTrace();
			}
			downloadTasks.getChildren().remove(DownloadBar.this);
			messageArea.setText(finalFilename + " download was cancelled.");
		}
		
		/**This method is called when a download fails
		 * 
		 * @return nothing
		 */
		@Override
		protected void failed() {		
			super.failed();	
			try{
				outputStream.close();
				Files.delete(Paths.get("./" + finalFilename));
			} catch (Exception e){
				e.printStackTrace();
			}
			downloadTasks.getChildren().remove(DownloadBar.this);
			messageArea.setText(finalFilename + " download failed.");
		}
	}		
}
