package web;

import java.io.IOException;

/**
 * command函数式接口用于前后端的指令处理
 */
public interface Command {
    void command() throws IOException;
}
