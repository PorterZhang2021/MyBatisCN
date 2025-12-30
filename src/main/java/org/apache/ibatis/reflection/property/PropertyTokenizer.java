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
package org.apache.ibatis.reflection.property;

import java.util.Iterator;

/**
 * @author Clinton Begin
 * 属性标记器
 */

/**
 * 假设传入的为student[sId].name
 * 则各个属性得到以下结果
 *
 * 该属性标记器只能处理一级，即点后面的都作为children
 *
 * 本身情况下这个还是一个迭代器，可以迭代出下一级属性
 */
public class PropertyTokenizer implements Iterator<PropertyTokenizer> {

  // student
  private String name;
  // student[sId]
  private final String indexedName;
  // sId
  private String index;
  // name
  private final String children;

  public PropertyTokenizer(String fullname) {
    // student[sId].name 按 . 分割
    int delim = fullname.indexOf('.');
    if (delim > -1) {
      // student[sId]
      name = fullname.substring(0, delim);
      // name
      children = fullname.substring(delim + 1);
    } else {
      // student[sId] 没有 .
      name = fullname;
      children = null;
    }
    indexedName = name;
    // 找到[] 符号
    delim = name.indexOf('[');
    if (delim > -1) {
      // sId
      index = name.substring(delim + 1, name.length() - 1);
      // student
      name = name.substring(0, delim);
    }
  }

  public String getName() {
    return name;
  }

  public String getIndex() {
    return index;
  }

  public String getIndexedName() {
    return indexedName;
  }

  public String getChildren() {
    return children;
  }

  @Override
  public boolean hasNext() {
    return children != null;
  }

  @Override
  public PropertyTokenizer next() {
    return new PropertyTokenizer(children);
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Remove is not supported, as it has no meaning in the context of properties.");
  }
}
