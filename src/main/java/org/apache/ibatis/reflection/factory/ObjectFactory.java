/**
 *    Copyright 2009-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.reflection.factory;

import java.util.List;
import java.util.Properties;

/**
 * MyBatis uses an ObjectFactory to create all needed new Objects.
 *
 * @author Clinton Begin
 * 对象工厂
 */
public interface ObjectFactory {

  /**
   * 设置工厂属性
   * Sets configuration properties.
   * @param properties configuration properties
   */
  default void setProperties(Properties properties) {
    // NOP
    // 本身是一个接口，所以支持默认实现方法，这里看着默认实现应该是空操作
  }

  /**
   * 创建一个无参构造的类型实例，这里传入的是一个Class<T>类型，
   * 这里Class表明的是可以用相关的反射操作，而这个T则是需要
   * 构建的对象类型。
   * Creates a new object with default constructor.
   * @param type Object type
   * @return
   */
  <T> T create(Class<T> type);

  /**
   * 创建一个有参构造的类型实例，其中通过constructorArgTypes和constructorArgs来指定构造参数的类型和值
   * Creates a new object with the specified constructor and params.
   * @param type Object type
   * @param constructorArgTypes Constructor argument types
   * @param constructorArgs Constructor argument values
   * @return
   */
  <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs);

  /**
   * 这里单独对集合类型有一个判断，为啥需要这个并且写在接口层面还有待观察
   * Returns true if this object can have a set of other objects.
   * It's main purpose is to support non-java.util.Collection objects like Scala collections.
   *
   * @param type Object type
   * @return whether it is a collection or not
   * @since 3.1.0
   */
  <T> boolean isCollection(Class<T> type);

}
