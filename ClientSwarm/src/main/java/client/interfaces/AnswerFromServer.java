package client.interfaces;

//Интерфейс для получения ответов сервера
import java.io.IOException;

public interface AnswerFromServer {
    void call(Object o) throws IOException;
}
