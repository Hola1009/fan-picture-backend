package com.fancier.picture.backend.auth.model;

import lombok.Data;

import java.util.List;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Data
public class SpaceRole {
    String key;
    String name;
    String description;
    List<String> permissions;
}
