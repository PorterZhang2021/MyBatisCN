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
package org.apache.ibatis.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import org.apache.ibatis.reflection.invoker.GetFieldInvoker;
import org.apache.ibatis.reflection.invoker.Invoker;
import org.apache.ibatis.reflection.invoker.MethodInvoker;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

/**
 * @author Clinton Begin
 * 元类，看着应该是用反射器来操作的
 */
public class MetaClass {

  // 反射器工厂
  private final ReflectorFactory reflectorFactory;
  // 反射器
  private final Reflector reflector;

  private MetaClass(Class<?> type, ReflectorFactory reflectorFactory) {
    this.reflectorFactory = reflectorFactory;
    // 从反射器工厂中获取反射器
    this.reflector = reflectorFactory.findForClass(type);
  }

  public static MetaClass forClass(Class<?> type, ReflectorFactory reflectorFactory) {
    // 这里看着像是一个工厂方法，返回一个MetaClass对象
    return new MetaClass(type, reflectorFactory);
  }

  public MetaClass metaClassForProperty(String name) {
    // 从反射器获取属性类型
    Class<?> propType = reflector.getGetterType(name);
    return MetaClass.forClass(propType, reflectorFactory);
  }

  public String findProperty(String name) {
    // 构建一个属性名，并返回
    StringBuilder prop = buildProperty(name, new StringBuilder());
    // 这里如果有属性名就返回，没有就返回null
    return prop.length() > 0 ? prop.toString() : null;
  }

  public String findProperty(String name, boolean useCamelCaseMapping) {
    // 如果是开启驼峰命名转换，就把下划线去掉
    if (useCamelCaseMapping) {
      name = name.replace("_", "");
    }
    return findProperty(name);
  }

  public String[] getGetterNames() {
    // 能够读的属性列表，即有get方法的属性列表
    return reflector.getGetablePropertyNames();
  }

  public String[] getSetterNames() {
    return reflector.getSetablePropertyNames();
  }

  public Class<?> getSetterType(String name) {
    // 看着和BeanWrapper中的getSetterType方法类似
    // 最终是通过反射器获取到set的属性类型
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaClass metaProp = metaClassForProperty(prop.getName());
      return metaProp.getSetterType(prop.getChildren());
    } else {
      return reflector.getSetterType(prop.getName());
    }
  }

  public Class<?> getGetterType(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaClass metaProp = metaClassForProperty(prop);
      return metaProp.getGetterType(prop.getChildren());
    }
    // issue #506. Resolve the type inside a Collection Object
    return getGetterType(prop);
  }

  private MetaClass metaClassForProperty(PropertyTokenizer prop) {
    // 内部使用的一个构造方法，创建property类型的MetaClass对象
    Class<?> propType = getGetterType(prop);
    return MetaClass.forClass(propType, reflectorFactory);
  }

  private Class<?> getGetterType(PropertyTokenizer prop) {
    // 通过反射器获取属性类型
    Class<?> type = reflector.getGetterType(prop.getName());
    // 索引不为空，并且是集合类型
    if (prop.getIndex() != null && Collection.class.isAssignableFrom(type)) {
      Type returnType = getGenericGetterType(prop.getName());
      // 如果是泛型类型，就获取泛型的类型
      if (returnType instanceof ParameterizedType) {
        Type[] actualTypeArguments = ((ParameterizedType) returnType).getActualTypeArguments();
        // 只有一个泛型类型
        if (actualTypeArguments != null && actualTypeArguments.length == 1) {
          returnType = actualTypeArguments[0];
          if (returnType instanceof Class) {
            type = (Class<?>) returnType;
          } else if (returnType instanceof ParameterizedType) {
            type = (Class<?>) ((ParameterizedType) returnType).getRawType();
          }
        }
      }
    }
    return type;
  }

  private Type getGenericGetterType(String propertyName) {
    try {
      // 通过反射器获取getter方法
      Invoker invoker = reflector.getGetInvoker(propertyName);
      // 如果是MethodInvoker类型
      if (invoker instanceof MethodInvoker) {
        Field _method = MethodInvoker.class.getDeclaredField("method");
        _method.setAccessible(true);
        Method method = (Method) _method.get(invoker);
        return TypeParameterResolver.resolveReturnType(method, reflector.getType());
        // 如果是GetFieldInvoker类型
      } else if (invoker instanceof GetFieldInvoker) {
        Field _field = GetFieldInvoker.class.getDeclaredField("field");
        _field.setAccessible(true);
        Field field = (Field) _field.get(invoker);
        return TypeParameterResolver.resolveFieldType(field, reflector.getType());
      }
    } catch (NoSuchFieldException | IllegalAccessException ignored) {
    }
    return null;
  }

  public boolean hasSetter(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      if (reflector.hasSetter(prop.getName())) {
        MetaClass metaProp = metaClassForProperty(prop.getName());
        return metaProp.hasSetter(prop.getChildren());
      } else {
        return false;
      }
    } else {
      return reflector.hasSetter(prop.getName());
    }
  }

  public boolean hasGetter(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      if (reflector.hasGetter(prop.getName())) {
        MetaClass metaProp = metaClassForProperty(prop);
        return metaProp.hasGetter(prop.getChildren());
      } else {
        return false;
      }
    } else {
      return reflector.hasGetter(prop.getName());
    }
  }

  public Invoker getGetInvoker(String name) {
    // 通过反射器获取getter方法
    return reflector.getGetInvoker(name);
  }

  public Invoker getSetInvoker(String name) {
    return reflector.getSetInvoker(name);
  }

  private StringBuilder buildProperty(String name, StringBuilder builder) {
    // 构建一个PropertyTokenizer对象，然后通过反射器获取属性名
    PropertyTokenizer prop = new PropertyTokenizer(name);
    // 这里会看prop是否会有下一个属性
    if (prop.hasNext()) {
      // 通过反射器获取属性名
      String propertyName = reflector.findPropertyName(prop.getName());
      // 不为空，说明找到了属性名
      if (propertyName != null) {
        builder.append(propertyName);
        builder.append(".");
        MetaClass metaProp = metaClassForProperty(propertyName);
        // 看着这里是递归调用，直到没有下一个属性为止
        metaProp.buildProperty(prop.getChildren(), builder);
      }
    } else {
      // 否则直接获取属性名
      String propertyName = reflector.findPropertyName(name);
      if (propertyName != null) {
        builder.append(propertyName);
      }
    }
    return builder;
  }

  public boolean hasDefaultConstructor() {
    // 是否有默认构造函数
    return reflector.hasDefaultConstructor();
  }

}
