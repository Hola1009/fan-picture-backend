package com.fancier.picture.backend.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
public class FileParserUtil {
    public static <T> List<T> parseJsonFile2ListFormResource(Class<T> clazz, String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        try (InputStream inputStream = FileParserUtil.class
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

    public static String parseStringFromResource(String filePath) {
        try (InputStream inputStream = FileParserUtil.class.getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new RuntimeException("未找到 " + filePath + " 文件");
            }
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
