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
package org.apache.ibatis.reflection.wrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

/**
 * @author Clinton Begin
 * Map包装器，用于包装相关的map对象
 */
public class MapWrapper extends BaseWrapper {

  private final Map<String, Object> map;

  public MapWrapper(MetaObject metaObject, Map<String, Object> map) {
    super(metaObject);
    this.map = map;
  }

  @Override
  public Object get(PropertyTokenizer prop) {
    if (prop.getIndex() != null) {
      // 多个层级，递归解析
      // todo 这里需要debug一下，要不然不太知道这个map的作用是干嘛的
      Object collection = resolveCollection(prop, map);
      return getCollectionValue(prop, collection);
    } else {
      // 单一层级
      return map.get(prop.getName());
    }
  }

  @Override
  public void set(PropertyTokenizer prop, Object value) {
    if (prop.getIndex() != null) {
      // 多个层级，递归解析
      // todo 这个地方同样需要debug一下，map的作用是什么
      Object collection = resolveCollection(prop, map);
      setCollectionValue(prop, collection, value);
    } else {
      map.put(prop.getName(), value);
    }
  }

  @Override
  public String findProperty(String name, boolean useCamelCaseMapping) {
    // todo 奇葩，这个地方的bool是干嘛用的？看着没有使用到
    return name;
  }

  @Override
  public String[] getGetterNames() {
    // 这里看着就是把map的key转成字符串数组
    return map.keySet().toArray(new String[map.keySet().size()]);
  }

  @Override
  public String[] getSetterNames() {
    // todo 与上面一样，把map的key转成字符串数组，但这里变成了SetterNames
    // todo 看着两个应该是互斥的，也就是说包装的map，只可能出现有getter或者setter，不可能同时存在
    return map.keySet().toArray(new String[map.keySet().size()]);
  }

  @Override
  public Class<?> getSetterType(String name) {
    // 这个部分和之前类似就是递归解析，获取setter的类型
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
      if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
        return Object.class;
      } else {
        return metaValue.getSetterType(prop.getChildren());
      }
    } else {
      if (map.get(name) != null) {
        return map.get(name).getClass();
      } else {
        return Object.class;
      }
    }
  }

  @Override
  public Class<?> getGetterType(String name) {
    // 这个部分和之前类似就是递归解析，获取getter的类型
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
      if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
        return Object.class;
      } else {
        return metaValue.getGetterType(prop.getChildren());
      }
    } else {
      if (map.get(name) != null) {
        return map.get(name).getClass();
      } else {
        return Object.class;
      }
    }
  }

  @Override
  public boolean hasSetter(String name) {
    // 这里判断是否有setter，这里看着是直接返回true，没有做任何判断
    // todo 这里为什么直接返回true，也就是说默认是setter的？
    // todo 那不是和我上面的想法矛盾了，实际上它可以同时拥有setter和getter
    return true;
  }

  @Override
  public boolean hasGetter(String name) {
    // 这里看着就是递归解析，判断是否有getter
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      if (map.containsKey(prop.getIndexedName())) {
        MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
        if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
          return true;
        } else {
          return metaValue.hasGetter(prop.getChildren());
        }
      } else {
        return false;
      }
    } else {
      return map.containsKey(prop.getName());
    }
  }

  @Override
  public MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory) {
    // 这里就是进行初始化PropertyValue的操作
    HashMap<String, Object> map = new HashMap<>();
    set(prop, map);
    return MetaObject.forObject(map, metaObject.getObjectFactory(), metaObject.getObjectWrapperFactory(), metaObject.getReflectorFactory());
  }

  @Override
  public boolean isCollection() {
    // 是否是集合，这里是false
    // 这里设置的时候用接口做了契约
    // 这个应该是所有的都要用，所以对于Map这个，重写成了false
    return false;
  }

  @Override
  public void add(Object element) {
    // 这里就是添加元素的操作，由于是mapWrapper，所以重写了，不支持这个操作
    // 所以接口契约层面上，如果发现有些方法不需要，可以在接口写上default实现，抛出异常
    // 或者在实现类中重写，抛出异常
    throw new UnsupportedOperationException();
  }

  // 通过这部分就可以理解一些接口契约的写法了，实际契约内容写多了
  // 影响并没有那么大，可以将其改成空实现，或者抛出异常，或者写上注释就可以了
  // 如果要对这种契约做重构也简单，就是先新增不做拆分，等所有的拆分功能都替换实现了
  // 这个时候就可以移除旧有的这个契约上的方法了
  @Override
  public <E> void addAll(List<E> element) {
    throw new UnsupportedOperationException();
  }

}
