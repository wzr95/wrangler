/*
 * Copyright © 2016 Cask Data, Inc.
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
import co.cask.wrangler.api.RecipeContext;
import co.cask.wrangler.api.Row;
import co.cask.wrangler.api.DirectiveExecutionException;
import co.cask.wrangler.api.annotations.Usage;

import java.util.ArrayList;
import java.util.List;

/**
 * A Wrangler step for splitting a col into two additional columns based on a start and end.
 */
@Plugin(type = "directives")
@Name("indexsplit")
@Usage("indexsplit <source> <start> <end> <destination>")
@Description("[DEPRECATED] Use the 'split-to-columns' or 'parse-as-fixed-length' directives instead.")
public class IndexSplit extends AbstractDirective {
  // Name of the column to be split
  private String col;

  // Start and end index of the split
  private int start, end;

  // Destination column
  private String dest;

  public IndexSplit(int lineno, String detail, String col, int start, int end, String dest) {
    super(lineno, detail);
    this.col = col;
    this.start = start - 1; // Assumes the wrangle configuration starts @ 1
    this.end = end - 1;
    this.dest = dest;
  }

  /**
   * Splits column based on the start and end index.
   *
   * @param rows Input {@link Row} to be wrangled by this step.
   * @param context Specifies the context of the pipeline.
   * @return Transformed {@link Row} in which the 'col' value is lower cased.
   * @throws DirectiveExecutionException thrown when type of 'col' is not STRING.
   */
  @Override
  public List<Row> execute(List<Row> rows, RecipeContext context) throws DirectiveExecutionException {
    List<Row> results = new ArrayList<>();
    for (Row row : rows) {
      int idx = row.find(col);

      if (idx != -1) {
        String val = (String) row.getValue(idx);
        if (end > val.length() - 1) {
          end = val.length() - 1;
        }
        if (start < 0) {
          start = 0;
        }
        val = val.substring(start, end);
        row.add(dest, val);
      } else {
        throw new DirectiveExecutionException(
          col + " is not of type string in the row. Please check the wrangle configuration."
        );
      }
      results.add(row);
    }
    return results;
  }
}
