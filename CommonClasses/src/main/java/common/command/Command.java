package common.command;//список команд и констант для взаимодествия клиента и сервера

public class Command {
    public static final Integer PORT = 12345;
    public static final String HOST = "localhost";
    public static final String LOCALSTOR = "ClientSwarm/FilesClient/";
    public static final String CLOUD = "ServerNetty/FilesServer/";
    public static final String MKDIR = "/mkdir ";
    public static final String REG = "/reg";
    public static final String OK = "/OK ";
    public static final String END = "/exit ";
    public static final String AUTH = "/auth";
    public static final String ERROR = "/error ";
    public static final String REMOVE = "/rm ";
    public static final String CD = "/cd ";
    public static final String DOWNLOAD = "/download ";
    public static final String COPY = "/copy ";
    public static final String UPDATE = "/update ";
}