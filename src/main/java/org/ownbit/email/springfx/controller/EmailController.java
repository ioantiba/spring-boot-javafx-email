package org.ownbit.email.springfx.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.mail.internet.InternetAddress;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ownbit.email.springfx.config.EmailSender;
import org.springframework.beans.factory.annotation.Autowired;

import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.web.HTMLEditor;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@FXMLController
@Getter
@Setter
@Slf4j
public class EmailController {

  @FXML
  private TextField fromEmailText;

  @FXML
  private TextField subjectEmailText;

  @FXML
  private TextField attachedFileText;

  private List<File> attachedFiles;

  @FXML
  private TextArea toRecipientsTextArea;

  @FXML
  private TextArea htmlCodeTextArea;

  @FXML
  private TextArea textAreaStatus;

  @FXML
  private HTMLEditor emailEditor;

  @Autowired
  private EmailSender emailSender;

  @FXML
  private ProgressBar progressBar;

  private int totalEmailSent;

  private boolean atLeastOnemessageFailed;

  @FXML
  private Button sendEmailBtn;

 /* @FXML
  public void initialize() {
    System.out.println("initialize something....");
  }*/

  @FXML
  private void onGetHtmlCode(final Event event) {
    this.htmlCodeTextArea.setText(emailEditor.getHtmlText());
  }

  @FXML
  private void onLoadHtmlByCode(final Event event) {
    this.emailEditor.setHtmlText(this.htmlCodeTextArea.getText());
  }

  @FXML
  private void onAttachFileToEmail(final Event event) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select files to be attached");
    //fileChooser.setInitialDirectory(new File("/storage"));
    //fileChooser.getExtensionFilters().addAll(new ExtensionFilter("PDF Files", "*.pdf"));

    List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);

    if (CollectionUtils.isNotEmpty(selectedFiles)) {
      attachedFiles = selectedFiles;
      String fileNames = selectedFiles.stream().map(e -> e.getName()).collect(Collectors.joining(", "));
      this.attachedFileText.setText(fileNames);
    }
  }
  
  @FXML
  private void onLoadHtmlTemplate(final Event event) {
    log.debug("Execute load HTML template from .html file...");
    
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select HTML template");
    fileChooser.getExtensionFilters().addAll(new ExtensionFilter("HTML Files", "*.html"));

    File selectedTemplate = fileChooser.showOpenDialog(null);

    if (selectedTemplate != null) {
      try (Stream<String> stream = Files.lines(Paths.get(selectedTemplate.getAbsolutePath()))) {
        this.emailEditor.setHtmlText(stream.map(Object::toString).collect(Collectors.joining(" ")));
      } catch (IOException e) {
        log.error("Error while trying to read html file template: " + selectedTemplate.getName(), e);
      }
    }
    else {
      log.debug("HTML file selection cancelled.");
    }
  }

  @FXML
  private void onLoadRecipientsFromFile(final Event event) {
    log.debug("Execute load recipients from file...");

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open Resource File to load recipients");
    fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Text Files", "*.txt"));

    File selectedFile = fileChooser.showOpenDialog(null);

    if (selectedFile != null) {

      try (Stream<String> stream = Files.lines(Paths.get(selectedFile.getAbsolutePath()))) {

        stream.forEach(line -> {
          toRecipientsTextArea.appendText(line + "\n");
          log.debug("The email address: " + line + " has been added to email area");
        });

      } catch (IOException e) {
        log.error("Error while trying to read recipients file: " + selectedFile.getName(), e);
      }
    }
    else {
      log.debug("File selection cancelled.");
    }
  }

  @FXML
  private void onSendEmail(final Event event) {
    log.debug("Execute send email action...");
    List<String> recipientsSplittedByNewLine = Arrays.asList(toRecipientsTextArea.getText().trim().split("\n"));
    sendEmailTask(recipientsSplittedByNewLine); //create new async task to send email
  }

 /* @FXML
  private void onKeyPressedEmailEditor(final KeyEvent event) {
    log.debug("key press html editor event...");
    if (event.getCode() == KeyCode.ENTER) {
    }
  }*/

  /*private void ressetProperties() {
    this.fromEmailText.setText("");
    this.subjectEmailText.setText("");
    this.attachedFileText.setText("");
    this.attachedFiles = null;;
    this.toRecipientsTextArea.clear();
    this.htmlCodeTextArea.clear();
    this.emailEditor.setHtmlText("");
  }*/

  private void sendEmailTask(List<String> recipientsSplittedByNewLine) {
    Task<Void> sendEmailTask = new Task<Void>() {
      @Override
      protected Void call() throws Exception {

        //Optional<InternetAddress[]> recipients = emailSender.getValidRecipients(toRecipientsTextArea.getText());

        //validate input fields
        if (StringUtils.isNotBlank(fromEmailText.getText()) && emailSender.isValidEmail(fromEmailText.getText())) {

          if (CollectionUtils.isNotEmpty(recipientsSplittedByNewLine)) {

            if (StringUtils.isNotBlank(emailEditor.getHtmlText())) {

              if (StringUtils.isNotBlank(subjectEmailText.getText())) {

                textAreaStatus.clear();
                setAtLeastOnemessageFailed(false); //reset message failed
                //resset progressBar
                updateProgress(totalEmailSent, recipientsSplittedByNewLine.size());
                Platform.runLater(new Runnable() {
                  @Override
                  public void run() {
                    getProgressBar().progressProperty().bind(progressProperty()); //update progressBar with current task progress
                  }
                });

                //iterate each email address and send the message
                for (String email : recipientsSplittedByNewLine) {

                  if (isCancelled()) {
                    break;
                  }

                  try {
                    InternetAddress emailAddress = new InternetAddress(email);
                    emailSender.sendEmail(emailEditor.getHtmlText(), emailAddress, fromEmailText.getText(), subjectEmailText.getText(), attachedFiles);
                    //Thread.sleep(300);
                    getTextAreaStatus().appendText("Sending to:" + email + " : OK \n");
                  } catch (Exception e) {
                    setAtLeastOnemessageFailed(true);
                    getTextAreaStatus().appendText("Sending to:" + email + " : FAILED \n");
                    log.error("Error while trying to send email", e);
                  }

                  updateProgress(++totalEmailSent, recipientsSplittedByNewLine.size());

                  Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                      getProgressBar().progressProperty().bind(progressProperty()); //update progressBar with current task progress
                    }
                  });

                  // Now block the thread for a short time, but be sure
                  // to check the interrupted exception for cancellation!
                  try {
                    Thread.sleep(1000);
                  } catch (InterruptedException interrupted) {
                    if (isCancelled()) {
                      updateMessage("Cancelled");
                      break;
                    }
                  }
                }

                if (isAtLeastOnemessageFailed()) {
                  Platform.runLater(() -> {
                    new Alert(AlertType.WARNING, "At least one message has not been sent.\nPlease check log file...").showAndWait();
                  });
                }
                else {
                  Platform.runLater(() -> {
                    new Alert(AlertType.INFORMATION, "The message has been sent successfully!").showAndWait();
                  });
                }
              }

              else {
                Platform.runLater(() -> {
                  new Alert(AlertType.ERROR, "The 'subject' field is empty").showAndWait();
                });
              }

            }
            else {
              Platform.runLater(() -> {
                new Alert(AlertType.ERROR, "The content body is empty").showAndWait();
              });
            }

          }
          else {
            Platform.runLater(() -> {
              new Alert(AlertType.ERROR, "The list of recipients is invalid").showAndWait();
            });
          }

        }
        else {
          Platform.runLater(() -> {
            new Alert(AlertType.ERROR, "The email address 'From' is invalid").showAndWait();
          });
        }

        return null;
      }
    };

    sendEmailTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
      @Override
      public void handle(WorkerStateEvent t) {
        totalEmailSent = 0;
      }
    });

    new Thread(sendEmailTask).start(); //start the thread to send email
  }
}