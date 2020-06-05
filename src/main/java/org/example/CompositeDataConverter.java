package org.example;

import org.apache.camel.Exchange;
import org.apache.camel.support.TypeConverterSupport;
import org.example.data.CompositeData;
import org.example.data.DetailData;
import org.example.data.MasterData;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CompositeDataConverter extends TypeConverterSupport {
  // region Fields

  // endregion

  // region Methods

  @Override public <T> T convertTo(Class<T> type, Exchange exchange, Object value) {

    if (value.getClass().equals(String.class)) {

      String[] splitLines = value.toString().split(System.lineSeparator());

      MasterData aMaster = convertMaster(splitLines[0]);

      List<DetailData> details = Arrays.stream(splitLines)
        .skip(1)
        .map(this::convertDetail)
        .collect(Collectors.toList());

      CompositeData aCompositeData = new CompositeData();
      aCompositeData.setMaster(aMaster);
      aCompositeData.setDetails(details);

      return (T) aCompositeData;
    }
    return null;
  }

  private MasterData convertMaster(String master) {
    String[] splitMaster = master.split("&");
    MasterData masterData = new MasterData();

    masterData.setId(splitMaster[0]);

    return masterData;
  }

  private DetailData convertDetail(String detail) {
    return new DetailData();
  }
  // endregion
}
