package com.fancier.picture.backend.config.filter;

import cn.hutool.core.util.CharsetUtil;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
public class HttpRequestWrapper extends HttpServletRequestWrapper {
    private final String body;
    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request The request to wrap
     * @throws IllegalArgumentException if the request is null
     */
    public HttpRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        // 设置请求编码，防止中文乱码
        request.setCharacterEncoding(CharsetUtil.UTF_8);

        // 创建一个 StringBuilder 用于存储读取到的请求体内容
        StringBuilder requestBody = new StringBuilder();
        // 获取字符输入流
        BufferedReader reader = request.getReader();

        String line;
        // 逐行读取请求体内容
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }

        // 将 StringBuilder 转换为 String
        this.body = requestBody.toString();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.getBytes());
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener listener) {

            }

            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream(), StandardCharsets.UTF_8));
    }

    public String getBody() {
        return body;
    }
}
