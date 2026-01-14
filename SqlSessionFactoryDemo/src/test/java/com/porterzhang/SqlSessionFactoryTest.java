package com.porterzhang;

import com.porterzhang.mapper.UserMapper;
import com.porterzhang.pojo.User;
import java.io.InputStream;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SqlSessionFactoryTest {
  static SqlSessionFactory factory;

  @BeforeAll
  static void init() throws Exception {
    // ★ 断点 1：从这里 F7 进去，一路跟到 XMLConfigBuilder.parse()
    String resource = "mybatis-config.xml";
    try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
      // Q1 - 20260114 - 1 - 创建 SqlSessionFactory 对象并进行初始化
      factory = new SqlSessionFactoryBuilder().build(inputStream);
    }
  }

  @Test
  @DisplayName("测试 SqlSessionFactory 成功创建并执行查询")
  void testFactory() {
    // ★ 断点 2：观察 Configuration 对象内部
    assertNotNull(factory);
    assertEquals("dev", factory.getConfiguration().getEnvironment().getId());

    // ★ 断点 3：观察 MappedStatement 的封装
    try (SqlSession session = factory.openSession()) {
      UserMapper mapper = session.getMapper(UserMapper.class);
      User u = mapper.selectById(1);
      assertEquals("mybatis352", u.getName());
    }
  }
}
