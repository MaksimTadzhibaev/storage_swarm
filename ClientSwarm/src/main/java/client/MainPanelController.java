package client;

import common.FileInfo;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import static common.command.Command.*;

public class MainPanelController implements Initializable {

    Controller controller = new Controller();
    public String normalPath; //полный путь до файлов
    @FXML
    public TextField filePath; //отображает файлы начиная с папки хранилища (выше не подняться)
    @FXML
    Button Up;
    @FXML
    public TableView<FileInfo> tableInfo;

    //построение двух таблиц, отображающих файлы и их свойства
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            TableColumn<FileInfo, String> fileType = new TableColumn<>("Type");
            fileType.setCellValueFactory(param ->
                    new SimpleStringProperty(param.getValue().getType().getName()));
            fileType.setPrefWidth(50);

            TableColumn<FileInfo, String> fileName = new TableColumn<>("Name");
            fileName.setCellValueFactory(param ->
                    new SimpleStringProperty(param.getValue().getFilename()));
            fileName.setPrefWidth(120);

            TableColumn<FileInfo, Long> fileSize = new TableColumn<>("Size");
            fileSize.setCellValueFactory(param ->
                    new SimpleObjectProperty(param.getValue().getSize()));
            fileSize.setPrefWidth(90);
            fileSize.setCellFactory(column -> {
                        return new TableCell<FileInfo, Long>() {
                            @Override
                            protected void updateItem(Long item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item == null || empty) {
                                    setText(null);
                                    setStyle("");
                                } else {
                                    String text = String.format("%,d bytes", item);
                                    if (item == -1L)
                                        text = "dir";
                                    setText(text);
                                }
                            }
                        };
                    }
            );
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
            TableColumn<FileInfo, String> fileDate = new TableColumn<>("Date");
            fileDate.setCellValueFactory(param ->
                    new SimpleStringProperty(param.getValue().getModifiedTime().format(dateTimeFormatter)));
            fileDate.setPrefWidth(120);

            tableInfo.getColumns().addAll(fileType, fileName, fileSize, fileDate);
            tableInfo.getSortOrder().add(fileType);
            tableInfo.getSortOrder().add(fileName);

            tableInfo.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() == 2) {
                        Path path = Paths.get(normalPath)
                                .resolve(tableInfo.getSelectionModel()
                                        .getSelectedItem()
                                        .getFilename());
                        if (Files.isDirectory(path))
                            updateList(path);
                    }
                }
            });
        });
    }

    //обновление листов с файлами при выполнении действий над ними
    public void updateList(Path path) {
        try {
            Path maxPath = path.toAbsolutePath();
            if (String.valueOf(path).contains("ClientSwarm" + File.separator + "FilesClient")) {
                Path minPath = Paths.get(LOCALSTOR).toAbsolutePath();
                filePath.setText(String.valueOf(minPath.relativize(maxPath)));
            } else {
                Path minPath = Paths.get(CLOUD).toAbsolutePath();
                filePath.setText(String.valueOf(minPath.relativize(maxPath)));
            }
            setNormalPath(path.normalize().toAbsolutePath().toString());
            tableInfo.getItems().clear();
            tableInfo.getItems()
                    .addAll(Files.list(path)
                            .map(FileInfo::new)
                            .collect(Collectors.toList()));
            tableInfo.sort();
        } catch (
                IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Error. Cannot update list of files", ButtonType.OK);
            alert.showAndWait();
        }
    }

    //возврат вверх по директории до папок сервера и клиента
    public void upToDirectory(ActionEvent actionEvent) {
        Path up = Paths.get(getNormalPath()).getParent();
        if (normalPath.endsWith("swarm") || normalPath.endsWith("local")) {
            return;
        } else {
            updateList(up);
        }
    }

    //получить имя выбранного файла
    public String getFileName() {
        if (!tableInfo.isFocused()) {
            return null;
        }
        return tableInfo.getSelectionModel().getSelectedItem().getFilename();
    }

    //получить путь выбранного файла
    public String getFilePath() {
        return filePath.getText();
    }

    //изменить путь выбранного файла
    public void setFilePath(String filePath) {
        this.filePath.setText(filePath);
    }

    //полный путь изменить
    public void setNormalPath(String normalPath) {
        this.normalPath = normalPath;
    }

    //полный путь получить
    public String getNormalPath() {
        return normalPath;
    }

    //полный путь изменить
    public void setLogin(String normalPath) {
        this.normalPath = normalPath;
    }
}




