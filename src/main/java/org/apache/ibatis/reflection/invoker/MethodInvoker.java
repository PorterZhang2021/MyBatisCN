/**
 *    Copyright 2009-2018 the original author or authors.
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
package org.apache.ibatis.reflection.invoker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.ibatis.reflection.Reflector;

/**
 * @author Clinton Begin
 */
public class MethodInvoker implements Invoker {

  // 传入参数或者传出参数类型
  private final Class<?> type;
  private final Method method;

  /**
   * MethodInvoker构造方法
   * @param method 方法
   */
  public MethodInvoker(Method method) {
    this.method = method;

    // 这里传入的是方法的参数类型，如果只有一个入参，则返回入参类型，否则返回出参类型
    if (method.getParameterTypes().length == 1) {
      // 有且只有一个入参时，这里放入入参
      type = method.getParameterTypes()[0];
    } else {
      // 否则是出参
      type = method.getReturnType();
    }
  }

  // 执行函数
  @Override
  public Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException {
    try {
      // 反射执行，这里传入的args是反射执行时传入的参数
      return method.invoke(target, args);
    } catch (IllegalAccessException e) {
      if (Reflector.canControlMemberAccessible()) {
        method.setAccessible(true);
        return method.invoke(target, args);
      } else {
        throw e;
      }
    }
  }

  @Override
  public Class<?> getType() {
    // 这里可能会返回方法的入参类型或者返回类型
    return type;
  }
}
