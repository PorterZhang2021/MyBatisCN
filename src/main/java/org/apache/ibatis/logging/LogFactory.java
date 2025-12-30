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
package org.apache.ibatis.logging;

import java.lang.reflect.Constructor;

/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 * 对于对应的适配期，这里会构建一个日志实例工厂，创建对应能够使用的日志实例
 * 对应的日志工厂
 */
public final class LogFactory {

  /**
   * Marker to be used by logging implementations that support markers.
   */
  public static final String MARKER = "MYBATIS";

  private static Constructor<? extends Log> logConstructor;

  static {
    // 需要注意的是这里都是依此尝试创建，而不是确保能够创建成功
    tryImplementation(LogFactory::useSlf4jLogging);
    tryImplementation(LogFactory::useCommonsLogging);
    tryImplementation(LogFactory::useLog4J2Logging);
    tryImplementation(LogFactory::useLog4JLogging);
    tryImplementation(LogFactory::useJdkLogging);
    tryImplementation(LogFactory::useNoLogging);
  }

  private LogFactory() {
    // disable construction
  }

  public static Log getLog(Class<?> aClass) {
    return getLog(aClass.getName());
  }

  public static Log getLog(String logger) {
    try {
      // 创建日志实例
      return logConstructor.newInstance(logger);
    } catch (Throwable t) {
      throw new LogException("Error creating logger for logger " + logger + ".  Cause: " + t, t);
    }
  }


  // 设置日志实现类，这里是自定义日志实现类，我们可以传入我们的实现
  // 可以发现这部分内容是有synchronized关键字，保证线程安全
  public static synchronized void useCustomLogging(Class<? extends Log> clazz) {
    //  为什么这部分需要加锁？是不是为了防止多线程的情况下创建多个实例？
    // 看了一下上面的static部分，好像是设置了单例？
    setImplementation(clazz);
  }

  public static synchronized void useSlf4jLogging() {
    // 这里就是对应的slf4j日志实现类
    setImplementation(org.apache.ibatis.logging.slf4j.Slf4jImpl.class);
  }

  public static synchronized void useCommonsLogging() {
    setImplementation(org.apache.ibatis.logging.commons.JakartaCommonsLoggingImpl.class);
  }

  public static synchronized void useLog4JLogging() {
    setImplementation(org.apache.ibatis.logging.log4j.Log4jImpl.class);
  }

  public static synchronized void useLog4J2Logging() {
    setImplementation(org.apache.ibatis.logging.log4j2.Log4j2Impl.class);
  }

  public static synchronized void useJdkLogging() {
    setImplementation(org.apache.ibatis.logging.jdk14.Jdk14LoggingImpl.class);
  }

  public static synchronized void useStdOutLogging() {
    setImplementation(org.apache.ibatis.logging.stdout.StdOutImpl.class);
  }

  public static synchronized void useNoLogging() {
    setImplementation(org.apache.ibatis.logging.nologging.NoLoggingImpl.class);
  }


  /**
   * 尝试实现一个日志实例
   * @param runnable 用来尝试实现日志实例的操作
   */
  private static void tryImplementation(Runnable runnable) {
    if (logConstructor == null) {
      try {
        // 执行尝试实现日志实例的操作， 看着是一个线程任务在run
        runnable.run();
      } catch (Throwable t) {
        // ignore
      }
    }
  }

  /**
   * 设置日志实现
   * @param implClass 日志实现类
   */
  private static void setImplementation(Class<? extends Log> implClass) {
    try {
      // 当前日志实现类的构造方法
      Constructor<? extends Log> candidate = implClass.getConstructor(String.class);
      // 尝试生成日志实现类的实例
      Log log = candidate.newInstance(LogFactory.class.getName());
      if (log.isDebugEnabled()) {
        log.debug("Logging initialized using '" + implClass + "' adapter.");
      }
      // 如果运行到这里，说明没有异常发生。则实例化日志实现类成功。
      logConstructor = candidate;
    } catch (Throwable t) {
      throw new LogException("Error setting Log implementation.  Cause: " + t, t);
    }
  }

}
