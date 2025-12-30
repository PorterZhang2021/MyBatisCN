/**
 *    Copyright 2009-2015 the original author or authors.
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
package org.apache.ibatis.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * @author Clinton Begin
 */
public class ExceptionUtil {

  private ExceptionUtil() {
    // Prevent Instantiation
  }

  /**
   * 拆解InvocationTargetException和UndeclaredThrowableException异常的包装，从而得到被包装的真正异常
   * @param wrapped 包装后的异常
   * @return 拆解出的被包装异常
   */
  public static Throwable unwrapThrowable(Throwable wrapped) {
    // 该变量用以存放拆包得到的异常
    Throwable unwrapped = wrapped;
    while (true) {
      // 这部分是因为InvocationTargetException和UndeclaredThrowableException是包装异常，
      // 为Java.lang.reflect包下的异常，所以这里判断是否是这两个异常类型
      // 如果内部异常是这两个异常类型，则继续拆包，直到拆包到非这两个异常类型为止
      // 为什么这样设计呢？是因为Java.lang.reflect想要进行异常统一，所以把
      // InvocationTargetException和UndeclaredThrowableException作为包装起来了，
      // 而我们这里本身也是做reflection相关的功能，所以这里需要拆包，把异常抛出
      if (unwrapped instanceof InvocationTargetException) {
        // 拆包获得内部异常
        unwrapped = ((InvocationTargetException) unwrapped).getTargetException();
      } else if (unwrapped instanceof UndeclaredThrowableException) {
        // 拆包获得内部异常
        unwrapped = ((UndeclaredThrowableException) unwrapped).getUndeclaredThrowable();
      } else {
        // 该异常无需拆包
        return unwrapped;
      }
    }
  }

}
