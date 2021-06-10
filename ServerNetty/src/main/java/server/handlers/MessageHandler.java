package server.handlers;

import common.FileMove;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static common.command.Command.*;

public class MessageHandler extends SimpleChannelInboundHandler<String> {
    private String currentDirectory;
    private String abs;
    private int numberOfNewFiles = 0;
    private int numberOfNewDirectories = 0;


    //подключение к БД
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected: " + ctx.channel());
        DBHandler.getConnectionWithDB();
    }

    //чтение и обработка комманд от клиента
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        String command = msg
                .replace("\n", "")
                .replace("\r", "");

        String[] commands = command.split(" ");
        try {
            if (command.startsWith(CD)) {
                currentDirectory = commands[1];
            } else if (command.startsWith(REG)) {
                registrationDB(commands, ctx);
            } else if (command.startsWith(AUTH)) {
                authentication(commands, ctx);
            } else if (command.startsWith(END)) {
                channelInactive(ctx);
            } else if (command.startsWith(MKDIR)) {
                createNewDirectory(commands, ctx);
            } else if (command.startsWith(REMOVE)) {
                deleteFile(commands, ctx);
            } else if (command.startsWith(DOWNLOAD)) {
                download(commands, ctx);
            }
//            else if (command.startsWith(SHOW)) {
//                showFile(commands[1], ctx);
//            }
            else if (command.startsWith(COPY)) {
                copyFile(commands[1], ctx);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // отключение от базы данных
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(END);
        System.out.println("Client disconnected: " + ctx.channel());
        DBHandler.disconnectDB();
        ctx.close();
    }

    //копирование файлов на сервере
    private void copyFile(String com, ChannelHandlerContext ctx) throws IOException {
        Path sourcePath = Paths.get(currentDirectory, com);
        Path destPath = Paths.get(currentDirectory, com + "-копия");
        methodForCopy(sourcePath, destPath);
        ctx.writeAndFlush(UPDATE);
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

    //отправка файла на клиент после принятия команды от клиента
    public void download(String com[], ChannelHandlerContext ctx) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(com[1], com[2]));
        FileMove fileMove = new FileMove(com[2], bytes, com[1]);
        ctx.writeAndFlush(fileMove);
    }

    //удаление файла или директории на сервере после принятие команды от клиента
    private void deleteFile(String com[], ChannelHandlerContext ctx) throws IOException {
        methodForDelete(Paths.get(currentDirectory, com[1]));
        ctx.writeAndFlush(UPDATE);
    }

    //основной метод удаления файла или директории
    public void methodForDelete(Path sourcePath) throws IOException {
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
    }

    //создание директории на сервере после принятие команды от клиента
    private void createNewDirectory(String[] com, ChannelHandlerContext ctx) throws IOException {
        try {
            Path newFile = Paths.get(currentDirectory, com[1]);
            if (!Files.exists(newFile)) {
                Files.createDirectory(newFile);
                ctx.writeAndFlush(UPDATE);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Path newFile = Paths.get(currentDirectory, "New Folder(" + numberOfNewDirectories + ")");
            numberOfNewDirectories++;
            Files.createDirectory(newFile);
            ctx.writeAndFlush(UPDATE);
        }
    }

    //отправка запроса в базу данных для входа, создание директории пользователя на сервере, если она ещё не создан и отправка новой директории сервера клиенту в случае успеха
    private void authentication(String[] commands, ChannelHandlerContext ctx) {
        DBHandler.getConnectionWithDB();
        if (DBHandler.checkIfUserExistsForAuthorization(commands[1])) {
            if (!DBHandler.checkIfPasswordIsRight(commands[1], commands[2])) {
                ctx.writeAndFlush(AUTH + ERROR + "Неверно введен логин или пароль");
            }
        }
        String login = commands[1];
        currentDirectory = CLOUD + login + "_swarm";
        try {
            Path newDir = Paths.get(currentDirectory);
            abs = newDir.toAbsolutePath().toString();
            if (!Files.exists(newDir))
                Files.createDirectory(newDir);
        } catch (IOException e) {
            ctx.writeAndFlush(AUTH + ERROR + "Не удалось создать директорию облака");
        }
        ctx.writeAndFlush(AUTH + OK + abs + " " + login);
        DBHandler.disconnectDB();
    }

    //регистрация нового пользователя в базе данных, создание директории пользоваетля на сервере
    private void registrationDB(String[] commands, ChannelHandlerContext ctx) {
        DBHandler.getConnectionWithDB();
        if (DBHandler.registerNewUser(commands[1], commands[2])) {
            ctx.writeAndFlush((REG + OK));
        } else if (DBHandler.checkIfUserExistsForAuthorization(commands[1])) {
            ctx.writeAndFlush(REG + ERROR);
        }
        currentDirectory = CLOUD + commands[1];
        try {
            Path newDir = Paths.get(currentDirectory);
            if (!Files.exists(newDir))
                Files.createDirectory(newDir);
        } catch (IOException e) {
            ctx.writeAndFlush(ERROR + "Не удалось создать хранилище");
        }
        DBHandler.disconnectDB();
    }

}