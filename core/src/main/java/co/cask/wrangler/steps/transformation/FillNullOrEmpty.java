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

package co.cask.wrangler.steps.transformation;

import co.cask.cdap.api.annotation.Description;
import co.cask.cdap.api.annotation.Name;
import co.cask.cdap.api.annotation.Plugin;
import co.cask.wrangler.api.AbstractDirective;
import co.cask.wrangler.api.DirectiveExecutionException;
import co.cask.wrangler.api.RecipeContext;
import co.cask.wrangler.api.Row;
import co.cask.wrangler.api.annotations.Usage;
import org.json.JSONObject;

import java.util.List;

/**
 * A step to fill null or empty column values with a fixed value.
 */
@Plugin(type = "directives")
@Name("fill-null-or-empty")
@Usage("fill-null-or-empty <column> <fixed-value>")
@Description("Fills a value of a column with a fixed value if it is either null or empty.")
public class FillNullOrEmpty extends AbstractDirective {
  private String column;
  private String value;

  public FillNullOrEmpty(int lineno, String detail, String column, String value) {
    super(lineno, detail);
    this.column = column;
    this.value = value;
  }

  /**
   * Fills the null or empty column (and missing) values with fixed value.
   *
   * @param rows Input {@link Row} to be wrangled by this step
   * @param context Specifies the context of the pipeline
   * @return Transformed {@link Row}
   * @throws DirectiveExecutionException thrown when type of 'col' is not STRING
   */
  @Override
  public List<Row> execute(List<Row> rows, RecipeContext context) throws DirectiveExecutionException {
    for (Row row : rows) {
      int idx = row.find(column);
      if (idx == -1) {
        row.add(column, value);
        continue;
      }
      Object object = row.getValue(idx);
      if (object == null) {
        row.setValue(idx, value);
      } else {
        if (object instanceof String) {
          if (((String) object).isEmpty()) {
            row.setValue(idx, value);
          }
        } else if (object instanceof JSONObject) {
          if (JSONObject.NULL.equals(object)) {
            row.setValue(idx, value);
          }
        }
      }
    }
    return rows;
  }
}
