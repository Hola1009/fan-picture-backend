package com.fancier.picture.backend.auth.model;

import com.fancier.picture.backend.util.FileParserUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
public class SpaceUserAuth {
    public static final Map<String , List<String>> spaceRolePermissionsMap;

    static {
        List<SpaceRole> spaceRoles = FileParserUtil.parseJsonFile2ListFormResource(SpaceRole.class, "biz/spaceUserRoles.json");
        spaceRolePermissionsMap = spaceRoles.stream().collect(Collectors.toMap(SpaceRole::getKey, SpaceRole::getPermissions));
    }

    public static List<String> getPermissionsByRole(String role) {
        return spaceRolePermissionsMap.get(role);
    }
}
