package org.example.data;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode()
@CsvRecord(separator = "&")
public class MasterData {

  @DataField(pos = 1, trim = true)
  private String id;
  @DataField(pos = 2, trim = true)
  private String name;
  @DataField(pos = 3, pattern = "yyyy/MM/dd")
  private Date created;
}
