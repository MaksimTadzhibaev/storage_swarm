package common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/*
Данный класс описывает файлы и директории в основном окне
1. Тип файла
2. Имя файла
3. Размер файла
4.Дата изменения файла
 */

public class FileInfo {
    public enum TypeOfFile {
        FILE("File"),
        DIRECTORY("Dir");
        private String name;

        public String getName() {
            return name;
        }

        TypeOfFile(String name) {
            this.name = name;
        }
    }

    private String filename;
    private TypeOfFile type;
    private long size;
    private LocalDateTime modifiedTime;

    public FileInfo(Path pathToFile) {
        try {
            this.filename = pathToFile.getFileName().toString();
            this.size = Files.size(pathToFile);
            this.type = Files.isDirectory(pathToFile) ? TypeOfFile.DIRECTORY : TypeOfFile.FILE;
            if (this.type == TypeOfFile.DIRECTORY) {
                this.size = -1L;
            }
            this.modifiedTime = LocalDateTime.ofInstant(Files
                    .getLastModifiedTime(pathToFile)
                    .toInstant(), ZoneOffset.ofHours(0));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setType(TypeOfFile type) {
        this.type = type;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setModifiedTime(LocalDateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public String getFilename() {
        return filename;
    }

    public TypeOfFile getType() {
        return type;
    }

    public long getSize() {
        return size;
    }

    public LocalDateTime getModifiedTime() {
        return modifiedTime;
    }
}