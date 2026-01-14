package com.porterzhang.mapper;

import com.porterzhang.pojo.User;

public interface UserMapper {
  User selectById(Integer id);
}