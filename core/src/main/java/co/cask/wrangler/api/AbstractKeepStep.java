/*
 * Copyright © 2017 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.wrangler.api;

/**
 * An abstract class for {@link KeepStep}.
 */
public abstract class AbstractKeepStep extends AbstractStep
  implements KeepStep<Record, Record, String> {

  public AbstractKeepStep(int lineno, String detail) {
    super(lineno, detail);
  }

  @Override
  public void acceptOptimizerGraphBuilder(OptimizerGraphBuilder<Record, Record, String> optimizerGraphBuilder) {
    optimizerGraphBuilder.buildGraph(this);
  }
}
