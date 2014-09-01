/* Copyright 2009 - 2010 The Stajistics Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nars.util.meter.event;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import nars.util.meter.util.ServiceLifeCycle;

/**
 * A service for executing tasks.
 *
 * @author The Stajistics Project
 */
public interface TaskService extends Serializable, ServiceLifeCycle {

    void execute(Class<?> source, Runnable task);

    <T> Future<T> submit(Class<?> source, Callable<T> task);

}
