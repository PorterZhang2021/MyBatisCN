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
package org.apache.ibatis.reflection;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.reflection.wrapper.BeanWrapper;
import org.apache.ibatis.reflection.wrapper.CollectionWrapper;
import org.apache.ibatis.reflection.wrapper.MapWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;

/**
 * @author Clinton Begin
 * todo 元Object对象，这里和MetaClass的区别在哪儿？暂时还不太知道
 */
public class MetaObject {
  // 原始对象
  private final Object originalObject;
  // 对象包装器
  private final ObjectWrapper objectWrapper;
  // 对象工厂
  private final ObjectFactory objectFactory;
  // 对象包装器工厂
  private final ObjectWrapperFactory objectWrapperFactory;
  // 反射工厂
  private final ReflectorFactory reflectorFactory;

  private MetaObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
    // 原始对象
    this.originalObject = object;
    // 对象工厂
    this.objectFactory = objectFactory;
    // 对象包装器工厂
    this.objectWrapperFactory = objectWrapperFactory;
    // 反射工厂
    this.reflectorFactory = reflectorFactory;

    // 这里判断是否有对象包装器，这里看着之前应该是有
    // 工厂实现逻辑，但是后面代码没有实现，而是直接new对象
    if (object instanceof ObjectWrapper) {
      this.objectWrapper = (ObjectWrapper) object;
    } else if (objectWrapperFactory.hasWrapperFor(object)) {
      this.objectWrapper = objectWrapperFactory.getWrapperFor(this, object);
    } else if (object instanceof Map) {
      this.objectWrapper = new MapWrapper(this, (Map) object);
    } else if (object instanceof Collection) {
      this.objectWrapper = new CollectionWrapper(this, (Collection) object);
    } else {
      this.objectWrapper = new BeanWrapper(this, object);
    }
  }

  public static MetaObject forObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
    // 如果对象为空，则返回一个空对象
    if (object == null) {
      return SystemMetaObject.NULL_META_OBJECT;
    } else {
      // 构建一个新的对象
      return new MetaObject(object, objectFactory, objectWrapperFactory, reflectorFactory);
    }
  }

  public ObjectFactory getObjectFactory() {
    return objectFactory;
  }

  public ObjectWrapperFactory getObjectWrapperFactory() {
    return objectWrapperFactory;
  }

  public ReflectorFactory getReflectorFactory() {
    return reflectorFactory;
  }

  public Object getOriginalObject() {
    return originalObject;
  }

  public String findProperty(String propName, boolean useCamelCaseMapping) {
    return objectWrapper.findProperty(propName, useCamelCaseMapping);
  }

  public String[] getGetterNames() {
    //  能够读的属性列表，即有get方法的属性列表
    return objectWrapper.getGetterNames();
  }

  public String[] getSetterNames() {
    // 能够写的属性列表，即有set方法的属性列表
    return objectWrapper.getSetterNames();
  }

  public Class<?> getSetterType(String name) {
    return objectWrapper.getSetterType(name);
  }

  public Class<?> getGetterType(String name) {
    return objectWrapper.getGetterType(name);
  }

  public boolean hasSetter(String name) {
    return objectWrapper.hasSetter(name);
  }

  public boolean hasGetter(String name) {
    return objectWrapper.hasGetter(name);
  }

  public Object getValue(String name) {
    // 这里看着又创建了一个新的
    PropertyTokenizer prop = new PropertyTokenizer(name);
    // 如果还有下层属性，这里就是用了迭代器，看是否下面还有属性
    if (prop.hasNext()) {
      // 看着这里应该是不太一样的，这里拿的应该是index里面的属性
      MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
      if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
        return null;
      } else {
        // 循环解析
        return metaValue.getValue(prop.getChildren());
      }
    } else {
      // 如果没有了这个时候就会直接获取属性值
      return objectWrapper.get(prop);
    }
  }

  public void setValue(String name, Object value) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
      if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
        if (value == null) {
          // don't instantiate child path if value is null
          return;
        } else {
          metaValue = objectWrapper.instantiatePropertyValue(name, prop, objectFactory);
        }
      }
      metaValue.setValue(prop.getChildren(), value);
    } else {
      objectWrapper.set(prop, value);
    }
  }

  public MetaObject metaObjectForProperty(String name) {
    Object value = getValue(name);
    return MetaObject.forObject(value, objectFactory, objectWrapperFactory, reflectorFactory);
  }

  public ObjectWrapper getObjectWrapper() {
    return objectWrapper;
  }

  public boolean isCollection() {
    return objectWrapper.isCollection();
  }

  public void add(Object element) {
    objectWrapper.add(element);
  }

  public <E> void addAll(List<E> list) {
    objectWrapper.addAll(list);
  }

}
