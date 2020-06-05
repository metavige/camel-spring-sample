package org.example.data;

import lombok.Data;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

@Data
@CsvRecord(separator = "&&&")
public class DetailData {

  @DataField(pos = 1, trim = true)
  private String id;
  @DataField(pos = 2, trim = true)
  private String name;
  @DataField(pos = 3, precision = 0)
  private float amount;
}
