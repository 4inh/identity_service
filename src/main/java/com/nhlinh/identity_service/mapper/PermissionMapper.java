package com.nhlinh.identity_service.mapper;

import com.nhlinh.identity_service.dto.request.PermissionRequest;
import com.nhlinh.identity_service.dto.response.PermissionResponse;
import com.nhlinh.identity_service.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);
}
