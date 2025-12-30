/**
 *    Copyright 2009-2017 the original author or authors.
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
package org.apache.ibatis.reflection.wrapper;

import java.util.List;

import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectionException;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.invoker.Invoker;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

/**
 * @author Clinton Begin
 * 这个类是Bean的包装器，继承自BaseWrapper，实现了ObjectWrapper接口。
 */
public class BeanWrapper extends BaseWrapper {

  // 被包装的对象
  private final Object object;
  // 被包装对象所属类的元类
  private final MetaClass metaClass;

  public BeanWrapper(MetaObject metaObject, Object object) {
    super(metaObject);
    this.object = object;
    // 看着应该是获取包装对象的元类
    this.metaClass = MetaClass.forClass(object.getClass(), metaObject.getReflectorFactory());
  }

  // todo 对于这里的get操作还是要看一下的，需要debug
  @Override
  public Object get(PropertyTokenizer prop) {
    // 这里看着好像是要看Index是否不为null，如果是null，调用的别的方法
    if (prop.getIndex() != null) {
      // 不是单一属性，而是有数组，看着会把数组解析出来
      Object collection = resolveCollection(prop, object);
      // 这里的getCollectionValue方法，看着是获取集合中的值
      return getCollectionValue(prop, collection);
    } else {
      return getBeanProperty(prop, object);
    }
  }

  // todo 这里的set操作同理
  @Override
  public void set(PropertyTokenizer prop, Object value) {
    // 这里看着好像是要看Index是否不为null，如果是null，调用的别的方法
    if (prop.getIndex() != null) {
      // 不是单一属性，而是有数组，看着会把数组解析出来
      Object collection = resolveCollection(prop, object);
      // 这里的setCollectionValue方法，看着是设置集合中的值
      setCollectionValue(prop, collection, value);
    } else {
      setBeanProperty(prop, object, value);
    }
  }

  @Override
  public String findProperty(String name, boolean useCamelCaseMapping) {
    // 这里看着是到对应的元类中去找属性
    return metaClass.findProperty(name, useCamelCaseMapping);
  }

  @Override
  public String[] getGetterNames() {
    // 最终返回的是有get方法的属性列表
    return metaClass.getGetterNames();
  }

  @Override
  public String[] getSetterNames() {
    // 最终返回的是有set方法的属性列表
    return metaClass.getSetterNames();
  }

  @Override
  public Class<?> getSetterType(String name) {
    // todo 这里是只要涉及到类相关的操作，都需要使用PropertyTokenizer来解析属性名吗？
    // todo 看着getSetterType，getGetterType，hasSetter，hasGetter都是通过PropertyTokenizer来解析属性名
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      // 这里看着是先获取到属性名，然后获取到属性类型
      MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
      // metaValue如何和null_meta_object相似，这个时候通过metaClass.getSetterType(name)来获取属性类型
      if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
        return metaClass.getSetterType(name);
      } else {
        return metaValue.getSetterType(prop.getChildren());
      }
    } else {
      // 默认走的是元类
      return metaClass.getSetterType(name);
    }
  }

  @Override
  public Class<?> getGetterType(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
      if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
        return metaClass.getGetterType(name);
      } else {
        return metaValue.getGetterType(prop.getChildren());
      }
    } else {
      return metaClass.getGetterType(name);
    }
  }

  @Override
  public boolean hasSetter(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      if (metaClass.hasSetter(prop.getIndexedName())) {
        MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
        if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
          return metaClass.hasSetter(name);
        } else {
          return metaValue.hasSetter(prop.getChildren());
        }
      } else {
        return false;
      }
    } else {
      return metaClass.hasSetter(name);
    }
  }

  @Override
  public boolean hasGetter(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      if (metaClass.hasGetter(prop.getIndexedName())) {
        MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
        if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
          return metaClass.hasGetter(name);
        } else {
          return metaValue.hasGetter(prop.getChildren());
        }
      } else {
        return false;
      }
    } else {
      return metaClass.hasGetter(name);
    }
  }

  @Override
  public MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory) {
    // 遇到空属性就先 new 一个对象放进去，再把新的 MetaObject 交给你继续操作
    MetaObject metaValue;
    Class<?> type = getSetterType(prop.getName());
    try {
      Object newObject = objectFactory.create(type);
      metaValue = MetaObject.forObject(newObject, metaObject.getObjectFactory(), metaObject.getObjectWrapperFactory(), metaObject.getReflectorFactory());
      set(prop, newObject);
    } catch (Exception e) {
      throw new ReflectionException("Cannot set value of property '" + name + "' because '" + name + "' is null and cannot be instantiated on instance of " + type.getName() + ". Cause:" + e.toString(), e);
    }
    return metaValue;
  }

  // 通过调用getter方法，获取对象属性
  private Object getBeanProperty(PropertyTokenizer prop, Object object) {
    try {
      // 获取反射的method
      Invoker method = metaClass.getGetInvoker(prop.getName());
      try {
        // 进行反射调用，这里是没有参数的反射调用
        return method.invoke(object, NO_ARGUMENTS);
      } catch (Throwable t) {
        throw ExceptionUtil.unwrapThrowable(t);
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable t) {
      throw new ReflectionException("Could not get property '" + prop.getName() + "' from " + object.getClass() + ".  Cause: " + t.toString(), t);
    }
  }

  private void setBeanProperty(PropertyTokenizer prop, Object object, Object value) {
    try {
      // 获取反射的method， 进行对应的set反射操作
      Invoker method = metaClass.getSetInvoker(prop.getName());
      Object[] params = {value};
      try {
        method.invoke(object, params);
      } catch (Throwable t) {
        throw ExceptionUtil.unwrapThrowable(t);
      }
    } catch (Throwable t) {
      throw new ReflectionException("Could not set property '" + prop.getName() + "' of '" + object.getClass() + "' with value '" + value + "' Cause: " + t.toString(), t);
    }
  }

  @Override
  public boolean isCollection() {
    return false;
  }

  @Override
  public void add(Object element) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <E> void addAll(List<E> list) {
    throw new UnsupportedOperationException();
  }

}
