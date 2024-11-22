package com.nhlinh.identity_service.service;

import com.nhlinh.identity_service.dto.request.UserCreationRequest;
import com.nhlinh.identity_service.dto.request.UserUpdateRequest;
import com.nhlinh.identity_service.dto.response.UserResponse;
import com.nhlinh.identity_service.entity.Role;
import com.nhlinh.identity_service.entity.User;
import com.nhlinh.identity_service.exception.AppException;
import com.nhlinh.identity_service.exception.ErrorCode;
import com.nhlinh.identity_service.mapper.UserMapper;
import com.nhlinh.identity_service.repository.RoleRepository;
import com.nhlinh.identity_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;

    public UserResponse createUser(UserCreationRequest request){
        if (userRepository.existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.USER_EXISTED);

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findByName("USER").ifPresent(roles::add);
        user.setRoles(roles);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserResponse getMyInfo(){
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name).orElseThrow(()
                -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }

    public UserResponse updateUser (String userId, UserUpdateRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException("User not found"));

        userMapper.updateUser(user, request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        var roles = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles));

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public void deleteUser(String userId){
        userRepository.deleteById(userId);

    }

//    @PreAuthorize("hasAuthority('APPROVE_POST')")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers(){
        log.info("In method get Users");
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse).toList();
    }

    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse getUser(String id){
        log.info("In method get user by Id");
        return userMapper.toUserResponse(userRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

}
