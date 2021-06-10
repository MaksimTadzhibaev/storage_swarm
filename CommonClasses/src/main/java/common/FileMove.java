package common;

import java.io.IOException;
import java.io.Serializable;

//объкты данного класса будут перемещаться между клиентом и сервером
public class FileMove implements Serializable {
    private String path;
    private String name;
    private byte[] bytes;

    public FileMove(String name, byte[] bytes, String path) throws IOException {
        this.name = name;
        this.bytes = bytes;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getPath() {
        return path;
    }
}