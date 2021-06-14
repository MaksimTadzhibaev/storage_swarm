package client;

import common.FileMove;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ResourceBundle;

import static common.command.Command.*;

public class Controller implements Initializable {
    public String currentLogin;
    private ClientNetty clientNetty;
    private String clientDirectory;
    private String serverDirectory;
    private MainPanelController clientPC;
    private MainPanelController serverPC;
    private Stage stage;
    private static int numberOfNewDirectories = 0;
    private FileMove fileMove;

    @FXML
    AnchorPane anchorPane;
    @FXML
    HBox Menu;
    @FXML
    VBox btnPanelBlock;
    @FXML
    HBox PanelBlock;
    @FXML
    VBox serverInfo;
    @FXML
    VBox clientInfo;
    @FXML
    VBox authenticationBlock;
    @FXML
    TextField loginField;
    @FXML
    PasswordField passwordField;
    @FXML
    Button entryButton;
    @FXML
    Button registrationButton;
    @FXML
    Button cancelRegistrationButton;
    @FXML
    VBox registrationBlock;
    @FXML
    TextField registrationLoginForm;
    @FXML
    TextField registrationPassForm;
    @FXML
    TextField repeatPassForm;
    @FXML
    Button finalRegistrationButton;
    @FXML
    Label registrationCompleted;
    @FXML
    TextArea messageToUser;
    @FXML
    ChoiceBox menu;

    //запуск
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clientPC = (MainPanelController) clientInfo.getProperties().get("ctrl");
        serverPC = (MainPanelController) serverInfo.getProperties().get("ctrl");
        menu.getSelectionModel().select(0);
        ServerListener();
    }

    //поток получения сообщений от сервера, их обработка и вывод на экран реакций
    public void ServerListener() {
        Platform.runLater(() -> {
            clientNetty = new ClientNetty((args) -> {
                if (args instanceof String) {
                    String msg = (String) args;
                    String[] answer = msg.split(" ");
                    if (msg.startsWith(AUTH + OK)) {
                        serverDirectory = answer[1];
                        currentLogin = answer[2];
                        clientPC.setNormalPath(clientDirectory);
                        serverPC.setNormalPath(serverDirectory);
                        setTitle(currentLogin);
                        updatePanel();
                        cancelAll();
                        messageToUser.setText("Выполнен вход");
                    } else if (msg.startsWith(AUTH + ERROR)) {
                        messageToUser.setText("Не верный логин или пароль");
                        loginField.clear();
                        passwordField.clear();
                    } else if (msg.startsWith(REG + OK)) {
                        messageToUser.setText("Вы успешно зарегистрировались");
                        registrationLoginForm.clear();
                        registrationBlock.setVisible(false);
                        registrationButton.setVisible(false);
                        cancelRegistrationButton.setVisible(true);
                        registrationCompleted.setVisible(true);
                    } else if (msg.startsWith(REG + ERROR)) {
                        messageToUser.setText("Пользователь уже существует");
                        registrationLoginForm.clear();
                    } else if (msg.startsWith(UPDATE)) {
                        updatePanel();
                    }
                } else if (args instanceof FileMove) {
                    try {
                        args = new FileMove(((FileMove) args).getName(), ((FileMove) args).getBytes(), ((FileMove) args).getPath());
                        if (!Files.exists(Paths.get(clientPC.getNormalPath(), ((FileMove) args).getName()))) {
                            Files.createFile(Paths.get(clientPC.getNormalPath(), ((FileMove) args).getName()));
                            Files.write(Paths.get(clientPC.getNormalPath(), ((FileMove) args).getName()), ((FileMove) args).getBytes());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    updatePanel();
                }
            });
        });
    }

    // форма аутентификации
    public void sendAuthMessage() {
        if (!loginField.getText().isEmpty() && !passwordField.getText().isEmpty()) {
            clientDirectory = LOCALSTOR + loginField.getText() + "_local";
            clientNetty.sendMessage(AUTH + " " + loginField.getText() + " " + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        }
        try {
            Path newDir = Paths.get(clientDirectory);
            if (!Files.exists(newDir))
                Files.createDirectory(newDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // форма регистрации
    public void showRegistrationForms() {
        authenticationBlock.setVisible(false);
        registrationBlock.setVisible(true);
        finalRegistrationButton.setVisible(true);
        registrationButton.setVisible(false);
        cancelRegistrationButton.setVisible(true);
        registrationCompleted.setVisible(false);

    }

    // выход из формы регистрации
    public void cancelRegistration() {
        registrationButton.setVisible(true);
        cancelRegistrationButton.setVisible(false);
        registrationLoginForm.clear();
        registrationPassForm.clear();
        repeatPassForm.clear();
        registrationBlock.setVisible(false);
        finalRegistrationButton.setVisible(false);
        messageToUser.setText("Добро пожаловать. Авторизуйтесь или перейдите в форму регистрации.");
        registrationCompleted.setVisible(false);
        authenticationBlock.setVisible(true);

    }

    //отправка данных на сервер для регистрации
    public void sendRegMessageToServer(ActionEvent actionEvent) {
        if (!registrationLoginForm.getText().isEmpty() && !registrationPassForm.getText().isEmpty() && !repeatPassForm.getText().isEmpty()) {
            if (registrationPassForm.getText().equals(repeatPassForm.getText())) {
                clientNetty.sendMessage(REG + " " + registrationLoginForm.getText() + " " + registrationPassForm.getText());
            } else {
                messageToUser.setText("Пароли не совпадают");
                registrationPassForm.clear();
                repeatPassForm.clear();
            }
        }
    }

    //метод обновляет директории после изменений и отправляет информацию о новой директории на сервер
    public void updatePanel() {
        Platform.runLater(() -> {
            clientPC.updateList(Paths.get(clientPC.getNormalPath()));
            serverPC.updateList(Paths.get(serverPC.getNormalPath()));
            clientNetty.sendMessage(CD + serverPC.getNormalPath());
            System.out.println(serverPC.getNormalPath());
        });
    }

    // раскрывающийся список меню, чтобы сменить профиль или выйти из приложения
    public void goMenu() {
        if (menu.getSelectionModel().getSelectedItem().toString().equals("Сменить профиль")) {
            anchorPane.setVisible(true);
            anchorPane.setManaged(true);
            btnPanelBlock.setVisible(false);
            PanelBlock.setVisible(false);
            authenticationBlock.setVisible(true);
            registrationButton.setVisible(true);
            authenticationBlock.setManaged(true);
            registrationButton.setManaged(true);
            messageToUser.setText("Добро пожаловать. Авторизуйтесь или перейдите в форму регистрации.");

        } else if (menu.getSelectionModel().getSelectedItem().toString().equals("Выйти")) {
            clientNetty.sendMessage(END);
            Stage stage;
            stage = (Stage) menu.getScene().getWindow();
            stage.close();
        }
    }

    //закрытие окон после аутентификации
    public void cancelAll() {
        authenticationBlock.setVisible(false);
        registrationBlock.setVisible(false);
        registrationButton.setVisible(false);
        anchorPane.setVisible(false);
        Menu.setVisible(true);
        btnPanelBlock.setVisible(true);
        PanelBlock.setVisible(true);
        authenticationBlock.setManaged(false);
        registrationBlock.setManaged(false);
        registrationButton.setManaged(false);
        anchorPane.setManaged(false);
    }

    //метод создания директории на сервере или клиенте
    public void createDirectory(ActionEvent actionEvent) {
        String target = null;
        String nameDir = "Новая_папка" + ++numberOfNewDirectories;

        if (serverPC.tableInfo.isFocused()) {
            target = serverPC.getNormalPath();
            clientNetty.sendMessage(MKDIR + nameDir);
        }
        if (clientPC.tableInfo.isFocused()) {
            target = clientPC.getNormalPath();
            try {
                Path newDir = Paths.get(target + "/" + nameDir);
                if (!Files.exists(newDir))
                    Files.createDirectory(newDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
            updatePanel();
        }
        if (target == null) {
            messageToUser.setText("Выберете хранилище");
        }

    }

    //метод отправляет файл на облако
    public void moveFileToServer(ActionEvent actionEvent) {
        if (!Files.isDirectory(Paths.get(clientPC.getNormalPath(), clientPC.getFileName()).toAbsolutePath())) {
            try {
                byte[] byteBuf = Files.readAllBytes(Paths.get(clientPC.getNormalPath(), clientPC.getFileName()).toAbsolutePath());
                fileMove = new FileMove(clientPC.getFileName(), byteBuf, serverPC.getNormalPath());
                clientNetty.sendMessage(fileMove);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            messageToUser.setText("Выберите файл для отправки");
        }
    }

    //метод отправляет сообщение на сервер о загрузке файла на клиент
    public void moveFileToClient(ActionEvent actionEvent) {
        if (!Files.isDirectory(Paths.get(serverPC.getNormalPath(), serverPC.getFileName()))) {
            clientNetty.sendMessage(DOWNLOAD + serverPC.getNormalPath() + " " + serverPC.getFileName());
        } else {
            messageToUser.setText("Выберите файл для отправки");
        }
    }

    //метод удаляет выбранный файл или директорию
    public void delete(ActionEvent actionEvent) {
        chooseFileAlert(serverPC, clientPC);

        MainPanelController source = null;

        if (serverPC.getFileName() != null) {
            source = serverPC;
            clientNetty.sendMessage(REMOVE + source.getFileName());
        }

        if (clientPC.getFileName() != null) {
            source = clientPC;
            Path sourcePath = Paths.get(source.getNormalPath(), source.getFileName());
            methodForDelete(sourcePath);
            updatePanel();
        }

    }

    //основной метод удаления файла или директории
    public void methodForDelete(Path sourcePath) {
        try {
            Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //метод изменяет заголовок окна программы добавляя ник пользователя
    private void setTitle(String login) {
        Platform.runLater(() -> {
            stage = (Stage) loginField.getScene().getWindow();
            stage.setTitle("SWARM - cloud storage/" + login);
        });
    }

    //метод копирования файла внутри директории клиента или сервера
    public void copyFile(ActionEvent actionEvent) {
        chooseFileAlert(serverPC, clientPC);

        MainPanelController source = null;

        if (serverPC.getFileName() != null) {
            clientNetty.sendMessage(COPY + serverPC.getFileName());
        }
        if (clientPC.getFileName() != null) {
            Path sourcePath = Paths.get(clientPC.getNormalPath(), clientPC.getFileName());
            Path destPath = Paths.get(clientPC.getNormalPath(), clientPC.getFileName() + "-копия");

            try {
                methodForCopy(sourcePath, destPath);
                updatePanel();
            } catch (IOException e) {
                messageToUser.setText("При копировании произошла ошибка");
            }
        }
    }

    //вспомогательный метод вызова предупреждения если не выбрана ни одна из директорий
    public void chooseFileAlert(MainPanelController serverPC, MainPanelController clientPC) {
        if (serverPC.getFileName() == null && clientPC.getFileName() == null) {
            messageToUser.setText("Выберите файл");
        }
    }

    //основной метод для копирования файлов или директорий
    public void methodForCopy(Path src, Path dst) throws IOException {
        Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetPath = dst.resolve(src.relativize(dir));
                if (!Files.exists(targetPath)) {
                    Files.createDirectory(targetPath);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, dst.resolve(src.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
