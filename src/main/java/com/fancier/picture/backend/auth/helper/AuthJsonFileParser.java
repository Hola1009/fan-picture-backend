package com.fancier.picture.backend.auth.helper;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
public class AuthJsonFileParser {
    public static <T> List<T> parse2ListFormResource(Class<T> clazz, String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        try (InputStream inputStream = AuthJsonFileParser.class
                .getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new RuntimeException("未找到 " + filePath + " 文件");
            }
            List<?> list = objectMapper.readValue(inputStream, List.class);
            return list.stream()
                    .map(o -> objectMapper.convertValue(o, clazz))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("解析 "+ filePath +" 文件时出错", e);
        }
    }
}
