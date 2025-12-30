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

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

/**
 * @author Clinton Begin
 * 集合包装器，用于包装集合对象
 */
public class CollectionWrapper implements ObjectWrapper {

  // 这里是利用统一的Collection接口包裹了所有的集合对象
  private final Collection<Object> object;

  public CollectionWrapper(MetaObject metaObject, Collection<Object> object) {
    this.object = object;
  }

  @Override
  public Object get(PropertyTokenizer prop) {
    // 集合情况下，看着这个方法不支持
    throw new UnsupportedOperationException();
  }

  @Override
  public void set(PropertyTokenizer prop, Object value) {
    // 集合情况下，看着这个方法不支持
    throw new UnsupportedOperationException();
  }

  @Override
  public String findProperty(String name, boolean useCamelCaseMapping) {
    // 集合情况下，看着这个方法不支持
    throw new UnsupportedOperationException();
  }

  @Override
  public String[] getGetterNames() {
    // 集合情况下，看着这个方法不支持
    throw new UnsupportedOperationException();
  }

  @Override
  public String[] getSetterNames() {
    // 集合情况下，看着这个方法不支持
    throw new UnsupportedOperationException();
  }

  @Override
  public Class<?> getSetterType(String name) {
    // 集合情况下，看着这个方法不支持
    throw new UnsupportedOperationException();
  }

  @Override
  public Class<?> getGetterType(String name) {
    // 集合情况下，看着这个方法不支持
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasSetter(String name) {
    // 集合情况下，看着这个方法不支持
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasGetter(String name) {
    // 集合情况下，看着这个方法不支持
    throw new UnsupportedOperationException();
  }

  @Override
  public MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory) {
    // 集合情况下，看着这个方法不支持
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isCollection() {
    return true;
  }

  @Override
  public void add(Object element) {
    // 添加元素
    object.add(element);
  }

  @Override
  public <E> void addAll(List<E> element) {
    // 添加全部元素
    object.addAll(element);
  }

}
