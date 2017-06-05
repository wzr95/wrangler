/*
 * Copyright © 2016-2017 Cask Data, Inc.
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

package co.cask.wrangler.steps;

import co.cask.cdap.api.annotation.Description;
import co.cask.cdap.api.annotation.Name;
import co.cask.cdap.api.annotation.Plugin;
import co.cask.wrangler.api.AbstractStep;
import co.cask.wrangler.api.ErrorRecordException;
import co.cask.wrangler.api.PipelineContext;
import co.cask.wrangler.api.Record;
import co.cask.wrangler.api.StepException;
import co.cask.wrangler.api.Usage;
import co.cask.wrangler.steps.transformation.JexlHelper;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.JexlScript;
import org.apache.commons.jexl3.MapContext;

import java.util.List;

/**
 * A directive that defines a transient variable who's life-expectancy is only within the record.
 *
 * The value set as transient variable is available to all the directives after that. But, it's
 * not available beyond the input record.
 */
@Plugin(type = "udd")
@Name("set-variable")
@Usage("set-variable <variable> <expression>")
@Description("Sets the value for a transient variable for the record being processed.")
public class SetTransientVariable extends AbstractStep {
  private final String variable;
  private final String expression;
  private final JexlEngine engine;
  private final JexlScript script;

  public SetTransientVariable(int lineno, String detail, String variable, String expression) {
    super(lineno, detail);
    this.variable = variable;
    this.expression = expression;
    engine = JexlHelper.getEngine();
    script = engine.createScript(this.expression);
  }

  /**
   * Executes a wrangle step on single {@link Record} and return an array of wrangled {@link Record}.
   *
   * @param records List of input {@link Record} to be wrangled by this step.
   * @param context {@link PipelineContext} passed to each step.
   * @return Wrangled List of {@link Record}.
   */
  @Override
  public List<Record> execute(List<Record> records, PipelineContext context)
    throws StepException, ErrorRecordException {
    for (Record record : records) {
      // Move the fields from the record into the context.
      JexlContext ctx = new MapContext();
      ctx.set("this", record);
      for (int i = 0; i < record.length(); ++i) {
        ctx.set(record.getColumn(i), record.getValue(i));
      }

      // Transient variables are added.
      if (context != null) {
        for (String variable : context.getTransientStore().getVariables()) {
          ctx.set(variable, context.getTransientStore().get(variable));
        }
      }

      // Execution of the script / expression based on the record data
      // mapped into context.
      try {
        Object result = script.execute(ctx);
        context.getTransientStore().set(variable, result);
      } catch (JexlException e) {
        // Generally JexlException wraps the original exception, so it's good idea
        // to check if there is a inner exception, if there is wrap it in 'StepException'
        // else just print the error message.
        if (e.getCause() != null) {
          throw new StepException(toString() + " : " + e.getMessage(), e.getCause());
        } else {
          throw new StepException(toString() + " : " + e.getMessage());
        }
      } catch (NumberFormatException e) {
        throw new StepException(toString() + " : " + " type mismatch. Change type of constant " +
                                  "or convert to right data type using conversion functions available. Reason : " + e.getMessage());
      } catch (Exception e) {
        // We want to propogate this exception up!
        if (e instanceof ErrorRecordException) {
          throw e;
        }
        if (e.getCause() != null) {
          throw new StepException(toString() + " : " + e.getMessage(), e.getCause());
        } else {
          throw new StepException(toString() + " : " + e.getMessage());
        }
      }
    }
    return records;
  }
}